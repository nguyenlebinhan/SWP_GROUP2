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
        List<Department> list = new ArrayList<>();
        String SQL = "SELECT d.departmentId, d.departmentCode, d.departmentName, d.description, d.managerId, d.maxHeadCount, d.status, d.region, d.foundedDate FROM Departments d ORDER BY d.departmentName";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapDepartment(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve all departments", e);
        }
        return list;
    }

    public List<Department> getAllActiveDepartments() {
        List<Department> list = new ArrayList<>();
        String SQL = "SELECT d.departmentId, d.departmentCode, d.departmentName, d.description, d.managerId, d.maxHeadCount, d.status, d.region, d.foundedDate FROM Departments d WHERE d.status = 1 ORDER BY departmentName";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapDepartment(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve active departments", e);
        }
        return list;
    }

    public Department getDepartmentById(int id) {
        String SQL = "SELECT d.departmentId, d.departmentCode, d.departmentName, d.description, d.managerId, d.maxHeadCount, d.status, d.region, d.foundedDate FROM Departments d WHERE d.departmentId = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapDepartment(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve department by id: " + id, e);
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
    public int countEmployeesByDepartmentId(int departmentId) {
        String SQL = "SELECT COUNT(*) FROM Employees WHERE departmentId = ? AND status != 0";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot count employees for departmentId: " + departmentId, e);
        }
        return 0;
    }

    public List<Position> getAllPositions() {
        List<Position> list = new ArrayList<>();
        String SQL = "SELECT positionId, positionName, level, description FROM Positions ORDER BY level DESC, positionName";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Position p = new Position();
                p.setPositionId(rs.getInt("positionId"));
                p.setPositionName(rs.getString("positionName"));
                p.setLevel(rs.getInt("level"));
                p.setDescription(rs.getNString("description"));
                list.add(p);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve positions", e);
        }
        return list;
    }

    private Department mapDepartment(ResultSet rs) throws SQLException {
        Department d = new Department();
        d.setDepartmentId(rs.getInt("departmentId"));
        d.setDepartmentCode(rs.getString("departmentCode"));
        d.setDepartmentName(rs.getNString("departmentName"));
        d.setDescription(rs.getNString("description"));
        int managerId = rs.getInt("managerId");
        d.setManagerId(rs.wasNull() ? null : managerId);
        int maxHead = rs.getInt("maxHeadCount");
        d.setMaxHeadCount(rs.wasNull() ? null : maxHead);
        d.setStatus(rs.getInt("status"));
        d.setRegion(rs.getNString("region"));
        d.setFoundedDate(rs.getString("foundedDate"));
       
        return d;
    }
}
