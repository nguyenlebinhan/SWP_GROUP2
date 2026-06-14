package service;

import dao.EmploymentContractDAO;
import dal.DBContext;
import model.ContractOperationResult;
import model.ContractStatus;
import model.EmploymentContract;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service Layer for Employee Contract Management.
 * 
 * This class implements the structural split between HR Manual Flow 
 * and System Automated Scheduler Flow to ensure strict lifecycle 
 * integrity and transaction safety.
 * 
 * Architecture:
 * - HR Flow: Independent connection per request.
 * - Scheduler Flow: Shared Connection with manual commit/rollback per contract.
 */
public class EmploymentContractService {

    private static final Logger LOGGER = Logger.getLogger(EmploymentContractService.class.getName());
    private final EmploymentContractDAO contractDAO;
    private final DBContext dbContext;

    /**
     * Constructor with DAO and DBContext injection.
     * 
     * @param contractDAO the DAO for contract persistence
     * @param dbContext   the context for acquiring database connections
     */
    public EmploymentContractService(EmploymentContractDAO contractDAO, DBContext dbContext) {
        this.contractDAO = contractDAO;
        this.dbContext = dbContext;
    }

    // =========================================================================
    // [HR FLOW] - CONTRACT CREATION WITH OVERLAP PROTECTION
    // =========================================================================

    /**
     * Create a new contract with proactive overlap validation.
     * This prevents users from creating overlapping draft contracts.
     *
     * @param contract The contract to insert (must have employeeId, effectiveDate, endDate, status=DRAFT)
     * @return ContractOperationResult with DATE_OVERLAP if conflict detected
     */
    public ContractOperationResult createContract(EmploymentContract contract) {
        Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);

            // 1. Proactive overlap check BEFORE insert
            if (contractDAO.hasOverlappingContract(conn, contract.getEmployeeId(),
                    contract.getEffectiveDate(), contract.getEndDate(), null)) {
                conn.rollback();
                return new ContractOperationResult(false,
                    ContractOperationResult.DATE_OVERLAP,
                    "Khong the tao! Thoi gian hop dong bi trung lap voi hop dong dang co hieu luc hoac dang cho duyet cua nhan vien nay.");
            }

            // 2. Insert the contract
            boolean success = contractDAO.addContract(conn, contract);

