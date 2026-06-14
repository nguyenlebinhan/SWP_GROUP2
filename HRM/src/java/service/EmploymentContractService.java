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
    public ContractOperationResult activateContract(int contractId) {
        // HR Flow: Each request manages its own isolated connection
        try (Connection conn = dbContext.getConnection()) {
            
            // 1. Retrieve latest state
            EmploymentContract contract = contractDAO.getContractById(conn, contractId);
            
            if (contract == null) {
                return new ContractOperationResult(false, 
                    ContractOperationResult.SYSTEM_ERROR, "Contract not found.");
            }

            // 2. Guard: Must be DRAFT
            if (contract.getStatus() != ContractStatus.DRAFT) {
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
                LOGGER.log(Level.INFO, "HR Manual Activation: Contract {0} -> {1}", 
                    new Object[]{contractId, targetStatus});
                return new ContractOperationResult(true, null, "Contract activated successfully.");
            } else {
                return new ContractOperationResult(false, 
                    ContractOperationResult.SQL_ERROR, "Failed to update contract status in database.");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during HR activation for contract " + contractId, e);
            return new ContractOperationResult(false, 
                ContractOperationResult.SQL_ERROR, "Database connection error: " + e.getMessage());
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
    public ContractOperationResult activatePendingContract(int contractId, Connection conn) {
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
            try (Connection conn = dbContext.getConnection()) {
                conn.setAutoCommit(false);
                
                boolean success = contractDAO.updateContractStatus(conn, id, ContractStatus.EXPIRED, null, null);
                
                if (success) {
                    conn.commit();
                    LOGGER.log(Level.INFO, "Transaction committed: Contract {0} expired by system.", id);
                } else {
                    conn.rollback();
                    LOGGER.log(Level.WARNING, "Transaction rolled back: Contract {0} failed expiration.", id);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "System error during expiration batch for contract " + id, e);
            }
        }

        LOGGER.log(Level.INFO, "Automated Daily Contract Updates Batch Process Completed.");
    }
}
