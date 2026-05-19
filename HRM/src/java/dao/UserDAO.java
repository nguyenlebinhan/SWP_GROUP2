/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dal.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
        
        String sql = "UPDATE users u SET u.password = ? WHERE u.userId = ? ";
        
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, password);
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
    
    public boolean updatePassword(String email , String oldPassword){
        LOGGER.log(Level.INFO,"Updated password for user with email: {0}",email);
        
        String SQL = "UPDATE users u set u.password = ? WHERE u.email = ?";
        try(Connection conn = dbContext.getConnection();
            PreparedStatement ps = conn.prepareCall(SQL)){
            
            ps.setString(1, oldPassword);
            ps.setString(2, email);
            
            int rowsAffected = ps.executeUpdate();
            if(rowsAffected > 0){
                LOGGER.log(Level.INFO,"Update password successfully for email: {0}",email);
                return true;
            }else{
                LOGGER.log(Level.WARNING,"Updating password failed: no rows affected for email: {0}",email);
                return false;
            }
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE,"Error updating password for email: "+ email,e);
            return false;
        }
    } 

    public boolean updateIsTemporaryPassword(String email, int setTrueFalse ){
        LOGGER.log(Level.INFO,"Updated isTemporaryPassword for user with email: {0}",email);
        
        String SQL = "UPDATE users u set u.isTemporaryPassword = ? WHERE u.email = ?";
        try(Connection conn = dbContext.getConnection();
            PreparedStatement ps = conn.prepareCall(SQL)){
            
            ps.setInt(1, setTrueFalse);
            ps.setString(2, email);
            
            int rowsAffected = ps.executeUpdate();
            if(rowsAffected > 0){
                LOGGER.log(Level.INFO,"Update isTemporaryPassword successfully for email: {0}",email);
                return true;
            }else{
                LOGGER.log(Level.WARNING,"Updating isTemporaryPassword failed: no rows affected for email: {0}",email);
                return false;
            }
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE,"Error updating isTemporaryPassword for email: "+ email,e);
            return false;
        }
    }     
    
    
    public boolean isPasswordChanged(String email){
        LOGGER.log(Level.INFO,"Verify user changing password for user with email: {0}",email);
        boolean isTemporaryPassword = false;
        String SQL = "SELECT u.isTemporaryPassword FROM users u WHERE u.email = ?";
        try(Connection conn = dbContext.getConnection();
            PreparedStatement ps = conn.prepareCall(SQL)){
            ps.setString(1, email);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    isTemporaryPassword = rs.getBoolean("isTemporaryPassword");
                    
                }
            }
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE,"Error verifying changing password for email: " +email, e);
        }
        return isTemporaryPassword;
    }
    
    public boolean isEmailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking email existence: " + email, e);
        }
        return false;
    }

    public boolean addUser(String email, String password, String fullName, String dob, String gender, String address, int roleId) {
        LOGGER.log(Level.INFO, "Adding new user with email: {0}", email);

        if (isEmailExists(email)) {
            LOGGER.log(Level.WARNING, "Add user failed: email already exists: {0}", email);
            return false;
        }
        String sql = "INSERT INTO users (email, password, fullName, dob, address, roleId, isTemporaryPassword) VALUES (?, ?, ?, ?, ?, ?, 1)";

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            ps.setString(3, fullName);
            ps.setString(4, dob);
            ps.setString(5, address);
            ps.setInt(6, roleId);

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
    
    public boolean handleStatus(int status,int userId){
        LOGGER.log(Level.INFO,"Handling new status account with userId: {0}", userId);
        String SQL = "UPDATE users u SET u.status = ? WHERE u.userId = ? ";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
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

    public User getUserByEmail(String email) {
        LOGGER.log(Level.INFO, "Get user by email: ", email);
        String SQL = "SELECT u.userId,u.username,u.email,u.password,u.fullName,u.dob,u.address,r.roleName,u.isTemporaryPassword FROM Users u JOIN Roles r on r.roleId = u.roleId WHERE u.email = ?  ";
        try(Connection conn = dbContext.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL)){
            ps.setString(1, email);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    User user = mapUser(rs);
                    LOGGER.log(Level.INFO, "User found: userId ={0}, fullName={1}", new Object[]{user.getUserId(),user.getFullName()}); 
                    return user;
                }else{
                    LOGGER.log(Level.WARNING, "User not found with email: {0}", email);
                }
            }
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE, "Error getting user by email: " + email, e);               
        }
        return null;
    }

    public User getUserByUsername(String username) {
        LOGGER.log(Level.INFO, "Get user by username: {0}", username);
        String SQL = "SELECT u.userId,u.username,u.email,u.password,u.fullName,u.dob,u.address,r.roleName,u.isTemporaryPassword "
                + "FROM Users u JOIN Roles r on r.roleId = u.roleId "
                + "WHERE u.username = ?";
        try(Connection conn = dbContext.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL)){
            ps.setString(1, username);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return mapUser(rs);
                }
            }
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE, "Error getting user by username: " + username, e);
        }
        return null;
    }

    public User authenticate(String username, String password) {
        User user = getUserByUsername(username);
        if (user == null || password == null || !password.equals(user.getPassword())) {
            return null;
        }
        return user;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        int userId = rs.getInt("userId");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String password = rs.getString("password");
        String fullName = rs.getNString("fullName");
        String dateOfBirth = rs.getString("dob");
        String address = rs.getString("address");
        String roleName = rs.getString("roleName");
        boolean isTemporaryPassword = rs.getBoolean("isTemporaryPassword");
        return new User(userId, username, email, password, fullName, dateOfBirth, address, roleName, isTemporaryPassword);
    }
}
