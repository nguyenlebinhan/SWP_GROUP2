/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dal.DBContext;
import java.util.*;
import java.util.logging.*;
import model.Permission;
import java.sql.*;
/**
 *
 * @author ADMIN
 */
public class PermissionDAO {
    private static final Logger LOGGER = Logger.getLogger(PermissionDAO.class.getName());
    private final DBContext dbContext;

    public PermissionDAO() {
        this.dbContext = new DBContext();
    }    
    
    public List<Permission>getAllPermissionByRoleId(int roleId){
        LOGGER.log(Level.INFO,"Getting all permissions by roleId: {0}",roleId);
        List<Permission> permissions  = new ArrayList<>();
        String SQL = "SELECT * FROM permissions p WHERE p.roleId = ?";
        try(Connection conn = dbContext.getConnection();
            PreparedStatement ps = conn.prepareCall(SQL)){
            ps.setInt(1, roleId);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    permissions.add(mapPermission(rs));
                }
            }
            LOGGER.log(Level.INFO,"Retrieve {0} permissions by roleId",permissions.size());
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE,"Cannot retrieve any permissions",e);
        }
        return permissions;
    }
    private Permission mapPermission(ResultSet rs) throws SQLException{
        Permission permission  = new Permission();
        permission.setPermissionId(rs.getInt("permissionId"));
        permission.setPermissionCode("permissionCode");
        permission.setPermissionName(rs.getString("permissionName"));
        permission.setDescription(rs.getNString("description"));
        return permission;
    }
}
