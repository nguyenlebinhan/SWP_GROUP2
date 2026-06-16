/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dal.DBContext;
import dto.AttendancePeriodSummaryDTO;
import enums.AttendancePeriodStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.AttendancePeriod;

/**
 *
 * @author admin
 */
public class AttendancePeriodDAO {

    private static final Logger LOGGER = Logger.getLogger(AttendancePeriodDAO.class.getName());
    private final DBContext dbContext;

    public AttendancePeriodDAO() {
        this.dbContext = new DBContext();
    }

    public AttendancePeriod getPeriod(int departmentId, int month, int year) {
        String SQL = "SELECT * FROM Attendance_Periods WHERE departmentId = ? AND month = ? AND year = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, departmentId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AttendancePeriod p = new AttendancePeriod();
                    p.setPeriodId(rs.getInt("periodId"));
                    p.setDepartmentId(rs.getInt("departmentId"));
                    p.setMonth(rs.getInt("month"));
                    p.setYear(rs.getInt("year"));
                    p.setStatus(rs.getInt("status"));
                    int publishedBy = rs.getInt("publishedBy");
                    p.setPublishedBy(rs.wasNull() ? null : publishedBy);
                    p.setPublishedAt(rs.getTimestamp("publishedAt"));
                    return p;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get attendance period", e);
        }
        return null;
    }
    


    public boolean insertPeriod(int departmentId, int month, int year) {
        String SQL = "INSERT INTO Attendance_Periods(departmentId,month,year) VALUES(?,?,?,1) ON DUPLICATE KEY UPDATE periodId = periodId";        
        try (Connection conn = dbContext.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, departmentId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            ps.executeUpdate();            
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot ensure attendance period", e);
        }
        return false;
    }

    public AttendancePeriod getPeriodForUpdate(Connection conn, int departmentId, int month, int year)
            throws SQLException {
        String SQL = "SELECT * FROM Attendance_Periods WHERE departmentId = ? AND month = ? AND year = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, departmentId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AttendancePeriod p = new AttendancePeriod();
                    p.setPeriodId(rs.getInt("periodId"));
                    p.setDepartmentId(rs.getInt("departmentId"));
                    p.setMonth(rs.getInt("month"));
                    p.setYear(rs.getInt("year"));
                    p.setStatus(rs.getInt("status"));
                    int publishedBy = rs.getInt("publishedBy");
                    p.setPublishedBy(rs.wasNull() ? null : publishedBy);
                    p.setPublishedAt(rs.getTimestamp("publishedAt"));
                    return p;
                }
            }
        }
        return null;
    }
    

    public boolean setStatusConditional(int periodId, int expectedStatus, int newStatus,
            Integer publishedByEmployeeId) {
        String SQL = "UPDATE Attendance_Periods SET status = ?, "
                + "publishedBy = CASE WHEN ? = 1 THEN ? ELSE NULL END, "
                + "publishedAt = CASE WHEN ? = 1 THEN CURRENT_TIMESTAMP ELSE NULL END "
                + "WHERE periodId = ? AND status = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, newStatus);
            ps.setInt(2, newStatus);
            if (publishedByEmployeeId != null) {
                ps.setInt(3, publishedByEmployeeId);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setInt(4, newStatus);
            ps.setInt(5, periodId);
            ps.setInt(6, expectedStatus);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot set attendance period status conditionally", e);
        }
        return false;
    }


    public List<AttendancePeriodSummaryDTO> getPeriodSummaries(int month, int year) {
        List<AttendancePeriodSummaryDTO> list = new ArrayList<>();
            String SQL = "SELECT "
            + "    d.departmentId, "
            + "    d.departmentName, "
            + "    p.periodId, "
            + "    p.status, "
            + "    p.publishedAt, "
            + "    u.fullName AS publishedByName, "
            + "    COALESCE(f_stat.fileCount, 0) AS fileCount, "
            + "    COALESCE(f_stat.importedRows, 0) AS importedRows, "
            + "    COALESCE(f_stat.failedRows, 0) AS failedRows "
            + "FROM Departments d "
            + "LEFT JOIN Attendance_Periods p ON p.departmentId = d.departmentId "
            + "    AND p.month = ? AND p.year = ? " // Cặp ? số 1 (Index 1, 2)
            + "LEFT JOIN Employees pe ON pe.employeeId = p.publishedBy "
            + "LEFT JOIN Users u ON u.userId = pe.userId "
            + "LEFT JOIN ("
            + "    SELECT "
            + "        f.departmentId, "
            + "        COUNT(DISTINCT f.fileId) AS fileCount, "
            + "        COUNT(CASE WHEN r.validateStatus = 1 THEN 1 END) AS importedRows, "
            + "        COUNT(CASE WHEN r.validateStatus = 0 THEN 1 END) AS failedRows "
            + "    FROM Uploaded_Files f "
            + "    LEFT JOIN Attendance_Import_Rows r ON f.fileId = r.fileId "
            + "    WHERE f.fileType = 'ATTENDANCE' AND f.month = ? AND f.year = ? " // Cặp ? số 2 (Index 3, 4)
            + "    GROUP BY f.departmentId "
            + ") f_stat ON f_stat.departmentId = d.departmentId "
            + "WHERE d.status = 1 "
            + "ORDER BY d.departmentName";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL)) {
            for (int i = 0; i < 2; i++) {
                ps.setInt(i * 2 + 1, month);
                ps.setInt(i * 2 + 2, year);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendancePeriodSummaryDTO dto = new AttendancePeriodSummaryDTO();
                    dto.setDepartmentId(rs.getInt("departmentId"));
                    dto.setDepartmentName(rs.getNString("departmentName"));
                    int periodId = rs.getInt("periodId");
                    dto.setPeriodId(rs.wasNull() ? null : periodId);
                    int status = rs.getInt("status");
                    dto.setStatus(rs.wasNull() ? null : status);
                    dto.setPublishedByName(rs.getNString("publishedByName"));
                    dto.setPublishedAt(rs.getTimestamp("publishedAt"));
                    dto.setFileCount(rs.getInt("fileCount"));
                    dto.setImportedRows(rs.getInt("importedRows"));
                    dto.setFailedRows(rs.getInt("failedRows"));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get attendance period summaries", e);
        }
        return list;
    }


    public int countPublishedDepartments(int month, int year) {
        String SQL = "SELECT COUNT(*) FROM Attendance_Periods WHERE month = ? AND year = ? AND status = 1";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot count published departments", e);
        }
        return 0;
    }
}
