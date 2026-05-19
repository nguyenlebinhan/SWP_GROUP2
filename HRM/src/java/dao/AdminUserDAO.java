package dao;

import dal.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;

public class AdminUserDAO {
    private static final Logger LOGGER = Logger.getLogger(AdminUserDAO.class.getName());
    private final DBContext dbContext;

    public AdminUserDAO() {
        this.dbContext = new DBContext();
    }

    public User getUserById(int userId) {
        String sql = "SELECT u.userId, u.username, u.email, u.fullName, u.dob, u.address, "
                + "r.roleName, u.isTemporaryPassword "
                + "FROM Users u "
                + "JOIN Roles r ON r.roleId = u.roleId "
                + "WHERE u.userId = ?";

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUserWithoutPassword(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user detail for admin view: " + userId, e);
        }
        return null;
    }

    private User mapUserWithoutPassword(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("userId"),
                rs.getString("username"),
                rs.getString("email"),
                null,
                rs.getNString("fullName"),
                rs.getString("dob"),
                rs.getString("address"),
                rs.getString("roleName"),
                rs.getBoolean("isTemporaryPassword")
        );
    }
}
