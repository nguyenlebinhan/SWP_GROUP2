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
import dto.EmployeeDTO;

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

    public List<Employee> getAllemployees() {
        LOGGER.log(Level.INFO, "Get all employees");
        List<Employee> list = new ArrayList<>();
        String SQL = "SELECT * FROM employees WHERE status != 0";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareCall(SQL); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapEmployee(rs));
            }
            LOGGER.log(Level.INFO, "Retrieve {0} employees from DB.", list.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve employees from DB", e);
        }
        return list;
    }

    public Employee getEmployeeById(int employeeId) {
        LOGGER.log(Level.INFO, "Get employee by employeeId: {0}", employeeId);
        String SQL = "SELECT * FROM employees WHERE employeeId = ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEmployee(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve employee by employeeId: " + employeeId, e);
        }
        return null;
    }

    public Employee getEmployeeByUserId(int userId) {
        LOGGER.log(Level.INFO, "Get employee by userId: {0}", userId);
        String SQL = "SELECT * FROM employees WHERE userId = ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEmployee(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve employee by userId: " + userId, e);
        }
        return null;
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
             experience, degree, hireDate, probationEndDate, status, managerId, nationalId, contractType)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?, ?)
            """;
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, emp.getEmployeeCode());
            ps.setInt(2, emp.getUserId());
            ps.setInt(3, emp.getDepartmentId());
            ps.setInt(4, emp.getPositionId());
            ps.setString(5, emp.getPhoneNumber());
            ps.setString(6, emp.getSkills());
            ps.setString(7, emp.getExperience());
            ps.setString(8, emp.getDegree());
            ps.setDate(9, Date.valueOf(emp.getHireDate()));
            if (emp.getProbationEndDate() != null) {
                ps.setDate(10, Date.valueOf(emp.getProbationEndDate()));
            } else {
                ps.setNull(10, Types.DATE);
            }
            if (emp.getManagerId() != null) {
                ps.setInt(11, emp.getManagerId());
            } else {
                ps.setNull(11, Types.INTEGER);
            }
            ps.setString(12, emp.getNationalId());
            ps.setString(13, emp.getContractType());

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
        String SQL = """
            UPDATE employees SET
                departmentId = ?, positionId = ?, phoneNumber = ?, skills = ?,
                experience = ?, degree = ?, hireDate = ?, probationEndDate = ?,
                managerId = ?, nationalId = ?, contractType = ?
            WHERE employeeId = ?
            """;
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, emp.getDepartmentId());
            ps.setInt(2, emp.getPositionId());
            ps.setString(3, emp.getPhoneNumber());
            ps.setString(4, emp.getSkills());
            ps.setString(5, emp.getExperience());
            ps.setString(6, emp.getDegree());
            ps.setDate(7, Date.valueOf(emp.getHireDate()));
            if (emp.getProbationEndDate() != null) {
                ps.setDate(8, Date.valueOf(emp.getProbationEndDate()));
            } else {
                ps.setNull(8, Types.DATE);
            }
            if (emp.getManagerId() != null) {
                ps.setInt(9, emp.getManagerId());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            ps.setString(10, emp.getNationalId());
            ps.setString(11, emp.getContractType());
            ps.setInt(12, emp.getEmployeeId());

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

    private Employee mapEmployee(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setEmployeeId(rs.getInt("employeeId"));
        e.setEmployeeCode(rs.getString("employeeCode"));
        e.setUserId(rs.getInt("userId"));
        e.setDepartmentId(rs.getInt("departmentId"));
        e.setPositionId(rs.getInt("positionId"));
        e.setPhoneNumber(rs.getString("phoneNumber"));
        e.setSkills(rs.getString("skills"));
        e.setExperience(rs.getString("experience"));
        e.setDegree(rs.getString("degree"));
        Date hireDate = rs.getDate("hireDate");
        e.setHireDate(hireDate != null ? hireDate.toLocalDate() : null);
        Date probation = rs.getDate("probationEndDate");
        e.setProbationEndDate(probation != null ? probation.toLocalDate() : null);
        e.setStatus(rs.getInt("status"));
        int mid = rs.getInt("managerId");
        e.setManagerId(rs.wasNull() ? null : mid);
        e.setNationalId(rs.getString("nationalId"));
        e.setContractType(rs.getString("contractType"));
        return e;
    }

    private EmployeeDTO mapEmployeeDTO(ResultSet rs) throws SQLException {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmployeeId(rs.getInt("employeeId"));
        dto.setEmployeeCode(rs.getString("employeeCode"));
        dto.setFullName(rs.getString("fullName"));
        dto.setEmail(rs.getString("email"));
        dto.setPhoneNumber(rs.getString("phoneNumber"));
        dto.setDepartmentName(rs.getString("departmentName"));
        dto.setPositionName(rs.getString("positionName"));
        dto.setManagerName(rs.getString("managerName"));
        dto.setHireDate(rs.getString("hireDate"));
        dto.setStatus(rs.getInt("status"));
        dto.setAvatar(rs.getString("avatar"));
        return dto;
    }
}
