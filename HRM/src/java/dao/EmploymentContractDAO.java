package dao;

import dal.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
}
