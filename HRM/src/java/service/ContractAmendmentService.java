/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.ContractAmendmentDAO;
import dao.EmploymentContractDAO;
import dal.DBContext;
import dto.TransferRequestDTO;
import dto.EmployeeDetailDTO;
import model.ContractAmendment;
import model.ContractOperationResult;
import model.EmploymentContract;
import enums.AmendmentType;
import enums.ContractErrorCode;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Admin
 */
public class ContractAmendmentService {

    private static final Logger LOGGER = Logger.getLogger(ContractAmendmentService.class.getName());

    private final ContractAmendmentDAO amendmentDAO;
    private final EmploymentContractDAO contractDAO;
    private final DBContext dbContext;

    public ContractAmendmentService(ContractAmendmentDAO amendmentDAO,
            EmploymentContractDAO contractDAO,
            DBContext dbContext) {
        this.amendmentDAO = amendmentDAO;
        this.contractDAO = contractDAO;
        this.dbContext = dbContext;
    }

    public int recordAmendment(Connection conn, ContractAmendment amendment) throws SQLException {
        if (amendment == null) {
            throw new IllegalArgumentException("Amendment cannot be null");
        }
        if (amendment.getContractId() <= 0) {
            throw new IllegalArgumentException("ContractId is required for amendment");
        }
        if (amendment.getAmendmentType() == null) {
            throw new IllegalArgumentException("AmendmentType is required");
        }
        if (amendment.getEffectiveDate() == null) {
            throw new IllegalArgumentException("EffectiveDate is required");
        }
        if (amendment.getCreatedBy() == null || amendment.getCreatedBy() <= 0) {
            throw new IllegalArgumentException("CreatedBy (userId) is required");
        }

        return amendmentDAO.addAmendment(conn, amendment);
    }
    
    public ContractOperationResult createTransferAmendment(TransferRequestDTO form, EmployeeDetailDTO currentEmployee, int approverEmployeeId) {
        if (form == null || currentEmployee == null || approverEmployeeId <= 0) {
            return new ContractOperationResult(false, "INVALID_INPUT", "Du lieu phu luc hop dong khong hop le.");
        }

        if (form.getTargetDepartmentId() == null) {
            return new ContractOperationResult(false, "INVALID_TRANSFER", "Don thuyen chuyen khong co phong ban dich.");
        }

        Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);

            int approverUserId = getApproverUserId(conn, approverEmployeeId);
            if (approverUserId <= 0) {
                conn.rollback();
                return new ContractOperationResult(false, "INVALID_APPROVER", "Khong tim thay userId cua nguoi duyet.");
            }

            EmploymentContract contract = contractDAO.getActiveOrPendingContract(conn, currentEmployee.getEmployeeId());
            if (contract == null) {
                conn.rollback();
                return new ContractOperationResult(true, null, "Nhan vien chua co hop dong hien tai hoac sap hieu luc, bo qua tao phu luc.");
            }

            ContractAmendment amendment = new ContractAmendment();
            amendment.setContractId(contract.getContractId());
            amendment.setAmendmentCode(generateAmendmentCode(contract.getContractId(), form.getFormId()));

            amendment.setAmendmentType(AmendmentType.TRANSFER);

            amendment.setEffectiveDate(Date.valueOf(LocalDate.now()));

            amendment.setOldDepartmentId(currentEmployee.getDepartmentId());
            amendment.setNewDepartmentId(form.getTargetDepartmentId());
            amendment.setOldPositionId(currentEmployee.getPositionId());
            amendment.setNewPositionId(currentEmployee.getPositionId()); 

            amendment.setOldSalary(contract.getSalary());
            amendment.setNewSalary(contract.getSalary()); 

            amendment.setReason(form.getReason());
            amendment.setSourceFormId(form.getFormId());
            amendment.setStatus("APPROVED"); 
            amendment.setCreatedBy(approverUserId);
            amendment.setApprovedBy(approverUserId);


            int amendmentId = recordAmendment(conn, amendment);
            if (amendmentId <= 0) {
                conn.rollback();
                return new ContractOperationResult(false, ContractErrorCode.DATABASE_ERROR.name(), "Khong the tao phu luc hop dong.");
            }

            contractDAO.insertAuditLog(conn, contract.getContractId(), contract.getStatus().name(), contract.getStatus().name(),
                    approverUserId, "Tao phu luc thuyen chuyen phong ban tu don #" + form.getFormId());

            conn.commit();
            return new ContractOperationResult(true, null, "Tao phu luc hop dong thanh cong");
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            LOGGER.log(Level.SEVERE, "Error creating transfer contact amendment", e);
            return new ContractOperationResult(false, ContractErrorCode.DATABASE_ERROR.name(), "Loi he thong khi tao phu luc hop dong: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    private String generateAmendmentCode(int contractId, int formId) {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return "AMD-" + contractId + "-" + formId + "-" + date;
    }

    private int getApproverUserId(Connection conn, int employeeId) {
        String sql = "SELECT userId FROM Employees WHERE employeeId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("userId");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying userId from employeeId: " + employeeId, e);
        }
        return -1;
    }
}
