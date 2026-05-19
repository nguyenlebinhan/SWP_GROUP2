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
        String SQL = "SELECT * FROM roles";
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
    
    private Role mapRole(ResultSet rs) throws SQLException{
        Role role = new Role();
        role.setRoleId(rs.getInt("roleId"));
        role.setRoleCode(rs.getString("roleCode"));
        role.setRoleName(rs.getString("roleName"));
        return role;
    }
        
}
