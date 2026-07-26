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
            + "effectiveDate, endDate, actualEndDate, salary, departmentName, positionName, "
            + "contractFilePath, contractFileName, uploadedAt, uploadedBy, "
            + "durationValue, durationUnit, "
            + "status, note, "
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
                + "salary, departmentName, positionName, "
                + "contractFilePath, contractFileName, uploadedAt, uploadedBy, "
                + "durationValue, durationUnit, "
                + "status, note, previousContractId, terminationReason, createdBy) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            ps.setString(10, contract.getContractFilePath());
            ps.setString(11, contract.getContractFileName());
            ps.setObject(12, contract.getUploadedAt() != null ? new java.sql.Timestamp(contract.getUploadedAt().getTime()) : null);
            ps.setObject(13, contract.getUploadedBy());
            ps.setObject(14, contract.getDurationValue());
            ps.setString(15, contract.getDurationUnit());
            ps.setString(16, contract.getStatus() != null ? contract.getStatus().name() : null);
            ps.setString(17, contract.getNote());
            if (contract.getPreviousContractId() != null) {
                ps.setInt(18, contract.getPreviousContractId());
            } else {
                ps.setNull(18, Types.INTEGER);
            }
            ps.setString(19, contract.getTerminationReason());
            ps.setInt(20, contract.getCreatedBy());

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
        insertAuditLog(conn, contractId, oldStatus, newStatus, changedBy, actionReason, null, null, null);
    }

    public void insertAuditLog(Connection conn, int contractId, String oldStatus,
            String newStatus, int changedBy, String actionReason,
            String fieldName, String oldValue, String newValue) throws SQLException {
        String SQL = "INSERT INTO Contract_Audit_Log (ContractId, OldStatus, NewStatus, ChangedBy, ChangeDate, ActionReason, FieldName, OldValue, NewValue) "
                + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, contractId);
            ps.setString(2, oldStatus);
            ps.setString(3, newStatus);
            ps.setInt(4, changedBy);
            ps.setString(5, actionReason);
            ps.setString(6, fieldName);
            ps.setString(7, oldValue);
            ps.setString(8, newValue);
            ps.executeUpdate();
        }
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
        contract.setContractFilePath(rs.getString("contractFilePath"));
        contract.setContractFileName(rs.getString("contractFileName"));
        contract.setUploadedAt(rs.getDate("uploadedAt"));
        contract.setUploadedBy(rs.getObject("uploadedBy", Integer.class));
        contract.setDurationValue(rs.getObject("durationValue", Integer.class));
        contract.setDurationUnit(rs.getString("durationUnit"));
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

    public boolean updateContract(Connection conn, EmploymentContract contract) throws SQLException {
        String SQL = "UPDATE Employment_Contracts SET "
                + "contractCode = ?, "
                + "employeeId = ?, "
                + "contractType = ?, "
                + "signedDate = ?, "
                + "effectiveDate = ?, "
                + "endDate = ?, "
                + "salary = ?, "
                + "departmentName = ?, "
                + "positionName = ?, "
                + "contractFilePath = ?, "
                + "contractFileName = ?, "
                + "uploadedAt = ?, "
                + "uploadedBy = ?, "
                + "durationValue = ?, "
                + "durationUnit = ?, "
                + "status = ?, "
                + "note = ?, "
                + "previousContractId = ?, "
                + "terminationReason = ?, "
                + "updatedAt = CURRENT_TIMESTAMP "
                + "WHERE contractId = ?";

        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, contract.getContractCode());
            ps.setInt(2, contract.getEmployeeId());
            ps.setString(3, contract.getContractType() != null ? contract.getContractType().name() : null);
            ps.setDate(4, contract.getSignedDate());
            ps.setDate(5, contract.getEffectiveDate());
            ps.setDate(6, contract.getEndDate());
            ps.setBigDecimal(7, contract.getSalary());
            ps.setString(8, contract.getDepartmentName());
            ps.setString(9, contract.getPositionName());
            ps.setString(10, contract.getContractFilePath());
            ps.setString(11, contract.getContractFileName());
            ps.setObject(12, contract.getUploadedAt() != null ? new java.sql.Timestamp(contract.getUploadedAt().getTime()) : null);
            ps.setObject(13, contract.getUploadedBy());
            ps.setObject(14, contract.getDurationValue());
            ps.setString(15, contract.getDurationUnit());
            ps.setString(16, contract.getStatus() != null ? contract.getStatus().name() : null);
            ps.setString(17, contract.getNote());
            if (contract.getPreviousContractId() != null) {
                ps.setInt(18, contract.getPreviousContractId());
            } else {
                ps.setNull(18, Types.INTEGER);
            }
            ps.setString(19, contract.getTerminationReason());
            ps.setInt(20, contract.getContractId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateContractWithAudit(Connection conn, EmploymentContract oldContract,
            EmploymentContract newContract, int userId, String reason) throws SQLException {

        String SQL = "UPDATE Employment_Contracts SET "
                + "contractCode = ?, "
                + "contractType = ?, "
                + "signedDate = ?, "
                + "effectiveDate = ?, "
                + "endDate = ?, "
                + "salary = ?, "
                + "departmentName = ?, "
                + "positionName = ?, "
                + "contractFilePath = ?, "
                + "contractFileName = ?, "
                + "uploadedAt = ?, "
                + "uploadedBy = ?, "
                + "durationValue = ?, "
                + "durationUnit = ?, "
                + "status = ?, "
                + "note = ?, "
                + "previousContractId = ?, "
                + "terminationReason = ?, "
                + "updatedAt = CURRENT_TIMESTAMP "
                + "WHERE contractId = ?";

        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, newContract.getContractCode());
            ps.setString(2, newContract.getContractType() != null ? newContract.getContractType().name() : null);
            ps.setDate(3, newContract.getSignedDate());  // signedDate = effectiveDate (service sẽ enforce)
            ps.setDate(4, newContract.getEffectiveDate());
            ps.setDate(5, newContract.getEndDate());
            ps.setBigDecimal(6, newContract.getSalary());
            ps.setString(7, newContract.getDepartmentName());
            ps.setString(8, newContract.getPositionName());
            ps.setString(9, newContract.getContractFilePath());
            ps.setString(10, newContract.getContractFileName());
            ps.setObject(11, newContract.getUploadedAt() != null ? new java.sql.Timestamp(newContract.getUploadedAt().getTime()) : null);
            ps.setObject(12, newContract.getUploadedBy());
            ps.setObject(13, newContract.getDurationValue());
            ps.setString(14, newContract.getDurationUnit());
            ps.setString(15, newContract.getStatus() != null ? newContract.getStatus().name() : null);
            ps.setString(16, newContract.getNote());
            if (newContract.getPreviousContractId() != null) {
                ps.setInt(17, newContract.getPreviousContractId());
            } else {
                ps.setNull(17, Types.INTEGER);
            }
            ps.setString(18, newContract.getTerminationReason());
            ps.setInt(19, newContract.getContractId());

            int updated = ps.executeUpdate();
            if (updated == 0) {
                return false;
            }

            java.util.function.BiConsumer<String, String[]> logField = (fieldName, values) -> {
                String oldVal = values[0];
                String newVal = values[1];
                if (oldVal == null && newVal == null) {
                    return;
                }
                if (oldVal != null && oldVal.equals(newVal)) {
                    return;
                }
                try {
                    insertAuditLog(conn, newContract.getContractId(),
                            oldContract.getStatus().name(), newContract.getStatus().name(),
                            userId, reason, fieldName, oldVal, newVal);
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Failed to log field change: " + fieldName, ex);
                }
            };

            if (oldContract.getStatus() != newContract.getStatus()) {
                logField.accept("status", new String[]{
                    oldContract.getStatus() != null ? oldContract.getStatus().name() : null,
                    newContract.getStatus() != null ? newContract.getStatus().name() : null
                });
            }

            logField.accept("salary", new String[]{
                oldContract.getSalary() != null ? oldContract.getSalary().toString() : null,
                newContract.getSalary() != null ? newContract.getSalary().toString() : null
            });
            logField.accept("contractType", new String[]{
                oldContract.getContractType() != null ? oldContract.getContractType().name() : null,
                newContract.getContractType() != null ? newContract.getContractType().name() : null
            });
            logField.accept("effectiveDate", new String[]{
                oldContract.getEffectiveDate() != null ? oldContract.getEffectiveDate().toString() : null,
                newContract.getEffectiveDate() != null ? newContract.getEffectiveDate().toString() : null
            });
            logField.accept("endDate", new String[]{
                oldContract.getEndDate() != null ? oldContract.getEndDate().toString() : null,
                newContract.getEndDate() != null ? newContract.getEndDate().toString() : null
            });
            logField.accept("departmentName", new String[]{
                oldContract.getDepartmentName(), newContract.getDepartmentName()
            });
            logField.accept("positionName", new String[]{
                oldContract.getPositionName(), newContract.getPositionName()
            });
            logField.accept("durationValue", new String[]{
                oldContract.getDurationValue() != null ? oldContract.getDurationValue().toString() : null,
                newContract.getDurationValue() != null ? newContract.getDurationValue().toString() : null
            });
            logField.accept("durationUnit", new String[]{
                oldContract.getDurationUnit(), newContract.getDurationUnit()
            });
            logField.accept("note", new String[]{
                oldContract.getNote(), newContract.getNote()
            });
            logField.accept("contractFilePath", new String[]{
                oldContract.getContractFilePath(), newContract.getContractFilePath()
            });
            logField.accept("terminationReason", new String[]{
                oldContract.getTerminationReason(), newContract.getTerminationReason()
            });

            return true;
        }
    }

    public List<EmploymentContract> getAllContractsForOverview(
            String keyword, String contractType, String status,
            Integer departmentId, Integer loggedInEmpId, boolean isHrStaff)
            throws SQLException {

        List<EmploymentContract> contracts = new ArrayList<>();
        String sql = "SELECT c.* FROM Employment_Contracts c "
                + "JOIN Employees e ON c.employeeId = e.employeeId "
                + "JOIN Users u ON e.userId = u.userId "
                + "WHERE 1=1";

        List<Object> params = new ArrayList<>();

        if (!isHrStaff) {
            sql += " AND e.departmentId = (SELECT departmentId FROM Employees WHERE employeeId = ?)";
            params.add(loggedInEmpId);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND (c.contractCode LIKE ? OR u.fullName LIKE ? OR e.employeeCode LIKE ?)";
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (contractType != null && !contractType.trim().isEmpty()) {
            sql += " AND c.contractType = ?";
            params.add(contractType);
        }

        if (status != null && !status.trim().isEmpty()) {
            sql += " AND c.status = ?";
            params.add(status);
        }

        if (departmentId != null) {
            sql += " AND e.departmentId = ?";
            params.add(departmentId);
        }

        sql += " ORDER BY c.createdAt DESC";

        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contracts.add(mapContract(rs));
                }
            }
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

    public List<EmploymentContract> getAllContractsByEmployeeId(int employeeId) throws SQLException {
        List<EmploymentContract> contracts = new ArrayList<>();
        String sql = "SELECT * FROM Employment_Contracts WHERE employeeId = ? ORDER BY createdAt DESC";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contracts.add(mapContract(rs));
                }
            }
        }
        return contracts;
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

    public String getMaxContractCode() {
        String SQL = "SELECT MAX(CAST(SUBSTRING(contractCode, 3, 6) AS UNSIGNED)) FROM Employment_Contracts WHERE contractCode LIKE 'HD%'";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int maxSeq = rs.getInt(1);
                if (maxSeq > 0) {
                    return String.format("HD%06d", maxSeq);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get max contract code", e);
        }
        return null;
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
