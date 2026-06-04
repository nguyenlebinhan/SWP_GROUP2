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
import java.sql.Statement;
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
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot count total departments", e);
        }
        return 0;
    }

    public List<Department> getAllDepartments() {
        List<Department> list = new ArrayList<>();
        String SQL = "SELECT d.departmentId, d.departmentCode, d.departmentName, d.description, d.managerId, d.status FROM Departments d ORDER BY d.departmentName";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL); ResultSet rs = ps.executeQuery()) {
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
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL); ResultSet rs = ps.executeQuery()) {
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
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapDepartment(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve department by id: " + id, e);
        }
        return null;
    }

    public String getDepartmentCodeById(int departmentId) {
        String SQL = "SELECT departmentCode FROM Departments WHERE departmentId = ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("departmentCode");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get departmentCode for id: " + departmentId, e);
        }
        return null;
    }

    public int addDepartment(Department dept) {
        LOGGER.log(Level.INFO, "Adding new department with code: {0}", dept.getDepartmentCode());
        String SQL = "INSERT INTO departments(departmentCode, departmentName, description,status) VALUES (?, ?, ?, 1)";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
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

    public List<String> getAllowedRoleNames(int departmentId) {
        List<String> names = new ArrayList<>();
        String SQL = "SELECT r.roleName FROM Department_Roles dr "
                + "JOIN Roles r ON r.roleId = dr.roleId "
                + "WHERE dr.departmentId = ? ORDER BY r.roleName";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString("roleName"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get allowed roles for dept: " + departmentId, e);
        }
        return names;
    }

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

    public boolean updateDepartmentInfo(Department dept) {
        LOGGER.log(Level.INFO, "Updating department info with departmentId: {0}", dept.getDepartmentId());
        String SQL = "UPDATE departments SET departmentName = ?, description = ?, status = ? WHERE departmentId = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, dept.getDepartmentName());
            ps.setString(2, dept.getDescription());
            ps.setInt(3, dept.getStatus());
            ps.setInt(4, dept.getDepartmentId());

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
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, dept.getDepartmentName());
            ps.setString(2, dept.getDescription());
            ps.setInt(3, dept.getManagerId());
            ps.setString(4, dept.getRegion());
            ps.setString(5, dept.getFoundedDate());
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
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
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
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot count employees for departmentId: " + departmentId, e);
        }
        return 0;
    }

    public List<Position> getAllPositions() {
        List<Position> list = new ArrayList<>();
        String SQL = "SELECT positionId, positionName, level, description FROM Positions ";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL); ResultSet rs = ps.executeQuery()) {
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

    public List<model.Employee> getAssignableManagers(int baUserId) {
        List<model.Employee> list = new java.util.ArrayList<>();
        // roleId của BA (người đang đăng nhập) → chỉ lấy nhân viên có roleId > đó
        String SQL
                = "SELECT e.employeeId, e.employeeCode, e.userId, e.departmentId, e.positionId, "
                + "       e.phoneNumber, e.skills, e.experience, e.degree, e.status, e.managerId, "
                + "       u.fullName, u.email, r.roleName, r.roleId "
                + "FROM Employees e "
                + "JOIN Users u ON u.userId = e.userId "
                + "JOIN Roles r ON r.roleId = u.roleId "
                + "WHERE u.isActive = 1 "
                + "  AND e.status = 1 "
                + // role phải thấp hơn BA: roleId > roleId của BA hiện tại
                "  AND r.roleId > (SELECT u2.roleId FROM Users u2 WHERE u2.userId = ?) "
                + // chưa là manager của phòng ban active nào
                "  AND e.employeeId NOT IN ( "
                + "      SELECT d.managerId FROM Departments d "
                + "      WHERE d.managerId IS NOT NULL AND d.status = 1 "
                + "  ) "
                + "ORDER BY u.fullName";
        try (java.sql.Connection conn = dbContext.getConnection(); java.sql.PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, baUserId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.Employee emp = new model.Employee();
                    emp.setEmployeeId(rs.getInt("employeeId"));
                    emp.setEmployeeCode(rs.getString("employeeCode"));
                    emp.setUserId(rs.getInt("userId"));
                    emp.setDepartmentId(rs.getInt("departmentId"));
                    emp.setPositionId(rs.getInt("positionId"));
                    emp.setPhoneNumber(rs.getString("phoneNumber"));
                    emp.setSkills(rs.getNString("skills"));
                    emp.setExperience(rs.getNString("experience"));
                    emp.setDegree(rs.getNString("degree"));
                    emp.setStatus(rs.getInt("status"));
                    int mgr = rs.getInt("managerId");
                    emp.setManagerId(rs.wasNull() ? null : mgr);
                    // fullName và roleName lưu tạm vào transient — xem ghi chú bên dưới (*)
                    // Vì Employee model không có fullName, ta dùng EmployeeDetailDTO thay thế
                    list.add(emp);
                }
            }
        } catch (java.sql.SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Cannot get assignable managers", e);
        }
        return list;
    }

    public List<dto.EmployeeDetailDTO> getAssignableManagerDTOs(int baUserId, int departmentId) {
        List<dto.EmployeeDetailDTO> list = new java.util.ArrayList<>();
        String SQL
            = "SELECT e.employeeId, e.employeeCode, e.userId, e.departmentId, e.positionId, "
            + "       e.phoneNumber, e.skills, e.experience, e.degree, e.status, e.managerId, "
            + "       u.fullName, u.email, u.username, "
            + "       COALESCE(d.departmentName, N'Chưa phân công') AS departmentName, "
            + "       COALESCE(p.positionName, N'') AS positionName, "
            + "       r.roleName "
            + "FROM Employees e "
            + "JOIN Users u ON u.userId = e.userId "
            + "JOIN Roles r ON r.roleId = u.roleId "
            + "LEFT JOIN Departments d ON d.departmentId = e.departmentId "
            + "LEFT JOIN Positions p ON p.positionId = e.positionId "
            + "WHERE u.isActive = 1 "
            + "  AND e.status = 1 "
            + "  AND r.roleName LIKE '%Manager' "
            + "  AND r.roleId > (SELECT u2.roleId FROM Users u2 WHERE u2.userId = ?) "
            + "  AND e.employeeId NOT IN ( "
            + "      SELECT d2.managerId FROM Departments d2 "
            + "      WHERE d2.managerId IS NOT NULL AND d2.status = 1 "
            + "  ) "
            + "  AND EXISTS ( "
            + "      SELECT 1 FROM Departments dept2 "
            + "      WHERE dept2.departmentId = ? "
            + "        AND r.roleName LIKE CONCAT(dept2.departmentCode, '%') "
            + "  ) "
            + "ORDER BY u.fullName";
        try (java.sql.Connection conn = dbContext.getConnection(); java.sql.PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, baUserId);
            ps.setInt(2, departmentId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dto.EmployeeDetailDTO e = new dto.EmployeeDetailDTO();
                    e.setEmployeeId(rs.getInt("employeeId"));
                    e.setEmployeeCode(rs.getString("employeeCode"));
                    e.setUserId(rs.getInt("userId"));
                    e.setDepartmentId(rs.getInt("departmentId"));
                    e.setPositionId(rs.getInt("positionId"));
                    e.setPhoneNumber(rs.getString("phoneNumber"));
                    e.setSkills(rs.getNString("skills"));
                    e.setExperience(rs.getNString("experience"));
                    e.setDegree(rs.getNString("degree"));
                    e.setStatus(rs.getInt("status"));
                    int mgr = rs.getInt("managerId");
                    e.setManagerId(rs.wasNull() ? null : mgr);
                    e.setFullName(rs.getNString("fullName"));
                    e.setEmail(rs.getString("email"));
                    e.setUsername(rs.getString("username"));
                    e.setDepartmentName(rs.getNString("departmentName"));
                    e.setPositionName(rs.getNString("positionName"));
                    e.setRoleName(rs.getString("roleName"));
                    list.add(e);
                }
            }
        } catch (java.sql.SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Cannot get assignable manager DTOs", e);
        }
        return list;
    }

    public boolean assignManager(int departmentId, int employeeId) {
        String sqlDept = "UPDATE Departments SET managerId = ? WHERE departmentId = ? AND status = 1";
        String sqlEmp  = "UPDATE Employees SET departmentId = ? WHERE employeeId = ?";
        java.sql.Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);

            // 1. Set managerId trên Departments
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlDept)) {
                ps.setInt(1, employeeId);
                ps.setInt(2, departmentId);
                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 2. Set departmentId trên Employees cho manager
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlEmp)) {
                ps.setInt(1, departmentId);
                ps.setInt(2, employeeId);
                ps.executeUpdate();
            }

            conn.commit();
            LOGGER.log(java.util.logging.Level.INFO,
                    "Assigned employeeId={0} as manager of departmentId={1}",
                    new Object[]{employeeId, departmentId});
            return true;
        } catch (java.sql.SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE,
                    "Cannot assign manager for deptId=" + departmentId, e);
            if (conn != null) {
                try { conn.rollback(); } catch (java.sql.SQLException ignored) {}
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (java.sql.SQLException ignored) {}
            }
        }
        return false;
    }

    public boolean unassignManager(int departmentId) {
        String SQL = "UPDATE Departments SET managerId = NULL WHERE departmentId = ?";
        try (java.sql.Connection conn = dbContext.getConnection(); java.sql.PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, departmentId);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                LOGGER.log(java.util.logging.Level.INFO,
                        "Unassigned manager from departmentId={0}", departmentId);
            }
            return ok;
        } catch (java.sql.SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE,
                    "Cannot unassign manager for deptId=" + departmentId, e);
        }
        return false;
    }

    public dto.EmployeeDetailDTO getCurrentManager(int departmentId) {
        String SQL
                = "SELECT e.employeeId, e.employeeCode, e.userId, e.departmentId, e.positionId, "
                + "       e.phoneNumber, e.skills, e.experience, e.degree, e.status, e.managerId, "
                + "       u.fullName, u.email, u.username, "
                + "       COALESCE(d.departmentName, N'') AS departmentName, "
                + "       COALESCE(p.positionName, N'') AS positionName, "
                + "       r.roleName "
                + "FROM Departments dept "
                + "JOIN Employees e ON e.employeeId = dept.managerId "
                + "JOIN Users u ON u.userId = e.userId "
                + "JOIN Roles r ON r.roleId = u.roleId "
                + "LEFT JOIN Departments d ON d.departmentId = e.departmentId "
                + "LEFT JOIN Positions p ON p.positionId = e.positionId "
                + "WHERE dept.departmentId = ?";
        try (java.sql.Connection conn = dbContext.getConnection(); java.sql.PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, departmentId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dto.EmployeeDetailDTO e = new dto.EmployeeDetailDTO();
                    e.setEmployeeId(rs.getInt("employeeId"));
                    e.setEmployeeCode(rs.getString("employeeCode"));
                    e.setUserId(rs.getInt("userId"));
                    e.setDepartmentId(rs.getInt("departmentId"));
                    e.setPositionId(rs.getInt("positionId"));
                    e.setPhoneNumber(rs.getString("phoneNumber"));
                    e.setSkills(rs.getNString("skills"));
                    e.setExperience(rs.getNString("experience"));
                    e.setDegree(rs.getNString("degree"));
                    e.setStatus(rs.getInt("status"));
                    int mgr = rs.getInt("managerId");
                    e.setManagerId(rs.wasNull() ? null : mgr);
                    e.setFullName(rs.getNString("fullName"));
                    e.setEmail(rs.getString("email"));
                    e.setUsername(rs.getString("username"));
                    e.setDepartmentName(rs.getNString("departmentName"));
                    e.setPositionName(rs.getNString("positionName"));
                    e.setRoleName(rs.getString("roleName"));
                    return e;
                }
            }
        } catch (java.sql.SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE,
                    "Cannot get current manager for deptId=" + departmentId, e);
        }
        return null;
    }
}
