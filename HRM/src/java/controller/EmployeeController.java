package controller;

import dao.AttendanceDAO;
import dao.CandidateDAO;
import dao.DepartmentDAO;
import dao.EmployeeDAO;
import dao.EmploymentContractDAO;
import dao.FormRequestDAO;
import dao.FormTypeDAO;
import dao.PermissionDAO;
import dao.RoleDAO;
import dao.UploadedFileDAO;
import dao.UserDAO;
import dao.LeaveBalanceDAO;
import dto.AttendanceDetailDTO;
import dto.AttendanceImportResultDTO;
import dto.AttendanceReportDTO;
import dto.CandidateImportResultDTO;
import dto.EmployeeDetailDTO;
import dto.FormRequestDTO;
import dto.PayrollPreviewDTO;
import enums.FileStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.math.BigDecimal;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.*;
import service.AttendanceImportService;
import service.AttendanceService;
import service.CandidateImportService;
import service.EmailService;
import service.PayrollService;
import utils.AttendanceExcelExporter;
import utils.Paging;
import utils.ConfigManager;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, // 1MB ghi ra đĩa
        maxFileSize = 10L * 1024 * 1024, // 10MB / file
        maxRequestSize = 11L * 1024 * 1024 // 11MB / request
)

public class EmployeeController extends HttpServlet {

    private final ConfigManager config = ConfigManager.getInstance();
    private final Logger LOGGER = Logger.getLogger(EmployeeController.class.getName());
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final EmploymentContractDAO contractDAO = new EmploymentContractDAO();
    private final UserDAO userDAO = new UserDAO();
    private final PermissionDAO permissionDAO = new PermissionDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    private final FormRequestDAO formRequestDAO = new FormRequestDAO();
    private final FormTypeDAO formTypeDAO = new FormTypeDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final UploadedFileDAO uploadedFileDAO = new UploadedFileDAO();
    private final LeaveBalanceDAO leaveBalanceDAO = new LeaveBalanceDAO();
    private final AttendanceImportService importService = new AttendanceImportService();
    private final AttendanceService attendanceService = new AttendanceService();
    private final AttendanceExcelExporter attendanceExporter = new AttendanceExcelExporter();
    private final String UPLOAD_DIR = config.getProperty("UPLOAD_DIR");
    private final String FILE_PART = config.getProperty("FILE_PART");
    private final CandidateDAO candidateDAO = new CandidateDAO();
    private final EmailService emailService = new EmailService();
    private final CandidateImportService candidateImportService = new CandidateImportService();
    private final PayrollService payrollService = new PayrollService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        preventBackCache(response);

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        String action = request.getPathInfo();
        if (action == null || action.equals("/")) {
            displayDashboard(request, response, user);
            return;
        }
        switch (action) {
            case "/dashboard":
                displayDashboard(request, response, user);
                break;
            case "/employee-list":
                displayEmployeeList(request, response, user);
                break;
            case "/employee-detail":
                displayEmployeeDetail(request, response, user);
                break;
            case "/update-employee":
                displayUpdateEmployeeForm(request, response, user);
                break;
            case "/contract/add":
                displayAddContractForm(request, response, user);
                break;
            case "/contract/preview":
                displayContractPreview(request, response, user);
                break;
            case "/department/detail":
                displayEmployeeDepartmentDetail(request, response, user);
                break;
            case "/department/assign":
                displayAssignDepartmentForm(request, response, user);
                break;
            case "/department/list":
                displayDepartmentList(request, response, user);
                break;
            case "/department/add":
                displayAddDepartmentForm(request, response, user);
                break;
            case "/department/update":
                displayUpdateDepartmentForm(request, response, user);
                break;
            case "/my-profile":
                displayMyProfile(request, response, user);
                break;
            case "/attendance/import":
                displayImportForm(request, response, user);
                break;
            case "/attendance/update":
                displayUpdateAttendanceForm(request, response, user);
                break;
            case "/attendance/own-attendance":
                displayOwnAttendanceList(request, response, user);
                break;
            case "/salary/own":
                displayOwnSalary(request, response, user);
                break;
            case "/salary/all":
                displayAllSalary(request, response, user);
                break;
            case "/salary/detail":
                displaySalaryDetail(request, response, user);
                break;
            case "/salary/export":
                exportSalary(request, response, user);
                break;
            case "/attendance/overview":
                displayAttendanceOverview(request, response, user);
                break;
            case "/attendance/detail":
                displayAttendanceDetail(request, response, user);
                break;
            case "/attendance/export":
                exportAttendanceReport(request, response, user);
                break;
            case "/forms/my-forms":
                displayMyForms(request, response, user);
                break;
            case "/forms/leave/new":
                displayLeaveForm(request, response, user);
                break;
            case "/forms/complaint/new":
                displayComplaintForm(request, response, user);
                break;
            case "/forms/all":
                displayAllForms(request, response, user);
                break;
            case "/forms/detail":
                displayFormDetail(request, response, user);
                break;
            case "/recruitment-list":
                displayRecruitmentList(request, response, user);
                break;
            case "/recruitment-detail":
                displayRecruitmentDetail(request, response, user);
                break;
            case "/recruitment-import":
                displayRecruitmentImport(request, response, user);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        preventBackCache(response);

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        String action = request.getPathInfo();
        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        switch (action) {
            case "/department/assign":
                handleAssignDepartment(request, response, user);
                break;
            case "/update-employee":
                handleUpdateEmployee(request, response, user);
                break;
            case "/contract/add":
                handleAddContract(request, response, user);
                break;
            case "/department/unassign":
                handleUnassignDepartment(request, response, user);
                break;
            case "/department/add":
                handleAddDepartment(request, response, user);
                break;
            case "/department/update":
                handleUpdateDepartment(request, response, user);
                break;
            case "/my-profile/update":
                handleUpdateMyProfile(request, response, user);
                break;
            case "/attendance/import":
                handleImportAttendance(request, response, user);
                break;
            case "/attendance/update":
                handleUpdateAttendance(request, response, user);
                break;
            case "/update-employee-detail":
                handleUpdateEmployeeDetail(request, response, user);
                break;
            case "/forms/leave/submit":
                handleLeaveFormSubmit(request, response, user);
                break;
            case "/forms/complaint/submit":
                handleComplaintFormSubmit(request, response, user);
                break;
            case "/recruitment-review":
                handleRecruitmentReview(request, response, user);
                break;
            case "/recruitment-import":
                handleImportCandidates(request, response, user);
                break;
            case "/salary/generate":
                handleGeneratePayroll(request, response, user);
                break;
            case "/salary/approve-all":
                handleApproveAllPayroll(request, response, user);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
                break;
        }
    }

    private void displayDashboard(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);

        EmployeeDetailDTO myEmployee = employeeDAO.getEmployeeByUserId(user.getUserId());
        request.setAttribute("myEmployee", myEmployee);

