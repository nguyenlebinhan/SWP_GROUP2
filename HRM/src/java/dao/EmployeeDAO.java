package dao;

import dal.DBContext;
import dto.EmployeeDetailDTO;
import model.Employee;
import model.User;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

public class EmployeeDAO {
    private static final Logger LOGGER = Logger.getLogger(EmployeeDAO.class.getName());
    private final DBContext dbContext;

    public EmployeeDAO() {
        this.dbContext = new DBContext();
    }

    public List<EmployeeDetailDTO> getAllEmployees() {
        List<EmployeeDetailDTO> list = new ArrayList<>();
        String SQL = "SELECT e.employeeId, e.employeeCode, e.userId, e.departmentId, e.positionId, "
                   + "e.phoneNumber, e.skills, e.experience, e.degree, e.status, e.managerId, "
                   + "u.fullName, u.email, u.username, "
                   + "d.departmentName, p.positionName "
                   + "FROM Employees e "
                   + "JOIN Users u ON u.userId = e.userId "
                   + "JOIN Departments d ON d.departmentId = e.departmentId "
                   + "JOIN Positions p ON p.positionId = e.positionId "
                   + "ORDER BY e.employeeId DESC";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapEmployeeDTO(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve all employees", e);
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

    public List<User> getUsersNotYetEmployees() {
        List<User> list = new ArrayList<>();
        String SQL = "SELECT u.userId, u.username, u.fullName, u.email, r.roleName "
                   + "FROM Users u "
                   + "JOIN Roles r ON r.roleId = u.roleId "
                   + "WHERE u.isActive = 1 "
                   + "AND u.userId NOT IN (SELECT userId FROM Employees) "
                   + "ORDER BY u.fullName";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("userId"));
                u.setUsername(rs.getString("username"));
                u.setFullName(rs.getNString("fullName"));
                u.setEmail(rs.getString("email"));
                u.setRoleName(rs.getString("roleName"));
                list.add(u);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve users without employee records", e);
        }
        return list;
    }

    public boolean assignEmployeeToDepartment(int userId, int departmentId, int positionId,
                                               String phoneNumber, String skills,
                                               String experience, String degree) {
        String SQL = "INSERT INTO Employees (employeeCode, userId, departmentId, positionId, "
                   + "phoneNumber, skills, experience, degree, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)";
        Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);
            String code = generateNextEmployeeCode(conn);
            try (PreparedStatement ps = conn.prepareStatement(SQL)) {
                ps.setString(1, code);
                ps.setInt(2, userId);
                ps.setInt(3, departmentId);
                ps.setInt(4, positionId);
                ps.setString(5, phoneNumber);
                ps.setString(6, skills);
                ps.setString(7, experience);
                ps.setString(8, degree);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    conn.commit();
                    LOGGER.log(Level.INFO, "Employee assigned: userId={0}, dept={1}, code={2}",
                               new Object[]{userId, departmentId, code});
                    return true;
                }
            }
            conn.rollback();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot assign employee to department", e);
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
        return false;
    }

    public boolean transferDepartment(int employeeId, int newDepartmentId) {
        String SQL = "UPDATE Employees SET departmentId = ? WHERE employeeId = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, newDepartmentId);
            ps.setInt(2, employeeId);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) LOGGER.log(Level.INFO, "Employee {0} transferred to dept {1}", new Object[]{employeeId, newDepartmentId});
            return ok;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot transfer employee department", e);
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
}