            if (success) {
                // 3. Audit log: Creation (OldStatus = null, NewStatus = DRAFT)
                contractDAO.insertAuditLog(conn, contract.getContractId(), 
                    null, ContractStatus.DRAFT.name(), 
                    contract.getCreatedBy(), "Created contract");
                
                conn.commit();
                LOGGER.log(Level.INFO, "Contract created successfully for employee {0}", contract.getEmployeeId());
                return new ContractOperationResult(true, null, "Tao hop dong thanh cong.");
            } else {
                conn.rollback();
                return new ContractOperationResult(false,
                    ContractOperationResult.SQL_ERROR, "Khong the them hop dong vao database.");
            }
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            LOGGER.log(Level.SEVERE, "Database error during contract creation", e);
            return new ContractOperationResult(false,
                ContractOperationResult.SYSTEM_ERROR, "Loi he thong khi tao hop dong: " + e.getMessage());
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException ex) {}
        }
    }

    // =========================================================================
    // [HR FLOW] - MANUAL ACTIONS
    // =========================================================================

    /**
     * Transition a contract from DRAFT to ACTIVE or PENDING_ACTIVATION.
     * 
     * Business Logic:
     * 1. Only DRAFT contracts can be manually activated.
     * 2. If effectiveDate <= today -> ACTIVE.
     * 3. If effectiveDate > today -> PENDING_ACTIVATION.
     * 
     * @param contractId ID of the draft contract to activate.
     * @return ContractOperationResult indicating success or the specific error.
     */
    public ContractOperationResult activateContract(int contractId, int userId) {
        Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Retrieve latest state
            EmploymentContract contract = contractDAO.getContractById(conn, contractId);
            
            if (contract == null) {
                conn.rollback();
                return new ContractOperationResult(false, 
                    ContractOperationResult.SYSTEM_ERROR, "Contract not found.");
            }

            // 2. Guard: Must be DRAFT
            if (contract.getStatus() != ContractStatus.DRAFT) {
                conn.rollback();
                return new ContractOperationResult(false, 
                    ContractOperationResult.INVALID_STATUS, 
                    "Only draft contracts can be manually activated.");
            }

            // 3. Determine target status based on timeline
            Date today = Date.valueOf(LocalDate.now());
            ContractStatus targetStatus;
            
            if (contract.getEffectiveDate().after(today)) {
                targetStatus = ContractStatus.PENDING_ACTIVATION;
            } else {
                targetStatus = ContractStatus.ACTIVE;
            }

            // 4. Persist change
            boolean success = contractDAO.updateContractStatus(conn, contractId, targetStatus, null, null);
            
            if (success) {
                // 5. Audit log
                contractDAO.insertAuditLog(conn, contractId, 
                    ContractStatus.DRAFT.name(), targetStatus.name(), 
                    userId, "Manual activation by HR");
                
                conn.commit();
                LOGGER.log(Level.INFO, "HR Manual Activation: Contract {0} -> {1}", 
                    new Object[]{contractId, targetStatus});
                return new ContractOperationResult(true, null, "Contract activated successfully.");
            } else {
                conn.rollback();
                return new ContractOperationResult(false, 
                    ContractOperationResult.SQL_ERROR, "Failed to update contract status in database.");
            }

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            LOGGER.log(Level.SEVERE, "Database error during HR activation for contract " + contractId, e);
            return new ContractOperationResult(false, 
                ContractOperationResult.SQL_ERROR, "Database connection error: " + e.getMessage());
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException ex) {}
        }
    }

    // =========================================================================
    // [HR FLOW] - APPROVAL / REJECTION
    // =========================================================================

    /**
     * Approve a PENDING_APPROVAL contract, transitioning to PENDING_ACTIVATION.
     * Validates overlap before promoting. All operations in a single transaction.
     */
    public ContractOperationResult approveContract(int contractId, int userId) {
        Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);

            EmploymentContract contract = contractDAO.getContractById(conn, contractId);
            if (contract == null) {
                conn.rollback();
                return new ContractOperationResult(false, "NOT_FOUND", "Khong tim thay hop dong.");
            }

            if (!ContractStatus.PENDING_APPROVAL.name().equals(contract.getStatus())) {
                conn.rollback();
                return new ContractOperationResult(false, "INVALID_STATUS", "Hop dong khong o trang thai cho duyet.");
            }

            // Check overlap before promoting
            if (contractDAO.hasOverlappingContract(conn, contract.getEmployeeId(),
                    contract.getEffectiveDate(), contract.getEndDate(), contractId)) {
                conn.rollback();
                return new ContractOperationResult(false, ContractOperationResult.DATE_OVERLAP,
                    "Thoi gian hop dong bi trung lap voi mot hop dong dang co hieu luc.");
            }

            String oldStatus = contract.getStatus().name();
            String newStatus = ContractStatus.PENDING_ACTIVATION.name();
            contractDAO.updateContractStatus(conn, contractId, ContractStatus.PENDING_ACTIVATION, null, null);
            contractDAO.insertAuditLog(conn, contractId, oldStatus, newStatus, userId, "Phe duyet");

            conn.commit();
            return new ContractOperationResult(true, null, "Phe duyet thanh cong.");
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            LOGGER.log(Level.SEVERE, "Database error during contract approval", e);
            return new ContractOperationResult(false, "SYSTEM_ERROR",
                "Loi he thong khi phe duyet: " + e.getMessage());
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException ex) {}
        }
    }

    /**
     * Reject a PENDING_APPROVAL contract, transitioning to CANCELLED.
     * Includes audit log entry in the same transaction.
     */
    public ContractOperationResult rejectContract(int contractId, int userId, String reason) {
        Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);

            EmploymentContract contract = contractDAO.getContractById(conn, contractId);
            if (contract == null) {
                conn.rollback();
                return new ContractOperationResult(false, "NOT_FOUND", "Khong tim thay hop dong.");
            }

            String oldStatus = contract.getStatus().name();
            String newStatus = ContractStatus.CANCELLED.name();
            contractDAO.updateContractStatus(conn, contractId, ContractStatus.CANCELLED, null, reason);
            contractDAO.insertAuditLog(conn, contractId, oldStatus, newStatus, userId,
                "Tu choi: " + (reason != null ? reason : ""));

            conn.commit();
            return new ContractOperationResult(true, null, "Tu choi hop dong thanh cong.");
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            LOGGER.log(Level.SEVERE, "Database error during contract rejection", e);
            return new ContractOperationResult(false, "SYSTEM_ERROR",
                "Loi he thong khi tu choi: " + e.getMessage());
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException ex) {}
        }
    }

    // =========================================================================
    // [SCHEDULER FLOW] - AUTOMATED ACTIONS
    // =========================================================================

    /**
     * Transition a contract from PENDING_ACTIVATION to ACTIVE.
     * 
     * This method is intended for the Automated Scheduler and requires 
     * a shared connection to participate in a larger transaction.
     * 
     * @param contractId ID of the pending contract.
     * @param conn       An active, shared connection (caller manages commit/rollback).
     * @return ContractOperationResult indicating success or failure.
     */
    public ContractOperationResult activatePendingContract(int contractId, Connection conn) throws SQLException {
            // 1. Re-fetch latest state from DB to prevent race conditions
            EmploymentContract contract = contractDAO.getContractById(conn, contractId);
            
            if (contract == null) {
                return new ContractOperationResult(false, 
                    ContractOperationResult.SYSTEM_ERROR, "Contract not found during auto-activation.");
            }

            // 2. Guard: Must be PENDING_ACTIVATION
            if (contract.getStatus() != ContractStatus.PENDING_ACTIVATION) {
                return new ContractOperationResult(false, 
                    ContractOperationResult.INVALID_STATUS, 
                    "Contract is not in PENDING_ACTIVATION state.");
            }

            // 3. Timeline Check: effectiveDate must be <= today
            Date today = Date.valueOf(LocalDate.now());
            if (contract.getEffectiveDate().after(today)) {
                return new ContractOperationResult(false, 
                    ContractOperationResult.DATE_MISMATCH, 
                    "Effective date has not arrived yet.");
            }

            // 4. Transition to ACTIVE
            boolean success = contractDAO.updateContractStatus(conn, contractId, ContractStatus.ACTIVE, null, null);
            
            if (success) {
                // 5. Audit log (throws SQLException to trigger rollback in caller)
                contractDAO.insertAuditLog(conn, contractId, 
                    ContractStatus.PENDING_ACTIVATION.name(), ContractStatus.ACTIVE.name(), 
                    0, "System auto-activation");
                
                return new ContractOperationResult(true, null, "System auto-activation successful.");
            } else {
                return new ContractOperationResult(false, 
                    ContractOperationResult.SQL_ERROR, "Failed to execute auto-activation update.");
            }
    }

    /**
     * Process all pending activations and expirations for the day.
     * 
     * This is the entry point for the midnight batch cron job.
     * Implements per-row transaction isolation to ensure one failure 
     * does not crash the entire batch.
     */
    public void processDailyContractUpdates() {
        LOGGER.log(Level.INFO, "Starting Automated Daily Contract Updates Batch Process...");

        // PHASE 1: PENDING_ACTIVATION -> ACTIVE
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

        // PHASE 2: ACTIVE -> EXPIRED
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
                        0, "System auto-expiration");
                    
                    conn.commit();
                    LOGGER.log(Level.INFO, "Transaction committed: Contract {0} expired by system.", id);
                } else {
                    conn.rollback();
                    LOGGER.log(Level.WARNING, "Transaction rolled back: Contract {0} failed expiration.", id);
                }
            } catch (SQLException e) {
                if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
                LOGGER.log(Level.SEVERE, "System error during expiration batch for contract " + id, e);
            } finally {
                if (conn != null) try { conn.close(); } catch (SQLException ex) {}
            }
        }

        LOGGER.log(Level.INFO, "Automated Daily Contract Updates Batch Process Completed.");
    }
}