        request.getRequestDispatcher("/public/employee/dashboard.jsp").forward(request, response);
    }

    private void displayEmployeeList(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error",
                    "Bạn không có quyền xem danh sách nhân viên bởi bạn không phải HR");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        if (!hasPermission(user, "VIEW_EMPLOYEES")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem danh sách nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        List<EmployeeDetailDTO> employees = employeeDAO.getAllEmployees();
        request.setAttribute("employees", employees);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/employee_info/employee_list.jsp").forward(request, response);
    }

    private void displayEmployeeDetail(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error",
                    "Bạn không có quyền xem chi tiết nhân viên bởi bạn không phải HR");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        if (!hasPermission(user, "VIEW_EMPLOYEES")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem chi tiết nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        EmployeeDetailDTO employee = getEmployeeFromRequest(request, response);
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/employee-list");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("employee", employee);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/employee_info/employee_detail.jsp").forward(request, response);
    }

    private void displayUpdateEmployeeForm(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền chỉnh sửa nhân viên bởi bạn không phải HR");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        if (!hasPermission(user, "EDIT_EMPLOYEE")) {
            request.getSession().setAttribute("error", "Bạn không có quyền chỉnh sửa nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        EmployeeDetailDTO employee = getEmployeeFromRequest(request, response);
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/employee-list");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("employee", employee);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/employee_info/update_employee.jsp").forward(request, response);
    }

    private void displayAddContractForm(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm hợp đồng bởi bạn không phải HR");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        if (!hasPermission(user, "ADD_EMPLOYMENT_CONTRACT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm hợp đồng lao động.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("employees", employeeDAO.getAllEmployees());
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/contract/add_contract.jsp").forward(request, response);
    }

    private void displayContractPreview(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem hợp đồng lao động.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        if (!hasPermission(user, "ADD_EMPLOYMENT_CONTRACT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem hợp đồng lao động.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        EmploymentContract contract = getContractFromRequest(request);
        if (contract == null) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/employee-list");
            return;
        }

        EmployeeDetailDTO employee = employeeDAO.getEmployeeById(contract.getEmployeeId());
        if (employee == null) {
            request.getSession().setAttribute("error", "Không tìm thấy nhân viên của hợp đồng.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/employee-list");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("contract", contract);
        request.setAttribute("employee", employee);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/contract/contract_preview.jsp").forward(request, response);
    }

    private void displayOwnAttendanceList(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);

        java.time.LocalDate now = java.time.LocalDate.now();
        int month = attParam(request, "month", now.minusMonths(1).getMonthValue());
        int year = attParam(request, "year", now.minusMonths(1).getYear());
        int day = attParam(request, "day", 0); 

        EmployeeDetailDTO me = employeeDAO.getEmployeeByUserId(user.getUserId());
        List<Attendance> monthRows = (me != null)
                ? attendanceDAO.getDailyAttendance(me.getEmployeeId(), month, year)
                : new java.util.ArrayList<>();

        dto.AttendanceSummaryDTO summary = new dto.AttendanceSummaryDTO();
        java.math.BigDecimal worked = java.math.BigDecimal.ZERO;
        for (Attendance a : monthRows) {
            switch (a.getAttendanceStatus()) {
                case 0:
                    summary.setPresentDays(summary.getPresentDays() + 1);
                    break;
                case 1:
                    summary.setLateDays(summary.getLateDays() + 1);
                    break;
                case 4:
                    summary.setLeaveDays(summary.getLeaveDays() + 1);
                    break;
                case 2:
                case 3:
                    summary.setAbsentDays(summary.getAbsentDays() + 1);
                    break;
                case 5:
                    summary.setHolidayDays(summary.getHolidayDays() + 1);
                    break;
                case 6:
                    summary.setWeekendDays(summary.getWeekendDays() + 1);
                    break;
                default:
                    break;
            }
            if (a.getHoursWorked() != null) {
                worked = worked.add(a.getHoursWorked());
            }
        }
        summary.setWorkedHours(worked);
        summary.setStandardDays(attendanceService.standardWorkingDays(month, year));
        request.setAttribute("summary", summary);

        List<Attendance> filtered;
        if (day >= 1 && day <= 31) {
            filtered = new java.util.ArrayList<>();
            for (Attendance a : monthRows) {
                if (a.getWorkDate() != null && a.getWorkDate().toLocalDate().getDayOfMonth() == day) {
                    filtered.add(a);
                }
            }
        } else {
            filtered = monthRows;
        }

        request.setAttribute("attendances", Paging.page(request, filtered));
        request.setAttribute("monthRows", monthRows);
        
        if (me != null) {
            java.util.List<Integer> approvedOTDays = new dao.OvertimeDAO().getApprovedOTDaysInMonth(me.getEmployeeId(), month, year);
            request.setAttribute("approvedOTDays", approvedOTDays);
        }

        request.setAttribute("selectedMonth", month);
        request.setAttribute("selectedYear", year);
        request.setAttribute("selectedDay", day);
        request.getRequestDispatcher("/public/employee/attendance/own_attendance_list.jsp").forward(request, response);
    }

    private void displayOwnSalary(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);

        if (!payrollService.canViewOwnSalary(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem lương cá nhân.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        int[] period = parseSalaryPeriod(request);
        PayrollPreviewDTO payrollPreview = payrollService.getOwnPayroll(user, period[0], period[1]);
        if (payrollPreview == null) {
            request.setAttribute("salaryError", buildSalaryPeriodMessage(period[0], period[1]));
        }
        request.setAttribute("payrollPreview", payrollPreview);
        request.setAttribute("selectedYear", period[0]);
        request.setAttribute("selectedMonth", period[1]);
        request.getRequestDispatcher("/public/employee/salary/own_salary.jsp").forward(request, response);
    }

    private void displayAllSalary(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);

        if (!payrollService.canViewAllSalary(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem bảng lương tất cả nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        int[] period = parseSalaryPeriod(request);
        Integer departmentId = parseIntOrNull(request.getParameter("departmentId"));
        List<PayrollPreviewDTO> payrollPreviews = payrollService.getAllPayrollForHr(user, period[0], period[1], departmentId);
        if (payrollPreviews.isEmpty()) {
            request.setAttribute("salaryError", buildSalaryPeriodMessage(period[0], period[1]));
        }
        request.setAttribute("payrollPreviews", payrollPreviews);
        request.setAttribute("selectedYear", period[0]);
        request.setAttribute("selectedMonth", period[1]);
        request.setAttribute("selectedDepartmentId", departmentId);
        request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
        request.setAttribute("canExportPayroll", payrollService.canExportPayroll(user));
        boolean canApproveAll = payrollService.canApprovePayroll(user);
        request.setAttribute("canApprovePayroll", canApproveAll);
        if (canApproveAll) {
            int pendingCount = payrollService.countPendingApprovalForPeriod(user, period[0], period[1], departmentId);
            request.setAttribute("pendingApprovalCount", pendingCount);
        }
        request.getRequestDispatcher("/public/employee/salary/salary_list.jsp").forward(request, response);
    }

    private void displaySalaryDetail(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);

        if (!payrollService.canViewAllSalary(user) && !payrollService.canViewOwnSalary(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem chi tiết bảng lương.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        Integer payrollId = parseIntOrNull(request.getParameter("id"));
        PayrollPreviewDTO payrollPreview = payrollId == null ? null : payrollService.getPayrollDetail(user, payrollId);
        if (payrollPreview == null) {
            request.setAttribute("salaryError", "Không tìm thấy bảng lương cần xem chi tiết.");
        }
        request.setAttribute("payrollPreview", payrollPreview);
        request.getRequestDispatcher("/public/employee/salary/salary_detail.jsp").forward(request, response);
    }

    private void exportSalary(HttpServletRequest request, HttpServletResponse response,
            User user) throws IOException {
        if (!payrollService.canExportPayroll(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền xuất bảng lương.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        int[] period = parseSalaryPeriod(request);
        Integer departmentId = parseIntOrNull(request.getParameter("departmentId"));
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=payroll_"
                + String.format("%04d_%02d", period[0], period[1]) + ".xlsx");
        try (OutputStream out = response.getOutputStream()) {
            payrollService.exportPayrollWorkbook(user, period[0], period[1], departmentId, out);
        }
    }

    private void handleApproveAllPayroll(HttpServletRequest request, HttpServletResponse response,
            User user) throws IOException {
        if (!payrollService.canApprovePayroll(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền duyệt bảng lương.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        int[] period = parseSalaryPeriod(request);
        Integer departmentId = parseIntOrNull(request.getParameter("departmentId"));

        int approvedCount = payrollService.approveAllPayrollForPeriod(user, period[0], period[1], departmentId);
        if (approvedCount > 0) {
            request.getSession().setAttribute("success",
                    "Đã duyệt " + approvedCount + " bảng lương cho kỳ lương "
                    + String.format("%02d/%d", period[1], period[0]) + ".");
        } else {
            request.getSession().setAttribute("error",
                    "Không có bảng lương nào đang chờ duyệt để xử lý (hoặc tất cả đều là lương của chính bạn).");
        }

        StringBuilder url = new StringBuilder(request.getContextPath() + "/v1/employee/salary/all");
        url.append("?month=").append(period[1]).append("&year=").append(period[0]);
        if (departmentId != null) {
            url.append("&departmentId=").append(departmentId);
        }
        response.sendRedirect(url.toString());
    }

    private void handleGeneratePayroll(HttpServletRequest request, HttpServletResponse response,
            User user) throws IOException {
        if (!payrollService.canViewAllSalary(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền tạo bảng lương.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        int[] period = parseSalaryPeriod(request);
        Integer departmentId = parseIntOrNull(request.getParameter("departmentId"));
        int generated = payrollService.saveGeneratedPayrollForPeriod(period[0], period[1], departmentId);
        request.getSession().setAttribute("success", "Đã tạo bảng lương cho " + generated + " nhân sự.");
        response.sendRedirect(request.getContextPath() + "/v1/employee/salary/all?month=" + period[1]
                + "&year=" + period[0]
                + (departmentId == null ? "" : "&departmentId=" + departmentId));
    }

    private int attParam(HttpServletRequest request, String name, int defaultValue) {
        String raw = request.getParameter(name);
        if (raw != null && !raw.trim().isEmpty()) {
            try {
                return Integer.parseInt(raw.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private void displayAttendanceOverview(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);
        setEmployeeAttendanceLayout(request);

        if (!perms.contains("VIEW_ALL_ATTENDANCE")) {
            request.setAttribute("error", "Bạn không có quyền xem toàn bộ chấm công.");
            request.getRequestDispatcher("/public/employee/attendance/attendance_overview.jsp").forward(request, response);
            return;
        }

        LocalDate now = LocalDate.now();
        int month = attParam(request, "month", now.minusMonths(1).getMonthValue());
        int year = attParam(request, "year", now.minusMonths(1).getYear());
        Integer departmentId = attDepartmentParam(request);

        java.util.List<dto.AttendanceSummaryDTO> summaries
                = attendanceService.getMonthlySummaries(departmentId, month, year);
        request.setAttribute("summaries", summaries);
        request.setAttribute("pagedSummaries", Paging.page(request, summaries));
        request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
        request.setAttribute("selectedDepartmentId", departmentId);
        if (departmentId != null) {
            model.Department dept = departmentDAO.getDepartmentById(departmentId);
            request.setAttribute("departmentName", dept != null ? dept.getDepartmentName() : "");
        }
        request.setAttribute("selectedMonth", month);
        request.setAttribute("selectedYear", year);
        request.getRequestDispatcher("/public/employee/attendance/attendance_overview.jsp").forward(request, response);
    }

    private void displayAttendanceDetail(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);
        setEmployeeAttendanceLayout(request);

        if (!perms.contains("VIEW_ALL_ATTENDANCE")) {
            request.setAttribute("error", "Bạn không có quyền xem toàn bộ chấm công.");
            request.getRequestDispatcher("/public/employee/attendance/attendance_detail.jsp").forward(request, response);
            return;
        }

        int employeeId = attParam(request, "employeeId", -1);
        LocalDate now = LocalDate.now();
        int month = attParam(request, "month", now.minusMonths(1).getMonthValue());
        int year = attParam(request, "year", now.minusMonths(1).getYear());
        Integer departmentId = attDepartmentParam(request);

        AttendanceDetailDTO detail = (employeeId > 0)
                ? attendanceService.getEmployeeDetail(employeeId, departmentId, month, year)
                : null;
        if (detail == null) {
            request.setAttribute("error", "Không tìm thấy dữ liệu chấm công của nhân viên.");
            request.getRequestDispatcher("/public/employee/attendance/attendance_detail.jsp").forward(request, response);
            return;
        }
        
        java.util.List<Integer> approvedOTDays = new dao.OvertimeDAO().getApprovedOTDaysInMonth(employeeId, month, year);
        request.setAttribute("approvedOTDays", approvedOTDays);

        int day = attParam(request, "day", 0);
        List<Attendance> filtered = detail.getDailyRows();
        if (day >= 1 && day <= 31) {
            filtered = new java.util.ArrayList<>();
            for (model.Attendance a : detail.getDailyRows()) {
                if (a.getWorkDate() != null && a.getWorkDate().toLocalDate().getDayOfMonth() == day) {
                    filtered.add(a);
                }
            }
        }
        request.setAttribute("selectedDay", day);
        request.setAttribute("pagedRows", Paging.page(request, filtered));
        request.setAttribute("detail", detail);
        request.setAttribute("selectedDepartmentId", departmentId);
        request.setAttribute("selectedMonth", month);
        request.setAttribute("selectedYear", year);
        request.getRequestDispatcher("/public/employee/attendance/attendance_detail.jsp").forward(request, response);
    }

    private void exportAttendanceReport(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!hasPermission(user, "VIEW_ALL_ATTENDANCE")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền xuất báo cáo chấm công.");
            return;
        }
        LocalDate now = LocalDate.now();
        int month = attParam(request, "month", now.minusMonths(1).getMonthValue());
        int year = attParam(request, "year", now.minusMonths(1).getYear());
        Integer departmentId = attDepartmentParam(request);

        AttendanceReportDTO report = attendanceService.getReport(departmentId, month, year);
        String scope = (departmentId == null) ? "company" : "dept" + departmentId;
        String fileName = "attendance_" + scope + "_" + year + "_" + String.format("%02d", month) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        attendanceExporter.write(report, response.getOutputStream());
    }

    private void displayEmployeeDepartmentDetail(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {

        if (!isHrStaff(user) || !hasPermission(user, "VIEW_DEPARTMENT_EMPLOYEES_DETAIL")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem nhân viên của phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        String rawDepartmentId = request.getParameter("id");
        if (isBlank(rawDepartmentId)) {
            request.getSession().setAttribute("error", "Thiếu mã phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
            return;
        }

        int departmentId;
        try {
            departmentId = Integer.parseInt(rawDepartmentId);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã phòng ban không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
            return;
        }

        Department department = departmentDAO.getDepartmentById(departmentId);
        if (department == null) {
            request.getSession().setAttribute("error", "Không tìm thấy phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        List<EmployeeDetailDTO> employees = employeeDAO.getEmployeesByDepartmentId(departmentId);
        request.setAttribute("department", department);
        request.setAttribute("employees", employees);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/employee_info/department_employee_detail.jsp").forward(request, response);
    }

    private void displayAssignDepartmentForm(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {

        if (!isHrStaff(user) || !hasPermission(user, "ASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền phân cóng phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        List<User> availableEmployees = employeeDAO.getEmployees(user.getUserId());
        List<Department> departments = departmentDAO.getAllActiveDepartments();
        List<Position> positions = departmentDAO.getAllPositions();

        request.setAttribute("availableEmployees", availableEmployees);
        request.setAttribute("departments", departments);
        request.setAttribute("positions", positions);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/department/assign_department.jsp").forward(request, response);
    }

    private void displayDepartmentList(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        List<Department> departments = departmentDAO.getAllDepartments();

        Map<Integer, Integer> empCounts = new HashMap<>();
        for (Department d : departments) {
            empCounts.put(d.getDepartmentId(), employeeDAO.countByDepartmentId(d.getDepartmentId()));
        }

        request.setAttribute("departments", departments);
        request.setAttribute("empCounts", empCounts);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/department/department_list.jsp").forward(request, response);
    }

    private void displayMyProfile(HttpServletRequest request, HttpServletResponse response,
            User sessionUser) throws ServletException, IOException {
        User currentUser = userDAO.getUserById(sessionUser.getUserId());
        request.setAttribute("currentUser", currentUser);
        EmployeeDetailDTO myEmployee = employeeDAO.getEmployeeByUserId(sessionUser.getUserId());
        request.setAttribute("myEmployee", myEmployee);
        request.getRequestDispatcher("/public/employee/employee_info/my_profile.jsp").forward(request, response);
    }

    private void displayImportForm(HttpServletRequest request, HttpServletResponse response,
            model.User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "IMPORT_ATTENDANCE")) {
            request.getSession().setAttribute("error", "Bạn không có quyền import chấm cóng.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setImportWindowAttributes(request);
        request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
    }

    /**
     * Đặt các thuộc tính phục vụ giao diện import: tháng được phép (tháng liền trước),
     * và cờ cho biết cửa sổ import (2 ngày đầu tháng) còn mở hay không.
     */
    private void setImportWindowAttributes(HttpServletRequest request) {
        LocalDate today = LocalDate.now();
        LocalDate prevMonth = today.minusMonths(1);
        request.setAttribute("allowedMonth", prevMonth.getMonthValue());
        request.setAttribute("allowedYear", prevMonth.getYear());
        request.setAttribute("importWindowOpen", today.getDayOfMonth() <= 2);
        if (request.getAttribute("selectedMonth") == null) {
            request.setAttribute("selectedMonth", prevMonth.getMonthValue());
        }
        if (request.getAttribute("selectedYear") == null) {
            request.setAttribute("selectedYear", prevMonth.getYear());
        }
    }

    /**
     * Kiểm tra ràng buộc thời gian import chấm công:
     * chỉ cho phép import trong 2 ngày đầu mỗi tháng (ngày 1 và 2),
     * và chỉ cho tháng liền trước. Trả về thông báo lỗi nếu không hợp lệ, null nếu hợp lệ.
     */
    private String validateImportWindow(int month, int year) {
        LocalDate today = LocalDate.now();
        LocalDate prevMonth = today.minusMonths(1);
        if (today.getDayOfMonth() > 2) {
            return "Chỉ được import chấm công trong 2 ngày đầu mỗi tháng (ngày 1 và ngày 2). "
                    + "Hôm nay đã qua hạn, không thể import chấm công Tháng "
                    + prevMonth.getMonthValue() + "/" + prevMonth.getYear() + " nữa.";
        }
        if (month != prevMonth.getMonthValue() || year != prevMonth.getYear()) {
            return "Chỉ được import chấm công cho tháng liền trước (Tháng "
                    + prevMonth.getMonthValue() + "/" + prevMonth.getYear() + ").";
        }
        return null;
    }

    private void displayUpdateAttendanceForm(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "EDIT_ATTENDANCE")) {
            request.getSession().setAttribute("error", "Bạn không có quyền chỉnh sửa dữ liệu chấm cóng.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);

        String backUrl = request.getContextPath() + "/v1/employee/attendance/detail"
                + buildAttendanceDetailQuery(request);

        Integer attendanceId = parseIntOrNull(request.getParameter("id"));
        Attendance attendance = (attendanceId != null) ? attendanceDAO.getAttendanceById(attendanceId) : null;
        if (attendance == null) {
            request.getSession().setAttribute("error", "Không tìm thấy bản ghi chấm cóng.");
            response.sendRedirect(backUrl);
            return;
        }
        request.setAttribute("attendance", attendance);
        request.setAttribute("editLocked", isAttendanceEditLocked(attendance.getWorkDate()));
        request.setAttribute("adjustmentHistory", attendanceDAO.getAdjustmentHistory(attendanceId));
        request.setAttribute("backUrl", backUrl);
        request.setAttribute("filterMonth", trimToNull(request.getParameter("month")));
        request.setAttribute("filterYear", trimToNull(request.getParameter("year")));
        request.setAttribute("filterDepartmentId", trimToNull(request.getParameter("departmentId")));
        request.setAttribute("filterEmployeeCode", trimToNull(request.getParameter("employeeCode")));
        String filterEmployeeId = trimToNull(request.getParameter("employeeId"));
        if (filterEmployeeId == null) {
            filterEmployeeId = String.valueOf(attendance.getEmployeeId());
        }
        request.setAttribute("filterEmployeeId", filterEmployeeId);
        request.getRequestDispatcher("/public/employee/attendance/attendance_update.jsp").forward(request, response);
    }

    private void handleUpdateAttendance(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "EDIT_ATTENDANCE")) {
            request.getSession().setAttribute("error", "Bạn không có quyền chỉnh sửa dữ liệu chấm cóng.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        String redirectUrl = request.getContextPath() + "/v1/employee/attendance/detail"
                + buildAttendanceDetailQuery(request);

        Integer attendanceId = parseIntOrNull(request.getParameter("attendanceId"));
        if (attendanceId == null) {
            request.getSession().setAttribute("error", "Dữ liệu chỉnh sửa chấm cóng không hợp lệ.");
            response.sendRedirect(redirectUrl);
            return;
        }

        String reason = trimToNull(request.getParameter("reason"));
        if (reason == null) {
            request.getSession().setAttribute("error", "Vui lêng nhập lý do chỉnh sửa chấm cóng.");
            response.sendRedirect(redirectUrl);
            return;
        }

        Attendance attendance = attendanceDAO.getAttendanceById(attendanceId);
        if (attendance == null) {
            request.getSession().setAttribute("error", "Không tìm thấy bản ghi chấm cóng.");
            response.sendRedirect(redirectUrl);
            return;
        }

        if (isAttendanceEditLocked(attendance.getWorkDate())) {
            request.getSession().setAttribute("error",
                    "Đã quá hạn chỉnh sửa. Chấm cóng chỉ được sửa đến hết ngày 5 của thông kế tiếp.");
            response.sendRedirect(redirectUrl);
            return;
        }

        Time timeIn;
        Time timeOut;
        try {
            timeIn = parseTimeOrNull(request.getParameter("timeIn"));
            timeOut = parseTimeOrNull(request.getParameter("timeOut"));
        } catch (IllegalArgumentException e) {
            request.getSession().setAttribute("error", "Giờ vào / giờ ra không hợp lệ.");
            response.sendRedirect(redirectUrl);
            return;
        }

        if (timeIn != null && timeOut != null && timeOut.before(timeIn)) {
            request.getSession().setAttribute("error", "Giờ ra phải sau giờ vào.");
            response.sendRedirect(redirectUrl);
            return;
        }

        int status;
        try {
            status = importService.resolveStatus(attendance.getEmployeeId(),
                    attendance.getWorkDate(), timeIn, timeOut).getRelatedNum();
        } catch (SQLException e) {
            request.getSession().setAttribute("error", "Lỗi hệ thống khi xác định trạng thái chấm công.");
            response.sendRedirect(redirectUrl);
            return;
        }

        BigDecimal hoursWorked;
        if (timeIn != null && timeOut != null && (status == 0 || status == 1)) {
            hoursWorked = utils.WorkHoursCalculator.hoursWorked(timeIn, timeOut);
            // Không có đơn OT được duyệt cho ngày này thì giới hạn giờ công ở 8 tiếng chuẩn,
            // dù nhân viên đến sớm hơn hay về muộn hơn.
            BigDecimal standardHours = new BigDecimal("8.00");
            if (hoursWorked.compareTo(standardHours) > 0
                    && !new dao.OvertimeDAO().hasApprovedOT(attendance.getEmployeeId(), attendance.getWorkDate())) {
                hoursWorked = standardHours;
            }
        } else {
            hoursWorked = BigDecimal.ZERO;
        }

        String updateError = attendanceDAO.updateAttendanceWithHistory(attendanceId, timeIn, timeOut,
                hoursWorked, status, reason, user.getUserId());
        if (updateError == null) {
            request.getSession().setAttribute("success", "Đã cập nhật dữ liệu chấm cóng.");
        } else {
            request.getSession().setAttribute("error", updateError);
        }
        response.sendRedirect(redirectUrl);
    }

    private void handleImportAttendance(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "IMPORT_ATTENDANCE")) {
            request.getSession().setAttribute("error", "Bạn không có quyền import file chấm cóng");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
        }
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);

        int month, year;
        int departmentId = 0;
        try {
            month = Integer.parseInt(request.getParameter("month").trim());
            year = Integer.parseInt(request.getParameter("year").trim());
            String rawDept = request.getParameter("departmentId");
            if (rawDept != null && !rawDept.trim().isEmpty()) {
                departmentId = Integer.parseInt(rawDept.trim());
            }
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Hệ thống bị lỗi. Vui lêng nhập lại");
            response.sendRedirect(request.getContextPath() + "/v1/employee/attendance/import");
            return;
        }

        if (month < 1 || month > 12) {
            request.setAttribute("error", "Vui lêng chọn thông hợp lệ (1-12).");
            List<Department> activeDepartments = departmentDAO.getAllActiveDepartments();
            request.setAttribute("departments", activeDepartments);
            request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
        }
        if (year < 2000 || year > 2100) {
            request.setAttribute("error", "Vui lA?ng ch?n nam h?p l?");
            List<Department> activeDepartments = departmentDAO.getAllActiveDepartments();
            request.setAttribute("departments", activeDepartments);
            request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
            return;
        }

        String windowError = validateImportWindow(month, year);
        if (windowError != null) {
            request.setAttribute("error", windowError);
            request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
            setImportWindowAttributes(request);
            request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
            return;
        }

        Part filePart = request.getPart(FILE_PART);
        if (filePart == null || filePart.getSize() == 0) {
            request.setAttribute("error", "Vui lêng chọn file Excel .xlsx để import.");
            List<Department> activeDepartments = departmentDAO.getAllActiveDepartments();
            request.setAttribute("departments", activeDepartments);
            request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
            return;
        }

        String submittedName = filePart.getSubmittedFileName();
        if (submittedName == null || !submittedName.toLowerCase().endsWith(".xlsx")) {
            request.setAttribute("error", "File phải có định dạng .xlsx");
            List<Department> activeDepartments = departmentDAO.getAllActiveDepartments();
            request.setAttribute("departments", activeDepartments);
            request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
            return;
        }

        String contentType = filePart.getContentType();
        if (contentType != null && !isAcceptableXlsxContentType(contentType)) {
            request.setAttribute("error", "Loại file không hợp lệ. Yeaua cầu file excel .xlsx ");
            List<Department> activeDepartments = departmentDAO.getAllActiveDepartments();
            request.setAttribute("departments", activeDepartments);
            request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
            return;
        }
        EmployeeDetailDTO employee = employeeDAO.getEmployeeByUserId(user.getUserId());
        Integer submittedEmployeeId = (employee != null) ? employee.getEmployeeId() : null;
        int fileDepartmentId = (departmentId > 0)
                ? departmentId
                : (employee != null ? employee.getDepartmentId() : 0);
        String uploadPath = getServletContext().getRealPath("/" + UPLOAD_DIR);
        Path savedPath;
        String serverFileName = "ATT_" + departmentId
                + "_" + month + "_" + year + "_" + System.currentTimeMillis()
                + "_" + UUID.randomUUID().toString().substring(0, 8) + ".xlsx";
        try {
            Path dir = Paths.get(uploadPath);
            Files.createDirectories(dir);
            savedPath = dir.resolve(serverFileName);
            try (InputStream is = filePart.getInputStream()) {
                Files.copy(is, savedPath);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot save uploaded attendance file", e);
            List<Department> activeDepartments = departmentDAO.getAllActiveDepartments();
            request.setAttribute("departments", activeDepartments);
            request.setAttribute("error", "Không thể lưu file lên mãy chủ. Vui lêng thử lại.");
            request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
            return;
        }

        UploadedFile uf = new UploadedFile();
        uf.setFileCode("UPF-" + System.currentTimeMillis());
        uf.setFileType("ATTENDANCE");
        uf.setDepartmentId(fileDepartmentId);
        uf.setEmployeeId(submittedEmployeeId);
        uf.setFileUrl(UPLOAD_DIR + "/" + serverFileName);
        uf.setFileName(sanitizeFileName(submittedName));
        uf.setMonth(month);
        uf.setYear(year);
        int fileId = uploadedFileDAO.createUploadedFile(uf);
        if (fileId <= 0) {
            request.setAttribute("error", "Không thể tạo bản ghi file. Vui lêng thử lại.");
            List<Department> activeDepartments = departmentDAO.getAllActiveDepartments();
            request.setAttribute("departments", activeDepartments);
            request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
            return;
        }

        AttendanceImportResultDTO result;
        try (InputStream is = Files.newInputStream(savedPath)) {
            result = importService.importAttendance(is, departmentId, month, year, fileId);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot read saved attendance file", e);
            uploadedFileDAO.updateImportResult(fileId, 0, 0, 0,
                    FileStatus.FILE_STATUS_FAILED.getRelatedNum(), "Không thể đọc lại file đã lưu.");
            request.setAttribute("error", "Không thể đọc file đã lưu để import.");
            List<Department> activeDepartments = departmentDAO.getAllActiveDepartments();
            request.setAttribute("departments", activeDepartments);
            request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
            return;
        }
        result.setFileName(uf.getFileName());
        LOGGER.log(Level.INFO, "Attendance import done by userId={0}: fileId={1}, total={2}, imported={3}, failed={4}",
                new Object[]{user.getUserId(), fileId, result.getTotalRows(),
                    result.getImportedRows(), result.getFailedRows()});

        request.setAttribute("importResult", result);
        request.setAttribute("selectedMonth", month);
        request.setAttribute("selectedYear", year);
        request.setAttribute("selectedDepartmentId", departmentId);
        request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
    }

    private void handleAssignDepartment(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm phòng ban bởi bạn không phải HR");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        if (!hasPermission(user, "ASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        String rawUserId = request.getParameter("userId");
        String rawDepartmentId = request.getParameter("departmentId");
        String rawPositionId = request.getParameter("positionId");
        String phoneNumber = request.getParameter("phoneNumber");
        String skills = request.getParameter("skills");
        String experience = request.getParameter("experience");
        String degree = request.getParameter("degree");

        if (isBlank(rawUserId) || isBlank(rawDepartmentId) || isBlank(rawPositionId)) {
            request.setAttribute("error", "Vui lêng chọn đầy đủ nhân viên, phòng ban và vị trò.");
            request.setAttribute("availableEmployees", employeeDAO.getEmployees(user.getUserId()));
            request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
            request.setAttribute("positions", departmentDAO.getAllPositions());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/department/assign_department.jsp").forward(request, response);
            return;
        }

        int userId, departmentId, positionId;
        try {
            userId = Integer.parseInt(rawUserId);
            departmentId = Integer.parseInt(rawDepartmentId);
            positionId = Integer.parseInt(rawPositionId);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Dữ liệu không hợp lệ.");
            request.setAttribute("availableEmployees", employeeDAO.getEmployees(user.getUserId()));
            request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
            request.setAttribute("positions", departmentDAO.getAllPositions());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/department/assign_department.jsp").forward(request, response);
            return;
        }

        if (employeeDAO.isUserAssignedToDepartment(userId)) {
            request.setAttribute("error", "Người dùng này đã được phân cóng phòng ban rồi.");
            request.setAttribute("availableEmployees", employeeDAO.getEmployees(user.getUserId()));
            request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
            request.setAttribute("positions", departmentDAO.getAllPositions());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/department/assign_department.jsp").forward(request, response);
            return;
        }

        int userRoleId = userDAO.getRoleIdByUserId(userId);
        if (!departmentDAO.isRoleAllowedForDepartment(departmentId, userRoleId)) {
            Department dept = departmentDAO.getDepartmentById(departmentId);
            String deptName = (dept != null) ? dept.getDepartmentName() : "phòng ban này";
            List<String> allowed = departmentDAO.getAllowedRoleNames(departmentId);
            String msg = "Vai trò hiện tại của nhân viên không phù hợp với phòng \"" + deptName + "\". "
                    + "Phòng này chỉ nhận vai trò: " + String.join(", ", allowed) + ". "
                    + "Vui lêng đổi vai trò của người dùng trước khi phân cóng.";
            request.setAttribute("error", msg);
            request.setAttribute("availableEmployees", employeeDAO.getEmployees(user.getUserId()));
            request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
            request.setAttribute("positions", departmentDAO.getAllPositions());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/department/assign_department.jsp").forward(request, response);
            return;
        }

        boolean success = employeeDAO.assignEmployeeToDepartment(
                userId, departmentId, positionId,
                isBlank(phoneNumber) ? null : phoneNumber.trim(),
                isBlank(skills) ? null : skills.trim(),
                isBlank(experience) ? null : experience.trim(),
                isBlank(degree) ? null : degree.trim());

        if (!success) {
            request.setAttribute("error", "Phân cóng thất bại. Vui lêng thử lại.");
            request.setAttribute("availableEmployees", employeeDAO.getEmployees(user.getUserId()));
            request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
            request.setAttribute("positions", departmentDAO.getAllPositions());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/department/assign_department.jsp").forward(request, response);
            return;
        }

        String roleName = roleDAO.getRoleByUserId(userId);
        EmployeeDetailDTO assigned = employeeDAO.getEmployeeByUserId(userId);
        if (assigned != null) {
            Department assignedDept = departmentDAO.getDepartmentById(departmentId);
            boolean deptHasManager = assignedDept != null && assignedDept.getManagerId() != null;
            boolean isManagerRole = roleName != null && roleName.toLowerCase().contains("manager");

            if (isManagerRole && !deptHasManager) {
                employeeDAO.assignAsManager(departmentId, assigned.getEmployeeId());
            } else if (deptHasManager) {
                employeeDAO.setEmployeeManager(assigned.getEmployeeId(), assignedDept.getManagerId());
            }
        }

        LOGGER.log(Level.INFO, "Employee assigned: userId={0} → deptId={1}", new Object[]{userId, departmentId});

        request.getSession().setAttribute("success", "Phân cóng nhân viên vào phòng ban thành cóng.");
        response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
    }

    private void handleUnassignDepartment(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền gỡ phân cóng bởi bạn không phải HR");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        if (!hasPermission(user, "UNASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền gỡ phân cóng.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        String rawEmployeeId = request.getParameter("employeeId");
        if (isBlank(rawEmployeeId)) {
            request.getSession().setAttribute("error", "Thiếu mã nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/employee-list");
            return;
        }

        int employeeId;
        try {
            employeeId = Integer.parseInt(rawEmployeeId);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã nhân viên không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/employee-list");
            return;
        }

        EmployeeDetailDTO employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null || employee.getDepartmentId() <= 0) {
            request.getSession().setAttribute("error", "Nhân viên không hợp lệ hoặc chưa được phân cóng phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/employee-detail?id=" + employeeId);
            return;
        }

        boolean success = employeeDAO.unassignEmployee(employeeId);
        if (success) {
            LOGGER.log(Level.INFO, "Employee unassigned: employeeId={0}", employeeId);
            request.getSession().setAttribute("success",
                    "Đã gỡ phân cóng nhân viên. Hệ thống admin có thể đổi vai trò, sau đã phân cóng lại vào phòng phù hợp.");
        } else {
            request.getSession().setAttribute("error", "Gỡ phân cóng thất bại. Vui lêng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/employee/employee-detail?id=" + employeeId);
    }

    private void displayAddDepartmentForm(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {

        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm phòng ban bởi bạn không phải HR");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        if (!hasPermission(user, "EDIT_DEPARTMENTS")) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("roles", roleDAO.getAllActiveRoles());
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/department/add_department.jsp").forward(request, response);
    }

    private void handleAddDepartment(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm phòng ban bởi bạn không phải HR");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        if (!hasPermission(user, "EDIT_DEPARTMENTS")) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        String code = request.getParameter("departmentCode");
        String name = request.getParameter("departmentName");
        String description = request.getParameter("description");
        List<Integer> roleIds = parseRoleIds(request.getParameterValues("roleIds"));

        if (isBlank(code) || isBlank(name)) {
            request.setAttribute("error", "Mã phòng ban và tên phòng ban là bắt buộc.");
            request.setAttribute("input_code", code);
            request.setAttribute("input_name", name);
            request.setAttribute("input_description", description);
            request.setAttribute("roles", roleDAO.getAllActiveRoles());
            request.setAttribute("selectedRoleIds", roleIds);
            setPermissionFlags(request, getPermissions((User) request.getSession().getAttribute("user")));
            request.getRequestDispatcher("/public/employee/department/add_department.jsp").forward(request, response);
            return;
        }

        Department dept = new Department();
        dept.setDepartmentCode(code.trim());
        dept.setDepartmentName(name.trim());
        dept.setDescription(isBlank(description) ? null : description.trim());

        int newDeptId = departmentDAO.addDepartment(dept);
        if (newDeptId <= 0) {
            request.setAttribute("error", "Thêm phòng ban thất bại. Vui lêng thử lại.");
            request.setAttribute("input_code", code);
            request.setAttribute("input_name", name);
            request.setAttribute("input_description", description);
            request.setAttribute("roles", roleDAO.getAllActiveRoles());
            request.setAttribute("selectedRoleIds", roleIds);
            setPermissionFlags(request, getPermissions((User) request.getSession().getAttribute("user")));
            request.getRequestDispatcher("/public/employee/department/add_department.jsp").forward(request, response);
            return;
        }

        if (!roleIds.isEmpty()) {
            departmentDAO.replaceDepartmentRoles(newDeptId, roleIds);
        }

        LOGGER.log(Level.INFO, "Department created: code={0} by userId={1}", new Object[]{code, user.getUserId()});
        request.getSession().setAttribute("success", "Thêm phòng ban \"" + name.trim() + "\" thành cóng.");
        response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
    }

    private void displayUpdateDepartmentForm(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {

        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền sửa phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        if (!hasPermission(user, "EDIT_DEPARTMENTS")) {
            request.getSession().setAttribute("error", "Bạn không có quyền sửa phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        String idParam = request.getParameter("id");
        if (isBlank(idParam)) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
            return;
        }
        int deptId;
        try {
            deptId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
            return;
        }
        Department dept = departmentDAO.getDepartmentById(deptId);
        if (dept == null) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("department", dept);
        request.setAttribute("roles", roleDAO.getAllActiveRoles());

        List<Role> activeRoles = roleDAO.getAllActiveRoles();
        List<String> allowedRoles = departmentDAO.getAllowedRoleNames(deptId);
        List<Integer> selectedRoleIds = new ArrayList<>();
        for (Role r : activeRoles) {
            if (allowedRoles.contains(r.getRoleName())) {
                selectedRoleIds.add(r.getRoleId());
            }
        }
        request.setAttribute("selectedRoleIds", selectedRoleIds);

        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/department/update_department.jsp").forward(request, response);
    }

    private void handleUpdateDepartment(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {

        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền sửa phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        if (!hasPermission(user, "EDIT_DEPARTMENTS")) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        String idParam = request.getParameter("departmentId");
        String name = request.getParameter("departmentName");
        String description = request.getParameter("description");
        List<Integer> roleIds = parseRoleIds(request.getParameterValues("roleIds"));

        if (isBlank(idParam) || isBlank(name)) {
            request.getSession().setAttribute("error", "Tên phòng ban là bắt buộc.");
            response.sendRedirect(
                    request.getContextPath() + "/v1/employee/department/update?id=" + (idParam != null ? idParam : ""));
            return;
        }

        int deptId;
        try {
            deptId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
            return;
        }

        Department dept = departmentDAO.getDepartmentById(deptId);
        if (dept == null) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
            return;
        }

        dept.setDepartmentName(name.trim());
        dept.setDescription(isBlank(description) ? null : description.trim());

        String statusStr = request.getParameter("status");
        if (statusStr != null) {
            try {
                dept.setStatus(Integer.parseInt(statusStr));
            } catch (NumberFormatException ignored) {
            }
        }

        boolean success = departmentDAO.updateDepartmentInfo(dept);
        if (success) {
            departmentDAO.replaceDepartmentRoles(deptId, roleIds);
            request.getSession().setAttribute("success", "Cập nhật phòng ban thành cóng.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
        } else {
            request.getSession().setAttribute("error", "Cập nhật thất bại. Vui lêng thử lại.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/update?id=" + deptId);
        }
    }

    private void handleUpdateEmployee(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền chỉnh sửa nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        if (!hasPermission(user, "EDIT_EMPLOYEE")) {
            request.getSession().setAttribute("error", "Bạn không có quyền chỉnh sửa nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        String idParam = request.getParameter("employeeId");
        String statusParam = request.getParameter("status");

        int employeeId, status;
        try {
            employeeId = Integer.parseInt(idParam);
            status = Integer.parseInt(statusParam);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Dữ liệu nhân viên không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/employee-list");
            return;
        }

        if (!isValidEmployeeStatus(status)) {
            request.getSession().setAttribute("error", "Trạng thái nhân viên không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/update-employee?id=" + employeeId);
            return;
        }

        EmployeeDetailDTO current = employeeDAO.getEmployeeById(employeeId);
        if (current == null) {
            request.getSession().setAttribute("error", "Không tìm thấy nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/employee-list");
            return;
        }

        int departmentId = current.getDepartmentId();
        int positionId = current.getPositionId();
        int userRoleId = userDAO.getRoleIdByUserId(current.getUserId());
        if (!departmentDAO.isRoleAllowedForDepartment(departmentId, userRoleId)) {
            request.getSession().setAttribute("error",
                    "Vai trò hiện tại của nhân viên không phù hợp với phòng ban đã chọn.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/update-employee?id=" + employeeId);
            return;
        }

        Employee emp = new Employee();
        emp.setEmployeeId(employeeId);
        emp.setDepartmentId(departmentId);
        emp.setPositionId(positionId);
        emp.setStatus(status);
        emp.setManagerId(current.getManagerId());
        emp.setPhoneNumber(trimToNull(request.getParameter("phoneNumber")));
        emp.setSkills(trimToNull(request.getParameter("skills")));
        emp.setExperience(trimToNull(request.getParameter("experience")));
        emp.setDegree(trimToNull(request.getParameter("degree")));

        boolean success = employeeDAO.updateEmployee(emp);
        if (success) {
            request.getSession().setAttribute("success", "Cập nhật thông tin nhân viên thành cóng.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/employee-detail?id=" + employeeId);
        } else {
            request.getSession().setAttribute("error", "Cập nhật nhân viên thất bại. Vui lêng thử lại.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/update-employee?id=" + employeeId);
        }
    }

    private void handleAddContract(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm hợp đồng lao động.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        if (!hasPermission(user, "ADD_EMPLOYMENT_CONTRACT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm hợp đồng lao động.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        String code = trimToNull(request.getParameter("contractCode"));
        String type = trimToNull(request.getParameter("contractType"));
        String employeeParam = request.getParameter("employeeId");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String salaryParam = request.getParameter("salary");

        if (code == null || type == null || isBlank(employeeParam) || isBlank(startDate) || isBlank(salaryParam)) {
            request.setAttribute("error",
                    "Vui lêng nhập đầy đủ mã hợp đồng, nhân viên, loại hợp đồng, ngày bắt đầu và lương.");
            request.setAttribute("employees", employeeDAO.getAllEmployees());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/contract/add_contract.jsp").forward(request, response);
            return;
        }

        EmploymentContract contract = new EmploymentContract();
        try {
            contract.setEmployeeId(Integer.parseInt(employeeParam));
            contract.setStartDate(java.sql.Date.valueOf(startDate));
            contract.setEndDate(isBlank(endDate) ? null : java.sql.Date.valueOf(endDate));
            contract.setSalary(new BigDecimal(salaryParam));
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", "Dữ liệu hợp đồng không hợp lệ.");
            request.setAttribute("employees", employeeDAO.getAllEmployees());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/contract/add_contract.jsp").forward(request, response);
            return;
        }

        if (!isValidContractType(type) || contract.getSalary().compareTo(BigDecimal.ZERO) < 0) {
            request.setAttribute("error", "Loại hợp đồng hoặc lương không hợp lệ.");
            request.setAttribute("employees", employeeDAO.getAllEmployees());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/contract/add_contract.jsp").forward(request, response);
            return;
        }

        if (employeeDAO.getEmployeeById(contract.getEmployeeId()) == null) {
            request.setAttribute("error", "Nhân viên được chọn không tồn tại.");
            request.setAttribute("employees", employeeDAO.getAllEmployees());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/contract/add_contract.jsp").forward(request, response);
            return;
        }

        if (contractDAO.hasActiveContract(contract.getEmployeeId())) {
            request.setAttribute("error", "Hợp đồng của nhân viên vẫn cón hiệu lực");
            request.setAttribute("employees", employeeDAO.getAllEmployees());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/contract/add_contract.jsp").forward(request, response);
            return;
        }

        if (contract.getEndDate() != null && contract.getEndDate().before(contract.getStartDate())) {
            request.setAttribute("error", "Ngày kết thúc không được trước ngày bắt đầu.");
            request.setAttribute("employees", employeeDAO.getAllEmployees());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/contract/add_contract.jsp").forward(request, response);
            return;
        }

        contract.setContractCode(code);
        contract.setContractType(type);
        contract.setStatus(1);
        contract.setNote(trimToNull(request.getParameter("note")));
        contract.setCreatedBy(user.getUserId());

        boolean success = contractDAO.addContract(contract);
        if (success) {
            request.getSession().setAttribute("success", "Thêm hợp đồng lao động thành cóng.");
            response.sendRedirect(
                    request.getContextPath() + "/v1/employee/contract/preview?employeeId=" + contract.getEmployeeId());
        } else {
            request.setAttribute("error", "Thêm hợp đồng thất bại. Mã hợp đồng có thể đã tồn tại.");
            request.setAttribute("employees", employeeDAO.getAllEmployees());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/contract/add_contract.jsp").forward(request, response);
        }
    }

    private EmploymentContract getContractFromRequest(HttpServletRequest request) {
        String contractIdParam = request.getParameter("id");
        if (!isBlank(contractIdParam)) {
            try {
                EmploymentContract contract = contractDAO.getContractById(Integer.parseInt(contractIdParam));
                if (contract == null) {
                    request.getSession().setAttribute("error", "Không tìm thấy hợp đồng.");
                }
                return contract;
            } catch (NumberFormatException e) {
                request.getSession().setAttribute("error", "Mã hợp đồng không hợp lệ.");
                return null;
            }
        }

        String employeeIdParam = request.getParameter("employeeId");
        if (isBlank(employeeIdParam)) {
            request.getSession().setAttribute("error", "Thiếu mã hợp đồng hoặc mã nhân viên.");
            return null;
        }
        try {
            EmploymentContract contract = contractDAO.getLatestContractByEmployeeId(Integer.parseInt(employeeIdParam));
            if (contract == null) {
                request.getSession().setAttribute("error", "Nhân viên này chưa có hợp đồng lao động.");
            }
            return contract;
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã nhân viên không hợp lệ.");
            return null;
        }
    }

    private EmployeeDetailDTO getEmployeeFromRequest(HttpServletRequest request, HttpServletResponse response) {
        String idParam = request.getParameter("id");
        if (isBlank(idParam)) {
            request.getSession().setAttribute("error", "Thiếu mã nhân viên.");
            return null;
        }
        try {
            int employeeId = Integer.parseInt(idParam);
            EmployeeDetailDTO employee = employeeDAO.getEmployeeById(employeeId);
            if (employee == null) {
                request.getSession().setAttribute("error", "Không tìm thấy nhân viên.");
            }
            return employee;
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã nhân viên không hợp lệ.");
            return null;
        }
    }

    private void handleUpdateMyProfile(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        String phoneNumber = request.getParameter("phoneNumber");
        String skills = request.getParameter("skills");
        String experience = request.getParameter("experience");
        String degree = request.getParameter("degree");

        EmployeeDetailDTO myEmployee = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (myEmployee == null) {
            request.getSession().setAttribute("error", "Không tìm thấy hồ sơ nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/my-profile");
            return;
        }

        boolean success = employeeDAO.updateOwnProfile(
                myEmployee.getEmployeeId(),
                isBlank(phoneNumber) ? null : phoneNumber.trim(),
                isBlank(skills) ? null : skills.trim(),
                isBlank(experience) ? null : experience.trim(),
                isBlank(degree) ? null : degree.trim());

        if (success) {
            request.getSession().setAttribute("success", "Cập nhật hồ sơ thành cóng.");
        } else {
            request.getSession().setAttribute("error", "Cập nhật thất bại. Vui lêng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/employee/my-profile");
    }


    private void handleUpdateEmployeeDetail(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền cập nhật nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        String rawEmployeeId = request.getParameter("employeeId");
        String rawStatus = request.getParameter("status");
        String phoneNumber = request.getParameter("phoneNumber");
        String degree = request.getParameter("degree");
        String experience = request.getParameter("experience");
        String skills = request.getParameter("skills");

        if (isBlank(rawEmployeeId) || isBlank(rawStatus)) {
            request.getSession().setAttribute("error", "Dữ liệu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
            return;
        }

        int employeeId;
        int status;
        try {
            employeeId = Integer.parseInt(rawEmployeeId);
            status = Integer.parseInt(rawStatus);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Dữ liệu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
            return;
        }

        EmployeeDetailDTO employeeDetail = employeeDAO.getEmployeeById(employeeId);
        if (employeeDetail == null) {
            request.getSession().setAttribute("error", "Không tìm thấy nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
            return;
        }

        boolean statusSuccess = employeeDAO.updateEmployeeStatus(employeeId, status);
        boolean profileSuccess = employeeDAO.updateOwnProfile(
                employeeId,
                isBlank(phoneNumber) ? null : phoneNumber.trim(),
                isBlank(skills) ? null : skills.trim(),
                isBlank(experience) ? null : experience.trim(),
                isBlank(degree) ? null : degree.trim());

        if (statusSuccess || profileSuccess) {
            request.getSession().setAttribute("success", "Cập nhật nhân viên thành cóng.");
        } else {
            request.getSession().setAttribute("error", "Cập nhật thất bại hoặc không có thay đổi.");
        }

        if (employeeDetail.getDepartmentId() > 0) {
            response.sendRedirect(
                    request.getContextPath() + "/v1/employee/department/detail?id=" + employeeDetail.getDepartmentId());
        } else {
            response.sendRedirect(request.getContextPath() + "/v1/employee/employee-detail?id=" + employeeId);
        }
    }

    private void displayMyForms(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        EmployeeDetailDTO em = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (em == null) {
            request.getSession().setAttribute("error", "Bạn chưa được gắn hồ sơ nhân viên");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        Integer day = parseIntOrNull(request.getParameter("day"));
        Integer month = parseIntOrNull(request.getParameter("month"));
        Integer year = parseIntOrNull(request.getParameter("year"));

        request.setAttribute("filterDay", day);
        request.setAttribute("filterMonth", month);
        request.setAttribute("filterYear", year);
        request.setAttribute("forms",
                formRequestDAO.getAllFormRequestsByEmployeeId(em.getEmployeeId(), day, month, year));
        request.getRequestDispatcher("/public/employee/forms/my_form_list.jsp").forward(request, response);
    }

    private void displayAllForms(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!hasPermission(user, "VIEW_ALL_FORMS")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem tất cả đơn");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        Integer day = parseIntOrNull(request.getParameter("day"));
        Integer month = parseIntOrNull(request.getParameter("month"));
        Integer year = parseIntOrNull(request.getParameter("year"));
        String keyword = request.getParameter("keyword");

        request.setAttribute("forms", formRequestDAO.getAllFormRequests(day, month, year, keyword));
        request.setAttribute("departments", departmentDAO.getAllActiveDepartments());

        request.setAttribute("filterDay", day);
        request.setAttribute("filterMonth", month);
        request.setAttribute("filterYear", year);
        request.setAttribute("keyword", keyword);
        request.getRequestDispatcher("/public/employee/forms/all_form_list.jsp").forward(request, response);
    }

    private void displayFormDetail(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String formIdRaw = request.getParameter("id");
        if (isBlank(formIdRaw)) {
            request.getSession().setAttribute("error", "Mã đơn không tồn tại");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        try {
            int id = Integer.parseInt(formIdRaw);
            FormRequestDTO form = formRequestDAO.getFormRequestById(id);
            if (form == null) {
                request.getSession().setAttribute("error", "Không tìm thấy đơn yêu cầu");
                response.sendRedirect(request.getContextPath() + "/v1/employee/forms/my-forms");
                return;
            }
            EmployeeDetailDTO me = employeeDAO.getEmployeeByUserId(user.getUserId());
            if (me == null) {
                request.getSession().setAttribute("error", "Bạn chưa được gắn hồ sơ nhân viên");
                response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
                return;
            }
            EmployeeDetailDTO formOwner = employeeDAO.getEmployeeById(form.getEmployeeId());
            Department dept = (formOwner != null) ? departmentDAO.getDepartmentById(formOwner.getDepartmentId()) : null;

            boolean isMyForm = (form.getEmployeeId() == me.getEmployeeId());
            boolean canViewAll = getPermissions(user).contains("VIEW_ALL_FORMS");
            boolean isDeptManager = (dept != null && dept.getManagerId() != null
                    && dept.getManagerId() == me.getEmployeeId());
            if (!isMyForm && !canViewAll && !isDeptManager) {
                request.getSession().setAttribute("error", "Bạn không có quyền xem đơn này");
                response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
                return;
            }

            String backUrl = "/v1/employee/forms/my-forms";
            if (canViewAll) {
                backUrl = "/v1/employee/forms/all";
            }
            request.setAttribute("backUrl", backUrl);

            request.setAttribute("form", form);
            request.getRequestDispatcher("/public/employee/forms/form_detail.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã đơn không hợp lệ");
            response.sendRedirect(request.getContextPath() + "/v1/employee/forms/my-forms");
        }
    }

    private void displayLeaveForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);

        EmployeeDetailDTO me = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (me != null) {
            int currentYear = LocalDate.now().getYear();
            LeaveBalance lb = leaveBalanceDAO.getLeaveBalance(me.getEmployeeId(), currentYear);
            if (lb == null) {
                lb = new LeaveBalance(0, me.getEmployeeId(), currentYear, 12, 0);
                leaveBalanceDAO.createLeaveBalance(lb);
            }
            request.setAttribute("remainingDays", lb.getRemainingDays());
        }

        request.getRequestDispatcher("/public/employee/forms/leave_form.jsp").forward(request, response);
    }

    // Hiển thị trang Đơn Khiếu Nại
    private void displayComplaintForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/forms/complaint_form.jsp").forward(request, response);
    }

    private void handleLeaveFormSubmit(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        FormType ft = formTypeDAO.getByCode("LEAVE");
        if (ft == null) {
            request.getSession().setAttribute("error", "Loại đơn LEAVE không tồn tại.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        int formTypeId = ft.getFormTypeId();

        EmployeeDetailDTO me = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (me == null) {
            request.getSession().setAttribute("error", "Bạn chưa được gắn hồ sơ nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        String reason = request.getParameter("reason");
        String rawStart = trimToNull(request.getParameter("startDate"));
        String rawEnd = trimToNull(request.getParameter("endDate"));

        if (rawStart == null || rawEnd == null) {
            request.setAttribute("error", "Đơn nghỉ phép yêu cầu nhập ngày bắt đầu và ngày kết thúc.");
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/forms/leave_form.jsp").forward(request, response);
            return;
        }

        Date startDate, endDate;
        try {
            startDate = Date.valueOf(rawStart);
            endDate = Date.valueOf(rawEnd);
        } catch (IllegalArgumentException ex) {
            request.setAttribute("error", "Ngày không hợp lệ. Vui lòng nhập đúng định dạng.");
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/forms/leave_form.jsp").forward(request, response);
            return;
        }

        LocalDate today = LocalDate.now();
        if (startDate.toLocalDate().isBefore(today)) {
            request.setAttribute("error", "Ngày bắt đầu không được là ngày trong quá khứ.");
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/forms/leave_form.jsp").forward(request, response);
            return;
        }
        if (endDate.before(startDate)) {
            request.setAttribute("error", "Ngày kết thúc không được trước ngày bắt đầu.");
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/forms/leave_form.jsp").forward(request, response);
            return;
        }

        long diffMs = endDate.getTime() - startDate.getTime();
        int totalDays = (int) (diffMs / (1000L * 60 * 60 * 24)) + 1;

        int currentYear = LocalDate.now().getYear();
        LeaveBalance lb = leaveBalanceDAO.getLeaveBalance(me.getEmployeeId(), currentYear);
        int remaining = 0;
        if (lb != null) {
            remaining = lb.getRemainingDays();
        }
        if (totalDays > remaining) {
            request.setAttribute("error", "Số ngày nghỉ vượt quá số ngày phép còn lại (" + remaining + " ngày).");
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/forms/leave_form.jsp").forward(request, response);
            return;
        }

        // Kiểm tra đơn trùng lặp
        if (formRequestDAO.hasOverlappingLeave(me.getEmployeeId(), startDate, endDate)) {
            request.setAttribute("error", "Bạn đã có đơn xin nghỉ phép (Chờ duyệt hoặc Đã duyệt) trong khoảng thời gian này. Vui lòng kiểm tra lại!");
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/forms/leave_form.jsp").forward(request, response);
            return;
        }

        // Xử lý file đính kèm
        String savedUrl = null, savedName = null;
        Part filePart = request.getPart("attachment");
        if (filePart != null && filePart.getSize() > 0) {
            String submitted = filePart.getSubmittedFileName();
            if (submitted != null && !submitted.isEmpty()) {
                String ext = submitted.contains(".") ? submitted.substring(submitted.lastIndexOf('.')).toLowerCase()
                        : "";
                String[] allowed = {".xlsx", ".pdf", ".docx", ".doc", ".xls", ".jpg", ".png", ".zip"};
                boolean ok = false;
                for (String a : allowed) {
                    if (a.equals(ext)) {
                        ok = true;
                        break;
                    }
                }
                if (!ok) {
                    request.setAttribute("error", "Định dạng file không hợp lệ.");
                    setPermissionFlags(request, getPermissions(user));
                    request.getRequestDispatcher("/public/employee/forms/leave_form.jsp").forward(request, response);
                    return;
                }
                String uploadDir = "uploads/forms";
                String serverName = "FORM_" + me.getEmployeeId() + "_" + System.currentTimeMillis() + "_"
                        + java.util.UUID.randomUUID().toString().substring(0, 8) + ext;
                Path dir = Paths.get(getServletContext().getRealPath("/" + uploadDir));
                Files.createDirectories(dir);
                try (InputStream is = filePart.getInputStream()) {
                    Files.copy(is, dir.resolve(serverName));
                }
                savedUrl = uploadDir + "/" + serverName;
                savedName = sanitizeFileName(submitted);
            }
        }

        LeaveFormRequest fr = new LeaveFormRequest();
        fr.setFormCode("LEAVE-" + me.getEmployeeId() + "-" + System.currentTimeMillis());
        fr.setEmployeeId(me.getEmployeeId());
        fr.setFormTypeId(formTypeId);
        fr.setReason(isBlank(reason) ? null : reason.trim());
        fr.setStartDate(startDate);
        fr.setEndDate(endDate);
        fr.setTotalDays(totalDays);
        fr.setAttachmentUrl(savedUrl);
        fr.setAttachmentName(savedName);

        int id = formRequestDAO.addFormRequest(fr);
        if (id <= 0) {
            request.setAttribute("error", "Gửi đơn thất bại. Vui lòng thử lại.");
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/forms/leave_form.jsp").forward(request, response);
            return;
        }
        request.getSession().setAttribute("success", "Đã gửi đơn nghỉ phép thành cóng.");
        response.sendRedirect(request.getContextPath() + "/v1/employee/forms/my-forms");
    }

    // Xử lý gửi Đơn Khiếu Nại
    private void handleComplaintFormSubmit(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        FormType ft = formTypeDAO.getByCode("COMPLAINT");
        if (ft == null) {
            request.getSession().setAttribute("error", "Loại đơn COMPLAINT không tồn tại.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        int formTypeId = ft.getFormTypeId();

        EmployeeDetailDTO me = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (me == null) {
            request.getSession().setAttribute("error", "Bạn chưa được gắn hồ sơ nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        String reason = request.getParameter("reason");
        String rawDate = trimToNull(request.getParameter("startDate"));
        String rawStartTime = trimToNull(request.getParameter("startTime"));
        String rawEndTime = trimToNull(request.getParameter("endTime"));

        if (isBlank(reason)) {
            request.setAttribute("error", "Vui lòng nhập nội dung khiếu nại.");
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/forms/complaint_form.jsp").forward(request, response);
            return;
        }

        if (rawDate == null || rawStartTime == null || rawEndTime == null) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ ngày và giờ làm việc cần sửa.");
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/forms/complaint_form.jsp").forward(request, response);
            return;
        }

        Date startDate;
        Time startTime, endTime;
        try {
            startDate = Date.valueOf(rawDate);
            // Append seconds for Time.valueOf format (HH:mm:ss)
            startTime = Time.valueOf(rawStartTime.length() == 5 ? rawStartTime + ":00" : rawStartTime);
            endTime = Time.valueOf(rawEndTime.length() == 5 ? rawEndTime + ":00" : rawEndTime);
        } catch (IllegalArgumentException ex) {
            request.setAttribute("error", "Định dạng ngày/giờ không hợp lệ.");
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/forms/complaint_form.jsp").forward(request, response);
            return;
        }

        if (!endTime.after(startTime)) {
            request.setAttribute("error", "Giờ kết thúc phải lớn hơn giờ bắt đầu.");
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/forms/complaint_form.jsp").forward(request, response);
            return;
        }

        // Xử lý file đính kèm
        String savedUrl = null, savedName = null;
        Part filePart = request.getPart("attachment");
        if (filePart != null && filePart.getSize() > 0) {
            String submitted = filePart.getSubmittedFileName();
            if (submitted != null && !submitted.isEmpty()) {
                String ext = submitted.contains(".") ? submitted.substring(submitted.lastIndexOf('.')).toLowerCase()
                        : "";
                String[] allowed = {".xlsx", ".pdf", ".docx", ".doc", ".xls", ".jpg", ".png", ".zip"};
                boolean ok = false;
                for (String a : allowed) {
                    if (a.equals(ext)) {
                        ok = true;
                        break;
                    }
                }
                if (!ok) {
                    request.setAttribute("error", "Định dạng file không hợp lệ.");
                    setPermissionFlags(request, getPermissions(user));
                    request.getRequestDispatcher("/public/employee/forms/complaint_form.jsp").forward(request, response);
                    return;
                }
                String uploadDir = "uploads/forms";
                String serverName = "FORM_" + me.getEmployeeId() + "_" + System.currentTimeMillis() + "_"
                        + java.util.UUID.randomUUID().toString().substring(0, 8) + ext;
                Path dir = Paths.get(getServletContext().getRealPath("/" + uploadDir));
                Files.createDirectories(dir);
                try (InputStream is = filePart.getInputStream()) {
                    Files.copy(is, dir.resolve(serverName));
                }
                savedUrl = uploadDir + "/" + serverName;
                savedName = sanitizeFileName(submitted);
            }
        }

        ComplaintFormRequest fr = new ComplaintFormRequest();
        fr.setFormCode("COMPLAINT-" + me.getEmployeeId() + "-" + System.currentTimeMillis());
        fr.setEmployeeId(me.getEmployeeId());
        fr.setFormTypeId(formTypeId);
        fr.setReason(reason.trim());
        fr.setStartDate(startDate);
        fr.setStartTime(startTime);
        fr.setEndTime(endTime);
        fr.setAttachmentUrl(savedUrl);
        fr.setAttachmentName(savedName);

        int id = formRequestDAO.addFormRequest(fr);
        if (id <= 0) {
            request.setAttribute("error", "Gửi đơn thất bại. Vui lòng thử lại.");
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/forms/complaint_form.jsp").forward(request, response);
            return;
        }
        request.getSession().setAttribute("success", "Đã gửi đơn khiếu nại thành cóng.");
        response.sendRedirect(request.getContextPath() + "/v1/employee/forms/my-forms");
    }

    private List<Integer> parseRoleIds(String[] raw) {
        List<Integer> ids = new ArrayList<>();
        if (raw != null) {
            for (String r : raw) {
                try {
                    ids.add(Integer.parseInt(r));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return ids;
    }

    private void displayRecruitmentList(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem tuyển dụng.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        String stage = trimToNull(request.getParameter("stage"));
        if (stage == null) {
            stage = "APPLIED";
        }
        String keyword = trimToNull(request.getParameter("keyword"));

        List<Candidate> candidates = keyword == null
                ? candidateDAO.getByStage(stage)
                : candidateDAO.searchByName(stage, keyword);

        int pageSize = 7;
        int currentPage = parseIntOrNull(request.getParameter("page")) == null
                ? 1 : Math.max(1, parseIntOrNull(request.getParameter("page")));
        int totalCandidates = candidates.size();
        int totalPages = Math.max(1, (int) Math.ceil(totalCandidates / (double) pageSize));
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }
        int from = Math.min((currentPage - 1) * pageSize, totalCandidates);
        int to = Math.min(from + pageSize, totalCandidates);

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("candidates", candidates.subList(from, to));
        request.setAttribute("currentStage", stage);
        request.setAttribute("keyword", keyword);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalCandidates", totalCandidates);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/recruitment/recruitment_list.jsp").forward(request, response);
    }

    private void displayRecruitmentDetail(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem tuyển dụng.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        Integer candidateId = parseIntOrNull(request.getParameter("id"));
        if (candidateId == null) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/recruitment-list");
            return;
        }
        Candidate candidate = candidateDAO.getById(candidateId);
        if (candidate == null) {
            request.getSession().setAttribute("error", "Không tìm thấy ứng viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/recruitment-list");
            return;
        }
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("candidate", candidate);
        request.setAttribute("latestLog", candidateDAO.getLatestLog(candidateId));
        request.setAttribute("logs", candidateDAO.getLogsByCandidateId(candidateId));
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/recruitment/recruitment_detail.jsp").forward(request, response);
    }

    private void handleRecruitmentReview(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "PROCESS_RECRUITMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xử lý tuyển dụng.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        Integer candidateId = parseIntOrNull(request.getParameter("candidateId"));
        String result = trimToNull(request.getParameter("result"));
        String note = trimToNull(request.getParameter("note"));
        String toEmail = trimToNull(request.getParameter("toEmail"));
        String emailSubject = trimToNull(request.getParameter("emailSubject"));
        String emailBody = trimToNull(request.getParameter("emailBody"));
        if (candidateId == null || isBlank(result) || isBlank(toEmail)
                || isBlank(emailSubject) || isBlank(emailBody)) {
            request.getSession().setAttribute("error", "Thieu thong tin xu ly tuyen dung.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/recruitment-list");
            return;
        }

        Candidate candidate = candidateDAO.getById(candidateId);
        EmployeeDetailDTO reviewer = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (candidate == null || reviewer == null) {
            request.getSession().setAttribute("error", "Không tìm thấy dữ liệu ứng viên hoặc người xử lý.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/recruitment-list");
            return;
        }

        String fromStage = candidate.getStage();
        String toStage;
        if ("PASSED".equals(result)) {
            toStage = "APPLIED".equals(fromStage) ? "INTERVIEW" : "PROBATION";
        } else {
            toStage = "REJECTED";
        }

        ApplicationStageLog log = new ApplicationStageLog();
        log.setCandidateId(candidateId);
        log.setFromStage(fromStage);
        log.setToStage(toStage);
        log.setResult(result);
        log.setReviewedBy(reviewer.getEmployeeId());
        log.setNote(note);
        log.setToEmail(toEmail);
        log.setEmailSubject(emailSubject);
        log.setEmailBody(emailBody);
        log.setEmailType("PASSED".equals(result) ? "ACCEPTED" : "REJECTED");

        int logId = candidateDAO.insertLog(log);
        if (logId <= 0) {
            request.getSession().setAttribute("error", "Không thể lưu lịch sử xử lý ứng viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/recruitment-detail?id=" + candidateId);
            return;
        }

        boolean sent = "PASSED".equals(result)
                ? emailService.sendAcceptedCandidateNotify(toEmail, emailSubject, emailBody)
                : emailService.sendRejectCandidateNotify(toEmail, emailSubject, emailBody);
        candidateDAO.updateEmailStatus(logId, sent ? "SENT" : "FAILED");
        if (!sent) {
            request.getSession().setAttribute("error", "Gui email that bai, trang thai ung vien chua duoc cap nhat.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/recruitment-detail?id=" + candidateId);
            return;
        }

        candidateDAO.updateStage(candidateId, toStage);
        request.getSession().setAttribute("success", "Đã xử lý ứng viên và gửi email thành công.");
        response.sendRedirect(request.getContextPath() + "/v1/employee/recruitment-list?stage=" + toStage);
    }

    private void displayRecruitmentImport(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "PROCESS_RECRUITMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền import ứng viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/recruitment/recruitment_import.jsp").forward(request, response);
    }

    private void handleImportCandidates(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "PROCESS_RECRUITMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền import ứng viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        Part filePart = request.getPart("file");
        if (filePart == null || filePart.getSize() == 0) {
            request.getSession().setAttribute("error", "Vui long chon file Excel.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/recruitment-import");
            return;
        }
        String submittedName = filePart.getSubmittedFileName();
        if (submittedName == null || !submittedName.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            request.getSession().setAttribute("error", "File phai co dinh dang .xlsx.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/recruitment-import");
            return;
        }

        Path dir = Paths.get(getServletContext().getRealPath("/" + UPLOAD_DIR));
        Files.createDirectories(dir);
        String serverFileName = "CAND_" + System.currentTimeMillis() + "_"
                + UUID.randomUUID().toString().substring(0, 8) + ".xlsx";
        Path savedPath = dir.resolve(serverFileName);
        try (InputStream is = filePart.getInputStream()) {
            Files.copy(is, savedPath);
        }

        EmployeeDetailDTO me = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (me == null || me.getDepartmentId() <= 0) {
            request.getSession().setAttribute("error", "Không tìm thấy phòng ban của bạn.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/recruitment-import");
            return;
        }

        UploadedFile uf = new UploadedFile();
        uf.setFileCode("CAND-" + System.currentTimeMillis());
        uf.setFileType("CANDIDATE");
        uf.setDepartmentId(me.getDepartmentId());
        uf.setEmployeeId(me.getEmployeeId());
        uf.setFileUrl(UPLOAD_DIR + "/" + serverFileName);
        uf.setFileName(sanitizeFileName(submittedName));
        uf.setMonth(0);
        uf.setYear(0);
        uf.setStatus(CandidateImportService.FILE_STATUS_PENDING);
        int fileId = uploadedFileDAO.createUploadedFile(uf);
        if (fileId <= 0) {
            request.getSession().setAttribute("error", "Không thể tạo bản ghi file import.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/recruitment-import");
            return;
        }

        CandidateImportResultDTO result;
        try (InputStream is = Files.newInputStream(savedPath)) {
            result = candidateImportService.importCandidates(is, fileId);
        }
        result.setFileName(uf.getFileName());
        uploadedFileDAO.updateImportResult(fileId, result.getTotalRows(), result.getImportedRows(),
                result.getFailedRows(), result.getStatus(), result.getNote());

        request.setAttribute("importResult", result);
        request.getRequestDispatcher("/public/employee/recruitment/recruitment_import.jsp").forward(request, response);
    }

    private boolean isAcceptableXlsxContentType(String contentType) {
        String ct = contentType.toLowerCase();
        return ct.contains("openxmlformats-officedocument.spreadsheetml.sheet")
                || ct.contains("application/octet-stream")
                || ct.contains("application/zip");
    }

    private String sanitizeFileName(String name) {
        if (name == null) {
            return "attendance.xlsx";
        }

        String base = Paths.get(name).getFileName().toString();
        return base.replaceAll("[\\r\\n]", "");
    }

    private Time parseTimeOrNull(String raw) {
        String value = trimToNull(raw);
        if (value == null) {
            return null;
        }
        if (value.matches("\\d{2}:\\d{2}")) {
            value += ":00";
        }
        return Time.valueOf(value);
    }

    private String buildAttendanceFilterQuery(HttpServletRequest request) {
        StringBuilder qs = new StringBuilder();
        appendParamIfPresent(qs, request, "month");
        appendParamIfPresent(qs, request, "year");
        appendParamIfPresent(qs, request, "departmentId");
        appendParamIfPresent(qs, request, "employeeCode");
        return qs.toString();
    }

    private String buildAttendanceDetailQuery(HttpServletRequest request) {
        StringBuilder qs = new StringBuilder();
        appendParamIfPresent(qs, request, "employeeId");
        appendParamIfPresent(qs, request, "departmentId");
        appendParamIfPresent(qs, request, "month");
        appendParamIfPresent(qs, request, "year");
        return qs.toString();
    }

    private void appendParamIfPresent(StringBuilder qs, HttpServletRequest request, String name) {
        String value = trimToNull(request.getParameter(name));
        if (value != null) {
            qs.append(qs.length() == 0 ? '?' : '&').append(name).append('=')
                    .append(java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private Integer parseIntOrNull(String v) {
        if (isBlank(v)) {
            return null;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int[] parseSalaryPeriod(HttpServletRequest request) {
        LocalDate now = LocalDate.now();
        Integer year = parseIntOrNull(request.getParameter("year"));
        Integer month = parseIntOrNull(request.getParameter("month"));
        if (year == null || year < 2000) {
            year = now.getYear();
        }
        if (month == null || month < 1 || month > 12) {
            month = now.getMonthValue();
        }
        return new int[]{year, month};
    }

    private String buildSalaryPeriodMessage(int year, int month) {
        java.time.YearMonth selected = java.time.YearMonth.of(year, month);
        java.time.YearMonth current = java.time.YearMonth.now();
        if (selected.isAfter(current)) {
            return "Tháng lương " + String.format("%02d/%d", month, year)
                    + " là tháng tương lai, hệ thống chưa thể hiển thị bảng lương.";
        }
        return "Chưa có bảng lương cho tháng " + String.format("%02d/%d", month, year)
                + ". Có thể tháng này chưa được chốt lương hoặc nhân sự chưa làm việc trong thời gian này.";
    }

    @SuppressWarnings("unchecked")
    private Set<String> getPermissions(User user) {
        Set<String> hs = permissionDAO.getPermissionCodeByUserId(user.getUserId());
        return hs;
    }

    private boolean hasPermission(User user, String code) {
        return getPermissions(user).contains(code);
    }

    private boolean isValidEmployeeStatus(int status) {
        return status == 0 || status == 1 || status == 2;
    }

    private boolean isAttendanceEditLocked(Date workDate) {
        if (workDate == null) {
            return false;
        }
        LocalDate deadline = workDate.toLocalDate()
                .withDayOfMonth(1)
                .plusMonths(1)
                .withDayOfMonth(5);
        return LocalDate.now().isAfter(deadline);
    }

    private boolean isValidContractType(String type) {
        return "Probation".equals(type)
                || "Full-time".equals(type)
                || "Part-time".equals(type)
                || "Fixed-term".equals(type);
    }

    private boolean isHrStaff(User user) {
        String role = roleDAO.getRoleByUserId(user.getUserId());
        return role.contains("HR");
    }

    private Integer attDepartmentParam(HttpServletRequest request) {
        int id = attParam(request, "departmentId", 0);
        return id > 0 ? id : null;
    }

    private void setEmployeeAttendanceLayout(HttpServletRequest request) {
        request.setAttribute("sidebarPath", "/public/components/employeeSideBar.jsp");
        request.setAttribute("topbarPath", "/public/components/employeeTopBar.jsp");
        request.setAttribute("baseUrl", request.getContextPath() + "/v1/employee/attendance");
        request.setAttribute("canViewAll", true);
    }

    private void setPermissionFlags(HttpServletRequest request, Set<String> perms) {
        request.setAttribute("canViewEmployees", perms.contains("VIEW_EMPLOYEES"));
        request.setAttribute("canAddEmployee", perms.contains("ADD_EMPLOYEE"));
        request.setAttribute("canAddEmploymentContract", perms.contains("ADD_EMPLOYMENT_CONTRACT"));
        request.setAttribute("canEditEmployee", perms.contains("EDIT_EMPLOYEE"));
        request.setAttribute("canDeleteEmployee", perms.contains("DELETE_EMPLOYEE"));
        request.setAttribute("canViewDepartments", perms.contains("VIEW_DEPARTMENTS"));
        request.setAttribute("canEditDepts", perms.contains("EDIT_DEPARTMENTS"));
        request.setAttribute("canAssignDept", perms.contains("ASSIGN_DEPARTMENT"));
        request.setAttribute("canUnassignDept", perms.contains("UNASSIGN_DEPARTMENT"));
        request.setAttribute("canEditAttendance", perms.contains("EDIT_ATTENDANCE"));
        request.setAttribute("canViewOwnSalary", perms.contains("VIEW_OWN_SALARY"));
        request.setAttribute("canViewAllSalary", perms.contains("VIEW_ALL_SALARY"));
        request.setAttribute("canExportPayroll", perms.contains("EXPORT_PAYROLL"));
    }
    private boolean isBlank(String v) {
        return v == null || v.trim().isEmpty();
    }

    private void preventBackCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

    }
}
