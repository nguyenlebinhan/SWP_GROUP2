



package dao;

import dal.DBContext;
import java.util.*;
import java.util.logging.*;
import model.Permission;
import java.sql.*;




public class PermissionDAO {
    private static final Logger LOGGER = Logger.getLogger(PermissionDAO.class.getName());
    private final DBContext dbContext;

    public PermissionDAO() {
        this.dbContext = new DBContext();
    }

    public Set<Permission>getAllPermissionByRoleId(int roleId){
        LOGGER.log(Level.INFO,"Getting all permissions by roleId: {0}",roleId);
        Set<Permission> permissions  = new HashSet<>();
        String SQL = "SELECT * FROM permissions p INNER JOIN role_permissions rp on rp.permissionId = p.permissionId WHERE rp.roleId = ?";
        try(Connection conn = dbContext.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL)){
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

    /**
     * Get permission codes for a user using an injected connection (for filter transactional flow).
     */
    public Set<String> getPermissionCodesByUserId(Connection conn, int userId) {
        LOGGER.log(Level.FINE, "Getting permissions by userId (conn-injected): {0}", userId);
        Set<String> permissions = new HashSet<>();
        String SQL = "SELECT p.permissionCode "
                + "FROM permissions p "
                + "JOIN role_permissions rp ON rp.permissionId = p.permissionId "
                + "JOIN users u ON u.roleId = rp.roleId "
                + "WHERE u.userId = ?";
        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permissions.add(rs.getString("permissionCode"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve permissions by userId: " + userId, e);
        }
        return permissions;
    }

    public Set<String> getPermissionCodeByUserId(int userId) {
        LOGGER.log(Level.INFO, "Getting permissions by userId through role_permissions: {0}", userId);
        Set<String> permissions = new HashSet<>();
        String SQL = "SELECT p.permissionCode "
                + "FROM permissions p "
                + "JOIN role_permissions rp ON rp.permissionId = p.permissionId "
                + "JOIN users u ON u.roleId = rp.roleId "
                + "WHERE u.userId = ? "
                + "ORDER BY p.permissionName";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permissions.add(rs.getString("permissionCode"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve permissions by userId: " + userId, e);
        }
        return permissions;
    }

    public List<Permission> getAllPermissions() {
        List<Permission> permissions = new ArrayList<>();
        String SQL = "SELECT permissionId, permissionCode, permissionName, description FROM Permissions ORDER BY permissionName";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                permissions.add(mapPermission(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve all permissions", e);
        }
        return permissions;
    }

    public boolean updateRolePermissions(int roleId, List<Integer> permissionIds) {
        String deleteSQL = "DELETE FROM Role_Permissions WHERE roleId = ?";
        String insertSQL = "INSERT INTO Role_Permissions (roleId, permissionId) VALUES (?, ?)";
        Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement del = conn.prepareStatement(deleteSQL)) {
                del.setInt(1, roleId);
                del.executeUpdate();
            }

            if (permissionIds != null && !permissionIds.isEmpty()) {
                try (PreparedStatement ins = conn.prepareStatement(insertSQL)) {
                    for (int permId : permissionIds) {
                        ins.setInt(1, roleId);
                        ins.setInt(2, permId);
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
            }

            conn.commit();
            LOGGER.log(Level.INFO, "Updated permissions for roleId={0}: {1} permission(s)", new Object[]{roleId, permissionIds == null ? 0 : permissionIds.size()});
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot update role permissions for roleId: " + roleId, e);
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
        return false;
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

