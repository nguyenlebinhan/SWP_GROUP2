package controller;

import com.lowagie.text.DocumentException;
import config.ContractSchedulerInitializerListener;
import dao.*;
import dto.AttendanceImportResultDTO;
import dto.CandidateImportResultDTO;
import dto.ContractImportDTO;
import dto.EmployeeDTO;
import dto.EmployeeDetailDTO;
import dto.PayrollPreviewDTO;
import dto.FormRequestDTO;
import dto.LeaveFormRequestDTO;
import dto.ComplaintFormRequestDTO;
import dto.TransferRequestDTO;
import model.TransferFormRequest;
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
import model.ContractOperationResult;
import enums.ContractStatus;
import enums.ContractType;
import model.Department;
import model.Employee;
import model.EmploymentContract;
import model.FormRequest;
import model.FormType;
import model.LeaveBalance;
import model.Position;
import model.PayrollAllowanceType;
import model.PayrollConfigChangeRequest;
import model.PayrollDeductionRule;
import model.PayrollSetting;
import model.PayrollTaxBracket;
import model.Role;
import model.UploadedFile;
import model.User;
import model.ContractAmendment;
import service.CandidateImportService;
import service.EmailService;
import service.AttendanceImportService;
import service.AttendanceClosingService;
import service.PayrollService;
import service.PayrollConfigWorkflowService;
import service.EmploymentContractService;
import service.ContractPdfService;
import service.PdfParsingService;
import dal.DBContext;
import java.time.LocalTime;
import java.sql.Time;
import java.sql.Date;
import utils.AttendanceExcelExporter;
import dao.ContractAmendmentDAO;
import dto.ClosingResult;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import java.time.LocalDate;

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
    private static final DependentDAO dependentDAO = new DependentDAO();
    private static final PermissionDAO permissionDAO = new PermissionDAO();
    private static final EmploymentContractDAO contractDAO = new EmploymentContractDAO();
    private static final ContractAmendmentDAO amendmentDAO = new ContractAmendmentDAO();
    private static final service.ContractAmendmentService contractAmendmentService = new service.ContractAmendmentService(amendmentDAO, contractDAO, new dal.DBContext());
    private static final FormRequestDAO formRequestDAO = new FormRequestDAO();
    private static final EmploymentContractService contractService = new EmploymentContractService(contractDAO, employeeDAO, new dal.DBContext());
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
    private static final AttendanceClosingService attendanceClosingService = new AttendanceClosingService();
    private static final PayrollConfigDAO payrollConfigDAO = new PayrollConfigDAO();
    private static final PayrollConfigWorkflowService payrollConfigWorkflowService = new PayrollConfigWorkflowService();
    private static final String UPLOAD_DIR = "uploads";
    private static final String ATTENDANCE_FILE_PART = "attendanceFile";
    
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, type, context) -> new JsonPrimitive(src.toString()))
            .create();

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
            case "/contract/pending":
                displayPendingContracts(request, response, user);
                break;
            case "/contract/history":
                displayContractHistory(request, response, user);
                break;
            case "/contract/detail":
                displayContractDetail(request, response, user);
                break;
            case "/contract/terminate":
                displayTerminateContractList(request, response, user);
                break;
            case "/contract/amendments":
                displayContractAmendments(request, response, user);
                break;
            case "/contract/blank-template":
                downloadBlankTemplate(request, response);
                break;
            case "/contract/download-signed":
                downloadSignedContract(request, response);
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
            case "/payroll-config":
                displayPayrollConfig(request, response, user);
                break;
            case "/payroll-config/history":
                displayPayrollConfigHistory(request, response, user);
                break;
            case "/forms/ot-requests":
                displayOTRequests(request, response, user);
                break;
            case "/forms/create-ot":
                displayCreateOTForm(request, response, user);
                break;
            case "/forms/dependent/new":
                displayDependentForm(request, response, user);
                break;
            case "/forms/ot-detail":
                displayOTRequestDetail(request, response, user);
                break;
            case "/forms/submit-promotion":
                displayRequestPromotionForm(request, response, user);
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
            case "/contract/approve":
                handleApproveContract(request, response, user);
                break;
            case "/contract/reject":
                handleRejectContract(request, response, user);
                break;
            case "/contract/cancel":
                handleCancelContract(request, response, user);
                break;
            case "/contract/terminate":
                handleTerminateContract(request, response, user);
                break;
            case "/contract/parse-pdf":
                handleParsePdf(request, response);
                break;
            case "/scheduler/run-now":
                handleRunSchedulerNow(request, response, user);
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
            case "/forms/hr-approve":
                handleHrApproveForm(request, response, user);
                break;
            case "/forms/reject":
                handleRejectForm(request, response, user);
                break;
            case "/forms/hr-reject":
                handleHrRejectForm(request, response, user);
                break;
            case "/forms/create-ot":
                handleCreateOT(request, response, user);
                break;
            case "/forms/cancel-ot":
                handleCancelOT(request, response, user);
                break;
            case "/forms/dependent/submit":
                handleDependentFormSubmit(request, response, user);
                break;
            case "/forms/submit-promotion":
                handleRequestPromotionDemotion(request, response, user);
                break;
            case "/forms/dependent/status":
                handleDependentStatusRequest(request, response, user);
                break;
            case "/attendance/import":
                handleImportAttendance(request, response, user);
                break;
            case "/attendance/confirm":
                handleConfirmDeptAttendance(request, response, user);
                break;
            case "/salary/generate":
                handleGeneratePayroll(request, response, user);
                break;
            case "/salary/approve-all":
                handleApproveAllPayroll(request, response, user);
                break;
            case "/payroll-config/setting/save":
                handleRequestPayrollSetting(request, response, user);
                break;
            case "/payroll-config/deduction/save":
                handleRequestPayrollDeduction(request, response, user);
                break;
            case "/payroll-config/deduction/delete":
                handleRequestPayrollDeductionDelete(request, response, user);
                break;
            case "/payroll-config/tax/save":
                handleRequestPayrollTaxBracket(request, response, user);
                break;
            case "/payroll-config/allowance/save":
                handleRequestPayrollAllowance(request, response, user);
                break;
            case "/payroll-config/allowance/delete":
                handleRequestPayrollAllowanceDelete(request, response, user);
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

        // --- NEW DASHBOARD LOGIC (Appended without deleting base code) ---
        EmployeeDetailDTO manager = employeeDAO.getEmployeeByUserId(user.getUserId());
        request.setAttribute("myEmployee", manager);

        int deptTotalEmployees = totalEmployees;
        int pendingForms = 0;
        List<EmployeeDetailDTO> departmentEmployees = new ArrayList<>();
        Map<Integer, Integer> leaveBalances = new HashMap<>();
        List<dto.AttendanceSummaryDTO> topEmployees = new ArrayList<>();
        int prevMonth = java.time.LocalDate.now().minusMonths(1).getMonthValue();
        int prevYear = java.time.LocalDate.now().minusMonths(1).getYear();

        if (manager != null && manager.getDepartmentId() > 0) {
            departmentEmployees = employeeDAO.getEmployeesByDepartmentId(manager.getDepartmentId());
            deptTotalEmployees = departmentEmployees.size();
            int currentYear = java.time.LocalDate.now().getYear();

            dao.LeaveBalanceDAO lbDAO = new dao.LeaveBalanceDAO();
            for (EmployeeDetailDTO emp : departmentEmployees) {
                model.LeaveBalance lb = lbDAO.getLeaveBalance(emp.getEmployeeId(), currentYear);
                if (lb != null) {
                    leaveBalances.put(emp.getEmployeeId(), lb.getTotalAllowed() - lb.getUsedDays());
                } else {
                    leaveBalances.put(emp.getEmployeeId(), 0);
                }
            }

            List<dto.FormRequestDTO> forms = formRequestDAO.getAllFormRequestsByDepartmentId(manager.getDepartmentId(), null, null, null, null);
            for (dto.FormRequestDTO f : forms) {
                if (f.getStatus() == 0) {
                    pendingForms++;
                }
            }

            dao.AttendanceDAO attDAO = new dao.AttendanceDAO();
            List<dto.AttendanceSummaryDTO> summaries = attDAO.getMonthlySummary(manager.getDepartmentId(), prevMonth, prevYear);
            if (summaries != null) {
                summaries.sort((a, b) -> {
                    if (b.getWorkedHours() == null && a.getWorkedHours() == null) {
                        return 0;
                    }
                    if (b.getWorkedHours() == null) {
                        return -1;
                    }
                    if (a.getWorkedHours() == null) {
                        return 1;
                    }
                    return b.getWorkedHours().compareTo(a.getWorkedHours());
                });
                for (int i = 0; i < Math.min(3, summaries.size()); i++) {
                    topEmployees.add(summaries.get(i));
                }
            }
        }

        // We use 'deptTotalEmployees' to override 'totalEmployees' for the new UI's specific needs,
        // but the base code above still ran properly.
        request.setAttribute("totalEmployees", deptTotalEmployees);
        request.setAttribute("pendingForms", pendingForms);
        request.setAttribute("departmentEmployees", departmentEmployees);
        request.setAttribute("leaveBalances", leaveBalances);
        request.setAttribute("topEmployees", topEmployees);
        request.setAttribute("prevMonthStr", String.format("%02d/%d", prevMonth, prevYear));
        request.setAttribute("todayDate", java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        // -----------------------------------------------------------------
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
        setManagerLayout(request);

        EmployeeDetailDTO manager = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (manager == null || manager.getDepartmentId() <= 0) {
            request.setAttribute("error", "Bạn chưa được phân công vào phòng ban nào.");
            request.getRequestDispatcher("/public/manager/attendance/department_attendance.jsp").forward(request, response);
            return;
        }

        int departmentId = manager.getDepartmentId();
        LocalDate now = LocalDate.now();
        int month = paramOr(request, "month", now.minusMonths(1).getMonthValue());
        int year = paramOr(request, "year", now.minusMonths(1).getYear());

        // Cùng dữ liệu tổng hợp như trang tổng quan, nhưng khoá cứng vào phòng của trưởng phòng.
        java.util.List<dto.AttendanceSummaryDTO> summaries
                = attendanceService.getMonthlySummaries(departmentId, month, year);
        request.setAttribute("summaries", summaries);
        request.setAttribute("pagedSummaries", utils.Paging.page(request, summaries));
        request.setAttribute("canViewAll", false);
        request.setAttribute("selectedDepartmentId", departmentId);
        request.setAttribute("departmentName", manager.getDepartmentName());
        request.setAttribute("selectedMonth", month);
        request.setAttribute("selectedYear", year);

        // Panel chốt bảng chấm công cho trưởng phòng.
        model.AttendancePeriod closingRow
                = attendanceClosingService.getClosingRow(year, month, departmentId);
        java.util.List<model.AttendancePeriod> closingPeriods = new java.util.ArrayList<>();
        closingPeriods.add(closingRow);
        request.setAttribute("closingPeriods", closingPeriods);
        request.setAttribute("closingHasData", true);
        request.setAttribute("canManagerConfirm",
                closingRow.getStatus() == enums.AttendancePeriodStatus.WAITING_MANAGER.getRelatedNum());
        request.setAttribute("closingConfirmed",
                closingRow.getStatus() >= enums.AttendancePeriodStatus.MANAGER_CONFIRMED.getRelatedNum());
        request.setAttribute("closingDepartmentId", departmentId);

        request.getRequestDispatcher("/public/manager/attendance/department_attendance.jsp").forward(request, response);
    }

    private void displayOwnAttendance(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);

        LocalDate now = LocalDate.now();
        int month = paramOr(request, "month", now.minusMonths(1).getMonthValue());
        int year = paramOr(request, "year", now.minusMonths(1).getYear());

        EmployeeDetailDTO me = employeeDAO.getEmployeeByUserId(user.getUserId());
        List<Attendance> monthRows = (me != null)
                ? attendanceDAO.getDailyAttendance(me.getEmployeeId(), month, year)
                : new java.util.ArrayList<>();

        dto.AttendanceSummaryDTO summary = new dto.AttendanceSummaryDTO();
        BigDecimal worked = BigDecimal.ZERO;
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
                case 6:
                    summary.setWeekendDays(summary.getWeekendDays() + 1);
                    break;
                case 7:
                    summary.setMissingCheckDays(summary.getMissingCheckDays() + 1);
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
        request.setAttribute("monthRows", monthRows);

        if (me != null) {
            request.setAttribute("approvedOTDays",
                    overtimeDAO.getApprovedOTDaysInMonth(me.getEmployeeId(), month, year));
        }

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
        request.setAttribute("allowanceTypes", payrollConfigDAO.getAllowanceTypes(true));
        request.getRequestDispatcher("/public/manager/salary/own_salary.jsp").forward(request, response);
    }

    private void displayPayrollConfig(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "CONFIG_PAYROLL")) {
            request.getSession().setAttribute("error", "Bạn không có quyền cấu hình lương.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);

        List<PayrollSetting> settings = payrollConfigDAO.getConfigurablePayrollSettings();
        for (PayrollSetting setting : settings) {
            setting.setDisplayValue(payrollConfigWorkflowService.displayPayrollSettingValue(setting));
        }
        request.setAttribute("settings", settings);
        request.setAttribute("deductionRules", payrollConfigDAO.getDeductionRules(false));
        request.setAttribute("taxBrackets", payrollConfigDAO.getTaxBrackets(false));
        request.setAttribute("allowanceTypes", payrollConfigDAO.getAllowanceTypes(false));
        request.setAttribute("payrollConfigBaseUrl", request.getContextPath() + "/v1/manager/payroll-config");
        request.setAttribute("canEditPayrollConfig", true);
        request.setAttribute("canApprovePayrollConfig", false);
        request.setAttribute("sidebarPath", "/public/components/managerSideBar.jsp");
        request.setAttribute("topbarPath", "/public/components/managerTopBar.jsp");
        request.setAttribute("pendingRequests", payrollConfigWorkflowService.getPendingRequests());
        HttpSession session = request.getSession(false);
        if (session != null) {
            String success = (String) session.getAttribute("payrollConfigSuccess");
            String error = (String) session.getAttribute("payrollConfigError");
            if (success != null) {
                request.setAttribute("success", success);
                session.removeAttribute("payrollConfigSuccess");
            }
            if (error != null) {
                request.setAttribute("error", error);
                session.removeAttribute("payrollConfigError");
            }
        }
        request.getRequestDispatcher("/public/manager/salary/payroll_config.jsp").forward(request, response);
    }

    private void displayPayrollConfigHistory(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "CONFIG_PAYROLL")) {
            request.getSession().setAttribute("error", "Bạn không có quyền cấu hình lương.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        Integer status = parseIntOrNull(request.getParameter("status"));
        status = status != null && (status == PayrollConfigChangeRequest.STATUS_APPROVED
                || status == PayrollConfigChangeRequest.STATUS_REJECTED) ? status : null;
        String q = request.getParameter("q") == null ? "" : request.getParameter("q").trim();
        Integer requestedPage = parseIntOrNull(request.getParameter("page"));
        int pageSize = 10;
        int totalItems = payrollConfigWorkflowService.countHistory(status, q);
        int totalPages = Math.max(1, (totalItems + pageSize - 1) / pageSize);
        int currentPage = Math.min(Math.max(1, requestedPage == null ? 1 : requestedPage), totalPages);
        request.setAttribute("changeHistory", payrollConfigWorkflowService.getHistory(status, q, currentPage, pageSize));
        request.setAttribute("statusFilter", status);
        request.setAttribute("q", q);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalItems", totalItems);
        request.setAttribute("pageBase", request.getContextPath() + "/v1/manager/payroll-config/history?status="
                + (status == null ? "" : status) + "&q=" + q.replace(" ", "+"));
        request.setAttribute("payrollConfigBaseUrl", request.getContextPath() + "/v1/manager/payroll-config");
        request.getRequestDispatcher("/public/manager/salary/payroll_config_history.jsp").forward(request, response);
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
        boolean attendanceLocked = departmentId == null
                ? attendanceClosingService.isPeriodLocked(period[0], period[1])
                : attendanceClosingService.isDepartmentLocked(period[0], period[1], departmentId);
        List<PayrollPreviewDTO> payrollPreviews = payrollService.getAllPayrollForHr(user, period[0], period[1], departmentId);
        if (payrollPreviews.isEmpty()) {
            request.setAttribute("salaryError", buildSalaryPeriodMessage(period[0], period[1]));
        }
        request.setAttribute("payrollPreviews", payrollPreviews);
        request.setAttribute("selectedYear", period[0]);
        request.setAttribute("selectedMonth", period[1]);
        request.setAttribute("selectedDepartmentId", departmentId);
        request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
        request.setAttribute("attendanceLocked", attendanceLocked);
        request.setAttribute("canExportPayroll", payrollService.canExportPayroll(user));
        request.setAttribute("periodFinalized", payrollService.isPeriodFinalized(period[0], period[1]));
        boolean canApproveAll = payrollService.canApprovePayroll(user);
        request.setAttribute("canApprovePayroll", canApproveAll);
        if (canApproveAll) {
            // Duyệt luôn áp dụng cho toàn công ty nên số đếm cũng không lọc theo phòng ban đang xem.
            int pendingCount = payrollService.countPendingApprovalForPeriod(user, period[0], period[1], null);
            request.setAttribute("pendingApprovalCount", pendingCount);
        }
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
        request.setAttribute("allowanceTypes", payrollConfigDAO.getAllowanceTypes(true));
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
        boolean attendanceLocked = departmentId == null
                ? attendanceClosingService.isPeriodLocked(period[0], period[1])
                : attendanceClosingService.isDepartmentLocked(period[0], period[1], departmentId);
        if (!attendanceLocked) {
            request.getSession().setAttribute("error",
                    "Bảng chấm công kỳ này chưa được HR chốt cuối. Chưa thể xuất bảng lương.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/salary/all?month=" + period[1]
                    + "&year=" + period[0]
                    + (departmentId == null ? "" : "&departmentId=" + departmentId));
            return;
        }
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
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        int[] period = parseSalaryPeriod(request);
        Integer departmentId = parseIntOrNull(request.getParameter("departmentId"));

        // Duyệt luôn áp dụng cho toàn công ty, không bị giới hạn bởi bộ lọc phòng ban đang xem.
        int approvedCount = payrollService.approveAllPayrollForPeriod(user, period[0], period[1], null);
        if (approvedCount > 0) {
            request.getSession().setAttribute("success",
                    "Đã duyệt " + approvedCount + " bảng lương cho kỳ lương "
                    + String.format("%02d/%d", period[1], period[0]) + ".");
        } else {
            request.getSession().setAttribute("error",
                    "Không có bảng lương nào đang chờ duyệt để xử lý (hoặc tất cả đều là lương của chính bạn).");
        }

        StringBuilder url = new StringBuilder(request.getContextPath() + "/v1/manager/salary/all");
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
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        int[] period = parseSalaryPeriod(request);
        Integer departmentId = parseIntOrNull(request.getParameter("departmentId"));

        if (payrollService.isPeriodFinalized(period[0], period[1])) {
            request.getSession().setAttribute("error",
                    "Kỳ lương này đã được chốt, không thể tạo lại bảng lương.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/salary/all?month=" + period[1]
                    + "&year=" + period[0]
                    + (departmentId == null ? "" : "&departmentId=" + departmentId));
            return;
        }

        boolean locked = departmentId == null
                ? attendanceClosingService.isPeriodLocked(period[0], period[1])
                : attendanceClosingService.isDepartmentLocked(period[0], period[1], departmentId);
        if (!locked) {
            request.getSession().setAttribute("error",
                    "Bảng chấm công kỳ này chưa được HR chốt cuối. Chưa thể tính lương.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/salary/all?month=" + period[1]
                    + "&year=" + period[0]
                    + (departmentId == null ? "" : "&departmentId=" + departmentId));
            return;
        }

        int generated = payrollService.saveGeneratedPayrollForPeriod(period[0], period[1], departmentId);
        request.getSession().setAttribute("success", "Đã tạo bảng lương cho " + generated + " nhân sự.");
        response.sendRedirect(request.getContextPath() + "/v1/manager/salary/all?month=" + period[1]
                + "&year=" + period[0]
                + (departmentId == null ? "" : "&departmentId=" + departmentId));
    }

    private void handleRequestPayrollSetting(HttpServletRequest request, HttpServletResponse response,
            User user) throws IOException {
        String redirect = request.getContextPath() + "/v1/manager/payroll-config";
        if (!isHrStaff(user) || !hasPermission(user, "CONFIG_PAYROLL")) {
            request.getSession().setAttribute("payrollConfigError", "Bạn không có quyền cấu hình lương.");
            response.sendRedirect(redirect);
            return;
        }
        PayrollSetting setting = payrollConfigWorkflowService.buildSetting(
                request.getParameter("settingKey"),
                request.getParameter("settingValue"),
                request.getParameter("description"));
        String error = payrollConfigWorkflowService.validatePayrollSetting(setting);
        if (error != null) {
            request.getSession().setAttribute("payrollConfigError", error);
            response.sendRedirect(redirect);
            return;
        }
        int requestId = payrollConfigWorkflowService.requestSettingChange(setting, user);
        request.getSession().setAttribute(requestId > 0 ? "payrollConfigSuccess" : "payrollConfigError",
                requestId > 0 ? "Đã gửi yêu cầu thay đổi tham số lương chờ Quản trị doanh nghiệp duyệt."
                        : "Không thể gửi yêu cầu thay đổi tham số lương.");
        response.sendRedirect(redirect);
    }

    private void handleRequestPayrollDeduction(HttpServletRequest request, HttpServletResponse response,
            User user) throws IOException {
        String redirect = request.getContextPath() + "/v1/manager/payroll-config";
        if (!isHrStaff(user) || !hasPermission(user, "CONFIG_PAYROLL")) {
            request.getSession().setAttribute("payrollConfigError", "Bạn không có quyền cấu hình lương.");
            response.sendRedirect(redirect);
            return;
        }
        PayrollDeductionRule rule = payrollConfigWorkflowService.buildDeductionRule(
                request.getParameter("ruleId"),
                request.getParameter("ruleCode"),
                request.getParameter("ruleName"),
                request.getParameter("ruleType"),
                request.getParameter("employerRate"),
                request.getParameter("employeeRate"));
        String error = payrollConfigWorkflowService.validateDeductionRule(rule);
        if (error != null) {
            request.getSession().setAttribute("payrollConfigError", error);
            response.sendRedirect(redirect);
            return;
        }
        int requestId = payrollConfigWorkflowService.requestDeductionSave(rule, user);
        request.getSession().setAttribute(requestId > 0 ? "payrollConfigSuccess" : "payrollConfigError",
                requestId > 0 ? "Đã gửi yêu cầu thay đổi khoản khấu trừ chờ Quản trị doanh nghiệp duyệt."
                        : "Không thể gửi yêu cầu thay đổi khoản khấu trừ.");
        response.sendRedirect(redirect);
    }

    private void handleRequestPayrollDeductionDelete(HttpServletRequest request, HttpServletResponse response,
            User user) throws IOException {
        String redirect = request.getContextPath() + "/v1/manager/payroll-config";
        if (!isHrStaff(user) || !hasPermission(user, "CONFIG_PAYROLL")) {
            request.getSession().setAttribute("payrollConfigError", "Bạn không có quyền cấu hình lương.");
            response.sendRedirect(redirect);
            return;
        }
        Integer ruleId = parseIntOrNull(request.getParameter("ruleId"));
        if (ruleId == null) {
            request.getSession().setAttribute("payrollConfigError", "Thiếu khoản khấu trừ cần xóa.");
            response.sendRedirect(redirect);
            return;
        }
        int requestId = payrollConfigWorkflowService.requestDeductionDelete(ruleId, user);
        request.getSession().setAttribute(requestId > 0 ? "payrollConfigSuccess" : "payrollConfigError",
                requestId > 0 ? "Đã gửi yêu cầu xóa khoản khấu trừ chờ Quản trị doanh nghiệp duyệt."
                        : "Không thể gửi yêu cầu xóa khoản khấu trừ.");
        response.sendRedirect(redirect);
    }

    private void handleRequestPayrollTaxBracket(HttpServletRequest request, HttpServletResponse response,
            User user) throws IOException {
        String redirect = request.getContextPath() + "/v1/manager/payroll-config";
        if (!isHrStaff(user) || !hasPermission(user, "CONFIG_PAYROLL")) {
            request.getSession().setAttribute("payrollConfigError", "Bạn không có quyền cấu hình lương.");
            response.sendRedirect(redirect);
            return;
        }
        List<PayrollTaxBracket> brackets = payrollConfigWorkflowService.buildTaxBrackets(
                request.getParameterValues("bracketId"),
                request.getParameterValues("minIncome"),
                request.getParameterValues("maxIncome"),
                request.getParameterValues("taxRate"));
        String error = payrollConfigWorkflowService.validateTaxBrackets(brackets);
        if (error != null) {
            request.getSession().setAttribute("payrollConfigError", error);
            response.sendRedirect(redirect);
            return;
        }
        int requestId = payrollConfigWorkflowService.requestTaxBracketSave(brackets, user);
        request.getSession().setAttribute(requestId > 0 ? "payrollConfigSuccess" : "payrollConfigError",
                requestId > 0 ? "Đã gửi yêu cầu thay đổi bậc thuế chờ Quản trị doanh nghiệp duyệt."
                        : "Không thể gửi yêu cầu thay đổi bậc thuế.");
        response.sendRedirect(redirect);
    }

    private void handleRequestPayrollAllowance(HttpServletRequest request, HttpServletResponse response,
            User user) throws IOException {
        String redirect = request.getContextPath() + "/v1/manager/payroll-config";
        if (!isHrStaff(user) || !hasPermission(user, "CONFIG_PAYROLL")) {
            request.getSession().setAttribute("payrollConfigError", "Bạn không có quyền cấu hình lương.");
            response.sendRedirect(redirect);
            return;
        }
        PayrollAllowanceType type = payrollConfigWorkflowService.buildAllowanceType(
                request.getParameter("allowanceId"),
                request.getParameter("allowanceCode"),
                request.getParameter("allowanceName"),
                request.getParameter("amount"),
                request.getParameter("insuranceApplicable"),
                request.getParameter("active"));
        String error = payrollConfigWorkflowService.validateAllowanceType(type);
        if (error != null) {
            request.getSession().setAttribute("payrollConfigError", error);
            response.sendRedirect(redirect);
            return;
        }
        int requestId = payrollConfigWorkflowService.requestAllowanceSave(type, user);
        request.getSession().setAttribute(requestId > 0 ? "payrollConfigSuccess" : "payrollConfigError",
                requestId > 0 ? "Đã gửi yêu cầu thay đổi loại phụ cấp chờ Quản trị doanh nghiệp duyệt."
                        : "Không thể gửi yêu cầu thay đổi loại phụ cấp.");
        response.sendRedirect(redirect);
    }

    private void handleRequestPayrollAllowanceDelete(HttpServletRequest request, HttpServletResponse response,
            User user) throws IOException {
        String redirect = request.getContextPath() + "/v1/manager/payroll-config";
        if (!isHrStaff(user) || !hasPermission(user, "CONFIG_PAYROLL")) {
            request.getSession().setAttribute("payrollConfigError", "Bạn không có quyền cấu hình lương.");
            response.sendRedirect(redirect);
            return;
        }
        Integer allowanceId = parseIntOrNull(request.getParameter("allowanceId"));
        if (allowanceId == null) {
            request.getSession().setAttribute("payrollConfigError", "Thiếu loại phụ cấp cần xóa.");
            response.sendRedirect(redirect);
            return;
        }
        int requestId = payrollConfigWorkflowService.requestAllowanceDelete(allowanceId, user);
        request.getSession().setAttribute(requestId > 0 ? "payrollConfigSuccess" : "payrollConfigError",
                requestId > 0 ? "Đã gửi yêu cầu xóa loại phụ cấp chờ Quản trị doanh nghiệp duyệt."
                        : "Không thể gửi yêu cầu xóa loại phụ cấp.");
        response.sendRedirect(redirect);
    }

    private void handleConfirmDeptAttendance(HttpServletRequest request, HttpServletResponse response,
            User user) throws IOException {
        LocalDate prev = LocalDate.now().minusMonths(1);
        int month = paramOr(request, "month", prev.getMonthValue());
        int year = paramOr(request, "year", prev.getYear());
        Integer departmentId = parseIntOrNull(request.getParameter("departmentId"));
        if (departmentId == null) {
            departmentId = resolveManagerDepartmentId(user);
        }
        if (departmentId == null) {
            request.getSession().setAttribute("error", "Bạn chưa được phân công vào phòng ban nào.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/attendance/overview");
            return;
        }
        ClosingResult result = attendanceClosingService.confirmByManager(year, month, departmentId, user);
        request.getSession().setAttribute(result.isSuccess() ? "success" : "error", result.getMessage());
        response.sendRedirect(request.getContextPath()
                + "/v1/manager/attendance/my-department-attendance?month=" + month + "&year=" + year);
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
        int month = paramOr(request, "month", now.minusMonths(1).getMonthValue());
        int year = paramOr(request, "year", now.minusMonths(1).getYear());

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

        // Trạng thái chốt của phòng đang xem: hiển thị nút "Chốt phòng" khi đang chờ trưởng phòng.
        if (departmentId != null) {
            model.AttendancePeriod closingRow
                    = attendanceClosingService.getClosingRow(year, month, departmentId);
            java.util.List<model.AttendancePeriod> closingPeriods = new java.util.ArrayList<>();
            closingPeriods.add(closingRow);
            request.setAttribute("closingPeriods", closingPeriods);
            request.setAttribute("closingHasData", true);
            request.setAttribute("canManagerConfirm",
                    closingRow.getStatus() == enums.AttendancePeriodStatus.WAITING_MANAGER.getRelatedNum());
            request.setAttribute("closingConfirmed",
                    closingRow.getStatus() >= enums.AttendancePeriodStatus.MANAGER_CONFIRMED.getRelatedNum());
            request.setAttribute("closingDepartmentId", departmentId);
        }
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
        int month = paramOr(request, "month", now.minusMonths(1).getMonthValue());
        int year = paramOr(request, "year", now.minusMonths(1).getYear());

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
        int month = paramOr(request, "month", now.minusMonths(1).getMonthValue());
        int year = paramOr(request, "year", now.minusMonths(1).getYear());

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
        request.setAttribute("dependents", dependentDAO.getActiveByEmployeeId(employee.getEmployeeId()));
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

        String generatedCode = contractService.generateNextContractCode();
        request.setAttribute("generatedCode", generatedCode);
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
            request.getSession().setAttribute("error", "Không tìm thấy hợp đồng phù hợp.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee_info/list");
            return;
        }

        EmployeeDetailDTO employee = employeeDAO.getEmployeeById(contract.getEmployeeId());
        if (employee == null) {
            request.getSession().setAttribute("error", "Không tìm thấy nhân viên của hợp đồng.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee_info/list");
            return;
        }

        response.sendRedirect(request.getContextPath()
                + "/v1/manager/contract/detail?contractId=" + contract.getContractId());
    }

    private void displayPendingContracts(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrManager(user) || !hasPermission(user, "VIEW_PENDING_CONTRACTS")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem hợp đồng chờ duyệt.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        String keyword = trimToNull(request.getParameter("keyword"));
        String contractType = trimToNull(request.getParameter("contractType"));
        List<EmploymentContract> contracts = new ArrayList<>();
        Map<Integer, EmployeeDetailDTO> employeeMap = new HashMap<>();
        List<EmploymentContract> pendingContracts;
        try {
            pendingContracts = contractDAO.getPendingContracts();
        } catch (java.sql.SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to load pending contracts", e);
            request.getSession().setAttribute("error", "Không thể tải danh sách hợp đồng chờ duyệt.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        for (EmploymentContract contract : pendingContracts) {
            EmployeeDetailDTO employee = employeeDAO.getEmployeeById(contract.getEmployeeId());
            if (employee == null) {
                continue;
            }
            if (keyword != null) {
                String normalizedKeyword = keyword.toLowerCase();
                boolean matched = contract.getContractCode().toLowerCase().contains(normalizedKeyword)
                        || employee.getEmployeeCode().toLowerCase().contains(normalizedKeyword)
                        || employee.getFullName().toLowerCase().contains(normalizedKeyword);
                if (!matched) {
                    continue;
                }
            }
            if (contractType != null && (contract.getContractType() == null
                    || !contractType.equalsIgnoreCase(contract.getContractType().name()))) {
                continue;
            }
            contracts.add(contract);
            employeeMap.put(contract.getEmployeeId(), employee);
        }
        request.setAttribute("contracts", contracts);
        request.setAttribute("employeeMap", employeeMap);
        request.setAttribute("keyword", keyword);
        request.setAttribute("contractType", contractType);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/contract/pending_contract_list.jsp").forward(request, response);
    }

    private void displayContractHistory(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!"own".equalsIgnoreCase(request.getParameter("scope"))
                && (!isHrManager(user) || !hasPermission(user, "VIEW_ALL_CONTRACTS"))) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem lịch sử hợp đồng.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        if ("own".equalsIgnoreCase(request.getParameter("scope"))) {
            if (!perms.contains("VIEW_OWN_CONTRACT")) {
                request.getSession().setAttribute("error", "Bạn không có quyền xem lịch sử hợp đồng.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
                return;
            }
            EmployeeDetailDTO employee = employeeDAO.getEmployeeByUserId(user.getUserId());
            if (employee == null) {
                request.getSession().setAttribute("error", "Không tìm thấy thông tin nhân viên.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
                return;
            }
            request.setAttribute("employee", employee);
            request.setAttribute("contracts", contractDAO.getContractsByEmployeeId(employee.getEmployeeId()));
            Map<Integer, EmployeeDetailDTO> employeeMap = new HashMap<>();
            employeeMap.put(employee.getEmployeeId(), employee);
            request.setAttribute("employeeMap", employeeMap);
            setPermissionFlags(request, perms);
            request.getRequestDispatcher("/public/manager/contract/contract_history.jsp").forward(request, response);
            return;
        }
        List<EmploymentContract> contracts = contractDAO.getAllContracts();
        Map<Integer, EmployeeDetailDTO> employeeMap = new HashMap<>();
        for (EmploymentContract contract : contracts) {
            if (!employeeMap.containsKey(contract.getEmployeeId())) {
                employeeMap.put(contract.getEmployeeId(), employeeDAO.getEmployeeById(contract.getEmployeeId()));
            }
        }
        request.setAttribute("contracts", contracts);
        request.setAttribute("employeeMap", employeeMap);
        setPermissionFlags(request, perms);
        request.setAttribute("isHrStaffRole", isHrStaff(user));
        request.getRequestDispatcher("/public/manager/contract/contract_history.jsp").forward(request, response);
    }

    private void displayContractDetail(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrManager(user)
                || !(hasPermission(user, "VIEW_ALL_CONTRACTS") || hasPermission(user, "VIEW_PENDING_CONTRACTS"))) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem hợp đồng.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        EmploymentContract contract = getContractFromRequest(request);
        if (contract == null) {
            request.getSession().setAttribute("error", "Không tìm thấy hợp đồng phù hợp.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        EmployeeDetailDTO employee = employeeDAO.getEmployeeById(contract.getEmployeeId());
        if (employee == null) {
            request.getSession().setAttribute("error", "Không tìm thấy nhân viên của hợp đồng.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("contract", contract);
        request.setAttribute("employee", employee);
        request.setAttribute("backUrl", contract.getStatus() == ContractStatus.PENDING_APPROVAL
                ? "/v1/manager/contract/pending"
                : "/v1/manager/contract/history");
        List<ContractAmendment> amendments = amendmentDAO.getByContractId(contract.getContractId());
        request.setAttribute("amendments", amendments);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/contract/contract_preview.jsp").forward(request, response);
    }

    private void displayTerminateContractList(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrManager(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền chấm dứt hợp đồng.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);

        String contractIdParam = trimToNull(request.getParameter("id"));
        if (contractIdParam != null) {
            try {
                int contractId = Integer.parseInt(contractIdParam);
                EmploymentContract contract = contractDAO.getContractById(contractId);
                if (contract == null) {
                    request.getSession().setAttribute("error", "Không tìm thấy hợp đồng.");
                } else if (contract.getStatus() != ContractStatus.ACTIVE
                        && contract.getStatus() != ContractStatus.PENDING_ACTIVATION) {
                    request.getSession().setAttribute("error", "Chỉ có thể chấm dứt hợp đồng đang hiệu lực hoặc chờ hiệu lực.");
                } else {
                    // Valid - forward to form page
                    EmployeeDetailDTO employee = employeeDAO.getEmployeeById(contract.getEmployeeId());
                    request.setAttribute("contract", contract);
                    request.setAttribute("employee", employee);
                    setPermissionFlags(request, perms);
                    request.getRequestDispatcher("/public/manager/contract/terminate_contract.jsp").forward(request, response);
                    return;
                }
            } catch (NumberFormatException e) {
                request.getSession().setAttribute("error", "Mã hợp đồng không hợp lệ.");
            }
            response.sendRedirect(request.getContextPath() + "/v1/manager/contract/terminate");
            return;
        }

        List<EmploymentContract> terminableContracts = contractDAO.getTerminableContracts();
        request.setAttribute("contracts", terminableContracts);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/contract/terminate_contract_list.jsp").forward(request, response);
    }

    private void displayContractAmendments(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrManager(user)
                || !(hasPermission(user, "VIEW_ALL_CONTRACTS") || hasPermission(user, "VIEW_PENDING_CONTRACTS"))) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem phụ lục hợp đồng.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        String keyword = trimToNull(request.getParameter("keyword"));
        String type = trimToNull(request.getParameter("type"));
        String contractIdParam = trimToNull(request.getParameter("contractId"));

        int contractId = 0;
        EmploymentContract contract = null;
        EmployeeDetailDTO employee = null;

        if (contractIdParam != null) {
            try {
                contractId = Integer.parseInt(contractIdParam);
                contract = contractDAO.getContractById(contractId);
                if (contract != null) {
                    employee = employeeDAO.getEmployeeById(contract.getEmployeeId());
                }
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        List<ContractAmendment> amendments;
        if (contractId > 0) {

            amendments = amendmentDAO.getByContractId(contractId);

            if (keyword != null || type != null) {
                amendments = amendments.stream()
                        .filter(a -> (keyword == null || a.getAmendmentCode().contains(keyword) || a.getReason().contains(keyword)))
                        .filter(a -> (type == null || type.equals("ALL") || a.getAmendmentType().name().equals(type)))
                        .collect(Collectors.toList());
            }
        } else {
            amendments = amendmentDAO.searchAmendments(keyword, type);
        }

        request.setAttribute("contract", contract);
        request.setAttribute("employee", employee);
        request.setAttribute("amendments", amendments);
        request.setAttribute("currentKeyword", keyword);
        request.setAttribute("currentType", type);

        request.getRequestDispatcher("/public/manager/contract/amendment_history.jsp")
                .forward(request, response);
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
        if (myEmployee != null) {
            request.setAttribute("dependents", dependentDAO.getActiveByEmployeeId(myEmployee.getEmployeeId()));
            request.setAttribute("dependentStatusAction", request.getContextPath() + "/v1/manager/forms/dependent/status");
        }
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

        int dependentCount = current.getDependentCount();

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
        emp.setDependentCount(dependentCount);
        emp.setUnionMember(request.getParameter("unionMember") != null);
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
        String effectiveDate = request.getParameter("effectiveDate");
        String durationValueStr = request.getParameter("durationValue");
        String durationUnit = request.getParameter("durationUnit");
        String signedDate = request.getParameter("signedDate");
        String salaryParam = request.getParameter("salary");

        if (code == null || type == null || isBlank(employeeParam) || isBlank(effectiveDate) || isBlank(salaryParam)) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ mã hợp đồng, nhân viên, loại hợp đồng, ngày bắt đầu và lương.");
            request.setAttribute("employees", employeeDAO.getAllEmployees());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/contract/add_contract.jsp").forward(request, response);
            return;
        }

        EmploymentContract contract = new EmploymentContract();
        try {
            contract.setEmployeeId(Integer.parseInt(employeeParam));
            contract.setEffectiveDate(java.sql.Date.valueOf(effectiveDate));

            if (durationValueStr != null && !durationValueStr.isEmpty() && durationUnit != null && !durationUnit.isEmpty()) {
                int durVal = Integer.parseInt(durationValueStr);
                contract.setDurationValue(durVal);
                contract.setDurationUnit(durationUnit);
                contract.setEndDate(contractService.calculateEndDate(contract.getEffectiveDate(), durVal, durationUnit));
            }

            if (signedDate != null && !signedDate.isEmpty()) {
                contract.setSignedDate(java.sql.Date.valueOf(signedDate));
            } else {
                if (effectiveDate != null && !effectiveDate.isEmpty()) {
                    contract.setSignedDate(java.sql.Date.valueOf(effectiveDate));
                }
            }

            contract.setSalary(new BigDecimal(salaryParam));
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", "Dữ liệu hợp đồng không hợp lệ.");
            request.setAttribute("employees", employeeDAO.getAllEmployees());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/contract/add_contract.jsp").forward(request, response);
            return;
        }

        try {
            contract.setContractType(ContractType.valueOf(type.toUpperCase()));
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", "Loại hợp đồng không hợp lệ.");
            request.setAttribute("employees", employeeDAO.getAllEmployees());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/contract/add_contract.jsp").forward(request, response);
            return;
        }
        if (contract.getSalary().compareTo(BigDecimal.ZERO) < 0) {
            request.setAttribute("error", "Lương không được âm.");
            request.setAttribute("employees", employeeDAO.getAllEmployees());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/contract/add_contract.jsp").forward(request, response);
            return;
        }
        contract.setContractCode(code);
        contract.setStatus(ContractStatus.PENDING_APPROVAL);
        contract.setNote(trimToNull(request.getParameter("note")));
        contract.setCreatedBy(user.getUserId());

        Part filePart = request.getPart("signedContract");
        if (filePart != null && filePart.getSize() > 0) {
            String fileName = filePart.getSubmittedFileName();
            if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {

                String contentType = filePart.getContentType();
                if ("application/pdf".equals(contentType)) {

                    if (filePart.getSize() <= 10 * 1024 * 1024) {

                        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                        String safeContractCode = contract.getContractCode().replaceAll("[^a-zA-Z0-9]", "");
                        String uniqueFileName = safeContractCode + "_" + timestamp + ".pdf";

                        int year = java.time.Year.now().getValue();

                        String uploadBase = getServletContext().getRealPath("/") + "../../uploads/";
                        String uploadDir = uploadBase + "contracts/" + year;

                        java.io.File dir = new java.io.File(uploadDir);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }

                        java.io.File destFile = new java.io.File(dir, uniqueFileName);
                        try (java.io.InputStream is = filePart.getInputStream()) {
                            java.nio.file.Files.copy(is, destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        }

                        contract.setContractFilePath("contracts/" + year + "/" + uniqueFileName);
                        contract.setContractFileName(fileName);
                        contract.setUploadedAt(new java.sql.Date(System.currentTimeMillis()));
                        contract.setUploadedBy(user.getUserId());
                    }
                }
            }
        }

        ContractOperationResult result;
        result = contractService.createContract(contract);

        if (result.isSuccess()) {
            boolean unionMember = request.getParameter("unionMember") != null;
            employeeDAO.updateUnionMember(contract.getEmployeeId(), unionMember);

            request.getSession().setAttribute("success", "Thêm hợp đồng lao động thành công.");
            EmploymentContract createdContract = contractDAO.getLatestContractByEmployeeId(contract.getEmployeeId());
            if (createdContract != null) {
                response.sendRedirect(request.getContextPath()
                        + "/v1/manager/contract/detail?contractId=" + createdContract.getContractId());
            } else {
                response.sendRedirect(request.getContextPath() + "/v1/manager/contract/history");
            }
        } else {
            request.setAttribute("error", "Thêm hợp đồng thất bại: " + result.getMessage());
            request.setAttribute("employees", employeeDAO.getAllEmployees());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/contract/add_contract.jsp").forward(request, response);
        }
    }

    private EmploymentContract getContractFromRequest(HttpServletRequest request) {
        String contractIdParam = request.getParameter("contractId");
        if (isBlank(contractIdParam)) {
            contractIdParam = request.getParameter("id");
        }
        if (!isBlank(contractIdParam)) {
            try {
                return contractDAO.getContractById(Integer.parseInt(contractIdParam));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        String employeeIdParam = request.getParameter("employeeId");
        if (isBlank(employeeIdParam)) {
            return null;
        }
        try {
            return contractDAO.getLatestContractByEmployeeId(Integer.parseInt(employeeIdParam));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void handleApproveContract(HttpServletRequest request, HttpServletResponse response,
            User user) throws IOException {
        if (!isHrManager(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền duyệt hợp đồng.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        Integer contractId = parseIntOrNull(request.getParameter("contractId"));
        if (contractId == null) {
            request.getSession().setAttribute("error", "Mã hợp đồng không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/contract/pending");
            return;
        }
        ContractOperationResult result = contractService.approveContract(contractId, user.getUserId());
        request.getSession().setAttribute(result.isSuccess() ? "success" : "error", result.getMessage());
        response.sendRedirect(request.getContextPath() + "/v1/manager/contract/detail?contractId=" + contractId);
    }

    private void handleRejectContract(HttpServletRequest request, HttpServletResponse response,
            User user) throws IOException {
        if (!isHrManager(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền từ chối hợp đồng.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        Integer contractId = parseIntOrNull(request.getParameter("contractId"));
        if (contractId == null) {
            request.getSession().setAttribute("error", "Mã hợp đồng không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/contract/pending");
            return;
        }
        String rejectReason = trimToNull(request.getParameter("rejectReason"));
        if (rejectReason == null) {
            rejectReason = trimToNull(request.getParameter("reason"));
        }
        ContractOperationResult result = contractService.rejectContract(contractId, user.getUserId(), rejectReason);
        request.getSession().setAttribute(result.isSuccess() ? "success" : "error", result.getMessage());
        response.sendRedirect(request.getContextPath() + "/v1/manager/contract/detail?contractId=" + contractId);
    }

    private void handleCancelContract(HttpServletRequest request, HttpServletResponse response,
            User user) throws IOException {
        if (!isHrStaff(user) || !hasPermission(user, "ADD_EMPLOYMENT_CONTRACT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền hủy hợp đồng.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        Integer contractId = parseIntOrNull(request.getParameter("contractId"));
        if (contractId == null) {
            request.getSession().setAttribute("error", "Mã hợp đồng không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/contract/pending");
            return;
        }
        ContractOperationResult cancelResult = contractService.cancelContract(contractId, user.getUserId());
        request.getSession().setAttribute(cancelResult.isSuccess() ? "success" : "error",
                cancelResult.isSuccess() ? "Đã hủy hợp đồng." : cancelResult.getMessage());
        response.sendRedirect(request.getContextPath() + "/v1/manager/contract/detail?contractId=" + contractId);
    }

    private void handleTerminateContract(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrManager(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền chấm dứt hợp đồng.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        Integer contractId = parseIntOrNull(request.getParameter("contractId"));
        if (contractId == null) {
            request.getSession().setAttribute("error", "Mã hợp đồng không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/contract/terminate");
            return;
        }

        EmploymentContract contract = contractDAO.getContractById(contractId);
        if (contract == null) {
            request.getSession().setAttribute("error", "Không tìm thấy hợp đồng.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/contract/terminate");
            return;
        }

        String terminationDateStr = trimToNull(request.getParameter("terminationDate"));
        String terminationReason = trimToNull(request.getParameter("terminationReason"));
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);

        String error = null;
        java.sql.Date terminationDate = null;

        if (terminationDateStr == null) {
            error = "Ngày chấm dứt không được để trống.";
        } else {
            try {
                terminationDate = java.sql.Date.valueOf(terminationDateStr);
            } catch (IllegalArgumentException e) {
                error = "Ngày chấm dứt không hợp lệ.";
            }
        }

        if (terminationReason == null) {
            error = (error == null) ? "Lý do chấm dứt không được để trống." : error + " Lý do chấm dứt không được để trống.";
        }

        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("contract", contract);
            request.setAttribute("employee", employeeDAO.getEmployeeById(contract.getEmployeeId()));
            request.setAttribute("terminationDate", terminationDateStr);
            request.setAttribute("terminationReason", terminationReason);
            setPermissionFlags(request, perms);
            request.getRequestDispatcher("/public/manager/contract/terminate_contract.jsp").forward(request, response);
            return;
        }

        ContractOperationResult result = contractService.terminateContract(
                contractId, terminationDate, terminationReason, user.getUserId());

        if (result.isSuccess()) {
            request.getSession().setAttribute("success", result.getMessage());
            response.sendRedirect(request.getContextPath() + "/v1/manager/contract/detail?contractId=" + contractId);
        } else {
            request.setAttribute("error", result.getMessage());
            request.setAttribute("contract", contract);
            request.setAttribute("employee", employeeDAO.getEmployeeById(contract.getEmployeeId()));
            request.setAttribute("terminationDate", terminationDateStr);
            request.setAttribute("terminationReason", terminationReason);
            setPermissionFlags(request, perms);
            request.getRequestDispatcher("/public/manager/contract/terminate_contract.jsp").forward(request, response);
        }
    }

    private void handleParsePdf(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            jakarta.servlet.http.Part filePart = request.getPart("file");
            if (filePart == null || filePart.getSize() <= 0) {
                response.setStatus(400);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write("{\"error\":\"Không tìm thấy file PDF.\"}");
                return;
            }

            PdfParsingService parsingService = new PdfParsingService();
            ContractImportDTO dto = parsingService.parsePdf(filePart.getInputStream());

            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write(GSON.toJson(dto));

        } catch (Exception e) {
            response.setStatus(500);
            response.setContentType("application/json; charset=UTF-8");
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "Lỗi xử lý PDF: " + e.getMessage());
            response.getWriter().write(GSON.toJson(errorMap));
        }
    }

    private void handleRunSchedulerNow(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        if (!isHrManager(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền thực hiện thao tác này.");
            if (!isAjaxRequest(request)) {
                response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            } else {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Permission denied.\"}");
            }
            return;
        }

        try {
            ContractSchedulerInitializerListener listener = (ContractSchedulerInitializerListener) getServletContext().getAttribute("contractSchedulerListener");

            if (listener == null) {
                String msg = "Scheduler is not available (listener not initialized).";
                sendJsonResponse(response, false, msg);
                return;
            }

            if (listener.isRunning()) {
                sendJsonResponse(response, false, "Scheduler is already running. Please wait.");
                return;
            }

            boolean submitted = listener.runNow();
            if (submitted) {
                sendJsonResponse(response, true, "Scheduler task submitted successfully.");
            } else {
                sendJsonResponse(response, false, "Scheduler is not available (shutting down or error).");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error running scheduler manually", e);
            sendJsonResponse(response, false, "Internal error: " + e.getMessage());
        }
    }

    private void forwardContractFormError(HttpServletRequest request, HttpServletResponse response,
            User user, String message) throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("error", message);
        request.setAttribute("employees", employeeDAO.getAllEmployees());
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/contract/add_contract.jsp").forward(request, response);
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
        setImportWindowAttributes(request);
        request.getRequestDispatcher("/public/manager/attendance/attendance_import.jsp").forward(request, response);
    }

    /**
     * Đặt các thuộc tính phục vụ giao diện import: tháng được phép (tháng liền
     * trước), và cờ cho biết cửa sổ import (2 ngày đầu tháng) còn mở hay không.
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
     * Kiểm tra ràng buộc thời gian import chấm công: chỉ cho phép import trong
     * 2 ngày đầu mỗi tháng (ngày 1 và 2), và chỉ cho tháng liền trước. Trả về
     * thông báo lỗi nếu không hợp lệ, null nếu hợp lệ.
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

        String windowError = validateImportWindow(month, year);
        if (windowError != null) {
            forwardImportError(request, response, windowError, month, year, departmentId);
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
        setImportWindowAttributes(request);
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
            request.setAttribute("isHrStaff", isHrStaff(user));
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
            request.getSession().setAttribute("error", "Bạn chưa được phân công vào phòng ban nào.");
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

    // ========== XỬ LÝ DUYỆT ĐƠN (Manager - Bước 1) ==========
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
            FormRequestDTO form = formRequestDAO.getFormRequestById(formId);
            if (form == null) {
                request.getSession().setAttribute("error", "Không tìm thấy đơn.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/forms/dept-forms");
                return;
            }

            if ("DEPENDENT".equals(form.getFormTypeCode())) {
                request.getSession().setAttribute("error", "Đơn người phụ thuộc chỉ do HR duyệt.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/forms/dept-forms");
                return;
            }

            if ("LEAVE".equals(form.getFormTypeCode())) {
                LeaveFormRequestDTO leaveForm = (LeaveFormRequestDTO) form;
                int year = (leaveForm.getStartDate() != null)
                        ? leaveForm.getStartDate().toLocalDate().getYear()
                        : java.time.LocalDate.now().getYear();
                LeaveBalance lb = leaveBalanceDAO.getLeaveBalance(form.getEmployeeId(), year);
                if (lb != null) {
                    int remaining = lb.getRemainingDays();
                    if (leaveForm.getTotalDays() != null && leaveForm.getTotalDays() > remaining) {
                        request.getSession().setAttribute("error", "Duyệt đơn thất bại do vượt quá số phép. Đơn nghỉ (" + leaveForm.getTotalDays() + " ngày), Phép còn lại (" + remaining + " ngày).");
                        response.sendRedirect(request.getContextPath() + "/v1/manager/forms/dept-forms");
                        return;
                    }
                }
            }
            boolean ok = formRequestDAO.approveFormRequest(formId, me.getEmployeeId(), note);
            if (ok) {
                if (form != null) {
                    switch (form.getFormTypeCode()) {
                        case "LEAVE":
                            onManagerApproveLeave(form, me);
                            break;
                        case "COMPLAINT":
                            onManagerApproveComplaint(form, me);
                            break;
                        case "DEPENDENT":
                            LOGGER.log(Level.INFO, "Manager approved dependent formId={0}, waiting for HR second approval.", form.getFormId());
                            break;
                        default:
                            break;
                    }
                }
            }
            request.getSession().setAttribute(ok ? "success" : "error",
                    ok ? "Duyệt đơn thành công." : "Duyệt đơn thất bại.");
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã đơn không hợp lệ.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/manager/forms/dept-forms");
    }

    private void onManagerApproveLeave(FormRequestDTO form, EmployeeDetailDTO me) {
        if (form instanceof LeaveFormRequestDTO) {
            LeaveFormRequestDTO leaveForm = (LeaveFormRequestDTO) form;
            int year = (leaveForm.getStartDate() != null)
                    ? leaveForm.getStartDate().toLocalDate().getYear()
                    : java.time.LocalDate.now().getYear();
            LeaveBalance lb = leaveBalanceDAO.getLeaveBalance(form.getEmployeeId(), year);
            if (lb != null && leaveForm.getTotalDays() != null) {
                leaveBalanceDAO.updateUsedDays(form.getEmployeeId(), year, leaveForm.getTotalDays());
            }
        }
    }

    private void onManagerApproveComplaint(FormRequestDTO form, EmployeeDetailDTO me) {
        LOGGER.log(Level.INFO, "Manager approved complaint formId={0}, waiting for HR second approval.", form.getFormId());
    }

    // ========== Xử LÝ DUYỆT LẦN 2 (HR - BƯỚc 2, chỉ áp dụng cho COMPLAINT) ==========
    private void handleHrApproveForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Chỉ nhân viên HR mới có thể thực hiện bước duyệt này.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        EmployeeDetailDTO me = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (me == null) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        String rawId = request.getParameter("formId");
        String note = request.getParameter("note");
        if (isBlank(rawId)) {
            request.getSession().setAttribute("error", "Thiếu mã đơn.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/forms/all");
            return;
        }
        try {
            int formId = Integer.parseInt(rawId);
            FormRequestDTO form = formRequestDAO.getFormRequestById(formId);
            if (form == null) {
                request.getSession().setAttribute("error", "Không tìm thấy đơn yêu cầu.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/forms/all");
                return;
            }
            // Chỉ cho phép duyệt đơn đang ở status 1 (Manager đã duyệt)
            int fromStatus = "DEPENDENT".equals(form.getFormTypeCode()) ? 0 : 1;
            if (form.getStatus() != fromStatus) {
                request.getSession().setAttribute("error", "Đơn này không ở trạng thái chờ HR duyệt.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/forms/all");
                return;
            }
            boolean ok = formRequestDAO.approveFormRequestFromStatus(formId, fromStatus, 4, me.getEmployeeId(), note);
            boolean dependentAdded = false;
            boolean dependentChanged = false;
            if (ok) {
                switch (form.getFormTypeCode()) {
                    case "COMPLAINT":
                        onHrApproveComplaint(form, me, request);
                        break;
                    case "DEPENDENT":
                        dependentAdded = dependentDAO.approveByFormId(formId);
                        dependentChanged = !dependentAdded && dependentDAO.approveStatusChangeByFormId(formId);
                        break;
                    default:
                        break;
                }
                if ("DEPENDENT".equals(form.getFormTypeCode())) {
                    ok = dependentAdded || dependentChanged;
                }
            }
            request.getSession().setAttribute(ok ? "success" : "error",
                    ok ? "HR duyệt đơn thành công." : "HR duyệt đơn thất bại.");
            if (ok && dependentAdded) {
                request.getSession().setAttribute("success", "Thêm người phụ thuộc thành công");
            } else if (ok && dependentChanged) {
                request.getSession().setAttribute("success", "Cập nhật người phụ thuộc thành công");
            }
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã đơn không hợp lệ.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/manager/forms/all");
    }

    private void onHrApproveComplaint(FormRequestDTO form, EmployeeDetailDTO me, HttpServletRequest request) {
        if (!(form instanceof ComplaintFormRequestDTO)) {
            return;
        }
        ComplaintFormRequestDTO compForm = (ComplaintFormRequestDTO) form;
        if (compForm.getStartDate() == null || compForm.getStartTime() == null || compForm.getEndTime() == null) {
            LOGGER.log(Level.WARNING, "Complaint formId={0} missing date/time data, skipping attendance update.", form.getFormId());
            return;
        }
        Time timeIn = compForm.getStartTime() != null ? Time.valueOf(compForm.getStartTime().toLocalTime()) : null;
        Time timeOut = compForm.getEndTime() != null ? Time.valueOf(compForm.getEndTime().toLocalTime()) : null;

        dao.OvertimeDAO overtimeDAO = new dao.OvertimeDAO();
        boolean hasOT = overtimeDAO.hasApprovedOT(form.getEmployeeId(), compForm.getStartDate());

        Time maxTime = hasOT ? Time.valueOf("19:00:00") : Time.valueOf("17:00:00");

        if (timeOut != null && timeOut.after(maxTime)) {
            boolean otRevived = overtimeDAO.reviveAndCompleteOTForm(form.getEmployeeId(), compForm.getStartDate());
            if (otRevived && !hasOT) {
                hasOT = true;
                maxTime = Time.valueOf("19:00:00");
            }
        }

        Time calcTimeOut = timeOut;
        if (calcTimeOut != null && calcTimeOut.after(maxTime)) {
            calcTimeOut = maxTime;
        }

        // Nếu giờ vào cũng quá giới hạn (ví dụ > 17h/19h) thì giờ làm việc = 0
        long minutes = 0;
        if (timeIn != null && calcTimeOut != null && !timeIn.after(maxTime)) {
            minutes = java.time.Duration.between(
                    timeIn.toLocalTime(),
                    calcTimeOut.toLocalTime()).toMinutes();
        }
        BigDecimal hoursWorked = BigDecimal.valueOf(Math.max(0, minutes) / 60.0);

        BigDecimal standardHours = new BigDecimal("8.00");
        if (!hasOT && hoursWorked.compareTo(standardHours) > 0) {
            hoursWorked = standardHours;
        }

        LocalTime stdStart = LocalTime.of(8, 30);
        int newStatus = compForm.getStartTime().toLocalTime().isAfter(stdStart) ? 2 : 1;

        Attendance att = attendanceDAO.getAttendanceByDate(form.getEmployeeId(), compForm.getStartDate());
        if (att != null) {
            attendanceDAO.updateAttendanceWithHistory(
                    att.getAttendanceId(),
                    compForm.getStartTime(), compForm.getEndTime(),
                    hoursWorked, newStatus,
                    "Updated by complaint approval (HR step)", me.getUserId());
            LOGGER.log(Level.INFO, "Attendance updated via complaint HR approval: formId={0}, attId={1}",
                    new Object[]{form.getFormId(), att.getAttendanceId()});
        } else {
            // Không INSERT mới — chỉ báo warning
            LOGGER.log(Level.WARNING,
                    "Complaint formId={0}: no attendance record found for employeeId={1} on date={2}. Attendance NOT inserted.",
                    new Object[]{form.getFormId(), form.getEmployeeId(), compForm.getStartDate()});
            request.getSession().setAttribute("warning",
                    "Đơn khiếu nại được duyệt nhưng không tìm thấy bản ghi chấm công ngày "
                    + compForm.getStartDate() + " — không có gì được cập nhật.");
        }
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
                    ok ? "Từ chối đơn thành công." : "Từ chối đơn thất bại.");
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã đơn không hợp lệ.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/manager/forms/dept-forms");
    }

    /**
     * HR từ chối COMPLAINT bước 2 (khi đơn đang ở status = 1). Dùng
     * approveFormRequestFromStatus: WHERE status = 1 → status = 2.
     */
    private void handleHrRejectForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Chỉ nhân viên HR mới có thể thực hiện bước này.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        EmployeeDetailDTO me = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (me == null) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        String rawId = request.getParameter("formId");
        String note = request.getParameter("note");
        if (isBlank(rawId)) {
            request.getSession().setAttribute("error", "Thiếu mã đơn.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/forms/all");
            return;
        }
        try {
            int formId = Integer.parseInt(rawId);
            // Từ chối từ status 1 → status 2 (khác với reject thường cần status = 0)
            FormRequestDTO form = formRequestDAO.getFormRequestById(formId);
            int fromStatus = form != null && "DEPENDENT".equals(form.getFormTypeCode()) ? 0 : 1;
            boolean ok = formRequestDAO.approveFormRequestFromStatus(formId, fromStatus, 2, me.getEmployeeId(), note);
            if (ok && form != null && "DEPENDENT".equals(form.getFormTypeCode())
                    && !dependentDAO.rejectByFormId(formId)) {
                dependentDAO.rejectStatusChangeByFormId(formId);
            }
            request.getSession().setAttribute(ok ? "success" : "error",
                    ok ? "HR đã từ chối đơn khiếu nại thành công." : "Từ chối đơn thất bại — đơn có thể đã được xử lý rồi.");
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã đơn không hợp lệ.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/manager/forms/all");
    }

    // ========== XỬ LÝ GỬI ĐƠN THUYÊN CHUYỂN / THĂNG GIÁNG CHỨC ==========
    private void displayRequestPromotionForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        request.setAttribute("employees", employeeDAO.getAllEmployees());
        request.setAttribute("roles", roleDAO.getAllActiveRoles());
        request.setAttribute("positions", departmentDAO.getAllPositions());
        request.getRequestDispatcher("/public/manager/forms/promotion_form.jsp").forward(request, response);
    }

    /**
     * HR gửi đơn Thăng/Giáng chức cho nhân viên. Ràng buộc: targetRole phải
     * thuộc đúng phòng ban của nhân viên.
     */
    private void handleRequestPromotionDemotion(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Chỉ HR mới có quyền tạo đơn thăng/giáng chức.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        String rawEmpId = request.getParameter("employeeId");
        String rawTargetRole = request.getParameter("targetRoleId");
        String reason = request.getParameter("reason");

        if (isBlank(rawEmpId) || isBlank(rawTargetRole)) {
            request.getSession().setAttribute("error", "Vui lòng điền đầy đủ thông tin nhân viên và vai trò mới.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/forms/submit-promotion");
            return;
        }

        int empId, targetRoleId;
        try {
            empId = Integer.parseInt(rawEmpId);
            targetRoleId = Integer.parseInt(rawTargetRole);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Dữ liệu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/forms/submit-promotion");
            return;
        }

        EmployeeDetailDTO emp = employeeDAO.getEmployeeById(empId);
        if (emp == null) {
            request.getSession().setAttribute("error", "Không tìm thấy nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/forms/submit-promotion");
            return;
        }

        // Ràng buộc: targetRole phải được phép trong phòng ban hiện tại của nhân viên
        if (!departmentDAO.isRoleAllowedForDepartment(emp.getDepartmentId(), targetRoleId)) {
            request.getSession().setAttribute("error", "Vai trò mới không phù hợp với phòng ban hiện tại của nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/forms/submit-promotion");
            return;
        }

        int formTypeId = formTypeDAO.getFormTypeIdByCode("PROMOTION_DEMOTION");
        if (formTypeId <= 0) {
            request.getSession().setAttribute("error", "Loại đơn PROMOTION_DEMOTION chưa được cấu hình.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/forms/submit-promotion");
            return;
        }

        // Người tạo đơn là HR, nhưng employeeId trong đơn là nhân viên được thăng/giáng
        TransferFormRequest fr = new TransferFormRequest();
        fr.setFormCode("PRO-" + empId + "-" + System.currentTimeMillis());
        fr.setEmployeeId(empId);
        fr.setFormTypeId(formTypeId);
        fr.setReason(isBlank(reason) ? null : reason.trim());
        fr.setTargetRoleId(targetRoleId);

        int newId = formRequestDAO.addFormRequest(fr);
        if (newId > 0) {
            LOGGER.log(Level.INFO, "Promotion/Demotion request submitted: employeeId={0}, targetRoleId={1}, formId={2}",
                    new Object[]{empId, targetRoleId, newId});
            request.getSession().setAttribute("success", "Đơn thăng/giáng chức đã được gửi, chờ Business Admin phê duyệt.");
        } else {
            request.getSession().setAttribute("error", "Gửi đơn thăng/giáng chức thất bại. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
    }

    private void handleDependentStatusRequest(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        EmployeeDetailDTO me = employeeDAO.getEmployeeByUserId(user.getUserId());
        FormType ft = formTypeDAO.getByCode("DEPENDENT");
        Integer dependentId = parseIntOrNull(request.getParameter("dependentId"));
        if (me == null || ft == null || dependentId == null) {
            request.getSession().setAttribute("error", "Không thể gửi yêu cầu đổi trạng thái người phụ thuộc.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/my-profile");
            return;
        }
        if (!dependentDAO.canRequestStatusChange(dependentId, me.getEmployeeId())) {
            request.getSession().setAttribute("error", "Người phụ thuộc không hợp lệ hoặc đang có yêu cầu chờ duyệt.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/my-profile");
            return;
        }

        FormRequest fr = new FormRequest();
        fr.setFormCode("DEPENDENT-STATUS-" + me.getEmployeeId() + "-" + System.currentTimeMillis());
        fr.setEmployeeId(me.getEmployeeId());
        fr.setFormTypeId(ft.getFormTypeId());
        fr.setReason("Yêu cầu ngừng tính người phụ thuộc ID: " + dependentId);

        int formId = formRequestDAO.addFormRequest(fr);
        boolean ok = formId > 0 && dependentDAO.requestStatusChange(dependentId, me.getEmployeeId(), formId, 2);
        request.getSession().setAttribute(ok ? "success" : "error",
                ok ? "Đã gửi yêu cầu đổi trạng thái người phụ thuộc, chờ HR duyệt."
                        : "Gửi yêu cầu đổi trạng thái thất bại hoặc đang có yêu cầu chờ duyệt.");
        response.sendRedirect(request.getContextPath() + "/v1/manager/my-profile");
    }

    private void displayDependentForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        request.getSession().setAttribute("userPermissions", getPermissions(user));
        request.setAttribute("formAction", request.getContextPath() + "/v1/manager/forms/dependent/submit");
        request.setAttribute("cancelUrl", request.getContextPath() + "/v1/manager/dashboard");
        request.setAttribute("managerForm", true);
        request.getRequestDispatcher("/public/employee/forms/dependent_form.jsp").forward(request, response);
    }

    private void handleDependentFormSubmit(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        FormType ft = formTypeDAO.getByCode("DEPENDENT");
        EmployeeDetailDTO me = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (ft == null || me == null) {
            request.getSession().setAttribute("error", "Không thể tạo đơn người phụ thuộc.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/forms/dependent/new");
            return;
        }

        String fullName = trimToNull(request.getParameter("fullName"));
        String relationship = trimToNull(request.getParameter("relationship"));
        String rawDateOfBirth = trimToNull(request.getParameter("dateOfBirth"));
        String taxCode = trimToNull(request.getParameter("taxCode"));
        String note = trimToNull(request.getParameter("note"));
        java.sql.Date dateOfBirth = null;
        try {
            if (rawDateOfBirth != null) {
                dateOfBirth = java.sql.Date.valueOf(rawDateOfBirth);
            }
        } catch (IllegalArgumentException e) {
            dateOfBirth = null;
        }
        boolean invalidTaxCode = taxCode != null && !taxCode.matches("\\d+");
        if (fullName == null || relationship == null || dateOfBirth == null || invalidTaxCode) {
            request.setAttribute("error", invalidTaxCode
                    ? "Mã số thuế chỉ được nhập số."
                    : "Vui lòng nhập tên, quan hệ và ngày sinh người phụ thuộc.");
            displayDependentForm(request, response, user);
            return;
        }

        FormRequest fr = new FormRequest();
        fr.setFormCode("DEPENDENT-" + me.getEmployeeId() + "-" + System.currentTimeMillis());
        fr.setEmployeeId(me.getEmployeeId());
        fr.setFormTypeId(ft.getFormTypeId());
        fr.setReason("Tên: " + fullName + "\nQuan hệ: " + relationship
                + "\nNgày sinh: " + dateOfBirth
                + (taxCode == null ? "" : "\nMã số thuế: " + taxCode)
                + (note == null ? "" : "\nGhi chú: " + note));

        int formId = formRequestDAO.addFormRequest(fr);
        boolean ok = formId > 0 && dependentDAO.addPending(formId, me.getEmployeeId(), fullName, relationship, dateOfBirth, taxCode, note);
        request.getSession().setAttribute(ok ? "success" : "error",
                ok ? "Đã gửi đơn đăng ký người phụ thuộc, chờ HR duyệt."
                        : "Gửi đơn người phụ thuộc thất bại.");
        response.sendRedirect(request.getContextPath() + "/v1/manager/forms/all");
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

            // Phân trang
            List<OvertimeRequestDTO> pagedRequests = utils.Paging.page(request, requests);

            request.setAttribute("otRequests", pagedRequests);
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
                request.getSession().setAttribute("error", "Vui lòng điền đầy đủ thông tin và chọn ít nhất 1 nhân viên.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/forms/create-ot");
                return;
            }

            List<String> busyEmployees = overtimeDAO.getBusyEmployeeNamesForOT(assigneeIds, otDate);
            if (!busyEmployees.isEmpty()) {
                request.getSession().setAttribute("error", "Các nhân viên sau đã có lịch OT (Đang chờ/Đã duyệt/Hoàn thành) vào ngày này: " + String.join(", ", busyEmployees));
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
                java.time.LocalDate today = java.time.LocalDate.now();

                if (!date.isAfter(today)) {
                    request.getSession().setAttribute("error", "Chỉ được phép đăng ký OT cho các ngày tiếp theo (không được đăng ký cho ngày hiện tại hoặc quá khứ).");
                    response.sendRedirect(request.getContextPath() + "/v1/manager/forms/create-ot");
                    return;
                }

                java.time.DayOfWeek dow = date.getDayOfWeek();
                boolean isWeekend = (dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY);

                if (isWeekend) {
                    request.getSession().setAttribute("error", "Không được phép đăng ký tăng ca vào Thứ Bảy và Chủ Nhật.");
                    response.sendRedirect(request.getContextPath() + "/v1/manager/forms/create-ot");
                    return;
                }

                java.time.LocalTime minTime = java.time.LocalTime.of(17, 0);
                java.time.LocalTime maxTime = java.time.LocalTime.of(19, 0);
                if (start.isBefore(minTime) || end.isAfter(maxTime)) {
                    request.getSession().setAttribute("error", "Nhân viên chỉ được phép tăng ca trong khung giờ 17:00 đến 19:00.");
                    response.sendRedirect(request.getContextPath() + "/v1/manager/forms/create-ot");
                    return;
                }
            } catch (Exception e) {
                request.getSession().setAttribute("error", "Định dạng ngày/giờ không hợp lệ.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/forms/create-ot");
                return;
            }

            // Khóa cố định Loại ngày là "Ngày thường" (dayType = 1)
            int dayType = 1;

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
        java.time.YearMonth latestClosedPeriod = java.time.YearMonth.now().minusMonths(1);
        Integer year = parseIntOrNull(request.getParameter("year"));
        Integer month = parseIntOrNull(request.getParameter("month"));
        if (year == null || year < 2000 || month == null || month < 1 || month > 12) {
            return new int[]{latestClosedPeriod.getYear(), latestClosedPeriod.getMonthValue()};
        }
        java.time.YearMonth selected = java.time.YearMonth.of(year, month);
        if (selected.isAfter(latestClosedPeriod)) {
            selected = latestClosedPeriod;
        }
        return new int[]{selected.getYear(), selected.getMonthValue()};
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

    private void downloadBlankTemplate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String type = request.getParameter("type");
        if (type == null || type.isEmpty()) {
            type = "fixed_term";
        }
        String templateName = "contract_" + type + ".html";
        String templatePath = getServletContext().getRealPath("/templates/" + templateName);
        try {
            new ContractPdfService().generateBlankPdf(templatePath, response);
        } catch (DocumentException e) {
            throw new IOException("Blank PDF generation failed", e);
        }
    }

    private void downloadSignedContract(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendError(400, "Missing contract id");
            return;
        }

        int contractId = Integer.parseInt(idParam);
        EmploymentContract contract = contractDAO.getContractById(contractId);
        if (contract == null || contract.getContractFilePath() == null) {
            response.sendError(404, "Signed contract not found");
            return;
        }

        String uploadBase = getServletContext().getRealPath("/") + "../../uploads/";
        java.io.File file = new java.io.File(uploadBase + contract.getContractFilePath());

        if (!file.exists()) {
            response.sendError(404, "File not found on server");
            return;
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "inline; filename=\"" + (contract.getContractFileName() != null ? contract.getContractFileName() : "contract.pdf") + "\"");

        java.nio.file.Files.copy(file.toPath(), response.getOutputStream());
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

    private boolean isHrManager(User user) {
        String role = roleDAO.getRoleByUserId(user.getUserId());
        return "HRManager".equals(role);
    }

    private void setPermissionFlags(HttpServletRequest request, Set<String> perms) {
        User sessionUser = (User) request.getSession().getAttribute("user");
        boolean isHrMgr = isHrManager(sessionUser);
        request.setAttribute("canViewEmployees", perms.contains("VIEW_EMPLOYEES"));
        request.setAttribute("canAddEmployee", perms.contains("ADD_EMPLOYEE"));
        request.setAttribute("canAddEmploymentContract", perms.contains("ADD_EMPLOYMENT_CONTRACT"));
        request.setAttribute("canEditEmployee", perms.contains("EDIT_EMPLOYEE"));
        request.setAttribute("canDeleteEmployee", perms.contains("DELETE_EMPLOYEE"));
        request.setAttribute("canViewDepartments", perms.contains("VIEW_DEPARTMENTS"));
        request.setAttribute("canEditDepts", perms.contains("EDIT_DEPARTMENTS"));
        request.setAttribute("canAssignDept", perms.contains("ASSIGN_DEPARTMENT"));
        request.setAttribute("canReassignDept", perms.contains("REASSIGN_DEPARTMENT"));
        request.setAttribute("canViewOwnSalary", true);
        request.setAttribute("canViewAllSalary", perms.contains("VIEW_ALL_SALARY"));
        request.setAttribute("canExportPayroll", perms.contains("EXPORT_PAYROLL"));
        request.setAttribute("canViewOwnContract", perms.contains("VIEW_OWN_CONTRACT"));
        request.setAttribute("canViewAllContracts", perms.contains("VIEW_ALL_CONTRACTS"));
        request.setAttribute("canViewPendingContracts", perms.contains("VIEW_PENDING_CONTRACTS"));
        request.setAttribute("canApproveContract", isHrMgr);
        request.setAttribute("canRejectContract", isHrMgr);
        request.setAttribute("canTerminateContract", isHrMgr);
    }

    private void sendJsonResponse(HttpServletResponse response, boolean success, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"success\":" + success + ",\"message\":\"" + message + "\"}");
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String xRequestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(xRequestedWith);
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
