/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dal.DBContext;
import java.util.*;
import java.util.logging.*;
import model.Permission;
import model.RolePermissionItem;
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
        return getPermissionsByRoleId(roleId);
    }

    public List<Permission> getPermissionsByRoleId(int roleId) {
        LOGGER.log(Level.INFO, "Getting permissions by roleId through Role_Permissions: {0}", roleId);
        List<Permission> permissions = new ArrayList<>();
        String SQL = "SELECT p.permissionId, p.permissionCode, p.permissionName, p.description "
                + "FROM Permissions p "
                + "JOIN Role_Permissions rp ON rp.permissionId = p.permissionId "
                + "WHERE rp.roleId = ? "
                + "ORDER BY p.permissionName";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permissions.add(mapPermission(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve permissions by roleId: " + roleId, e);
        }
        return permissions;
    }

    public List<RolePermissionItem> getRolePermissionItems(int roleId) {
        LOGGER.log(Level.INFO, "Getting permission selection rows for roleId: {0}", roleId);
        List<RolePermissionItem> items = new ArrayList<>();
        String SQL = "SELECT p.permissionId, p.permissionCode, p.permissionName, p.description, "
                + "CASE WHEN rp.permissionId IS NULL THEN 0 ELSE 1 END AS granted "
                + "FROM Permissions p "
                + "LEFT JOIN Role_Permissions rp ON rp.permissionId = p.permissionId AND rp.roleId = ? "
                + "ORDER BY p.permissionName";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new RolePermissionItem(mapPermission(rs), rs.getBoolean("granted")));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve permission selection rows for roleId: " + roleId, e);
        }
        return items;
    }

    public boolean updateRolePermissions(int roleId, Set<Integer> permissionIds) {
        String deleteSql = "DELETE FROM Role_Permissions WHERE roleId = ?";
        String insertSql = "INSERT INTO Role_Permissions (roleId, permissionId) VALUES (?, ?)";

        try (Connection conn = dbContext.getConnection()) {
            Set<Integer> validPermissionIds = filterExistingPermissionIds(conn, permissionIds);
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
                    deletePs.setInt(1, roleId);
                    deletePs.executeUpdate();
                }

                if (!validPermissionIds.isEmpty()) {
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        for (Integer permissionId : validPermissionIds) {
                            insertPs.setInt(1, roleId);
                            insertPs.setInt(2, permissionId);
                            insertPs.addBatch();
                        }
                        insertPs.executeBatch();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot update role permissions for roleId: " + roleId, e);
            return false;
        }
    }

    private Set<Integer> filterExistingPermissionIds(Connection conn, Set<Integer> permissionIds) throws SQLException {
        Set<Integer> validPermissionIds = new LinkedHashSet<>();
        if (permissionIds == null || permissionIds.isEmpty()) {
            return validPermissionIds;
        }

        Set<Integer> existingPermissionIds = new HashSet<>();
        String SQL = "SELECT permissionId FROM Permissions";
        try (PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                existingPermissionIds.add(rs.getInt("permissionId"));
            }
        }

        for (Integer permissionId : permissionIds) {
            if (existingPermissionIds.contains(permissionId)) {
                validPermissionIds.add(permissionId);
            }
        }
        return validPermissionIds;
    }

    private Permission mapPermission(ResultSet rs) throws SQLException{
        Permission permission  = new Permission();
        permission.setPermissionId(rs.getInt("permissionId"));
        permission.setPermissionCode(rs.getString("permissionCode"));
        permission.setPermissionName(rs.getString("permissionName"));
        permission.setDescription(rs.getNString("description"));
        return permission;
    }
}
