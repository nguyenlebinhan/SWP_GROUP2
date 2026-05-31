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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Employee;
import model.User;
import dto.EmployeeDTO;
import dto.EmployeeDetailDTO;

/**
 *
 * @author admin
 */
public class EmployeeDAO {

    private static final Logger LOGGER = Logger.getLogger(EmployeeDAO.class.getName());
    private final DBContext dbContext;

    public EmployeeDAO() {
        this.dbContext = new DBContext();
    }

    public int countTotal() {
        String SQL = "SELECT COUNT(*) FROM employees";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot count total employees", e);
        }
        return 0;
    }

    public int countActive() {
        String SQL = "SELECT COUNT(*) FROM employees WHERE status = 1";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot count active employees", e);
        }
        return 0;
    }

    public int countInactive() {
        String SQL = "SELECT COUNT(*) FROM employees WHERE status = 0";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot count inactive employees", e);
        }
        return 0;
    }

    public List<EmployeeDetailDTO> getAllEmployees(int userId) {
        List<EmployeeDetailDTO> list = new ArrayList<>();
        String SQL = "SELECT e.employeeId, e.employeeCode, e.userId, e.departmentId, e.positionId, "
                   + "e.phoneNumber, e.skills, e.experience, e.degree, e.status, e.managerId, "
                   + "u.fullName, u.email, u.username, "
                   + "d.departmentName, p.positionName, r.roleName "
                   + "FROM Employees e "
                   + "JOIN Users u ON u.userId = e.userId "
                   + "LEFT JOIN Departments d ON d.departmentId = e.departmentId "
                   + "LEFT JOIN Positions p ON p.positionId = e.positionId "
                   + "JOIN Roles r on r.roleId = u.roleId "
                   + "WHERE e.userId != ? "
                   + "ORDER BY e.employeeId DESC";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)){
                ps.setInt(1, userId);
                try(ResultSet rs = ps.executeQuery()){               
                    while (rs.next()) {
                        list.add(mapEmployeeDTO(rs));
                    }
                }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve all employees", e);
        }
        return list;
    }
    
    public List<EmployeeDetailDTO> getAllEmployeesByDepartmentId(int departmentId) {
        List<EmployeeDetailDTO> list = new ArrayList<>();
        String SQL = "SELECT e.employeeId, e.employeeCode, e.userId, e.departmentId, e.positionId, "
                   + "e.phoneNumber, e.skills, e.experience, e.degree, e.status, e.managerId, "
                   + "u.fullName, u.email, u.username, "
                   + "d.departmentName, p.positionName, r.roleName "
                   + "FROM Employees e "
                   + "JOIN Users u ON u.userId = e.userId "
                   + "LEFT JOIN Departments d ON d.departmentId = e.departmentId "
                   + "LEFT JOIN Positions p ON p.positionId = e.positionId "
                   + "JOIN Roles r on r.roleId = u.roleId "
                   + "WHERE d.departmentId != ? "
                   + "ORDER BY e.employeeId DESC";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)){
                ps.setInt(1, departmentId);
                try(ResultSet rs = ps.executeQuery()){               
                    while (rs.next()) {
                        list.add(mapEmployeeDTO(rs));
                    }
                }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve all employees in department by departmentId: "+ departmentId, e);
        }
        return list;
    }    

    public EmployeeDetailDTO getEmployeeById(int employeeId) {
        String SQL = "SELECT e.employeeId, e.employeeCode, e.userId, e.departmentId, e.positionId, "
                   + "e.phoneNumber, e.skills, e.experience, e.degree, e.status, e.managerId, "
                   + "u.fullName, u.email, u.username, "
                   + "d.departmentName, p.positionName, r.roleName "
                   + "FROM Employees e "
                   + "JOIN Users u ON u.userId = e.userId "
                   + "JOIN Departments d ON d.departmentId = e.departmentId "
                   + "JOIN Positions p ON p.positionId = e.positionId "
                   + "JOIN Roles r ON r.roleId = u.roleId "
                   + "WHERE e.employeeId = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapEmployeeDTO(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve employee by id: " + employeeId, e);
        }
        return null;
    }

    public EmployeeDetailDTO getEmployeeByUserId(int userId) {
        String SQL = "SELECT e.employeeId, e.employeeCode, e.userId, e.departmentId, e.positionId, "
                   + "e.phoneNumber, e.skills, e.experience, e.degree, e.status, e.managerId, "
                   + "u.fullName, u.email, u.username, "
                   + "d.departmentName, p.positionName, r.roleName "
                   + "FROM Employees e "
                   + "JOIN Users u ON u.userId = e.userId "
                   + "JOIN Departments d ON d.departmentId = e.departmentId "
                   + "JOIN Positions p ON p.positionId = e.positionId "
                   + "JOIN Roles r ON r.roleId = u.roleId "
                   + "WHERE e.userId = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapEmployeeDTO(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve employee by userId: " + userId, e);
        }
        return null;
    }

    public List<EmployeeDetailDTO> getEmployeesByDepartmentId(int departmentId) {
        List<EmployeeDetailDTO> list = new ArrayList<>();
        String SQL = "SELECT e.employeeId, e.employeeCode, e.userId, e.departmentId, e.positionId, "
                   + "e.phoneNumber, e.skills, e.experience, e.degree, e.status, e.managerId, "
                   + "u.fullName, u.email, u.username, "
                   + "d.departmentName, p.positionName, r.roleName "
                   + "FROM Employees e "
                   + "JOIN Users u ON u.userId = e.userId "
                   + "JOIN Departments d ON d.departmentId = e.departmentId "
                   + "JOIN Positions p ON p.positionId = e.positionId "
                   + "JOIN Roles r ON r.roleId = u.roleId "
                   + "WHERE e.departmentId = ? "
                   + "ORDER BY e.employeeId DESC";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEmployeeDTO(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve employees for departmentId: " + departmentId, e);
        }
        return list;
    }

    public Map<String, Integer> countByDepartment() {
        LOGGER.log(Level.INFO, "Count employees by department");
        Map<String, Integer> map = new LinkedHashMap<>();
        String SQL = """
            SELECT d.departmentName, COUNT(e.employeeId) AS total
            FROM departments d
            LEFT JOIN employees e ON d.departmentId = e.departmentId AND e.status != 0
            GROUP BY d.departmentId, d.departmentName
            """;
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("departmentName"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot count employees by department", e);
        }
        return map;
    }

    public boolean addEmployee(Employee emp) {
        LOGGER.log(Level.INFO, "Adding new employee with code: {0}", emp.getEmployeeCode());
        String SQL = """
            INSERT INTO employees
            (employeeCode, userId, departmentId, positionId, phoneNumber, skills,
             experience, degree, status, managerId)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, ? )
            """;
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, generateNextEmployeeCode(conn));
            ps.setInt(2, emp.getUserId());
            ps.setInt(3, emp.getDepartmentId());
            ps.setInt(4, emp.getPositionId());
            ps.setString(5, emp.getPhoneNumber());
            ps.setString(6, emp.getSkills());
            ps.setString(7, emp.getExperience());
            ps.setString(8, emp.getDegree());
            ps.setInt(9, emp.getManagerId());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Employee added successfully with code: {0}", emp.getEmployeeCode());
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Add employee failed: no rows affected for code: {0}", emp.getEmployeeCode());
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding employee with code: " + emp.getEmployeeCode(), e);
        }
        return false;
    }

    public boolean updateEmployee(Employee emp) {
        LOGGER.log(Level.INFO, "Updating employee with employeeId: {0}", emp.getEmployeeId());
        String SQL = "UPDATE employees SET departmentId = ?, positionId = ?, phoneNumber = ?, skills = ?, experience = ?, degree = ?, managerId = ? WHERE employeeId = ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, emp.getDepartmentId());
            ps.setInt(2, emp.getPositionId());
            ps.setString(3, emp.getPhoneNumber());
            ps.setString(4, emp.getSkills());
            ps.setString(5, emp.getExperience());
            ps.setString(6, emp.getDegree());
            ps.setInt(7, emp.getEmployeeId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Employee updated successfully with employeeId: {0}", emp.getEmployeeId());
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Update employee failed: no rows affected for employeeId: {0}", emp.getEmployeeId());
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating employee with employeeId: " + emp.getEmployeeId(), e);
        }
        return false;
    }

    public boolean activateEmployee(int employeeId) {
        LOGGER.log(Level.INFO, "Activating employee with employeeId :{0}", employeeId);
        String SQL = "UPDATE employees SET status = 1 WHERE employeeId = ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Employee activated successfully with employeeId: {0}", employeeId);
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Activate employee failed: no rows affected for employeeId: {0}", employeeId);
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deactivating employee with employeeId: " + employeeId, e);
            return false;
        }
    }
    public List<User> getEmployees(int userId) {
        List<User> list = new ArrayList<>();
        String SQL = "SELECT u.userId, u.username, u.email, u.password, u.fullName, u.dob, "
                   + "u.gender, u.address, r.roleName, u.isTemporaryPassword, u.isActive "
                   + "FROM Users u "
                   + "JOIN Roles r ON r.roleId = u.roleId "
                   + "JOIN Employees e on e.userId = u.userId "
                   + "WHERE e.departmentId IS NULL "
                   + "AND r.roleId != 1 AND u.userId != ? "
                   + "ORDER BY u.fullName ";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)){
                ps.setInt(1, userId);
                try(ResultSet rs = ps.executeQuery()){   
                    while (rs.next()) {
                        list.add(new User(
                                rs.getInt("userId"),
                                rs.getString("username"),
                                rs.getString("email"),
                                rs.getString("password"),
                                rs.getNString("fullName"),
                                rs.getString("dob"),
                                rs.getNString("gender"),
                                rs.getString("address"),
                                rs.getString("roleName"),
                                rs.getBoolean("isTemporaryPassword"),
                                rs.getInt("isActive")
                        ));
                    }
                }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve users not yet employees", e);
        }
        return list;
    }

    public boolean assignEmployeeToDepartment(int userId, int departmentId, int positionId,
                                              String phoneNumber, String skills,
                                              String experience, String degree) {
        LOGGER.log(Level.INFO, "Assigning userId={0} to departmentId={1}", new Object[]{userId, departmentId});
        // Hồ sơ nhân viên đã được tạo sẵn (dept = NULL) khi tạo user → assign là UPDATE.
        // Điều kiện departmentId IS NULL đảm bảo không ghi đè người đã được phân công.
        String SQL = "UPDATE Employees SET departmentId = ?, positionId = ?, phoneNumber = ?, "
                   + "skills = ?, experience = ?, degree = ?, status = 1 "
                   + "WHERE userId = ? AND departmentId IS NULL";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, departmentId);
            ps.setInt(2, positionId);
            ps.setString(3, phoneNumber);
            ps.setString(4, skills);
            ps.setString(5, experience);
            ps.setString(6, degree);
            ps.setInt(7, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot assign employee to department for userId: " + userId, e);
        }
        return false;
    }

    public boolean isUserAlreadyEmployee(int userId) {
        String SQL = "SELECT 1 FROM Employees WHERE userId = ? LIMIT 1";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot check if user is employee", e);
        }
        return false;
    }

    public boolean isUserAssignedToDepartment(int userId) {
        String SQL = "SELECT 1 FROM Employees WHERE userId = ? AND departmentId IS NOT NULL LIMIT 1";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot check if user is assigned to a department", e);
        }
        return false;
    }

    public boolean deactiveEmployee(int employeeId) {
        LOGGER.log(Level.INFO, "Deactivating employee with employeeId: {0}", employeeId);
        String SQL = "UPDATE employees SET status = 0 WHERE employeeId = ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Employee deactivated successfully with employeeId: {0}", employeeId);
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Deactivate employee failed: no rows affected for employeeId: {0}", employeeId);
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deactivating employee with employeeId: " + employeeId, e);
        }
        return false;
    }

    public int countByDepartmentId(int departmentId) {
        String SQL = "SELECT COUNT(*) FROM Employees WHERE departmentId = ? AND status != 0";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot count employees for dept", e);
        }
        return 0;
    }

    
    
    private String generateNextEmployeeCode(Connection conn) throws SQLException {
        String SQL = "SELECT COALESCE(MAX(employeeId), 0) + 1 AS nextId FROM Employees";
        try (PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return String.format("EMP%04d", rs.getInt("nextId"));
        }
        return "EMP0001";
    }

    private EmployeeDetailDTO mapEmployeeDTO(ResultSet rs) throws SQLException {
        EmployeeDetailDTO e = new EmployeeDetailDTO();
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

    public boolean updateOwnProfile(int employeeId, String phoneNumber, String skills, String experience, String degree) {
        LOGGER.log(Level.INFO, "Updating own profile for employeeId: {0}", employeeId);
        String SQL = "UPDATE employees SET phoneNumber = ?, skills = ?, experience = ?, degree = ? WHERE employeeId = ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, phoneNumber);
            ps.setString(2, skills);
            ps.setString(3, experience);
            ps.setString(4, degree);
            ps.setInt(5, employeeId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating own profile with employeeId: " + employeeId, e);
        }
        return false;
    }
}
