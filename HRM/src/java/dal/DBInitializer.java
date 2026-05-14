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
                + "email VARCHAR(50) NOT NULL UNIQUE,"
                + "fullName NVARCHAR(150) NOT NULL,"
                + "dob DATETIME,"
                + "roleId INT NOT NULL,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
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
                "Users",            
                "Roles"
            };
            String[] createOrder = {
                 "Roles","Users"
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
                    }
                }
            }
            
            insertInitialData(conn);
            LOGGER.info("Database initialization completed successfully!");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Initialization failed", e);
        }
    }


    private void insertInitialData(Connection conn) {
        try {
            if (countRows(conn, "Users") > 0) return;
        }catch(Exception e){
            LOGGER.log(Level.SEVERE,"CANNOT INSERT TABLE");
        }
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
    
