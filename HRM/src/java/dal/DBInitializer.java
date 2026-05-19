/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.PasswordUtil;

/**
 *
 * @author ADMIN
 */
public class DBInitializer {
    private static final Logger LOGGER = Logger.getLogger(DBInitializer.class.getName());
    private final DBContext dbContext;

    public DBInitializer() {
        this.dbContext = new DBContext();
    }
    public void createTableRoles(Connection conn){
        String SQL = "CREATE TABLE Roles("
                + "roleId INT PRIMARY KEY AUTO_INCREMENT,"
                + "roleCode VARCHAR(100) NOT NULL UNIQUE,"
                + "roleName VARCHAR(50) NOT NULL UNIQUE,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"                
                + ")";
        execute(conn,SQL,"CREATE ROLES TABLE SUCCESSFULLY");
    }
   
    
//    public void createTablePermissions(Connection conn){
//        String SQL = "CREATE TABLE Permissions("
//                + "permissionId INT PRIMARY KEY AUTO_INCREMENT,"
//                + "permissionCode VARCHAR(50) NOT NULL UNIQUE,"
//                + "permissionName VARCHAR(100) NOT NULL UNIQUE,"
//                + "description NVARCHAR(150) NOT NULL,"
//                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
//                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"                
//                + ")";
//        execute(conn,SQL,"CREATE PERMISSIONS TABLE SUCCESSFULLY");
//    }
//    
//    public void createTableRolesPermissions(Connection conn){
//        String SQL = "CREATE TABLE Roles_Permissions ("
//                    + "roles_permissionsId INT PRIMARY KEY AUTO_INCREMENT, "
//                    + "roleId INT, "
//                    + "permissionId INT, "
//                    + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
//                    + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"                
//                    + "FOREIGN KEY (roleId) REFERENCES Roles(roleId),"
//                    + "FOREIGN KEY (permissionId) REFERENCES Permissions(permissionId)"
//                    + ")";
//        execute(conn,SQL,"CREATE ROLES_PERMISSIONS TABLE SUCCESSFULLY");
//    }    
    
    public void createTableUsers(Connection conn){
        String SQL ="CREATE TABLE Users("
                + "userId INT PRIMARY KEY AUTO_INCREMENT,"
                + "username VARCHAR(50) NOT NULL UNIQUE,"
                + "email VARCHAR(100) NOT NULL UNIQUE,"
                + "passwordHash VARCHAR(255),"
                + "fullName NVARCHAR(150) NOT NULL,"
                + "dob DATETIME,"
                + "roleId INT NOT NULL,"
                + "authProvider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',"
                + "googleId VARCHAR(128) UNIQUE,"
                + "avatarUrl VARCHAR(255),"
                + "isActive BOOLEAN NOT NULL DEFAULT TRUE,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "CONSTRAINT chk_users_auth_provider CHECK (authProvider IN ('LOCAL','GOOGLE')),"
                + "FOREIGN KEY (roleId) REFERENCES Roles(roleId)"
                + ")";
        execute(conn,SQL,"CREATE USERS TABLE SUCCESSFULLY");
        
    }
    
    public void createTableEmployees(Connection conn) {
        String SQL = "CREATE TABLE Employees("
                + "employeeId INT PRIMARY KEY AUTO_INCREMENT,"
                + "employeeCode VARCHAR(50) NOT NULL UNIQUE,"
                + "userId INT NOT NULL,"               
                + "department NVARCHAR(100),"
                + "position NVARCHAR(100),"
                + "phoneNumber VARCHAR(20),"
                + "address NVARCHAR(255),"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (userId) REFERENCES Users(userId)"
                + ")";
        execute(conn, SQL, "CREATE EMPLOYEES TABLE SUCCESSFULLY");  
    }
    
    
    public void initializeDatabase(boolean enforceReset) {
        try (Connection conn = dbContext.getConnection()) {
            if (conn == null) {
                LOGGER.severe("Cannot get database connection!");
                return;
            }

            String[] dropOrder = {        
                "Employees",
                "Users",            
                "Roles"
            };
            String[] createOrder = {
                 "Roles","Users","Employees"
            };

            if (enforceReset) {
                LOGGER.info("Enforce reset: Dropping all tables...");
                for (String table : dropOrder) {
                    dropTable(conn, table);
                }
            }

            for (String table : createOrder) {
                if (enforceReset || !tableExists(conn, table)) {
                    switch (table) {
                        case "Roles":createTableRoles (conn); break;
                        case "Users":createTableUsers (conn); break;
                        case "Employees":createTableEmployees(conn); break;
                    }
                }
            }

            ensureUserLoginColumns(conn);
            
            insertInitialData(conn);
            LOGGER.info("Database initialization completed successfully!");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Initialization failed", e);
        }
    }


