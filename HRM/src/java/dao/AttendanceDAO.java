/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dal.DBContext;
import dto.AttendanceSummaryDTO;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Attendance;
import model.AttendanceAdjustment;
import model.Position;

/**
 *
 * @author admin
 */
public class AttendanceDAO {

    private static final Logger LOGGER = Logger.getLogger(AttendanceDAO.class.getName());


    private final DBContext dbContext;

    public AttendanceDAO() {
        this.dbContext = new DBContext();
    }

    public int findEmployeeIdByCode(String employeeCode) {
        String SQL = "SELECT employeeId FROM Employees WHERE employeeCode = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, employeeCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("employeeId");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot find employeeId by code: " + employeeCode, e);
        }
        return -1;
    }

    
    public model.Department getEmployeeDepartment(int employeeId) {
        String SQL = "SELECT e.departmentId, d.departmentName "
                + "FROM Employees e LEFT JOIN Departments d ON d.departmentId = e.departmentId "
                + "WHERE e.employeeId = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int deptId = rs.getInt("departmentId");
                    if (rs.wasNull() || deptId <= 0) {
                        return null;
                    }
                    model.Department dep = new model.Department();
                    dep.setDepartmentId(deptId);
                    dep.setDepartmentName(rs.getNString("departmentName"));
                    return dep;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get department for employeeId: " + employeeId, e);
        }
        return null;
    }


    public Position getEmployeePosition(int employeeId) {
        String SQL = "SELECT e.positionId, p.positionName "
                + "FROM Employees e LEFT JOIN Positions p ON p.positionId = e.positionId "
                + "WHERE e.employeeId = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int posId = rs.getInt("positionId");
                    if (rs.wasNull() || posId <= 0) {
                        return null;
                    }
                    model.Position pos = new model.Position();
                    pos.setPositionId(posId);
                    pos.setPositionName(rs.getString("positionName"));
                    return pos;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get position for employeeId: " + employeeId, e);
        }
        return null;
    }

    public boolean employeeBelongsToDepartment(int employeeId, int departmentId) {
        String SQL = "SELECT 1 FROM Employees WHERE employeeId = ? AND departmentId = ? LIMIT 1";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot check employee-department for employeeId: " + employeeId, e);
        }
        return false;
    }
    
    


    public boolean upsertAttendance(Connection conn, Attendance a) throws SQLException {
        String SQL = "INSERT INTO Attendance "
                + "(attendanceCode, employeeId, employeeCode, fullName, departmentId, departmentName, positionId, positionName, "
                + "workDate, timeIn, timeOut, hoursWorked, attendanceStatus, fileId) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "employeeCode = VALUES(employeeCode), fullName = VALUES(fullName), "
                + "departmentId = VALUES(departmentId), departmentName = VALUES(departmentName), "
                + "positionId = VALUES(positionId), positionName = VALUES(positionName), "
                + "timeIn = VALUES(timeIn), timeOut = VALUES(timeOut), hoursWorked = VALUES(hoursWorked), "
                + "attendanceStatus = VALUES(attendanceStatus), fileId = VALUES(fileId)";
        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, a.getAttendanceCode());
            ps.setInt(2, a.getEmployeeId());
            ps.setString(3, a.getEmployeeCode());
            ps.setNString(4, a.getFullName());
            if (a.getDepartmentId() != null) {
                ps.setInt(5, a.getDepartmentId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setNString(6, a.getDepartmentName());
            if (a.getPositionId() != null) {
                ps.setInt(7, a.getPositionId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setNString(8, a.getPositionName());
            ps.setDate(9, a.getWorkDate());
            if (a.getTimeIn() != null) {
                ps.setTime(10, a.getTimeIn());
            } else {
                ps.setNull(10, Types.TIME);
            }
            if (a.getTimeOut() != null) {
                ps.setTime(11, a.getTimeOut());
            } else {
                ps.setNull(11, Types.TIME);
            }
            if (a.getHoursWorked() != null) {
                ps.setBigDecimal(12, a.getHoursWorked());
            } else {
                ps.setNull(12, Types.DECIMAL);
            }
            ps.setInt(13, a.getAttendanceStatus());
            if (a.getFileId() != null) {
                ps.setInt(14, a.getFileId());
            } else {
                ps.setNull(14, Types.INTEGER);
            }
            return ps.executeUpdate() > 0;
        }
    }


    public List<Attendance> getAttendanceList(Integer departmentId, Integer month, Integer year,
            String employeeCode, Integer restrictEmployeeId) {
        List<Attendance> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT a.attendanceId, a.attendanceCode, a.employeeId, a.workDate, a.timeIn, a.timeOut, "
                + "a.hoursWorked, a.attendanceStatus, a.fileId, "
                + "a.employeeCode, a.departmentId, a.fullName, a.departmentName, a.positionId, a.positionName, "
                + "EXISTS(SELECT 1 FROM Attendance_Adjustment_History h WHERE h.attendanceId = a.attendanceId) AS isEdited "
                + "FROM Attendance a "
                + "WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (departmentId != null) {
            sql.append("AND a.departmentId = ? ");
            params.add(departmentId);
        }
        if (month != null) {
            sql.append("AND MONTH(a.workDate) = ? ");
            params.add(month);
        }
        if (year != null) {
            sql.append("AND YEAR(a.workDate) = ? ");
            params.add(year);
        }
        if (employeeCode != null && !employeeCode.trim().isEmpty()) {
            sql.append("AND a.employeeCode = ? ");
            params.add(employeeCode.trim());
        }
        if (restrictEmployeeId != null) {
            sql.append("AND a.employeeId = ? ");
            params.add(restrictEmployeeId);
        }
        sql.append("ORDER BY a.workDate DESC, a.employeeCode ASC");

        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAttendance(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve attendance list", e);
        }
        return list;
    }
    
    
    public List<Attendance> getAttendanceListByEmployeeId(int employeeId, Integer month, Integer year) {
        List<Attendance> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT a.attendanceId, a.attendanceCode, a.employeeId, a.workDate, a.timeIn, a.timeOut, "
                + "a.hoursWorked, a.attendanceStatus, a.fileId, "
                + "a.employeeCode, a.departmentId, a.fullName, a.departmentName, a.positionId, a.positionName, "
                + "EXISTS(SELECT 1 FROM Attendance_Adjustment_History h WHERE h.attendanceId = a.attendanceId) AS isEdited "
                + "FROM Attendance a "
                + "WHERE a.employeeId = ? ");

        List<Object> params = new ArrayList<>();
        params.add(employeeId);

        if (month != null && month > 0) {
            sql.append("AND MONTH(a.workDate) = ? ");
            params.add(month);
        }
        if (year != null && year > 0) {
            sql.append("AND YEAR(a.workDate) = ? ");
            params.add(year);
        }

        sql.append("ORDER BY a.workDate DESC");


        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAttendance(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve attendance list", e);
        }
        return list;
    }    


    public List<AttendanceSummaryDTO> getMonthlySummary(Integer departmentId, int month, int year) {
        List<dto.AttendanceSummaryDTO> list = new ArrayList<>();
        String sql =
                "SELECT a.employeeId, a.employeeCode, a.fullName, a.positionName, a.departmentName, "
                + "COALESCE(SUM(a.hoursWorked), 0) AS workedHours, "
                + "COALESCE(SUM(a.attendanceStatus = 0), 0) AS presentDays, "
                + "COALESCE(SUM(a.attendanceStatus = 1), 0) AS lateDays, "
                + "COALESCE(SUM(a.attendanceStatus = 4), 0) AS leaveDays, "
                + "COALESCE(SUM(a.attendanceStatus IN (2,3)), 0) AS absentDays, "
                + "COALESCE(SUM(a.attendanceStatus = 5), 0) AS holidayDays, "
                + "COALESCE(SUM(a.attendanceStatus = 6), 0) AS weekendDays, "
                + "COALESCE(SUM(a.attendanceStatus = 7), 0) AS missingCheckDays "
                + "FROM Employees e JOIN Attendance a ON a.employeeId = e.employeeId "
                + "WHERE e.status = 1 "
                + "  AND MONTH(a.workDate) = ? AND YEAR(a.workDate) = ? "
                + "  AND (? IS NULL OR e.departmentId = ?) "
                + "GROUP BY a.employeeId, a.employeeCode, a.fullName, a.positionName, a.departmentName "
                + "ORDER BY a.departmentName, e.employeeCode";

        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            if (departmentId != null) {
                ps.setInt(3, departmentId);
                ps.setInt(4, departmentId);
            } else {
                ps.setNull(3, Types.INTEGER);
                ps.setNull(4, Types.INTEGER);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dto.AttendanceSummaryDTO s = new dto.AttendanceSummaryDTO();
                    s.setEmployeeId(rs.getInt("employeeId"));
                    s.setEmployeeCode(rs.getString("employeeCode"));
                    s.setFullName(rs.getNString("fullName"));
                    s.setPositionName(rs.getString("positionName"));
                    s.setDepartmentName(rs.getNString("departmentName"));
                    s.setWorkedHours(rs.getBigDecimal("workedHours"));
                    s.setPresentDays(rs.getInt("presentDays"));
                    s.setLateDays(rs.getInt("lateDays"));
                    s.setLeaveDays(rs.getInt("leaveDays"));
                    s.setAbsentDays(rs.getInt("absentDays"));
                    s.setHolidayDays(rs.getInt("holidayDays"));
                    s.setWeekendDays(rs.getInt("weekendDays"));
                    s.setMissingCheckDays(rs.getInt("missingCheckDays"));
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve monthly attendance summary", e);
        }
        return list;
    }

    public List<Attendance> getDailyAttendance(int employeeId, int month, int year) {
        List<Attendance> list = new ArrayList<>();
        String sql =
                "SELECT a.attendanceId, a.attendanceCode, a.employeeId, a.workDate, a.timeIn, a.timeOut, "
                + "a.hoursWorked, a.attendanceStatus, a.fileId, "
                + "a.employeeCode, a.departmentId, a.fullName, a.departmentName, a.positionId, a.positionName, "
                + "EXISTS(SELECT 1 FROM Attendance_Adjustment_History h WHERE h.attendanceId = a.attendanceId) AS isEdited "
                + "FROM Attendance a "
                + "WHERE a.employeeId = ? AND MONTH(a.workDate) = ? AND YEAR(a.workDate) = ? "
                + "ORDER BY a.workDate ASC";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAttendance(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve daily attendance for employeeId: " + employeeId, e);
        }
        return list;
    }

    public Attendance getAttendanceById(int attendanceId) {
        String sql = "SELECT a.attendanceId, a.attendanceCode, a.employeeId, a.workDate, a.timeIn, a.timeOut, "
                + "a.hoursWorked, a.attendanceStatus, a.fileId, "
                + "a.employeeCode, a.departmentId, a.fullName, a.departmentName, a.positionId, a.positionName, "
                + "EXISTS(SELECT 1 FROM Attendance_Adjustment_History h WHERE h.attendanceId = a.attendanceId) AS isEdited "
                + "FROM Attendance a "
                + "WHERE a.attendanceId = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attendanceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAttendance(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve attendance by id: " + attendanceId, e);
        }
        return null;
    }

    public Attendance getAttendanceByDate(int employeeId, java.sql.Date workDate) {
        String sql = "SELECT a.attendanceId, a.attendanceCode, a.employeeId, a.workDate, a.timeIn, a.timeOut, "
                + "a.hoursWorked, a.attendanceStatus, a.fileId, "
                + "a.employeeCode, a.departmentId, a.fullName, a.departmentName, a.positionId, a.positionName, "
                + "EXISTS(SELECT 1 FROM Attendance_Adjustment_History h WHERE h.attendanceId = a.attendanceId) AS isEdited "
                + "FROM Attendance a "
                + "WHERE a.employeeId = ? AND a.workDate = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, workDate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAttendance(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve attendance by date: " + workDate, e);
        }
        return null;
    }


    public String updateAttendanceWithHistory(int attendanceId, Time timeIn, Time timeOut,
            BigDecimal hoursWorked, int newStatus, String reason, int updatedByUserId) {
        String selectSQL = "SELECT timeIn, timeOut, hoursWorked, attendanceStatus "
                + "FROM Attendance WHERE attendanceId = ? FOR UPDATE";
        try (Connection conn = dbContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Time oldIn;
                Time oldOut;
                BigDecimal oldHours;
                int oldStatus;
                try (PreparedStatement ps = conn.prepareStatement(selectSQL)) {
                    ps.setInt(1, attendanceId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return "Không tìm thấy bản ghi chấm công.";
                        }
                        oldIn = rs.getTime("timeIn");
                        oldOut = rs.getTime("timeOut");
                        oldHours = rs.getBigDecimal("hoursWorked");
                        oldStatus = rs.getInt("attendanceStatus");
                    }
                }

                String updateSQL = "UPDATE Attendance SET timeIn = ?, timeOut = ?, "
                        + "hoursWorked = ?, attendanceStatus = ? WHERE attendanceId = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSQL)) {
                    if (timeIn != null) {
                        ps.setTime(1, timeIn);
                    } else {
                        ps.setNull(1, Types.TIME);
                    }
                    if (timeOut != null) {
                        ps.setTime(2, timeOut);
                    } else {
                        ps.setNull(2, Types.TIME);
                    }
                    if (hoursWorked != null) {
                        ps.setBigDecimal(3, hoursWorked);
                    } else {
                        ps.setNull(3, Types.DECIMAL);
                    }
                    ps.setInt(4, newStatus);
                    ps.setInt(5, attendanceId);
                    ps.executeUpdate();
                }

                String historySQL = "INSERT INTO Attendance_Adjustment_History "
                        + "(attendanceId, oldValue, newValue, reason, updatedBy) "
                        + "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(historySQL)) {
                    ps.setInt(1, attendanceId);
                    ps.setNString(2, formatAdjustmentValue(oldIn, oldOut, oldHours, oldStatus));
                    ps.setNString(3, formatAdjustmentValue(timeIn, timeOut, hoursWorked, newStatus));
                    ps.setNString(4, reason);
                    ps.setInt(5, updatedByUserId);
                    ps.executeUpdate();
                }

                conn.commit();
                return null;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot update attendance for attendanceId: " + attendanceId, e);
            return "Lỗi hệ thống khi cập nhật dữ liệu chấm công.";
        }
    }

    public List<AttendanceAdjustment> getAdjustmentHistory(int attendanceId) {
        List<AttendanceAdjustment> list = new ArrayList<>();
        String SQL = "SELECT h.adjustmentId, h.attendanceId, h.oldValue, h.newValue, h.reason, "
                + "h.updatedBy, h.updatedAt, u.fullName AS updatedByName "
                + "FROM Attendance_Adjustment_History h "
                + "JOIN Users u ON u.userId = h.updatedBy "
                + "WHERE h.attendanceId = ? ORDER BY h.updatedAt DESC, h.adjustmentId DESC";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, attendanceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendanceAdjustment adj = new AttendanceAdjustment();
                    adj.setAdjustmentId(rs.getInt("adjustmentId"));
                    adj.setAttendanceId(rs.getInt("attendanceId"));
                    adj.setOldValue(rs.getNString("oldValue"));
                    adj.setNewValue(rs.getNString("newValue"));
                    adj.setReason(rs.getNString("reason"));
                    adj.setUpdatedBy(rs.getInt("updatedBy"));
                    adj.setUpdatedAt(rs.getTimestamp("updatedAt"));
                    adj.setUpdatedByName(rs.getNString("updatedByName"));
                    list.add(adj);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve adjustment history for attendanceId: " + attendanceId, e);
        }
        return list;
    }

    private String formatAdjustmentValue(Time timeIn, Time timeOut, BigDecimal hours, int status) {
        return "Giờ vào=" + (timeIn != null ? timeIn : "-")
                + "; Giờ ra=" + (timeOut != null ? timeOut : "-")
                + "; Giờ làm=" + formatHoursLabel(timeIn, timeOut, hours)
                + "; Trạng thái=" + statusLabelOf(status);
    }

    private String formatHoursLabel(Time timeIn, Time timeOut, BigDecimal hours) {
        long minutes;
        if (timeIn != null && timeOut != null) {
            minutes = utils.WorkHoursCalculator.workedMinutes(timeIn, timeOut);
        } else if (hours != null) {
            minutes = hours.multiply(BigDecimal.valueOf(60))
                    .setScale(0, java.math.RoundingMode.HALF_UP).longValue();
        } else {
            return "-";
        }
        return utils.WorkHoursCalculator.label(minutes);
    }

    private String statusLabelOf(int status) {
        switch (status) {
            case 0: return "Đúng giờ";
            case 1: return "Đi muộn";
            case 2: return "Vắng mặt";
            case 3: return "Không phép";
            case 4: return "Nghỉ phép";
            case 5: return "Nghỉ lễ";
            case 6: return "Cuối tuần";
            case 7: return "Quên chấm công";
            default: return "Không xác định";
        }
    }

    private Attendance mapAttendance(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setAttendanceId(rs.getInt("attendanceId"));
        a.setAttendanceCode(rs.getString("attendanceCode"));
        a.setEmployeeId(rs.getInt("employeeId"));
        a.setWorkDate(rs.getDate("workDate"));
        a.setTimeIn(rs.getTime("timeIn"));
        a.setTimeOut(rs.getTime("timeOut"));
        a.setHoursWorked(rs.getBigDecimal("hoursWorked"));
        a.setAttendanceStatus(rs.getInt("attendanceStatus"));
        int fileId = rs.getInt("fileId");
        a.setFileId(rs.wasNull() ? null : fileId);
        a.setEmployeeCode(rs.getString("employeeCode"));
        int departmentId = rs.getInt("departmentId");
        a.setDepartmentId(rs.wasNull() ? null : departmentId);
        a.setFullName(rs.getNString("fullName"));
        a.setDepartmentName(rs.getNString("departmentName"));
        int positionId = rs.getInt("positionId");
        a.setPositionId(rs.wasNull() ? null : positionId);
        a.setPositionName(rs.getNString("positionName"));
        a.setEdited(rs.getBoolean("isEdited"));
        return a;
    }
}
