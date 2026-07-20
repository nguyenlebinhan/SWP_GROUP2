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
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.FormRequest;
import model.LeaveFormRequest;
import model.ComplaintFormRequest;
import model.TransferFormRequest;
import dto.FormRequestDTO;
import dto.LeaveFormRequestDTO;
import dto.ComplaintFormRequestDTO;
import dto.TransferRequestDTO;
import java.util.Objects;

/**
 *
 * @author admin
 */
public class FormRequestDAO {

    private static final Logger LOGGER = Logger.getLogger(FormRequestDAO.class.getName());
    private final DBContext dbContext;

    public FormRequestDAO() {
        this.dbContext = new DBContext();
    }

    /**
     * Dùng trong transaction import: nhân viên có đơn NGHỈ PHÉP đã DUYỆT phủ
     * workDate hay không. status = 1 nghĩa là Approved (xem FormRequest).
     */
    public boolean hasApprovedLeave(Connection conn, int employeeId, java.sql.Date workDate)
            throws SQLException {
        String sql = "SELECT 1 FROM Form_Requests fr "
                + "JOIN Form_Types ft ON fr.formTypeId = ft.formTypeId "
                + "WHERE ft.formTypeCode = 'LEAVE' "
                + "  AND fr.status = 1 "
                + "  AND fr.employeeId = ? "
                + "  AND ? BETWEEN fr.startDate AND fr.endDate LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, workDate);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean hasApprovedLeave(int employeeId, java.sql.Date workDate) {
        try (Connection conn = dbContext.getConnection()) {
            return hasApprovedLeave(conn, employeeId, workDate);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking approved leave without connection param", e);
        }
        return false;
    }

