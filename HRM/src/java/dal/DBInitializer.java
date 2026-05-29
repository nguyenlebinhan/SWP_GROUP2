/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import at.favre.lib.crypto.bcrypt.BCrypt;
import dao.UserDAO;
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

    // ==================== BẢNG GỐC ====================

    public void createTableRoles(Connection conn) {
        String SQL = "CREATE TABLE Roles("
                + "roleId INT PRIMARY KEY AUTO_INCREMENT,"
                + "roleCode VARCHAR(100) NOT NULL UNIQUE,"
                + "roleName VARCHAR(50) NOT NULL UNIQUE,"
                + "description TEXT NULL,"
                + "isActive BIT DEFAULT 1,"
                + "isDeleted BIT DEFAULT 0,"
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

    public void createTableRolePermissions(Connection conn) {
        String SQL = "CREATE TABLE Role_Permissions("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "roleId INT NOT NULL,"
                + "permissionId INT NOT NULL,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (roleId) REFERENCES Roles(roleId),"
                + "FOREIGN KEY (permissionId) REFERENCES Permissions(permissionId)"
                + ")";
        execute(conn, SQL, "CREATE ROLE_PERMISSIONS TABLE SUCCESSFULLY");
    }

    public void createTableDepartmentRoles(Connection conn) {
        String SQL = "CREATE TABLE Department_Roles("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "departmentId INT NOT NULL,"
                + "roleId INT NOT NULL,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "UNIQUE KEY uq_dept_role (departmentId, roleId),"
                + "FOREIGN KEY (departmentId) REFERENCES Departments(departmentId),"
                + "FOREIGN KEY (roleId) REFERENCES Roles(roleId)"
                + ")";
        execute(conn, SQL, "CREATE DEPARTMENT_ROLES TABLE SUCCESSFULLY");
    }

    public void createTableEmailTemplates(Connection conn) {
        String SQL = "CREATE TABLE Email_Templates("
                + "templateId INT PRIMARY KEY AUTO_INCREMENT,"
                + "templateCode VARCHAR(50) NOT NULL UNIQUE,"
                + "subject NVARCHAR(200) NOT NULL,"
                + "body NVARCHAR(2000) NOT NULL,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ")";
        execute(conn, SQL, "CREATE EMAIL_TEMPLATES TABLE SUCCESSFULLY");
    }

    public void createTableUsers(Connection conn) {
        String SQL = "CREATE TABLE Users("
                + "userId INT PRIMARY KEY AUTO_INCREMENT,"
                + "username VARCHAR(100) NOT NULL UNIQUE,"
                + "email VARCHAR(50) NOT NULL UNIQUE,"
                + "password VARCHAR(150) NOT NULL,"
                + "fullName NVARCHAR(150) NOT NULL,"
                + "dob DATE,"
                + "gender VARCHAR(50),"
                + "address NVARCHAR(150),"
                + "roleId INT NOT NULL,"
                + "avatar VARCHAR(500),"
                + "isTemporaryPassword BIT DEFAULT 0,"
                + "isActive BIT DEFAULT 1,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (roleId) REFERENCES Roles(roleId)"
                + ")";
        execute(conn, SQL, "CREATE USERS TABLE SUCCESSFULLY");
    }

    public void createTablePosition(Connection conn) {
        String SQL = "CREATE TABLE Positions("
                + "positionId INT PRIMARY KEY AUTO_INCREMENT,"
                + "positionName VARCHAR(150) NOT NULL UNIQUE,"
                + "level INT NOT NULL,"
                + "description NVARCHAR(500)"
                + ")";
        execute(conn, SQL, "CREATE POSITIONS TABLE SUCCESSFULLY");
    }

    public void createTableDepartments(Connection conn) {
        String SQL = "CREATE TABLE Departments("
                + "departmentId INT PRIMARY KEY AUTO_INCREMENT,"
                + "departmentCode VARCHAR(50) NOT NULL UNIQUE,"
                + "departmentName NVARCHAR(150) NOT NULL UNIQUE,"
                + "description NVARCHAR(500),"
                + "managerId INT,"
//                + "maxHeadCount INT,"
                + "status TINYINT DEFAULT 1,"        // 0: Inactive, 1: Active
//                + "region NVARCHAR(100),"
//                + "budget DECIMAL(15,2),"
//                + "foundedDate DATE,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (managerId) REFERENCES Employees(employeeId)"
                + ")";
        execute(conn, SQL, "CREATE DEPARTMENTS TABLE SUCCESSFULLY");
    }



    public void createTableEmployees(Connection conn) {
        String SQL = "CREATE TABLE Employees("
                + "employeeId INT PRIMARY KEY AUTO_INCREMENT,"
                + "employeeCode VARCHAR(50) NOT NULL UNIQUE,"
                + "userId INT NOT NULL,"
                + "departmentId INT NOT NULL,"
                + "positionId INT NOT NULL,"
                + "phoneNumber VARCHAR(20),"
                + "skills NVARCHAR(255),"
                + "experience NVARCHAR(255),"
                + "degree NVARCHAR(100),"
//                + "hireDate DATE,"
//                + "probationEndDate DATE,"
                + "status TINYINT DEFAULT 1,"        // 0: Inactive, 1: Active, 2: On Leave
                + "managerId INT,"
//                + "nationalId VARCHAR(20),"          // CCCD/CMND
//                + "contractType VARCHAR(50),"        // Full-time, Part-time, Thử việc
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (userId) REFERENCES Users(userId),"
                + "FOREIGN KEY (departmentId) REFERENCES Departments(departmentId),"
                + "FOREIGN KEY (positionId) REFERENCES Positions(positionId),"
                + "FOREIGN KEY (managerId) REFERENCES Employees(employeeId)"
                + ")";
        execute(conn, SQL, "CREATE EMPLOYEES TABLE SUCCESSFULLY");
    }

    public void createTableCandidates(Connection conn) {
        String SQL = "CREATE TABLE Candidates("
                + "candidateId INT PRIMARY KEY AUTO_INCREMENT,"
                + "candidateCode VARCHAR(50) NOT NULL UNIQUE,"
                + "userId INT NOT NULL,"
                + "departmentId INT NOT NULL,"
                + "phoneNumber VARCHAR(20),"
                + "skills NVARCHAR(255),"
                + "experience NVARCHAR(255),"
                + "degree NVARCHAR(100),"
                + "positionId INT NOT NULL,"
                + "status TINYINT DEFAULT 0,"        // 0: Đang xét, 1: Phỏng vấn, 2: Thử việc, 3: Đậu, 4: Trượt
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (userId) REFERENCES Users(userId),"
                + "FOREIGN KEY (departmentId) REFERENCES Departments(departmentId),"
                + "FOREIGN KEY (positionId) REFERENCES Positions(positionId)"
                + ")";
        execute(conn, SQL, "CREATE CANDIDATES TABLE SUCCESSFULLY");
    }

    // ==================== NGHỈ PHÉP ====================

    public void createTableLeaveTypes(Connection conn) {
        String SQL = "CREATE TABLE Leave_Types("
                + "leaveTypeId INT PRIMARY KEY AUTO_INCREMENT,"
                + "leaveTypeCode VARCHAR(50) NOT NULL UNIQUE,"
                + "leaveTypeName NVARCHAR(100) NOT NULL UNIQUE,"
                + "maxDaysPerYear INT DEFAULT 12,"
                + "isPaid BIT DEFAULT 1,"            // 1: có lương, 0: không lương
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ")";
        execute(conn, SQL, "CREATE LEAVE_TYPES TABLE SUCCESSFULLY");
    }

    public void createTableLeaveRequests(Connection conn) {
        String SQL = "CREATE TABLE Leave_Requests("
                + "leaveId INT PRIMARY KEY AUTO_INCREMENT,"
                + "leaveCode VARCHAR(50) NOT NULL UNIQUE,"
                + "employeeId INT NOT NULL,"
                + "leaveTypeId INT NOT NULL,"
                + "startDate DATE NOT NULL,"
                + "endDate DATE NOT NULL,"
                + "totalDays DECIMAL(4,1) NOT NULL,"
                + "reason NVARCHAR(500),"
                + "status TINYINT DEFAULT 0,"        // 0: Chờ duyệt, 1: Đã duyệt, 2: Từ chối, 3: Đã hủy
                + "approverId INT,"
                + "approverNote NVARCHAR(255),"
                + "approvedAt TIMESTAMP NULL,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (employeeId) REFERENCES Employees(employeeId),"
                + "FOREIGN KEY (leaveTypeId) REFERENCES Leave_Types(leaveTypeId),"
                + "FOREIGN KEY (approverId) REFERENCES Employees(employeeId)"
                + ")";
        execute(conn, SQL, "CREATE LEAVE_REQUESTS TABLE SUCCESSFULLY");
    }

    //cache để giúp tính toán số ngày còn lại nhanh hơn
    public void createTableLeaveBalance(Connection conn) {
        String SQL = "CREATE TABLE Leave_Balance("
                + "balanceId INT PRIMARY KEY AUTO_INCREMENT,"
                + "employeeId INT NOT NULL,"
                + "leaveTypeId INT NOT NULL,"
                + "year INT NOT NULL,"
                + "totalDays DECIMAL(4,1) NOT NULL,"
                + "usedDays DECIMAL(4,1) DEFAULT 0,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "UNIQUE KEY uq_balance (employeeId, leaveTypeId, year),"
                + "FOREIGN KEY (employeeId) REFERENCES Employees(employeeId),"
                + "FOREIGN KEY (leaveTypeId) REFERENCES Leave_Types(leaveTypeId)"
                + ")";
        execute(conn, SQL, "CREATE LEAVE_BALANCE TABLE SUCCESSFULLY");
    }

    // ==================== CHẤM CÔNG ====================

    public void createTableUploadedFiles(Connection conn) {
        String SQL = "CREATE TABLE Uploaded_Files("
                + "fileId INT PRIMARY KEY AUTO_INCREMENT,"
                + "fileCode VARCHAR(50) NOT NULL UNIQUE,"
                + "fileType VARCHAR(20) NOT NULL,"   // 'ATTENDANCE', 'CANDIDATE', 'PAYROLL'
                + "departmentId INT NOT NULL,"
                + "employeeId INT,"
                + "fileUrl VARCHAR(255) NOT NULL,"
                + "fileName VARCHAR(255) NOT NULL,"
                + "month TINYINT NOT NULL,"
                + "year INT NOT NULL,"
                + "status TINYINT DEFAULT 0,"        // 0: Pending, 1: Approved, 2: Rejected
                + "totalRows INT DEFAULT 0,"
                + "importedRows INT DEFAULT 0,"
                + "failedRows INT DEFAULT 0,"
                + "errorFileUrl VARCHAR(255),"
                + "submittedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "reviewedBy INT,"
                + "reviewedAt TIMESTAMP NULL,"
                + "note NVARCHAR(255),"
                + "FOREIGN KEY (departmentId) REFERENCES Departments(departmentId),"
                + "FOREIGN KEY (employeeId) REFERENCES Employees(employeeId),"
                + "FOREIGN KEY (reviewedBy) REFERENCES Employees(employeeId)"
                + ")";
        execute(conn, SQL, "CREATE UPLOADED_FILES TABLE SUCCESSFULLY");
    }

    public void createTableAttendance(Connection conn) {
        String SQL = "CREATE TABLE Attendance("
                + "attendanceId INT PRIMARY KEY AUTO_INCREMENT,"
                + "attendanceCode VARCHAR(50) NOT NULL UNIQUE,"
                + "employeeId INT NOT NULL,"
                + "workDate DATE NOT NULL,"
                + "timeIn TIME,"
                + "timeOut TIME,"
                + "hoursWorked DECIMAL(4,2),"
                + "attendanceStatus TINYINT DEFAULT 0," // 0: Đúng giờ, 1: Đi muộn, 2: Vắng mặt, 3: Không phép
                + "dayOff DATE,"
                + "workingDay DATE,"
                + "penalty DECIMAL(15,2) DEFAULT 0,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (employeeId) REFERENCES Employees(employeeId)"
                + ")";
        execute(conn, SQL, "CREATE ATTENDANCE TABLE SUCCESSFULLY");
    }

    // ==================== LƯƠNG & ĐÁNH GIÁ ====================

    public void createTablePayroll(Connection conn) {
        String SQL = "CREATE TABLE Payroll("
                + "payrollId INT PRIMARY KEY AUTO_INCREMENT,"
                + "periodStart DATE NOT NULL,"
                + "employeeId INT NOT NULL,"
                + "positionId INT NOT NULL,"
                + "departmentId INT,"
                + "workingDays INT DEFAULT 0,"
                + "hoursWorked DECIMAL(6,2) DEFAULT 0,"
                + "baseSalary DECIMAL(15,2) DEFAULT 0,"
                + "allowance DECIMAL(15,2) DEFAULT 0,"
                + "bonus DECIMAL(15,2) DEFAULT 0,"
                + "taxDeduction DECIMAL(15,2) DEFAULT 0,"
                + "insuranceDeduction DECIMAL(15,2) DEFAULT 0,"
                + "netSalary DECIMAL(15,2) DEFAULT 0,"
                + "status TINYINT DEFAULT 0,"        // 0: Chờ duyệt, 1: Đã duyệt, 2: Đã chuyển khoản
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (employeeId) REFERENCES Employees(employeeId),"
                + "FOREIGN KEY (departmentId) REFERENCES Departments(departmentId),"
                + "FOREIGN KEY (positionId) REFERENCES Positions(positionId)"
                + ")";
        execute(conn, SQL, "CREATE PAYROLL TABLE SUCCESSFULLY");
    }

    public void createTablePerformance(Connection conn) {
        String SQL = "CREATE TABLE Performance("
                + "performanceId INT PRIMARY KEY AUTO_INCREMENT,"
                + "departmentId INT NOT NULL,"
                + "employeeId INT NOT NULL,"
                + "positionId INT NOT NULL,"
                + "evaluatorId INT NOT NULL,"
                + "evaluationDate DATE NOT NULL,"
                + "content NVARCHAR(500),"
                + "result NVARCHAR(100),"            // Xuất sắc / Tốt / Trung bình / Yếu
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (departmentId) REFERENCES Departments(departmentId),"
                + "FOREIGN KEY (employeeId) REFERENCES Employees(employeeId),"
                + "FOREIGN KEY (evaluatorId) REFERENCES Employees(employeeId),"
                + "FOREIGN KEY (positionId) REFERENCES Positions(positionId)"
                + ")";
        execute(conn, SQL, "CREATE PERFORMANCE TABLE SUCCESSFULLY");
    }

    // ==================== THÔNG BÁO & AUDIT ====================

    public void createTableNotifications(Connection conn) {
        String SQL = "CREATE TABLE Notifications("
                + "notificationId INT PRIMARY KEY AUTO_INCREMENT,"
                + "userId INT NOT NULL,"
                + "title NVARCHAR(200),"
                + "content NVARCHAR(500),"
                + "type VARCHAR(50),"               // 'LEAVE', 'SALARY', 'TASK', 'ATTENDANCE'
                + "referenceId INT,"
                + "referenceType VARCHAR(50),"
                + "isRead BIT DEFAULT 0,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (userId) REFERENCES Users(userId)"
                + ")";
        execute(conn, SQL, "CREATE NOTIFICATIONS TABLE SUCCESSFULLY");
    }

    public void createTableAuditLogs(Connection conn) {
        String SQL = "CREATE TABLE Audit_Logs("
                + "logId INT PRIMARY KEY AUTO_INCREMENT,"
                + "userId INT,"
                + "action VARCHAR(50) NOT NULL,"     // 'CREATE', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT'
                + "tableName VARCHAR(50),"
                + "recordId INT,"
                + "oldValue NVARCHAR(1000),"
                + "newValue NVARCHAR(1000),"
                + "ipAddress VARCHAR(45),"
                + "userAgent NVARCHAR(255),"
                + "status VARCHAR(10) DEFAULT 'SUCCESS',"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (userId) REFERENCES Users(userId)"
                + ")";
        execute(conn, SQL, "CREATE AUDIT_LOGS TABLE SUCCESSFULLY");
    }

    // ==================== KHỞI TẠO ====================

    public void initializeDatabase(boolean enforceReset) {
        try (Connection conn = dbContext.getConnection()) {
            if (conn == null) {
                LOGGER.log(Level.SEVERE,"Cannot get database connection!");
                return;
            }

            String[] dropOrder = {
                "Audit_Logs", "Notifications", "Performance", "Payroll", "Attendance",
                "Uploaded_Files", "Leave_Balance", "Leave_Requests", "Leave_Types", "Candidates", "Employees",
                "Users", "Department_Roles", "Role_Permissions", "Permissions", "Email_Templates", "Departments",
                "Positions", "Roles"
            };

            String[] createOrder = {
                "Roles", "Permissions", "Role_Permissions", "Email_Templates", "Positions",
                "Departments", "Department_Roles", "Users", "Employees", "Candidates", "Leave_Types",
                "Leave_Requests", "Leave_Balance", "Uploaded_Files", "Attendance", "Payroll", "Performance", 
                "Notifications", "Audit_Logs"
            };

            if (enforceReset) {
                LOGGER.log(Level.INFO,"Enforce reset: Dropping all tables...");
                execute(conn, "SET FOREIGN_KEY_CHECKS=0", "DISABLE FK CHECKS FOR DROP");
                for (String table : dropOrder) {
                    dropTable(conn, table);
                }
                execute(conn, "SET FOREIGN_KEY_CHECKS=1", "ENABLE FK CHECKS AFTER DROP");
            }

            // --- BẮT ĐẦU ĐOẠN SỬA ĐỔI QUAN TRỌNG CHẮN LỖI TẠO BẢNG CHÉO ---
            LOGGER.log(Level.INFO, "Tạm thời tắt kiểm tra khóa ngoại để tiến hành tạo bảng tuần tự...");
            execute(conn, "SET FOREIGN_KEY_CHECKS=0", "DISABLE FK CHECKS FOR CREATE");

            for (String table : createOrder) {
                if (enforceReset || !tableExists(conn, table)) {
                    switch (table) {
                        case "Roles":             createTableRoles(conn);             break;
                        case "Permissions":       createTablePermissions(conn);       break;
                        case "Role_Permissions":  createTableRolePermissions(conn);   break;
                        case "Email_Templates":   createTableEmailTemplates(conn);    break;
                        case "Positions":         createTablePosition(conn);          break;
                        case "Departments":       createTableDepartments(conn);       break;
                        case "Department_Roles":  createTableDepartmentRoles(conn);   break;
                        case "Users":             createTableUsers(conn);             break;
                        case "Employees":         createTableEmployees(conn);         break; // Đã thêm break bị thiếu ở đây
                        case "Candidates":        createTableCandidates(conn);        break;
                        case "Leave_Types":       createTableLeaveTypes(conn);        break;
                        case "Leave_Requests":    createTableLeaveRequests(conn);     break;
                        case "Leave_Balance":     createTableLeaveBalance(conn);      break;
                        case "Uploaded_Files":    createTableUploadedFiles(conn);     break;
                        case "Attendance":        createTableAttendance(conn);        break;
                        case "Payroll":           createTablePayroll(conn);           break;
                        case "Performance":       createTablePerformance(conn);       break;
                        case "Notifications":     createTableNotifications(conn);     break;
                        case "Audit_Logs":        createTableAuditLogs(conn);         break;
                        default: LOGGER.log(Level.WARNING,"Unknown table: {0}", table);     break;
                    }
                }
            }

            // Sau khi tất cả các bảng đã được tạo dựng thành công, bật lại ràng buộc để bảo vệ dữ liệu toàn vẹn
            execute(conn, "SET FOREIGN_KEY_CHECKS=1", "ENABLE FK CHECKS AFTER CREATE");
            LOGGER.log(Level.INFO, "Đã kích hoạt lại toàn bộ kiểm tra khóa ngoại hệ thống.");
            // --- KẾT THÚC ĐOẠN SỬA ĐỔI ---

            ensureUsersUsernameColumn(conn);
            insertInitialData(conn);
            LOGGER.log(Level.INFO,"Database initialized successfully!");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,"Database initialization failed: {0} ", e.getMessage());
        }
    }

    private void insertInitialData(Connection conn) {
        UserDAO userDao = new UserDAO();
        try {
            if (countRows(conn, "Roles") == 0) {
                LOGGER.info("Starting to seed initial data...");
                insertRole(conn, "AD", "Admin","Người quản trị hệ thống và đưa ra các quyền hạn với user account");
                insertRole(conn, "HM", "HRManager", "Người tuyển nhận sự ");
                insertRole(conn, "HE", "HREmployee","Người làm nhân sự ");

            }
            if (countRows(conn, "Users") == 0) {
                insertUser(conn, "admin", "nguyenlebinhank63@gmail.com",BCrypt.withDefaults().hashToString(12, "admin123".toCharArray()), "Nguyễn Lê Bình An", "2006-01-06", "Phủ Lý, Hà Nam", 1);
                insertUser(conn,"minhquan","minhquan153452@gmail.com",BCrypt.withDefaults().hashToString(12, "google123".toCharArray()),"Minh Quân","2006-01-01","Hà Nội",1);
                insertUser(conn, "vu", "didoan482@gmail.com", BCrypt.withDefaults().hashToString(12, "soss123".toCharArray()), "Phạm Vũ", "2006-10-17", "Thanh Hóa", 1);
                insertUser(conn, "manager", "vu2006124@gmail.com", BCrypt.withDefaults().hashToString(12, "manager123".toCharArray()), "MnM", "2006-10-10", "Ohio", 2);
                insertUser(conn,"annguyen123","nguyenlebinhank69@gmail.com",BCrypt.withDefaults().hashToString(12, "nguyenlebinhank63CBH".toCharArray()),"Nguyễn Mèo","2006-06-01","America",3);
            }
            if (countRows(conn, "Permissions") == 0) {
                insertPermission(conn, "VIEW_USERS",       "Xem người dùng",         "Quyền xem danh sách và chi tiết người dùng");
                insertPermission(conn, "ADD_USER",         "Thêm người dùng",         "Quyền thêm mới người dùng");
                insertPermission(conn, "EDIT_USER",        "Sửa người dùng",          "Quyền chỉnh sửa thông tin người dùng");
                insertPermission(conn, "DELETE_USER",      "Xóa người dùng",          "Quyền xóa / vô hiệu hóa người dùng");
                insertPermission(conn, "VIEW_ROLES",       "Xem vai trò",             "Quyền xem danh sách và chi tiết vai trò");
                insertPermission(conn, "ADD_ROLE",         "Thêm vai trò",            "Quyền thêm mới vai trò");
                insertPermission(conn, "EDIT_ROLE",        "Sửa vai trò",             "Quyền chỉnh sửa thông tin vai trò");
                insertPermission(conn, "DELETE_ROLE",      "Xóa vai trò",             "Quyền xóa vai trò");
                insertPermission(conn, "MANAGE_PERMISSIONS","Quản lý phân quyền",     "Quyền gán / thu hồi quyền cho vai trò");
                insertPermission(conn, "VIEW_EMPLOYEES",   "Xem nhân viên",              "Quyền xem danh sách nhân viên");   
                insertPermission(conn, "ADD_EMPLOYEE","Thêm nhân viên",     "Quyền thêm nhân viên");   
                insertPermission(conn, "EDIT_EMPLOYEE","Chỉnh sửa nhân viên",     "Quyền chỉnh sửa nhân viên");   
                insertPermission(conn, "VIEW_DEPARTMENTS","Xem phòng ban",     "Quyền xem phòng ban");   
                insertPermission(conn, "EDIT_DEPARTMENTS","Chỉnh sửa phòng ban",     "Quyền chỉnh sửa phòng ban ");   
                insertPermission(conn, "ASSIGN_DEPARTMENT","Gán nhân viên vào phòng ban",     "Quyền gán nhân viên vào phòng ban");                  
                insertPermission(conn,"ADD_DEPARTMENT","Thêm phòng ban","Quyền thêm phòng ban");
                insertPermission(conn,"VIEW_ATTENDANCE","Xem chấm công","Quyền xem dữ liệu chấm công (Manager: theo phòng mình; Employee: của bản thân)");
            }

            if (countRows(conn, "Positions") == 0) {

                // --- Chung ---
                insertPosition(conn, "Thực tập sinh",          1, "Sinh viên thực tập tại công ty");
                insertPosition(conn, "Nhân viên chính thức",   2, "Hỗ trợ công việc hành chính");
                insertPosition(conn, "Trưởng phòng ",   3, "Quản lý toàn bộ hoạt động của phòng ban");


            }
            
            LOGGER.log(Level.INFO,"Seeding completed successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot insert initial data", e);
        }
    }

    private void insertPermission(Connection conn, String code, String name, String description) throws SQLException {
        String sql = "INSERT INTO Permissions (permissionCode, permissionName, description) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setNString(3, description);
            ps.executeUpdate();
        }
    }

    private void insertPosition(Connection conn, String name, int level, String description) throws SQLException {
        String sql = "INSERT INTO Positions (positionName, level, description) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, name);
            ps.setInt(2, level);
            ps.setNString(3, description);
            ps.executeUpdate();
        }
    }


    private void insertRole(Connection conn, String code, String name,String description) throws SQLException {
        String sql = "INSERT INTO Roles (roleCode, roleName, description) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setString(3, description);
            ps.executeUpdate();
        }
    }

    private int insertUser(Connection conn, String username, String e, String p, String fn, String d, String a, int r) throws SQLException {
        String sql = "INSERT INTO Users (username, email, password, fullName, dob, address, roleId) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username); ps.setString(2, e); ps.setString(3, p); ps.setString(4, fn); ps.setDate(5, java.sql.Date.valueOf(d)); ps.setString(6, a); ps.setInt(7, r);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }



    private void ensureUsersUsernameColumn(Connection conn) {
        try {
            if (tableExists(conn, "Users") && !columnExists(conn, "Users", "username")) {
                execute(conn, "ALTER TABLE Users ADD COLUMN username VARCHAR(100) NULL AFTER userId", "ADD USERS.USERNAME COLUMN");
                execute(conn, "UPDATE Users SET username = SUBSTRING_INDEX(email, '@', 1) WHERE username IS NULL OR username = ''", "BACKFILL USERS.USERNAME");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not ensure Users.username column: {0}", e.getMessage());
        }
    }

    private boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    private void execute(Connection conn, String sql, String label) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.log(Level.INFO, "EXECUTED: {0}", label);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "CANNOT EXECUTE: {0}", label);
        }
    }

    private void dropTable(Connection conn, String tableName) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS " + tableName);
            LOGGER.log(Level.INFO,"DROPPED TABLE:{0} ", tableName);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,"COULD NOT DROP {0} : {1}" ,new Object[]{tableName,e.getMessage()});
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
