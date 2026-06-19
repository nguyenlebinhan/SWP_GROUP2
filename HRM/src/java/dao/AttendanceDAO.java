/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dal.DBContext;
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
    
    


    public boolean insertAttendance(Connection conn, Attendance a) throws SQLException {
        String SQL = "INSERT INTO Attendance "
                + "(attendanceCode, employeeId, employeeCode, fullName, departmentId, departmentName, "
                + "workDate, timeIn, timeOut, hoursWorked, attendanceStatus, fileId) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "employeeCode = VALUES(employeeCode), fullName = VALUES(fullName), "
                + "departmentId = VALUES(departmentId), departmentName = VALUES(departmentName), "
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
            ps.setDate(7, a.getWorkDate());
            if (a.getTimeIn() != null) {
                ps.setTime(8, a.getTimeIn());
            } else {
                ps.setNull(8, Types.TIME);
            }
            if (a.getTimeOut() != null) {
                ps.setTime(9, a.getTimeOut());
            } else {
                ps.setNull(9, Types.TIME);
            }
            if (a.getHoursWorked() != null) {
                ps.setBigDecimal(10, a.getHoursWorked());
            } else {
                ps.setNull(10, Types.DECIMAL);
            }
            ps.setInt(11, a.getAttendanceStatus());
            if (a.getFileId() != null) {
                ps.setInt(12, a.getFileId());
            } else {
                ps.setNull(12, Types.INTEGER);
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
                + "a.employeeCode, a.departmentId, a.fullName, a.departmentName "
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
                + "a.employeeCode, a.departmentId, a.fullName, a.departmentName "
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

    public Attendance getAttendanceById(int attendanceId) {
        String sql = "SELECT a.attendanceId, a.attendanceCode, a.employeeId, a.workDate, a.timeIn, a.timeOut, "
                + "a.hoursWorked, a.attendanceStatus, a.fileId, "
                + "a.employeeCode, a.departmentId, a.fullName, a.departmentName "
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
        return "Giờ vào: " + (timeIn != null ? timeIn.toString().substring(0, 5) : "—")
                + " | Giờ ra: " + (timeOut != null ? timeOut.toString().substring(0, 5) : "—")
                + " | Số giờ: " + formatHours(timeIn, timeOut, hours)
                + " | Trạng thái: " + statusLabel(status);
    }

    private String formatHours(Time timeIn, Time timeOut, BigDecimal hours) {
        long minutes;
        if (timeIn != null && timeOut != null) {
            minutes = (timeOut.getTime() - timeIn.getTime()) / 60000L;
        } else if (hours != null) {
            minutes = hours.multiply(BigDecimal.valueOf(60))
                    .setScale(0, java.math.RoundingMode.HALF_UP).longValue();
        } else {
            return "—";
        }
        if (minutes < 0) {
            minutes = 0;
        }
        long h = minutes / 60;
        long m = minutes % 60;
        return h + "h" + String.format("%02d", m) + "m";
    }

    private String statusLabel(int status) {
        switch (status) {
            case 0: return "Đúng giờ";
            case 1: return "Đi muộn";
            case 2: return "Vắng mặt";
            case 3: return "Không phép";
            case 4: return "Nghỉ phép";
            case 5: return "Nghỉ lễ";
            case 6: return "Cuối tuần";
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
        return a;
    }
}
