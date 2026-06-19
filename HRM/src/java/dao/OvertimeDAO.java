package dao;

import dal.DBContext;
import dto.OvertimeRequestDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OvertimeDAO {
    private static final Logger LOGGER = Logger.getLogger(OvertimeDAO.class.getName());
    private final DBContext dbContext;

    public OvertimeDAO() {
        this.dbContext = new DBContext();
    }

    public List<OvertimeRequestDTO> getOvertimeRequestsByManager(int managerId, String statusFilter, String dateFilter) {
        List<OvertimeRequestDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT f.formId, f.formCode, f.employeeId, f.formTypeId, f.reason, f.status, f.approverId, f.approverNote, f.approvedAt, f.createdAt, f.updatedAt, " +
                "od.otDate, od.startTime, od.endTime, od.dayType, " +
                "(SELECT COUNT(*) FROM Overtime_Assignees oa WHERE oa.formId = f.formId) AS totalAssignees, " +
                "u.fullName AS approverName " +
                "FROM Form_Requests f " +
                "JOIN Overtime_Details od ON f.formId = od.formId " +
                "LEFT JOIN Employees e ON e.employeeId = f.approverId " +
                "LEFT JOIN Users u ON u.userId = e.userId " +
                "WHERE f.employeeId = ? AND f.formTypeId = (SELECT formTypeId FROM Form_Types WHERE formTypeCode = 'OVERTIME' LIMIT 1) "
        );

        List<Object> params = new ArrayList<>();
        params.add(managerId);

        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            sql.append("AND f.status = ? ");
            params.add(Integer.parseInt(statusFilter));
        }

        if (dateFilter != null && !dateFilter.trim().isEmpty()) {
            sql.append("AND od.otDate = ? ");
            params.add(java.sql.Date.valueOf(dateFilter));
        }

        sql.append("ORDER BY f.createdAt DESC");

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OvertimeRequestDTO dto = new OvertimeRequestDTO();
                    dto.setFormId(rs.getInt("formId"));
                    dto.setFormCode(rs.getString("formCode"));
                    dto.setEmployeeId(rs.getInt("employeeId"));
                    dto.setFormTypeId(rs.getInt("formTypeId"));
                    dto.setReason(rs.getString("reason"));
                    dto.setStatus(rs.getInt("status"));
                    
                    int approverId = rs.getInt("approverId");
                    dto.setApproverId(rs.wasNull() ? null : approverId);
                    
                    dto.setApproverNote(rs.getString("approverNote"));
                    dto.setApprovedAt(rs.getTimestamp("approvedAt"));
                    dto.setCreatedAt(rs.getTimestamp("createdAt"));
                    dto.setUpdatedAt(rs.getTimestamp("updatedAt"));
                    
                    dto.setOtDate(rs.getDate("otDate"));
                    dto.setStartTime(rs.getTime("startTime"));
                    dto.setEndTime(rs.getTime("endTime"));
                    dto.setDayType(rs.getInt("dayType"));
                    dto.setTotalAssignees(rs.getInt("totalAssignees"));
                    dto.setApproverName(rs.getString("approverName"));
                    
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get overtime requests by manager", e);
        }
        return list;
    }

    public boolean addOvertimeDetails(int formId, String otDate, String startTime, String endTime, int dayType) {
        String SQL = "INSERT INTO Overtime_Details (formId, otDate, startTime, endTime, dayType) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, formId);
            ps.setDate(2, java.sql.Date.valueOf(otDate));
            
            if (startTime != null && startTime.length() == 5) startTime += ":00";
            if (endTime != null && endTime.length() == 5) endTime += ":00";
            
            ps.setTime(3, java.sql.Time.valueOf(startTime));
            ps.setTime(4, java.sql.Time.valueOf(endTime));
            ps.setInt(5, dayType);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot add overtime details", e);
            return false;
        }
    }

    public boolean addOvertimeAssignees(int formId, String[] assigneeIds) {
        if (assigneeIds == null || assigneeIds.length == 0) return false;
        
        String SQL = "INSERT INTO Overtime_Assignees (formId, employeeId) VALUES (?, ?)";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            for (String empIdStr : assigneeIds) {
                ps.setInt(1, formId);
                ps.setInt(2, Integer.parseInt(empIdStr));
                ps.addBatch();
            }
            int[] results = ps.executeBatch();
            return results.length > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot add overtime assignees", e);
            return false;
        }
    }
    public OvertimeRequestDTO getOvertimeRequestById(int formId) {
        String sql = "SELECT f.formId, f.formCode, f.employeeId, f.formTypeId, f.reason, f.status, f.approverId, f.approverNote, f.approvedAt, f.createdAt, f.updatedAt, " +
                "od.otDate, od.startTime, od.endTime, od.dayType, " +
                "(SELECT COUNT(*) FROM Overtime_Assignees oa WHERE oa.formId = f.formId) AS totalAssignees, " +
                "u.fullName AS approverName " +
                "FROM Form_Requests f " +
                "JOIN Overtime_Details od ON f.formId = od.formId " +
                "LEFT JOIN Employees e ON e.employeeId = f.approverId " +
                "LEFT JOIN Users u ON u.userId = e.userId " +
                "WHERE f.formId = ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, formId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OvertimeRequestDTO dto = new OvertimeRequestDTO();
                    dto.setFormId(rs.getInt("formId"));
                    dto.setFormCode(rs.getString("formCode"));
                    dto.setEmployeeId(rs.getInt("employeeId"));
                    dto.setFormTypeId(rs.getInt("formTypeId"));
                    dto.setReason(rs.getString("reason"));
                    dto.setStatus(rs.getInt("status"));
                    
                    int approverId = rs.getInt("approverId");
                    dto.setApproverId(rs.wasNull() ? null : approverId);
                    
                    dto.setApproverNote(rs.getString("approverNote"));
                    dto.setApprovedAt(rs.getTimestamp("approvedAt"));
                    dto.setCreatedAt(rs.getTimestamp("createdAt"));
                    dto.setUpdatedAt(rs.getTimestamp("updatedAt"));
                    
                    dto.setOtDate(rs.getDate("otDate"));
                    dto.setStartTime(rs.getTime("startTime"));
                    dto.setEndTime(rs.getTime("endTime"));
                    dto.setDayType(rs.getInt("dayType"));
                    dto.setTotalAssignees(rs.getInt("totalAssignees"));
                    dto.setApproverName(rs.getString("approverName"));
                    return dto;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get overtime request by id", e);
        }
        return null;
    }

    public List<dto.EmployeeDetailDTO> getOvertimeAssignees(int formId) {
        List<dto.EmployeeDetailDTO> list = new ArrayList<>();
        String sql = "SELECT e.*, u.fullName, u.email, u.username, d.departmentName, p.positionName, r.roleName " +
                "FROM Overtime_Assignees oa " +
                "JOIN Employees e ON oa.employeeId = e.employeeId " +
                "JOIN Users u ON e.userId = u.userId " +
                "LEFT JOIN Departments d ON e.departmentId = d.departmentId " +
                "LEFT JOIN Positions p ON e.positionId = p.positionId " +
                "LEFT JOIN Roles r ON u.roleId = r.roleId " +
                "WHERE oa.formId = ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, formId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dto.EmployeeDetailDTO dto = new dto.EmployeeDetailDTO();
                    dto.setEmployeeId(rs.getInt("employeeId"));
                    dto.setEmployeeCode(rs.getString("employeeCode"));
                    dto.setUserId(rs.getInt("userId"));
                    dto.setDepartmentId(rs.getInt("departmentId"));
                    dto.setPositionId(rs.getInt("positionId"));
                    dto.setPhoneNumber(rs.getString("phoneNumber"));
                    dto.setSkills(rs.getString("skills"));
                    dto.setExperience(rs.getString("experience"));
                    dto.setDegree(rs.getString("degree"));
                    dto.setStatus(rs.getInt("status"));
                    
                    int managerId = rs.getInt("managerId");
                    dto.setManagerId(rs.wasNull() ? null : managerId);
                    
                    dto.setFullName(rs.getString("fullName"));
                    dto.setEmail(rs.getString("email"));
                    dto.setUsername(rs.getString("username"));
                    dto.setDepartmentName(rs.getString("departmentName"));
                    dto.setPositionName(rs.getString("positionName"));
                    dto.setRoleName(rs.getString("roleName"));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get overtime assignees", e);
        }
        return list;
    }
}
