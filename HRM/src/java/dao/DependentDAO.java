package dao;

import dal.DBContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Dependent;

public class DependentDAO {

    private static final Logger LOGGER = Logger.getLogger(DependentDAO.class.getName());
    private final DBContext dbContext = new DBContext();

    public boolean checkTaxCodeExist(String taxCode) {
        if (taxCode == null || taxCode.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT 1 FROM Dependents WHERE taxCode = ? AND status = 1";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, taxCode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot check taxCode existence: " + taxCode, e);
        }
        return false;
    }

    public boolean isDuplicateDependent(int employeeId, String fullName, java.util.Date dateOfBirth, String taxCode) {
        if (taxCode != null && !taxCode.trim().isEmpty()) {
            if (checkTaxCodeExist(taxCode)) {
                return true;
            }
        }
        String sqlCond = (dateOfBirth == null) ? "AND dateOfBirth IS NULL" : "AND dateOfBirth = ?";
        String sql = "SELECT 1 FROM Dependents WHERE employeeId = ? AND LOWER(fullName) = LOWER(?) " + sqlCond + " AND status = 1";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setNString(2, fullName != null ? fullName.trim() : "");
            if (dateOfBirth != null) {
                ps.setDate(3, new java.sql.Date(dateOfBirth.getTime()));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot check duplicate dependent for employeeId=" + employeeId, e);
        }
        return false;
    }

    public boolean insertApprovedDependent(int employeeId, String fullName, String relationship, java.util.Date dateOfBirth, String taxCode, String note) {
        String insertSQL = """
                INSERT INTO Dependents (employeeId, fullName, relationship, dateOfBirth, taxCode, note, status)
                VALUES (?, ?, ?, ?, ?, ?, 1)
                """;
        String syncEmployee = """
                UPDATE Employees SET dependentCount = (
                    SELECT COUNT(*) FROM Dependents WHERE employeeId = ? AND status = 1
                ) WHERE employeeId = ?
                """;
        Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);
            int rows;
            try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
                ps.setInt(1, employeeId);
                ps.setNString(2, fullName);
                ps.setNString(3, relationship);
                if (dateOfBirth == null) ps.setNull(4, Types.DATE); else ps.setDate(4, new java.sql.Date(dateOfBirth.getTime()));
                if (taxCode == null || taxCode.trim().isEmpty()) ps.setNull(5, Types.VARCHAR); else ps.setString(5, taxCode.trim());
                ps.setNString(6, note);
                rows = ps.executeUpdate();
            }
            if (rows > 0) {
                try (PreparedStatement ps = conn.prepareStatement(syncEmployee)) {
                    ps.setInt(1, employeeId);
                    ps.setInt(2, employeeId);
                    ps.executeUpdate();
                }
            }
            conn.commit();
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot insert approved dependent for employeeId=" + employeeId, e);
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
        return false;
    }

    public boolean deactivateDependent(int dependentId, int employeeId) {
        String updateSQL = "UPDATE Dependents SET status = 0 WHERE dependentId = ? AND employeeId = ?";
        String syncEmployee = """
                UPDATE Employees SET dependentCount = (
                    SELECT COUNT(*) FROM Dependents WHERE employeeId = ? AND status = 1
                ) WHERE employeeId = ?
                """;
        Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);
            int rows;
            try (PreparedStatement ps = conn.prepareStatement(updateSQL)) {
                ps.setInt(1, dependentId);
                ps.setInt(2, employeeId);
                rows = ps.executeUpdate();
            }
            if (rows > 0) {
                try (PreparedStatement ps = conn.prepareStatement(syncEmployee)) {
                    ps.setInt(1, employeeId);
                    ps.setInt(2, employeeId);
                    ps.executeUpdate();
                }
            }
            conn.commit();
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot deactivate dependent dependentId=" + dependentId, e);
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
        return false;
    }

    public boolean canRequestStatusChange(int dependentId, int employeeId) {
        String sql = "SELECT 1 FROM Dependents WHERE dependentId = ? AND employeeId = ? AND status = 1";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dependentId);
            ps.setInt(2, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot check dependent status change dependentId=" + dependentId, e);
        }
        return false;
    }

    public List<Dependent> getActiveByEmployeeId(int employeeId) {
        List<Dependent> list = new ArrayList<>();
        String sql = """
                SELECT dependentId, employeeId, fullName, relationship, dateOfBirth, taxCode, note, status
                FROM Dependents
                WHERE employeeId = ? AND status = 1
                ORDER BY dependentId DESC
                """;
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Dependent d = new Dependent();
                    d.setDependentId(rs.getInt("dependentId"));
                    d.setEmployeeId(rs.getInt("employeeId"));
                    d.setFormId(0);
                    d.setFullName(rs.getNString("fullName"));
                    d.setRelationship(rs.getNString("relationship"));
                    d.setDateOfBirth(rs.getDate("dateOfBirth"));
                    d.setTaxCode(rs.getString("taxCode"));
                    d.setNote(rs.getNString("note"));
                    d.setStatus(rs.getInt("status"));
                    d.setPendingStatus(null);
                    d.setApprovedAt(null);
                    list.add(d);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot list dependents for employeeId=" + employeeId, e);
        }
        return list;
    }

    // Deprecated methods kept temporarily as safe no-ops to preserve compilation during phase progression
    public boolean addPending(int formId, int employeeId, String fullName, String relationship, Date dateOfBirth, String taxCode, String note) { return true; }
    public boolean approveByFormId(int formId) { return true; }
    public boolean rejectByFormId(int formId) { return true; }
    public boolean requestStatusChange(int dependentId, int employeeId, int formId, int targetStatus) { return true; }
    public boolean approveStatusChangeByFormId(int formId) { return true; }
    public boolean rejectStatusChangeByFormId(int formId) { return true; }
}
