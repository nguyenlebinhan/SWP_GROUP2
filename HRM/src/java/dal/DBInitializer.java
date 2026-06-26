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
import java.util.Arrays;
import java.util.List;
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
                + "stage VARCHAR(20) DEFAULT 'APPLIED',"
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
                + "UNIQUE KEY uq_role_permission (roleId, permissionId),"
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
                + "maxHeadCount INT,"
                + "status TINYINT DEFAULT 1," // 0: Inactive, 1: Active
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
                + "departmentId INT NULL," // NULL = chưa phân công phòng ban (gán sau qua assign-department)
                + "positionId INT NULL," // NULL = chưa phân công vị trí
                + "phoneNumber VARCHAR(20),"
                + "skills NVARCHAR(255),"
                + "experience NVARCHAR(255),"
                + "degree NVARCHAR(100),"
                + "status TINYINT DEFAULT 1," // 0: Inactive, 1: Active, 2: On Leave
                + "managerId INT,"
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
                + "signedDate DATE,"
                + "effectiveDate DATE NOT NULL,"
                + "endDate DATE NULL,"
                + "actualEndDate DATE NULL,"
                + "salary DECIMAL(15,2) NOT NULL DEFAULT 0,"
                + "status VARCHAR(50) DEFAULT 'DRAFT',"
                + "note NVARCHAR(500),"
                + "previousContractId INT,"
                + "terminationReason NVARCHAR(255),"
                + "rejectionReason NVARCHAR(255),"
                + "createdBy INT,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (employeeId) REFERENCES Employees(employeeId),"
                + "FOREIGN KEY (previousContractId) REFERENCES Employment_Contracts(contractId),"
                + "FOREIGN KEY (createdBy) REFERENCES Users(userId)"
                + ")";
        execute(conn, SQL, "CREATE EMPLOYMENT_CONTRACTS TABLE SUCCESSFULLY");
    }

    public void createTableContractAuditLog(Connection conn) {
        String SQL = "CREATE TABLE Contract_Audit_Log("
                + "logId INT PRIMARY KEY AUTO_INCREMENT,"
                + "contractId INT NOT NULL,"
                + "oldStatus VARCHAR(50),"
                + "newStatus VARCHAR(50),"
                + "changedBy INT NOT NULL,"
                + "changeDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "actionReason NVARCHAR(255),"
                + "FOREIGN KEY (contractId) REFERENCES Employment_Contracts(contractId),"
                + "FOREIGN KEY (changedBy) REFERENCES Users(userId)"
                + ")";
        execute(conn, SQL, "CREATE CONTRACT_AUDIT_LOG TABLE SUCCESSFULLY");
    }

    public void createTableCandidates(Connection conn) {
        String SQL = "CREATE TABLE Candidates("
                + "candidateId INT PRIMARY KEY AUTO_INCREMENT,"
                + "candidateCode VARCHAR(50) NOT NULL UNIQUE,"
                + "fullName NVARCHAR(150) NOT NULL,"
                + "email VARCHAR(100) NOT NULL,"
                + "phoneNumber VARCHAR(20),"
                + "dateOfBirth DATE,"
                + "gender VARCHAR(20),"
                + "address NVARCHAR(255),"
                + "skills NVARCHAR(500),"
                + "experience NVARCHAR(500),"
                + "certificates NVARCHAR(255),"
                + "degree NVARCHAR(100),"
                + "cvFileUrl VARCHAR(500),"
                + "departmentId INT NOT NULL,"
                + "positionId INT NOT NULL,"
                + "importFileId INT,"
                + "status TINYINT DEFAULT 0," // 0: Đang xét, 1: Phỏng vấn, 2: Thử việc, 3: Đậu, 4: Trượt
                + "status TINYINT DEFAULT 0," // 0: Đang xét, 1: Phỏng vấn, 2: Thử việc, 3: Đậu, 4: Trượt
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (departmentId) REFERENCES Departments(departmentId),"
                + "FOREIGN KEY (positionId) REFERENCES Positions(positionId),"
                + "FOREIGN KEY (importFileId) REFERENCES Uploaded_Files(fileId)"
                + ")";
        execute(conn, SQL, "CREATE CANDIDATES TABLE SUCCESSFULLY");
    }

    // ==================== NGHỈ PHÉP ====================
    public void createTableApplicationStageLogs(Connection conn) {
        String SQL = "CREATE TABLE Application_Stage_Logs("
                + "logId INT PRIMARY KEY AUTO_INCREMENT,"
                + "candidateId INT NOT NULL,"
                + "fromStage VARCHAR(20),"
                + "toStage VARCHAR(20) NOT NULL,"
                + "result VARCHAR(20) NOT NULL,"
                + "reviewedBy INT NOT NULL,"
                + "reviewedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "note NVARCHAR(500),"
                + "toEmail VARCHAR(100) NOT NULL,"
                + "emailSubject NVARCHAR(200),"
                + "emailBody NVARCHAR(3000),"
                + "emailType VARCHAR(30),"
                + "emailStatus VARCHAR(20) DEFAULT 'PENDING',"
                + "sentAt TIMESTAMP NULL,"
                + "FOREIGN KEY (candidateId) REFERENCES Candidates(candidateId),"
                + "FOREIGN KEY (reviewedBy) REFERENCES Employees(employeeId)"
                + ")";
        execute(conn, SQL, "CREATE APPLICATION_STAGE_LOGS TABLE SUCCESSFULLY");
    }

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
                + "startTime TIME NULL,"
                + "endTime TIME NULL,"
                + "totalDays INT NULL,"
                + "usedDays INT DEFAULT 0,"
                + "status TINYINT DEFAULT 0,"
                + "approverId INT,"
                + "approverNote NVARCHAR(255),"
                + "approvedAt TIMESTAMP NULL,"
                + "attachmentUrl VARCHAR(255) NULL,"
                + "attachmentName VARCHAR(255) NULL,"
                + "targetDepartmentId INT NULL,"
                + "targetRoleId INT NULL,"
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

    public void createTableLeaveBalances(Connection conn) {
        String SQL = "CREATE TABLE Leave_Balances("
                + "balanceId INT PRIMARY KEY AUTO_INCREMENT,"
                + "employeeId INT NOT NULL,"
                + "year INT NOT NULL,"
                + "totalAllowed INT DEFAULT 12,"
                + "usedDays INT DEFAULT 0,"
                + "UNIQUE KEY uq_balance (employeeId, year),"
                + "FOREIGN KEY (employeeId) REFERENCES Employees(employeeId)"
                + ")";
        execute(conn, SQL, "CREATE LEAVE_BALANCES TABLE SUCCESSFULLY");
    }
    // ==================== CHẤM CÔNG ====================

    public void createTableUploadedFiles(Connection conn) {
        String SQL = "CREATE TABLE Uploaded_Files("
                + "fileId INT PRIMARY KEY AUTO_INCREMENT,"
                + "fileCode VARCHAR(50) NOT NULL UNIQUE,"
                + "fileType VARCHAR(20) NOT NULL," // 'ATTENDANCE', 'CANDIDATE', 'PAYROLL'
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
                + "status TINYINT DEFAULT 0," // 0: Pending, 1: Imported, 2: Failed, 3: Partial
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
                + "positionId INT,"
                + "positionName VARCHAR(100),"
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

    public void createTableAttendanceAdjustmentHistory(Connection conn) {
        String SQL = "CREATE TABLE Attendance_Adjustment_History("
                + "adjustmentId INT PRIMARY KEY AUTO_INCREMENT,"
                + "attendanceId INT NOT NULL,"
                + "oldValue NVARCHAR(500),"
                + "newValue NVARCHAR(500),"
                + "reason NVARCHAR(500) NOT NULL,"
                + "updatedBy INT NOT NULL," // userId người sửa
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (attendanceId) REFERENCES Attendance(attendanceId),"
                + "FOREIGN KEY (updatedBy) REFERENCES Users(userId)"
                + ")";
        execute(conn, SQL, "CREATE ATTENDANCE_ADJUSTMENT_HISTORY TABLE SUCCESSFULLY");
    }

    public void createTableHoliday(Connection conn) {
        String SQL = "CREATE TABLE Holiday("
                + "holidayId INT PRIMARY KEY AUTO_INCREMENT,"
                + "holidayName NVARCHAR(255) NOT NULL,"
                + "startDate DATE NOT NULL,"
                + "endDate DATE NOT NULL,"
                + "isActive TINYINT(1) NOT NULL DEFAULT 1,"
                + "INDEX idx_holiday_range (startDate, endDate)"
                + ")";
        execute(conn, SQL, "CREATE HOLIDAY TABLE SUCCESSFULLY");
    }

    public void createTablePayroll(Connection conn) {
        String SQL = "CREATE TABLE Payroll("
                + "payrollId INT PRIMARY KEY AUTO_INCREMENT,"
                + "periodStart DATE NOT NULL,"
                + "periodEnd DATE NOT NULL,"
                + "employeeId INT NOT NULL,"
                + "positionId INT NOT NULL,"
                + "departmentId INT,"
                + "workingDays INT DEFAULT 0,"
                + "hoursWorked DECIMAL(6,2) DEFAULT 0,"
                + "baseSalary DECIMAL(15,2) DEFAULT 0,"
                + "allowance DECIMAL(15,2) DEFAULT 0,"
                + "bonus DECIMAL(15,2) DEFAULT 0,"
                + "overtimePay DECIMAL(15,2) DEFAULT 0,"
                + "penalty DECIMAL(15,2) DEFAULT 0,"
                + "grossSalary DECIMAL(15,2) DEFAULT 0,"
                + "insuranceDeduction DECIMAL(15,2) DEFAULT 0,"
                + "personalIncomeTax DECIMAL(15,2) DEFAULT 0,"
                + "netSalary DECIMAL(15,2) DEFAULT 0,"
                + "note NVARCHAR(1000),"
                + "approvedBy INT,"
                + "approvedAt DATETIME,"
                + "status TINYINT DEFAULT 0," // 0: Chờ nhân viên xác nhận, 1: HR duyệt
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (employeeId) REFERENCES Employees(employeeId),"
                + "FOREIGN KEY (departmentId) REFERENCES Departments(departmentId),"
                + "FOREIGN KEY (positionId) REFERENCES Positions(positionId),"
                + "FOREIGN KEY (approvedBy) REFERENCES Users(userId)"
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
                + "result NVARCHAR(100)," // Xuất sắc / Tốt / Trung bình / Yếu
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
                + "type VARCHAR(50)," // 'LEAVE', 'SALARY', 'TASK', 'ATTENDANCE'
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
                + "action VARCHAR(50) NOT NULL," // 'CREATE', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT'
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
                LOGGER.log(Level.SEVERE, "Cannot get database connection!");
                return;
            }

            String[] dropOrder = {
                "Audit_Logs",
                "Notifications",
                "Performance",
                "Payroll",
                "Attendance_Adjustment_History",
                "Attendance",
                "Holiday",
                "Application_Stage_Logs",
                "Candidates",
                "Uploaded_Files",
                "Leave_Balances",
                "Overtime_Assignees",
                "Overtime_Details",
                "Form_Requests",
                "Form_Types",
                "Contract_Audit_Log",
                "Employment_Contracts",
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
                "Employment_Contracts",
                "Contract_Audit_Log",
                "Uploaded_Files",
                "Candidates",
                "Application_Stage_Logs",
                "Form_Types",
                "Form_Requests",
                "Overtime_Details",
                "Overtime_Assignees",
                "Leave_Balances",
                "Attendance",
                "Attendance_Adjustment_History",
                "Holiday",
                "Payroll",
                "Performance",
                "Notifications",
                "Audit_Logs"
            };

            if (enforceReset) {
                LOGGER.log(Level.INFO, "Enforce reset: Dropping all tables...");
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
                        case "Roles":
                            createTableRoles(conn);
                            break;
                        case "Permissions":
                            createTablePermissions(conn);
                            break;
                        case "Role_Permissions":
                            createTableRolePermissions(conn);
                            break;
                        case "Email_Templates":
                            createTableEmailTemplates(conn);
                            break;
                        case "Positions":
                            createTablePosition(conn);
                            break;
                        case "Departments":
                            createTableDepartments(conn);
                            break;
                        case "Department_Roles":
                            createTableDepartmentRoles(conn);
                            break;
                        case "Users":
                            createTableUsers(conn);
                            break;
                        case "Employees":
                            createTableEmployees(conn);
                            break;
                        case "Employment_Contracts":
                            createTableEmploymentContracts(conn);
                            break;
                        case "Contract_Audit_Log":
                            createTableContractAuditLog(conn);
                            break;
                        case "Uploaded_Files":
                            createTableUploadedFiles(conn);
                            break;
                        case "Candidates":
                            createTableCandidates(conn);
                            break;
                        case "Application_Stage_Logs":
                            createTableApplicationStageLogs(conn);
                            break;
                        case "Form_Types":
                            createTableFormTypes(conn);
                            break;
                        case "Form_Requests":
                            createTableFormRequests(conn);
                            break;
                        case "Overtime_Details":
                            createTableOvertimeDetails(conn);
                            break;
                        case "Overtime_Assignees":
                            createTableOvertimeAssignees(conn);
                            break;
                        case "Leave_Balances":
                            createTableLeaveBalances(conn);
                            break;
                        case "Attendance":
                            createTableAttendance(conn);
                            break;
                        case "Attendance_Adjustment_History":
                            createTableAttendanceAdjustmentHistory(conn);
                            break;
                        case "Holiday":
                            createTableHoliday(conn);
                            break;
                        case "Payroll":
                            createTablePayroll(conn);
                            break;
                        case "Performance":
                            createTablePerformance(conn);
                            break;
                        case "Notifications":
                            createTableNotifications(conn);
                            break;
                        case "Audit_Logs":
                            createTableAuditLogs(conn);
                            break;
                        default:
                            LOGGER.log(Level.WARNING, "Unknown table: {0}", table);
                            break;
                    }
                }
            }

            execute(conn, "SET FOREIGN_KEY_CHECKS=1", "ENABLE FK CHECKS AFTER CREATE");
            LOGGER.log(Level.INFO, "Đã kích hoạt lại toàn bộ kiểm tra khóa ngoại hệ thống.");

            ensureRolePermissionUniqueKey(conn);
            ensurePayrollApprovalColumns(conn);
            ensureFormRequestColumns(conn);
            insertInitialData(conn);
            LOGGER.log(Level.INFO, "Database initialized successfully!");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database initialization failed: {0} ", e.getMessage());
        }
    }

    private void insertInitialData(Connection conn) {
        try {
            if (countRows(conn, "Roles") == 0) {
                LOGGER.info("Starting to seed initial data...");
                insertRole(conn, "SA", "SystemAdmin", "Người quản trị hệ thống và đưa ra các quyền hạn với user account");
                insertRole(conn, "BA", "BusinessAdmin", "Người quản trị nghiệp vụ và công việc");
                insertRole(conn, "HM", "HRManager", "Quản lý phòng Nhân sự");
                insertRole(conn, "HE", "HREmployee", "Nhân viên phòng Nhân sự");
                insertRole(conn, "ITM", "ITManager", "Quản lý phòng Công nghệ thông tin");
                insertRole(conn, "ITE", "ITEmployee", "Nhân viên phòng Công nghệ thông tin");
                insertRole(conn, "FIM", "FIManager", "Quản lý phòng Tài chính");
                insertRole(conn, "FIE", "FIEmployee", "Nhân viên phòng Tài chính");
            }

            if (countRows(conn, "Permissions") == 0) {
                insertPermission(conn, "VIEW_USERS", "Xem người dùng", "Quyền xem danh sách và chi tiết người dùng");
                insertPermission(conn, "ADD_USER", "Thêm người dùng", "Quyền thêm mới người dùng");
                insertPermission(conn, "EDIT_USER", "Sửa người dùng", "Quyền chỉnh sửa thông tin người dùng");
                insertPermission(conn, "DELETE_USER", "Xóa người dùng", "Quyền xóa / vô hiệu hóa người dùng");
                insertPermission(conn, "VIEW_ROLES", "Xem vai trò", "Quyền xem danh sách và chi tiết vai trò");
                insertPermission(conn, "ADD_ROLE", "Thêm vai trò", "Quyền thêm mới vai trò");
                insertPermission(conn, "EDIT_ROLE", "Sửa vai trò", "Quyền chỉnh sửa thông tin vai trò");
                insertPermission(conn, "DELETE_ROLE", "Xóa vai trò", "Quyền xóa vai trò");
                insertPermission(conn, "MANAGE_PERMISSIONS", "Quản lý phân quyền", "Quyền gán / thu hồi quyền cho vai trò");
                insertPermission(conn, "VIEW_EMPLOYEES", "Xem nhân viên", "Quyền xem danh sách nhân viên");
                insertPermission(conn, "ADD_EMPLOYEE", "Thêm nhân viên", "Quyền thêm nhân viên");
                insertPermission(conn, "EDIT_EMPLOYEE", "Chỉnh sửa nhân viên", "Quyền chỉnh sửa nhân viên");
                insertPermission(conn, "ADD_EMPLOYMENT_CONTRACT", "Thêm hợp đồng lao động", "Quyền thêm hợp đồng lao động cho nhân viên");
                insertPermission(conn, "EDIT_DEPARTMENTS", "Chỉnh sửa phòng ban", "Quyền chỉnh sửa phòng ban ");
                insertPermission(conn, "ASSIGN_DEPARTMENT", "Gán nhân viên vào phòng ban", "Quyền gán nhân viên vào phòng ban");
                insertPermission(conn, "UNASSIGN_DEPARTMENT", "Xóa gán phòng ban nhân viên", "Quyền xóa gán nhân viên sang phòng ban khác");
                insertPermission(conn, "ADD_DEPARTMENT", "Thêm phòng ban", "Quyền thêm phòng ban");
                insertPermission(conn, "VIEW_ATTENDANCE", "Xem chấm công", "Quyền xem dữ liệu chấm công (Manager: theo phòng mình; Employee: của bản thân)");
                insertPermission(conn, "IMPORT_ATTENDANCE", "Import chấm công", "Quyền import dữ liệu chấm công từ file Excel");
                insertPermission(conn, "EDIT_ATTENDANCE", "Chỉnh sửa chấm công", "Quyền chỉnh sửa trạng thái chấm công khi kỳ chấm công chưa công khai");
                insertPermission(conn, "VIEW_DEPARTMENT_EMPLOYEES_DETAIL", "Xem danh sách nhân viên của phòng ban khác", "Quyền xem dữ liệu nhân viên của phòng ban khác");
                insertPermission(conn, "VIEW_ALL_FORMS", "Xem tất cả đơn", "Quyền xem toàn bộ đơn yêu cầu của mọi phòng ban (chỉ HR)");
                insertPermission(conn, "VIEW_ALL_DEPT_FORMS", "Xem tất cả đơn của phòng ban", "Quyền xem toàn bộ đơn yêu cầu của một phòng ban cụ thể");
                insertPermission(conn, "PROCESS_RECRUITMENT", "Xử lý tuyển dụng", "Quyền import, duyệt và gửi thông báo kết quả tuyển dụng");
                insertPermission(conn, "VIEW_CONTRACT_PREVIEW", "Xem hợp đồng", "Quyền xem hợp đồng hiện tại và lịch sử hợp đồng");
                insertPermission(conn, "PERM_APPROVE_CONTRACT", "Duyệt hợp đồng", "Quyền duyệt hợp đồng lao động");
                insertPermission(conn, "PERM_VIEW_ALL_CONTRACTS", "Xem toàn bộ hợp đồng", "Quyền xem toàn bộ lịch sử hợp đồng");
                insertPermission(conn, "EDIT_DEPARTMENTS", "Chỉnh sửa phòng ban", "Quyền chỉnh sửa phòng ban ");
                insertPermission(conn, "ASSIGN_DEPARTMENT", "Gán nhân viên vào phòng ban", "Quyền gán nhân viên vào phòng ban");
                insertPermission(conn, "UNASSIGN_DEPARTMENT", "Xóa gán phòng ban nhân viên", "Quyền xóa gán nhân viên sang phòng ban khác");
                insertPermission(conn, "ADD_DEPARTMENT", "Thêm phòng ban", "Quyền thêm phòng ban");
                insertPermission(conn, "VIEW_DEPARTMENT_ATTENDANCE", "Xem chấm công phòng ban", "Quyền xem dashboard chấm công của phòng ban mình quản lý (Manager)");
                insertPermission(conn, "VIEW_ALL_ATTENDANCE", "Xem toàn bộ chấm công", "Quyền xem dashboard chấm công của tất cả phòng ban trong toàn công ty (HR)");
                insertPermission(conn, "IMPORT_ATTENDANCE", "Import chấm công", "Quyền import dữ liệu chấm công từ file Excel");
                insertPermission(conn, "EDIT_ATTENDANCE", "Chỉnh sửa chấm công", "Quyền chỉnh sửa trạng thái chấm công khi kỳ chấm công chưa công khai");
                insertPermission(conn, "VIEW_DEPARTMENT_EMPLOYEES_DETAIL", "Xem danh sách nhân viên của phòng ban khác", "Quyền xem dữ liệu nhân viên của phòng ban khác");
                insertPermission(conn, "VIEW_ALL_FORMS", "Xem tất cả đơn", "Quyền xem toàn bộ đơn yêu cầu của mọi phòng ban (chỉ HR)");
                insertPermission(conn, "VIEW_ALL_DEPT_FORMS", "Xem tất cả đơn của phòng ban", "Quyền xem toàn bộ đơn yêu cầu của một phòng ban cụ thể");
                insertPermission(conn, "PROCESS_RECRUITMENT", "Xử lý tuyển dụng", "Quyền import, duyệt và gửi thông báo kết quả tuyển dụng");
            }
            ensurePermission(conn, "VIEW_USERS", "Xem người dùng", "Quyền xem danh sách và chi tiết người dùng");
            ensurePermission(conn, "ADD_USER", "Thêm người dùng", "Quyền thêm mới người dùng");
            ensurePermission(conn, "EDIT_USER", "Sửa người dùng", "Quyền chỉnh sửa thông tin người dùng");
            ensurePermission(conn, "DELETE_USER", "Xóa người dùng", "Quyền xóa / vô hiệu hóa người dùng");
            ensurePermission(conn, "VIEW_ROLES", "Xem vai trò", "Quyền xem danh sách và chi tiết vai trò");
            ensurePermission(conn, "ADD_ROLE", "Thêm vai trò", "Quyền thêm mới vai trò");
            ensurePermission(conn, "EDIT_ROLE", "Sửa vai trò", "Quyền chỉnh sửa thông tin vai trò");
            ensurePermission(conn, "DELETE_ROLE", "Xóa vai trò", "Quyền xóa vai trò");
            ensurePermission(conn, "MANAGE_PERMISSIONS", "Quản lý phân quyền", "Quyền gán / thu hồi quyền cho vai trò");
            ensurePermission(conn, "VIEW_EMPLOYEES", "Xem nhân viên", "Quyền xem danh sách nhân viên");
            ensurePermission(conn, "ADD_EMPLOYEE", "Thêm nhân viên", "Quyền thêm nhân viên");
            ensurePermission(conn, "EDIT_EMPLOYEE", "Chỉnh sửa nhân viên", "Quyền chỉnh sửa nhân viên");
            ensurePermission(conn, "ADD_EMPLOYMENT_CONTRACT", "Thêm hợp đồng lao động", "Quyền thêm hợp đồng lao động cho nhân viên");
            ensurePermission(conn, "APPROVE_CONTRACT", "Phê duyệt hợp đồng", "Quyền phê duyệt hợp đồng lao động");
            ensurePermission(conn, "REJECT_CONTRACT", "Từ chối hợp đồng", "Quyền từ chối hợp đồng lao động");
            ensurePermission(conn, "TERMINATE_CONTRACT", "Chấm dứt hợp đồng", "Quyền chấm dứt sớm hợp đồng đang hiệu lực");
            ensurePermission(conn, "VIEW_PENDING_CONTRACTS", "Xem hợp đồng chờ duyệt", "Quyền xem danh sách hợp đồng đang chờ duyệt");
            ensurePermission(conn, "VIEW_OWN_CONTRACT", "Xem hợp đồng của mình", "Quyền xem hợp đồng và lịch sử hợp đồng của chính mình");
            ensurePermission(conn, "VIEW_ALL_CONTRACTS", "Xem tất cả hợp đồng", "Quyền xem lịch sử hợp đồng của nhân viên");
            ensurePermission(conn, "EDIT_DEPARTMENTS", "Chỉnh sửa phòng ban", "Quyền chỉnh sửa phòng ban");
            ensurePermission(conn, "ASSIGN_DEPARTMENT", "Gán nhân viên vào phòng ban", "Quyền gán nhân viên vào phòng ban");
            ensurePermission(conn, "UNASSIGN_DEPARTMENT", "Xóa gán phòng ban nhân viên", "Quyền xóa gán nhân viên sang phòng ban khác");
            ensurePermission(conn, "ADD_DEPARTMENT", "Thêm phòng ban", "Quyền thêm phòng ban");
            ensurePermission(conn, "VIEW_DEPARTMENT_ATTENDANCE", "Xem chấm công phòng ban", "Quyền xem dashboard chấm công của phòng ban mình quản lý (Manager)");
            ensurePermission(conn, "VIEW_ALL_ATTENDANCE", "Xem toàn bộ chấm công", "Quyền xem dashboard chấm công của tất cả phòng ban trong toàn công ty (HR)");
            ensurePermission(conn, "IMPORT_ATTENDANCE", "Import chấm công", "Quyền import dữ liệu chấm công từ file Excel");
            ensurePermission(conn, "EDIT_ATTENDANCE", "Chỉnh sửa chấm công", "Quyền chỉnh sửa trạng thái chấm công khi tháng chấm công chưa công khai");
            ensurePermission(conn, "VIEW_DEPARTMENT_EMPLOYEES_DETAIL", "Xem danh sách nhân viên của phòng ban khác", "Quyền xem dữ liệu nhân viên của phòng ban khác");
            ensurePermission(conn, "VIEW_ALL_FORMS", "Xem tất cả đơn", "Quyền xem toàn bộ đơn yêu cầu của mọi phòng ban (chỉ HR)");
            ensurePermission(conn, "VIEW_ALL_DEPT_FORMS", "Xem tất cả đơn của phòng ban", "Quyền xem toàn bộ đơn yêu cầu của một phòng ban cụ thể");
            ensurePermission(conn, "PROCESS_RECRUITMENT", "Xử lý tuyển dụng", "Quyền import, duyệt và gửi thông báo kết quả tuyển dụng");
            ensurePermission(conn, "VIEW_ALL_SALARY", "Xem tất cả lương nhân viên", "Quyền xem lương của tất cả nhân viên");
            ensurePermission(conn, "VIEW_OWN_SALARY", "Xem lương cá nhân", "Quyền xem, gửi đơn khiếu nại về lương của cá nhân");
            ensurePermission(conn, "APPROVE_PAYROLL", "Duyệt bảng lương", "Quyền duyệt bảng lương trước khi thanh toán");
            ensurePermission(conn, "EXPORT_PAYROLL", "Xuất bảng lương", "Quyền xuất bảng lương ra Excel");

            if (countRows(conn, "Positions") == 0) {
                insertPosition(conn, "Thực tập sinh", 1, "Sinh viên thực tập tại công ty");
                insertPosition(conn, "Nhân viên chính thức", 2, "Hỗ trợ công việc hành chính");
                insertPosition(conn, "Trưởng phòng ", 3, "Quản lý toàn bộ hoạt động của phòng ban");
            }

            if (countRows(conn, "Departments") == 0) {

                insertDepartment(conn, "IT", "Phòng Công nghệ thông tin", "Phát triển và vận hành hệ thống phần mềm");
                insertDepartment(conn, "HR", "Phòng Nhân sự", "Tuyển dụng, đào tạo và quản lý nhân viên");
                insertDepartment(conn, "FI", "Phòng Tài chính", "Quản lý ngân sách và kế toán");
            }

            if (countRows(conn, "Users") == 0) {
                // userId 1 = admin    (SA)
                insertUser(conn, "admin", "admin@company.com", BCrypt.withDefaults().hashToString(12, "admin123".toCharArray()), "Nguyễn Lê Bình An", "2006-01-06", "Phủ Lý, Hà Nam", 1);
                // userId 2 = minhquan (BA)
                insertUser(conn, "minhquan", "minhquan@company.com", BCrypt.withDefaults().hashToString(12, "google123".toCharArray()), "Minh Quân", "2000-01-01", "Hà Nội", 2);
                // userId 3 = vu       (SA)
                insertUser(conn, "vu", "didoan482@gmail.com", BCrypt.withDefaults().hashToString(12, "soss123".toCharArray()), "Phạm Vũ", "2006-10-17", "Thanh Hóa", 1);
                // userId 4 = mixi     (BA)
                insertUser(conn, "mixi", "mixi@gmail.com", BCrypt.withDefaults().hashToString(12, "misi".toCharArray()), "Phung Thanh Do", "2006-10-10", "Cao Bang", 2);
                // userId 5 = misi     (HRManager)
                insertUser(conn, "misi", "ngng@gmail.com", BCrypt.withDefaults().hashToString(12, "mixi".toCharArray()), "Nguyen Nguyen", "2006-10-10", "Cao Bang", 3);
                // userId 6 = it_mgr   (ITManager)
                insertUser(conn, "it_mgr", "it.manager@company.com", BCrypt.withDefaults().hashToString(12, "123456".toCharArray()), "Trần Văn IT", "1990-05-10", "Hà Nội", 5);
                // userId 7 = it_emp1  (ITEmployee)
                insertUser(conn, "it_emp1", "it.emp1@company.com", BCrypt.withDefaults().hashToString(12, "123456".toCharArray()), "Lê Thị IT", "1995-03-15", "Hà Nội", 6);
                // userId 8 = it_emp2  (ITEmployee)
                insertUser(conn, "it_emp2", "it.emp2@company.com", BCrypt.withDefaults().hashToString(12, "123456".toCharArray()), "Phạm Văn Dev", "1997-07-20", "Hà Nội", 6);
                // userId 9 = hr_mgr   (HRManager)
                insertUser(conn, "hr_mgr", "hr.manager@company.com", BCrypt.withDefaults().hashToString(12, "123456".toCharArray()), "Nguyễn Thị HR", "1988-11-01", "TP HCM", 3);
                // userId 10 = hr_emp1 (HREmployee)
                insertUser(conn, "hr_emp1", "hr.emp1@company.com", BCrypt.withDefaults().hashToString(12, "123456".toCharArray()), "Vũ Thị Nhân Sự", "1993-06-25", "TP HCM", 4);
                // userId 11 = fi_mgr  (FIManager)
                insertUser(conn, "fi_mgr", "fi.manager@company.com", BCrypt.withDefaults().hashToString(12, "123456".toCharArray()), "Hoàng Văn FI", "1985-09-12", "Đà Nẵng", 7);
                // userId 12 = fi_emp1 (FIEmployee)
                insertUser(conn, "fi_emp1", "fi.emp1@company.com", BCrypt.withDefaults().hashToString(12, "123456".toCharArray()), "Đinh Thị Kế Toán", "1996-02-18", "Đà Nẵng", 8);
            }

            if (countRows(conn, "Employees") == 0) {
                // departmentId: 1=IT, 2=HR, 3=FI | positionId: 1=Thực tập sinh, 2=Nhân viên, 3=Trưởng phòng
                // IT — userId 6,7,8
                insertEmployee(conn, "EMP001", 6, 1, 3, "0901000001", "Java, SQL, Spring Boot", "5 năm phát triển web", "Kỹ sư CNTT");
                insertEmployee(conn, "EMP002", 7, 1, 2, "0901000002", "React, TypeScript", "2 năm frontend", "Cử nhân CNTT");
                insertEmployee(conn, "EMP003", 8, 1, 1, "0901000003", "DevOps, Docker", "1 năm vận hành", "Cử nhân CNTT");
                // HR — userId 9,10
                insertEmployee(conn, "EMP004", 9, 2, 3, "0901000004", "Tuyển dụng, HRIS", "6 năm nhân sự", "Cử nhân Quản trị nhân lực");
                insertEmployee(conn, "EMP005", 10, 2, 2, "0901000005", "Đào tạo, C&B", "3 năm C&B", "Cử nhân Kinh tế");
                // FI — userId 11,12
                insertEmployee(conn, "EMP006", 11, 3, 3, "0901000006", "Kế toán, MISA, Excel", "8 năm kế toán tài chính", "Cử nhân Kế toán");
                insertEmployee(conn, "EMP007", 12, 3, 2, "0901000007", "Thuế, kiểm toán", "2 năm tài chính", "Cử nhân Tài chính");
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
                insertFormType(conn, "LEAVE", "Nghỉ phép");
                insertFormType(conn, "COMPLAINT", "Khiếu nại");
                insertFormType(conn, "OVERTIME", "Tăng ca");
                insertFormType(conn, "TRANSFER", "Thuyên chuyển phòng ban");
                insertFormType(conn, "PROMOTION_DEMOTION", "Thăng/Giáng chức");
            } else {
                // Ensure new form types exist even if table was already seeded
                insertFormTypeIfAbsent(conn, "TRANSFER", "Thuyên chuyển phòng ban");
                insertFormTypeIfAbsent(conn, "PROMOTION_DEMOTION", "Thăng/Giáng chức");
            }

            ensureRolePermission(conn, "HRManager", "ADD_EMPLOYMENT_CONTRACT");
            ensureRolePermission(conn, "HRManager", "APPROVE_CONTRACT");
            ensureRolePermission(conn, "HRManager", "REJECT_CONTRACT");
            ensureRolePermission(conn, "HRManager", "TERMINATE_CONTRACT");
            ensureRolePermission(conn, "HRManager", "VIEW_PENDING_CONTRACTS");
            ensureRolePermission(conn, "HRManager", "VIEW_ALL_CONTRACTS");
            ensureRolePermission(conn, "HRManager", "VIEW_OWN_CONTRACT");

            ensureRolePermission(conn, "HREmployee", "ADD_EMPLOYMENT_CONTRACT");
            ensureRolePermission(conn, "HREmployee", "VIEW_OWN_CONTRACT");

            ensureRolePermission(conn, "ITManager", "VIEW_OWN_CONTRACT");
            ensureRolePermission(conn, "ITEmployee", "VIEW_OWN_CONTRACT");
            ensureRolePermission(conn, "FIManager", "VIEW_OWN_CONTRACT");
            ensureRolePermission(conn, "FIEmployee", "VIEW_OWN_CONTRACT");

            List<String> systemAdminPermissions = Arrays.asList(
                    "EDIT_USER",
                    "ADD_USER",
                    "VIEW_USERS",
                    "DELETE_USER",
                    "MANAGE_PERMISSIONS",
                    "EDIT_ROLE",
                    "ADD_ROLE",
                    "VIEW_ROLES",
                    "DELETE_ROLE"
            );
            for (String permissionCode : systemAdminPermissions) {
                ensureRolePermission(conn, "SystemAdmin", permissionCode);
            }
            String businessAdminSql = "SELECT permissionCode FROM Permissions WHERE permissionCode NOT IN (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(businessAdminSql)) {
                for (int i = 0; i < systemAdminPermissions.size(); i++) {
                    ps.setString(i + 1, systemAdminPermissions.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ensureRolePermission(conn, "BusinessAdmin", rs.getString("permissionCode"));
                    }
                }
            }

            if (countRows(conn, "Role_Permissions") == 0) {
                // HR Employee (HE) permissions
                insertRolePermission(conn, "HE", "ADD_EMPLOYMENT_CONTRACT");
                insertRolePermission(conn, "HE", "VIEW_CONTRACT_PREVIEW");
                // HR Manager (HM) permissions
                insertRolePermission(conn, "HM", "ADD_EMPLOYMENT_CONTRACT");
                insertRolePermission(conn, "HM", "VIEW_CONTRACT_PREVIEW");
                insertRolePermission(conn, "HM", "PERM_APPROVE_CONTRACT");
                insertRolePermission(conn, "HM", "PERM_VIEW_ALL_CONTRACTS");
            }

            LOGGER.log(Level.INFO, "Seeding completed successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot insert initial data", e);
        }
    }

    private void ensurePermission(Connection conn, String code, String name, String description) throws SQLException {
        String checkSql = "SELECT 1 FROM Permissions WHERE permissionCode = ?";
        try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setString(1, code);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }
        String insertSql = "INSERT INTO Permissions (permissionCode, permissionName, description) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setNString(3, description);
            ps.executeUpdate();
        }
    }

    private void insertPermission(Connection conn, String code, String name, String description) throws SQLException {
        String sql = "INSERT IGNORE INTO Permissions (permissionCode, permissionName, description) VALUES (?, ?, ?)";
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

    private void insertFormTypeIfAbsent(Connection conn, String code, String name) throws SQLException {
        String sql = "INSERT IGNORE INTO Form_Types (formTypeCode, formTypeName) VALUES (?, ?)";
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

    private void insertRole(Connection conn, String code, String name, String description) throws SQLException {
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
            ps.setString(1, username);
            ps.setString(2, e);
            ps.setString(3, p);
            ps.setString(4, fn);
            ps.setDate(5, java.sql.Date.valueOf(d));
            ps.setString(6, a);
            ps.setInt(7, r);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
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

    private void insertRolePermission(Connection conn, String roleCode, String permissionCode) throws SQLException {
        String sql = "INSERT IGNORE INTO Role_Permissions (roleId, permissionId) "
                + "SELECT r.roleId, p.permissionId FROM Roles r, Permissions p "
                + "WHERE r.roleCode = ? AND p.permissionCode = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleCode);
            ps.setString(2, permissionCode);
            ps.executeUpdate();
        }
    }

    private void ensureRolePermission(Connection conn, String roleName, String permissionCode) throws SQLException {
        String findSql = "SELECT r.roleId, p.permissionId FROM Roles r JOIN Permissions p ON 1=1 WHERE r.roleName = ? AND p.permissionCode = ?";
        try (PreparedStatement findPs = conn.prepareStatement(findSql)) {
            findPs.setString(1, roleName);
            findPs.setString(2, permissionCode);
            try (ResultSet rs = findPs.executeQuery()) {
                if (!rs.next()) {
                    return;
                }
                int roleId = rs.getInt("roleId");
                int permissionId = rs.getInt("permissionId");
                String existsSql = "SELECT 1 FROM Role_Permissions WHERE roleId = ? AND permissionId = ?";
                try (PreparedStatement existsPs = conn.prepareStatement(existsSql)) {
                    existsPs.setInt(1, roleId);
                    existsPs.setInt(2, permissionId);
                    try (ResultSet existsRs = existsPs.executeQuery()) {
                        if (existsRs.next()) {
                            return;
                        }
                    }
                }
                String insertSql = "INSERT INTO Role_Permissions (roleId, permissionId) VALUES (?, ?)";
                try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                    insertPs.setInt(1, roleId);
                    insertPs.setInt(2, permissionId);
                    insertPs.executeUpdate();
                }
            }
        }
    }

    private void ensureRolePermissionUniqueKey(Connection conn) throws SQLException {
        String checkSql = "SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'Role_Permissions' AND INDEX_NAME = 'uq_role_permission'";
        try (PreparedStatement checkPs = conn.prepareStatement(checkSql); ResultSet rs = checkPs.executeQuery()) {
            if (rs.next()) {
                return;
            }
        }
        execute(conn, "ALTER TABLE Role_Permissions ADD CONSTRAINT uq_role_permission UNIQUE (roleId, permissionId)", "ADD ROLE_PERMISSIONS UNIQUE KEY");
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
            LOGGER.log(Level.INFO, "DROPPED TABLE:{0} ", tableName);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "COULD NOT DROP {0} : {1}", new Object[]{tableName, e.getMessage()});
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

    private void ensurePayrollApprovalColumns(Connection conn) throws SQLException {
        if (!tableExists(conn, "Payroll")) {
            return;
        }
        if (!columnExists(conn, "Payroll", "approvedBy")) {
            execute(conn, "ALTER TABLE Payroll ADD COLUMN approvedBy INT", "ADD PAYROLL APPROVED BY COLUMN");
        }
        if (!columnExists(conn, "Payroll", "approvedAt")) {
            execute(conn, "ALTER TABLE Payroll ADD COLUMN approvedAt DATETIME", "ADD PAYROLL APPROVED AT COLUMN");
        }

    }

    private void ensureFormRequestColumns(Connection conn) throws SQLException {
        if (!tableExists(conn, "Form_Requests")) {
            return;
        }
        if (!columnExists(conn, "Form_Requests", "targetDepartmentId")) {
            execute(conn, "ALTER TABLE Form_Requests ADD COLUMN targetDepartmentId INT", "ADD FORM_REQUESTS TARGET DEPT COLUMN");
        }
        if (!columnExists(conn, "Form_Requests", "targetRoleId")) {
            execute(conn, "ALTER TABLE Form_Requests ADD COLUMN targetRoleId INT", "ADD FORM_REQUESTS TARGET ROLE COLUMN");
        }

    }

    private int countRows(Connection conn, String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public static void main(String[] args) {
        DBInitializer initializer = new DBInitializer();
        initializer.initializeDatabase(true);
    }
}
