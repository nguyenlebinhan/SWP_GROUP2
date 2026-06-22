package controller;

import dao.*;
import dto.AttendanceImportResultDTO;
import dto.CandidateImportResultDTO;
import dto.EmployeeDTO;
import dto.EmployeeDetailDTO;
import dto.PayrollPreviewDTO;
import dto.FormRequestDTO;
import dto.LeaveFormRequestDTO;
import dto.ComplaintFormRequestDTO;
import enums.FileStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.*;
import model.ApplicationStageLog;
import model.Attendance;
import model.Candidate;
import dto.OvertimeRequestDTO;
import java.time.LocalDate;
import model.Department;
import model.Employee;
import model.EmploymentContract;
import model.LeaveBalance;
import model.Position;
import model.Role;
import model.UploadedFile;
import model.User;
import service.CandidateImportService;
import service.EmailService;
import service.AttendanceImportService;
import service.PayrollService;
import dal.DBContext;
import utils.AttendanceExcelExporter;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024, // 1MB ghi ra đĩa
        maxFileSize = 10L * 1024 * 1024, // 10MB / file
        maxRequestSize = 11L * 1024 * 1024 // 11MB / request
)
public class ManagerController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ManagerController.class.getName());
    private static final UserDAO userDAO = new UserDAO();
    private static final RoleDAO roleDAO = new RoleDAO();
    private static final EmployeeDAO employeeDAO = new EmployeeDAO();
    private static final DepartmentDAO departmentDAO = new DepartmentDAO();
    private static final PermissionDAO permissionDAO = new PermissionDAO();
    private static final EmploymentContractDAO contractDAO = new EmploymentContractDAO();
    private static final FormRequestDAO formRequestDAO = new FormRequestDAO();
    private static final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private static final service.AttendanceService attendanceService = new service.AttendanceService();
    private static final AttendanceExcelExporter attendanceExporter = new utils.AttendanceExcelExporter();
    private static final dao.OvertimeDAO overtimeDAO = new dao.OvertimeDAO();
    private static final dao.FormTypeDAO formTypeDAO = new dao.FormTypeDAO();
    private static final LeaveBalanceDAO leaveBalanceDAO = new LeaveBalanceDAO();
    private static final CandidateDAO candidateDAO = new CandidateDAO();
    private static final UploadedFileDAO uploadedFileDAO = new UploadedFileDAO();
    private static final CandidateImportService candidateImportService = new CandidateImportService();
    private static final EmailService emailService = new EmailService();
    private static final AttendanceImportService importService = new AttendanceImportService();
    private static final PayrollService payrollService = new PayrollService();
    private static final String UPLOAD_DIR = "uploads";
    private static final String ATTENDANCE_FILE_PART = "attendanceFile";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        preventBackCache(response);

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        String action = request.getPathInfo();
        if (action == null || action.equals("/")) {
            action = "/dashboard";
        }
        switch (action) {
            case "/dashboard":
                displayDashboard(request, response, user);
                break;
            case "/department/my-department-list":
                displayMyDepartmentEmployees(request, response, user);
                break;
            case "/employee/list":
                displayEmployeeList(request, response, user);
                break;
            case "/employee/detail":
                displayEmployeeDetail(request, response, user);
                break;
            case "/employee/update":
                displayUpdateEmployeeForm(request, response, user);
                break;
            case "/contract/add":
                displayAddContractForm(request, response, user);
                break;
            case "/contract/preview":
                displayContractPreview(request, response, user);
                break;
            case "/department/employee-detail":
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
            case "/forms/all":
                displayAllForms(request, response, user);
                break;
            case "/forms/detail":
                displayFormDetail(request, response, user);
                break;
            case "/forms/dept-forms":
                displayDeptForms(request, response, user);
                break;
            case "/attendance/my-department-attendance":
                displayDepartmentAttendance(request, response, user);
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
            case "/attendance/import":
                displayImportForm(request, response, user);
                break;
            case "/attendance/own-attendance":
                displayOwnAttendance(request, response, user);
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
            case "/forms/ot-requests":
                displayOTRequests(request, response, user);
                break;
            case "/forms/create-ot":
                displayCreateOTForm(request, response, user);
                break;
            case "/forms/ot-detail":
                displayOTRequestDetail(request, response, user);
                break;
            case "/recruitment/list":
                displayRecruitmentList(request, response, user);
                break;
            case "/recruitment/detail":
                displayRecruitmentDetail(request, response, user);
                break;
            case "/recruitment/import":
                displayRecruitmentImport(request, response, user);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
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
        if (action == null || action.equals("/")) {
            displayDashboard(request, response, user);
            return;
        }
        switch (action) {
            case "/department/assign":
                handleAssignDepartment(request, response, user);
                break;
            case "/employee/update":
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
            case "/employee/update-detail":
                handleUpdateEmployeeDetail(request, response);
                break;
            case "/forms/approve":
                handleApproveForm(request, response, user);
                break;
            case "/forms/reject":
                handleRejectForm(request, response, user);
                break;
            case "/forms/create-ot":
                handleCreateOT(request, response, user);
                break;
            case "/forms/cancel-ot":
                handleCancelOT(request, response, user);
                break;
            case "/attendance/import":
                handleImportAttendance(request, response, user);
                break;
            case "/salary/approve":
                handleApprovePayroll(request, response, user);
                break;
            case "/salary/reject":
                handleRejectPayroll(request, response, user);
                break;
            case "/salary/generate":
                handleGeneratePayroll(request, response, user);
                break;
            case "/salary/confirm-own":
                handleConfirmOwnPayroll(request, response, user);
                break;
            case "/salary/report-own":
                handleReportOwnPayroll(request, response, user);
                break;
            case "/recruitment/review":
                handleRecruitmentReview(request, response, user);
                break;
            case "/recruitment/import":
                handleImportCandidates(request, response, user);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
                break;
        }
    }

    private void displayDashboard(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);

        int totalEmployees = employeeDAO.countTotal();
        int activeEmployees = employeeDAO.countActive();
        int inactiveEmployees = employeeDAO.countInactive();
        Map<String, Integer> deptChart = employeeDAO.countByDepartment();

        request.setAttribute("totalEmployees", totalEmployees);
        request.setAttribute("activeEmployees", activeEmployees);
        request.setAttribute("inactiveEmployees", inactiveEmployees);
        request.setAttribute("pendingLeaves", 0);
        request.setAttribute("deptChart", deptChart);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/dashboard.jsp").forward(request, response);
    }

    private void displayMyDepartmentEmployees(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);

        String keyword = request.getParameter("keyword") != null ? request.getParameter("keyword").trim() : "";
        String status = request.getParameter("status") != null ? request.getParameter("status").trim() : "";

        final int PAGE_SIZE = 5;
        int currentPage = 1;
        try {
            int p = Integer.parseInt(request.getParameter("page"));
            if (p > 1) {
                currentPage = p;
            }
        } catch (NumberFormatException ignored) {
        }
        int offset = (currentPage - 1) * PAGE_SIZE;

        EmployeeDetailDTO manager = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (manager == null || manager.getDepartmentId() <= 0) {
            request.setAttribute("departmentName", null);
            request.setAttribute("employees", java.util.Collections.emptyList());
            request.setAttribute("error", "Bạn chưa được phân cóng vào phòng ban nào.");
            setPermissionFlags(request, perms);
            request.getRequestDispatcher("/public/manager/employee_info/employee_list.jsp").forward(request, response);
            return;
        }

        int totalEmployees = employeeDAO.countEmployeesFiltered(null, manager.getDepartmentId(), keyword, null, status);
        int totalPages = (int) Math.ceil((double) totalEmployees / PAGE_SIZE);
        if (totalPages < 1) {
            totalPages = 1;
        }

        List<EmployeeDetailDTO> employees = employeeDAO.getEmployeesFiltered(null, manager.getDepartmentId(), keyword, null, status, offset, PAGE_SIZE);

        request.setAttribute("departmentName", manager.getDepartmentName());
        request.setAttribute("employees", employees);
        request.setAttribute("keyword", keyword);
        request.setAttribute("status", status);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalEmployees", totalEmployees);

        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/employee_info/employee_list.jsp").forward(request, response);
    }

    private void displayDepartmentAttendance(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);

        EmployeeDetailDTO manager = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (manager == null || manager.getDepartmentId() <= 0) {
            request.setAttribute("error", "Bạn chưa được phân cóng vào phòng ban nào.");
            request.getRequestDispatcher("/public/manager/attendance/department_attendance.jsp").forward(request, response);
            return;
        }

        String rawMonth = request.getParameter("month");
        String rawYear = request.getParameter("year");
        String employeeCode = request.getParameter("employeeCode");
        if (employeeCode != null) {
            employeeCode = employeeCode.trim();
        }

        Integer month = null;
        Integer year = null;
        try {
            if (rawMonth != null && !rawMonth.trim().isEmpty() && !rawMonth.equals("0")) {
                month = Integer.parseInt(rawMonth.trim());
            }
            if (rawYear != null && !rawYear.trim().isEmpty()) {
                year = Integer.parseInt(rawYear.trim());
            }
        } catch (NumberFormatException ignored) {
        }

        List<Attendance> attendances = attendanceDAO.getAttendanceList(
                manager.getDepartmentId(), month, year, employeeCode, null);

        request.setAttribute("attendances", attendances);
        request.setAttribute("filterMonth", month);
        request.setAttribute("filterYear", year);
        request.setAttribute("filterEmployeeCode", employeeCode);
        request.setAttribute("departmentName", manager.getDepartmentName());

        request.getRequestDispatcher("/public/manager/attendance/department_attendance.jsp").forward(request, response);
    }

    private void displayOwnAttendance(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);

        String rawMonth = request.getParameter("month");
        String rawYear = request.getParameter("year");
        Integer month = null;
        Integer year = null;
        try {
            if (rawMonth != null && !rawMonth.trim().isEmpty() && !rawMonth.equals("0")) {
                month = Integer.parseInt(rawMonth.trim());
            }
            if (rawYear != null && !rawYear.trim().isEmpty()) {
                year = Integer.parseInt(rawYear.trim());
            }
        } catch (NumberFormatException ignored) {
        }

        EmployeeDetailDTO me = employeeDAO.getEmployeeByUserId(user.getUserId());
        List<Attendance> attendances = (me != null)
                ? attendanceDAO.getAttendanceListByEmployeeId(me.getEmployeeId(), month, year)
                : new java.util.ArrayList<>();

        request.setAttribute("attendances", attendances);
        request.setAttribute("selectedMonth", month);
        request.setAttribute("selectedYear", year);
        request.getRequestDispatcher("/public/manager/attendance/own_attendance_list.jsp").forward(request, response);
    }

    private void displayOwnSalary(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);

        if (!payrollService.canViewOwnSalary(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem lương cá nhân.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
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
        request.getRequestDispatcher("/public/manager/salary/own_salary.jsp").forward(request, response);
    }

    private void displayAllSalary(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);

        if (!payrollService.canViewAllSalary(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem bảng lương tất cả nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
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
        request.getRequestDispatcher("/public/manager/salary/salary_list.jsp").forward(request, response);
    }

    private void displaySalaryDetail(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);

        if (!payrollService.canViewAllSalary(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem chi tiết bảng lương.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        Integer payrollId = parseIntOrNull(request.getParameter("id"));
        PayrollPreviewDTO payrollPreview = payrollId == null ? null : payrollService.getPayrollDetailForHr(user, payrollId);
        if (payrollPreview == null) {
            request.setAttribute("salaryError", "Không tìm thấy bảng lương cần xem chi tiết.");
        }
        request.setAttribute("payrollPreview", payrollPreview);
        request.setAttribute("canApprovePayroll", payrollId != null
                && payrollService.getPayrollApprovalError(user, payrollId) == null);
        request.getRequestDispatcher("/public/manager/salary/salary_detail.jsp").forward(request, response);
    }

    private void exportSalary(HttpServletRequest request, HttpServletResponse response,
            User user) throws IOException {
        if (!payrollService.canExportPayroll(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền xuất bảng lương.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
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

    private void handleApprovePayroll(HttpServletRequest request, HttpServletResponse response,
            User user) throws IOException {
        Integer payrollId = parseIntOrNull(request.getParameter("payrollId"));
        if (payrollId == null) {
            request.getSession().setAttribute("error", "Không tìm thấy bảng lương cần duyệt.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/salary/all");
            return;
        }
        String approvalError = payrollService.getPayrollApprovalError(user, payrollId);
        if (approvalError == null && payrollService.approvePayroll(user, payrollId)) {
            request.getSession().setAttribute("success", "Đã duyệt bảng lương.");
        } else {
            request.getSession().setAttribute("error", approvalError == null
                    ? "Không thể duyệt bảng lương này."
                    : approvalError);
        }
        response.sendRedirect(request.getContextPath() + "/v1/manager/salary/detail?id=" + payrollId);
    }

    private void handleRejectPayroll(HttpServletRequest request, HttpServletResponse response,
            User user) throws IOException {
        Integer payrollId = parseIntOrNull(request.getParameter("payrollId"));
        String rejectNote = request.getParameter("rejectNote");
        if (payrollId == null) {
            request.getSession().setAttribute("error", "Không tìm thấy bảng lương cần xử lý.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/salary/all");
            return;
        }
        String rejectionError = payrollService.getPayrollRejectionError(user, payrollId, rejectNote);
        if (rejectionError == null && payrollService.rejectPayroll(user, payrollId, rejectNote)) {
            request.getSession().setAttribute("success", "Đã đánh dấu bảng lương chưa được duyệt.");
        } else {
            request.getSession().setAttribute("error", rejectionError == null
                    ? "Không thể từ chối bảng lương này."
                    : rejectionError);
        }
        response.sendRedirect(request.getContextPath() + "/v1/manager/salary/detail?id=" + payrollId);
    }

    private void handleGeneratePayroll(HttpServletRequest request, HttpServletResponse response,
            User user) throws IOException {
        if (!payrollService.canViewAllSalary(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền tạo bảng lương.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        int[] period = parseSalaryPeriod(request);
        Integer departmentId = parseIntOrNull(request.getParameter("departmentId"));
        int generated = payrollService.saveGeneratedPayrollForPeriod(period[0], period[1], departmentId);
        request.getSession().setAttribute("success", "Đã tạo bảng lương cho " + generated + " nhân sự.");
        response.sendRedirect(request.getContextPath() + "/v1/manager/salary/all?month=" + period[1]
                + "&year=" + period[0]
                + (departmentId == null ? "" : "&departmentId=" + departmentId));
    }

    private void handleConfirmOwnPayroll(HttpServletRequest request, HttpServletResponse response,
            User user) throws IOException {
        Integer payrollId = parseIntOrNull(request.getParameter("payrollId"));
        if (payrollId == null) {
            request.getSession().setAttribute("error", "Không tìm thấy bảng lương cần xác nhận.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/salary/own");
            return;
        }
        int[] period = parseSalaryPeriod(request);
        String error = payrollService.getOwnPayrollActionError(user, payrollId);
        if (error == null && payrollService.confirmOwnPayroll(user, payrollId)) {
            request.getSession().setAttribute("success", "Đã xác nhận bảng lương cá nhân.");
        } else {
            request.getSession().setAttribute("error", error == null ? "Không thể xác nhận bảng lương này." : error);
        }
        response.sendRedirect(request.getContextPath() + "/v1/manager/salary/own?month=" + period[1]
                + "&year=" + period[0]);
    }

    private void handleReportOwnPayroll(HttpServletRequest request, HttpServletResponse response,
            User user) throws IOException {
        Integer payrollId = parseIntOrNull(request.getParameter("payrollId"));
        if (payrollId == null) {
            request.getSession().setAttribute("error", "Không tìm thấy bảng lương cần báo sai.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/salary/own");
            return;
        }
        int[] period = parseSalaryPeriod(request);
        String error = payrollService.getOwnPayrollActionError(user, payrollId);
        if (error == null && payrollService.reportOwnPayrollWrongInfo(user, payrollId)) {
            String reason = java.net.URLEncoder.encode("Bảng lương payrollId=" + payrollId
                    + " chưa đúng thông tin, vui lòng kiểm tra lại.", java.nio.charset.StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/v1/manager/forms/complaint/new?reason=" + reason);
        } else {
            request.getSession().setAttribute("error", error == null ? "Không thể báo sai bảng lương này." : error);
            response.sendRedirect(request.getContextPath() + "/v1/manager/salary/own?month=" + period[1]
                    + "&year=" + period[0]);
        }
    }

    // ===================== Attendance Dashboard (Overview / Detail / Export) =====================

    private Integer resolveManagerDepartmentId(User user) {
        EmployeeDetailDTO manager = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (manager == null || manager.getDepartmentId() <= 0) {
            return null;
        }
        return manager.getDepartmentId();
    }

    private int paramOr(HttpServletRequest request, String name, int defaultValue) {
        String raw = request.getParameter(name);
        if (raw != null && !raw.trim().isEmpty()) {
            try {
                return Integer.parseInt(raw.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private void setManagerLayout(HttpServletRequest request) {
        request.setAttribute("sidebarPath", "/public/components/managerSideBar.jsp");
        request.setAttribute("topbarPath", "/public/components/managerTopBar.jsp");
        request.setAttribute("baseUrl", request.getContextPath() + "/v1/manager/attendance");
    }

    private Integer optionalDepartmentParam(HttpServletRequest request) {
        int id = paramOr(request, "departmentId", 0);
        return id > 0 ? id : null;
    }

    private void displayAttendanceOverview(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);
        setManagerLayout(request);

        boolean canViewAll = perms.contains("VIEW_ALL_ATTENDANCE");
        boolean canViewDept = perms.contains("VIEW_DEPARTMENT_ATTENDANCE");
        if (!canViewAll && !canViewDept) {
            request.setAttribute("error", "Bạn không có quyền xem chấm công.");
            request.getRequestDispatcher("/public/manager/attendance/attendance_overview.jsp").forward(request, response);
            return;
        }

        LocalDate now = LocalDate.now();
        int month = paramOr(request, "month", now.getMonthValue());
        int year = paramOr(request, "year", now.getYear());

        Integer departmentId;
        if (canViewAll) {
            departmentId = optionalDepartmentParam(request); // null = toàn công ty
            request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
            request.setAttribute("canViewAll", true);
            if (departmentId != null) {
                model.Department dept = departmentDAO.getDepartmentById(departmentId);
                request.setAttribute("departmentName", dept != null ? dept.getDepartmentName() : "");
            }
        } else {
            departmentId = resolveManagerDepartmentId(user);
            request.setAttribute("canViewAll", false);
            if (departmentId == null) {
                request.setAttribute("error", "Bạn chưa được phân công vào phòng ban nào.");
                request.getRequestDispatcher("/public/manager/attendance/attendance_overview.jsp").forward(request, response);
                return;
            }
            model.Department dept = departmentDAO.getDepartmentById(departmentId);
            request.setAttribute("departmentName", dept != null ? dept.getDepartmentName() : "");
        }

        java.util.List<dto.AttendanceSummaryDTO> summaries
                = attendanceService.getMonthlySummaries(departmentId, month, year);
        request.setAttribute("summaries", summaries);
        request.setAttribute("pagedSummaries", utils.Paging.page(request, summaries));
        request.setAttribute("selectedDepartmentId", departmentId);
        request.setAttribute("selectedMonth", month);
        request.setAttribute("selectedYear", year);
        request.getRequestDispatcher("/public/manager/attendance/attendance_overview.jsp").forward(request, response);
    }

    private void displayAttendanceDetail(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);
        setManagerLayout(request);

        boolean canViewAll = perms.contains("VIEW_ALL_ATTENDANCE");
        boolean canViewDept = perms.contains("VIEW_DEPARTMENT_ATTENDANCE");
        if (!canViewAll && !canViewDept) {
            request.setAttribute("error", "Bạn không có quyền xem chấm công.");
            request.getRequestDispatcher("/public/manager/attendance/attendance_detail.jsp").forward(request, response);
            return;
        }

        int employeeId = paramOr(request, "employeeId", -1);
        java.time.LocalDate now = java.time.LocalDate.now();
        int month = paramOr(request, "month", now.getMonthValue());
        int year = paramOr(request, "year", now.getYear());

        // canViewAll: không giới hạn phòng ban; ngược lại chỉ phòng ban của Manager.
        Integer restrictDept = canViewAll ? null : resolveManagerDepartmentId(user);
        request.setAttribute("canViewAll", canViewAll);
        request.setAttribute("selectedDepartmentId", canViewAll ? optionalDepartmentParam(request) : restrictDept);

        dto.AttendanceDetailDTO detail = (employeeId > 0 && (canViewAll || restrictDept != null))
                ? attendanceService.getEmployeeDetail(employeeId, restrictDept, month, year)
                : null;
        if (detail == null) {
            request.setAttribute("error", "Không tìm thấy nhân viên hoặc bạn không có quyền xem.");
            request.getRequestDispatcher("/public/manager/attendance/attendance_detail.jsp").forward(request, response);
            return;
        }

        int day = paramOr(request, "day", 0);
        List<Attendance> filtered = detail.getDailyRows();
        if (day >= 1 && day <= 31) {
            filtered = new ArrayList<>();
            for (Attendance a : detail.getDailyRows()) {
                if (a.getWorkDate() != null && a.getWorkDate().toLocalDate().getDayOfMonth() == day) {
                    filtered.add(a);
                }
            }
        }
        request.setAttribute("selectedDay", day);
        request.setAttribute("pagedRows", utils.Paging.page(request, filtered));
        request.setAttribute("detail", detail);
        request.setAttribute("selectedMonth", month);
        request.setAttribute("selectedYear", year);
        request.getRequestDispatcher("/public/manager/attendance/attendance_detail.jsp").forward(request, response);
    }

    private void exportAttendanceReport(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        boolean canViewAll = perms.contains("VIEW_ALL_ATTENDANCE");
        boolean canViewDept = perms.contains("VIEW_DEPARTMENT_ATTENDANCE");
        if (!canViewAll && !canViewDept) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền xuất báo cáo chấm công.");
            return;
        }
        java.time.LocalDate now = java.time.LocalDate.now();
        int month = paramOr(request, "month", now.getMonthValue());
        int year = paramOr(request, "year", now.getYear());

        Integer departmentId;
        if (canViewAll) {
            departmentId = optionalDepartmentParam(request);
        } else {
            departmentId = resolveManagerDepartmentId(user);
            if (departmentId == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn chưa được phân công phòng ban.");
                return;
            }
        }

        dto.AttendanceReportDTO report = attendanceService.getReport(departmentId, month, year);
        String scopeName = (departmentId == null) ? "company" : "dept" + departmentId;
        String fileName = "attendance_" + scopeName + "_" + year + "_" + String.format("%02d", month) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        attendanceExporter.write(report, response.getOutputStream());
    }

    private void displayEmployeeList(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "VIEW_EMPLOYEES")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem danh sách nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        List<EmployeeDetailDTO> employees = employeeDAO.getAllEmployees();
        request.setAttribute("employees", employees);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/employee_info/employee_list.jsp").forward(request, response);
    }

    private void displayEmployeeDetail(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        EmployeeDetailDTO employee = getEmployeeFromRequest(request, response);
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee/list");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("employee", employee);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/employee_info/employee_detail.jsp").forward(request, response);
    }

    private void displayUpdateEmployeeForm(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "EDIT_EMPLOYEE")) {
            request.getSession().setAttribute("error", "Bạn không có quyền chỉnh sửa nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        EmployeeDetailDTO employee = getEmployeeFromRequest(request, response);
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee/list");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("employee", employee);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/employee_info/update_employee.jsp").forward(request, response);
    }

    private void displayAddContractForm(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "ADD_EMPLOYMENT_CONTRACT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm hợp đồng lao động.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("employees", employeeDAO.getAllEmployees());
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/contract/add_contract.jsp").forward(request, response);
    }

    private void displayContractPreview(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "ADD_EMPLOYMENT_CONTRACT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem hợp đồng lao động.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        EmploymentContract contract = getContractFromRequest(request);
        if (contract == null) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee/list");
            return;
        }

        EmployeeDetailDTO employee = employeeDAO.getEmployeeById(contract.getEmployeeId());
        if (employee == null) {
            request.getSession().setAttribute("error", "Không tìm thấy nhân viên của hợp đồng.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee/list");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("contract", contract);
        request.setAttribute("employee", employee);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/contract/contract_preview.jsp").forward(request, response);
    }

    private void displayEmployeeDepartmentDetail(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {

        if (!isHrStaff(user) || !hasPermission(user, "VIEW_DEPARTMENT_EMPLOYEES_DETAIL")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem nhân viên của phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        String rawDepartmentId = request.getParameter("id");
        if (isBlank(rawDepartmentId)) {
            request.getSession().setAttribute("error", "Thiếu mã phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department/list");
            return;
        }

        int departmentId;
        try {
            departmentId = Integer.parseInt(rawDepartmentId);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã phòng ban không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department/list");
            return;
        }

        Department department = departmentDAO.getDepartmentById(departmentId);
        if (department == null) {
            request.getSession().setAttribute("error", "Không tìm thấy phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department/list");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        List<EmployeeDetailDTO> employees = employeeDAO.getEmployeesByDepartmentId(departmentId);
        request.setAttribute("department", department);
        request.setAttribute("employees", employees);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/employee_info/department_employee_detail.jsp").forward(request, response);
    }

    private void displayAssignDepartmentForm(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {

        if (!isHrStaff(user) || !hasPermission(user, "ASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền phân cóng phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
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
        request.getRequestDispatcher("/public/manager/department/assign_department.jsp").forward(request, response);
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
        request.getRequestDispatcher("/public/manager/department/department_list.jsp").forward(request, response);
    }

    private void displayMyProfile(HttpServletRequest request, HttpServletResponse response,
            User sessionUser) throws ServletException, IOException {
        User currentUser = userDAO.getUserById(sessionUser.getUserId());
        request.setAttribute("currentUser", currentUser);
        EmployeeDetailDTO myEmployee = employeeDAO.getEmployeeByUserId(sessionUser.getUserId());
        request.setAttribute("myEmployee", myEmployee);
        request.getRequestDispatcher("/public/manager/employee_info/my_profile.jsp").forward(request, response);
    }

    private void handleAssignDepartment(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "ASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
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
            request.getRequestDispatcher("/public/manager/department/assign_department.jsp").forward(request, response);
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
            request.getRequestDispatcher("/public/manager/department/assign_department.jsp").forward(request, response);
            return;
        }

        if (employeeDAO.isUserAssignedToDepartment(userId)) {
            request.setAttribute("error", "Người dùng này đã được phân cóng phòng ban rồi.");
            request.setAttribute("availableEmployees", employeeDAO.getEmployees(user.getUserId()));
            request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
            request.setAttribute("positions", departmentDAO.getAllPositions());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/department/assign_department.jsp").forward(request, response);
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
            request.getRequestDispatcher("/public/manager/department/assign_department.jsp").forward(request, response);
            return;
        }

        boolean success = employeeDAO.assignEmployeeToDepartment(
                userId, departmentId, positionId,
                isBlank(phoneNumber) ? null : phoneNumber.trim(),
                isBlank(skills) ? null : skills.trim(),
                isBlank(experience) ? null : experience.trim(),
                isBlank(degree) ? null : degree.trim()
        );

        if (!success) {
            request.setAttribute("error", "Phân cóng thất bại. Vui lêng thử lại.");
            request.setAttribute("availableEmployees", employeeDAO.getEmployees(user.getUserId()));
            request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
            request.setAttribute("positions", departmentDAO.getAllPositions());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/department/assign_department.jsp").forward(request, response);
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
        response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
    }

    private void handleUnassignDepartment(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "REASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền gỡ phân cóng.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        String rawEmployeeId = request.getParameter("employeeId");
        if (isBlank(rawEmployeeId)) {
            request.getSession().setAttribute("error", "Thiếu mã nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee/list");
            return;
        }

        int employeeId;
        try {
            employeeId = Integer.parseInt(rawEmployeeId);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã nhân viên không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee/list");
            return;
        }

        EmployeeDetailDTO employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null || employee.getDepartmentId() <= 0) {
            request.getSession().setAttribute("error", "Nhân viên không hợp lệ hoặc chưa được phân cóng phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee/detail?id=" + employeeId);
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
        response.sendRedirect(request.getContextPath() + "/v1/manager/employee/detail?id=" + employeeId);
    }

    private void displayAddDepartmentForm(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {

        if (!isHrStaff(user) || !hasPermission(user, "EDIT_DEPARTMENTS")) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("roles", roleDAO.getAllActiveRoles());
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/department/add_department.jsp").forward(request, response);
    }

    private void handleAddDepartment(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!hasPermission(user, "EDIT_DEPARTMENTS")) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
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
            request.getRequestDispatcher("/public/manager/department/add_department.jsp").forward(request, response);
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
            request.getRequestDispatcher("/public/manager/department/add_department.jsp").forward(request, response);
            return;
        }

        // Gắn luật vai trò cho phòng ban. Để trống = phòng ban nhận mọi vai trò.
        if (!roleIds.isEmpty()) {
            departmentDAO.replaceDepartmentRoles(newDeptId, roleIds);
        }

        LOGGER.log(Level.INFO, "Department created: code={0} by userId={1}", new Object[]{code, user.getUserId()});
        request.getSession().setAttribute("success", "Thêm phòng ban \"" + name.trim() + "\" thành cóng.");
        response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
    }

    private void displayUpdateDepartmentForm(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {

        if (!isHrStaff(user) || !hasPermission(user, "EDIT_DEPARTMENTS")) {
            request.getSession().setAttribute("error", "Bạn không có quyền sửa phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        String idParam = request.getParameter("id");
        if (isBlank(idParam)) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/department/list");
            return;
        }
        int deptId;
        try {
            deptId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/department/list");
            return;
        }
        Department dept = departmentDAO.getDepartmentById(deptId);
        if (dept == null) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/department/list");
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
        request.getRequestDispatcher("/public/manager/department/update_department.jsp").forward(request, response);
    }

    private void handleUpdateDepartment(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {

        if (!isHrStaff(user) || !hasPermission(user, "EDIT_DEPARTMENTS")) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        String idParam = request.getParameter("departmentId");
        String name = request.getParameter("departmentName");
        String description = request.getParameter("description");
        List<Integer> roleIds = parseRoleIds(request.getParameterValues("roleIds"));

        if (isBlank(idParam) || isBlank(name)) {
            request.getSession().setAttribute("error", "Tên phòng ban là bắt buộc.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department/update?id=" + (idParam != null ? idParam : ""));
            return;
        }

        int deptId;
        try {
            deptId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/department/list");
            return;
        }

        Department dept = departmentDAO.getDepartmentById(deptId);
        if (dept == null) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/department/list");
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
            response.sendRedirect(request.getContextPath() + "/v1/manager/department/list");
        } else {
            request.getSession().setAttribute("error", "Cập nhật thất bại. Vui lêng thử lại.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department/update?id=" + deptId);
        }
    }

    private void handleUpdateEmployee(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "EDIT_EMPLOYEE")) {
            request.getSession().setAttribute("error", "Bạn không có quyền chỉnh sửa nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
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
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee/list");
            return;
        }

        if (!isValidEmployeeStatus(status)) {
            request.getSession().setAttribute("error", "Trạng thái nhân viên không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee/update?id=" + employeeId);
            return;
        }

        EmployeeDetailDTO current = employeeDAO.getEmployeeById(employeeId);
        if (current == null) {
            request.getSession().setAttribute("error", "Không tìm thấy nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee/list");
            return;
        }

        int departmentId = current.getDepartmentId();
        int positionId = current.getPositionId();
        int userRoleId = userDAO.getRoleIdByUserId(current.getUserId());
        if (!departmentDAO.isRoleAllowedForDepartment(departmentId, userRoleId)) {
            request.getSession().setAttribute("error", "Vai trò hiện tại của nhân viên không phù hợp với phòng ban đã chọn.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee/update?id=" + employeeId);
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
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee/detail?id=" + employeeId);
        } else {
            request.getSession().setAttribute("error", "Cập nhật nhân viên thất bại. Vui lêng thử lại.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee/update?id=" + employeeId);
        }
    }

    private void handleAddContract(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "ADD_EMPLOYMENT_CONTRACT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm hợp đồng lao động.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        String code = trimToNull(request.getParameter("contractCode"));
        String type = trimToNull(request.getParameter("contractType"));
        String employeeParam = request.getParameter("employeeId");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String salaryParam = request.getParameter("salary");

        if (code == null || type == null || isBlank(employeeParam) || isBlank(startDate) || isBlank(salaryParam)) {
            request.setAttribute("error", "Vui lêng nhập đầy đủ mã hợp đồng, nhân viên, loại hợp đồng, ngày bắt đầu và lương.");
            request.setAttribute("employees", employeeDAO.getAllEmployees());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/contract/add_contract.jsp").forward(request, response);
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
            request.getRequestDispatcher("/public/manager/contract/add_contract.jsp").forward(request, response);
            return;
        }

        if (!isValidContractType(type) || contract.getSalary().compareTo(BigDecimal.ZERO) < 0) {
            request.setAttribute("error", "Loại hợp đồng hoặc lương không hợp lệ.");
            request.setAttribute("employees", employeeDAO.getAllEmployees());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/contract/add_contract.jsp").forward(request, response);
            return;
        }

        if (employeeDAO.getEmployeeById(contract.getEmployeeId()) == null) {
            request.setAttribute("error", "Nhân viên được chọn không tồn tại.");
            request.setAttribute("employees", employeeDAO.getAllEmployees());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/contract/add_contract.jsp").forward(request, response);
            return;
        }

        if (contractDAO.hasActiveContract(contract.getEmployeeId())) {
            request.setAttribute("error", "Hợp đồng của nhân viên vẫn cón hiệu lực");
            request.setAttribute("employees", employeeDAO.getAllEmployees());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/contract/add_contract.jsp").forward(request, response);
            return;
        }

        if (contract.getEndDate() != null && contract.getEndDate().before(contract.getStartDate())) {
            request.setAttribute("error", "Ngày kết thúc không được trước ngày bắt đầu.");
            request.setAttribute("employees", employeeDAO.getAllEmployees());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/contract/add_contract.jsp").forward(request, response);
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
            response.sendRedirect(request.getContextPath() + "/v1/manager/contract/preview?employeeId=" + contract.getEmployeeId());
        } else {
            request.setAttribute("error", "Thêm hợp đồng thất bại. Mã hợp đồng có thể đã tồn tại.");
            request.setAttribute("employees", employeeDAO.getAllEmployees());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/contract/add_contract.jsp").forward(request, response);
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

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String sanitizeFileName(String name) {
        if (name == null) {
            return "upload.xlsx";
        }
        String base = Paths.get(name).getFileName().toString();
        return base.replaceAll("[\\r\\n]", "");
    }

    private void displayImportForm(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "IMPORT_ATTENDANCE")) {
            request.getSession().setAttribute("error", "Bạn không có quyền import chấm công.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);
        request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
        request.getRequestDispatcher("/public/manager/attendance/attendance_import.jsp").forward(request, response);
    }

    private void handleImportAttendance(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "IMPORT_ATTENDANCE")) {
            request.getSession().setAttribute("error", "Bạn không có quyền import file chấm công.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);

        int month;
        int year;
        int departmentId = 0;
        try {
            month = Integer.parseInt(request.getParameter("month").trim());
            year = Integer.parseInt(request.getParameter("year").trim());
            String rawDept = request.getParameter("departmentId");
            if (rawDept != null && !rawDept.trim().isEmpty()) {
                departmentId = Integer.parseInt(rawDept.trim());
            }
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Dữ liệu tháng, năm hoặc phòng ban không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/attendance/import");
            return;
        }

        if (month < 1 || month > 12) {
            forwardImportError(request, response, "Vui lòng chọn tháng hợp lệ (1-12).", month, year, departmentId);
            return;
        }
        if (year < 2000 || year > 2100) {
            forwardImportError(request, response, "Vui lòng chọn năm hợp lệ.", month, year, departmentId);
            return;
        }

        Part filePart = request.getPart(ATTENDANCE_FILE_PART);
        if (filePart == null || filePart.getSize() == 0) {
            forwardImportError(request, response, "Vui lòng chọn file Excel .xlsx để import.", month, year, departmentId);
            return;
        }

        String submittedName = filePart.getSubmittedFileName();
        if (submittedName == null || !submittedName.toLowerCase().endsWith(".xlsx")) {
            forwardImportError(request, response, "File phải có định dạng .xlsx.", month, year, departmentId);
            return;
        }

        String contentType = filePart.getContentType();
        if (contentType != null && !isAcceptableXlsxContentType(contentType)) {
            forwardImportError(request, response, "Loại file không hợp lệ. Yêu cầu file Excel .xlsx.", month, year, departmentId);
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
            forwardImportError(request, response, "Không thể lưu file lên máy chủ. Vui lòng thử lại.", month, year, departmentId);
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
            forwardImportError(request, response, "Không thể tạo bản ghi file. Vui lòng thử lại.", month, year, departmentId);
            return;
        }

        AttendanceImportResultDTO result;
        try (InputStream is = Files.newInputStream(savedPath)) {
            result = importService.importAttendance(is, departmentId, month, year, fileId);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot read saved attendance file", e);
            uploadedFileDAO.updateImportResult(fileId, 0, 0, 0,
                    FileStatus.FILE_STATUS_FAILED.getRelatedNum(), "Không thể đọc lại file đã lưu.");
            forwardImportError(request, response, "Không thể đọc file đã lưu để import.", month, year, departmentId);
            return;
        }

        result.setFileName(uf.getFileName());
        request.setAttribute("auditLogged", Boolean.TRUE);
        request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
        request.setAttribute("importResult", result);
        request.setAttribute("selectedMonth", month);
        request.setAttribute("selectedYear", year);
        request.setAttribute("selectedDepartmentId", departmentId);
        request.getRequestDispatcher("/public/manager/attendance/attendance_import.jsp").forward(request, response);
    }

    private void forwardImportError(HttpServletRequest request, HttpServletResponse response,
            String message, int month, int year, int departmentId) throws ServletException, IOException {
        request.setAttribute("error", message);
        request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
        request.setAttribute("selectedMonth", month);
        request.setAttribute("selectedYear", year);
        request.setAttribute("selectedDepartmentId", departmentId);
        request.getRequestDispatcher("/public/manager/attendance/attendance_import.jsp").forward(request, response);
    }

    private boolean isAcceptableXlsxContentType(String contentType) {
        String ct = contentType.toLowerCase();
        return ct.contains("openxmlformats-officedocument.spreadsheetml.sheet")
                || ct.contains("application/octet-stream")
                || ct.contains("application/zip");
    }

    private void displayAllForms(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        if (!hasPermission(user, "VIEW_ALL_FORMS")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem tất cả đơn");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
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
        request.getRequestDispatcher("/public/manager/forms/all_form_list.jsp").forward(request, response);
    }

    private void displayFormDetail(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String formIdRaw = request.getParameter("id");
        if (isBlank(formIdRaw)) {
            request.getSession().setAttribute("error", "Thiếu mã đơn.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/forms/dept-forms");
            return;
        }
        try {
            int formId = Integer.parseInt(formIdRaw);
            dto.FormRequestDTO form = formRequestDAO.getFormRequestById(formId);
            if (form == null) {
                request.getSession().setAttribute("error", "Không tìm thấy đơn yêu cầu.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/forms/dept-forms");
                return;
            }

            EmployeeDetailDTO employee = employeeDAO.getEmployeeByUserId(user.getUserId());
            boolean canApprove = employee != null && employee.getDepartmentId() > 0
                    && form.getDepartmentId() == employee.getDepartmentId();

            request.setAttribute("form", form);
            request.setAttribute("canApprove", canApprove);
            request.getRequestDispatcher("/public/manager/forms/form_detail.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã đơn không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/forms/dept-forms");
        }
    }

    private void displayDeptForms(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        EmployeeDetailDTO me = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (me == null || me.getDepartmentId() <= 0) {
            request.getSession().setAttribute("error", "Bạn chưa được phân cóng vào phòng ban nào.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        Integer day = parseIntOrNull(request.getParameter("day"));
        Integer month = parseIntOrNull(request.getParameter("month"));
        Integer year = parseIntOrNull(request.getParameter("year"));
        String name = request.getParameter("empName");
        request.setAttribute("forms", formRequestDAO.getAllFormRequestsByDepartmentId(me.getDepartmentId(), day, month, year, name));
        request.setAttribute("filterDay", day);
        request.setAttribute("filterMonth", month);
        request.setAttribute("filterYear", year);
        request.setAttribute("filterName", name);
        request.getRequestDispatcher("/public/manager/forms/dept_form_list.jsp").forward(request, response);
    }

    private void handleApproveForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        EmployeeDetailDTO me = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (me == null) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        String rawId = request.getParameter("formId");
        String note = request.getParameter("note");
        if (isBlank(rawId)) {
            request.getSession().setAttribute("error", "Thiếu mã đơn.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/forms/dept-forms");
            return;
        }
        try {
            int formId = Integer.parseInt(rawId);
            if (!isDepartmentManager(me, formId)) {
                request.getSession().setAttribute("error", "Bạn không có quyền xử lý đơn này.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/forms/dept-forms");
                return;
            }
            boolean ok = formRequestDAO.approveFormRequest(formId, me.getEmployeeId(), note);
            if (ok) {
                FormRequestDTO form = formRequestDAO.getFormRequestById(formId);
                if (form != null) {
                    if (form instanceof LeaveFormRequestDTO) {
                        LeaveFormRequestDTO leaveForm = (LeaveFormRequestDTO) form;
                        int year = (leaveForm.getStartDate() != null) ? leaveForm.getStartDate().toLocalDate().getYear() : java.time.LocalDate.now().getYear();
                        LeaveBalance lb = leaveBalanceDAO.getLeaveBalance(form.getEmployeeId(), year);
                        if (lb != null && leaveForm.getTotalDays() != null) {
                            leaveBalanceDAO.updateUsedDays(form.getEmployeeId(), year, leaveForm.getTotalDays());
                        }
                    } else if (form instanceof ComplaintFormRequestDTO) {
                        ComplaintFormRequestDTO compForm = (ComplaintFormRequestDTO) form;
                        if (compForm.getStartDate() != null && compForm.getStartTime() != null && compForm.getEndTime() != null) {
                            Attendance att = attendanceDAO.getAttendanceByDate(form.getEmployeeId(), compForm.getStartDate());
                            if (att != null) {
                                attendanceDAO.updateAttendanceWithHistory(att.getAttendanceId(), compForm.getStartTime(), compForm.getEndTime(), null, 0, "Updated by complaint approval", me.getUserId());
                            } else {
                                EmployeeDetailDTO empForAtt = employeeDAO.getEmployeeById(form.getEmployeeId());
                                Attendance newAtt = new Attendance();
                                newAtt.setAttendanceCode("ATT-" + form.getEmployeeId() + "-" + System.currentTimeMillis());
                                newAtt.setEmployeeId(form.getEmployeeId());
                                if (empForAtt != null) {
                                    newAtt.setEmployeeCode(empForAtt.getEmployeeCode());
                                    newAtt.setFullName(empForAtt.getFullName());
                                    newAtt.setDepartmentId(empForAtt.getDepartmentId());
                                    newAtt.setDepartmentName(empForAtt.getDepartmentName());
                                }
                                newAtt.setWorkDate(compForm.getStartDate());
                                newAtt.setTimeIn(compForm.getStartTime());
                                newAtt.setTimeOut(compForm.getEndTime());
                                newAtt.setAttendanceStatus(0); // Đúng giờ
                                try {
                                    try (java.sql.Connection conn = new DBContext().getConnection()) {
                                        attendanceDAO.upsertAttendance(conn, newAtt);
                                    }
                                } catch (java.sql.SQLException ex) {
                                    LOGGER.log(Level.SEVERE, "Lỗi khi chèn điểm danh mới từ đơn khiếu nại", ex);
                                }
                            }
                        }
                    }
                }
            }
            request.getSession().setAttribute(ok ? "success" : "error",
                    ok ? "Duyệt đơn thành cóng." : "Duyệt đơn thất bại.");
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã đơn không hợp lệ.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/manager/forms/dept-forms");
    }

    private void handleRejectForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        EmployeeDetailDTO me = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (me == null) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        String rawId = request.getParameter("formId");
        String note = request.getParameter("note");
        if (isBlank(rawId)) {
            request.getSession().setAttribute("error", "Thiếu mã đơn.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/forms/dept-forms");
            return;
        }
        try {
            int formId = Integer.parseInt(rawId);
            if (!isDepartmentManager(me, formId)) {
                request.getSession().setAttribute("error", "Bạn không có quyền xử lý đơn này.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/forms/dept-forms");
                return;
            }
            boolean ok = formRequestDAO.rejectFormRequest(formId, me.getEmployeeId(), note);
            request.getSession().setAttribute(ok ? "success" : "error",
                    ok ? "Từ chối đơn thành cóng." : "Từ chối đơn thất bại.");
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã đơn không hợp lệ.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/manager/forms/dept-forms");
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
            response.sendRedirect(request.getContextPath() + "/v1/manager/my-profile");
            return;
        }

        boolean success = employeeDAO.updateOwnProfile(
                myEmployee.getEmployeeId(),
                isBlank(phoneNumber) ? null : phoneNumber.trim(),
                isBlank(skills) ? null : skills.trim(),
                isBlank(experience) ? null : experience.trim(),
                isBlank(degree) ? null : degree.trim()
        );

        if (success) {
            request.getSession().setAttribute("success", "Cập nhật hồ sơ thành cóng.");
        } else {
            request.getSession().setAttribute("error", "Cập nhật thất bại. Vui lêng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/manager/my-profile");
    }

    private void handleUpdateEmployeeDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String rawEmployeeId = request.getParameter("employeeId");
        String rawStatus = request.getParameter("status");
        String phoneNumber = request.getParameter("phoneNumber");
        String degree = request.getParameter("degree");
        String experience = request.getParameter("experience");
        String skills = request.getParameter("skills");

        if (isBlank(rawEmployeeId) || isBlank(rawStatus)) {
            request.getSession().setAttribute("error", "Dữ liệu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department/list");
            return;
        }

        int employeeId;
        int status;
        try {
            employeeId = Integer.parseInt(rawEmployeeId);
            status = Integer.parseInt(rawStatus);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Dữ liệu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department/list");
            return;
        }

        EmployeeDetailDTO employeeDetail = employeeDAO.getEmployeeById(employeeId);
        if (employeeDetail == null) {
            request.getSession().setAttribute("error", "Không tìm thấy nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department/list");
            return;
        }

        boolean statusSuccess = employeeDAO.updateEmployeeStatus(employeeId, status);
        boolean profileSuccess = employeeDAO.updateOwnProfile(
                employeeId,
                isBlank(phoneNumber) ? null : phoneNumber.trim(),
                isBlank(skills) ? null : skills.trim(),
                isBlank(experience) ? null : experience.trim(),
                isBlank(degree) ? null : degree.trim()
        );

        if (statusSuccess || profileSuccess) {
            request.getSession().setAttribute("success", "Cập nhật nhân viên thành cóng.");
        } else {
            request.getSession().setAttribute("error", "Cập nhật thất bại hoặc không có thay đổi.");
        }

        if (employeeDetail.getDepartmentId() > 0) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/department/employee-detail?id=" + employeeDetail.getDepartmentId());
        } else {
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee/detail?id=" + employeeId);
        }
    }

    private void displayCreateOTForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);

        EmployeeDetailDTO manager = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (manager == null || manager.getDepartmentId() <= 0) {
            request.getSession().setAttribute("error", "Bạn chưa được phân công vào phòng ban nào nên không thể tạo đơn OT.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        List<EmployeeDetailDTO> departmentEmployees = employeeDAO.getEmployeesFiltered(null, manager.getDepartmentId(), "", null, "", 0, 1000);

        request.setAttribute("department", departmentDAO.getDepartmentById(manager.getDepartmentId()));
        request.setAttribute("departmentEmployees", departmentEmployees);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/forms/ot_create.jsp").forward(request, response);
    }

    private void displayOTRequests(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);

        EmployeeDetailDTO manager = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (manager != null) {
            String statusFilter = request.getParameter("status");
            String dateFilter = request.getParameter("otDate");

            List<OvertimeRequestDTO> requests = overtimeDAO.getOvertimeRequestsByManager(manager.getEmployeeId(), statusFilter, dateFilter);
            request.setAttribute("otRequests", requests);
            request.setAttribute("statusFilter", statusFilter);
            request.setAttribute("dateFilter", dateFilter);
        }

        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/forms/ot_requests.jsp").forward(request, response);
    }

    private void displayOTRequestDetail(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/forms/ot-requests");
            return;
        }

        try {
            int formId = Integer.parseInt(idParam);
            OvertimeRequestDTO otRequest = overtimeDAO.getOvertimeRequestById(formId);

            if (otRequest == null) {
                request.getSession().setAttribute("error", "Không tìm thấy đơn OT.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/forms/ot-requests");
                return;
            }

            EmployeeDetailDTO manager = employeeDAO.getEmployeeByUserId(user.getUserId());
            if (manager == null || (otRequest.getEmployeeId() != manager.getEmployeeId() && !hasPermission(user, "VIEW_ALL_DEPT_FORMS") && !hasPermission(user, "VIEW_ALL_FORMS"))) {
                request.getSession().setAttribute("error", "Bạn không có quyền xem đơn OT này.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/forms/ot-requests");
                return;
            }

            List<EmployeeDetailDTO> assignees = overtimeDAO.getOvertimeAssignees(formId);
            request.setAttribute("otRequest", otRequest);
            request.setAttribute("assignees", assignees);

            setPermissionFlags(request, perms);
            request.getRequestDispatcher("/public/manager/forms/ot_detail.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/forms/ot-requests");
        }
    }

    private void handleCancelOT(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        try {
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/v1/auth/login");
                return;
            }

            String formIdParam = request.getParameter("formId");
            if (formIdParam == null || formIdParam.trim().isEmpty()) {
                request.getSession().setAttribute("error", "Dữ liệu formId không hợp lệ.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/forms/ot-requests");
                return;
            }

            int formId = Integer.parseInt(formIdParam.trim());
            EmployeeDetailDTO manager = employeeDAO.getEmployeeByUserId(user.getUserId());

            if (manager != null) {
                dto.FormRequestDTO fr = formRequestDAO.getFormRequestById(formId);
                if (fr != null && fr.getEmployeeId() == manager.getEmployeeId() && fr.getStatus() == 0) {
                    boolean success = formRequestDAO.updateFormRequest(formId, 3, manager.getEmployeeId(), "Đã hủy bởi người tạo");
                    if (success) {
                        request.getSession().setAttribute("success", "Đã hủy đơn OT thành cóng.");
                    } else {
                        request.getSession().setAttribute("error", "Lỗi khi cập nhật trạng thái hủy đơn. Vui lòng thử lại.");
                    }
                } else {
                    request.getSession().setAttribute("error", "Đơn không tồn tại, không thuộc về bạn, hoặc đã được xử lý.");
                }
            } else {
                request.getSession().setAttribute("error", "Không tìm thấy thông tin quản lý.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi hủy đơn OT", e);
            request.getSession().setAttribute("error", "Đã xảy ra lỗi hệ thống: " + e.getClass().getName() + " - " + e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/v1/manager/forms/ot-requests");
    }

    private void handleCreateOT(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        try {
            EmployeeDetailDTO manager = employeeDAO.getEmployeeByUserId(user.getUserId());
            if (manager == null || manager.getDepartmentId() <= 0) {
                request.getSession().setAttribute("error", "Bạn chưa được phân cóng vào phòng ban nào.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
                return;
            }

            String otDate = request.getParameter("otDate");
            String startTime = request.getParameter("startTime");
            String endTime = request.getParameter("endTime");
            String dayTypeStr = request.getParameter("dayType");
            String reason = request.getParameter("reason");
            String[] assigneeIds = request.getParameterValues("assignees");

            if (otDate == null || otDate.isEmpty() || startTime == null || startTime.isEmpty()
                    || endTime == null || endTime.isEmpty() || dayTypeStr == null || assigneeIds == null || assigneeIds.length == 0) {
                request.getSession().setAttribute("error", "Vui lêng điền đầy đủ thông tin và chọn ít nhất 1 nhân viên.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/forms/create-ot");
                return;
            }

            try {
                java.time.LocalTime start = java.time.LocalTime.parse(startTime);
                java.time.LocalTime end = java.time.LocalTime.parse(endTime);

                if (!start.isBefore(end)) {
                    request.getSession().setAttribute("error", "Thời gian kết thúc phải lớn hơn thời gian bắt đầu.");
                    response.sendRedirect(request.getContextPath() + "/v1/manager/forms/create-ot");
                    return;
                }

                java.time.LocalDate date = java.time.LocalDate.parse(otDate);
                java.time.DayOfWeek dow = date.getDayOfWeek();
                boolean isWeekend = (dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY);

                if (!isWeekend) {
                    java.time.LocalTime minTime = java.time.LocalTime.of(17, 0);
                    java.time.LocalTime maxTime = java.time.LocalTime.of(19, 0);
                    if (start.isBefore(minTime) || end.isAfter(maxTime)) {
                        request.getSession().setAttribute("error", "Đối với ngày thường (Thứ 2 - Thứ 6), nhân viên chỉ được phép OT trong khung giờ 17:00 đến 19:00.");
                        response.sendRedirect(request.getContextPath() + "/v1/manager/forms/create-ot");
                        return;
                    }
                }
            } catch (Exception e) {
                request.getSession().setAttribute("error", "Định dạng ngày/giờ không hợp lệ.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/forms/create-ot");
                return;
            }

            int dayType = Integer.parseInt(dayTypeStr);

            // Tìm formTypeId của OVERTIME
            int formTypeId = -1;
            List<model.FormType> types = formTypeDAO.getAll();
            for (model.FormType type : types) {
                if ("OVERTIME".equalsIgnoreCase(type.getFormTypeCode())) {
                    formTypeId = type.getFormTypeId();
                    break;
                }
            }
            if (formTypeId == -1) {
                request.getSession().setAttribute("error", "Loại đơn OVERTIME chưa được cấu hình trong hệ thống.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/forms/create-ot");
                return;
            }

            // Tạo mã đơn ngẫu nhiên
            String formCode = "OT-" + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());

            model.FormRequest fr = new model.FormRequest();
            fr.setFormCode(formCode);
            fr.setEmployeeId(manager.getEmployeeId());
            fr.setFormTypeId(formTypeId);
            fr.setReason(reason);
            fr.setStatus(0); // Pending

            int newFormId = formRequestDAO.addFormRequest(fr);
            if (newFormId > 0) {
                boolean detailAdded = overtimeDAO.addOvertimeDetails(newFormId, otDate, startTime, endTime, dayType);
                boolean assigneesAdded = overtimeDAO.addOvertimeAssignees(newFormId, assigneeIds);
                if (detailAdded && assigneesAdded) {
                    request.getSession().setAttribute("success", "Đã tạo đơn Overtime thành cóng (Mã đơn: " + formCode + ") và gửi chờ duyệt.");
                    response.sendRedirect(request.getContextPath() + "/v1/manager/forms/ot-requests");
                    return;
                }
            }

            request.getSession().setAttribute("error", "Đã xảy ra lỗi trong quá trònh tạo đơn OT.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/forms/create-ot");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tạo đơn OT", e);
            request.getSession().setAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/v1/manager/forms/create-ot");
        }
    }

    private void displayRecruitmentList(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem tuyển dụng.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
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
        Integer requestedPage = parseIntOrNull(request.getParameter("page"));
        int currentPage = requestedPage == null ? 1 : Math.max(1, requestedPage);
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
        request.getRequestDispatcher("/public/manager/recruitment/recruitment_list.jsp").forward(request, response);
    }

    private void displayRecruitmentDetail(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem tuyển dụng.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        Integer candidateId = parseIntOrNull(request.getParameter("id"));
        if (candidateId == null) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/recruitment-list");
            return;
        }
        Candidate candidate = candidateDAO.getById(candidateId);
        if (candidate == null) {
            request.getSession().setAttribute("error", "Không tìm thấy ứng viên.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/recruitment-list");
            return;
        }
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("candidate", candidate);
        request.setAttribute("latestLog", candidateDAO.getLatestLog(candidateId));
        request.setAttribute("logs", candidateDAO.getLogsByCandidateId(candidateId));
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/recruitment/recruitment_detail.jsp").forward(request, response);
    }

    private void handleRecruitmentReview(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "PROCESS_RECRUITMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xử lý tuyển dụng.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
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
            response.sendRedirect(request.getContextPath() + "/v1/manager/recruitment-list");
            return;
        }

        Candidate candidate = candidateDAO.getById(candidateId);
        EmployeeDetailDTO reviewer = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (candidate == null || reviewer == null) {
            request.getSession().setAttribute("error", "Không tìm thấy dữ liệu ứng viên hoặc người xử lý.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/recruitment-list");
            return;
        }

        String fromStage = candidate.getStage();
        String toStage = "PASSED".equals(result)
                ? ("APPLIED".equals(fromStage) ? "INTERVIEW" : "PROBATION")
                : "REJECTED";

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
            response.sendRedirect(request.getContextPath() + "/v1/manager/recruitment-detail?id=" + candidateId);
            return;
        }

        boolean sent = "PASSED".equals(result)
                ? emailService.sendAcceptedCandidateNotify(toEmail, emailSubject, emailBody)
                : emailService.sendRejectCandidateNotify(toEmail, emailSubject, emailBody);
        candidateDAO.updateEmailStatus(logId, sent ? "SENT" : "FAILED");
        if (!sent) {
            request.getSession().setAttribute("error", "Gui email that bai, trang thai ung vien chua duoc cap nhat.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/recruitment-detail?id=" + candidateId);
            return;
        }

        candidateDAO.updateStage(candidateId, toStage);
        request.getSession().setAttribute("success", "Đã xử lý ứng viên và gửi email thành công.");
        response.sendRedirect(request.getContextPath() + "/v1/manager/recruitment-list?stage=" + toStage);
    }

    private void displayRecruitmentImport(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "PROCESS_RECRUITMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền import ứng viên.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/recruitment/recruitment_import.jsp").forward(request, response);
    }

    private void handleImportCandidates(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "PROCESS_RECRUITMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền import ứng viên.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        Part filePart = request.getPart("file");
        if (filePart == null || filePart.getSize() == 0) {
            request.getSession().setAttribute("error", "Vui long chon file Excel.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/recruitment-import");
            return;
        }
        String submittedName = filePart.getSubmittedFileName();
        if (submittedName == null || !submittedName.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            request.getSession().setAttribute("error", "File phai co dinh dang .xlsx.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/recruitment-import");
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
            response.sendRedirect(request.getContextPath() + "/v1/manager/recruitment-import");
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
            response.sendRedirect(request.getContextPath() + "/v1/manager/recruitment-import");
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
        request.getRequestDispatcher("/public/manager/recruitment/recruitment_import.jsp").forward(request, response);
    }

    private boolean isDepartmentManager(EmployeeDetailDTO me, int formId) {
        if (me == null || me.getDepartmentId() <= 0) {
            return false;
        }
        dto.FormRequestDTO form = formRequestDAO.getFormRequestById(formId);
        return form != null && form.getStatus() == 0
                && form.getDepartmentId() == me.getDepartmentId();
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
        java.time.LocalDate now = java.time.LocalDate.now();
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

    private void setPermissionFlags(HttpServletRequest request, Set<String> perms) {
        request.setAttribute("canViewEmployees", perms.contains("VIEW_EMPLOYEES"));
        request.setAttribute("canAddEmployee", perms.contains("ADD_EMPLOYEE"));
        request.setAttribute("canAddEmploymentContract", perms.contains("ADD_EMPLOYMENT_CONTRACT"));
        request.setAttribute("canEditEmployee", perms.contains("EDIT_EMPLOYEE"));
        request.setAttribute("canDeleteEmployee", perms.contains("DELETE_EMPLOYEE"));
        request.setAttribute("canViewDepartments", perms.contains("VIEW_DEPARTMENTS"));
        request.setAttribute("canEditDepts", perms.contains("EDIT_DEPARTMENTS"));
        request.setAttribute("canAssignDept", perms.contains("ASSIGN_DEPARTMENT"));
        request.setAttribute("canReassignDept", perms.contains("REASSIGN_DEPARTMENT"));
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

    private void reloadReassignFormWithError(HttpServletRequest request, HttpServletResponse response,
            User user, String message) throws ServletException, IOException {
        request.setAttribute("error", message);
        request.setAttribute("assignedEmployees", employeeDAO.getAssignedEmployees(user.getUserId()));
        request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
        request.setAttribute("positions", departmentDAO.getAllPositions());
        setPermissionFlags(request, getPermissions(user));
        request.getRequestDispatcher("/public/manager/department/reassign_department.jsp").forward(request, response);
    }
}
