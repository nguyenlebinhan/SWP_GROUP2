/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dal.DBContext;
import java.util.logging.*;
import java.util.*;
import java.sql.*;

import model.Role;
/**
 *
 * @author ADMIN
 */
public class RoleDAO {
    private static final Logger LOGGER = Logger.getLogger(RoleDAO.class.getName());
    private final DBContext dbContext;

    public RoleDAO() {
        this.dbContext = new DBContext();
    }    
    
    public List<Role> getAllRoles(){
        LOGGER.log(Level.INFO,"Get all roles");
        List<Role> roles = new ArrayList<>();
        String SQL = "SELECT * FROM Roles ORDER BY roleId";
        try(Connection conn = dbContext.getConnection();
            PreparedStatement ps = conn.prepareCall(SQL);
            ResultSet rs = ps.executeQuery()){
            while(rs.next()){
                roles.add(mapRole(rs));
            }
            LOGGER.log(Level.INFO,"Retrieve {0} roles from DB.",roles.size());
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE,"Cannot retrieve roles from DB",e);
        }
        return roles;
    }

    public Role getRoleById(int roleId) {
        LOGGER.log(Level.INFO, "Get role by roleId: {0}", roleId);
        String SQL = "SELECT * FROM Roles WHERE roleId = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRole(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve role by roleId: " + roleId, e);
        }
        return null;
    }

    public int countUsersByRoleId(int roleId) {
        String SQL = "SELECT COUNT(*) FROM Users WHERE roleId = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot count users by roleId: " + roleId, e);
        }
        return 0;
    }

    public int countPermissionsByRoleId(int roleId) {
        String SQL = "SELECT COUNT(*) FROM Role_Permissions WHERE roleId = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot count permissions by roleId: " + roleId, e);
        }
        return 0;
    }
    
    public boolean handleStatus(int status,int roleId){
        LOGGER.log(Level.INFO,"Handling new status role with roleId: {0}", roleId);
        String SQL = "UPDATE users u SET u.status = ? WHERE u.userId = ? ";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, status);
            ps.setInt(2, roleId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Role updated successfully with roleId: {0}", roleId);
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Updated roles failed: no rows affected for roleId: {0}", roleId);
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating roles with roleId: " + roleId, e);
        }
        return false;        
    }    
    public boolean addRole(String roleCode, String roleName) {
        LOGGER.log(Level.INFO, "Adding new role  {0}", roleCode);

        String sql = "INSERT INTO Roles (roleCode,roleName) VALUES (?, ?)";

        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleCode);
            ps.setString(2,roleName);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Roles added successfully with roleCode: {0}", roleCode);
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Add roles failed: no rows affected for roleCode: {0}", roleCode);
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding roles with roleCode: " + roleCode, e);
        }
        return false;
    }    
    
    
    
    private Role mapRole(ResultSet rs) throws SQLException{
        Role role = new Role();
        role.setRoleId(rs.getInt("roleId"));
        role.setRoleCode(rs.getString("roleCode"));
        role.setRoleName(rs.getString("roleName"));
        role.setIsActive(rs.getInt("isActive"));
        return role;
    }
        
}
