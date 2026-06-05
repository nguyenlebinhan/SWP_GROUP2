/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dal.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Attendance;

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
    
    


    public boolean upsertAttendance(Attendance a) {
        String SQL = "INSERT INTO Attendance "
                + "(attendanceCode, employeeId, workDate, timeIn, timeOut, hoursWorked, attendanceStatus, fileId) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "timeIn = VALUES(timeIn), timeOut = VALUES(timeOut), hoursWorked = VALUES(hoursWorked), "
                + "attendanceStatus = VALUES(attendanceStatus), fileId = VALUES(fileId)";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL)) {
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
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot upsert attendance for employeeId: " + a.getEmployeeId(), e);
        }
        return false;
    }


    public List<Attendance> getAttendanceList(Integer departmentId, Integer month, Integer year,
            String employeeCode, Integer restrictEmployeeId) {
        List<Attendance> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT a.attendanceId, a.attendanceCode, a.employeeId, a.workDate, a.timeIn, a.timeOut, "
                + "a.hoursWorked, a.attendanceStatus, a.fileId, "
                + "e.employeeCode, u.fullName, d.departmentName "
                + "FROM Attendance a "
                + "JOIN Employees e ON e.employeeId = a.employeeId "
                + "JOIN Users u ON u.userId = e.userId "
                + "LEFT JOIN Departments d ON d.departmentId = e.departmentId "
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
    
    public List<Attendance> getAttendanceListByUserId(int userId) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.attendanceId, a.attendanceCode, a.employeeId, a.workDate, a.timeIn, a.timeOut, "
                + "a.hoursWorked, a.attendanceStatus, a.fileId, "
                + "e.employeeCode, u.fullName, d.departmentName "
                + "FROM Attendance a "
                + "JOIN Employees e ON e.employeeId = a.employeeId "
                + "JOIN Users u ON u.userId = e.userId "
                + "JOIN Departments d ON d.departmentId = e.departmentId "
                + "WHERE u.userId = ? ";


        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
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
        a.setFullName(rs.getNString("fullName"));
        a.setDepartmentName(rs.getNString("departmentName"));
        return a;
    }
}
