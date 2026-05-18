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
    
    public boolean updatePassword(int userId , String oldPassword){
        LOGGER.log(Level.INFO,"Updated password for user with userId: {0}",userId);
        
        String SQL = "UPDATE users u set u.password = ? WHERE u.userId = ?";
        try(Connection conn = dbContext.getConnection();
            PreparedStatement ps = conn.prepareCall(SQL)){
            
            ps.setString(1, oldPassword);
            ps.setInt(2, userId);
            
            int rowsAffected = ps.executeUpdate();
            if(rowsAffected > 0){
                LOGGER.log(Level.INFO,"Update password successfully for userId: {0}",userId);
                return true;
            }else{
                LOGGER.log(Level.WARNING,"Updating password failed: no rows affected for userId: {0}",userId);
                return false;
            }
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE,"Error updating password for userId: "+ userId,e);
            return false;
        }
    } 

    public boolean updateIsTemporaryPassword(int userId ){
        LOGGER.log(Level.INFO,"Updated isTemporaryPassword for user with userId: {0}",userId);
        
        String SQL = "UPDATE users u set u.isTemporaryPassword = 1 WHERE u.userId = ?";
        try(Connection conn = dbContext.getConnection();
            PreparedStatement ps = conn.prepareCall(SQL)){
            
            ps.setInt(1, userId);
            
            int rowsAffected = ps.executeUpdate();
            if(rowsAffected > 0){
                LOGGER.log(Level.INFO,"Update isTemporaryPassword successfully for userId: {0}",userId);
                return true;
            }else{
                LOGGER.log(Level.WARNING,"Updating isTemporaryPassword failed: no rows affected for userId: {0}",userId);
                return false;
            }
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE,"Error updating isTemporaryPassword for userId: "+ userId,e);
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
    
    public User getUserByEmail(String email) {
        LOGGER.log(Level.INFO, "Get user by email: ", email);
        String SQL = "SELECT u.userId,u.password,u.fullName,u.dob,u.address,r.roleName,u.isTemporaryPassword FROM Users u JOIN Roles r on r.roleId = u.roleId WHERE u.email = ?  ";
        try(Connection conn = dbContext.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL)){
            ps.setString(1, email);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    int userId = rs.getInt("userId");
                    String password = rs.getString("password");
                    String fullName = rs.getNString("fullName");
                    String dateOfBirth = rs.getString("dob");
                    String address = rs.getString("address");
                    String roleName = rs.getString("roleName");
                    boolean isTemporaryPassword = rs.getBoolean("isTemporaryPassword");
                    User user = new User (userId,email,password,fullName,dateOfBirth,address,roleName,isTemporaryPassword);
                    LOGGER.log(Level.INFO, "User found: userId ={0}, fullName={1}", new Object[]{userId,fullName}); 
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
}