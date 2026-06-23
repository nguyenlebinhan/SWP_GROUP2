package service;

import dao.EmployeeDAO;
import dao.PayrollDAO;
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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Payroll;
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
    private static final BigDecimal WORKING_HOURS_PER_DAY = new BigDecimal("8");
    private static final BigDecimal MINUTES_PER_HOUR = new BigDecimal("60");
    private static final BigDecimal SOCIAL_INSURANCE_RATE = new BigDecimal("0.08");
    private static final BigDecimal HEALTH_INSURANCE_RATE = new BigDecimal("0.015");
    private static final BigDecimal UNEMPLOYMENT_INSURANCE_RATE = new BigDecimal("0.01");
    private static final BigDecimal PERSONAL_ALLOWANCE = new BigDecimal("15500000");
    private static final LocalTime STANDARD_START_TIME = LocalTime.of(8, 0);

    private final DBContext dbContext;
    private final PayrollDAO payrollDAO;
    private final EmployeeDAO employeeDAO;
    private final PermissionDAO permissionDAO;
    private final RoleDAO roleDAO;
    private final AuditLogService auditLogService;

    public PayrollService() {
        this.dbContext = new DBContext();
        this.payrollDAO = new PayrollDAO();
        this.employeeDAO = new EmployeeDAO();
        this.permissionDAO = new PermissionDAO();
        this.roleDAO = new RoleDAO();
        this.auditLogService = new AuditLogService();
    }

    // ── Quyền ──────────────────────────────────────────────────────────────────
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

    // ── Xem lương ──────────────────────────────────────────────────────────────
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
                preview.setGenerationError("Bảng lương đang chưa thể tạo ra. Cần kiểm tra lại dữ liệu nguồn...");
            }
        }
    }

    public PayrollPreviewDTO getPayrollDetailForHr(User user, int payrollId) {
        if (!canViewAllSalary(user)) {
            return null;
        }
        PayrollPreviewDTO preview = payrollDAO.getPayrollPreviewById(payrollId);
        if (preview != null) {
            preview.setDetails(buildSavedDetails(preview.getPayroll()));
        }
        return preview;
    }

    // ── Duyệt tổng (0 → 1) ────────────────────────────────────────────────────
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

    // ── Export ─────────────────────────────────────────────────────────────────
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

    // ── Generate ───────────────────────────────────────────────────────────────
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

    // ── Tính lương ─────────────────────────────────────────────────────────────
    private PayrollPreviewDTO calculatePayroll(Connection conn, EmployeePayrollBase employee, int year, int month)
            throws SQLException {
        if (employee.contractSalary == null || employee.contractSalary.signum() <= 0) {
            return buildGenerationErrorPreview(employee, "Chưa có hợp đồng active hoặc lương hợp đồng hợp lệ.");
        }

        Date periodStart = toPeriodStart(year, month);
        Date periodEnd = toPeriodEnd(year, month);
        YearMonth ym = YearMonth.of(year, month);
        int standardWorkingDays = countStandardWorkingDays(ym);
        if (standardWorkingDays <= 0) {
            return buildGenerationErrorPreview(employee, "Tháng lương không có ngày làm việc chuẩn.");
        }

        BigDecimal dailyRate = divideMoney(employee.contractSalary, new BigDecimal(standardWorkingDays));
        BigDecimal minuteRate = divideMoney(dailyRate, WORKING_HOURS_PER_DAY.multiply(MINUTES_PER_HOUR));
        AttendanceSummary attendance = getAttendanceSummary(conn, employee.employeeId, year, month, dailyRate, minuteRate);
        if (attendance.recordCount == 0) {
            return buildGenerationErrorPreview(employee, "Chưa có dữ liệu chấm công trong tháng lương này.");
        }

        OvertimeSummary overtime = getOvertimeSummary(conn, employee.employeeId, year, month, dailyRate);
        BigDecimal attendanceBonus = attendance.lateMinutes == 0 && attendance.unauthorizedAbsentDays == 0
                ? employee.contractSalary.multiply(new BigDecimal("0.03"))
                : ZERO;
        BigDecimal baseSalary = dailyRate.multiply(new BigDecimal(attendance.paidWorkingDays));
        BigDecimal bonus = attendanceBonus;
        BigDecimal penalty = attendance.latePenalty.add(attendance.unauthorizedAbsentPenalty);
        BigDecimal grossSalary = baseSalary.add(bonus).add(overtime.overtimePay).subtract(penalty);
        BigDecimal insuranceDeduction = employee.contractSalary.multiply(
                SOCIAL_INSURANCE_RATE.add(HEALTH_INSURANCE_RATE).add(UNEMPLOYMENT_INSURANCE_RATE));
        BigDecimal taxableIncome = grossSalary.subtract(insuranceDeduction).subtract(PERSONAL_ALLOWANCE);
        if (taxableIncome.signum() < 0) {
            taxableIncome = ZERO;
        }
        BigDecimal personalIncomeTax = calculatePersonalIncomeTax(taxableIncome);
        BigDecimal netSalary = grossSalary.subtract(insuranceDeduction).subtract(personalIncomeTax);
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
        payroll.setInsuranceDeduction(scale(insuranceDeduction));
        payroll.setPersonalIncomeTax(scale(personalIncomeTax));
        payroll.setNetSalary(scale(netSalary));
        payroll.setStatus(STATUS_PENDING_APPROVAL);
        payroll.setNote(buildPayrollNote(attendance, overtime));

        PayrollPreviewDTO preview = new PayrollPreviewDTO();
        preview.setPayroll(payroll);
        preview.setEmployeeCode(employee.employeeCode);
        preview.setFullName(employee.fullName);
        preview.setDepartmentName(employee.departmentName);
        preview.setPositionName(employee.positionName);
        preview.setContractSalary(employee.contractSalary);
        preview.setDetails(buildDetails(payroll, attendance, overtime, attendanceBonus));
        return preview;
    }

    // ── Helpers nội bộ ─────────────────────────────────────────────────────────
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
                preview.setDetails(buildSavedDetails(preview.getPayroll()));
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

    private List<PayrollDetailDTO> buildDetails(Payroll payroll, AttendanceSummary attendance,
            OvertimeSummary overtime, BigDecimal attendanceBonus) {
        List<PayrollDetailDTO> details = new ArrayList<>();
        details.add(new PayrollDetailDTO("BASE_SALARY", "Lương cơ bản", PayrollDetailDTO.TYPE_EARNING,
                payroll.getBaseSalary(), "Ngày công được tính: " + payroll.getWorkingDays()));
        details.add(new PayrollDetailDTO("OVERTIME_PAY", "Lương tăng ca", PayrollDetailDTO.TYPE_EARNING,
                payroll.getOvertimePay(), "Giờ tăng ca được duyệt: " + scale(overtime.overtimeHours)));
        details.add(new PayrollDetailDTO("ATTENDANCE_BONUS", "Thưởng chuyên cần", PayrollDetailDTO.TYPE_EARNING,
                scale(attendanceBonus), null));
        details.add(new PayrollDetailDTO("LATE_PENALTY", "Phạt đi muộn", PayrollDetailDTO.TYPE_DEDUCTION,
                scale(attendance.latePenalty), "Số phút đi muộn: " + attendance.lateMinutes));
        details.add(new PayrollDetailDTO("ABSENT_PENALTY", "Phạt vắng không phép",
                PayrollDetailDTO.TYPE_DEDUCTION, scale(attendance.unauthorizedAbsentPenalty),
                "Số ngày vắng không phép: " + attendance.unauthorizedAbsentDays));
        details.add(new PayrollDetailDTO("PERSONAL_INCOME_TAX", "Thuế thu nhập cá nhân",
                PayrollDetailDTO.TYPE_DEDUCTION, payroll.getPersonalIncomeTax(), null));
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

    private AttendanceSummary getAttendanceSummary(Connection conn, int employeeId, int year, int month,
            BigDecimal dailyRate, BigDecimal minuteRate) throws SQLException {

        AttendanceSummary summary = new AttendanceSummary();

        String SQL = "SELECT attendanceStatus, COALESCE(hoursWorked, 0) AS hoursWorked, timeIn, workDate "
                + "FROM Attendance WHERE employeeId = ? AND YEAR(workDate) = ? AND MONTH(workDate) = ?";

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

                    if (status == 2) {
                        summary.unpaidLeaveDays++;
                    } else if (status == 3) {
                        summary.unauthorizedAbsentDays++;
                        summary.unauthorizedAbsentPenalty
                                = summary.unauthorizedAbsentPenalty.add(dailyRate);
                    } else {
                        summary.paidWorkingDays++;

                        if (status == 1) {
                            summary.lateCount++;

                            LocalTime timeIn = rs.getTime("timeIn") == null
                                    ? STANDARD_START_TIME
                                    : rs.getTime("timeIn").toLocalTime();

                            int lateMinutes = Math.max(0,
                                    (int) java.time.Duration.between(STANDARD_START_TIME, timeIn).toMinutes());

                            summary.lateMinutes += lateMinutes;
                            summary.latePenalty = summary.latePenalty.add(
                                    minuteRate.multiply(new BigDecimal(lateMinutes)));
                        }
                    }
                }
            }
        }

        return summary;
    }

    private OvertimeSummary getOvertimeSummary(Connection conn, int employeeId, int year, int month,
            BigDecimal dailyRate) throws SQLException {

        OvertimeSummary summary = new OvertimeSummary();
        BigDecimal hourlyRate = divideMoney(dailyRate, WORKING_HOURS_PER_DAY);

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

                    long validBlocks = workedMinutes / 30;
                    double validHours = (validBlocks * 30) / 60.0;

                    if (validHours <= 0) {
                        continue;
                    }

                    BigDecimal hours = new BigDecimal(String.valueOf(validHours))
                            .setScale(2, RoundingMode.HALF_UP);

                    BigDecimal pay = hourlyRate
                            .multiply(hours)
                            .multiply(overtimeMultiplier(rs.getInt("dayType")));

                    summary.overtimeHours = summary.overtimeHours.add(hours);
                    summary.overtimePay = summary.overtimePay.add(pay);
                }
            }
        }

        return summary;
    }

    private BigDecimal calculatePersonalIncomeTax(BigDecimal taxableIncome) {
        BigDecimal remaining = scale(taxableIncome);
        BigDecimal tax = ZERO;
        BigDecimal[] limits = {
            new BigDecimal("10000000"),
            new BigDecimal("20000000"),
            new BigDecimal("30000000"),
            new BigDecimal("40000000")
        };

        BigDecimal[] rates = {
            new BigDecimal("0.05"),
            new BigDecimal("0.10"),
            new BigDecimal("0.20"),
            new BigDecimal("0.30"),
            new BigDecimal("0.35")
        };

        for (int i = 0; i < rates.length; i++) {
            BigDecimal amount = i < limits.length ? remaining.min(limits[i]) : remaining;

            if (amount.signum() <= 0) {
                break;
            }

            tax = tax.add(amount.multiply(rates[i]));
            remaining = remaining.subtract(amount);
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
        return "SELECT e.employeeId, e.employeeCode, e.positionId, e.departmentId, "
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

    private BigDecimal overtimeMultiplier(int dayType) {
        switch (dayType) {
            case 2:
                return new BigDecimal("2.0");
            case 3:
                return new BigDecimal("3.0");
            case 1:
            default:
                return new BigDecimal("1.5");
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

    private void setNumeric(Row row, int column, BigDecimal value) {
        row.createCell(column).setCellValue(moneyOrZero(value).doubleValue());
    }

    private String buildPayrollNote(AttendanceSummary attendance, OvertimeSummary overtime) {
        return "paidLeaveDays=" + attendance.paidLeaveDays
                + "; unpaidLeaveDays=" + attendance.unpaidLeaveDays
                + "; unauthorizedAbsentDays=" + attendance.unauthorizedAbsentDays
                + "; lateMinutes=" + attendance.lateMinutes
                + "; overtimeHours=" + scale(overtime.overtimeHours);
    }

    private Set<String> getPermissions(User user) {
        return permissionDAO.getPermissionCodeByUserId(user.getUserId());
    }

    private boolean isHrStaff(User user) {
        String role = roleDAO.getRoleByUserId(user.getUserId());
        return role != null && role.contains("HR");
    }

    // ── Inner classes ──────────────────────────────────────────────────────────
    private static class EmployeePayrollBase {

        int employeeId;
        String employeeCode;
        int positionId;
        Integer departmentId;
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
        BigDecimal hoursWorked = BigDecimal.ZERO;
        BigDecimal latePenalty = BigDecimal.ZERO;
        BigDecimal unauthorizedAbsentPenalty = BigDecimal.ZERO;
    }

    private static class OvertimeSummary {

        BigDecimal overtimeHours = BigDecimal.ZERO;
        BigDecimal overtimePay = BigDecimal.ZERO;
    }
}
