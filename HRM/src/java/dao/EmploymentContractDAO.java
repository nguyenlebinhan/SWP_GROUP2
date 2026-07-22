package dao;

import dal.DBContext;
import java.sql.Connection;
import java.sql.Date;
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
import enums.ContractStatus;
import enums.ContractType;
import model.EmploymentContract;

public class EmploymentContractDAO {

    private static final Logger LOGGER = Logger.getLogger(EmploymentContractDAO.class.getName());
    private static final String BASE_COLUMNS
            = "contractId, contractCode, employeeId, contractType, signedDate, "
            + "effectiveDate, endDate, actualEndDate, salary, departmentName, positionName, status, note, "
            + "previousContractId, terminationReason, rejectionReason, "
            + "createdBy, createdAt, updatedAt";
    private final DBContext dbContext;

    public EmploymentContractDAO() {
        this.dbContext = new DBContext();
    }

    public DBContext getDBContext() {
        return dbContext;
    }

    private Connection getInternalConnection() throws SQLException {
        return dbContext.getConnection();
    }

    public int addContract(Connection conn, EmploymentContract contract) throws SQLException {
        String SQL = "INSERT INTO Employment_Contracts "
                + "(contractCode, employeeId, contractType, signedDate, effectiveDate, endDate, "
                + "salary, departmentName, positionName, status, note, previousContractId, terminationReason, createdBy) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            ps.setString(8, contract.getDepartmentName());
            ps.setString(9, contract.getPositionName());
            ps.setString(10, contract.getStatus() != null ? contract.getStatus().name() : null);
            ps.setString(11, contract.getNote());
            if (contract.getPreviousContractId() != null) {
                ps.setInt(12, contract.getPreviousContractId());
            } else {
                ps.setNull(12, Types.INTEGER);
            }
            ps.setString(13, contract.getTerminationReason());
            ps.setInt(14, contract.getCreatedBy());

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

    public EmploymentContract getContractById(int contractId) {
        try (Connection conn = getInternalConnection()) {
            return getContractById(conn, contractId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get connection for contractId: " + contractId, e);
        }
        return null;
    }

    public EmploymentContract getContractById(Connection conn, int contractId) {
        String SQL = "SELECT ec.*, e.employeeCode, u.fullName "
                + "FROM Employment_Contracts ec "
                + "LEFT JOIN Employees e ON ec.employeeId = e.employeeId "
                + "LEFT JOIN Users u ON e.userId = u.userId "
                + "WHERE ec.contractId = ?";
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

    public EmploymentContract getLatestContractByEmployeeId(int employeeId) {
        String SQL = "SELECT " + BASE_COLUMNS + " FROM Employment_Contracts WHERE employeeId = ? "
                + "ORDER BY effectiveDate DESC, contractId DESC LIMIT 1";
        try (Connection conn = getInternalConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
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

    public EmploymentContract getActiveContract(int employeeId) {
        try (Connection conn = getInternalConnection()) {
            return getActiveContract(conn, employeeId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get connection for employeeId: " + employeeId, e);
        }
        return null;
    }

    public EmploymentContract getActiveContract(Connection conn, int employeeId) {
        String SQL = "SELECT " + BASE_COLUMNS + " FROM Employment_Contracts WHERE employeeId = ? AND status = 'ACTIVE' "
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

    public EmploymentContract getActiveOrPendingContract(int employeeId) {
        try (Connection conn = getInternalConnection()) {
            return getActiveOrPendingContract(conn, employeeId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve current/upcoming contract for employeeId: " + employeeId, e);
        }
        return null;
    }

    public EmploymentContract getActiveOrPendingContract(Connection conn, int employeeId) throws SQLException {
        String SQL = "SELECT " + BASE_COLUMNS + " FROM Employment_Contracts WHERE employeeId = ? "
                + "AND status IN ('ACTIVE', 'PENDING_ACTIVATION') "
                + "ORDER BY CASE WHEN status = 'ACTIVE' THEN 0 ELSE 1 END, "
                + "effectiveDate ASC "
                + "LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapContract(rs);
                }
            }
        }

        return null;
    }

    public List<EmploymentContract> getContractHistory(int employeeId) {
        List<EmploymentContract> contracts = new ArrayList<>();
        String SQL = "SELECT " + BASE_COLUMNS + " FROM Employment_Contracts WHERE employeeId = ? "
                + "ORDER BY effectiveDate ASC, contractId ASC";
        try (Connection conn = getInternalConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
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

    public List<EmploymentContract> getFutureContracts(int employeeId) {
        List<EmploymentContract> contracts = new ArrayList<>();
        String SQL = "SELECT " + BASE_COLUMNS + " FROM Employment_Contracts WHERE employeeId = ? AND status = 'PENDING_ACTIVATION' "
                + "ORDER BY effectiveDate ASC";
        try (Connection conn = getInternalConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
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

    public boolean updateContractStatus(int contractId, ContractStatus newStatus,
            java.sql.Date actualEndDate, String terminationReason) {
        try (Connection conn = getInternalConnection()) {
            return updateContractStatus(conn, contractId, newStatus, actualEndDate, terminationReason);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get connection for contractId: " + contractId, e);
        }
        return false;
    }

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

    public boolean updateContractStatus(Connection conn, int contractId, ContractStatus newStatus,
            java.sql.Date actualEndDate, String terminationReason,
            java.sql.Date signedDate) {
        String SQL = "UPDATE Employment_Contracts "
                + "SET status = ?, actualEndDate = ?, terminationReason = ?, "
                + "signedDate = ?, updatedAt = CURRENT_TIMESTAMP "
                + "WHERE contractId = ?";
        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, newStatus.name());
            if (actualEndDate != null) {
                ps.setDate(2, actualEndDate);
            } else {
                ps.setNull(2, Types.DATE);
            }
            ps.setString(3, terminationReason);
            if (signedDate != null) {
                ps.setDate(4, signedDate);
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setInt(5, contractId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot update contract status (with signedDate) for contractId: " + contractId, e);
        }
        return false;
    }

    public List<EmploymentContract> getContractsReadyForActivation() {
        List<EmploymentContract> contracts = new ArrayList<>();
        String SQL = "SELECT " + BASE_COLUMNS + " FROM Employment_Contracts "
                + "WHERE status = 'PENDING_ACTIVATION' AND effectiveDate <= CURRENT_DATE";
        try (Connection conn = getInternalConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
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

    public List<EmploymentContract> getContractsByEmployeeId(int empId) {
        List<EmploymentContract> contracts = new ArrayList<>();
        String SQL = "SELECT " + BASE_COLUMNS + " FROM Employment_Contracts WHERE employeeId = ? "
                + "ORDER BY effectiveDate DESC, contractId DESC";
        try (Connection conn = getInternalConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
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

    public List<EmploymentContract> getAllContracts() {
        List<EmploymentContract> contracts = new ArrayList<>();
        String SQL = "SELECT " + BASE_COLUMNS + " FROM Employment_Contracts "
                + "ORDER BY createdAt DESC, contractId DESC";
        try (Connection conn = getInternalConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
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

    public boolean updateContractStatusWithoutEndDate(Connection conn, int contractId, ContractStatus newStatus) throws SQLException {
        String SQL = "UPDATE Employment_Contracts SET status = ?, updatedAt = CURRENT_TIMESTAMP WHERE contractId = ?";
        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, newStatus.name());
            ps.setInt(2, contractId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<EmploymentContract> getContractsReadyForExpiration() {
        List<EmploymentContract> contracts = new ArrayList<>();
        String SQL = "SELECT " + BASE_COLUMNS + " FROM Employment_Contracts "
                + "WHERE status = 'ACTIVE' AND endDate IS NOT NULL AND endDate < CURRENT_DATE";
        try (Connection conn = getInternalConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
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

    public List<EmploymentContract> getContractsExpiringSoon(int days) {
        List<EmploymentContract> contracts = new ArrayList<>();
        String SQL = "SELECT ec.*, e.employeeCode, u.fullName, d.departmentName, p.positionName "
                + "FROM Employment_Contracts ec "
                + "JOIN Employees e ON ec.employeeId = e.employeeId "
                + "JOIN Users u ON u.userId = e.userId "
                + "LEFT JOIN Departments d ON e.departmentId = d.departmentId "
                + "LEFT JOIN Positions p ON e.positionId = p.positionId "
                + "WHERE ec.status = 'ACTIVE' "
                + "AND ec.endDate IS NOT NULL "
                + "AND ec.endDate = DATE_ADD(CURRENT_DATE, INTERVAL ? DAY) "
                + "ORDER BY ec.endDate ASC";
        try (Connection conn = getInternalConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EmploymentContract contract = mapContract(rs);
                    try {
                        contract.setDepartmentName(rs.getString("departmentName"));
                    } catch (SQLException ignored) {
                    }
                    try {
                        contract.setPositionName(rs.getString("positionName"));
                    } catch (SQLException ignored) {
                    }
                    contracts.add(contract);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve contracts expiring in " + days + " days", e);
        }
        return contracts;
    }

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

    public List<ContractAuditLog> searchContractHistory(Integer targetEmpId, String nameKeyword,
            Integer departmentId, int loggedInEmpId, boolean isHrStaff) {
        List<ContractAuditLog> logs = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT la.LogId, la.ContractId, la.OldStatus, la.NewStatus, "
                + "la.ChangedBy, la.ChangeDate, la.ActionReason, "
                + "u.userName AS changedByName, ec.employeeId, e.fullName AS employeeName "
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
        if (!isHrStaff) {
            sql.append("AND ec.employeeId = ? ");
        }

        sql.append("ORDER BY la.ChangeDate DESC");

        try (Connection conn = getInternalConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (targetEmpId != null) {
                ps.setInt(idx++, targetEmpId);
            }
            if (nameKeyword != null && !nameKeyword.isBlank()) {
                ps.setString(idx++, "%" + nameKeyword + "%");
            }
            if (departmentId != null) {
                ps.setInt(idx++, departmentId);
            }
            if (!isHrStaff) {
                ps.setInt(idx++, loggedInEmpId);
            }

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
                    log.setEmployeeId(rs.getInt("employeeId"));
                    log.setEmployeeName(rs.getString("employeeName"));
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching contract audit history", e);
        }
        return logs;
    }

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
        try {
            contract.setEmployeeFullName(rs.getString("fullName"));
        } catch (SQLException ignored) {
        }
        try {
            contract.setEmployeeCode(rs.getString("employeeCode"));
        } catch (SQLException ignored) {
        }
        try {
            contract.setDepartmentName(rs.getString("departmentName"));
        } catch (SQLException ignored) {
        }
        try {
            contract.setPositionName(rs.getString("positionName"));
        } catch (SQLException ignored) {
        }
        contract.setCreatedBy(rs.getInt("createdBy"));
        try {
            contract.setCreatedByName(rs.getString("createdByName"));
        } catch (SQLException ignored) {
        }
        contract.setCreatedAt(rs.getDate("createdAt"));
        contract.setUpdatedAt(rs.getDate("updatedAt"));
        return contract;
    }

    public List<EmploymentContract> getTerminableContracts() {
        List<EmploymentContract> contracts = new ArrayList<>();
        String SQL = "SELECT ec.*, u.fullName, e.employeeCode, d.departmentName, p.positionName "
                + "FROM Employment_Contracts ec "
                + "JOIN Employees e ON ec.employeeId = e.employeeId "
                + "JOIN Users u ON u.userId = e.userId "
                + "LEFT JOIN Departments d ON e.departmentId = d.departmentId "
                + "LEFT JOIN Positions p ON e.positionId = p.positionId "
                + "WHERE ec.status IN (?, ?) "
                + "ORDER BY ec.updatedAt DESC, ec.contractId DESC";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, ContractStatus.ACTIVE.name());
            ps.setString(2, ContractStatus.PENDING_ACTIVATION.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EmploymentContract contract = mapContract(rs);
                    try {
                        contract.setDepartmentName(rs.getString("departmentName"));
                    } catch (SQLException ignored) {
                    }
                    try {
                        contract.setPositionName(rs.getString("positionName"));
                    } catch (SQLException ignored) {
                    }
                    contracts.add(contract);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve terminable contracts", e);
        }
        return contracts;
    }

    public boolean updateRejectionReason(Connection conn, int contractId, String reason) throws SQLException {
        String SQL = "UPDATE Employment_Contracts SET rejectionReason = ?, updatedAt = CURRENT_TIMESTAMP WHERE contractId = ?";
        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, reason);
            ps.setInt(2, contractId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean existsByContractCode(String contractCode, Integer excludeContractId) {
        String SQL = "SELECT 1 FROM Employment_Contracts WHERE contractCode = ?";
        if (excludeContractId != null) {
            SQL += " AND contractId != ?";
        }
        SQL += " LIMIT 1";
        try (Connection conn = getInternalConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, contractCode);
            if (excludeContractId != null) {
                ps.setInt(2, excludeContractId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking duplicate contractCode: " + contractCode, e);
        }
        return false;
    }

    public List<EmploymentContract> getPendingContracts() throws SQLException {
        List<EmploymentContract> contracts = new ArrayList<>();
        String SQL = "SELECT ec.*, u.fullName, e.employeeCode "
                + "FROM Employment_Contracts ec "
                + "JOIN Employees e ON ec.employeeId = e.employeeId "
                + "JOIN Users u ON u.userId = e.userId "
                + "WHERE ec.status = ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
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