    private void insertInitialData(Connection conn) {
        try {
            if (countRows(conn, "Roles") == 0) {
                insertDefaultRoles(conn);
            }

            if (countRows(conn, "Users") == 0) {
                insertDefaultUsers(conn);
            }
        }catch(Exception e){
            LOGGER.log(Level.SEVERE,"CANNOT INSERT INITIAL DATA", e);
        }
    }

    private void ensureUserLoginColumns(Connection conn) throws SQLException {
        if (!tableExists(conn, "Users")) {
            return;
        }

        addColumnIfMissing(conn, "Users", "passwordHash", "ALTER TABLE Users ADD COLUMN passwordHash VARCHAR(255)");
        addColumnIfMissing(conn, "Users", "authProvider", "ALTER TABLE Users ADD COLUMN authProvider VARCHAR(20) NOT NULL DEFAULT 'LOCAL'");
        addColumnIfMissing(conn, "Users", "googleId", "ALTER TABLE Users ADD COLUMN googleId VARCHAR(128) UNIQUE");
        addColumnIfMissing(conn, "Users", "avatarUrl", "ALTER TABLE Users ADD COLUMN avatarUrl VARCHAR(255)");
        addColumnIfMissing(conn, "Users", "isActive", "ALTER TABLE Users ADD COLUMN isActive BOOLEAN NOT NULL DEFAULT TRUE");
    }

    private void addColumnIfMissing(Connection conn, String tableName, String columnName, String sql) throws SQLException {
        if (!columnExists(conn, tableName, columnName)) {
            execute(conn, sql, "ADD COLUMN " + tableName + "." + columnName);
        }
    }

    private void insertDefaultRoles(Connection conn) throws SQLException {
        String sql = "INSERT INTO Roles(roleCode, roleName) VALUES (?, ?), (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "ADMIN");
            ps.setString(2, "Administrator");
            ps.setString(3, "EMPLOYEE");
            ps.setString(4, "Employee");
            ps.executeUpdate();
            LOGGER.info("INSERTED DEFAULT ROLES");
        }
    }

    private void insertDefaultUsers(Connection conn) throws SQLException {
        String sql = "INSERT INTO Users(username, email, passwordHash, fullName, dob, roleId, authProvider, googleId) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int adminRoleId = getRoleId(conn, "ADMIN");
            int employeeRoleId = getRoleId(conn, "EMPLOYEE");

            ps.setString(1, "admin");
            ps.setString(2, "admin@gmail.com");
            ps.setString(3, PasswordUtil.hashPassword("123456"));
            ps.setString(4, "System Administrator");
            ps.setDate(5, null);
            ps.setInt(6, adminRoleId);
            ps.setString(7, "LOCAL");
            ps.setString(8, null);
            ps.addBatch();

            ps.setString(1, "google_user");
            ps.setString(2, "group2@gmail.com");
            ps.setString(3, null);
            ps.setString(4, "Google User");
            ps.setDate(5, null);
            ps.setInt(6, employeeRoleId);
            ps.setString(7, "GOOGLE");
            ps.setString(8, "google-group2-demo");
            ps.addBatch();

            ps.executeBatch();
            LOGGER.info("INSERTED DEFAULT USERS");
        }
    }

    private int getRoleId(Connection conn, String roleCode) throws SQLException {
        String sql = "SELECT roleId FROM Roles WHERE roleCode = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("roleId");
                }
            }
        }
        throw new SQLException("Role not found: " + roleCode);
    }



    private void execute(Connection conn, String sql, String label)  {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.log(Level.INFO,"EXECUTED: " + label);
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE,"CANNOT EXECUTE: " + label);
        }
    }

    private void dropTable(Connection conn, String tableName) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE " + tableName);
            LOGGER.info("DROPPED TABLE: " + tableName);
        } catch (SQLException e) {
            LOGGER.severe("COULD NOT DROP " + tableName + ": " + e.getMessage());
        }
    }

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    private int countRows(Connection conn, String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public static void main(String[] args) {
        DBInitializer initializer = new DBInitializer();
        initializer.initializeDatabase(true);
    }
}
    
