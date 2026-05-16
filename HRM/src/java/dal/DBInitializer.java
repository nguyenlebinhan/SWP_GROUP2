/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    // ==================== BẢNG GỐC (bạn đã có) ====================

    public void createTableRoles(Connection conn) {
        String SQL = "CREATE TABLE Roles("
                + "roleId INT PRIMARY KEY AUTO_INCREMENT,"
                + "roleCode VARCHAR(100) NOT NULL UNIQUE,"
                + "roleName VARCHAR(50) NOT NULL UNIQUE,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ")";
        execute(conn, SQL, "CREATE ROLES TABLE SUCCESSFULLY");
    }

    public void createTablePermissions(Connection conn) {
        String SQL = "CREATE TABLE Permissions("
                + "permissionId INT PRIMARY KEY AUTO_INCREMENT,"
                + "permissionCode VARCHAR(50) NOT NULL UNIQUE,"
                + "permissionName VARCHAR(100) NOT NULL UNIQUE,"
                + "description NVARCHAR(150),"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ")";
        execute(conn, SQL, "CREATE PERMISSIONS TABLE SUCCESSFULLY");
    }

    public void createTableDepartments(Connection conn) {
        String SQL = "CREATE TABLE Departments("
                + "departmentId INT PRIMARY KEY AUTO_INCREMENT,"
                + "departmentCode VARCHAR(50) NOT NULL UNIQUE,"
                + "departmentName NVARCHAR(150) NOT NULL UNIQUE,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ")";
        execute(conn, SQL, "CREATE DEPARTMENTS TABLE SUCCESSFULLY");
    }

    public void createTableUsers(Connection conn) {
        String SQL = "CREATE TABLE Users("
                + "userId INT PRIMARY KEY AUTO_INCREMENT,"
                + "email VARCHAR(50) NOT NULL UNIQUE,"
                + "password VARCHAR(150) NOT NULL,"
                + "fullName NVARCHAR(150) NOT NULL,"
                + "dob DATETIME,"
                + "address NVARCHAR(150),"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ")";
        execute(conn, SQL, "CREATE USERS TABLE SUCCESSFULLY");
    }

    public void createTableEmployees(Connection conn) {
        String SQL = "CREATE TABLE Employees("
                + "employeeId INT PRIMARY KEY AUTO_INCREMENT,"
                + "employeeCode VARCHAR(50) NOT NULL UNIQUE,"
                + "userId INT NOT NULL,"
                + "departmentId INT,"
                + "position NVARCHAR(150),"
                + "phoneNumber VARCHAR(20),"
                + "address NVARCHAR(255),"
                + "skills NVARCHAR(255),"
                + "experience NVARCHAR(255),"
                + "degree NVARCHAR(100),"
                + "bankAccount VARCHAR(50),"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (userId) REFERENCES Users(userId),"
                + "FOREIGN KEY (departmentId) REFERENCES Departments(departmentId)"
                + ")";
        execute(conn, SQL, "CREATE EMPLOYEES TABLE SUCCESSFULLY");
    }

    public void createTableUserDepartmentRoles(Connection conn) {
        String SQL = "CREATE TABLE User_Department_Roles("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "userId INT NOT NULL,"
                + "departmentId INT NOT NULL,"
                + "roleId INT NOT NULL,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "UNIQUE KEY uq_user_dept_role (userId, departmentId, roleId),"
                + "FOREIGN KEY (userId) REFERENCES Users(userId),"
                + "FOREIGN KEY (departmentId) REFERENCES Departments(departmentId),"
                + "FOREIGN KEY (roleId) REFERENCES Roles(roleId)"
                + ")";
        execute(conn, SQL, "CREATE USER_DEPARTMENT_ROLES TABLE SUCCESSFULLY");
    }

    public void createTableDepartmentRolePermissions(Connection conn) {
        String SQL = "CREATE TABLE Department_Role_Permissions("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "departmentId INT NOT NULL,"
                + "roleId INT NOT NULL,"
                + "permissionId INT NOT NULL,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "UNIQUE KEY uq_dept_role_perm (departmentId, roleId, permissionId),"
                + "FOREIGN KEY (departmentId) REFERENCES Departments(departmentId),"
                + "FOREIGN KEY (roleId) REFERENCES Roles(roleId),"
                + "FOREIGN KEY (permissionId) REFERENCES Permissions(permissionId)"
                + ")";
        execute(conn, SQL, "CREATE DEPARTMENT_ROLE_PERMISSIONS TABLE SUCCESSFULLY");
    }


    // Hồ sơ ứng viên (HoSoUngVien)
    public void createTableCandidates(Connection conn) {
        String SQL = "CREATE TABLE Candidates("
                + "candidateId INT PRIMARY KEY AUTO_INCREMENT,"
                + "candidateCode VARCHAR(50) NOT NULL UNIQUE,"
                + "fullName NVARCHAR(150) NOT NULL,"
                + "dob DATETIME,"
                + "gender TINYINT(1),"           // 0: Nữ, 1: Nam
                + "phoneNumber VARCHAR(20),"
                + "address NVARCHAR(255),"
                + "email VARCHAR(100),"
                + "skills NVARCHAR(255),"
                + "experience NVARCHAR(255),"
                + "degree NVARCHAR(100),"
                + "position NVARCHAR(150),"
                + "status TINYINT(1) DEFAULT 0," // 0: Đang xét, 1: Đậu, 2: Trượt
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ")";
        execute(conn, SQL, "CREATE CANDIDATES TABLE SUCCESSFULLY");
    }

    // Công việc (CongViec)
    public void createTableJobs(Connection conn) {
        String SQL = "CREATE TABLE Jobs("
                + "jobId INT PRIMARY KEY AUTO_INCREMENT,"
                + "jobCode VARCHAR(50) NOT NULL UNIQUE,"
                + "jobName NVARCHAR(150) NOT NULL,"
                + "description NVARCHAR(500),"
                + "employeeId INT,"              // người được giao
                + "priority TINYINT DEFAULT 1,"  // 1: Thấp, 2: Trung bình, 3: Cao
                + "startDate DATE,"
                + "endDate DATE,"
                + "standardCost DECIMAL(15,2),"
                + "reportCode VARCHAR(50),"
                + "reportContent NVARCHAR(500),"
                + "reportChart NVARCHAR(255),"   // đường dẫn file biểu đồ
                + "status TINYINT DEFAULT 0,"    // 0: Chưa bắt đầu, 1: Đang làm, 2: Hoàn thành
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (employeeId) REFERENCES Employees(employeeId)"
                + ")";
        execute(conn, SQL, "CREATE JOBS TABLE SUCCESSFULLY");
    }

    // Chấm công (BangCong)
    public void createTableAttendance(Connection conn) {
        String SQL = "CREATE TABLE Attendance("
                + "attendanceId INT PRIMARY KEY AUTO_INCREMENT,"
                + "attendanceCode VARCHAR(50) NOT NULL UNIQUE,"
                + "employeeId INT NOT NULL,"
                + "workDate DATE NOT NULL,"
                + "timeIn TIME,"
                + "timeOut TIME,"
                + "hoursWorked DECIMAL(4,2),"
                + "dayOff DATE,"
                + "workingDay DATE,"
                + "penalty DECIMAL(15,2) DEFAULT 0,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (employeeId) REFERENCES Employees(employeeId)"
                + ")";
        execute(conn, SQL, "CREATE ATTENDANCE TABLE SUCCESSFULLY");
    }

    // Bảng lương (BangLuong)
    public void createTablePayroll(Connection conn) {
        String SQL = "CREATE TABLE Payroll("
                + "payrollId INT PRIMARY KEY AUTO_INCREMENT,"
                + "periodStart DATE NOT NULL,"   // kỳ lương từ ngày
                + "employeeId INT NOT NULL,"
                + "position NVARCHAR(150),"
                + "departmentId INT,"
                + "workingDays INT DEFAULT 0,"
                + "hoursWorked DECIMAL(6,2) DEFAULT 0,"
                + "baseSalary DECIMAL(15,2) DEFAULT 0,"
                + "allowance DECIMAL(15,2) DEFAULT 0,"
                + "bonus DECIMAL(15,2) DEFAULT 0,"
                + "taxDeduction DECIMAL(15,2) DEFAULT 0,"
                + "insuranceDeduction DECIMAL(15,2) DEFAULT 0,"
                + "netSalary DECIMAL(15,2) DEFAULT 0,"
                + "status TINYINT DEFAULT 0,"   // 0: Chờ duyệt, 1: Đã duyệt, 2: Đã chuyển khoản
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (employeeId) REFERENCES Employees(employeeId),"
                + "FOREIGN KEY (departmentId) REFERENCES Departments(departmentId)"
                + ")";
        execute(conn, SQL, "CREATE PAYROLL TABLE SUCCESSFULLY");
    }

    // Đánh giá năng lực (DanhGiaNangLuc)
    public void createTablePerformance(Connection conn) {
        String SQL = "CREATE TABLE Performance("
                + "performanceId INT PRIMARY KEY AUTO_INCREMENT,"
                + "departmentId INT NOT NULL,"
                + "employeeId INT NOT NULL,"     // người được đánh giá
                + "position NVARCHAR(150),"
                + "evaluatorId INT NOT NULL,"    // người đánh giá
                + "evaluationDate DATE NOT NULL,"
                + "content NVARCHAR(500),"
                + "result NVARCHAR(100),"        // Xuất sắc / Tốt / Trung bình / Yếu
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (departmentId) REFERENCES Departments(departmentId),"
                + "FOREIGN KEY (employeeId) REFERENCES Employees(employeeId),"
                + "FOREIGN KEY (evaluatorId) REFERENCES Employees(employeeId)"
                + ")";
        execute(conn, SQL, "CREATE PERFORMANCE TABLE SUCCESSFULLY");
    }

    public void initializeDatabase(boolean enforceReset) {
        try (Connection conn = dbContext.getConnection()) {
            if (conn == null) {
                LOGGER.severe("Cannot get database connection!");
                return;
            }

            
            String[] dropOrder = {
                "Performance",
                "Payroll",
                "Attendance",
                "Jobs",
                "Department_Role_Permissions",
                "User_Department_Roles",
                "Employees",
                "Candidates",
                "Users",
                "Permissions",
                "Departments",
                "Roles"
            };


            String[] createOrder = {
                "Roles",
                "Permissions",
                "Departments",
                "Users",
                "Candidates",
                "Employees",
                "User_Department_Roles",
                "Department_Role_Permissions",
                "Jobs",
                "Attendance",
                "Payroll",
                "Performance"
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
                        case "Roles":                      createTableRoles(conn);                     break;
                        case "Permissions":                createTablePermissions(conn);               break;
                        case "Departments":                createTableDepartments(conn);               break;
                        case "Users":                      createTableUsers(conn);                     break;
                        case "Candidates":                 createTableCandidates(conn);                break;
                        case "Employees":                  createTableEmployees(conn);                 break;
                        case "User_Department_Roles":      createTableUserDepartmentRoles(conn);       break;
                        case "Department_Role_Permissions":createTableDepartmentRolePermissions(conn); break;
                        case "Jobs":                       createTableJobs(conn);                      break;
                        case "Attendance":                 createTableAttendance(conn);                break;
                        case "Payroll":                    createTablePayroll(conn);                   break;
                        case "Performance":                createTablePerformance(conn);               break;
                        default: LOGGER.warning("Unknown table: " + table);                           break;
                    }
                }
            }

            LOGGER.info("Database initialized successfully!");

        } catch (SQLException e) {
            LOGGER.severe("Database initialization failed: " + e.getMessage());
        }
    }

    private void insertInitialData(Connection conn) {
        try {
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Cannot insert initial data", e);
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
            stmt.execute("DROP TABLE IF EXISTS " + tableName);
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
    
