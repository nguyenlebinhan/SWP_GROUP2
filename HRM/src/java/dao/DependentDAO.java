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

    public boolean addPending(int formId, int employeeId, String fullName, String relationship, Date dateOfBirth, String taxCode, String note) {
        String sql = """
                INSERT INTO Dependents (formId, employeeId, fullName, relationship, dateOfBirth, taxCode, note, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, 0)
                """;
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, formId);
            ps.setInt(2, employeeId);
            ps.setNString(3, fullName);
            ps.setNString(4, relationship);
            ps.setDate(5, dateOfBirth);
            if (taxCode == null || taxCode.trim().isEmpty()) {
                ps.setNull(6, Types.VARCHAR);
            } else {
                ps.setString(6, taxCode.trim());
            }
            ps.setNString(7, note);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot add pending dependent for formId=" + formId, e);
        }
        return false;
    }

    public boolean approveByFormId(int formId) {
        return setStatusByFormId(formId, 1);
    }

    public boolean rejectByFormId(int formId) {
        return setStatusByFormId(formId, 2);
    }

    public boolean canRequestStatusChange(int dependentId, int employeeId) {
        String sql = """
                SELECT 1 FROM Dependents
                WHERE dependentId = ? AND employeeId = ? AND status = 1 AND pendingStatus IS NULL
                """;
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

    public boolean requestStatusChange(int dependentId, int employeeId, int formId, int targetStatus) {
        String sql = """
                UPDATE Dependents
                SET pendingStatus = ?, statusFormId = ?
                WHERE dependentId = ? AND employeeId = ? AND status = 1 AND pendingStatus IS NULL
                """;
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, targetStatus);
            ps.setInt(2, formId);
            ps.setInt(3, dependentId);
            ps.setInt(4, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot request dependent status change dependentId=" + dependentId, e);
        }
        return false;
    }

    public boolean approveStatusChangeByFormId(int formId) {
        String updateDependent = """
                UPDATE Dependents
                SET status = pendingStatus, pendingStatus = NULL, statusFormId = NULL, approvedAt = NOW()
                WHERE statusFormId = ? AND pendingStatus IS NOT NULL
                """;
        String syncEmployee = """
                UPDATE Employees e
                JOIN Dependents d ON d.employeeId = e.employeeId
                SET e.dependentCount = (
                    SELECT COUNT(*) FROM Dependents x
                    WHERE x.employeeId = e.employeeId AND x.status = 1
                )
                WHERE d.statusFormId IS NULL AND d.dependentId = ?
                """;
        return applyStatusChange(formId, updateDependent, syncEmployee);
    }

    public boolean rejectStatusChangeByFormId(int formId) {
        String updateDependent = """
                UPDATE Dependents
                SET pendingStatus = NULL, statusFormId = NULL
                WHERE statusFormId = ? AND pendingStatus IS NOT NULL
                """;
        return applyStatusChange(formId, updateDependent, null);
    }

    public List<Dependent> getActiveByEmployeeId(int employeeId) {
        List<Dependent> list = new ArrayList<>();
        String sql = """
                SELECT dependentId, employeeId, formId, fullName, relationship, dateOfBirth, taxCode, note, status, pendingStatus, approvedAt
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
                    d.setFormId(rs.getInt("formId"));
                    d.setFullName(rs.getNString("fullName"));
                    d.setRelationship(rs.getNString("relationship"));
                    d.setDateOfBirth(rs.getDate("dateOfBirth"));
                    d.setTaxCode(rs.getString("taxCode"));
                    d.setNote(rs.getNString("note"));
                    d.setStatus(rs.getInt("status"));
                    int pendingStatus = rs.getInt("pendingStatus");
                    d.setPendingStatus(rs.wasNull() ? null : pendingStatus);
                    d.setApprovedAt(rs.getTimestamp("approvedAt"));
                    list.add(d);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot list dependents for employeeId=" + employeeId, e);
        }
        return list;
    }

    private boolean setStatusByFormId(int formId, int status) {
        String updateDependent = "UPDATE Dependents SET status = ?, approvedAt = IF(? = 1, NOW(), approvedAt) WHERE formId = ?";
        String syncEmployee = """
                UPDATE Employees e
                JOIN Dependents d ON d.employeeId = e.employeeId
                SET e.dependentCount = (
                    SELECT COUNT(*) FROM Dependents x
                    WHERE x.employeeId = e.employeeId AND x.status = 1
                )
                WHERE d.formId = ?
                """;
        Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);
            int rows;
            try (PreparedStatement ps = conn.prepareStatement(updateDependent)) {
                ps.setInt(1, status);
                ps.setInt(2, status);
                ps.setInt(3, formId);
                rows = ps.executeUpdate();
            }
            if (rows > 0) {
                try (PreparedStatement ps = conn.prepareStatement(syncEmployee)) {
                    ps.setInt(1, formId);
                    ps.executeUpdate();
                }
            }
            conn.commit();
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot update dependent for formId=" + formId, e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
        return false;
    }

    private boolean applyStatusChange(int formId, String updateDependent, String syncEmployee) {
        Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);
            Integer dependentId = null;
            try (PreparedStatement find = conn.prepareStatement("SELECT dependentId FROM Dependents WHERE statusFormId = ?")) {
                find.setInt(1, formId);
                try (ResultSet rs = find.executeQuery()) {
                    if (rs.next()) {
                        dependentId = rs.getInt("dependentId");
                    }
                }
            }
            int rows;
            try (PreparedStatement ps = conn.prepareStatement(updateDependent)) {
                ps.setInt(1, formId);
                rows = ps.executeUpdate();
            }
            if (rows > 0 && syncEmployee != null && dependentId != null) {
                try (PreparedStatement ps = conn.prepareStatement(syncEmployee)) {
                    ps.setInt(1, dependentId);
                    ps.executeUpdate();
                }
            }
            conn.commit();
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot apply dependent status change formId=" + formId, e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
        return false;
    }
}
