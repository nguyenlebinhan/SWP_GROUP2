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
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Department;
import model.Position;

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
        String SQL = "SELECT d.departmentId, d.departmentCode, d.departmentName, d.description, d.managerId, d.status FROM Departments d ORDER BY d.departmentName";
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
        String SQL = "SELECT d.departmentId, d.departmentCode, d.departmentName, d.description, d.managerId,d.status FROM Departments d WHERE d.status = 1 ORDER BY departmentName";
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
        String SQL = "SELECT d.departmentId, d.departmentCode, d.departmentName, d.description, d.managerId, d.status FROM Departments d WHERE d.departmentId = ?";
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

    public String getDepartmentCodeById(int departmentId) {
        String SQL = "SELECT departmentCode FROM Departments WHERE departmentId = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("departmentCode");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get departmentCode for id: " + departmentId, e);
        }
        return null;
    }

    
    public int addDepartment(Department dept) {
        LOGGER.log(Level.INFO, "Adding new department with code: {0}", dept.getDepartmentCode());
        String SQL = "INSERT INTO departments(departmentCode, departmentName, description,status) VALUES (?, ?, ?, 1)";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dept.getDepartmentCode());
            ps.setNString(2, dept.getDepartmentName());
            ps.setNString(3, dept.getDescription());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int newId = rs.getInt(1);
                        LOGGER.log(Level.INFO, "Department added successfully with code: {0}", dept.getDepartmentCode());
                        return newId;
                    }
                }
            }
            LOGGER.log(Level.WARNING, "Add department failed: no rows affected for code: {0}", dept.getDepartmentCode());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding department with code: " + dept.getDepartmentCode(), e);
        }
        return -1;
    }

    public boolean isRoleAllowedForDepartment(int departmentId, int roleId) {
        String countSQL = "SELECT COUNT(*) FROM Department_Roles WHERE departmentId = ?";
        String matchSQL = "SELECT 1 FROM Department_Roles WHERE departmentId = ? AND roleId = ? LIMIT 1";
        try (Connection conn = dbContext.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(countSQL)) {
                ps.setInt(1, departmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        return true;
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(matchSQL)) {
                ps.setInt(1, departmentId);
                ps.setInt(2, roleId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot check role-department compatibility for dept: " + departmentId, e);
        }
        return false;
    }

    /** Danh sách tên vai trò được phép trong phòng ban — dùng cho thông báo lỗi. */
    public List<String> getAllowedRoleNames(int departmentId) {
        List<String> names = new ArrayList<>();
        String SQL = "SELECT r.roleName FROM Department_Roles dr "
                   + "JOIN Roles r ON r.roleId = dr.roleId "
                   + "WHERE dr.departmentId = ? ORDER BY r.roleName";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) names.add(rs.getString("roleName"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get allowed roles for dept: " + departmentId, e);
        }
        return names;
    }

    /** Ghi đè toàn bộ luật vai trò của một phòng ban (xóa cũ rồi chèn mới) trong 1 transaction. */
    public boolean replaceDepartmentRoles(int departmentId, List<Integer> roleIds) {
        String deleteSQL = "DELETE FROM Department_Roles WHERE departmentId = ?";
        String insertSQL = "INSERT INTO Department_Roles (departmentId, roleId) VALUES (?, ?)";
        Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement del = conn.prepareStatement(deleteSQL)) {
                del.setInt(1, departmentId);
                del.executeUpdate();
            }

            if (roleIds != null && !roleIds.isEmpty()) {
                try (PreparedStatement ins = conn.prepareStatement(insertSQL)) {
                    for (int roleId : roleIds) {
                        ins.setInt(1, departmentId);
                        ins.setInt(2, roleId);
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
            }

            conn.commit();
            LOGGER.log(Level.INFO, "Updated role rules for departmentId={0}: {1} role(s)",
                    new Object[]{departmentId, roleIds == null ? 0 : roleIds.size()});
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot replace department roles for dept: " + departmentId, e);
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

    public boolean updateDepartmentInfo(Department dept) {
        LOGGER.log(Level.INFO, "Updating department info with departmentId: {0}", dept.getDepartmentId());
        String SQL = "UPDATE departments SET departmentName = ?, description = ? WHERE departmentId = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, dept.getDepartmentName());
            ps.setString(2, dept.getDescription());
            ps.setInt(3, dept.getDepartmentId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Department info updated successfully with departmentId: {0}", dept.getDepartmentId());
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Update department info failed: no rows affected for departmentId: {0}", dept.getDepartmentId());
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating department info with departmentId: " + dept.getDepartmentId(), e);
        }
        return false;
    }

    public boolean updateDepartment(Department dept) {
        LOGGER.log(Level.INFO, "Updating department with departmentId: {0}", dept.getDepartmentId());
        String SQL = " UPDATE departments SET departmentName = ?, description = ?, managerId = ? WHERE departmentId = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, dept.getDepartmentName());
            ps.setString(2, dept.getDescription());
            ps.setInt(3, dept.getManagerId());
            ps.setString(4, dept.getRegion());
            ps.setString(5,dept.getFoundedDate());
            ps.setInt(6, dept.getDepartmentId());

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
        String SQL = "SELECT positionId, positionName, level, description FROM Positions ";
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
        d.setStatus(rs.getInt("status"));
        return d;
    }
}
