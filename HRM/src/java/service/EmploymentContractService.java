package service;

import dao.EmploymentContractDAO;
import dal.DBContext;
import dao.RoleDAO;
import dto.EmployeeDetailDTO;
import enums.ContractErrorCode;
import model.ContractOperationResult;
import enums.ContractStatus;
import model.EmploymentContract;
import enums.ValidationError;
import model.ValidationResult;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import enums.ContractType;
import java.math.BigDecimal;

/**
 * Service Layer for Employee Contract Management.
 *
 * This class implements the structural split between HR Manual Flow and System
 * Automated Scheduler Flow to ensure strict lifecycle integrity and transaction
 * safety.
 *
 * Architecture: - HR Flow: Independent connection per request. - Scheduler
 * Flow: Shared Connection with manual commit/rollback per contract.
 */
public class EmploymentContractService {

    private static final Logger LOGGER = Logger.getLogger(EmploymentContractService.class.getName());
    private final EmploymentContractDAO contractDAO;
    private final dao.EmployeeDAO employeeDAO;
    private final DBContext dbContext;
    private final RoleDAO roleDAO = new RoleDAO();

    public EmploymentContractService(EmploymentContractDAO contractDAO, dao.EmployeeDAO employeeDAO, DBContext dbContext) {
        this.contractDAO = contractDAO;
        this.employeeDAO = employeeDAO;
        this.dbContext = dbContext;
    }

    private ValidationResult validateEmployee(int employeeId) {
        if (employeeId <= 0) {
            return ValidationResult.failure(ValidationError.EMPLOYEE_NOT_FOUND,
                    "Mã nhân viên không hợp lệ.");
        }

        if (employeeDAO.getEmployeeById(employeeId) == null) {
            return ValidationResult.failure(ValidationError.EMPLOYEE_NOT_FOUND,
                    "Nhân viên không tồn tại trong hệ thống.");
        }
        return ValidationResult.success();
    }

    private ValidationResult validateDuplicateCode(String contractCode, Integer excludeContractId) {
        boolean exists = contractDAO.existsByContractCode(contractCode, excludeContractId);
        if (exists) {
            return ValidationResult.failure(ValidationError.DUPLICATE_CONTRACT_CODE,
                    "Mã hợp đồng '" + contractCode + "' đã tồn tại trong hệ thống.");
        }
        return ValidationResult.success();
    }

    private ValidationResult validateDates(ContractType type, java.sql.Date effectiveDate, java.sql.Date endDate) {
        if (effectiveDate == null) {
            return ValidationResult.failure(ValidationError.INVALID_CONTRACT_TYPE,
                    "Ngày hiệu lực không hợp lệ.");
        }

        if (endDate != null && endDate.before(effectiveDate)) {
            return ValidationResult.failure(ValidationError.END_DATE_BEFORE_START_DATE,
                    "Ngày kết thúc không được trước ngày bắt đầu.");
        }

        boolean typeRequiresEndDate = type != null && type.hasEndDate();
        boolean hasEndDate = endDate != null;

        if (type != null && typeRequiresEndDate && !hasEndDate) {
            return ValidationResult.failure(ValidationError.END_DATE_REQUIRED_FOR_TYPE,
                    "Loại hợp đồng " + type.getDisplayName() + " yêu cầu ngày kết thúc.");
        }

        if (type != null && !typeRequiresEndDate && hasEndDate) {
            return ValidationResult.failure(ValidationError.END_DATE_NOT_ALLOWED_FOR_TYPE,
                    "Hợp đồng không xác định thời hạn không được có ngày kết thúc.");
        }

        return ValidationResult.success();
    }

    private ValidationResult validateOverlap(Connection conn, int employeeId,
            java.sql.Date effectiveDate, java.sql.Date endDate, Integer excludeContractId) throws SQLException {
        boolean overlap = contractDAO.hasOverlappingContract(conn, employeeId, effectiveDate, endDate, excludeContractId);
        if (overlap) {
            return ValidationResult.failure(ValidationError.OVERLAPPING_CONTRACT,
                    "Thời gian hợp đồng bị trùng lặp với hợp đồng đang có hiệu lực hoặc đang chờ duyệt của nhân viên này.");
        }
        return ValidationResult.success();
    }

