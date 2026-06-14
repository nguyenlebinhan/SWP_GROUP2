/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dal.DBContext;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
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
import service.AttendancePeriodService;

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
    
    


    public boolean upsertAttendance(Connection conn, Attendance a) throws SQLException {
        String SQL = "INSERT INTO Attendance "
                + "(attendanceCode, employeeId, workDate, timeIn, timeOut, hoursWorked, attendanceStatus, fileId, periodId) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "timeIn = VALUES(timeIn), timeOut = VALUES(timeOut), hoursWorked = VALUES(hoursWorked), "
                + "attendanceStatus = VALUES(attendanceStatus), fileId = VALUES(fileId), periodId = VALUES(periodId)";
        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, a.getAttendanceCode());
            ps.setInt(2, a.getEmployeeId());
            ps.setDate(3, a.getWorkDate());
            if (a.getTimeIn() != null) {
                ps.setTime(4, a.getTimeIn());
            } else {
                ps.setNull(4, Types.TIME);
            }
            if (a.getTimeOut() != null) {
                ps.setTime(5, a.getTimeOut());
            } else {
                ps.setNull(5, Types.TIME);
            }
            if (a.getHoursWorked() != null) {
                ps.setBigDecimal(6, a.getHoursWorked());
            } else {
                ps.setNull(6, Types.DECIMAL);
            }
            ps.setInt(7, a.getAttendanceStatus());
            if (a.getFileId() != null) {
                ps.setInt(8, a.getFileId());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            if (a.getPeriodId() != null) {
                ps.setInt(9, a.getPeriodId());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            return ps.executeUpdate() > 0;
        }
    }


    public List<Attendance> getAttendanceList(Integer departmentId, Integer month, Integer year,
            String employeeCode, Integer restrictEmployeeId) {
        List<Attendance> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT a.attendanceId, a.attendanceCode, a.employeeId, a.workDate, a.timeIn, a.timeOut, "
                + "a.hoursWorked, a.attendanceStatus, a.fileId, a.periodId, "
                + "e.employeeCode, u.fullName, d.departmentName, "
                + "COALESCE(p.status, 0) AS periodStatus "
                + "FROM Attendance a "
                + "JOIN Employees e ON e.employeeId = a.employeeId "
                + "JOIN Users u ON u.userId = e.userId "
                + "LEFT JOIN Departments d ON d.departmentId = e.departmentId "
                + "LEFT JOIN Attendance_Periods p ON (a.periodId IS NOT NULL AND p.periodId = a.periodId) "
                + "OR (a.periodId IS NULL AND p.departmentId = e.departmentId "
                + "AND p.month = MONTH(a.workDate) AND p.year = YEAR(a.workDate)) "
                + "WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (departmentId != null) {
            sql.append("AND e.departmentId = ? ");
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
            sql.append("AND e.employeeCode = ? ");
            params.add(employeeCode.trim());
        }
        if (restrictEmployeeId != null) {
            sql.append("AND a.employeeId = ? ");
            params.add(restrictEmployeeId);
        }
        sql.append("ORDER BY a.workDate DESC, e.employeeCode ASC");

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
    
    
    public List<Attendance> getAttendanceListByUserId(int userId, Integer month, Integer year) {
        List<Attendance> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT a.attendanceId, a.attendanceCode, a.employeeId, a.workDate, a.timeIn, a.timeOut, "
                + "a.hoursWorked, a.attendanceStatus, a.fileId, a.periodId, "
                + "e.employeeCode, u.fullName, d.departmentName, "
                + "p.status AS periodStatus "
                + "FROM Attendance a "
                + "JOIN Employees e ON e.employeeId = a.employeeId "
                + "JOIN Users u ON u.userId = e.userId "
                + "LEFT JOIN Departments d ON d.departmentId = e.departmentId "
                + "LEFT JOIN Attendance_Periods p ON (a.periodId IS NOT NULL AND p.periodId = a.periodId) "
                + "OR (a.periodId IS NULL AND p.departmentId = e.departmentId "
                + "AND p.month = MONTH(a.workDate) AND p.year = YEAR(a.workDate)) "
                + "WHERE u.userId = ? AND p.status = 1 ");

        List<Object> params = new ArrayList<>();
        params.add(userId);

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
                + "a.hoursWorked, a.attendanceStatus, a.fileId, a.periodId, "
                + "e.employeeCode, u.fullName, d.departmentName, "
                + "COALESCE(p.status, 0) AS periodStatus "
                + "FROM Attendance a "
                + "JOIN Employees e ON e.employeeId = a.employeeId "
                + "JOIN Users u ON u.userId = e.userId "
                + "LEFT JOIN Departments d ON d.departmentId = e.departmentId "
                +"LEFT JOIN Attendance_Periods p ON (a.periodId IS NOT NULL AND p.periodId = a.periodId) "
                + "OR (a.periodId IS NULL AND p.departmentId = e.departmentId "
                + "AND p.month = MONTH(a.workDate) AND p.year = YEAR(a.workDate)) "
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
            BigDecimal hoursWorked, int newStatus, String reason,
            int updatedByUserId, AttendancePeriodService periodService) {
        String selectSQL = "SELECT a.timeIn, a.timeOut, a.hoursWorked, a.attendanceStatus, a.workDate, "
                + "p.status AS periodStatus, p.month AS periodMonth, p.year AS periodYear "
                + "FROM Attendance a "
                + "JOIN Employees e ON e.employeeId = a.employeeId "
                +"LEFT JOIN Attendance_Periods p ON (a.periodId IS NOT NULL AND p.periodId = a.periodId) "
                + "OR (a.periodId IS NULL AND p.departmentId = e.departmentId "
                + "AND p.month = MONTH(a.workDate) AND p.year = YEAR(a.workDate)) "
                + "WHERE a.attendanceId = ? FOR UPDATE";
        try (Connection conn = dbContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Time oldIn;
                Time oldOut;
                BigDecimal oldHours;
                int oldStatus;
                Integer periodStatus;
                int month;
                int year;
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
                        Date workDate = rs.getDate("workDate");
                        int pStatus = rs.getInt("periodStatus");
                        periodStatus = rs.wasNull() ? null : pStatus;
                        if (periodStatus != null) {
                            month = rs.getInt("periodMonth");
                            year = rs.getInt("periodYear");
                        } else {
                            month = workDate.toLocalDate().getMonthValue();
                            year = workDate.toLocalDate().getYear();
                        }
                    }
                }

                String err = periodService.checkEdit(periodStatus, month, year);
                if (err != null) {
                    conn.rollback();
                    return err;
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
        return "timeIn=" + (timeIn != null ? timeIn : "-")
                + "; timeOut=" + (timeOut != null ? timeOut : "-")
                + "; hoursWorked=" + (hours != null ? hours : "-")
                + "; status=" + status;
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
        int periodId = rs.getInt("periodId");
        a.setPeriodId(rs.wasNull() ? null : periodId);
        a.setEmployeeCode(rs.getString("employeeCode"));
        a.setFullName(rs.getNString("fullName"));
        a.setDepartmentName(rs.getNString("departmentName"));
        a.setPeriodStatus(rs.getInt("periodStatus"));
        return a;
    }
}
