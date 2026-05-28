/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
import model.Department;

/**
 *
 * @author admin
 */
public class DepartmentDAO {

    private static final Logger LOGGER = Logger.getLogger(DepartmentDAO.class.getName());
    private final DBContext dbContext;

    public DepartmentDAO() {
        this.dbContext = new DBContext();
    }

    public int countTotal() {
        String SQL = "SELECT COUNT(*) FROM departments WHERE status = 1";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot count total departments", e);
        }
        return 0;
    }

    public List<Department> getAllDepartments() {
        LOGGER.log(Level.INFO, "Get all departments");
        List<Department> list = new ArrayList<>();
        String SQL = "SELECT * FROM departments WHERE status = 1";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareCall(SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapDepartment(rs));
            }
            LOGGER.log(Level.INFO, "Retrieve {0} departments from DB.", list.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve departments from DB", e);
        }
        return list;
    }

    public Department getDepartmentById(int departmentId) {
        LOGGER.log(Level.INFO, "Get department by departmentId: {0}", departmentId);
        String SQL = "SELECT * FROM Departments WHERE departmentId = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapDepartment(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve department by departmentId: " + departmentId, e);
        }
        return null;
    }

    public boolean addDepartment(Department dept) {
        LOGGER.log(Level.INFO, "Adding new department with code: {0}", dept.getDepartmentCode());
        String SQL = """
            INSERT INTO departments
            (departmentCode, departmentName, description, managerId, maxHeadCount,
             status, region, budget, foundedDate)
            VALUES (?, ?, ?, ?, ?, 1, ?, ?, ?)
            """;
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, dept.getDepartmentCode());
            ps.setString(2, dept.getDepartmentName());
            ps.setString(3, dept.getDescription());
            if (dept.getManagerId() > 0) {
                ps.setInt(4, dept.getManagerId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if (dept.getMaxHeadCount() > 0) {
                ps.setInt(5, dept.getMaxHeadCount());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setString(6, dept.getRegion());
            ps.setDouble(7, dept.getBudget());
            if (dept.getFoundedDate() != null) {
                ps.setDate(8, Date.valueOf(dept.getFoundedDate()));
            } else {
                ps.setNull(8, Types.DATE);
            }

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Department added successfully with code: {0}", dept.getDepartmentCode());
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Add department failed: no rows affected for code: {0}", dept.getDepartmentCode());
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding department with code: " + dept.getDepartmentCode(), e);
        }
        return false;
    }

    public boolean updateDepartment(Department dept) {
        LOGGER.log(Level.INFO, "Updating department with departmentId: {0}", dept.getDepartmentId());
        String SQL = """
            UPDATE departments SET
                departmentName = ?, description = ?, managerId = ?,
                maxHeadCount = ?, region = ?, budget = ?, foundedDate = ?
            WHERE departmentId = ?
            """;
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, dept.getDepartmentName());
            ps.setString(2, dept.getDescription());
            if (dept.getManagerId() > 0) {
                ps.setInt(3, dept.getManagerId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            if (dept.getMaxHeadCount() > 0) {
                ps.setInt(4, dept.getMaxHeadCount());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, dept.getRegion());
            ps.setDouble(6, dept.getBudget());
            if (dept.getFoundedDate() != null) {
                ps.setDate(7, Date.valueOf(dept.getFoundedDate()));
            } else {
                ps.setNull(7, Types.DATE);
            }
            ps.setInt(8, dept.getDepartmentId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Department updated successfully with departmentId: {0}", dept.getDepartmentId());
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Update department failed: no rows affected for departmentId: {0}", dept.getDepartmentId());
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating department with departmentId: " + dept.getDepartmentId(), e);
        }
        return false;
    }

    public boolean deleteDepartment(int departmentId) {
        LOGGER.log(Level.INFO, "Deleting department with departmentId: {0}", departmentId);
        String SQL = "UPDATE departments SET status = 0 WHERE departmentId = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, departmentId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Department deleted successfully with departmentId: {0}", departmentId);
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Delete department failed: no rows affected for departmentId: {0}", departmentId);
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting department with departmentId: " + departmentId, e);
        }
        return false;
    }

    private Department mapDepartment(ResultSet rs) throws SQLException {
        Department d = new Department();
        d.setDepartmentId(rs.getInt("departmentId"));
        d.setDepartmentCode(rs.getString("departmentCode"));
        d.setDepartmentName(rs.getString("departmentName"));
        d.setDescription(rs.getString("description"));
        int mid = rs.getInt("managerId");
        d.setManagerId(rs.wasNull() ? 0 : mid);
        int mhc = rs.getInt("maxHeadCount");
        d.setMaxHeadCount(rs.wasNull() ? 0 : mhc);
        d.setStatus(rs.getInt("status"));
        d.setRegion(rs.getString("region"));
        d.setBudget(rs.getDouble("budget"));
        Date fd = rs.getDate("foundedDate");
        d.setFoundedDate(fd != null ? fd.toLocalDate() : null);
        return d;
    }
}