    public ValidationResult validateForCreate(EmploymentContract contract) {
        ValidationResult empResult = validateEmployee(contract.getEmployeeId());
        if (!empResult.isSuccess()) {
            return empResult;
        }

        ValidationResult codeResult = validateDuplicateCode(contract.getContractCode(),
                contract.getContractId() > 0 ? contract.getContractId() : null);
        if (!codeResult.isSuccess()) {
            return codeResult;
        }

        if (contract.getContractType() == null) {
            return ValidationResult.failure(ValidationError.INVALID_CONTRACT_TYPE,
                    "Loại hợp đồng không hợp lệ.");
        }

        ValidationResult dateResult = validateDates(contract.getContractType(),
                contract.getEffectiveDate(), contract.getEndDate());
        if (!dateResult.isSuccess()) {
            return dateResult;
        }

        if (contract.getSalary() == null || contract.getSalary().compareTo(java.math.BigDecimal.ZERO) < 0) {
            return ValidationResult.failure(ValidationError.INVALID_SALARY,
                    "Mức lương không hợp lệ.");
        }

        java.sql.Date today = java.sql.Date.valueOf(java.time.LocalDate.now());
        if (contract.getEffectiveDate() != null && contract.getEffectiveDate().before(today)) {
            return ValidationResult.failure(ValidationError.EFFECTIVE_DATE_IN_PAST,
                    "Ngày hiệu lực không được trong quá khứ.");
        }

        return ValidationResult.success();
    }