    // INSERT đơn chung (Loại không có ngày)
    public int addFormRequest(FormRequest fr) {
        String SQL = """
                     INSERT INTO form_requests
                     (formCode, employeeId, formTypeId, reason, usedDays,
                      attachmentUrl, attachmentName, status)
                     VALUES (?, ?, ?, ?, 0, ?, ?, 0)
                     """;
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fr.getFormCode());
            ps.setInt(2, fr.getEmployeeId());
            ps.setInt(3, fr.getFormTypeId());
            ps.setNString(4, fr.getReason());
            if (fr.getAttachmentUrl() == null) {
                ps.setNull(5, Types.VARCHAR);
                ps.setNull(6, Types.VARCHAR);
            } else {
                ps.setString(5, fr.getAttachmentUrl());
                ps.setString(6, fr.getAttachmentName());
            }
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int newId = rs.getInt(1);
                        LOGGER.log(Level.INFO, "Form request added: id={0}, code={1}", new Object[]{newId, fr.getFormCode()});
                        return newId;
                    }
                }
            }
            LOGGER.log(Level.WARNING, "Add form request failed for code: {0}", fr.getFormCode());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding form request: " + fr.getFormCode(), e);
        }
        return -1;
    }

    // INSERT đơn Nghỉ phép (overload — đọc startDate, endDate, totalDays từ LeaveFormRequest)
    public int addFormRequest(LeaveFormRequest fr) {
        String SQL = """
                     INSERT INTO form_requests
                     (formCode, employeeId, formTypeId, reason,
                      startDate, endDate, totalDays, usedDays,
                      attachmentUrl, attachmentName, status)
                     VALUES (?, ?, ?, ?, ?, ?, ?, 0, ?, ?, 0)
                     """;
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fr.getFormCode());
            ps.setInt(2, fr.getEmployeeId());
            ps.setInt(3, fr.getFormTypeId());
            ps.setNString(4, fr.getReason());
            if (fr.getStartDate() == null) ps.setNull(5, Types.DATE); else ps.setDate(5, fr.getStartDate());
            if (fr.getEndDate() == null) ps.setNull(6, Types.DATE); else ps.setDate(6, fr.getEndDate());
            if (fr.getTotalDays() == null) ps.setNull(7, Types.INTEGER); else ps.setInt(7, fr.getTotalDays());
            if (fr.getAttachmentUrl() == null) {
                ps.setNull(8, Types.VARCHAR);
                ps.setNull(9, Types.VARCHAR);
            } else {
                ps.setString(8, fr.getAttachmentUrl());
                ps.setString(9, fr.getAttachmentName());
            }
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int newId = rs.getInt(1);
                        LOGGER.log(Level.INFO, "Leave form request added: id={0}, code={1}", new Object[]{newId, fr.getFormCode()});
                        return newId;
                    }
                }
            }
            LOGGER.log(Level.WARNING, "Add leave form request failed for code: {0}", fr.getFormCode());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding leave form request: " + fr.getFormCode(), e);
        }
        return -1;
    }

    // INSERT đơn Khiếu nại (overload)
    public int addFormRequest(ComplaintFormRequest fr) {
        String SQL = """
                     INSERT INTO form_requests
                     (formCode, employeeId, formTypeId, reason,
                       startDate, startTime, endTime, usedDays,
                       attachmentUrl, attachmentName, status)
                     VALUES (?, ?, ?, ?, ?, ?, ?, 0, ?, ?, 0)
                     """;
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fr.getFormCode());
            ps.setInt(2, fr.getEmployeeId());
            ps.setInt(3, fr.getFormTypeId());
            ps.setNString(4, fr.getReason());
            if (fr.getStartDate() == null) ps.setNull(5, Types.DATE); else ps.setDate(5, fr.getStartDate());
            if (fr.getStartTime() == null) ps.setNull(6, Types.TIME); else ps.setTime(6, fr.getStartTime());
            if (fr.getEndTime() == null) ps.setNull(7, Types.TIME); else ps.setTime(7, fr.getEndTime());
            if (fr.getAttachmentUrl() == null) {
                ps.setNull(8, Types.VARCHAR);
                ps.setNull(9, Types.VARCHAR);
            } else {
                ps.setString(8, fr.getAttachmentUrl());
                ps.setString(9, fr.getAttachmentName());
            }
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int newId = rs.getInt(1);
                        LOGGER.log(Level.INFO, "Complaint form request added: id={0}, code={1}", new Object[]{newId, fr.getFormCode()});
                        return newId;
                    }
                }
            }
            LOGGER.log(Level.WARNING, "Add complaint form request failed for code: {0}", fr.getFormCode());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding complaint form request: " + fr.getFormCode(), e);
        }
        return -1;
    }

    // INSERT đơn Thuyên chuyển / Thăng giáng chức (overload)
    public int addFormRequest(TransferFormRequest fr) {
        String SQL = """
                     INSERT INTO form_requests
                     (formCode, employeeId, formTypeId, reason, usedDays,
                      attachmentUrl, attachmentName,
                      targetDepartmentId, targetRoleId,
                      status)
                     VALUES (?, ?, ?, ?, 0, ?, ?, ?, ?, 0)
                     """;
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fr.getFormCode());
            ps.setInt(2, fr.getEmployeeId());
            ps.setInt(3, fr.getFormTypeId());
            ps.setNString(4, fr.getReason());
            if (fr.getAttachmentUrl() == null) {
                ps.setNull(5, Types.VARCHAR);
                ps.setNull(6, Types.VARCHAR);
            } else {
                ps.setString(5, fr.getAttachmentUrl());
                ps.setString(6, fr.getAttachmentName());
            }
            if (fr.getTargetDepartmentId() == null) ps.setNull(7, Types.INTEGER); else ps.setInt(7, fr.getTargetDepartmentId());
            if (fr.getTargetRoleId() == null) ps.setNull(8, Types.INTEGER); else ps.setInt(8, fr.getTargetRoleId());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int newId = rs.getInt(1);
                        LOGGER.log(Level.INFO, "Transfer/Promotion form request added: id={0}, code={1}", new Object[]{newId, fr.getFormCode()});
                        return newId;
                    }
                }
            }
            LOGGER.log(Level.WARNING, "Add transfer/promotion form request failed for code: {0}", fr.getFormCode());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding transfer/promotion form request: " + fr.getFormCode(), e);
        }
        return -1;
    }

    // Kiểm tra trùng ngày nghỉ
    public boolean hasOverlappingLeave(int employeeId, java.sql.Date startDate, java.sql.Date endDate) {
        String SQL = """
                     SELECT COUNT(*) FROM Form_Requests fr
                     JOIN Form_Types ft ON fr.formTypeId = ft.formTypeId
                     WHERE fr.employeeId = ? 
                       AND ft.formTypeCode = 'LEAVE'
                       AND fr.status IN (0, 1)
                       AND (fr.startDate <= ? AND fr.endDate >= ?)
                     """;
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, endDate);
            ps.setDate(3, startDate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking overlapping leave for employee: " + employeeId, e);
        }
        return false;
    }

    //Lấy tất cả đơn theo mã nhân viên (có filter theo thời gian)
    public List<FormRequestDTO> getAllFormRequestsByEmployeeId(int employeeId, Integer day, Integer month, Integer year) {
        List<FormRequestDTO> list = new ArrayList<>();
        StringBuilder SQL = new StringBuilder(MANAGER_FORM_QUERY).append(" WHERE e.employeeId = ?");
        List<Object> param = new ArrayList<>();

        param.add(employeeId);

        if (day != null) {
            SQL.append(" AND DAY(fr.createdAt) = ?");
            param.add(day);
        }
        if (month != null) {
            SQL.append(" AND MONTH(fr.createdAt) = ?");
            param.add(month);
        }
        if (year != null) {
            SQL.append(" AND YEAR(fr.createdAt) = ?");
            param.add(year);
        }
        SQL.append(" ORDER BY fr.createdAt DESC");

        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL.toString())) {
            for (int i = 0; i < param.size(); i++) {
                ps.setObject(i + 1, param.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapFormRequestDTO(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve form requests with employeeId: " + employeeId, e);
        }
        return list;
    }

    //Lấy tất cả đơn theo phòng ban (có filter theo thời gian và tên nhân viên)
    public List<FormRequestDTO> getAllFormRequestsByDepartmentId(int departmentId, Integer day, Integer month, Integer year, String name) {
        List<FormRequestDTO> list = new ArrayList<>();
        StringBuilder SQL = new StringBuilder(MANAGER_FORM_QUERY).append(" WHERE d.departmentId = ?");
        List<Object> param = new ArrayList<>();
        param.add(departmentId);

        if (day != null) {
            SQL.append(" AND DAY(fr.createdAt) = ?");
            param.add(day);
        }
        if (month != null) {
            SQL.append(" AND MONTH(fr.createdAt) = ?");
            param.add(month);
        }
        if (year != null) {
            SQL.append(" AND YEAR(fr.createdAt) = ?");
            param.add(year);
        }
        
        if (name != null && !name.trim().isEmpty()) {
            SQL.append(" AND u.fullName LIKE ?");
            param.add("%" + name.trim() + "%");
        }
        SQL.append(" ORDER BY fr.createdAt DESC");

        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL.toString())) {
            for (int i = 0; i < param.size(); i++) {
                ps.setObject(i + 1, param.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapFormRequestDTO(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve form request with departmentId= " + departmentId, e);
        }
        return list;
    }

    //Lấy tất cả đơn
    public List<FormRequestDTO> getAllFormRequests(Integer day, Integer month, Integer year, String keyword) {
        List<FormRequestDTO> list = new ArrayList<>();
        StringBuilder SQL = new StringBuilder(MANAGER_FORM_QUERY).append(" WHERE 1=1");
        List<Object> param = new ArrayList<>();
        if (day != null) {
            SQL.append(" AND DAY(fr.createdAt) = ?");
            param.add(day);
        }
        if (month != null) {
            SQL.append(" AND MONTH(fr.createdAt) = ?");
            param.add(month);
        }
        if (year != null) {
            SQL.append(" AND YEAR(fr.createdAt) = ?");
            param.add(year);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            SQL.append(" AND (u.fullName LIKE ? OR d.departmentName LIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            param.add(kw);
            param.add(kw);
        }
        SQL.append(" ORDER BY fr.createdAt DESC");
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL.toString())) {
            for (int i = 0; i < param.size(); i++) {
                ps.setObject(i + 1, param.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapFormRequestDTO(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve all form requests", e);
        }
        return list;
    }

    //Lấy đơn theo mã
    public FormRequestDTO getFormRequestById(int id) {
        String SQL = MANAGER_FORM_QUERY + " WHERE fr.formId = ?";

        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapFormRequestDTO(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve form request with id= " + id, e);
        }
        return null;
    }

    //Duyệt & Hủy đơn
    public boolean approveFormRequest(int formId, int approverId, String note){
        return updateFormRequest(formId, 1, approverId, note);
    }
    
    public boolean rejectFormRequest(int formId, int approverId, String note) {
        return updateFormRequest(formId, 2, approverId, note);
    }
    
    public boolean updateFormRequest(int formId, int status, int approverId, String note) {
        String SQL = """
                     UPDATE form_requests
                     SET status = ?, approverId = ?, approverNote = ?, approvedAt = NOW() 
                     WHERE formId = ? AND status = 0
                     """;

        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, status);
            if (approverId > 0) {
                ps.setInt(2, approverId);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, note);
            ps.setInt(4, formId);
            
            int rowsAffected = ps.executeUpdate();
            if(rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Form request updated successfully with id= {0}", formId);
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Updating form request failed with id= {0}", formId);
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating form request with id= " + formId, e);
        }
        return false;
    }

    /**
     * Duyệt đơn với kiểm tra trạng thái hiện tại (dùng cho bước HR duyệt sang status 4).
     * Khác với approveFormRequest() chỉ chấp nhận status = 0.
     */
    public boolean approveFormRequestFromStatus(int formId, int fromStatus, int toStatus, int approverId, String note) {
        String SQL = """
                     UPDATE form_requests
                     SET status = ?, approverId = ?, approverNote = ?, approvedAt = NOW()
                     WHERE formId = ? AND status = ?
                     """;
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, toStatus);
            if (approverId > 0) {
                ps.setInt(2, approverId);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, note);
            ps.setInt(4, formId);
            ps.setInt(5, fromStatus);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                LOGGER.log(Level.INFO, "Form request {0} approved from status {1} to {2}",
                        new Object[]{formId, fromStatus, toStatus});
                return true;
            }
            LOGGER.log(Level.WARNING, "approveFormRequestFromStatus failed: formId={0}, expectedFromStatus={1}",
                    new Object[]{formId, fromStatus});
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error in approveFormRequestFromStatus for formId= " + formId, e);
        }
        return false;
    }

    private static final String MANAGER_FORM_QUERY
            = "SELECT fr.formId, fr.formCode, fr.employeeId, fr.formTypeId, fr.reason, "
            + "fr.startDate, fr.endDate, fr.startTime, fr.endTime, fr.totalDays, fr.usedDays, "
            + "fr.status, fr.approverId, fr.approverNote, fr.approvedAt, "
            + "fr.attachmentUrl, fr.attachmentName, fr.createdAt, fr.updatedAt, "
            + "fr.targetDepartmentId, fr.targetRoleId, "
            + "e.employeeCode, u.fullName, "
            + "d.departmentId, d.departmentName, "
            + "ft.formTypeName, ft.formTypeCode, "
            + "ua.fullName AS approverName, "
            + "td.departmentName AS targetDepartmentName, "
            + "tr.roleName AS targetRoleName "
            + "FROM Form_Requests fr "
            + "JOIN Employees e   ON fr.employeeId  = e.employeeId "
            + "JOIN Users u       ON e.userId        = u.userId "
            + "LEFT JOIN Departments d  ON e.departmentId = d.departmentId "
            + "JOIN Form_Types ft ON fr.formTypeId   = ft.formTypeId "
            + "LEFT JOIN Employees ea ON fr.approverId  = ea.employeeId "
            + "LEFT JOIN Users ua     ON ea.userId       = ua.userId "
            + "LEFT JOIN Departments td ON fr.targetDepartmentId = td.departmentId "
            + "LEFT JOIN Roles tr       ON fr.targetRoleId       = tr.roleId ";

    /**
     * Map ResultSet thành đúng subtype DTO dựa vào formTypeCode.
     * - "LEAVE"     → LeaveFormRequestDTO (có startDate, endDate, totalDays, usedDays)
     * - "COMPLAINT" → ComplaintFormRequestDTO
     * - khác       → FormRequestDTO (base)
     */
    private FormRequestDTO mapFormRequestDTO(ResultSet rs) throws SQLException {
        String typeCode = rs.getString("formTypeCode");

        FormRequestDTO fr = new FormRequestDTO();
        if ("LEAVE".equals(typeCode)) {
            fr = new LeaveFormRequestDTO();
        } else if ("COMPLAINT".equals(typeCode)) {
            fr = new ComplaintFormRequestDTO();
        } else if ("TRANSFER".equals(typeCode) || "PROMOTION_DEMOTION".equals(typeCode)) {
            fr = new TransferRequestDTO();
        } else {
            fr = new FormRequestDTO();
        }

        if (fr instanceof LeaveFormRequestDTO) {
            LeaveFormRequestDTO leaveFr = (LeaveFormRequestDTO) fr;
            leaveFr.setStartDate(rs.getDate("startDate"));
            leaveFr.setEndDate(rs.getDate("endDate"));
            int totalDays = rs.getInt("totalDays");
            leaveFr.setTotalDays(rs.wasNull() ? null : totalDays);
            int usedDays = rs.getInt("usedDays");
            leaveFr.setUsedDays(rs.wasNull() ? null : usedDays);
        } else if (fr instanceof ComplaintFormRequestDTO) {
            ComplaintFormRequestDTO compFr = (ComplaintFormRequestDTO) fr;
            compFr.setStartDate(rs.getDate("startDate"));
            compFr.setStartTime(rs.getTime("startTime"));
            compFr.setEndTime(rs.getTime("endTime"));
        } else if (fr instanceof TransferRequestDTO) {
            TransferRequestDTO transferFr = (TransferRequestDTO) fr;
            int tDeptId = rs.getInt("targetDepartmentId");
            transferFr.setTargetDepartmentId(rs.wasNull() ? null : tDeptId);
            int tRoleId = rs.getInt("targetRoleId");
            transferFr.setTargetRoleId(rs.wasNull() ? null : tRoleId);
            
            transferFr.setTargetDepartmentName(rs.getNString("targetDepartmentName"));
            transferFr.setTargetRoleName(rs.getString("targetRoleName"));
        }

        fr.setFormId(rs.getInt("formId"));
        fr.setFormCode(rs.getString("formCode"));
        fr.setEmployeeId(rs.getInt("employeeId"));
        fr.setFormTypeId(rs.getInt("formTypeId"));
        fr.setReason(rs.getNString("reason"));
        fr.setStatus(rs.getInt("status"));
        int approverId = rs.getInt("approverId");
        fr.setApproverId(rs.wasNull() ? null : approverId);
        fr.setApproverNote(rs.getNString("approverNote"));
        fr.setApprovedAt(rs.getTimestamp("approvedAt"));
        fr.setAttachmentUrl(rs.getString("attachmentUrl"));
        fr.setAttachmentName(rs.getString("attachmentName"));
        fr.setCreatedAt(rs.getTimestamp("createdAt"));
        fr.setUpdatedAt(rs.getTimestamp("updatedAt"));
        fr.setEmployeeCode(rs.getString("employeeCode"));
        fr.setFullName(rs.getNString("fullName"));
        fr.setDepartmentId(rs.getInt("departmentId"));
        fr.setDepartmentName(rs.getNString("departmentName"));
        fr.setFormTypeName(rs.getNString("formTypeName"));
        fr.setFormTypeCode(typeCode);
        fr.setApproverName(rs.getNString("approverName"));
        return fr;
    }
}
