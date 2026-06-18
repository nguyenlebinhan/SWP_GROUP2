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
                + "departmentId INT NULL,"          // NULL = chưa phân công phòng ban (gán sau qua assign-department)
                + "positionId INT NULL,"            // NULL = chưa phân công vị trí
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
                + "startDate DATE,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (userId) REFERENCES Users(userId),"
                + "FOREIGN KEY (departmentId) REFERENCES Departments(departmentId),"
                + "FOREIGN KEY (positionId) REFERENCES Positions(positionId),"
                + "FOREIGN KEY (managerId) REFERENCES Employees(employeeId)"
                + ")";
        execute(conn, SQL, "CREATE EMPLOYEES TABLE SUCCESSFULLY");
    }

    public void createTableEmploymentContracts(Connection conn) {
        String SQL = "CREATE TABLE Employment_Contracts("
                + "contractId INT PRIMARY KEY AUTO_INCREMENT,"
                + "contractCode VARCHAR(50) NOT NULL UNIQUE,"
                + "employeeId INT NOT NULL,"
                + "contractType VARCHAR(50) NOT NULL,"
                + "startDate DATE NOT NULL,"
                + "endDate DATE NULL,"
                + "salary DECIMAL(15,2) NOT NULL DEFAULT 0,"
                + "status TINYINT DEFAULT 1,"
                + "note NVARCHAR(500),"
                + "createdBy INT,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (employeeId) REFERENCES Employees(employeeId),"
                + "FOREIGN KEY (createdBy) REFERENCES Users(userId)"
                + ")";
        execute(conn, SQL, "CREATE EMPLOYMENT_CONTRACTS TABLE SUCCESSFULLY");
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

    public void createTableFormTypes(Connection conn) {
        String SQL = "CREATE TABLE Form_Types("
                + "formTypeId INT PRIMARY KEY AUTO_INCREMENT,"
                + "formTypeCode VARCHAR(50) NOT NULL UNIQUE,"
                + "formTypeName NVARCHAR(100) NOT NULL UNIQUE,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ")";
        execute(conn, SQL, "CREATE FORM_TYPES TABLE SUCCESSFULLY");
    }

    public void createTableFormRequests(Connection conn) {
        String SQL = "CREATE TABLE Form_Requests("
                + "formId INT PRIMARY KEY AUTO_INCREMENT,"
                + "formCode VARCHAR(50) NOT NULL UNIQUE,"
                + "employeeId INT NOT NULL,"
                + "formTypeId INT NOT NULL,"
                + "reason NVARCHAR(500),"
                + "startDate DATE NULL,"
                + "endDate DATE NULL,"
                + "totalDays DECIMAL(4,1) NULL,"
                + "usedDays DECIMAL(4,1) DEFAULT 0,"
                + "status TINYINT DEFAULT 0,"
                + "approverId INT,"
                + "approverNote NVARCHAR(255),"
                + "approvedAt TIMESTAMP NULL,"
                + "attachmentUrl VARCHAR(255) NULL,"
                + "attachmentName VARCHAR(255) NULL,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (employeeId) REFERENCES Employees(employeeId),"
                + "FOREIGN KEY (formTypeId) REFERENCES Form_Types(formTypeId),"
                + "FOREIGN KEY (approverId) REFERENCES Employees(employeeId)"
                + ")";
        execute(conn, SQL, "CREATE FORM_REQUESTS TABLE SUCCESSFULLY");
    }

    public void createTableOvertimeDetails(Connection conn) {
        String SQL = "CREATE TABLE Overtime_Details("
                + "formId INT PRIMARY KEY,"
                + "otDate DATE NOT NULL,"
                + "startTime TIME NOT NULL,"
                + "endTime TIME NOT NULL,"
                + "dayType TINYINT NOT NULL," // 1: Thường, 2: Cuối tuần, 3: Lễ
                + "FOREIGN KEY (formId) REFERENCES Form_Requests(formId) ON DELETE CASCADE"
                + ")";
        execute(conn, SQL, "CREATE OVERTIME_DETAILS TABLE SUCCESSFULLY");
    }

    public void createTableOvertimeAssignees(Connection conn) {
        String SQL = "CREATE TABLE Overtime_Assignees("
                + "formId INT NOT NULL,"
                + "employeeId INT NOT NULL,"
                + "PRIMARY KEY(formId, employeeId),"
                + "FOREIGN KEY (formId) REFERENCES Form_Requests(formId) ON DELETE CASCADE,"
                + "FOREIGN KEY (employeeId) REFERENCES Employees(employeeId)"
                + ")";
        execute(conn, SQL, "CREATE OVERTIME_ASSIGNEES TABLE SUCCESSFULLY");
    }

    //cache để giúp tính toán số ngày còn lại nhanh hơn
    public void createTableLeaveForm(Connection conn) {
        String SQL = "CREATE TABLE Leave_Form("
                + "leaveId INT PRIMARY KEY AUTO_INCREMENT,"
                + "employeeId INT NOT NULL,"
                + "formTypeId INT NOT NULL,"
                + "formId INT NOT NULL,"
                + "startDate DATE NOT NULL,"
                + "endDate DATE NOT NULL,"
                + "totalDays DECIMAL(4,1) NOT NULL,"
                + "usedDays DECIMAL(4,1) DEFAULT 0,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "UNIQUE KEY uq_balance (employeeId, leaveId, formTypeId),"
                + "FOREIGN KEY (employeeId) REFERENCES Employees(employeeId),"
                + "FOREIGN KEY (formId) REFERENCES Form_Requests(formId),"
                + "FOREIGN KEY (formTypeId) REFERENCES Form_Types(formTypeId)"
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
                + "totalRows INT DEFAULT 0,"
                + "importedRows INT DEFAULT 0,"
                + "failedRows INT DEFAULT 0,"
                + "errorFileUrl VARCHAR(255),"
                + "status TINYINT DEFAULT 0,"        // 0: Pending, 1: Imported, 2: Failed, 3: Partial
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
                + "employeeCode VARCHAR(50),"         
                + "fullName NVARCHAR(100),"           
                + "departmentId INT,"                
                + "departmentName NVARCHAR(100),"     
                + "workDate DATE NOT NULL,"
                + "timeIn TIME,"
                + "timeOut TIME,"
                + "hoursWorked DECIMAL(4,2),"
                + "isOvertime BIT DEFAULT 0,"
                + "otHoursWorked DECIMAL(4,2) DEFAULT 0,"
                + "attendanceStatus TINYINT DEFAULT 0," // 0: Đúng giờ, 1: Đi muộn, 2: Vắng mặt, 3: Không phép
                + "dayOff DATE,"
                + "workingDay DATE,"
                + "penalty DECIMAL(15,2) DEFAULT 0,"
                + "fileId INT NULL,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "UNIQUE KEY uq_att_emp_date (employeeId, workDate)," // chống trùng employee + ngày
                + "FOREIGN KEY (employeeId) REFERENCES Employees(employeeId),"
                + "FOREIGN KEY (departmentId) REFERENCES Departments(departmentId),"
                + "FOREIGN KEY (fileId) REFERENCES Uploaded_Files(fileId)"
                + ")";
        execute(conn, SQL, "CREATE ATTENDANCE TABLE SUCCESSFULLY");
    }


    public void createTableAttendanceImportRows(Connection conn) {
        String SQL = "CREATE TABLE Attendance_Import_Rows("
                + "importRowId INT PRIMARY KEY AUTO_INCREMENT,"
                + "fileId INT NOT NULL,"
                + "rowNumber INT NOT NULL,"
                + "employeeCode VARCHAR(50),"
                + "fullName NVARCHAR(100),"
                + "departmentName NVARCHAR(100),"
                + "workDate VARCHAR(50),"
                + "timeIn VARCHAR(20),"
                + "timeOut VARCHAR(20),"
                + "attendanceStatus VARCHAR(30),"
                + "note NVARCHAR(255),"
                + "validateStatus TINYINT DEFAULT 0," // 0: lỗi/bị từ chối, 1: hợp lệ đã merge vào Attendance
                + "errorMessage NVARCHAR(500),"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (fileId) REFERENCES Uploaded_Files(fileId)"
                + ")";
        execute(conn, SQL, "CREATE ATTENDANCE_IMPORT_ROWS TABLE SUCCESSFULLY");
    }

    // Lịch sử chỉnh sửa chấm công: ai sửa, sửa gì, lý do.
    public void createTableAttendanceAdjustmentHistory(Connection conn) {
        String SQL = "CREATE TABLE Attendance_Adjustment_History("
                + "adjustmentId INT PRIMARY KEY AUTO_INCREMENT,"
                + "attendanceId INT NOT NULL,"
                + "oldValue NVARCHAR(500),"
                + "newValue NVARCHAR(500),"
                + "reason NVARCHAR(500) NOT NULL,"
                + "updatedBy INT NOT NULL,"          // userId người sửa
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (attendanceId) REFERENCES Attendance(attendanceId),"
                + "FOREIGN KEY (updatedBy) REFERENCES Users(userId)"
                + ")";
        execute(conn, SQL, "CREATE ATTENDANCE_ADJUSTMENT_HISTORY TABLE SUCCESSFULLY");
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
                "Audit_Logs",
                "Notifications",
                "Performance",
                "Payroll",
                "Attendance_Adjustment_History",
                "Attendance_Import_Rows",
                "Attendance",
                "Uploaded_Files",
                "Leave_Form",
                "Overtime_Assignees",
                "Overtime_Details",
                "Form_Requests",
                "Form_Types",
                "Candidates",
                "Employees",
                "Users",
                "Department_Roles",
                "Role_Permissions",
                "Permissions",
                "Email_Templates",
                "Departments",
                "Positions",
                "Roles"
            };

            String[] createOrder = {
                "Roles",
                "Permissions",
                "Role_Permissions",
                "Email_Templates",
                "Positions",
                "Departments",
                "Department_Roles",
                "Users",
                "Employees",
                "Candidates",
                "Form_Types",
                "Form_Requests",
                "Overtime_Details",
                "Overtime_Assignees",
                "Leave_Form",
                "Uploaded_Files",
                "Attendance",
                "Attendance_Import_Rows",
                "Attendance_Adjustment_History",
                "Payroll",
                "Performance",
                "Notifications",
                "Audit_Logs"
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
                        case "Employees":         createTableEmployees(conn);         break;
                        case "Employment_Contracts": createTableEmploymentContracts(conn); break;
                        case "Candidates":        createTableCandidates(conn);        break;
                        case "Form_Types":       createTableFormTypes(conn);         break;
                        case "Form_Requests":    createTableFormRequests(conn);     break;
                        case "Overtime_Details": createTableOvertimeDetails(conn);  break;
                        case "Overtime_Assignees": createTableOvertimeAssignees(conn); break;
                        case "Leave_Form":     createTableLeaveForm(conn);      break;
                        case "Uploaded_Files":    createTableUploadedFiles(conn);     break;
                        case "Attendance":        createTableAttendance(conn);        break;
                        case "Attendance_Import_Rows":        createTableAttendanceImportRows(conn);        break;
                        case "Attendance_Adjustment_History": createTableAttendanceAdjustmentHistory(conn); break;
                        case "Payroll":           createTablePayroll(conn);           break;
                        case "Performance":       createTablePerformance(conn);       break;
                        case "Notifications":     createTableNotifications(conn);     break;
                        case "Audit_Logs":        createTableAuditLogs(conn);         break;
                        default: LOGGER.log(Level.WARNING,"Unknown table: {0}", table);     break;
                    }
                }
            }

            execute(conn, "SET FOREIGN_KEY_CHECKS=1", "ENABLE FK CHECKS AFTER CREATE");
            LOGGER.log(Level.INFO, "Đã kích hoạt lại toàn bộ kiểm tra khóa ngoại hệ thống.");

            insertInitialData(conn);
            LOGGER.log(Level.INFO,"Database initialized successfully!");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,"Database initialization failed: {0} ", e.getMessage());
        }
    }



    private void insertInitialData(Connection conn) {
        try {
            if (countRows(conn, "Roles") == 0) {
                LOGGER.info("Starting to seed initial data...");
                insertRole(conn, "SA",  "SystemAdmin",   "Người quản trị hệ thống và đưa ra các quyền hạn với user account");
                insertRole(conn, "BA",  "BusinessAdmin", "Người quản trị nghiệp vụ và công việc");
                insertRole(conn, "HM",  "HRManager",     "Quản lý phòng Nhân sự");
                insertRole(conn, "HE",  "HREmployee",    "Nhân viên phòng Nhân sự");
                insertRole(conn, "ITM", "ITManager",     "Quản lý phòng Công nghệ thông tin");
                insertRole(conn, "ITE", "ITEmployee",    "Nhân viên phòng Công nghệ thông tin");
                insertRole(conn, "FIM", "FIManager",     "Quản lý phòng Tài chính");
                insertRole(conn, "FIE", "FIEmployee",    "Nhân viên phòng Tài chính");
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
                insertPermission(conn, "ADD_EMPLOYMENT_CONTRACT", "Thêm hợp đồng lao động", "Quyền thêm hợp đồng lao động cho nhân viên");
                insertPermission(conn, "EDIT_DEPARTMENTS","Chỉnh sửa phòng ban",     "Quyền chỉnh sửa phòng ban ");
                insertPermission(conn, "ASSIGN_DEPARTMENT","Gán nhân viên vào phòng ban",     "Quyền gán nhân viên vào phòng ban");
                insertPermission(conn, "UNASSIGN_DEPARTMENT","Xóa gán phòng ban nhân viên",     "Quyền xóa gán nhân viên sang phòng ban khác");
                insertPermission(conn,"ADD_DEPARTMENT","Thêm phòng ban","Quyền thêm phòng ban");
                insertPermission(conn,"VIEW_ATTENDANCE","Xem chấm công","Quyền xem dữ liệu chấm công (Manager: theo phòng mình; Employee: của bản thân)");
                insertPermission(conn,"IMPORT_ATTENDANCE","Import chấm công","Quyền import dữ liệu chấm công từ file Excel");
                insertPermission(conn,"EDIT_ATTENDANCE","Chỉnh sửa chấm công","Quyền chỉnh sửa trạng thái chấm công khi kỳ chấm công chưa công khai");                
                insertPermission(conn,"VIEW_DEPARTMENT_EMPLOYEES_DETAIL","Xem danh sách nhân viên của phòng ban khác","Quyền xem dữ liệu nhân viên của phòng ban khác");
                insertPermission(conn,"VIEW_ALL_FORMS","Xem tất cả đơn","Quyền xem toàn bộ đơn yêu cầu của mọi phòng ban (chỉ HR)");
                insertPermission(conn,"VIEW_ALL_DEPT_FORMS","Xem tất cả đơn của phòng ban","Quyền xem toàn bộ đơn yêu cầu của một phòng ban cụ thể");
                
            }

            if (countRows(conn, "Positions") == 0) {
                insertPosition(conn, "Thực tập sinh",          1, "Sinh viên thực tập tại công ty");
                insertPosition(conn, "Nhân viên chính thức",   2, "Hỗ trợ công việc hành chính");
                insertPosition(conn, "Trưởng phòng ",   3, "Quản lý toàn bộ hoạt động của phòng ban");
            }


            if (countRows(conn, "Departments") == 0) {

                insertDepartment(conn, "IT",  "Phòng Công nghệ thông tin", "Phát triển và vận hành hệ thống phần mềm");
                insertDepartment(conn, "HR",  "Phòng Nhân sự",             "Tuyển dụng, đào tạo và quản lý nhân viên");
                insertDepartment(conn, "FI", "Phòng Tài chính",           "Quản lý ngân sách và kế toán");
            }

            if (countRows(conn, "Users") == 0) {
                // userId 1 = admin    (SA)
                insertUser(conn, "admin",    "admin@company.com",    BCrypt.withDefaults().hashToString(12, "admin123".toCharArray()),  "Nguyễn Lê Bình An", "2006-01-06", "Phủ Lý, Hà Nam", 1);
                // userId 2 = minhquan (BA)
                insertUser(conn, "minhquan", "minhquan@company.com", BCrypt.withDefaults().hashToString(12, "google123".toCharArray()), "Minh Quân",          "2000-01-01", "Hà Nội",          2);
                // userId 3 = vu       (SA)
                insertUser(conn, "vu",       "didoan482@gmail.com",  BCrypt.withDefaults().hashToString(12, "soss123".toCharArray()),   "Phạm Vũ",            "2006-10-17", "Thanh Hóa",       1);
                // userId 4 = mixi     (BA)
                insertUser(conn, "mixi",     "mixi@gmail.com",       BCrypt.withDefaults().hashToString(12, "misi".toCharArray()),      "Phung Thanh Do",     "2006-10-10", "Cao Bang",        2);
                // userId 5 = misi     (HRManager)
                insertUser(conn, "misi",     "ngng@gmail.com",       BCrypt.withDefaults().hashToString(12, "mixi".toCharArray()),      "Nguyen Nguyen",      "2006-10-10", "Cao Bang",        3);
                // userId 6 = it_mgr   (ITManager)
                insertUser(conn, "it_mgr",   "it.manager@company.com", BCrypt.withDefaults().hashToString(12, "123456".toCharArray()), "Trần Văn IT",        "1990-05-10", "Hà Nội",          5);
                // userId 7 = it_emp1  (ITEmployee)
                insertUser(conn, "it_emp1",  "it.emp1@company.com",    BCrypt.withDefaults().hashToString(12, "123456".toCharArray()), "Lê Thị IT",          "1995-03-15", "Hà Nội",          6);
                // userId 8 = it_emp2  (ITEmployee)
                insertUser(conn, "it_emp2",  "it.emp2@company.com",    BCrypt.withDefaults().hashToString(12, "123456".toCharArray()), "Phạm Văn Dev",       "1997-07-20", "Hà Nội",          6);
                // userId 9 = hr_mgr   (HRManager)
                insertUser(conn, "hr_mgr",   "hr.manager@company.com", BCrypt.withDefaults().hashToString(12, "123456".toCharArray()), "Nguyễn Thị HR",      "1988-11-01", "TP HCM",          3);
                // userId 10 = hr_emp1 (HREmployee)
                insertUser(conn, "hr_emp1",  "hr.emp1@company.com",    BCrypt.withDefaults().hashToString(12, "123456".toCharArray()), "Vũ Thị Nhân Sự",     "1993-06-25", "TP HCM",          4);
                // userId 11 = fi_mgr  (FIManager)
                insertUser(conn, "fi_mgr",   "fi.manager@company.com", BCrypt.withDefaults().hashToString(12, "123456".toCharArray()), "Hoàng Văn FI",       "1985-09-12", "Đà Nẵng",         7);
                // userId 12 = fi_emp1 (FIEmployee)
                insertUser(conn, "fi_emp1",  "fi.emp1@company.com",    BCrypt.withDefaults().hashToString(12, "123456".toCharArray()), "Đinh Thị Kế Toán",   "1996-02-18", "Đà Nẵng",         8);
            }

            if (countRows(conn, "Employees") == 0) {
                // departmentId: 1=IT, 2=HR, 3=FI | positionId: 1=Thực tập sinh, 2=Nhân viên, 3=Trưởng phòng
                // IT — userId 6,7,8
                insertEmployee(conn, "EMP001", 6,  1, 3, "0901000001", "Java, SQL, Spring Boot", "5 năm phát triển web",    "Kỹ sư CNTT");
                insertEmployee(conn, "EMP002", 7,  1, 2, "0901000002", "React, TypeScript",      "2 năm frontend",          "Cử nhân CNTT");
                insertEmployee(conn, "EMP003", 8,  1, 1, "0901000003", "DevOps, Docker",         "1 năm vận hành",          "Cử nhân CNTT");
                // HR — userId 9,10
                insertEmployee(conn, "EMP004", 9,  2, 3, "0901000004", "Tuyển dụng, HRIS",       "6 năm nhân sự",           "Cử nhân Quản trị nhân lực");
                insertEmployee(conn, "EMP005", 10, 2, 2, "0901000005", "Đào tạo, C&B",           "3 năm C&B",               "Cử nhân Kinh tế");
                // FI — userId 11,12
                insertEmployee(conn, "EMP006", 11, 3, 3, "0901000006", "Kế toán, MISA, Excel",   "8 năm kế toán tài chính", "Cử nhân Kế toán");
                insertEmployee(conn, "EMP007", 12, 3, 2, "0901000007", "Thuế, kiểm toán",        "2 năm tài chính",         "Cử nhân Tài chính");
            }

            if (countRows(conn, "Department_Roles") == 0) {
                // roleId: 3=HRManager, 4=HREmployee, 5=ITManager, 6=ITEmployee, 7=FIManager, 8=FIEmployee

                // Phòng IT (departmentId=1)
                insertDepartmentRole(conn, 1, 5); // ITManager
                insertDepartmentRole(conn, 1, 6); // ITEmployee

                // Phòng HR (departmentId=2)
                insertDepartmentRole(conn, 2, 3); // HRManager
                insertDepartmentRole(conn, 2, 4); // HREmployee

                // Phòng FI (departmentId=3)
                insertDepartmentRole(conn, 3, 7); // FIManager
                insertDepartmentRole(conn, 3, 8); // FIEmployee
            }

            if (countRows(conn, "Form_Types") == 0) {
                insertFormType(conn, "LEAVE",     "Nghỉ phép");
                insertFormType(conn, "COMPLAINT", "Khiếu nại");
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

    private void insertFormType(Connection conn, String code, String name) throws SQLException {
        String sql = "INSERT INTO Form_Types (formTypeCode, formTypeName) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setNString(2, name);
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

    private void insertDepartment(Connection conn, String code, String name, String description) throws SQLException {
    String sql = "INSERT INTO Departments (departmentCode, departmentName, description, managerId) VALUES (?, ?, ?, NULL)";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, code);
        ps.setNString(2, name);
        ps.setNString(3, description);
        ps.executeUpdate();
        }
    }

    private void insertDepartmentRole(Connection conn, int departmentId, int roleId) throws SQLException {
        String sql = "INSERT IGNORE INTO Department_Roles (departmentId, roleId) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            ps.setInt(2, roleId);
            ps.executeUpdate();
        }
    }

    private int insertEmployee(Connection conn, String code, int userId, int deptId, int posId,
                                String phone, String skills, String experience, String degree) throws SQLException {
        String sql = "INSERT INTO Employees (employeeCode, userId, departmentId, positionId, phoneNumber, skills, experience, degree) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setInt(2, userId);
            ps.setInt(3, deptId);
            ps.setInt(4, posId);
            ps.setString(5, phone);
            ps.setNString(6, skills);
            ps.setNString(7, experience);
            ps.setNString(8, degree);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }



    private void execute(Connection conn, String sql, String label) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.log(Level.INFO, "EXECUTED: {0}", label);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "CANNOT EXECUTE: " + label,
                    e);
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