    public ContractOperationResult createContract(EmploymentContract contract) {
        Connection conn = null;
        try {
            ValidationResult validation = validateForCreate(contract);
            if (!validation.isSuccess()) {
                return new ContractOperationResult(false,
                        validation.getError().name(),
                        validation.getMessage());
            }

            conn = dbContext.getConnection();
            conn.setAutoCommit(false);

            ValidationResult overlapResult = validateOverlap(conn, contract.getEmployeeId(),
                    contract.getEffectiveDate(), contract.getEndDate(), null);
            if (!overlapResult.isSuccess()) {
                conn.rollback();
                return new ContractOperationResult(false,
                        ContractErrorCode.OVERLAP_DETECTED.name(),
                        overlapResult.getMessage());
            }

            EmployeeDetailDTO emp = employeeDAO.getEmployeeById(contract.getEmployeeId());
            if (emp != null) {
                contract.setDepartmentName(emp.getDepartmentName());
                contract.setPositionName(emp.getPositionName());
            }

            int contractId = contractDAO.addContract(conn, contract);

            if (contractId > 0) {

                contractDAO.insertAuditLog(conn, contractId,
                        null, ContractStatus.PENDING_APPROVAL.name(),
                        contract.getCreatedBy(), "Tạo hợp đồng");

                conn.commit();
                LOGGER.log(Level.INFO, "Contract created successfully for employee {0}, contractId={1}", new Object[]{contract.getEmployeeId(), contractId});
                return new ContractOperationResult(true, null, "Tao hop dong thanh cong.");
            } else {
                conn.rollback();
                return new ContractOperationResult(false,
                        ContractErrorCode.DATABASE_ERROR.name(), "Khong the them hop dong vao database.");
            }
        } catch (SQLException e) {
            if (conn != null) try {
                conn.rollback();
            } catch (SQLException ex) {
            }
            LOGGER.log(Level.SEVERE, "Database error during contract creation", e);
            return new ContractOperationResult(false,
                    ContractErrorCode.DATABASE_ERROR.name(), "Loi he thong khi tao hop dong: " + e.getMessage());
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException ex) {
            }
        }
    }

    public ContractOperationResult activateContract(int contractId, int userId) {
        Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);

            EmploymentContract contract = contractDAO.getContractById(conn, contractId);

            if (contract == null) {
                conn.rollback();
                return new ContractOperationResult(false,
                        ContractErrorCode.DATABASE_ERROR.name(), "Contract not found.");
            }

            if (contract.getStatus() != ContractStatus.PENDING_ACTIVATION) {
                conn.rollback();
                return new ContractOperationResult(false,
                        ContractErrorCode.INVALID_CONTRACT_STATUS.name(),
                        "Only pending activation contracts can be activated.");
            }

            Date today = Date.valueOf(LocalDate.now());
            if (contract.getEffectiveDate().after(today)) {
                conn.rollback();
                return new ContractOperationResult(false,
                        ContractErrorCode.INVALID_EFFECTIVE_DATE.name(),
                        "Ngày hiệu lực chưa đến.");
            }

            boolean success = contractDAO.updateContractStatus(conn, contractId, ContractStatus.ACTIVE, null, null);

            if (success) {
                contractDAO.insertAuditLog(conn, contractId,
                        contract.getStatus().name(), ContractStatus.ACTIVE.name(),
                        userId, "Kich hoat hop dong");

                conn.commit();
                LOGGER.log(Level.INFO, "HR kích hoạt thủ công: Hợp đồng {0} -> ACTIVE", contractId);
                return new ContractOperationResult(true, null, "Kich hoat hop dong thanh cong.");
            } else {
                conn.rollback();
                return new ContractOperationResult(false,
                        ContractErrorCode.DATABASE_ERROR.name(), "Failed to update contract status.");
            }

        } catch (SQLException e) {
            if (conn != null) try {
                conn.rollback();
            } catch (SQLException ex) {
            }
            LOGGER.log(Level.SEVERE, "Database error during HR activation for contract " + contractId, e);
            return new ContractOperationResult(false,
                    ContractErrorCode.DATABASE_ERROR.name(), "Database connection error: " + e.getMessage());
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException ex) {
            }
        }
    }

    public ContractOperationResult approveContract(int contractId, int userId) {
        Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);

            EmploymentContract contract = contractDAO.getContractById(conn, contractId);
            if (contract == null) {
                conn.rollback();
                return new ContractOperationResult(false, ContractErrorCode.CONTRACT_NOT_FOUND.name(), "Khong tim thay hop dong.");
            }

            if (contract.getStatus() != ContractStatus.PENDING_APPROVAL) {
                conn.rollback();
                return new ContractOperationResult(false, ContractErrorCode.INVALID_CONTRACT_STATUS.name(), "Hop dong khong o trang thai cho duyet.");
            }

            if (contractDAO.hasOverlappingContract(conn, contract.getEmployeeId(),
                    contract.getEffectiveDate(), contract.getEndDate(), contractId)) {
                conn.rollback();
                return new ContractOperationResult(false, ContractErrorCode.OVERLAP_DETECTED.name(),
                        "Thoi gian hop dong bi trung lap voi mot hop dong dang co hieu luc.");
            }

            java.sql.Date today = java.sql.Date.valueOf(java.time.LocalDate.now());

            ContractStatus targetStatus = ContractStatus.PENDING_ACTIVATION;

            String oldStatus = contract.getStatus().name();
            String newStatus = targetStatus.name();
            String auditNote = "Phe duyet";

            boolean updated = contractDAO.updateContractStatus(conn, contractId, targetStatus, null, null);
            if (!updated) {
                conn.rollback();
                return new ContractOperationResult(false, ContractErrorCode.DATABASE_ERROR.name(), "Khong the cap nhat trang thai hop dong.");
            }

            contractDAO.insertAuditLog(conn, contractId, oldStatus, newStatus, userId, auditNote);
            conn.commit();
            return new ContractOperationResult(true, null, "Phe duyet thanh cong.");
        } catch (SQLException e) {
            if (conn != null) try {
                conn.rollback();
            } catch (SQLException ex) {
            }
            LOGGER.log(Level.SEVERE, "Database error during contract approval", e);
            return new ContractOperationResult(false, ContractErrorCode.DATABASE_ERROR.name(),
                    "Loi he thong khi phe duyet: " + e.getMessage());
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException ex) {
            }
        }
    }

    public ContractOperationResult updateContractStatusWithAudit(int contractId,
            ContractStatus newStatus, java.sql.Date actualEndDate,
            String reason, int userId) {
        Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);

            EmploymentContract contract = contractDAO.getContractById(conn, contractId);
            if (contract == null) {
                conn.rollback();
                return new ContractOperationResult(false, ContractErrorCode.CONTRACT_NOT_FOUND.name(), "Không tìm thấy hợp đồng.");
            }

            String oldStatus = contract.getStatus().name();
            String newStatusStr = newStatus.name();
            String auditReason = "Hợp đồng chuyển từ " + oldStatus + " sang " + newStatusStr
                    + ". " + (reason != null ? reason : "");

            // Update status with termination details
            boolean updated = contractDAO.updateContractStatus(conn, contractId,
                    newStatus, actualEndDate, reason);

            if (!updated) {
                conn.rollback();
                return new ContractOperationResult(false,
                        ContractErrorCode.DATABASE_ERROR.name(),
                        "Không thể cập nhật trạng thái hợp đồng.");
            }

            // Audit log (same connection = same transaction)
            contractDAO.insertAuditLog(conn, contractId, oldStatus, newStatusStr,
                    userId, auditReason);

            conn.commit();
            return new ContractOperationResult(true, null,
                    "Cập nhật trạng thái hợp đồng thành công.");

        } catch (SQLException e) {
            if (conn != null) try {
                conn.rollback();
            } catch (SQLException ex) {
            }
            LOGGER.log(Level.SEVERE,
                    "Database error during status transition for contract " + contractId, e);
            return new ContractOperationResult(false,
                    ContractErrorCode.DATABASE_ERROR.name(),
                    "Lỗi hệ thống: " + e.getMessage());
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException ex) {
            }
        }
    }

    /**
     * Convenience: Terminate a contract (ACTIVE or PENDING_ACTIVATION) with
     * full validation + audit.
     */
    public ContractOperationResult terminateContract(int contractId,
            java.sql.Date terminationDate, String reason, int userId) {
        Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);

            EmploymentContract contract = contractDAO.getContractById(conn, contractId);
            if (contract == null) {
                conn.rollback();
                return new ContractOperationResult(false, ContractErrorCode.CONTRACT_NOT_FOUND.name(), "Không tìm thấy hợp đồng.");
            }

            ContractStatus status = contract.getStatus();
            if (status != ContractStatus.ACTIVE && status != ContractStatus.PENDING_ACTIVATION) {
                conn.rollback();
                return new ContractOperationResult(false,
                        ContractErrorCode.INVALID_CONTRACT_STATUS.name(),
                        "Chỉ có thể chấm dứt hợp đồng đang hiệu lực hoặc đang chờ kích hoạt.");
            }

            // Business Validation: Date rules
            if (status == ContractStatus.ACTIVE) {
                if (terminationDate == null || terminationDate.before(contract.getEffectiveDate())) {
                    conn.rollback();
                    return new ContractOperationResult(false, ContractErrorCode.INVALID_TERMINATION_DATE.name(),
                            "Ngày chấm dứt không được trước ngày hiệu lực.");
                }
                if (contract.getEndDate() != null && terminationDate.after(contract.getEndDate())) {
                    conn.rollback();
                    return new ContractOperationResult(false, ContractErrorCode.INVALID_TERMINATION_DATE.name(),
                            "Ngày chấm dứt không được sau ngày kết thúc hợp đồng.");
                }
            } else if (status == ContractStatus.PENDING_ACTIVATION) {
                if (terminationDate == null || terminationDate.after(contract.getEffectiveDate())) {
                    conn.rollback();
                    return new ContractOperationResult(false, ContractErrorCode.INVALID_TERMINATION_DATE.name(),
                            "Ngày chấm dứt không được sau ngày hiệu lực của hợp đồng chờ kích hoạt.");
                }
                if (terminationDate.after(java.sql.Date.valueOf(LocalDate.now()))) {
                    conn.rollback();
                    return new ContractOperationResult(false, ContractErrorCode.INVALID_TERMINATION_DATE.name(),
                            "Ngày chấm dứt không được sau ngày hiện tại.");
                }
            }

            String oldStatus = status.name();
            String newStatus = ContractStatus.TERMINATED.name();
            String auditReason = "Chấm dứt hợp đồng. Lý do: " + (reason != null ? reason : "");

            boolean updated = contractDAO.updateContractStatus(conn, contractId,
                    ContractStatus.TERMINATED, terminationDate, reason);

            if (!updated) {
                conn.rollback();
                return new ContractOperationResult(false,
                        ContractErrorCode.DATABASE_ERROR.name(),
                        "Không thể chấm dứt hợp đồng.");
            }

            contractDAO.insertAuditLog(conn, contractId,
                    oldStatus, newStatus, userId, auditReason);

            conn.commit();
            return new ContractOperationResult(true, null,
                    "Đã chấm dứt hợp đồng thành công.");

        } catch (SQLException e) {
            if (conn != null) try {
                conn.rollback();
            } catch (SQLException ex) {
            }
            LOGGER.log(Level.SEVERE,
                    "Database error during contract termination: " + contractId, e);
            return new ContractOperationResult(false,
                    ContractErrorCode.DATABASE_ERROR.name(),
                    "Lỗi hệ thống khi chấm dứt hợp đồng: " + e.getMessage());
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException ex) {
            }
        }
    }

    public ContractOperationResult rejectContract(int contractId, int userId, String reason) {
        Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);

            EmploymentContract contract = contractDAO.getContractById(conn, contractId);
            if (contract == null) {
                conn.rollback();
                return new ContractOperationResult(false, ContractErrorCode.CONTRACT_NOT_FOUND.name(), "Khong tim thay hop dong.");
            }

            if (contract.getStatus() != ContractStatus.PENDING_APPROVAL) {
                conn.rollback();
                return new ContractOperationResult(false, ContractErrorCode.INVALID_CONTRACT_STATUS.name(),
                        "Chi co the tu choi hop dong dang cho duyet.");
            }

            if (reason == null || reason.trim().isEmpty()) {
                conn.rollback();
                return new ContractOperationResult(false, ContractErrorCode.REJECTION_REASON_REQUIRED.name(),
                        "Vui long nhap ly do tu choi.");
            }

            String oldStatus = contract.getStatus().name();
            String newStatus = ContractStatus.REJECTED.name();
            boolean updated = contractDAO.updateContractStatus(conn, contractId, ContractStatus.REJECTED, null, null);
            if (!updated) {
                conn.rollback();
                return new ContractOperationResult(false, ContractErrorCode.DATABASE_ERROR.name(), "Khong the tu choi hop dong.");
            }

            boolean reasonUpdated = contractDAO.updateRejectionReason(conn, contractId, reason);
            if (!reasonUpdated) {
                conn.rollback();
                return new ContractOperationResult(false, ContractErrorCode.DATABASE_ERROR.name(), "Khong the luu ly do tu choi");
            }
            contractDAO.insertAuditLog(conn, contractId, oldStatus, newStatus, userId,
                    "Tu choi: " + reason);

            conn.commit();
            return new ContractOperationResult(true, null, "Tu choi hop dong thanh cong.");
        } catch (SQLException e) {
            if (conn != null) try {
                conn.rollback();
            } catch (SQLException ex) {
            }
            LOGGER.log(Level.SEVERE, "Database error during contract rejection", e);
            return new ContractOperationResult(false, ContractErrorCode.DATABASE_ERROR.name(),
                    "Loi he thong khi tu choi: " + e.getMessage());
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException ex) {
            }
        }
    }

    public ContractOperationResult cancelContract(int contractId, int userId) {
        Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);

            EmploymentContract contract = contractDAO.getContractById(conn, contractId);
            if (contract == null) {
                conn.rollback();
                return new ContractOperationResult(false, ContractErrorCode.CONTRACT_NOT_FOUND.name(), "Khong tim thay hop dong.");
            }

            if (contract.getStatus() != ContractStatus.PENDING_APPROVAL) {
                conn.rollback();
                return new ContractOperationResult(false, ContractErrorCode.INVALID_CONTRACT_STATUS.name(),
                        "Chi co the huy hop dong dang cho duyet.");
            }

            String roleName = roleDAO.getRoleByUserId(userId);
            boolean isHrStaff = roleName != null
                    && (roleName.equalsIgnoreCase("HRManager")
                    || roleName.equalsIgnoreCase("HREmployee"));

            if (!isHrStaff) {
                conn.rollback();
                return new ContractOperationResult(false, ContractErrorCode.FORBIDDEN.name(), "Chi nhan su phong HR moi co quen huy hop dong.");
            }

            String oldStatus = contract.getStatus().name();
            String newStatus = ContractStatus.CANCELLED.name();
            boolean updated = contractDAO.updateContractStatus(conn, contractId, ContractStatus.CANCELLED, null, "Huy truoc khi duyet");
            if (!updated) {
                conn.rollback();
                return new ContractOperationResult(false, ContractErrorCode.DATABASE_ERROR.name(), "Khong the huy hop dong.");
            }
            contractDAO.insertAuditLog(conn, contractId, oldStatus, newStatus, userId,
                    "Huy hop dong truoc khi duyet");

            conn.commit();
            return new ContractOperationResult(true, null, "Huy hop dong thanh cong.");
        } catch (SQLException e) {
            if (conn != null) try {
                conn.rollback();
            } catch (SQLException ex) {
            }
            LOGGER.log(Level.SEVERE, "Database error during contract cancellation", e);
            return new ContractOperationResult(false, ContractErrorCode.DATABASE_ERROR.name(),
                    "Loi he thong khi huy: " + e.getMessage());
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException ex) {
            }
        }
    }

    public ContractOperationResult activatePendingContract(int contractId, Connection conn) throws SQLException {
        EmploymentContract contract = contractDAO.getContractById(conn, contractId);

        if (contract == null) {
            return new ContractOperationResult(false,
                    ContractErrorCode.DATABASE_ERROR.name(), "Contract not found during auto-activation.");
        }

        if (contract.getStatus() != ContractStatus.PENDING_ACTIVATION) {
            return new ContractOperationResult(false,
                    ContractErrorCode.INVALID_CONTRACT_STATUS.name(),
                    "Contract is not in PENDING_ACTIVATION state.");
        }

        Date today = Date.valueOf(LocalDate.now());
        if (contract.getEffectiveDate().after(today)) {
            return new ContractOperationResult(false,
                    ContractErrorCode.INVALID_EFFECTIVE_DATE.name(),
                    "Effective date has not arrived yet.");
        }

        boolean success = contractDAO.updateContractStatus(conn, contractId, ContractStatus.ACTIVE, null, null);

        if (success) {
            contractDAO.insertAuditLog(conn, contractId,
                    ContractStatus.PENDING_ACTIVATION.name(), ContractStatus.ACTIVE.name(),
                    1, "Hệ thống tự động kích hoạt");

            return new ContractOperationResult(true, null, "System auto-activation successful.");
        } else {
            return new ContractOperationResult(false,
                    ContractErrorCode.DATABASE_ERROR.name(), "Failed to execute auto-activation update.");
        }
    }

    public void processDailyContractUpdates() {
        LOGGER.log(Level.INFO, "Starting Automated Daily Contract Updates Batch Process...");

        List<EmploymentContract> pendingList = contractDAO.getContractsReadyForActivation();
        LOGGER.log(Level.INFO, "Found {0} contracts ready for auto-activation.", pendingList.size());

        for (EmploymentContract contract : pendingList) {
            int id = contract.getContractId();
            try (Connection conn = dbContext.getConnection()) {
                conn.setAutoCommit(false); // Transactional control

                ContractOperationResult result = activatePendingContract(id, conn);

                if (result.isSuccess()) {
                    conn.commit();
                    LOGGER.log(Level.INFO, "Transaction committed: Contract {0} activated by system.", id);
                } else {
                    conn.rollback();
                    LOGGER.log(Level.WARNING, "Transaction rolled back: Contract {0} failed activation. Reason: {1}",
                            new Object[]{id, result.getMessage()});
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "System error during batch processing for contract " + id, e);
            }
        }

        List<EmploymentContract> expirationList = contractDAO.getContractsReadyForExpiration();
        LOGGER.log(Level.INFO, "Found {0} contracts ready for auto-expiration.", expirationList.size());

        for (EmploymentContract contract : expirationList) {
            int id = contract.getContractId();
            Connection conn = null;
            try {
                conn = dbContext.getConnection();
                conn.setAutoCommit(false);

                // Fetch current status for audit log
                EmploymentContract current = contractDAO.getContractById(conn, id);
                String oldStatus = current != null ? current.getStatus().name() : "ACTIVE";

                boolean success = contractDAO.updateContractStatus(conn, id, ContractStatus.EXPIRED, null, null);

                if (success) {
                    // Audit log for auto-expiration
                    contractDAO.insertAuditLog(conn, id,
                            oldStatus, ContractStatus.EXPIRED.name(),
                            1, "Hệ thống tự động hết hạn");

                    conn.commit();
                    LOGGER.log(Level.INFO, "Transaction committed: Contract {0} expired by system.", id);
                } else {
                    conn.rollback();
                    LOGGER.log(Level.WARNING, "Transaction rolled back: Contract {0} failed expiration.", id);
                }
            } catch (SQLException e) {
                if (conn != null) try {
                    conn.rollback();
                } catch (SQLException ex) {
                }
                LOGGER.log(Level.SEVERE, "System error during expiration batch for contract " + id, e);
            } finally {
                if (conn != null) try {
                    conn.close();
                } catch (SQLException ex) {
                }
            }
        }

        LOGGER.log(Level.INFO, "Automated Daily Contract Updates Batch Process Completed.");
    }

    public Date calculateEndDate(Date effectiveDate, int durationValue, String durationUnit) {
        if (effectiveDate == null) {
            return null;
        }
        if (durationUnit == null) {
            return null;
        }

        LocalDate localDate = effectiveDate.toLocalDate();
        LocalDate result;

        switch (durationUnit) {
            case "DAY":
                result = localDate.plusDays(durationValue);
                break;
            case "MONTH":
                result = localDate.plusMonths(durationValue);
                break;
            case "YEAR":
                result = localDate.plusYears(durationValue);
                break;
            default:
                return null;
        }

        return Date.valueOf(result);
    }
}
