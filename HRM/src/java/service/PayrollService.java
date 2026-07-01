package service;

import dao.EmployeeDAO;
import dao.PayrollDAO;
import dao.PayrollConfigDAO;
import dao.PermissionDAO;
import dao.RoleDAO;
import dal.DBContext;
import dto.EmployeeDetailDTO;
import dto.PayrollDetailDTO;
import dto.PayrollPreviewDTO;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Payroll;
import model.PayrollDeductionRule;
import model.PayrollTaxBracket;
import model.User;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class PayrollService {

    public static final int STATUS_PENDING_APPROVAL = 0;
    public static final int STATUS_APPROVED = 1;

    public static final String PERMISSION_VIEW_ALL_SALARY = "VIEW_ALL_SALARY";
    public static final String PERMISSION_VIEW_OWN_SALARY = "VIEW_OWN_SALARY";
    public static final String PERMISSION_APPROVE_PAYROLL = "APPROVE_PAYROLL";
    public static final String PERMISSION_EXPORT_PAYROLL = "EXPORT_PAYROLL";

    private static final Logger LOGGER = Logger.getLogger(PayrollService.class.getName());
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal MINUTES_PER_HOUR = new BigDecimal("60");
    private final DBContext dbContext;
    private final PayrollDAO payrollDAO;
    private final PayrollConfigDAO payrollConfigDAO;
    private final EmployeeDAO employeeDAO;
    private final PermissionDAO permissionDAO;
    private final RoleDAO roleDAO;
    private final AuditLogService auditLogService;
    private final AttendanceClosingService attendanceClosingService;

    public PayrollService() {
        this.dbContext = new DBContext();
        this.payrollDAO = new PayrollDAO();
        this.payrollConfigDAO = new PayrollConfigDAO();
        this.employeeDAO = new EmployeeDAO();
        this.permissionDAO = new PermissionDAO();
        this.roleDAO = new RoleDAO();
        this.auditLogService = new AuditLogService();
        this.attendanceClosingService = new AttendanceClosingService();
    }

    // Permissions
    public boolean canViewOwnSalary(User user) {
        return user != null && getPermissions(user).contains(PERMISSION_VIEW_OWN_SALARY);
    }

    public boolean canViewAllSalary(User user) {
        return user != null
                && isHrStaff(user)
                && getPermissions(user).contains(PERMISSION_VIEW_ALL_SALARY);
    }

    public boolean canApprovePayroll(User user) {
        if (user == null || !canViewAllSalary(user)) {
            return false;
        }
        return getPermissions(user).contains(PERMISSION_APPROVE_PAYROLL);
    }

    public boolean canExportPayroll(User user) {
        return user != null
                && canViewAllSalary(user)
                && getPermissions(user).contains(PERMISSION_EXPORT_PAYROLL);
    }

    public PayrollPreviewDTO getOwnPayroll(User user, int year, int month) {
        if (!canViewOwnSalary(user)) {
            return null;
        }
        EmployeeDetailDTO me = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (me == null) {
            return null;
        }
        return getSavedPayrollPreviewForEmployee(me.getEmployeeId(), year, month);
    }

    public List<PayrollPreviewDTO> getAllPayrollForHr(User user, int year, int month, Integer departmentId) {
        if (!canViewAllSalary(user)) {
            return new ArrayList<>();
        }
        Date start = toPeriodStart(year, month);
        Date end = toPeriodEnd(year, month);
        List<PayrollPreviewDTO> saved = payrollDAO.getPayrollPreviews(start, end, departmentId, null);
        markInvalidSavedPayrolls(saved);
        saved.addAll(getPayrollGenerationIssues(year, month, departmentId, saved));
        return saved;
    }

    private void markInvalidSavedPayrolls(List<PayrollPreviewDTO> payrolls) {
        for (PayrollPreviewDTO preview : payrolls) {
            Payroll payroll = preview.getPayroll();
            if (payroll == null) {
                continue;
            }
            if ((payroll.getNetSalary() != null && payroll.getNetSalary().signum() < 0)
                    || (payroll.getBaseSalary() != null && payroll.getBaseSalary().signum() < 0)) {
                preview.setGenerationError("Bảng lương chưa thể tạo ra. Cần kiểm tra lại dữ liệu nguồn...");
            }
        }
    }

    public PayrollPreviewDTO getPayrollDetailForHr(User user, int payrollId) {
        if (!canViewAllSalary(user)) {
            return null;
        }
        PayrollPreviewDTO preview = payrollDAO.getPayrollPreviewById(payrollId);
        if (preview != null) {
            enrichSavedPayrollPreview(preview);
        }
        return preview;
    }

    public PayrollPreviewDTO getPayrollDetail(User user, int payrollId) {
        if (user == null) {
            return null;
        }
        if (canViewAllSalary(user)) {
            return getPayrollDetailForHr(user, payrollId);
        }
        if (!canViewOwnSalary(user)) {
            return null;
        }
        EmployeeDetailDTO employee = employeeDAO.getEmployeeByUserId(user.getUserId());
        PayrollPreviewDTO preview = payrollDAO.getPayrollPreviewById(payrollId);
        if (employee == null || preview == null || preview.getEmployeeId() != employee.getEmployeeId()) {
            return null;
        }
        enrichSavedPayrollPreview(preview);
        return preview;
    }

    // Approval workflow
    public int approveAllPayrollForPeriod(User user, int year, int month, Integer departmentId) {
        if (!canApprovePayroll(user)) {
            return 0;
        }
        Date start = toPeriodStart(year, month);
        Date end = toPeriodEnd(year, month);

        int approvedCount = payrollDAO.approveAllPendingPayroll(start, end, departmentId,
                user.getUserId(), null);

        auditPayroll(user, "APPROVE_ALL_PAYROLL", null, "status=0",
                "status=1; approvedBy=" + user.getUserId()
                + "; period=" + String.format("%04d-%02d", year, month)
                + "; departmentId=" + departmentId
                + "; approvedCount=" + approvedCount, "SUCCESS");
        return approvedCount;
    }

    public int countPendingApprovalForPeriod(User user, int year, int month, Integer departmentId) {
        if (!canApprovePayroll(user)) {
            return 0;
        }
        Date start = toPeriodStart(year, month);
        Date end = toPeriodEnd(year, month);
        return payrollDAO.countPendingApproval(start, end, departmentId, null);
    }

    // Export
    public void exportPayrollWorkbook(User user, int year, int month, Integer departmentId, OutputStream out)
            throws IOException {
        boolean allowed = canExportPayroll(user);
        List<PayrollPreviewDTO> payrolls = allowed
                ? getAllPayrollForHr(user, year, month, departmentId)
                : new ArrayList<>();
        int exportedRows = 0;
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Payroll " + month + "-" + year);
            String[] headers = payrollHeaders();
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            int rowIndex = 1;
            for (PayrollPreviewDTO preview : payrolls) {
                if (preview.isGenerationBlocked() || preview.getPayroll() == null) {
                    continue;
                }
                Payroll p = preview.getPayroll();
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(preview.getEmployeeCode());
                row.createCell(1).setCellValue(preview.getFullName());
                row.createCell(2).setCellValue(preview.getDepartmentName());
                row.createCell(3).setCellValue(preview.getPositionName());
                row.createCell(4).setCellValue(p.getWorkingDays());
                setNumeric(row, 5, p.getHoursWorked());
                setNumeric(row, 6, p.getBaseSalary());
                setNumeric(row, 7, p.getAllowance());
                setNumeric(row, 8, p.getBonus());
                setNumeric(row, 9, p.getOvertimePay());
                setNumeric(row, 10, p.getPenalty());
                setNumeric(row, 11, p.getGrossSalary());
                setNumeric(row, 12, p.getInsuranceDeduction());
                setNumeric(row, 13, p.getPersonalIncomeTax());
                setNumeric(row, 14, p.getNetSalary());
                row.createCell(15).setCellValue(p.getStatus());
                row.createCell(16).setCellValue(p.getNote() == null ? "" : p.getNote());
                exportedRows++;
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
        }
        auditPayroll(user, "EXPORT_PAYROLL", null, null,
                "period=" + String.format("%04d-%02d", year, month)
                + "; departmentId=" + departmentId
                + "; rows=" + exportedRows, allowed ? "SUCCESS" : "DENIED");
    }

    // Generate
    public List<PayrollPreviewDTO> generatePayrollForAll(int year, int month, Integer departmentId, boolean save) {
        List<PayrollPreviewDTO> result = new ArrayList<>();
        try (Connection conn = dbContext.getConnection()) {
            List<EmployeePayrollBase> employees = getActiveEmployeesWithContracts(conn, year, month, departmentId);
            for (EmployeePayrollBase employee : employees) {
                PayrollPreviewDTO preview = calculatePayroll(conn, employee, year, month);
                if (save && preview.getPayroll() != null && !preview.isGenerationBlocked()) {
                    int payrollId = saveGeneratedPayrollIfEditable(preview.getPayroll());
                    preview.getPayroll().setPayrollId(payrollId);
                }
                result.add(preview);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot generate payroll for period " + year + "-" + month, e);
        }
        return result;
    }

    public PayrollPreviewDTO generatePayrollForEmployee(int employeeId, int year, int month, boolean save) {
        try (Connection conn = dbContext.getConnection()) {
            EmployeePayrollBase employee = getActiveEmployeeWithContract(conn, employeeId, year, month);
            if (employee == null) {
                return null;
            }
            PayrollPreviewDTO preview = calculatePayroll(conn, employee, year, month);
            if (save && preview.getPayroll() != null && !preview.isGenerationBlocked()) {
                int payrollId = saveGeneratedPayrollIfEditable(preview.getPayroll());
                preview.getPayroll().setPayrollId(payrollId);
            }
            return preview;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot generate payroll for employeeId: " + employeeId, e);
        }
        return null;
    }

    public int saveGeneratedPayrollForPeriod(int year, int month, Integer departmentId) {
        // Chặn cứng: chỉ lưu bảng lương khi bảng chấm công đã được BA chốt (LOCKED).
        boolean locked = departmentId == null
                ? attendanceClosingService.isPeriodLocked(year, month)
                : attendanceClosingService.isDepartmentLocked(year, month, departmentId);
        if (!locked) {
            LOGGER.log(Level.WARNING, "Refuse to generate payroll: attendance period {0}-{1} dept {2} is not LOCKED.",
                    new Object[]{year, month, departmentId});
            return 0;
        }
        int saved = 0;
        for (PayrollPreviewDTO preview : generatePayrollForAll(year, month, departmentId, true)) {
            if (preview.getPayroll() != null && !preview.isGenerationBlocked()) {
                saved++;
            }
        }
        return saved;
    }

    private int saveGeneratedPayrollIfEditable(Payroll payroll) {
        Payroll existing = payrollDAO.getPayrollByEmployeeAndPeriod(
                payroll.getEmployeeId(), payroll.getPeriodStart(), payroll.getPeriodEnd());
        if (existing != null && existing.getStatus() != STATUS_PENDING_APPROVAL) {
            return existing.getPayrollId();
        }
        return payrollDAO.saveOrUpdatePayroll(payroll);
    }

    // Tính lương
    private PayrollPreviewDTO calculatePayroll(Connection conn, EmployeePayrollBase employee, int year, int month)
            throws SQLException {

        if (employee.contractSalary == null || employee.contractSalary.signum() <= 0) {
            return buildGenerationErrorPreview(employee, "Chưa có hợp đồng active hoặc lương hợp đồng hợp lệ.");
        }

        Date periodStart = toPeriodStart(year, month);
        Date periodEnd = toPeriodEnd(year, month);
        PayrollRuntimeConfig config;
        try {
            config = loadPayrollRuntimeConfig();
        } catch (IllegalStateException e) {
            return buildGenerationErrorPreview(employee, e.getMessage());
        }

        YearMonth ym = YearMonth.of(year, month);
        int standardWorkingDays = countStandardWorkingDays(ym);

        if (standardWorkingDays <= 0) {
            return buildGenerationErrorPreview(employee, "Tháng lương không có ngày làm việc chuẩn.");
        }

        BigDecimal dailyRate = divideMoney(employee.contractSalary, new BigDecimal(standardWorkingDays));
        BigDecimal minuteRate = divideMoney(dailyRate, config.workingHoursPerDay.multiply(MINUTES_PER_HOUR));

        AttendanceSummary attendance
                = getAttendanceSummary(conn, employee.employeeId, year, month, dailyRate, minuteRate, config);

        if (attendance.recordCount == 0) {
            return buildGenerationErrorPreview(employee, "Chưa có dữ liệu chấm công trong tháng lương này.");
        }

        OvertimeSummary overtime
                = getOvertimeSummary(conn, employee.employeeId, year, month, dailyRate, config);

        BigDecimal attendanceBonus = attendance.lateMinutes == 0
                && attendance.unauthorizedAbsentDays == 0
                        ? employee.contractSalary.multiply(config.attendanceBonusRate)
                        : ZERO;

        BigDecimal baseSalary
                = dailyRate.multiply(new BigDecimal(attendance.paidWorkingDays));

        BigDecimal bonus = attendanceBonus;

        // Penalty dùng để hiển thị:
        // - latePenalty: phạt đi muộn, có trừ thật vào lương
        // - unauthorizedAbsentPenalty: tiền mất do nghỉ không phép, chỉ hiển thị
        BigDecimal penalty = attendance.latePenalty
                .add(attendance.unauthorizedAbsentPenalty);

        // Gross chỉ trừ latePenalty.
        // Không trừ unauthorizedAbsentPenalty nữa vì nghỉ không phép đã làm giảm paidWorkingDays.
        BigDecimal grossSalary = baseSalary
                .add(bonus)
                .add(overtime.overtimePay)
                .subtract(attendance.latePenalty);

        BigDecimal preTaxDeductions = calculateConfiguredDeductions(
                employee.contractSalary, grossSalary, ZERO, config.deductionRules, true);

        BigDecimal familyAllowance = calculateFamilyAllowance(
                config.personalAllowance, employee.dependentCount, config.dependentAllowance);
        BigDecimal taxableIncome = grossSalary
                .subtract(preTaxDeductions)
                .subtract(familyAllowance);

        if (taxableIncome.signum() < 0) {
            taxableIncome = ZERO;
        }

        BigDecimal personalIncomeTax = calculatePersonalIncomeTax(taxableIncome, config.taxBrackets);
        BigDecimal postTaxDeductions = calculateConfiguredDeductions(
                employee.contractSalary, grossSalary, taxableIncome, config.deductionRules, false);
        BigDecimal configuredDeductions = preTaxDeductions.add(postTaxDeductions);

        BigDecimal netSalary = grossSalary
                .subtract(configuredDeductions)
                .subtract(personalIncomeTax);

        if (netSalary.signum() < 0) {
            return buildGenerationErrorPreview(employee, "Dữ liệu chưa thể tạo ra. Cần kiểm tra lại dữ liệu nguồn...");
        }

        Payroll payroll = new Payroll();
        payroll.setPeriodStart(periodStart);
        payroll.setPeriodEnd(periodEnd);
        payroll.setEmployeeId(employee.employeeId);
        payroll.setPositionId(employee.positionId);
        payroll.setDepartmentId(employee.departmentId);
        payroll.setWorkingDays(attendance.paidWorkingDays);
        payroll.setHoursWorked(scale(attendance.hoursWorked));
        payroll.setBaseSalary(scale(baseSalary));
        payroll.setAllowance(ZERO);
        payroll.setBonus(scale(bonus));
        payroll.setOvertimePay(scale(overtime.overtimePay));
        payroll.setPenalty(scale(penalty));
        payroll.setGrossSalary(scale(grossSalary));
        payroll.setInsuranceDeduction(scale(configuredDeductions));
        payroll.setPersonalIncomeTax(scale(personalIncomeTax));
        payroll.setNetSalary(scale(netSalary));
        payroll.setStatus(STATUS_PENDING_APPROVAL);
        payroll.setNote(buildPayrollNote(attendance, overtime, employee.dependentCount, familyAllowance));

        PayrollPreviewDTO preview = new PayrollPreviewDTO();
        preview.setPayroll(payroll);
        preview.setEmployeeCode(employee.employeeCode);
        preview.setFullName(employee.fullName);
        preview.setDepartmentName(employee.departmentName);
        preview.setPositionName(employee.positionName);
        preview.setContractSalary(employee.contractSalary);
        preview.setDailyRate(scale(dailyRate));
        preview.setHourlyRate(scale(divideMoney(dailyRate, config.workingHoursPerDay)));
        preview.setMinuteRate(scale(minuteRate));
        preview.setStandardWorkingDays(standardWorkingDays);
        preview.setPaidLeaveDays(attendance.paidLeaveDays);
        preview.setUnpaidLeaveDays(attendance.unpaidLeaveDays);
        preview.setUnauthorizedAbsentDays(attendance.unauthorizedAbsentDays);
        preview.setLateMinutes(attendance.lateMinutes);
        preview.setLatePenaltyBlocks(attendance.latePenaltyBlocks);
        preview.setLatePenaltyMinutes(attendance.latePenaltyMinutes);
        preview.setOvertimeHours(scale(overtime.overtimeHours));
        preview.setAttendanceBonus(scale(attendanceBonus));
        preview.setLatePenalty(scale(attendance.latePenalty));
        preview.setUnauthorizedAbsentPenalty(scale(attendance.unauthorizedAbsentPenalty));
        preview.setPersonalAllowance(scale(config.personalAllowance));
        preview.setDependentCount(employee.dependentCount);
        preview.setFamilyAllowance(scale(familyAllowance));
        preview.setDependentAllowance(scale(config.dependentAllowance));
        preview.setTaxableIncome(scale(taxableIncome));
        preview.setTotalDeduction(scale(attendance.latePenalty.add(configuredDeductions).add(personalIncomeTax)));
        preview.setDetails(buildDetails(payroll, preview));

        return preview;
    }

    private void auditPayroll(User user, String action, Integer recordId, String oldValue,
            String newValue, String status) {
        Integer userId = user == null ? null : user.getUserId();
        auditLogService.logAsync(userId, action, "Payroll", recordId,
                oldValue, newValue, null, null, status);
    }

    private PayrollPreviewDTO getSavedPayrollPreviewForEmployee(int employeeId, int year, int month) {
        Date start = toPeriodStart(year, month);
        Date end = toPeriodEnd(year, month);
        List<PayrollPreviewDTO> saved = payrollDAO.getPayrollPreviews(start, end, null, null);
        for (PayrollPreviewDTO preview : saved) {
            if (preview.getEmployeeId() == employeeId) {
                enrichSavedPayrollPreview(preview);
                return preview;
            }
        }
        return null;
    }

    private List<PayrollPreviewDTO> getPayrollGenerationIssues(int year, int month, Integer departmentId,
            List<PayrollPreviewDTO> savedPayrolls) {
        List<PayrollPreviewDTO> issues = new ArrayList<>();
        Set<Integer> savedEmployeeIds = new HashSet<>();
        for (PayrollPreviewDTO saved : savedPayrolls) {
            if (saved.getEmployeeId() > 0) {
                savedEmployeeIds.add(saved.getEmployeeId());
            }
        }

        StringBuilder sql = new StringBuilder(
                "SELECT e.employeeId, e.employeeCode, e.positionId, e.departmentId, e.dependentCount, "
                + "u.fullName, d.departmentName, p.positionName, ec.salary, "
                + "(SELECT COUNT(*) FROM Attendance a "
                + " WHERE a.employeeId = e.employeeId AND YEAR(a.workDate) = ? AND MONTH(a.workDate) = ?) AS attendanceCount "
                + "FROM Employees e "
                + "JOIN Users u ON u.userId = e.userId "
                + "LEFT JOIN Departments d ON d.departmentId = e.departmentId "
                + "LEFT JOIN Positions p ON p.positionId = e.positionId "
                + "LEFT JOIN Employment_Contracts ec ON ec.contractId = ( "
                + "    SELECT ec2.contractId FROM Employment_Contracts ec2 "
                + "    WHERE ec2.employeeId = e.employeeId AND ec2.status = 1 "
                + "      AND ec2.startDate <= ? "
                + "      AND (ec2.endDate IS NULL OR ec2.endDate >= ?) "
                + "    ORDER BY ec2.contractId DESC LIMIT 1 "
                + ") "
                + "WHERE e.status = 1 ");
        if (departmentId != null) {
            sql.append("AND e.departmentId = ? ");
        }
        sql.append("ORDER BY d.departmentName, e.employeeCode");

        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            ps.setDate(3, toPeriodEnd(year, month));
            ps.setDate(4, toPeriodStart(year, month));
            if (departmentId != null) {
                ps.setInt(5, departmentId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int employeeId = rs.getInt("employeeId");
                    if (savedEmployeeIds.contains(employeeId)) {
                        continue;
                    }
                    BigDecimal salary = rs.getBigDecimal("salary");
                    int attendanceCount = rs.getInt("attendanceCount");
                    EmployeePayrollBase employee = new EmployeePayrollBase();
                    employee.employeeId = employeeId;
                    employee.employeeCode = rs.getString("employeeCode");
                    employee.positionId = rs.getInt("positionId");
                    int deptId = rs.getInt("departmentId");
                    employee.departmentId = rs.wasNull() ? null : deptId;
                    employee.dependentCount = Math.max(0, rs.getInt("dependentCount"));
                    employee.fullName = rs.getNString("fullName");
                    employee.departmentName = rs.getNString("departmentName");
                    employee.positionName = rs.getString("positionName");
                    employee.contractSalary = salary;
                    String error = null;
                    if (salary == null || salary.signum() <= 0) {
                        error = "Chưa có hợp đồng active hoặc lương hợp đồng hợp lệ.";
                    } else if (attendanceCount == 0) {
                        error = "Chưa có dữ liệu chấm công trong tháng lương này.";
                    }
                    if (error != null) {
                        issues.add(buildGenerationErrorPreview(employee, error));
                    } else {
                        PayrollPreviewDTO preview = calculatePayroll(conn, employee, year, month);
                        if (preview.isGenerationBlocked()) {
                            issues.add(preview);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get payroll generation issues", e);
        }
        return issues;
    }

    private PayrollPreviewDTO buildGenerationErrorPreview(EmployeePayrollBase employee, String error) {
        PayrollPreviewDTO preview = new PayrollPreviewDTO();
        Payroll payroll = new Payroll();
        payroll.setEmployeeId(employee.employeeId);
        payroll.setPositionId(employee.positionId);
        payroll.setDepartmentId(employee.departmentId);
        preview.setPayroll(payroll);
        preview.setEmployeeCode(employee.employeeCode);
        preview.setFullName(employee.fullName);
        preview.setDepartmentName(employee.departmentName);
        preview.setPositionName(employee.positionName);
        preview.setContractSalary(employee.contractSalary);
        preview.setGenerationError(error);
        return preview;
    }

    private List<PayrollDetailDTO> buildDetails(Payroll payroll, PayrollPreviewDTO preview) {
        List<PayrollDetailDTO> details = new ArrayList<>();
        AttendanceSummary attendance = new AttendanceSummary();
        attendance.lateMinutes = preview.getLateMinutes();
        attendance.unauthorizedAbsentDays = preview.getUnauthorizedAbsentDays();
        attendance.latePenalty = moneyOrZero(preview.getLatePenalty());
        attendance.unauthorizedAbsentPenalty = moneyOrZero(preview.getUnauthorizedAbsentPenalty());
        OvertimeSummary overtime = new OvertimeSummary();
        overtime.overtimeHours = moneyOrZero(preview.getOvertimeHours());
        BigDecimal attendanceBonus = moneyOrZero(preview.getAttendanceBonus());
        details.add(new PayrollDetailDTO("CONTRACT_SALARY", "Luong hop dong", PayrollDetailDTO.TYPE_EARNING,
                moneyOrZero(preview.getContractSalary()),
                "Luong thang tren hop dong, truoc khi quy doi theo ngay cong."));
        details.add(new PayrollDetailDTO("BASE_SALARY", "Lương cơ bản", PayrollDetailDTO.TYPE_EARNING,
                payroll.getBaseSalary(), "Ngày công được tính: " + payroll.getWorkingDays()));
        details.add(new PayrollDetailDTO("OVERTIME_PAY", "Lương tăng ca", PayrollDetailDTO.TYPE_EARNING,
                payroll.getOvertimePay(), "Giờ tăng ca được duyệt: " + scale(overtime.overtimeHours)));
        details.add(new PayrollDetailDTO("ATTENDANCE_BONUS", "Thưởng chuyên cần", PayrollDetailDTO.TYPE_EARNING,
                scale(attendanceBonus), null));
        details.add(new PayrollDetailDTO("LATE_PENALTY", "Phạt đi muộn", PayrollDetailDTO.TYPE_DEDUCTION,
                scale(attendance.latePenalty),
                "So block phat: " + preview.getLatePenaltyBlocks()
                + "; mot block phat: " + scale(latePenaltyBlockAmount(preview)) + "d."));
        details.add(new PayrollDetailDTO("ABSENT_PENALTY", "Phạt vắng không phép",
                PayrollDetailDTO.TYPE_DEDUCTION, scale(attendance.unauthorizedAbsentPenalty),
                "Số ngày vắng không phép: " + attendance.unauthorizedAbsentDays));
        details.add(new PayrollDetailDTO("PERSONAL_INCOME_TAX", "Thuế thu nhập cá nhân",
                PayrollDetailDTO.TYPE_DEDUCTION, payroll.getPersonalIncomeTax(), null));
        for (PayrollDeductionRule rule : payrollConfigDAO.getDeductionRules(true)) {
            BigDecimal amount = calculateDeductionRuleAmount(rule, preview.getContractSalary(),
                    payroll.getGrossSalary(), preview.getTaxableIncome());
            details.add(new PayrollDetailDTO(rule.getRuleCode(), rule.getRuleName(),
                    PayrollDetailDTO.TYPE_DEDUCTION, scale(amount), buildDeductionRuleNote(rule)));
            BigDecimal employerAmount = calculateEmployerContributionAmount(rule,
                    preview.getContractSalary(), payroll.getGrossSalary(), preview.getTaxableIncome());
            if (employerAmount.signum() > 0) {
                details.add(new PayrollDetailDTO(rule.getRuleCode() + "_EMPLOYER",
                        "Doanh nghiệp đóng - " + rule.getRuleName(), PayrollDetailDTO.TYPE_COMPANY_COST,
                        scale(employerAmount), buildEmployerContributionNote(rule)));
            }
        }
        details.add(new PayrollDetailDTO("TAXABLE_INCOME", "Thu nhap tinh thue", PayrollDetailDTO.TYPE_INFO,
                ZERO, "Sau bao hiem va giam tru gia canh "
                + scale(moneyOrZero(preview.getFamilyAllowance())) + "d (giam tru ca nhan "
                + scale(moneyOrZero(preview.getPersonalAllowance())) + "d + "
                + preview.getDependentCount() + " nguoi phu thuoc x "
                + scale(moneyOrZero(preview.getDependentAllowance())) + "d): "
                + scale(preview.getTaxableIncome()) + "d."));
        return details;
    }

    private List<PayrollDetailDTO> buildSavedDetails(Payroll payroll) {
        List<PayrollDetailDTO> details = new ArrayList<>();
        if (payroll == null) {
            return details;
        }
        details.add(new PayrollDetailDTO("BASE_SALARY", "Lương cơ bản", PayrollDetailDTO.TYPE_EARNING,
                moneyOrZero(payroll.getBaseSalary()), "Ngày công được tính: " + payroll.getWorkingDays()));
        details.add(new PayrollDetailDTO("ALLOWANCE", "Phụ cấp", PayrollDetailDTO.TYPE_EARNING,
                moneyOrZero(payroll.getAllowance()), null));
        details.add(new PayrollDetailDTO("BONUS", "Thưởng", PayrollDetailDTO.TYPE_EARNING,
                moneyOrZero(payroll.getBonus()), null));
        details.add(new PayrollDetailDTO("OVERTIME_PAY", "Lương tăng ca", PayrollDetailDTO.TYPE_EARNING,
                moneyOrZero(payroll.getOvertimePay()), null));
        details.add(new PayrollDetailDTO("PENALTY", "Phạt", PayrollDetailDTO.TYPE_DEDUCTION,
                moneyOrZero(payroll.getPenalty()), null));
        details.add(new PayrollDetailDTO("PERSONAL_INCOME_TAX", "Thuế thu nhập cá nhân",
                PayrollDetailDTO.TYPE_DEDUCTION, moneyOrZero(payroll.getPersonalIncomeTax()), null));
        return details;
    }

    private void enrichSavedPayrollPreview(PayrollPreviewDTO preview) {
        if (preview == null || preview.getPayroll() == null) {
            return;
        }

        Payroll payroll = preview.getPayroll();
        PayrollRuntimeConfig config;
        try {
            config = loadPayrollRuntimeConfig();
        } catch (IllegalStateException e) {
            preview.setDetails(buildSavedDetails(payroll));
            return;
        }
        YearMonth ym = YearMonth.from(payroll.getPeriodStart().toLocalDate());
        int standardWorkingDays = countStandardWorkingDays(ym);
        BigDecimal contractSalary = moneyOrZero(preview.getContractSalary());
        BigDecimal dailyRate = standardWorkingDays > 0 && contractSalary.signum() > 0
                ? divideMoney(contractSalary, new BigDecimal(standardWorkingDays))
                : ZERO;
        BigDecimal hourlyRate = divideMoney(dailyRate, config.workingHoursPerDay);
        BigDecimal minuteRate = divideMoney(hourlyRate, MINUTES_PER_HOUR);

        int lateMinutes = extractIntNoteValue(payroll.getNote(), "lateMinutes");
        int unauthorizedAbsentDays = extractIntNoteValue(payroll.getNote(), "unauthorizedAbsentDays");
        BigDecimal overtimeHours = extractDecimalNoteValue(payroll.getNote(), "overtimeHours");
        int latePenaltyMinutes = roundUpToLatePenaltyBlock(lateMinutes, config.latePenaltyBlockMinutes);
        int latePenaltyBlocks = latePenaltyMinutes / config.latePenaltyBlockMinutes;
        BigDecimal latePenalty = minuteRate.multiply(new BigDecimal(latePenaltyMinutes));
        BigDecimal absentPenalty = dailyRate.multiply(new BigDecimal(unauthorizedAbsentDays));

        int dependentCount = extractDependentCount(payroll);
        BigDecimal familyAllowance = calculateFamilyAllowance(
                config.personalAllowance, dependentCount, config.dependentAllowance);
        BigDecimal taxableIncome = moneyOrZero(payroll.getGrossSalary())
                .subtract(moneyOrZero(payroll.getInsuranceDeduction()))
                .subtract(familyAllowance);
        if (taxableIncome.signum() < 0) {
            taxableIncome = ZERO;
        }

        preview.setDailyRate(scale(dailyRate));
        preview.setHourlyRate(scale(hourlyRate));
        preview.setMinuteRate(scale(minuteRate));
        preview.setStandardWorkingDays(standardWorkingDays);
        preview.setPaidLeaveDays(extractIntNoteValue(payroll.getNote(), "paidLeaveDays"));
        preview.setUnpaidLeaveDays(extractIntNoteValue(payroll.getNote(), "unpaidLeaveDays"));
        preview.setUnauthorizedAbsentDays(unauthorizedAbsentDays);
        preview.setLateMinutes(lateMinutes);
        preview.setLatePenaltyBlocks(latePenaltyBlocks);
        preview.setLatePenaltyMinutes(latePenaltyMinutes);
        preview.setOvertimeHours(scale(overtimeHours));
        preview.setAttendanceBonus(moneyOrZero(payroll.getBonus()));
        preview.setLatePenalty(scale(latePenalty));
        preview.setUnauthorizedAbsentPenalty(scale(absentPenalty));
        preview.setPersonalAllowance(scale(config.personalAllowance));
        preview.setDependentCount(dependentCount);
        preview.setFamilyAllowance(scale(familyAllowance));
        preview.setDependentAllowance(scale(config.dependentAllowance));
        preview.setTaxableIncome(scale(taxableIncome));
        preview.setTotalDeduction(scale(latePenalty
                .add(moneyOrZero(payroll.getInsuranceDeduction()))
                .add(moneyOrZero(payroll.getPersonalIncomeTax()))));

        if (payroll.getNote() == null || payroll.getNote().trim().isEmpty()) {
            preview.setDetails(buildSavedDetails(payroll));
        } else {
            preview.setDetails(buildDetails(payroll, preview));
        }
    }

    private int extractIntNoteValue(String note, String key) {
        BigDecimal value = extractDecimalNoteValue(note, key);
        return value.setScale(0, RoundingMode.DOWN).intValue();
    }

    private BigDecimal extractDecimalNoteValue(String note, String key) {
        if (note == null || key == null) {
            return ZERO;
        }
        String prefix = key + "=";
        String[] parts = note.split(";");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.startsWith(prefix)) {
                try {
                    return new BigDecimal(trimmed.substring(prefix.length()).trim());
                } catch (NumberFormatException e) {
                    return ZERO;
                }
            }
        }
        return ZERO;
    }

    private AttendanceSummary getAttendanceSummary(Connection conn, int employeeId, int year, int month,
            BigDecimal dailyRate, BigDecimal minuteRate, PayrollRuntimeConfig config) throws SQLException {

        AttendanceSummary summary = new AttendanceSummary();

        String SQL = "SELECT attendanceStatus, COALESCE(hoursWorked, 0) AS hoursWorked, timeIn, workDate "
                + "FROM Attendance "
                + "WHERE employeeId = ? AND YEAR(workDate) = ? AND MONTH(workDate) = ?";

        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, year);
            ps.setInt(3, month);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    summary.recordCount++;

                    Date workDate = rs.getDate("workDate");
                    DayOfWeek dow = workDate.toLocalDate().getDayOfWeek();

                    if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                        continue;
                    }

                    int status = rs.getInt("attendanceStatus");

                    BigDecimal hours = moneyOrZero(rs.getBigDecimal("hoursWorked"));
                    summary.hoursWorked = summary.hoursWorked.add(hours);

                    if (status == 0) {
                        summary.paidWorkingDays++;

                    } else if (status == 1) {
                        summary.paidWorkingDays++;
                        summary.lateCount++;

                        // Giờ vào lưu trong DB là giờ thực; phút đi muộn tính theo
                        // block 30 phút (làm tròn LÊN) để khớp với cách tính giờ công.
                        LocalTime timeIn = rs.getTime("timeIn") == null
                                ? config.standardStartTime
                                : rs.getTime("timeIn").toLocalTime();

                        int lateMinutes = Math.max(0,
                                (int) java.time.Duration.between(config.standardStartTime, timeIn).toMinutes());
                        int penaltyMinutes = roundUpToLatePenaltyBlock(lateMinutes, config.latePenaltyBlockMinutes);

                        summary.lateMinutes += lateMinutes;
                        summary.latePenaltyMinutes += penaltyMinutes;
                        summary.latePenaltyBlocks += penaltyMinutes / config.latePenaltyBlockMinutes;
                        summary.latePenalty = summary.latePenalty.add(
                                minuteRate.multiply(new BigDecimal(penaltyMinutes)));

                    } else if (status == 4) {
                        // Nghỉ phép có lương
                        summary.paidLeaveDays++;
                        summary.paidWorkingDays++;

                    } else if (status == 2 || status == 3) {
                        // Vắng mặt / Không phép: không cộng công
                        summary.unauthorizedAbsentDays++;
                        summary.unauthorizedAbsentPenalty
                                = summary.unauthorizedAbsentPenalty.add(dailyRate);
                    }
                }
            }
        }

        return summary;
    }

    private OvertimeSummary getOvertimeSummary(Connection conn, int employeeId, int year, int month,
            BigDecimal dailyRate, PayrollRuntimeConfig config) throws SQLException {

        OvertimeSummary summary = new OvertimeSummary();
        BigDecimal hourlyRate = divideMoney(dailyRate, config.workingHoursPerDay);

        String SQL = "SELECT od.startTime, od.endTime, od.dayType, a.timeIn, a.timeOut "
                + "FROM Form_Requests fr "
                + "JOIN Overtime_Details od ON od.formId = fr.formId "
                + "LEFT JOIN Overtime_Assignees oa ON oa.formId = fr.formId "
                + "JOIN Attendance a ON a.employeeId = ? AND a.workDate = od.otDate "
                + "WHERE fr.status = 1 "
                + "AND fr.formTypeId = (SELECT formTypeId FROM Form_Types WHERE formTypeCode = 'OVERTIME') "
                + "AND (fr.employeeId = ? OR oa.employeeId = ?) "
                + "AND YEAR(od.otDate) = ? "
                + "AND MONTH(od.otDate) = ?";

        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, employeeId);
            ps.setInt(3, employeeId);
            ps.setInt(4, year);
            ps.setInt(5, month);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (rs.getTime("timeIn") == null || rs.getTime("timeOut") == null) {
                        continue;
                    }

                    LocalTime approvedStart = rs.getTime("startTime").toLocalTime();
                    LocalTime approvedEnd = rs.getTime("endTime").toLocalTime();

                    LocalTime actualIn = rs.getTime("timeIn").toLocalTime();
                    LocalTime actualOut = rs.getTime("timeOut").toLocalTime();

                    LocalTime validStart = approvedStart.isAfter(actualIn)
                            ? approvedStart
                            : actualIn;

                    LocalTime validEnd = approvedEnd.isBefore(actualOut)
                            ? approvedEnd
                            : actualOut;

                    if (!validEnd.isAfter(validStart)) {
                        continue;
                    }

                    long workedMinutes = java.time.Duration.between(validStart, validEnd).toMinutes();

                    long validBlocks = workedMinutes / config.overtimeBlockMinutes;
                    double validHours = (validBlocks * config.overtimeBlockMinutes) / MINUTES_PER_HOUR.doubleValue();

                    if (validHours <= 0) {
                        continue;
                    }

                    BigDecimal hours = new BigDecimal(String.valueOf(validHours))
                            .setScale(2, RoundingMode.HALF_UP);

                    BigDecimal pay = hourlyRate
                            .multiply(hours)
                            .multiply(overtimeMultiplier(rs.getInt("dayType"), config));

                    summary.overtimeHours = summary.overtimeHours.add(hours);
                    summary.overtimePay = summary.overtimePay.add(pay);
                }
            }
        }

        return summary;
    }

    private BigDecimal calculatePersonalIncomeTax(BigDecimal taxableIncome, List<PayrollTaxBracket> brackets) {
        BigDecimal tax = ZERO;
        for (PayrollTaxBracket bracket : brackets) {
            BigDecimal min = moneyOrZero(bracket.getMinIncome());
            BigDecimal max = bracket.getMaxIncome();
            if (taxableIncome.compareTo(min) <= 0) {
                continue;
            }
            BigDecimal upper = max == null ? taxableIncome : taxableIncome.min(max);
            BigDecimal amount = upper.subtract(min);
            if (amount.signum() > 0) {
                tax = tax.add(amount.multiply(moneyOrZero(bracket.getTaxRate())));
            }
        }
        return scale(tax);
    }

    private int countStandardWorkingDays(YearMonth ym) {
        int count = 0;
        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            DayOfWeek dow = ym.atDay(day).getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                count++;
            }
        }
        return count;
    }

    private List<EmployeePayrollBase> getActiveEmployeesWithContracts(Connection conn, int year, int month,
            Integer departmentId) throws SQLException {
        List<EmployeePayrollBase> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(baseEmployeeContractQuery()
                + "WHERE e.status = 1 "
                + "  AND ec.contractId = ( "
                + "      SELECT ec2.contractId FROM Employment_Contracts ec2 "
                + "      WHERE ec2.employeeId = e.employeeId AND ec2.status = 1 "
                + "        AND ec2.startDate <= ? "
                + "        AND (ec2.endDate IS NULL OR ec2.endDate >= ?) "
                + "      ORDER BY ec2.contractId DESC LIMIT 1 "
                + "  ) ");
        if (departmentId != null) {
            sql.append("AND e.departmentId = ? ");
        }
        sql.append("ORDER BY d.departmentName, e.employeeCode");
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setDate(1, toPeriodEnd(year, month));
            ps.setDate(2, toPeriodStart(year, month));
            if (departmentId != null) {
                ps.setInt(3, departmentId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEmployeePayrollBase(rs));
                }
            }
        }
        return list;
    }

    private EmployeePayrollBase getActiveEmployeeWithContract(Connection conn, int employeeId, int year, int month)
            throws SQLException {
        String SQL = baseEmployeeContractQuery()
                + "WHERE e.employeeId = ? AND e.status = 1 "
                + "  AND ec.status = 1 "
                + "  AND ec.startDate <= ? "
                + "  AND (ec.endDate IS NULL OR ec.endDate >= ?) "
                + "ORDER BY ec.contractId DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, toPeriodEnd(year, month));
            ps.setDate(3, toPeriodStart(year, month));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEmployeePayrollBase(rs);
                }
            }
        }
        return null;
    }

    private String baseEmployeeContractQuery() {
        return "SELECT e.employeeId, e.employeeCode, e.positionId, e.departmentId, e.dependentCount, "
                + "u.fullName, d.departmentName, p.positionName, ec.salary "
                + "FROM Employees e "
                + "JOIN Users u ON u.userId = e.userId "
                + "LEFT JOIN Departments d ON d.departmentId = e.departmentId "
                + "LEFT JOIN Positions p ON p.positionId = e.positionId "
                + "JOIN Employment_Contracts ec ON ec.employeeId = e.employeeId ";
    }

    private EmployeePayrollBase mapEmployeePayrollBase(ResultSet rs) throws SQLException {
        EmployeePayrollBase base = new EmployeePayrollBase();
        base.employeeId = rs.getInt("employeeId");
        base.employeeCode = rs.getString("employeeCode");
        base.positionId = rs.getInt("positionId");
        int departmentId = rs.getInt("departmentId");
        base.departmentId = rs.wasNull() ? null : departmentId;
        base.dependentCount = Math.max(0, rs.getInt("dependentCount"));
        base.fullName = rs.getNString("fullName");
        base.departmentName = rs.getNString("departmentName");
        base.positionName = rs.getString("positionName");
        base.contractSalary = rs.getBigDecimal("salary");
        return base;
    }

    private String[] payrollHeaders() {
        return new String[]{
            "employeeCode", "fullName", "departmentName", "positionName",
            "workingDays", "hoursWorked", "baseSalary", "allowance", "bonus",
            "overtimePay", "penalty", "grossSalary", "insuranceDeduction",
            "personalIncomeTax", "netSalary", "status", "note"
        };
    }

    private Date toPeriodStart(int year, int month) {
        return Date.valueOf(YearMonth.of(year, month).atDay(1));
    }

    private Date toPeriodEnd(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return Date.valueOf(ym.atEndOfMonth());
    }

    private BigDecimal overtimeMultiplier(int dayType, PayrollRuntimeConfig config) {
        switch (dayType) {
            case 2:
                return config.overtimeWeekendMultiplier;
            case 3:
                return config.overtimeHolidayMultiplier;
            case 1:
            default:
                return config.overtimeWorkdayMultiplier;
        }
    }

    private BigDecimal divideMoney(BigDecimal amount, BigDecimal divisor) {
        return amount.divide(divisor, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null ? ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal moneyOrZero(BigDecimal value) {
        return value == null ? ZERO : value;
    }

    private PayrollRuntimeConfig loadPayrollRuntimeConfig() {
        Map<String, BigDecimal> settings = payrollConfigDAO.getSettingsMap();
        PayrollRuntimeConfig config = new PayrollRuntimeConfig();
        config.personalAllowance = requiredNonNegativeSetting(settings, "PERSONAL_ALLOWANCE");
        config.dependentAllowance = requiredNonNegativeSetting(settings, "DEPENDENT_ALLOWANCE");
        config.latePenaltyBlockMinutes = requiredPositiveSetting(settings, "LATE_PENALTY_BLOCK_MINUTES").intValue();
        config.attendanceBonusRate = requiredNonNegativeSetting(settings, "ATTENDANCE_BONUS_RATE");
        BigDecimal workStartMinutes = requiredNonNegativeSetting(settings, "WORK_START", "WORK_START_MINUTES");
        BigDecimal workEndMinutes = requiredNonNegativeSetting(settings, "WORK_END", "WORK_END_MINUTES");
        BigDecimal workBreakMinutes = requiredNonNegativeSetting(settings, "WORK_BREAK_MINUTES");
        config.standardStartTime = minutesOfDay(workStartMinutes, "WORK_START");
        config.workingHoursPerDay = calculateWorkingHoursPerDay(workStartMinutes, workEndMinutes, workBreakMinutes);
        config.overtimeBlockMinutes = requiredPositiveSetting(settings, "OVERTIME_BLOCK_MINUTES").intValue();
        config.overtimeWorkdayMultiplier = requiredPositiveSetting(settings, "OVERTIME_WORKDAY_MULTIPLIER");
        config.overtimeWeekendMultiplier = requiredPositiveSetting(settings, "OVERTIME_WEEKEND_MULTIPLIER");
        config.overtimeHolidayMultiplier = requiredPositiveSetting(settings, "OVERTIME_HOLIDAY_MULTIPLIER");
        config.deductionRules = payrollConfigDAO.getDeductionRules(true);
        config.taxBrackets = payrollConfigDAO.getTaxBrackets(true);
        if (config.taxBrackets == null || config.taxBrackets.isEmpty()) {
            throw new IllegalStateException("Payroll tax bracket config is missing.");
        }
        return config;
    }

    private BigDecimal calculateWorkingHoursPerDay(BigDecimal start, BigDecimal end, BigDecimal breakMinutes) {
        BigDecimal workingMinutes = end.subtract(start).subtract(breakMinutes);
        if (workingMinutes.signum() <= 0) {
            throw new IllegalStateException("Work time config is invalid.");
        }
        return divideMoney(workingMinutes, MINUTES_PER_HOUR);
    }

    private LocalTime minutesOfDay(BigDecimal value, String settingKey) {
        int minutes = value.intValue();
        if (minutes < 0 || minutes >= MINUTES_PER_HOUR.intValue() * 24) {
            throw new IllegalStateException("Payroll setting " + settingKey + " is invalid.");
        }
        return LocalTime.of(minutes / MINUTES_PER_HOUR.intValue(), minutes % MINUTES_PER_HOUR.intValue());
    }

    private BigDecimal requiredPositiveSetting(Map<String, BigDecimal> settings, String key) {
        BigDecimal value = requiredSetting(settings, key);
        if (value.signum() <= 0) {
            throw new IllegalStateException("Payroll setting " + key + " must be greater than 0.");
        }
        return value;
    }

    private BigDecimal requiredNonNegativeSetting(Map<String, BigDecimal> settings, String key) {
        BigDecimal value = requiredSetting(settings, key);
        if (value.signum() < 0) {
            throw new IllegalStateException("Payroll setting " + key + " must not be negative.");
        }
        return value;
    }

    private BigDecimal requiredNonNegativeSetting(Map<String, BigDecimal> settings, String key, String legacyKey) {
        BigDecimal value = settingValue(settings, key, legacyKey);
        if (value == null) {
            throw new IllegalStateException("Payroll setting " + key + " is missing.");
        }
        if (value.signum() < 0) {
            throw new IllegalStateException("Payroll setting " + key + " must not be negative.");
        }
        return value;
    }

    private BigDecimal requiredSetting(Map<String, BigDecimal> settings, String key) {
        BigDecimal value = settings == null ? null : settings.get(key);
        if (value == null) {
            throw new IllegalStateException("Payroll setting " + key + " is missing.");
        }
        return value;
    }

    private BigDecimal settingValue(Map<String, BigDecimal> settings, String key, String legacyKey) {
        if (settings == null) {
            return null;
        }
        BigDecimal value = settings.get(key);
        return value != null ? value : settings.get(legacyKey);
    }

    private BigDecimal calculateConfiguredDeductions(BigDecimal contractSalary, BigDecimal grossSalary,
            BigDecimal taxableIncome, List<PayrollDeductionRule> rules, boolean taxableOnly) {
        BigDecimal total = ZERO;
        if (rules == null) {
            return total;
        }
        for (PayrollDeductionRule rule : rules) {
            if (taxableOnly != rule.isTaxableDeduction()) {
                continue;
            }
            total = total.add(calculateDeductionRuleAmount(rule, contractSalary, grossSalary, taxableIncome));
        }
        return total;
    }

    private BigDecimal calculateDeductionRuleAmount(PayrollDeductionRule rule, BigDecimal contractSalary,
            BigDecimal grossSalary, BigDecimal taxableIncome) {
        if (rule == null) {
            return ZERO;
        }
        return moneyOrZero(contractSalary).multiply(moneyOrZero(rule.getEmployeeRate()));
    }

    private BigDecimal calculateEmployerContributionAmount(PayrollDeductionRule rule, BigDecimal contractSalary,
            BigDecimal grossSalary, BigDecimal taxableIncome) {
        if (rule == null) {
            return ZERO;
        }
        return moneyOrZero(contractSalary).multiply(moneyOrZero(rule.getEmployerRate()));
    }

    private String buildDeductionRuleNote(PayrollDeductionRule rule) {
        return "Tổng: " + percent(rule.getRate())
                + "; công ty trả: " + percent(rule.getEmployerRate())
                + "; nhân viên trả: " + percent(rule.getEmployeeRate())
                + "; nền tính: lương hợp đồng.";
    }

    private String buildEmployerContributionNote(PayrollDeductionRule rule) {
        return "Chi phí doanh nghiệp, không trừ vào lương nhân viên. Tỷ lệ công ty trả: "
                + percent(rule.getEmployerRate()) + ".";
    }

    private String percent(BigDecimal rate) {
        return moneyOrZero(rate).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString() + "%";
    }

    private BigDecimal latePenaltyBlockAmount(PayrollPreviewDTO preview) {
        if (preview == null || preview.getLatePenaltyBlocks() <= 0) {
            return ZERO;
        }
        return moneyOrZero(preview.getLatePenalty())
                .divide(new BigDecimal(preview.getLatePenaltyBlocks()), 6, RoundingMode.HALF_UP);
    }

    private int roundUpToLatePenaltyBlock(int lateMinutes, int blockMinutes) {
        if (lateMinutes <= 0) {
            return 0;
        }
        if (blockMinutes <= 0) {
            throw new IllegalStateException("Late penalty block minutes must be greater than 0.");
        }
        return ((lateMinutes + blockMinutes - 1) / blockMinutes) * blockMinutes;
    }

    private void setNumeric(Row row, int column, BigDecimal value) {
        row.createCell(column).setCellValue(moneyOrZero(value).doubleValue());
    }

    private BigDecimal calculateFamilyAllowance(BigDecimal personalAllowance, int dependentCount, BigDecimal dependentAllowance) {
        int safeDependentCount = Math.max(0, dependentCount);
        return moneyOrZero(personalAllowance).add(
                moneyOrZero(dependentAllowance).multiply(new BigDecimal(safeDependentCount)));
    }

    private int extractDependentCount(Payroll payroll) {
        if (payroll == null) {
            return 0;
        }
        String note = payroll.getNote();
        if (hasNoteKey(note, "dependentCount")) {
            return Math.max(0, extractIntNoteValue(note, "dependentCount"));
        }
        EmployeeDetailDTO employee = employeeDAO.getEmployeeById(payroll.getEmployeeId());
        return employee == null ? 0 : Math.max(0, employee.getDependentCount());
    }

    private boolean hasNoteKey(String note, String key) {
        if (note == null || key == null) {
            return false;
        }
        String prefix = key + "=";
        String[] parts = note.split(";");
        for (String part : parts) {
            if (part.trim().startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private String buildPayrollNote(AttendanceSummary attendance, OvertimeSummary overtime, int dependentCount, BigDecimal familyAllowance) {
        return "paidLeaveDays=" + attendance.paidLeaveDays
                + "; unpaidLeaveDays=" + attendance.unpaidLeaveDays
                + "; unauthorizedAbsentDays=" + attendance.unauthorizedAbsentDays
                + "; lateMinutes=" + attendance.lateMinutes
                + "; overtimeHours=" + scale(overtime.overtimeHours)
                + "; dependentCount=" + Math.max(0, dependentCount)
                + "; familyAllowance=" + scale(familyAllowance);
    }

    private Set<String> getPermissions(User user) {
        return permissionDAO.getPermissionCodeByUserId(user.getUserId());
    }

    private boolean isHrStaff(User user) {
        String role = roleDAO.getRoleByUserId(user.getUserId());
        return role != null && role.contains("HR");
    }

    // Inner classes
    private static class EmployeePayrollBase {

        int employeeId;
        String employeeCode;
        int positionId;
        Integer departmentId;
        int dependentCount;
        String fullName;
        String departmentName;
        String positionName;
        BigDecimal contractSalary;
    }

    private static class AttendanceSummary {

        int recordCount;
        int paidWorkingDays;
        int paidLeaveDays;
        int unpaidLeaveDays;
        int unauthorizedAbsentDays;
        int lateCount;
        int lateMinutes;
        int latePenaltyBlocks;
        int latePenaltyMinutes;
        BigDecimal hoursWorked = BigDecimal.ZERO;
        BigDecimal latePenalty = BigDecimal.ZERO;
        BigDecimal unauthorizedAbsentPenalty = BigDecimal.ZERO;
    }

    private static class OvertimeSummary {

        BigDecimal overtimeHours = BigDecimal.ZERO;
        BigDecimal overtimePay = BigDecimal.ZERO;
    }

    private static class PayrollRuntimeConfig {

        BigDecimal workingHoursPerDay;
        BigDecimal personalAllowance;
        BigDecimal dependentAllowance;
        LocalTime standardStartTime;
        int latePenaltyBlockMinutes;
        BigDecimal attendanceBonusRate;
        int overtimeBlockMinutes;
        BigDecimal overtimeWorkdayMultiplier;
        BigDecimal overtimeWeekendMultiplier;
        BigDecimal overtimeHolidayMultiplier;
        List<PayrollDeductionRule> deductionRules = new ArrayList<>();
        List<PayrollTaxBracket> taxBrackets = new ArrayList<>();
    }
}
