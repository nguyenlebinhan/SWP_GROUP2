/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import at.favre.lib.crypto.bcrypt.BCrypt;
import dal.DBContext;

import dto.UserUpdateRequestDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.*;
import model.User;

/**
 *
 * @author ADMIN
 */
public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());
    private final DBContext dbContext;

    public UserDAO() {
        this.dbContext = new DBContext();
    }

    public String createResetPassword(int userId) {
        LOGGER.log(Level.INFO, "Creating reset password for userId: {0}", userId);

        String password = UUID.randomUUID().toString();

        LOGGER.log(Level.FINE, "Generated password: {0}", password);

        String sql = "UPDATE users u SET u.password = ? WHERE u.userId = ? and u.isActive = 1 ";

        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hashPassword(password));
            ps.setInt(2, userId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Reset password created successfully for userId: {0}", userId);
                return password;
            } else {
                LOGGER.log(Level.WARNING, "Reset password creation failed: no rows affected for userId: {0}", userId);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating reset password for userId: " + userId, e);
        }
        return null;
    }

    public boolean updatePassword(String email, String oldPassword) {
        LOGGER.log(Level.INFO, "Updated password for user with email: {0}", email);

        String SQL = "UPDATE users u set u.password = ? WHERE u.email = ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareCall(SQL)) {

            ps.setString(1, hashPassword(oldPassword));
            ps.setString(2, email);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Update password successfully for email: {0}", email);
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Updating password failed: no rows affected for email: {0}", email);
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating password for email: " + email, e);
            return false;
        }
    }

    public boolean updateIsTemporaryPassword(String email, int setTrueFalse) {
        LOGGER.log(Level.INFO, "Updated isTemporaryPassword for user with email: {0}", email);

        String SQL = "UPDATE users u set u.isTemporaryPassword = ? WHERE u.email = ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareCall(SQL)) {

            ps.setInt(1, setTrueFalse);
            ps.setString(2, email);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Update isTemporaryPassword successfully for email: {0}", email);
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Updating isTemporaryPassword failed: no rows affected for email: {0}", email);
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating isTemporaryPassword for email: " + email, e);
            return false;
        }
    }

    public boolean isPasswordChanged(String email) {
        LOGGER.log(Level.INFO, "Verify user changing password for user with email: {0}", email);
        boolean isTemporaryPassword = false;
        String SQL = "SELECT u.isTemporaryPassword FROM users u WHERE u.email = ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareCall(SQL)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    isTemporaryPassword = rs.getBoolean("isTemporaryPassword");

                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error verifying changing password for email: " + email, e);
        }
        return isTemporaryPassword;
    }

    public boolean isEmailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking email existence: " + email, e);
        }
        return false;
    }

    public boolean addUser(String username,String email, String password, String fullName, String dob, String gender, String address, int roleId) {
        LOGGER.log(Level.INFO, "Adding new user with email: {0}", email);

        if (isEmailExists(email)) {
            LOGGER.log(Level.WARNING, "Add user failed: email already exists: {0}", email);
            return false;
        }
        String sql = "INSERT INTO users (username, email, password, fullName, dob, gender, address, roleId, isTemporaryPassword) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)";

        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, hashPassword(password));
            ps.setString(4, fullName);
            ps.setString(5, dob);
            ps.setString(6, gender);
            ps.setString(7, address);
            ps.setInt(8, roleId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "User added successfully with email: {0}", email);
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Add user failed: no rows affected for email: {0}", email);
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding user with email: " + email, e);
        }
        return false;
    }

    public boolean handleStatus(int status, int userId) {
        LOGGER.log(Level.INFO, "Handling new status account with userId: {0}", userId);
        String SQL = "UPDATE users u SET u.isActive = ? WHERE u.userId = ? ";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, status);
            ps.setInt(2, userId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "User added successfully with userId: {0}", userId);
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Add user failed: no rows affected for userId: {0}", userId);
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding user with userId: " + userId, e);
        }
        return false;
    }

    public boolean isUsernameExistsForOtherUser(String username, int userId) {
        String sql = "SELECT 1 FROM users WHERE username = ? AND userId <> ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking username existence: " + username, e);
        }
        return true;
    }

    public boolean updateMyProfile(int userId, String username, String fullName, String dob, String address) {
        LOGGER.log(Level.INFO, "Updating profile for userId: {0}", userId);
        String sql = "UPDATE users SET username = ?, fullName = ?, dob = ?, address = ? WHERE userId = ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, fullName);
            ps.setString(3, dob);
            ps.setString(4, address);
            ps.setInt(5, userId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Profile updated successfully for userId: {0}", userId);
                return true;
            }
            LOGGER.log(Level.WARNING, "Profile update failed: no rows affected for userId: {0}", userId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating profile for userId: " + userId, e);
        }
        return false;
    }
    
    
    

    public User getUserByEmail(String email) {
        LOGGER.log(Level.INFO, "Get user by email: ", email);
        String SQL = "SELECT u.userId,u.username,u.email,u.password,u.fullName,u.dob,u.address,r.roleName,u.isTemporaryPassword,isActive FROM Users u JOIN Roles r on r.roleId = u.roleId WHERE u.email = ?  ";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = mapUser(rs);
                    LOGGER.log(Level.INFO, "User found: userId ={0}, fullName={1}", new Object[]{user.getUserId(), user.getFullName()});
                    return user;
                } else {
                    LOGGER.log(Level.WARNING, "User not found with email: {0}", email);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user by email: " + email, e);
        }
        return null;
    }

    public User getUserByUsername(String username) {
        LOGGER.log(Level.INFO, "Get user by username: {0}", username);
        String SQL = "SELECT u.userId,u.username,u.email,u.password,u.fullName,u.dob,u.gender,u.address,r.roleName,u.isTemporaryPassword,u.isActive "
                + "FROM Users u JOIN Roles r on r.roleId = u.roleId "
                + "WHERE u.username = ? AND u.isActive = 1";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user by username: " + username, e);
        }
        return null;
    }

    public User authenticate(String identifier, String password) {
        if (identifier == null || password == null) {
            return null;
        }
        User user = getUserByUsername(identifier);
//        if (user == null) {
//            user = getUserByEmail(identifier);
//        }
        if (user == null) {
            return null;
        }
        if (!verifyPassword(password, user.getPassword())) {
            return null;
        }
        return user;
    }

    public static String hashPassword(String plainPassword) {
        return BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray());
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword).verified;
    }


    public List<User> getAllUsers() {
        LOGGER.log(Level.INFO, "Getting all users");
        List<User> users = new ArrayList<>();
        String SQL = "SELECT u.userId, u.username, u.email, u.password, u.fullName, u.dob,u.gender,u.address, r.roleName, u.isTemporaryPassword,u.isActive "
                + "FROM Users u JOIN Roles r ON r.roleId = u.roleId";

        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }
            LOGGER.log(Level.INFO, "Retrieved {0} users", users.size());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all users", e);
        }
        return users;
    }
    
    public boolean updateUser(int userId, String username, String email,String password, String fullName,String dob,String gender, String address, int roleId){
        LOGGER.log(Level.INFO,"Update user with userId: {0}",userId);
        String SQL = "UPDATE Users u SET u.username = ?,u.email =? ,u.password = ?,u.fullName =? ,u.dob =?,u.gender = ?,u.address =?,u.roleId = ? WHERE u.userId = ?";
    
        try (Connection conn = dbContext.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1,username);
            ps.setString(2, email);
            ps.setString(3,hashPassword(password));
            ps.setString(4, fullName);
            ps.setString(5, dob);
            ps.setString(6, gender);
            ps.setString(7, address);
            ps.setInt(8, roleId);
            ps.setInt(9, userId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "User updated successfully: id={0}", userId);
                return true;
            } else {
                LOGGER.log(Level.WARNING, "User update failed: no rows affected for id={0}", userId);
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating userId : " , e);
            return false;
        }
    }
    
    public User getUserById(int userId) {
        LOGGER.log(Level.INFO,"Getting user by userId: {0}",userId);
        String sql = "SELECT u.userId, u.username, u.email,u.password, u.fullName, u.dob,u.gender, u.address, "
                + "r.roleName,  u.isTemporaryPassword,u.isActive "
                + "FROM Users u "
                + "JOIN Roles r ON r.roleId = u.roleId "
                + "WHERE u.userId = ?";

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user detail for admin view: " + userId, e);
        }
        return null;
    }  
    
    public UserUpdateRequestDTO getUserDTOById(int userId) {
        LOGGER.log(Level.INFO,"Getting user by userId: {0}",userId);
        String sql = "SELECT u.userId, u.username, u.email, u.fullName, u.dob,u.gender ,u.address, "
                + "u.roleId "
                + "FROM Users u "
                + "WHERE u.userId = ?";

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserUpdateRequestDTO(rs.getInt("roleId"),rs.getString("username"),rs.getString("email"),rs.getNString("fullName"),rs.getString("dob"),rs.getString("gender"),rs.getNString("address"),rs.getInt("roleId"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user detail for admin view: " + userId, e);
        }
        return null;
    }      

    public List<User> getUsersByRoleId(int roleId) {
        LOGGER.log(Level.INFO, "Getting users by roleId: {0}", roleId);
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.userId, u.username, u.email, u.password, u.fullName, u.dob,u.gender , u.address, "
                + "r.roleName, u.isTemporaryPassword, u.isActive "
                + "FROM Users u "
                + "JOIN Roles r ON r.roleId = u.roleId "
                + "WHERE u.roleId = ? "
                + "ORDER BY u.fullName";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting users by roleId: " + roleId, e);
        }
        return users;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        int userId = rs.getInt("userId");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String password = rs.getString("password");
        String fullName = rs.getNString("fullName");
        String dateOfBirth = rs.getString("dob");
        String gender = rs.getNString("gender");
        String address = rs.getString("address");
        String roleName = rs.getString("roleName");
        //String avatar = rs.getString("avatar");
        boolean isTemporaryPassword = rs.getBoolean("isTemporaryPassword");
        int isActive = rs.getInt("isActive");
        return new User(userId, username, email, password, fullName, dateOfBirth, gender, address, roleName, isTemporaryPassword,isActive);
    }    
}
