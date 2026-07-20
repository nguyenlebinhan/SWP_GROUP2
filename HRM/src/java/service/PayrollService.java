package service;

import dao.EmployeeDAO;
import dao.PayrollDAO;
import dao.PayrollConfigDAO;
import dao.PermissionDAO;
import dao.RoleDAO;
import dao.OvertimeDAO;
import dal.DBContext;
import dto.EmployeeDetailDTO;
import dto.PayrollAttendanceSummaryDTO;
import dto.PayrollDetailDTO;
import dto.PayrollOvertimeSummaryDTO;
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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Locale;
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
    private final OvertimeDAO overtimeDAO;
    private final AuditLogService auditLogService;
    private final AttendanceClosingService attendanceClosingService;
    private final AttendanceService attendanceService;

    public PayrollService() {
        this.dbContext = new DBContext();
        this.payrollDAO = new PayrollDAO();
        this.payrollConfigDAO = new PayrollConfigDAO();
        this.employeeDAO = new EmployeeDAO();
        this.permissionDAO = new PermissionDAO();
        this.roleDAO = new RoleDAO();
        this.overtimeDAO = new OvertimeDAO();
        this.auditLogService = new AuditLogService();
        this.attendanceClosingService = new AttendanceClosingService();
        this.attendanceService = new AttendanceService();
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
        for (PayrollPreviewDTO preview : saved) {
            enrichSavedPayrollPreview(preview);
        }
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

    // Xuất bảng lương
    public void exportPayrollWorkbook(User user, int year, int month, Integer departmentId, OutputStream out)
            throws IOException {
        boolean allowed = canExportPayroll(user);
        List<PayrollPreviewDTO> payrolls = allowed
                ? getAllPayrollForHr(user, year, month, departmentId)
                : new ArrayList<>();
        int exportedRows = 0;
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Bảng lương " + month + "-" + year);
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
                setNumeric(row, 10, p.getUnpaidDeduction());
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

    // Tạo bảng lương
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
        // Chặn cứng: chỉ lưu bảng lương khi bảng chấm công đã được Quản trị doanh nghiệp chốt (LOCKED).
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

        int standardWorkingDays = attendanceService.standardWorkingDays(month, year);

        if (standardWorkingDays <= 0) {
            return buildGenerationErrorPreview(employee, "Tháng lương không có ngày làm việc chuẩn.");
        }

        BigDecimal dailyRate = divideMoney(employee.contractSalary, new BigDecimal(standardWorkingDays));
        BigDecimal minuteRate = divideMoney(dailyRate, config.workingHoursPerDay.multiply(MINUTES_PER_HOUR));

        Date eligibleStart = employee.contractStartDate != null && employee.contractStartDate.after(periodStart)
                ? employee.contractStartDate : periodStart;
        Date eligibleEnd = employee.contractEndDate != null && employee.contractEndDate.before(periodEnd)
                ? employee.contractEndDate : periodEnd;

        PayrollAttendanceSummaryDTO attendance = attendanceService.getPayrollSummary(conn, employee.employeeId,
                year, month, eligibleStart, eligibleEnd, dailyRate, minuteRate, config.standardStartTime,
                config.lateDeductionBlockMinutes);

        if (attendance.getRecordCount() == 0) {
            return buildGenerationErrorPreview(employee, "Chưa có dữ liệu chấm công trong tháng lương này.");
        }

        PayrollOvertimeSummaryDTO overtime = overtimeDAO.getPayrollOvertimeSummary(conn, employee.employeeId,
                year, month, dailyRate, config.workingHoursPerDay, config.overtimeBlockMinutes,
                config.overtimeWorkdayMultiplier, config.overtimeWeekendMultiplier);

        int unpaidWorkingDays = Math.max(0, standardWorkingDays - attendance.getPaidWorkingDays());
        attendance.setUnauthorizedAbsentDays(unpaidWorkingDays);
        boolean insuranceCalculated = unpaidWorkingDays < config.insuranceNotWorkedDaysThreshold;
        BigDecimal baseSalary = employee.contractSalary;
        BigDecimal attendanceBonus = attendance.getLateMinutes() == 0
                && unpaidWorkingDays == 0
                        ? employee.contractSalary.multiply(config.attendanceBonusRate)
                        : ZERO;

        BigDecimal allowance = config.allowance;
        BigDecimal bonus = attendanceBonus;

        BigDecimal unpaidDeduction = dailyRate.multiply(new BigDecimal(unpaidWorkingDays))
                .add(attendance.getLateDeduction());

        // Tổng thu nhập = lương hợp đồng + phụ cấp + tăng ca + thưởng.
        // Lương thực nhận = tổng thu nhập - các khoản khấu trừ - thuế TNCN.
        BigDecimal grossSalary = baseSalary
                .add(allowance)
                .add(bonus)
                .add(overtime.getOvertimePay());

        BigDecimal preTaxDeductions = calculateConfiguredDeductions(
                employee.contractSalary, grossSalary, ZERO, config, true, employee.unionMember, insuranceCalculated);

        BigDecimal familyAllowance = calculateFamilyAllowance(
                config.personalAllowance, employee.dependentCount, config.dependentAllowance);
        BigDecimal taxableIncome = grossSalary
                .subtract(preTaxDeductions)
                .subtract(unpaidDeduction)
                .subtract(familyAllowance);

        if (taxableIncome.signum() < 0) {
            taxableIncome = ZERO;
        }

        BigDecimal personalIncomeTax = calculatePersonalIncomeTax(taxableIncome, config.taxBrackets);
        BigDecimal postTaxDeductions = calculateConfiguredDeductions(
                employee.contractSalary, grossSalary, taxableIncome, config, false, employee.unionMember, insuranceCalculated);
        BigDecimal configuredDeductions = preTaxDeductions.add(postTaxDeductions);

        BigDecimal netSalary = grossSalary
                .subtract(configuredDeductions)
                .subtract(personalIncomeTax)
                .subtract(unpaidDeduction);

        if (netSalary.signum() < 0) {
            return buildGenerationErrorPreview(employee, "Dữ liệu chưa thể tạo ra. Cần kiểm tra lại dữ liệu nguồn...");
        }

        Payroll payroll = new Payroll();
        payroll.setPeriodStart(periodStart);
        payroll.setPeriodEnd(periodEnd);
        payroll.setEmployeeId(employee.employeeId);
        payroll.setPositionId(employee.positionId);
        payroll.setDepartmentId(employee.departmentId);
        payroll.setWorkingDays(attendance.getPaidWorkingDays());
        payroll.setHoursWorked(scale(attendance.getHoursWorked()));
        payroll.setBaseSalary(scale(baseSalary));
        payroll.setAllowance(scale(allowance));
        payroll.setBonus(scale(bonus));
        payroll.setOvertimePay(scale(overtime.getOvertimePay()));
        payroll.setUnpaidDeduction(scale(unpaidDeduction));
        payroll.setGrossSalary(scale(grossSalary));
        payroll.setInsuranceDeduction(scale(configuredDeductions));
        payroll.setPersonalIncomeTax(scale(personalIncomeTax));
        payroll.setNetSalary(scale(netSalary));
        payroll.setStatus(STATUS_PENDING_APPROVAL);
        payroll.setNote(buildPayrollNote(attendance, overtime, employee.dependentCount, familyAllowance,
                employee.unionMember, insuranceCalculated));

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
        preview.setPaidLeaveDays(attendance.getPaidLeaveDays());
        preview.setUnpaidLeaveDays(unpaidWorkingDays);
        preview.setUnauthorizedAbsentDays(unpaidWorkingDays);
        preview.setLateMinutes(attendance.getLateMinutes());
        preview.setLateDeductionBlocks(attendance.getLateDeductionBlocks());
        preview.setLateDeductionMinutes(attendance.getLateDeductionMinutes());
        preview.setLateDeductionBlockMinutes(config.lateDeductionBlockMinutes);
        preview.setOvertimeHours(scale(overtime.getOvertimeHours()));
        preview.setOvertimeBlocks(overtime.getOvertimeBlocks());
        preview.setOvertimeBlockMinutes(config.overtimeBlockMinutes);
        preview.setOvertimeBlockAmount(scale(overtimeWorkdayBlockAmount(
                divideMoney(dailyRate, config.workingHoursPerDay), config)));
        preview.setOvertimeWorkdayMultiplier(scale(config.overtimeWorkdayMultiplier));
        preview.setAttendanceBonus(scale(attendanceBonus));
        preview.setLateDeduction(scale(attendance.getLateDeduction()));
        preview.setLateDeductionBlockAmount(scale(blockAmount(attendance.getLateDeduction(), attendance.getLateDeductionBlocks())));
        preview.setUnauthorizedAbsentDeduction(scale(dailyRate.multiply(new BigDecimal(unpaidWorkingDays))));
        preview.setPersonalAllowance(scale(config.personalAllowance));
        preview.setDependentCount(employee.dependentCount);
        preview.setUnionMember(employee.unionMember);
        preview.setInsuranceCalculated(insuranceCalculated);
        preview.setInsuranceNotWorkedDaysThreshold(config.insuranceNotWorkedDaysThreshold);
        preview.setFamilyAllowance(scale(familyAllowance));
        preview.setDependentAllowance(scale(config.dependentAllowance));
        preview.setTaxableIncome(scale(taxableIncome));
        preview.setTotalDeduction(scale(unpaidDeduction.add(configuredDeductions).add(personalIncomeTax)));
        preview.setDetails(buildDetails(payroll, preview, config));

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
                "SELECT e.employeeId, e.employeeCode, e.positionId, e.departmentId, "
                + "(SELECT COUNT(*) FROM Dependents dep WHERE dep.employeeId = e.employeeId AND dep.status = 1) AS dependentCount, "
                + "e.unionMember, "
                + "u.fullName, d.departmentName, p.positionName, ec.salary, ec.effectiveDate, ec.endDate "
                + "FROM Employees e "
                + "JOIN Users u ON u.userId = e.userId "
                + "LEFT JOIN Departments d ON d.departmentId = e.departmentId "
                + "LEFT JOIN Positions p ON p.positionId = e.positionId "
                + "LEFT JOIN Employment_Contracts ec ON ec.contractId = ( "
                + "    SELECT ec2.contractId FROM Employment_Contracts ec2 "
                + "    WHERE ec2.employeeId = e.employeeId AND ec2.status = 'ACTIVE' "
                + "      AND ec2.effectiveDate <= ? "
                + "      AND (ec2.endDate IS NULL OR ec2.endDate >= ?) "
                + "    ORDER BY ec2.contractId DESC LIMIT 1 "
                + ") "
                + "WHERE e.status = 1 ");
        if (departmentId != null) {
            sql.append("AND e.departmentId = ? ");
        }
        sql.append("ORDER BY d.departmentName, e.employeeCode");

        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setDate(1, toPeriodEnd(year, month));
            ps.setDate(2, toPeriodStart(year, month));
            if (departmentId != null) {
                ps.setInt(3, departmentId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int employeeId = rs.getInt("employeeId");
                    if (savedEmployeeIds.contains(employeeId)) {
                        continue;
                    }
                    BigDecimal salary = rs.getBigDecimal("salary");
                    EmployeePayrollBase employee = new EmployeePayrollBase();
                    employee.employeeId = employeeId;
                    employee.employeeCode = rs.getString("employeeCode");
                    employee.positionId = rs.getInt("positionId");
                    int deptId = rs.getInt("departmentId");
                    employee.departmentId = rs.wasNull() ? null : deptId;
                    employee.dependentCount = Math.max(0, rs.getInt("dependentCount"));
                    employee.unionMember = rs.getBoolean("unionMember");
                    employee.fullName = rs.getNString("fullName");
                    employee.departmentName = rs.getNString("departmentName");
                    employee.positionName = rs.getString("positionName");
                    employee.contractSalary = salary;
                    employee.contractStartDate = rs.getDate("effectiveDate");
                    employee.contractEndDate = rs.getDate("endDate");
                    String error = null;
                    if (salary == null || salary.signum() <= 0) {
                        error = "Chưa có hợp đồng active hoặc lương hợp đồng hợp lệ.";
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
        preview.setUnionMember(employee.unionMember);
        preview.setGenerationError(error);
        return preview;
    }

    private List<PayrollDetailDTO> buildDetails(Payroll payroll, PayrollPreviewDTO preview, PayrollRuntimeConfig config) {
        List<PayrollDetailDTO> details = new ArrayList<>();

        details.add(new PayrollDetailDTO("BASE_SALARY", "Lương cơ bản", PayrollDetailDTO.TYPE_EARNING,
                moneyOrZero(payroll.getBaseSalary()), "Lương tháng theo hợp đồng đang hiệu lực."));
        details.add(new PayrollDetailDTO("ALLOWANCE", "Phụ cấp", PayrollDetailDTO.TYPE_EARNING,
                moneyOrZero(payroll.getAllowance()), "Các khoản phụ cấp bổ sung."));
        details.add(new PayrollDetailDTO("OVERTIME_PAY", "Tiền tăng ca", PayrollDetailDTO.TYPE_EARNING,
                moneyOrZero(payroll.getOvertimePay()), "Tăng ca: " + preview.getOvertimeBlocks()
                + " lượt. Mỗi lượt " + preview.getOvertimeBlockMinutes() + " phút, tính theo hệ số "
                + overtimeMultiplierText(preview) + " lần lương giờ = "
                + scale(preview.getOvertimeBlockAmount()) + "đ/lượt."));
        details.add(new PayrollDetailDTO("BONUS", "Thưởng", PayrollDetailDTO.TYPE_EARNING,
                moneyOrZero(payroll.getBonus()), "Thưởng chuyên cần."));

        for (PayrollDeductionRule rule : payrollConfigDAO.getDeductionRules(true)) {
            if (!appliesToEmployee(rule, preview.isUnionMember(), preview.isInsuranceCalculated())) {
                continue;
            }
            BigDecimal amount = calculateDeductionRuleAmount(rule,
                    deductionBase(rule, preview.getContractSalary(), payroll.getGrossSalary(), config),
                    payroll.getGrossSalary(), preview.getTaxableIncome());
            String ruleDisplayName = deductionRuleDisplayName(rule);
            details.add(new PayrollDetailDTO(rule.getRuleCode(), ruleDisplayName,
                    PayrollDetailDTO.TYPE_DEDUCTION, scale(amount), buildDeductionBaseNote(rule, config)));
            BigDecimal employerAmount = calculateEmployerContributionAmount(rule,
                    deductionBase(rule, preview.getContractSalary(), payroll.getGrossSalary(), config),
                    payroll.getGrossSalary(), preview.getTaxableIncome());
            if (employerAmount.signum() > 0) {
                details.add(new PayrollDetailDTO(rule.getRuleCode() + "_EMPLOYER",
                        "Doanh nghiệp đóng - " + ruleDisplayName, PayrollDetailDTO.TYPE_COMPANY_COST,
                        scale(employerAmount), buildEmployerBaseNote(rule, config)));
            }
        }
        details.add(new PayrollDetailDTO("UNPAID_DEDUCTION", "Khấu trừ ngày không làm", PayrollDetailDTO.TYPE_DEDUCTION,
                moneyOrZero(payroll.getUnpaidDeduction()), "Ngày không làm: "
                + preview.getNotWorkedDays() + " ngày x " + scale(preview.getDailyRate()) + "đ; khấu trừ đi muộn: "
                + preview.getLateDeductionBlocks() + " lượt (" + preview.getLateDeductionBlockMinutes() + " phút/lượt) x "
                + scale(preview.getLateDeductionBlockAmount()) + "đ."));
        details.add(new PayrollDetailDTO("PERSONAL_INCOME_TAX", "Thuế thu nhập cá nhân",
                PayrollDetailDTO.TYPE_DEDUCTION, moneyOrZero(payroll.getPersonalIncomeTax()), null));
        details.add(new PayrollDetailDTO("TAXABLE_INCOME", "Thu nhập tính thuế", PayrollDetailDTO.TYPE_INFO,
                ZERO, "Tổng thu nhập - bảo hiểm - khấu trừ ngày không làm - giảm trừ gia cảnh "
                + scale(moneyOrZero(preview.getFamilyAllowance())) + "đ (giảm trừ cá nhân "
                + scale(moneyOrZero(preview.getPersonalAllowance())) + "đ + "
                + preview.getDependentCount() + " người phụ thuộc x "
                + scale(moneyOrZero(preview.getDependentAllowance())) + "đ): "
                + scale(preview.getTaxableIncome()) + "đ."));
        return details;
    }

    private List<PayrollDetailDTO> buildSavedDetails(Payroll payroll) {
        List<PayrollDetailDTO> details = new ArrayList<>();
        if (payroll == null) {
            return details;
        }
        details.add(new PayrollDetailDTO("BASE_SALARY", "Lương cơ bản", PayrollDetailDTO.TYPE_EARNING,
                moneyOrZero(payroll.getBaseSalary()), "Lương tháng theo hợp đồng đang hiệu lực."));
        details.add(new PayrollDetailDTO("ALLOWANCE", "Phụ cấp", PayrollDetailDTO.TYPE_EARNING,
                moneyOrZero(payroll.getAllowance()), null));
        details.add(new PayrollDetailDTO("OVERTIME_PAY", "Tiền tăng ca", PayrollDetailDTO.TYPE_EARNING,
                moneyOrZero(payroll.getOvertimePay()), null));
        details.add(new PayrollDetailDTO("BONUS", "Thưởng", PayrollDetailDTO.TYPE_EARNING,
                moneyOrZero(payroll.getBonus()), null));
        details.add(new PayrollDetailDTO("CONFIGURED_DEDUCTIONS", "Bảo hiểm xã hội / y tế / thất nghiệp", PayrollDetailDTO.TYPE_DEDUCTION,
                moneyOrZero(payroll.getInsuranceDeduction()), null));
        details.add(new PayrollDetailDTO("UNPAID_DEDUCTION", "Khấu trừ ngày không làm", PayrollDetailDTO.TYPE_DEDUCTION,
                moneyOrZero(payroll.getUnpaidDeduction()), null));
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
        int standardWorkingDays = attendanceService.standardWorkingDays(
                payroll.getPeriodStart().toLocalDate().getMonthValue(),
                payroll.getPeriodStart().toLocalDate().getYear());
        BigDecimal contractSalary = moneyOrZero(preview.getContractSalary());
        BigDecimal dailyRate = standardWorkingDays > 0 && contractSalary.signum() > 0
                ? divideMoney(contractSalary, new BigDecimal(standardWorkingDays))
                : ZERO;
        BigDecimal hourlyRate = divideMoney(dailyRate, config.workingHoursPerDay);
        BigDecimal minuteRate = divideMoney(hourlyRate, MINUTES_PER_HOUR);

        int lateMinutes = extractIntNoteValue(payroll.getNote(), "lateMinutes");
        int notWorkedDays = hasNoteKey(payroll.getNote(), "notWorkedDays")
                ? extractIntNoteValue(payroll.getNote(), "notWorkedDays")
                : extractIntNoteValue(payroll.getNote(), "unauthorizedAbsentDays");
        BigDecimal overtimeHours = extractDecimalNoteValue(payroll.getNote(), "overtimeHours");
        int overtimeBlocks = extractIntNoteValue(payroll.getNote(), "overtimeBlocks");
        if (overtimeBlocks <= 0 && overtimeHours.signum() > 0 && config.overtimeBlockMinutes > 0) {
            overtimeBlocks = overtimeHours.multiply(MINUTES_PER_HOUR)
                    .divide(new BigDecimal(config.overtimeBlockMinutes), 0, RoundingMode.DOWN)
                    .intValue();
        }
        BigDecimal overtimeBlockAmount = overtimeWorkdayBlockAmount(hourlyRate, config);
        if (overtimeBlocks <= 0 && moneyOrZero(payroll.getOvertimePay()).signum() > 0
                && overtimeBlockAmount.signum() > 0) {
            overtimeBlocks = moneyOrZero(payroll.getOvertimePay())
                    .divide(overtimeBlockAmount, 0, RoundingMode.HALF_UP)
                    .intValue();
            overtimeHours = new BigDecimal(overtimeBlocks)
                    .multiply(new BigDecimal(config.overtimeBlockMinutes))
                    .divide(MINUTES_PER_HOUR, 2, RoundingMode.HALF_UP);
        }
        int lateDeductionBlocks = extractIntNoteValue(payroll.getNote(), "lateDeductionBlocks");
        int lateDeductionMinutes = extractIntNoteValue(payroll.getNote(), "lateDeductionMinutes");
        if (lateDeductionBlocks <= 0 && lateDeductionMinutes <= 0) {
            lateDeductionMinutes = roundUpToLateDeductionBlock(lateMinutes, config.lateDeductionBlockMinutes);
            lateDeductionBlocks = lateDeductionMinutes / config.lateDeductionBlockMinutes;
        } else if (lateDeductionMinutes <= 0) {
            lateDeductionMinutes = lateDeductionBlocks * config.lateDeductionBlockMinutes;
        } else if (lateDeductionBlocks <= 0 && config.lateDeductionBlockMinutes > 0) {
            lateDeductionBlocks = lateDeductionMinutes / config.lateDeductionBlockMinutes;
        }
        BigDecimal lateDeduction = minuteRate.multiply(new BigDecimal(lateDeductionMinutes));
        BigDecimal absentUnpaidDeduction = dailyRate.multiply(new BigDecimal(notWorkedDays));

        int dependentCount = extractDependentCount(payroll);
        preview.setUnionMember(extractUnionMember(payroll));
        BigDecimal familyAllowance = calculateFamilyAllowance(
                config.personalAllowance, dependentCount, config.dependentAllowance);
        BigDecimal taxableIncome = moneyOrZero(payroll.getGrossSalary())
                .subtract(moneyOrZero(payroll.getInsuranceDeduction()))
                .subtract(moneyOrZero(payroll.getUnpaidDeduction()))
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
        preview.setUnauthorizedAbsentDays(notWorkedDays);
        preview.setLateMinutes(lateMinutes);
        preview.setLateDeductionBlocks(lateDeductionBlocks);
        preview.setLateDeductionMinutes(lateDeductionMinutes);
        preview.setLateDeductionBlockMinutes(config.lateDeductionBlockMinutes);
        preview.setOvertimeHours(scale(overtimeHours));
        preview.setOvertimeBlocks(overtimeBlocks);
        preview.setOvertimeBlockMinutes(config.overtimeBlockMinutes);
        preview.setOvertimeBlockAmount(scale(overtimeBlockAmount));
        preview.setOvertimeWorkdayMultiplier(scale(config.overtimeWorkdayMultiplier));
        preview.setAttendanceBonus(moneyOrZero(payroll.getBonus()));
        preview.setLateDeduction(scale(lateDeduction));
        preview.setLateDeductionBlockAmount(scale(blockAmount(lateDeduction, lateDeductionBlocks)));
        preview.setUnauthorizedAbsentDeduction(scale(absentUnpaidDeduction));
        preview.setPersonalAllowance(scale(config.personalAllowance));
        preview.setDependentCount(dependentCount);
        preview.setInsuranceCalculated(hasNoteKey(payroll.getNote(), "insuranceCalculated")
                ? extractIntNoteValue(payroll.getNote(), "insuranceCalculated") == 1
                : notWorkedDays < config.insuranceNotWorkedDaysThreshold);
        preview.setInsuranceNotWorkedDaysThreshold(config.insuranceNotWorkedDaysThreshold);
        preview.setFamilyAllowance(scale(familyAllowance));
        preview.setDependentAllowance(scale(config.dependentAllowance));
        preview.setTaxableIncome(scale(taxableIncome));
        preview.setTotalDeduction(scale(moneyOrZero(payroll.getUnpaidDeduction())
                .add(moneyOrZero(payroll.getInsuranceDeduction()))
                .add(moneyOrZero(payroll.getPersonalIncomeTax()))));

        if (payroll.getNote() == null || payroll.getNote().trim().isEmpty()) {
            preview.setDetails(buildSavedDetails(payroll));
        } else {
            preview.setDetails(buildDetails(payroll, preview, config));
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

    private List<EmployeePayrollBase> getActiveEmployeesWithContracts(Connection conn, int year, int month,
            Integer departmentId) throws SQLException {
        List<EmployeePayrollBase> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(baseEmployeeContractQuery()
                + "WHERE e.status = 1 "
                + "  AND ec.contractId = ( "
                + "      SELECT ec2.contractId FROM Employment_Contracts ec2 "
                + "      WHERE ec2.employeeId = e.employeeId AND ec2.status = 'ACTIVE' "
                + "        AND ec2.effectiveDate <= ? "
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
                + "  AND ec.status = 'ACTIVE' "
                + "  AND ec.effectiveDate <= ? "
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
        return "SELECT e.employeeId, e.employeeCode, e.positionId, e.departmentId, "
                + "(SELECT COUNT(*) FROM Dependents dep WHERE dep.employeeId = e.employeeId AND dep.status = 1) AS dependentCount, "
                + "e.unionMember, "
                + "u.fullName, d.departmentName, p.positionName, ec.salary, ec.effectiveDate, ec.endDate "
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
        base.unionMember = rs.getBoolean("unionMember");
        base.fullName = rs.getNString("fullName");
        base.departmentName = rs.getNString("departmentName");
        base.positionName = rs.getString("positionName");
        base.contractSalary = rs.getBigDecimal("salary");
        base.contractStartDate = rs.getDate("effectiveDate");
        base.contractEndDate = rs.getDate("endDate");
        return base;
    }

    private String[] payrollHeaders() {
        return new String[]{
            "Mã nhân viên", "Họ tên", "Phòng ban", "Chức vụ",
            "Ngày công", "Giờ làm", "Lương cơ bản", "Phụ cấp", "Thưởng",
            "Tiền tăng ca", "Khấu trừ ngày không làm", "Tổng thu nhập", "Bảo hiểm",
            "Thuế thu nhập cá nhân", "Lương thực nhận", "Trạng thái", "Ghi chú"
        };
    }

    private Date toPeriodStart(int year, int month) {
        return Date.valueOf(YearMonth.of(year, month).atDay(1));
    }

    private Date toPeriodEnd(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return Date.valueOf(ym.atEndOfMonth());
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
        config.personalAllowance = requiredNonNegativeSetting(settings, "PERSONAL_DEDUCTION");
        config.dependentAllowance = requiredNonNegativeSetting(settings, "DEPENDENT_ALLOWANCE");
        config.allowance = requiredNonNegativeSetting(settings, "ALLOWANCE");
        config.insuranceSalaryCap = requiredPositiveSetting(settings, "INSURANCE_SALARY_FLOOR");
        config.insuranceNotWorkedDaysThreshold = requiredPositiveSetting(settings, "INSURANCE_NOT_WORKED_DAYS_THRESHOLD").intValue();
        config.lateDeductionBlockMinutes = requiredPositiveSetting(settings, "LATE_DEDUCTION_BLOCK_MINUTES").intValue();
        config.attendanceBonusRate = requiredNonNegativeSetting(settings, "ATTENDANCE_BONUS_RATE");
        BigDecimal workStartMinutes = requiredNonNegativeSetting(settings, "WORK_START", "WORK_START_MINUTES");
        BigDecimal workEndMinutes = requiredNonNegativeSetting(settings, "WORK_END", "WORK_END_MINUTES");
        BigDecimal workBreakMinutes = requiredNonNegativeSetting(settings, "WORK_BREAK_MINUTES");
        config.standardStartTime = minutesOfDay(workStartMinutes, "WORK_START");
        config.workingHoursPerDay = calculateWorkingHoursPerDay(workStartMinutes, workEndMinutes, workBreakMinutes);
        config.overtimeBlockMinutes = requiredPositiveSetting(settings, "OVERTIME_BLOCK_MINUTES").intValue();
        config.overtimeWorkdayMultiplier = requiredPositiveSetting(settings, "OVERTIME_WORKDAY_MULTIPLIER");
//        config.overtimeWeekendMultiplier = requiredPositiveSetting(settings, "OVERTIME_WEEKEND_MULTIPLIER");
//        config.overtimeHolidayMultiplier = requiredPositiveSetting(settings, "OVERTIME_HOLIDAY_MULTIPLIER");

        config.deductionRules = payrollConfigDAO.getDeductionRules(true);
        config.taxBrackets = payrollConfigDAO.getTaxBrackets(true);
        if (config.taxBrackets == null || config.taxBrackets.isEmpty()) {
            throw new IllegalStateException("Thiếu cấu hình bậc thuế lương.");
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
            throw new IllegalStateException("Cấu hình lương " + settingKey + " không hợp lệ.");
        }
        return LocalTime.of(minutes / MINUTES_PER_HOUR.intValue(), minutes % MINUTES_PER_HOUR.intValue());
    }

    private BigDecimal requiredPositiveSetting(Map<String, BigDecimal> settings, String key) {
        BigDecimal value = requiredSetting(settings, key);
        if (value.signum() <= 0) {
            throw new IllegalStateException("Cấu hình lương " + key + " phải lớn hơn 0.");
        }
        return value;
    }

    private BigDecimal requiredNonNegativeSetting(Map<String, BigDecimal> settings, String key) {
        BigDecimal value = requiredSetting(settings, key);
        if (value.signum() < 0) {
            throw new IllegalStateException("Cấu hình lương " + key + " không được âm.");
        }
        return value;
    }

    private BigDecimal requiredNonNegativeSetting(Map<String, BigDecimal> settings, String key, String legacyKey) {
        BigDecimal value = settingValue(settings, key, legacyKey);
        if (value == null) {
            throw new IllegalStateException("Thiếu cấu hình lương " + key + ".");
        }
        if (value.signum() < 0) {
            throw new IllegalStateException("Cấu hình lương " + key + " không được âm.");
        }
        return value;
    }

    private BigDecimal requiredSetting(Map<String, BigDecimal> settings, String key) {
        BigDecimal value = settings == null ? null : settings.get(key);
        if (value == null) {
            throw new IllegalStateException("Thiếu cấu hình lương " + key + ".");
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
            BigDecimal taxableIncome, PayrollRuntimeConfig config, boolean taxableOnly, boolean unionMember,
            boolean insuranceCalculated) {
        BigDecimal total = ZERO;
        if (config == null || config.deductionRules == null) {
            return total;
        }
        for (PayrollDeductionRule rule : config.deductionRules) {
            if (taxableOnly != rule.isTaxableDeduction()) {
                continue;
            }
            if (!appliesToEmployee(rule, unionMember, insuranceCalculated)) {
                continue;
            }
            total = total.add(calculateDeductionRuleAmount(rule,
                    deductionBase(rule, contractSalary, grossSalary, config), grossSalary, taxableIncome));
        }
        return total;
    }

    private boolean appliesToEmployee(PayrollDeductionRule rule, boolean unionMember, boolean insuranceCalculated) {
        if (rule == null) {
            return true;
        }
        if (isInsuranceRule(rule) && !insuranceCalculated) {
            return false;
        }
        return !"UNION_FEE".equals(rule.getRuleCode()) || unionMember;
    }

    private boolean isInsuranceRule(PayrollDeductionRule rule) {
        return rule != null && "INSURANCE".equals(rule.getRuleType());
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

    private BigDecimal insuranceSalaryBase(BigDecimal contractSalary, BigDecimal cap) {
        BigDecimal salary = moneyOrZero(contractSalary);
        BigDecimal insuranceCap = moneyOrZero(cap);
        return insuranceCap.signum() > 0 && salary.compareTo(insuranceCap) > 0 ? insuranceCap : salary;
    }

    private BigDecimal deductionBase(PayrollDeductionRule rule, BigDecimal contractSalary,
            BigDecimal grossSalary, PayrollRuntimeConfig config) {
        return insuranceSalaryBase(contractSalary, config.insuranceSalaryCap);
    }

    private String baseNote(PayrollDeductionRule rule, PayrollRuntimeConfig config) {
        return "nền tính: lương làm căn cứ đóng bảo hiểm, mức trần " + moneyDisplay(config.insuranceSalaryCap) + ".";

    }
    

    private String buildDeductionBaseNote(PayrollDeductionRule rule, PayrollRuntimeConfig config) {
        return "Tổng: " + percent(rule.getRate())
                + "; công ty trả: " + percent(rule.getEmployerRate())
                + "; nhân viên trả: " + percent(rule.getEmployeeRate())
                + "; " + baseNote(rule, config);
    }

    private String buildEmployerBaseNote(PayrollDeductionRule rule, PayrollRuntimeConfig config) {
        return "Chi phí doanh nghiệp, không trừ vào lương nhân viên. Tỷ lệ công ty trả: "
                + percent(rule.getEmployerRate())
                + "; " + baseNote(rule, config);
    }

    private String deductionRuleDisplayName(PayrollDeductionRule rule) {
        if (rule == null || rule.getRuleCode() == null) {
            return "";
        }
        switch (rule.getRuleCode()) {
            case "SOCIAL_INSURANCE":
                return "Bảo hiểm xã hội";
            case "HEALTH_INSURANCE":
                return "Bảo hiểm y tế";
            case "UNEMPLOYMENT_INSURANCE":
                return "Bảo hiểm thất nghiệp";
            case "UNION_FEE":
                return "Kinh phí công đoàn";
            default:
                return rule.getRuleName();
        }
    }

    private String percent(BigDecimal rate) {
        return moneyOrZero(rate).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString() + "%";
    }

    private String moneyDisplay(BigDecimal value) {
        return new DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(Locale.US)).format(moneyOrZero(value));
    }

    private BigDecimal lateDeductionBlockAmount(PayrollPreviewDTO preview) {
        if (preview == null || preview.getLateDeductionBlocks() <= 0) {
            return ZERO;
        }
        return moneyOrZero(preview.getLateDeduction())
                .divide(new BigDecimal(preview.getLateDeductionBlocks()), 6, RoundingMode.HALF_UP);
    }

    private BigDecimal blockAmount(BigDecimal total, int blocks) {
        if (blocks <= 0) {
            return ZERO;
        }
        return moneyOrZero(total).divide(new BigDecimal(blocks), 6, RoundingMode.HALF_UP);
    }

    private BigDecimal overtimeWorkdayBlockAmount(BigDecimal hourlyRate, PayrollRuntimeConfig config) {
        if (config == null || config.overtimeBlockMinutes <= 0) {
            return ZERO;
        }
        BigDecimal blockHours = new BigDecimal(config.overtimeBlockMinutes)
                .divide(MINUTES_PER_HOUR, 6, RoundingMode.HALF_UP);
        return moneyOrZero(hourlyRate)
                .multiply(blockHours)
                .multiply(moneyOrZero(config.overtimeWorkdayMultiplier));
    }

    private String overtimeMultiplierText(PayrollPreviewDTO preview) {
        return moneyOrZero(preview == null ? null : preview.getOvertimeWorkdayMultiplier())
                .stripTrailingZeros().toPlainString();
    }

    private int roundUpToLateDeductionBlock(int lateMinutes, int blockMinutes) {
        if (lateMinutes <= 0) {
            return 0;
        }
        if (blockMinutes <= 0) {
            throw new IllegalStateException("Late deduction block minutes must be greater than 0.");
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

    private boolean extractUnionMember(Payroll payroll) {
        if (payroll == null) {
            return false;
        }
        String note = payroll.getNote();
        if (hasNoteKey(note, "unionMember")) {
            return extractIntNoteValue(note, "unionMember") == 1;
        }
        EmployeeDetailDTO employee = employeeDAO.getEmployeeById(payroll.getEmployeeId());
        return employee != null && employee.isUnionMember();
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

    private String buildPayrollNote(PayrollAttendanceSummaryDTO attendance, PayrollOvertimeSummaryDTO overtime,
            int dependentCount, BigDecimal familyAllowance, boolean unionMember, boolean insuranceCalculated) {
        return "paidLeaveDays=" + attendance.getPaidLeaveDays()
                + "; unpaidLeaveDays=" + attendance.getUnpaidLeaveDays()
                + "; notWorkedDays=" + attendance.getUnauthorizedAbsentDays()
                + "; insuranceCalculated=" + (insuranceCalculated ? 1 : 0)
                + "; lateMinutes=" + attendance.getLateMinutes()
                + "; lateDeductionBlocks=" + attendance.getLateDeductionBlocks()
                + "; lateDeductionMinutes=" + attendance.getLateDeductionMinutes()
                + "; overtimeHours=" + scale(overtime.getOvertimeHours())
                + "; overtimeBlocks=" + overtime.getOvertimeBlocks()
                + "; dependentCount=" + Math.max(0, dependentCount)
                + "; unionMember=" + (unionMember ? 1 : 0)
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
        boolean unionMember;
        String fullName;
        String departmentName;
        String positionName;
        BigDecimal contractSalary;
        Date contractStartDate;
        Date contractEndDate;
    }

    private static class PayrollRuntimeConfig {

        BigDecimal workingHoursPerDay;
        BigDecimal personalAllowance;
        BigDecimal dependentAllowance;
        BigDecimal allowance;
        BigDecimal insuranceSalaryCap;
        int insuranceNotWorkedDaysThreshold;
        LocalTime standardStartTime;
        int lateDeductionBlockMinutes;
        BigDecimal attendanceBonusRate;
        int overtimeBlockMinutes;
        BigDecimal overtimeWorkdayMultiplier;
        BigDecimal overtimeWeekendMultiplier;
        List<PayrollDeductionRule> deductionRules = new ArrayList<>();
        List<PayrollTaxBracket> taxBrackets = new ArrayList<>();
    }
}
