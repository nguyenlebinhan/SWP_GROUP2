package dao;

import dal.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.EmploymentContract;

public class EmploymentContractDAO {
    private static final Logger LOGGER = Logger.getLogger(EmploymentContractDAO.class.getName());
    private final DBContext dbContext;

    public EmploymentContractDAO() {
        this.dbContext = new DBContext();
    }

    public boolean addContract(EmploymentContract contract) {
        String SQL = "INSERT INTO Employment_Contracts "
                + "(contractCode, employeeId, contractType, startDate, endDate, salary, status, note, createdBy) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, contract.getContractCode());
            ps.setInt(2, contract.getEmployeeId());
            ps.setString(3, contract.getContractType());
            ps.setDate(4, contract.getStartDate());
            if (contract.getEndDate() == null) {
                ps.setNull(5, Types.DATE);
            } else {
                ps.setDate(5, contract.getEndDate());
            }
            ps.setBigDecimal(6, contract.getSalary());
            ps.setInt(7, contract.getStatus());
            ps.setString(8, contract.getNote());
            ps.setInt(9, contract.getCreatedBy());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot add employment contract: " + contract.getContractCode(), e);
        }
        return false;
    }

    public boolean hasActiveContract(int employeeId) {
        String SQL = "SELECT 1 FROM Employment_Contracts "
                + "WHERE employeeId = ? AND status = 1 "
                + "AND (endDate IS NULL OR endDate >= CURRENT_DATE) "
                + "LIMIT 1";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot check active employment contract for employeeId: " + employeeId, e);
        }
        return false;
    }

    public EmploymentContract getContractById(int contractId) {
        String SQL = "SELECT contractId, contractCode, employeeId, contractType, startDate, endDate, "
                + "salary, status, note, createdBy "
                + "FROM Employment_Contracts WHERE contractId = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
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
        String SQL = "SELECT contractId, contractCode, employeeId, contractType, startDate, endDate, "
                + "salary, status, note, createdBy "
                + "FROM Employment_Contracts WHERE employeeId = ? "
                + "ORDER BY contractId DESC LIMIT 1";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapContract(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve latest employment contract for employeeId: " + employeeId, e);
        }
        return null;
    }

    private EmploymentContract mapContract(ResultSet rs) throws SQLException {
        EmploymentContract contract = new EmploymentContract();
        contract.setContractId(rs.getInt("contractId"));
        contract.setContractCode(rs.getString("contractCode"));
        contract.setEmployeeId(rs.getInt("employeeId"));
        contract.setContractType(rs.getString("contractType"));
        contract.setStartDate(rs.getDate("startDate"));
        contract.setEndDate(rs.getDate("endDate"));
        contract.setSalary(rs.getBigDecimal("salary"));
        contract.setStatus(rs.getInt("status"));
        contract.setNote(rs.getNString("note"));
        contract.setCreatedBy(rs.getInt("createdBy"));
        return contract;
    }
}
