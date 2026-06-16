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
import model.ContractAuditLog;
import model.ContractOperationResult;
import model.ContractStatus;
import model.ContractType;
import model.EmploymentContract;

public class EmploymentContractDAO {
    private static final Logger LOGGER = Logger.getLogger(EmploymentContractDAO.class.getName());
    private final DBContext dbContext;

    public EmploymentContractDAO() {
        this.dbContext = new DBContext();
    }

    /**
     * Expose the DBContext for service layer construction.
     */
    public DBContext getDBContext() {
        return dbContext;
    }

    /**
     * Get a connection from the context (for backward compatibility).
     * New code should prefer injecting Connection from the caller.
     */
    private Connection getInternalConnection() throws SQLException {
        return dbContext.getConnection();
    }

    // =========================================================================
    // CRUD Methods
    // =========================================================================

    /**
     * Insert a new contract using an injected connection (transactional flow).
     * Returns the generated contractId, or -1 if failed.
     */
    public int addContract(Connection conn, EmploymentContract contract) throws SQLException {
        String SQL = "INSERT INTO Employment_Contracts "
                + "(contractCode, employeeId, contractType, signedDate, effectiveDate, endDate, "
                + "salary, status, note, previousContractId, terminationReason, createdBy) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, contract.getContractCode());
            ps.setInt(2, contract.getEmployeeId());
            ps.setString(3, contract.getContractType() != null ? contract.getContractType().name() : null);
            ps.setDate(4, contract.getSignedDate());
            ps.setDate(5, contract.getEffectiveDate());
            if (contract.getEndDate() == null) {
                ps.setNull(6, Types.DATE);
            } else {
                ps.setDate(6, contract.getEndDate());
            }
            ps.setBigDecimal(7, contract.getSalary());
            ps.setString(8, contract.getStatus() != null ? contract.getStatus().name() : null);
            ps.setString(9, contract.getNote());
            if (contract.getPreviousContractId() != null) {
                ps.setInt(10, contract.getPreviousContractId());
            } else {
                ps.setNull(10, Types.INTEGER);
            }
            ps.setString(11, contract.getTerminationReason());
            ps.setInt(12, contract.getCreatedBy());
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return -1;
        }
    }

    /**
     * Insert a new contract with its own connection (backward compatible).
     */
    public int addContract(EmploymentContract contract) {
        try (Connection conn = getInternalConnection()) {
            return addContract(conn, contract);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get connection for contract insert: " + contract.getContractCode(), e);
        }
        return -1;
    }

    // =========================================================================
    // Connection-Aware Query Methods (for Scheduler Transactional Flow)
    // =========================================================================

    /**
     * Get a contract by ID (backward compatible, creates own connection).
     */
    public EmploymentContract getContractById(int contractId) {
        try (Connection conn = getInternalConnection()) {
            return getContractById(conn, contractId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get connection for contractId: " + contractId, e);
        }
        return null;
    }

    /**
     * Get a contract by ID (using injected connection).
     * Used by scheduler flow for thread-safe re-fetching.
     */
    public EmploymentContract getContractById(Connection conn, int contractId) {
        String SQL = "SELECT contractId, contractCode, employeeId, contractType, signedDate, effectiveDate, "
                + "endDate, actualEndDate, salary, status, note, previousContractId, terminationReason, "
                + "rejectionReason, createdBy, createdAt, updatedAt "
                + "FROM Employment_Contracts WHERE contractId = ?";
        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapContract(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve employment contract by id: " + contractId, e);
        }
        return null;
    }

    /**
     * Get the latest contract by employeeId, ordered by effectiveDate DESC.
     * For backward compatibility.
     */
    public EmploymentContract getLatestContractByEmployeeId(int employeeId) {
        String SQL = "SELECT contractId, contractCode, employeeId, contractType, signedDate, effectiveDate, "
                + "endDate, actualEndDate, salary, status, note, previousContractId, terminationReason, "
                + "rejectionReason, createdBy, createdAt, updatedAt "
                + "FROM Employment_Contracts WHERE employeeId = ? "
                + "ORDER BY effectiveDate DESC, contractId DESC LIMIT 1";
        try (Connection conn = getInternalConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapContract(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve latest contract for employeeId: " + employeeId, e);
        }
        return null;
    }

    // =========================================================================
    // Backward Compatibility
    // =========================================================================

    /**
     * Check if an employee has an active contract.
     * @deprecated Use {@link #getActiveContract(int)} instead.
     */
    @Deprecated
    public boolean hasActiveContract(int employeeId) {
        return getActiveContract(employeeId) != null;
    }

    // =========================================================================
    // Query Methods
    // =========================================================================

    /**
     * Get the active contract for an employee (backward compatible).
     */
    public EmploymentContract getActiveContract(int employeeId) {
        try (Connection conn = getInternalConnection()) {
            return getActiveContract(conn, employeeId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get connection for employeeId: " + employeeId, e);
        }
        return null;
    }

    /**
     * Get the active contract for an employee (using injected connection).
     * Used by scheduler flow for thread-safe re-fetching.
     */
    public EmploymentContract getActiveContract(Connection conn, int employeeId) {
        String SQL = "SELECT contractId, contractCode, employeeId, contractType, signedDate, effectiveDate, "
                + "endDate, actualEndDate, salary, status, note, previousContractId, terminationReason, "
                + "rejectionReason, createdBy, createdAt, updatedAt "
                + "FROM Employment_Contracts WHERE employeeId = ? AND status = 'ACTIVE' "
                + "LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapContract(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve active contract for employeeId: " + employeeId, e);
        }
        return null;
    }

    /**
     * Get the current active contract, or the next upcoming contract if no active one exists.
     * Priority: ACTIVE (highest) -> PENDING_ACTIVATION (earliest effectiveDate).
     */
    public EmploymentContract getCurrentOrUpcomingContract(int employeeId) {
        String SQL = "SELECT contractId, contractCode, employeeId, contractType, signedDate, effectiveDate, "
                + "endDate, actualEndDate, salary, status, note, previousContractId, terminationReason, "
                + "rejectionReason, createdBy, createdAt, updatedAt "
                + "FROM Employment_Contracts WHERE employeeId = ? "
                + "AND status IN ('ACTIVE', 'PENDING_ACTIVATION') "
                + "ORDER BY CASE WHEN status = 'ACTIVE' THEN 0 ELSE 1 END, "
                + "effectiveDate ASC "
                + "LIMIT 1";
        try (Connection conn = getInternalConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapContract(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve current/upcoming contract for employeeId: " + employeeId, e);
        }
        return null;
    }

    /**
     * Get all contracts for an employee, ordered by effectiveDate ASC.
     */
    public List<EmploymentContract> getContractHistory(int employeeId) {
        List<EmploymentContract> contracts = new ArrayList<>();
        String SQL = "SELECT contractId, contractCode, employeeId, contractType, signedDate, effectiveDate, "
                + "endDate, actualEndDate, salary, status, note, previousContractId, terminationReason, "
                + "rejectionReason, createdBy, createdAt, updatedAt "
                + "FROM Employment_Contracts WHERE employeeId = ? "
                + "ORDER BY effectiveDate ASC, contractId ASC";
        try (Connection conn = getInternalConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contracts.add(mapContract(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve contract history for employeeId: " + employeeId, e);
        }
        return contracts;
    }

    /**
     * Get all future (PENDING_ACTIVATION) contracts for an employee,
     * ordered by effectiveDate ASC.
     */
    public List<EmploymentContract> getFutureContracts(int employeeId) {
        List<EmploymentContract> contracts = new ArrayList<>();
        String SQL = "SELECT contractId, contractCode, employeeId, contractType, signedDate, effectiveDate, "
                + "endDate, actualEndDate, salary, status, note, previousContractId, terminationReason, "
                + "createdBy, createdAt, updatedAt "
                + "FROM Employment_Contracts WHERE employeeId = ? AND status = 'PENDING_ACTIVATION' "
                + "ORDER BY effectiveDate ASC";
        try (Connection conn = getInternalConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contracts.add(mapContract(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve future contracts for employeeId: " + employeeId, e);
        }
        return contracts;
    }

    // =========================================================================
    // Validation Methods
    // =========================================================================

    /**
     * Check if a new contract period would overlap with existing ACTIVE or
     * PENDING_ACTIVATION contracts.
     */
    /**
     * Check if a new contract overlaps with existing active/pending contracts using pure SQL.
     * Overlap formula: (ExistingStart <= NewEnd OR NewEnd IS NULL) AND (ExistingEnd >= NewStart OR ExistingEnd IS NULL)
     * Only checks against statuses: ACTIVE, PENDING_ACTIVATION, PENDING_APPROVAL.
     *
     * @param conn            Database connection (caller manages transaction)
     * @param employeeId      Employee to check
     * @param newStart        New contract effective date
     * @param newEnd          New contract end date (null = indefinite)
     * @param excludeContractId Contract ID to exclude from check (for updates), or null
     * @return true if overlap exists
     * @throws SQLException   Database error
     */
    public boolean hasOverlappingContract(Connection conn, int employeeId, java.sql.Date newStart,
                                          java.sql.Date newEnd, Integer excludeContractId) throws SQLException {
        String SQL = "SELECT 1 FROM Employment_Contracts "
                + "WHERE employeeId = ? "
                + "AND status IN ('ACTIVE', 'PENDING_ACTIVATION', 'PENDING_APPROVAL') "
                + "AND (effectiveDate <= COALESCE(?, '9999-12-31')) "
                + "AND (COALESCE(endDate, '9999-12-31') >= ?)"
                + (excludeContractId != null ? " AND contractId != ?" : "");

        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, newEnd);
            ps.setDate(3, newStart);
            if (excludeContractId != null) {
                ps.setInt(4, excludeContractId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * @deprecated Use {@link #hasOverlappingContract(Connection, int, java.sql.Date, java.sql.Date, Integer)} instead.
     * This method loads all contracts into memory and checks in Java (inefficient).
     */
    @Deprecated
    public boolean checkOverlappingPeriods(int employeeId, java.sql.Date effectiveDate,
                                           java.sql.Date endDate, Integer excludeContractId) {
        try (Connection conn = getInternalConnection()) {
            return hasOverlappingContract(conn, employeeId, effectiveDate, endDate, excludeContractId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot check overlapping contracts for employeeId: " + employeeId, e);
            return false;
        }
    }

    // =========================================================================
    // Status Update Methods
    // =========================================================================

    /**
     * Update contract status (backward compatible, creates own connection).
     */
    public boolean updateContractStatus(int contractId, ContractStatus newStatus,
                                        java.sql.Date actualEndDate, String terminationReason) {
        try (Connection conn = getInternalConnection()) {
            return updateContractStatus(conn, contractId, newStatus, actualEndDate, terminationReason);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get connection for contractId: " + contractId, e);
        }
        return false;
    }

    /**
     * Update contract status and termination details (using injected connection).
     * Used by scheduler flow for transactional updates.
     */
    public boolean updateContractStatus(Connection conn, int contractId, ContractStatus newStatus,
                                        java.sql.Date actualEndDate, String terminationReason) {
        String SQL = "UPDATE Employment_Contracts "
                + "SET status = ?, actualEndDate = ?, terminationReason = ?, "
                + "updatedAt = CURRENT_TIMESTAMP "
                + "WHERE contractId = ?";
        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, newStatus.name());
            if (actualEndDate != null) {
                ps.setDate(2, actualEndDate);
            } else {
                ps.setNull(2, Types.DATE);
            }
            ps.setString(3, terminationReason);
            ps.setInt(4, contractId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot update contract status for contractId: " + contractId, e);
        }
        return false;
    }

    // =========================================================================
    // Scheduler Methods
    // =========================================================================

    /**
     * Find contracts ready to activate (PENDING_ACTIVATION where effectiveDate <= CURRENT_DATE).
     */
    public List<EmploymentContract> getContractsReadyForActivation() {
        List<EmploymentContract> contracts = new ArrayList<>();
        String SQL = "SELECT contractId, contractCode, employeeId, contractType, signedDate, effectiveDate, "
                + "endDate, actualEndDate, salary, status, note, previousContractId, terminationReason, "
                + "rejectionReason, createdBy, createdAt, updatedAt "
                + "FROM Employment_Contracts "
                + "WHERE status = 'PENDING_ACTIVATION' AND effectiveDate <= CURRENT_DATE";
        try (Connection conn = getInternalConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contracts.add(mapContract(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve contracts ready for activation", e);
        }
        return contracts;
    }

    // =========================================================================
    // Phase 7: Advanced Workflow & Security Methods
    // =========================================================================

    /**
     * Get all contracts for a specific employee (self-service view).
     * Strictly isolated - only returns contracts belonging to the given employeeId.
     * Used by EmployeeController for contract-history and contract-current views.
     */
    public List<EmploymentContract> getContractsByEmployeeId(int empId) {
        List<EmploymentContract> contracts = new ArrayList<>();
        String SQL = "SELECT contractId, contractCode, employeeId, contractType, signedDate, effectiveDate, "
                + "endDate, actualEndDate, salary, status, note, previousContractId, terminationReason, "
                + "rejectionReason, createdBy, createdAt, updatedAt "
                + "FROM Employment_Contracts WHERE employeeId = ? "
                + "ORDER BY effectiveDate DESC, contractId DESC";
        try (Connection conn = getInternalConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contracts.add(mapContract(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve contracts for employeeId: " + empId, e);
        }
        return contracts;
    }

    /**
     * Get all contracts in the system (HR Manager view).
     * Returns all contracts regardless of employee - for administrative overview.
     */
    public List<EmploymentContract> getAllContracts() {
        List<EmploymentContract> contracts = new ArrayList<>();
        String SQL = "SELECT contractId, contractCode, employeeId, contractType, signedDate, effectiveDate, "
                + "endDate, actualEndDate, salary, status, note, previousContractId, terminationReason, "
                + "rejectionReason, createdBy, createdAt, updatedAt "
                + "FROM Employment_Contracts "
                + "ORDER BY createdAt DESC, contractId DESC";
        try (Connection conn = getInternalConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contracts.add(mapContract(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve all contracts", e);
        }
        return contracts;
    }

    /**
     * Approve a contract: change status from PENDING_APPROVAL to PENDING_ACTIVATION.
     * Creates own connection - used for HR manual approval flow.
     */
    public ContractOperationResult approveContract(int contractId) {
        try (Connection conn = getInternalConnection()) {
            return approveContract(conn, contractId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get connection for contract approval: " + contractId, e);
            return new ContractOperationResult(false, ContractOperationResult.SYSTEM_ERROR, "Lỗi hệ thống khi kết nối cơ sở dữ liệu");
        }
    }

    /**
     * Approve a contract using injected connection (transactional flow).
     */
    public ContractOperationResult approveContract(Connection conn, int contractId) {
        String SQL = "UPDATE Employment_Contracts "
                + "SET status = 'PENDING_ACTIVATION', updatedAt = CURRENT_TIMESTAMP "
                + "WHERE contractId = ? AND status = 'PENDING_APPROVAL'";
        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, contractId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                return new ContractOperationResult(true, null, "Duyệt hợp đồng thành công");
            } else {
                return new ContractOperationResult(false, ContractOperationResult.INVALID_STATUS, "Hợp đồng không ở trạng thái 'Chờ duyệt'");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot approve contract: " + contractId, e);
            return new ContractOperationResult(false, ContractOperationResult.SQL_ERROR, "Lỗi cơ sở dữ liệu");
        }
    }

    /**
     * Reject a contract: change status from PENDING_APPROVAL to CANCELLED with rejection reason.
     * Creates own connection - used for HR manual rejection flow.
     */
    public ContractOperationResult rejectContract(int contractId, String reason) {
        try (Connection conn = getInternalConnection()) {
            return rejectContract(conn, contractId, reason);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get connection for contract rejection: " + contractId, e);
            return new ContractOperationResult(false, ContractOperationResult.SYSTEM_ERROR, "Lỗi hệ thống khi kết nối cơ sở dữ liệu");
        }
    }

    /**
     * Reject a contract using injected connection (transactional flow).
     */
    public ContractOperationResult rejectContract(Connection conn, int contractId, String reason) {
        String SQL = "UPDATE Employment_Contracts "
                + "SET status = 'CANCELLED', rejectionReason = ?, updatedAt = CURRENT_TIMESTAMP "
                + "WHERE contractId = ? AND status = 'PENDING_APPROVAL'";
        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, reason);
            ps.setInt(2, contractId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                return new ContractOperationResult(true, null, "Từ chối hợp đồng thành công");
            } else {
                return new ContractOperationResult(false, ContractOperationResult.INVALID_STATUS, "Hợp đồng không ở trạng thái 'Chờ duyệt'");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot reject contract: " + contractId, e);
            return new ContractOperationResult(false, ContractOperationResult.SQL_ERROR, "Lỗi cơ sở dữ liệu");
        }
    }

    /**
     * Find contracts ready to expire (ACTIVE where endDate < CURRENT_DATE).
     * INDEFINITE contracts (endDate IS NULL) are ignored.
     */
    public List<EmploymentContract> getContractsReadyForExpiration() {
        List<EmploymentContract> contracts = new ArrayList<>();
        String SQL = "SELECT contractId, contractCode, employeeId, contractType, signedDate, effectiveDate, "
                + "endDate, actualEndDate, salary, status, note, previousContractId, terminationReason, "
                + "rejectionReason, createdBy, createdAt, updatedAt "
                + "FROM Employment_Contracts "
                + "WHERE status = 'ACTIVE' AND endDate IS NOT NULL AND endDate < CURRENT_DATE";
        try (Connection conn = getInternalConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contracts.add(mapContract(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve contracts ready for expiration", e);
        }
        return contracts;
    }

    // =========================================================================
    // Audit Log Methods
    // =========================================================================

    /**
     * Insert an audit log entry for a contract state transition.
     * Shares the connection with the status update for transactional integrity.
     */
    public void insertAuditLog(Connection conn, int contractId, String oldStatus,
                               String newStatus, int changedBy, String actionReason) throws SQLException {
        String SQL = "INSERT INTO Contract_Audit_Log (ContractId, OldStatus, NewStatus, ChangedBy, ChangeDate, ActionReason) "
                   + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";
        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, contractId);
            ps.setString(2, oldStatus);
            ps.setString(3, newStatus);
            ps.setInt(4, changedBy);
            ps.setString(5, actionReason);
            ps.executeUpdate();
        }
    }

    /**
     * Advanced search for contract audit history with dynamic SQL construction.
     * Enforces strict employee isolation for non-HR users.
     *
     * @param targetEmpId    Filter by employee ID (optional)
     * @param nameKeyword    Filter by employee full name LIKE (optional)
     * @param departmentId   Filter by department ID (optional)
     * @param loggedInEmpId  Logged-in employee's ID (for isolation enforcement)
     * @param isHrManager    true = full access, false = own contracts only
     * @return List of audit log entries
     */
    public List<ContractAuditLog> searchContractHistory(Integer targetEmpId, String nameKeyword,
                                                        Integer departmentId, int loggedInEmpId, boolean isHrManager) {
        List<ContractAuditLog> logs = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT la.LogId, la.ContractId, la.OldStatus, la.NewStatus, "
            + "la.ChangedBy, la.ChangeDate, la.ActionReason, "
            + "u.userName AS changedByName, ec.employeeId "
            + "FROM Contract_Audit_Log la "
            + "JOIN Employment_Contracts ec ON la.ContractId = ec.contractId "
            + "JOIN Users u ON la.ChangedBy = u.userId "
            + "JOIN Employees e ON ec.employeeId = e.employeeId "
            + "WHERE 1=1 "
        );

        if (targetEmpId != null) {
            sql.append("AND ec.employeeId = ? ");
        }
        if (nameKeyword != null && !nameKeyword.isBlank()) {
            sql.append("AND e.fullName LIKE ? ");
        }
        if (departmentId != null) {
            sql.append("AND e.departmentId = ? ");
        }

        // CRITICAL: Data Isolation for non-HR users
        if (!isHrManager) {
            sql.append("AND ec.employeeId = ? ");
        }

        sql.append("ORDER BY la.ChangeDate DESC");

        try (Connection conn = getInternalConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (targetEmpId != null) ps.setInt(idx++, targetEmpId);
            if (nameKeyword != null && !nameKeyword.isBlank()) ps.setString(idx++, "%" + nameKeyword + "%");
            if (departmentId != null) ps.setInt(idx++, departmentId);
            if (!isHrManager) ps.setInt(idx++, loggedInEmpId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ContractAuditLog log = new ContractAuditLog();
                    log.setLogId(rs.getInt("LogId"));
                    log.setContractId(rs.getInt("ContractId"));
                    log.setOldStatus(rs.getString("OldStatus"));
                    log.setNewStatus(rs.getString("NewStatus"));
                    log.setChangedBy(rs.getInt("ChangedBy"));
                    log.setChangedByName(rs.getString("changedByName"));
                    log.setChangeDate(rs.getTimestamp("ChangeDate"));
                    log.setActionReason(rs.getString("ActionReason"));
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching contract audit history", e);
        }
        return logs;
    }

    // =========================================================================
    // ResultSet Mapping
    // =========================================================================

    private EmploymentContract mapContract(ResultSet rs) throws SQLException {
        EmploymentContract contract = new EmploymentContract();
        contract.setContractId(rs.getInt("contractId"));
        contract.setContractCode(rs.getString("contractCode"));
        contract.setEmployeeId(rs.getInt("employeeId"));

        String typeStr = rs.getString("contractType");
        if (typeStr != null) {
            try {
                contract.setContractType(ContractType.valueOf(typeStr));
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.SEVERE, "Invalid contractType in DB: " + typeStr, e);
            }
        }

        contract.setSignedDate(rs.getDate("signedDate"));
        contract.setEffectiveDate(rs.getDate("effectiveDate"));
        contract.setEndDate(rs.getDate("endDate"));
        contract.setActualEndDate(rs.getDate("actualEndDate"));
        contract.setSalary(rs.getBigDecimal("salary"));

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                contract.setStatus(ContractStatus.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.SEVERE, "Invalid status in DB: " + statusStr, e);
            }
        }

        contract.setNote(rs.getNString("note"));

        int prevId = rs.getInt("previousContractId");
        if (!rs.wasNull()) {
            contract.setPreviousContractId(prevId);
        }

        contract.setTerminationReason(rs.getString("terminationReason"));
        contract.setRejectionReason(rs.getString("rejectionReason"));
        contract.setCreatedBy(rs.getInt("createdBy"));
        contract.setCreatedAt(rs.getDate("createdAt"));
        contract.setUpdatedAt(rs.getDate("updatedAt"));
        return contract;
    }

    public List<EmploymentContract> getPendingContracts() throws SQLException {
        List<EmploymentContract> contracts = new ArrayList<>();
        String SQL = "SELECT ec.*, e.fullName, e.employeeCode "
                   + "FROM Employment_Contracts ec "
                   + "JOIN Employees e ON ec.employeeId = e.employeeId "
                   + "WHERE ec.status = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, ContractStatus.PENDING_APPROVAL.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contracts.add(mapContract(rs));
                }
            }
        }
        return contracts;
    }
}

