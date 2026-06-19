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
import dto.FormRequestDTO;
import dto.LeaveFormRequestDTO;
import dto.ComplaintFormRequestDTO;
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

    // INSERT đơn chung (Khiếu nại, hoặc loại không có ngày)
    public int addFormRequest(FormRequest fr) {
        String SQL = """
                     INSERT INTO form_requests
                     (formCode, employeeId, formTypeId, reason,
                      startDate, endDate, totalDays, usedDays,
                      attachmentUrl, attachmentName, status)
                     VALUES (?, ?, ?, ?, NULL, NULL, NULL, 0, ?, ?, 0)
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
            if (fr.getTotalDays() == null) ps.setNull(7, Types.DECIMAL); else ps.setDouble(7, fr.getTotalDays());
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
            ps.setInt(2, approverId);
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

    private static final String MANAGER_FORM_QUERY
            = "SELECT fr.formId, fr.formCode, fr.employeeId, fr.formTypeId, fr.reason, "
            + "fr.startDate, fr.endDate, fr.totalDays, fr.usedDays, "
            + "fr.status, fr.approverId, fr.approverNote, fr.approvedAt, "
            + "fr.attachmentUrl, fr.attachmentName, fr.createdAt, fr.updatedAt, "
            + "e.employeeCode, u.fullName, "
            + "d.departmentId, d.departmentName, "
            + "ft.formTypeName, ft.formTypeCode, "
            + "ua.fullName AS approverName "
            + "FROM Form_Requests fr "
            + "JOIN Employees e   ON fr.employeeId  = e.employeeId "
            + "JOIN Users u       ON e.userId        = u.userId "
            + "LEFT JOIN Departments d  ON e.departmentId = d.departmentId "
            + "JOIN Form_Types ft ON fr.formTypeId   = ft.formTypeId "
            + "LEFT JOIN Employees ea ON fr.approverId  = ea.employeeId "
            + "LEFT JOIN Users ua     ON ea.userId       = ua.userId ";

    /**
     * Map ResultSet thành đúng subtype DTO dựa vào formTypeCode.
     * - "LEAVE"     → LeaveFormRequestDTO (có startDate, endDate, totalDays, usedDays)
     * - "COMPLAINT" → ComplaintFormRequestDTO
     * - khác       → FormRequestDTO (base)
     */
    private FormRequestDTO mapFormRequestDTO(ResultSet rs) throws SQLException {
        String typeCode = rs.getString("formTypeCode");

        FormRequestDTO fr;
        if ("LEAVE".equals(typeCode)) {
            LeaveFormRequestDTO leave = new LeaveFormRequestDTO();
            leave.setStartDate(rs.getDate("startDate"));
            leave.setEndDate(rs.getDate("endDate"));
            double totalDays = rs.getDouble("totalDays");
            leave.setTotalDays(rs.wasNull() ? null : totalDays);
            double usedDays = rs.getDouble("usedDays");
            leave.setUsedDays(rs.wasNull() ? null : usedDays);
            fr = leave;
        } else if ("COMPLAINT".equals(typeCode)) {
            fr = new ComplaintFormRequestDTO();
        } else {
            fr = new FormRequestDTO();
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
