package dao;

import dal.DBContext;
import model.LeaveBalance;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeaveBalanceDAO {
    private final DBContext dbContext;
    private static final Logger LOGGER = Logger.getLogger(LeaveBalanceDAO.class.getName());

    public LeaveBalanceDAO() {
        dbContext = new DBContext();
    }

    public LeaveBalance getLeaveBalance(int employeeId, int year) {
        String sql = "SELECT * FROM Leave_Balances WHERE employeeId = ? AND year = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new LeaveBalance(
                            rs.getInt("balanceId"),
                            rs.getInt("employeeId"),
                            rs.getInt("year"),
                            rs.getInt("totalAllowed"),
                            rs.getInt("usedDays")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting leave balance", e);
        }
        return null;
    }

    public boolean createLeaveBalance(LeaveBalance lb) {
        String sql = "INSERT INTO Leave_Balances (employeeId, year, totalAllowed, usedDays) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lb.getEmployeeId());
            ps.setInt(2, lb.getYear());
            ps.setInt(3, lb.getTotalAllowed());
            ps.setInt(4, lb.getUsedDays());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating leave balance", e);
        }
        return false;
    }

    public boolean updateUsedDays(int employeeId, int year, int additionalUsedDays) {
        String sql = "UPDATE Leave_Balances SET usedDays = usedDays + ? WHERE employeeId = ? AND year = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, additionalUsedDays);
            ps.setInt(2, employeeId);
            ps.setInt(3, year);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating used days", e);
        }
        return false;
    }
}
