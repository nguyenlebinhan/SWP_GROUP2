package dal;

import entity.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.PasswordUtil;

public class UserDAO extends DBContext {

    public User loginWithAccount(String account, String password) throws SQLException {
        String sql = "SELECT userId, username, email, passwordHash, fullName, roleId, authProvider, googleId, avatarUrl, isActive "
                + "FROM Users "
                + "WHERE isActive = TRUE AND authProvider = 'LOCAL' AND (username = ? OR email = ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, account);
            ps.setString(2, account);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && PasswordUtil.matches(password, rs.getString("passwordHash"))) {
                    return mapUser(rs);
                }
            }
        }

        return null;
    }

    public User loginWithGmail(String email, String googleId) throws SQLException {
        String sql = "SELECT userId, username, email, fullName, roleId, authProvider, googleId, avatarUrl, isActive "
                + "FROM Users "
                + "WHERE isActive = TRUE AND authProvider = 'GOOGLE' AND email = ? "
                + "AND (? IS NULL OR googleId = ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, googleId);
            ps.setString(3, googleId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }

        return null;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("userId"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("fullName"));
        user.setRoleId(rs.getInt("roleId"));
        user.setAuthProvider(rs.getString("authProvider"));
        user.setGoogleId(rs.getString("googleId"));
        user.setAvatarUrl(rs.getString("avatarUrl"));
        user.setActive(rs.getBoolean("isActive"));
        return user;
    }
}
