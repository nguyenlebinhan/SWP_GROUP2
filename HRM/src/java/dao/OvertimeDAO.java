package dao;

import dal.DBContext;
import dto.PayrollOvertimeSummaryDTO;
import dto.OvertimeRequestDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OvertimeDAO {
    private static final Logger LOGGER = Logger.getLogger(OvertimeDAO.class.getName());
    private final DBContext dbContext;

    public OvertimeDAO() {
        this.dbContext = new DBContext();
    }

    public List<OvertimeRequestDTO> getOvertimeRequestsByManager(int managerId, String statusFilter, String dateFilter) {
        List<OvertimeRequestDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT f.formId, f.formCode, f.employeeId, f.formTypeId, f.reason, f.status, f.approverId, f.approverNote, f.approvedAt, f.createdAt, f.updatedAt, " +
                "od.otDate, od.startTime, od.endTime, od.dayType, " +
                "(SELECT COUNT(*) FROM Overtime_Assignees oa WHERE oa.formId = f.formId) AS totalAssignees, " +
                "u.fullName AS approverName " +
                "FROM Form_Requests f " +
                "JOIN Overtime_Details od ON f.formId = od.formId " +
                "LEFT JOIN Employees e ON e.employeeId = f.approverId " +
                "LEFT JOIN Users u ON u.userId = e.userId " +
                "WHERE f.employeeId = ? AND f.formTypeId = (SELECT formTypeId FROM Form_Types WHERE formTypeCode = 'OVERTIME' LIMIT 1) "
        );

        List<Object> params = new ArrayList<>();
        params.add(managerId);

        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            sql.append("AND f.status = ? ");
            params.add(Integer.parseInt(statusFilter));
        }

        if (dateFilter != null && !dateFilter.trim().isEmpty()) {
            sql.append("AND od.otDate = ? ");
            params.add(java.sql.Date.valueOf(dateFilter));
        }

        sql.append("ORDER BY f.createdAt DESC");

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OvertimeRequestDTO dto = new OvertimeRequestDTO();
                    dto.setFormId(rs.getInt("formId"));
                    dto.setFormCode(rs.getString("formCode"));
                    dto.setEmployeeId(rs.getInt("employeeId"));
                    dto.setFormTypeId(rs.getInt("formTypeId"));
                    dto.setReason(rs.getString("reason"));
                    dto.setStatus(rs.getInt("status"));
                    
                    int approverId = rs.getInt("approverId");
                    dto.setApproverId(rs.wasNull() ? null : approverId);
                    
                    dto.setApproverNote(rs.getString("approverNote"));
                    dto.setApprovedAt(rs.getTimestamp("approvedAt"));
                    dto.setCreatedAt(rs.getTimestamp("createdAt"));
                    dto.setUpdatedAt(rs.getTimestamp("updatedAt"));
                    
                    dto.setOtDate(rs.getDate("otDate"));
                    dto.setStartTime(rs.getTime("startTime"));
                    dto.setEndTime(rs.getTime("endTime"));
                    dto.setDayType(rs.getInt("dayType"));
                    dto.setTotalAssignees(rs.getInt("totalAssignees"));
                    dto.setApproverName(rs.getString("approverName"));
                    
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get overtime requests by manager", e);
        }
        return list;
    }

    public boolean addOvertimeDetails(int formId, String otDate, String startTime, String endTime, int dayType) {
        if (dayType != 1 && dayType != 2) {
            return false;
        }
        String SQL = "INSERT INTO Overtime_Details (formId, otDate, startTime, endTime, dayType) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, formId);
            ps.setDate(2, java.sql.Date.valueOf(otDate));
            
            if (startTime != null && startTime.length() == 5) startTime += ":00";
            if (endTime != null && endTime.length() == 5) endTime += ":00";
            
            ps.setTime(3, java.sql.Time.valueOf(startTime));
            ps.setTime(4, java.sql.Time.valueOf(endTime));
            ps.setInt(5, dayType);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot add overtime details", e);
            return false;
        }
    }

    public boolean addOvertimeAssignees(int formId, String[] assigneeIds) {
        if (assigneeIds == null || assigneeIds.length == 0) return false;
        
        String SQL = "INSERT INTO Overtime_Assignees (formId, employeeId) VALUES (?, ?)";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            for (String empIdStr : assigneeIds) {
                ps.setInt(1, formId);
                ps.setInt(2, Integer.parseInt(empIdStr));
                ps.addBatch();
            }
            int[] results = ps.executeBatch();
            return results.length > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot add overtime assignees", e);
            return false;
        }
    }
    public OvertimeRequestDTO getOvertimeRequestById(int formId) {
        String sql = "SELECT f.formId, f.formCode, f.employeeId, f.formTypeId, f.reason, f.status, f.approverId, f.approverNote, f.approvedAt, f.createdAt, f.updatedAt, " +
                "od.otDate, od.startTime, od.endTime, od.dayType, " +
                "(SELECT COUNT(*) FROM Overtime_Assignees oa WHERE oa.formId = f.formId) AS totalAssignees, " +
                "u.fullName AS approverName " +
                "FROM Form_Requests f " +
                "JOIN Overtime_Details od ON f.formId = od.formId " +
                "LEFT JOIN Employees e ON e.employeeId = f.approverId " +
                "LEFT JOIN Users u ON u.userId = e.userId " +
                "WHERE f.formId = ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, formId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OvertimeRequestDTO dto = new OvertimeRequestDTO();
                    dto.setFormId(rs.getInt("formId"));
                    dto.setFormCode(rs.getString("formCode"));
                    dto.setEmployeeId(rs.getInt("employeeId"));
                    dto.setFormTypeId(rs.getInt("formTypeId"));
                    dto.setReason(rs.getString("reason"));
                    dto.setStatus(rs.getInt("status"));
                    
                    int approverId = rs.getInt("approverId");
                    dto.setApproverId(rs.wasNull() ? null : approverId);
                    
                    dto.setApproverNote(rs.getString("approverNote"));
                    dto.setApprovedAt(rs.getTimestamp("approvedAt"));
                    dto.setCreatedAt(rs.getTimestamp("createdAt"));
                    dto.setUpdatedAt(rs.getTimestamp("updatedAt"));
                    
                    dto.setOtDate(rs.getDate("otDate"));
                    dto.setStartTime(rs.getTime("startTime"));
                    dto.setEndTime(rs.getTime("endTime"));
                    dto.setDayType(rs.getInt("dayType"));
                    dto.setTotalAssignees(rs.getInt("totalAssignees"));
                    dto.setApproverName(rs.getString("approverName"));
                    return dto;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get overtime request by id", e);
        }
        return null;
    }

    public List<dto.EmployeeDetailDTO> getOvertimeAssignees(int formId) {
        List<dto.EmployeeDetailDTO> list = new ArrayList<>();
        String sql = "SELECT e.*, u.fullName, u.email, u.username, d.departmentName, p.positionName, r.roleName, att.timeOut " +
                "FROM Overtime_Assignees oa " +
                "JOIN Overtime_Details od ON oa.formId = od.formId " +
                "JOIN Employees e ON oa.employeeId = e.employeeId " +
                "JOIN Users u ON e.userId = u.userId " +
                "LEFT JOIN Departments d ON e.departmentId = d.departmentId " +
                "LEFT JOIN Positions p ON e.positionId = p.positionId " +
                "LEFT JOIN Roles r ON u.roleId = r.roleId " +
                "LEFT JOIN Attendance att ON att.employeeId = oa.employeeId AND att.workDate = od.otDate " +
                "WHERE oa.formId = ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, formId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dto.EmployeeDetailDTO dto = new dto.EmployeeDetailDTO();
                    dto.setEmployeeId(rs.getInt("employeeId"));
                    dto.setEmployeeCode(rs.getString("employeeCode"));
                    dto.setUserId(rs.getInt("userId"));
                    dto.setDepartmentId(rs.getInt("departmentId"));
                    dto.setPositionId(rs.getInt("positionId"));
                    dto.setPhoneNumber(rs.getString("phoneNumber"));
                    dto.setSkills(rs.getString("skills"));
                    dto.setExperience(rs.getString("experience"));
                    dto.setDegree(rs.getString("degree"));
                    dto.setStatus(rs.getInt("status"));
                    
                    int managerId = rs.getInt("managerId");
                    dto.setManagerId(rs.wasNull() ? null : managerId);
                    
                    dto.setFullName(rs.getString("fullName"));
                    dto.setEmail(rs.getString("email"));
                    dto.setUsername(rs.getString("username"));
                    dto.setDepartmentName(rs.getString("departmentName"));
                    dto.setPositionName(rs.getString("positionName"));
                    dto.setRoleName(rs.getString("roleName"));
                    
                    java.sql.Time timeOut = rs.getTime("timeOut");
                    if (timeOut != null) {
                        java.sql.Time roundedTimeOut = utils.WorkHoursCalculator.ceilToBlock(timeOut);
                        java.sql.Time WORK_END = java.sql.Time.valueOf("17:00:00");
                        if (roundedTimeOut.after(WORK_END)) {
                            java.math.BigDecimal computed = utils.WorkHoursCalculator.hoursWorked(WORK_END, roundedTimeOut);
                            if (computed.compareTo(new java.math.BigDecimal("2.0")) > 0) {
                                computed = new java.math.BigDecimal("2.0");
                            }
                            dto.setOtHours(computed);
                        } else {
                            dto.setOtHours(java.math.BigDecimal.ZERO);
                        }
                    } else {
                        dto.setOtHours(java.math.BigDecimal.ZERO);
                    }
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get overtime assignees", e);
        }
        return list;
    }
    /**
     * Kiểm tra nhân viên có đơn OT đã được duyệt trong ngày làm việc cụ thể hay không.
     * Dùng để quyết định có cộng thêm giờ ngoài 8 tiếng chuẩn hay không.
     */
    public boolean hasApprovedOT(Connection conn, int employeeId, java.sql.Date workDate)
            throws SQLException {
        String sql = "SELECT 1 FROM Form_Requests fr "
                + "JOIN Overtime_Details od ON fr.formId = od.formId "
                + "JOIN Overtime_Assignees oa ON fr.formId = oa.formId "
                + "WHERE oa.employeeId = ? "
                + "  AND fr.status IN (1, 4) "
                + "  AND od.dayType IN (1, 2) "
                + "  AND od.otDate = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, workDate);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Phiên bản tự mở connection của {@link #hasApprovedOT(Connection, int, java.sql.Date)}.
     */
    public boolean hasApprovedOT(int employeeId, java.sql.Date workDate) {
        try (Connection conn = dbContext.getConnection()) {
            return hasApprovedOT(conn, employeeId, workDate);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot check approved OT for employee " + employeeId, e);
            return false;
        }
    }

    public List<Integer> getApprovedOTDaysInMonth(int employeeId, int month, int year) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT DAY(od.otDate) as otDay " +
                     "FROM Form_Requests fr " +
                     "JOIN Overtime_Details od ON fr.formId = od.formId " +
                     "JOIN Overtime_Assignees oa ON fr.formId = oa.formId " +
                     "LEFT JOIN Attendance att ON att.employeeId = oa.employeeId AND att.workDate = od.otDate " +
                     "WHERE oa.employeeId = ? " +
                     "AND fr.status IN (1, 4) " + 
                     "AND od.dayType IN (1, 2) " +
                     "AND MONTH(od.otDate) = ? " +
                     "AND YEAR(od.otDate) = ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt("otDay"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get approved OT days", e);
        }
        return list;
    }

    public PayrollOvertimeSummaryDTO getPayrollOvertimeSummary(Connection conn, int employeeId, int year, int month,
            BigDecimal dailyRate, BigDecimal workingHoursPerDay, int overtimeBlockMinutes,
            BigDecimal overtimeWorkdayMultiplier, BigDecimal overtimeWeekendMultiplier) throws SQLException {
        PayrollOvertimeSummaryDTO summary = new PayrollOvertimeSummaryDTO();
        BigDecimal hourlyRate = divide(dailyRate, workingHoursPerDay);
        String sql = "SELECT od.startTime, od.endTime, od.dayType, a.timeIn, a.timeOut "
                + "FROM Form_Requests fr "
                + "JOIN Overtime_Details od ON od.formId = fr.formId "
                + "LEFT JOIN Overtime_Assignees oa ON oa.formId = fr.formId "
                + "JOIN Attendance a ON a.employeeId = ? AND a.workDate = od.otDate "
                + "WHERE fr.status IN (1, 4) "
                + "AND fr.formTypeId = (SELECT formTypeId FROM Form_Types WHERE formTypeCode = 'OVERTIME') "
                + "AND (fr.employeeId = ? OR oa.employeeId = ?) "
                + "AND od.dayType IN (1, 2) "
                + "AND YEAR(od.otDate) = ? "
                + "AND MONTH(od.otDate) = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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

                    LocalTime validStart = approvedStart.isAfter(actualIn) ? approvedStart : actualIn;
                    LocalTime validEnd = approvedEnd.isBefore(actualOut) ? approvedEnd : actualOut;
                    if (!validEnd.isAfter(validStart)) {
                        continue;
                    }

                    long workedMinutes = java.time.Duration.between(validStart, validEnd).toMinutes();
                    long validBlocks = workedMinutes / overtimeBlockMinutes;
                    double validHours = (validBlocks * overtimeBlockMinutes) / 60.0;
                    if (validHours <= 0) {
                        continue;
                    }

                    BigDecimal hours = new BigDecimal(String.valueOf(validHours)).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal pay = hourlyRate
                            .multiply(hours)
                            .multiply(overtimeMultiplier(rs.getInt("dayType"), overtimeWorkdayMultiplier,
                                    overtimeWeekendMultiplier));
                    summary.addOvertimeBlocks((int) validBlocks);
                    summary.addOvertimeHours(hours);
                    summary.addOvertimePay(pay);
                }
            }
        }
        return summary;
    }

    private BigDecimal divide(BigDecimal amount, BigDecimal divisor) {
        if (amount == null || divisor == null || divisor.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return amount.divide(divisor, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal overtimeMultiplier(int dayType, BigDecimal workday, BigDecimal weekend) {
        switch (dayType) {
            case 2:
                return weekend;
            case 1:
                return workday;
            default:
                throw new IllegalArgumentException("Unsupported overtime day type: " + dayType);
        }
    }

    public void completeOTForm(Connection conn, int employeeId, java.sql.Date workDate) throws SQLException {
        String sql = "UPDATE Form_Requests fr " +
                     "JOIN Overtime_Details od ON fr.formId = od.formId " +
                     "JOIN Overtime_Assignees oa ON fr.formId = oa.formId " +
                     "SET fr.status = 4 " +
                     "WHERE oa.employeeId = ? AND od.otDate = ? AND od.dayType IN (1, 2) AND fr.status = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, workDate);
            ps.executeUpdate();
        }
    }

    public boolean reviveAndCompleteOTForm(int employeeId, java.sql.Date workDate) {
        try (Connection conn = dbContext.getConnection()) {
            return reviveAndCompleteOTForm(conn, employeeId, workDate);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot revive/complete OT form for employee " + employeeId, e);
            return false;
        }
    }

    public boolean reviveAndCompleteOTForm(Connection conn, int employeeId, java.sql.Date workDate) throws SQLException {
        String sql = "UPDATE Form_Requests fr " +
                     "JOIN Overtime_Details od ON fr.formId = od.formId " +
                     "JOIN Overtime_Assignees oa ON fr.formId = oa.formId " +
                     "SET fr.status = 4 " +
                     "WHERE oa.employeeId = ? AND od.otDate = ? AND od.dayType IN (1, 2) AND fr.status IN (1, 3)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, workDate);
            return ps.executeUpdate() > 0;
        }
    }


    public void cancelUnfulfilledOTForms(Connection conn, java.sql.Date workDate) throws SQLException {
        String sql = "UPDATE Form_Requests fr " +
                     "JOIN Overtime_Details od ON fr.formId = od.formId " +
                     "SET fr.status = 3 " +
                     "WHERE od.otDate = ? AND od.dayType IN (1, 2) AND fr.status = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, workDate);
            ps.executeUpdate();
        }
    }
}
