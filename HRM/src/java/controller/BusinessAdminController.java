package controller;

import dao.DepartmentDAO;
import dao.DependentDAO;
import dao.EmployeeDAO;
import dao.PermissionDAO;
import dao.RoleDAO;
import dao.UserDAO;
import dto.EmployeeDetailDTO;
import dto.FormRequestDTO;
import dto.TransferRequestDTO;
import dao.FormRequestDAO;
import dao.OvertimeDAO;
import dto.OvertimeRequestDTO;
import dao.PayrollConfigDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import model.Attendance;
import model.Department;
import model.PayrollSetting;
import model.Role;
import model.User;
import model.PayrollConfigChangeRequest;
import static org.apache.tomcat.jakartaee.commons.lang3.StringUtils.isBlank;
import service.EmailService;
import service.PayrollConfigWorkflowService;
import utils.Paging;
import dao.EmploymentContractDAO;
import dal.DBContext;
import dao.PayrollDAO;
import java.time.YearMonth;
import java.util.LinkedHashMap;

public class BusinessAdminController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(BusinessAdminController.class.getName());
    private static final UserDAO userDAO = new UserDAO();
    private static final RoleDAO roleDAO = new RoleDAO();
    private static final EmployeeDAO employeeDAO = new EmployeeDAO();
    private static final DepartmentDAO departmentDAO = new DepartmentDAO();
    private static final DependentDAO dependentDAO = new DependentDAO();
    private static final FormRequestDAO formRequestDAO = new FormRequestDAO();
    private static final OvertimeDAO overtimeDAO = new OvertimeDAO();
    private static final PayrollDAO payrollDAO = new PayrollDAO();
    private static final PayrollConfigDAO payrollConfigDAO = new PayrollConfigDAO();
    private static final PayrollConfigWorkflowService payrollConfigWorkflowService = new PayrollConfigWorkflowService();
    private static final service.AttendanceService attendanceService = new service.AttendanceService();
    private static final service.AttendanceClosingService attendanceClosingService = new service.AttendanceClosingService();
    private static final service.PayrollService payrollService = new service.PayrollService();
    private static final utils.AttendanceExcelExporter attendanceExporter = new utils.AttendanceExcelExporter();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        preventBackCache(response);
        String action = request.getPathInfo();

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (action == null || action.equals("/")) {
            displayDashboard(request, response);
            return;
        }
        switch (action) {
            case "/dashboard":
                displayDashboard(request, response);
                break;
            case "/my-profile":
                displayMyProfile(request, response);
                break;
            case "/department":
                displayDepartmentList(request, response);
                break;
            case "/department/assign":
                displayAssignPage(request, response, user);
                break;
            case "/department/employees":
                displayDepartmentEmployees(request, response);
                break;
            case "/employee-list":
                displayEmployeeList(request, response, user);
                break;
            case "/employee-detail":
                displayEmployeeDetail(request, response);
                break;
            case "/assign-department":
                displayAssignDepartmentForm(request, response, user);
                break;
            case "/add-department":
                displayAddDepartmentForm(request, response);
                break;
            case "/update-department":
                displayUpdateDepartmentForm(request, response);
                break;
            case "/salary/all":
                displayAllSalaryForBa(request, response, user);
                break;
            case "/salary/detail":
                displaySalaryDetailForBa(request, response, user);
                break;
            case "/salary/export":
                exportSalaryForBa(request, response, user);
                break;
            case "/payroll-config":
                displayPayrollConfig(request, response);
                break;
            case "/payroll-config/history":
                displayPayrollConfigHistory(request, response);
                break;
            case "/attendance/overview":
                response.sendRedirect(request.getContextPath() + "/v1/businessadmin/attendance/closing");
                break;
            case "/attendance/closing":
                displayAttendanceClosing(request, response);
                break;
            case "/attendance/detail":
                displayAttendanceDetail(request, response);
                break;
            case "/attendance/export":
                exportAttendanceReport(request, response);
                break;
            case "/forms":
                displayFormRequests(request, response);
                break;
            case "/forms/ot-detail":
                displayOTDetail(request, response);
                break;
            case "/forms/transfer-detail":
                displayTransferDetail(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        preventBackCache(response);
        String action = request.getPathInfo();

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (action == null || action.equals("/")) {
            displayDashboard(request, response);
            return;
        }
        switch (action) {
            case "/my-profile":
                handleUpdateMyProfile(request, response, user);
                break;
            case "/department/assign":
                handleAssignManager(request, response, user);
                break;
            case "/department/unassign":
                handleUnassignManager(request, response);
                break;
            case "/assign-department":
                handleAssignDepartment(request, response, user);
                break;
            case "/add-department":
                handleAddDepartment(request, response, user);
                break;
            case "/update-department":
                handleUpdateDepartment(request, response);
                break;
            case "/payroll-config/setting/save":
                handleSavePayrollSetting(request, response);
                break;
            case "/payroll-config/deduction/save":
                handleSavePayrollDeduction(request, response);
                break;
            case "/payroll-config/deduction/delete":
                handleDeletePayrollDeduction(request, response);
                break;
            case "/payroll-config/tax/save":
                handleSavePayrollTaxBracket(request, response);
                break;
            case "/payroll-config/allowance/save":
                handleSavePayrollAllowance(request, response);
                break;
            case "/payroll-config/allowance/delete":
                handleDeletePayrollAllowance(request, response);
                break;
            case "/payroll-config/request/approve":
                handleApprovePayrollConfigRequest(request, response, user);
                break;
            case "/payroll-config/request/reject":
                handleRejectPayrollConfigRequest(request, response, user);
                break;
            case "/forms/approve":
                handleApproveForm(request, response, user);
                break;
            case "/forms/reject":
                handleRejectForm(request, response, user);
                break;
            case "/salary/finalize":
                handleFinalizePayroll(request, response, user);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/");
                break;
        }
    }


    private void preventBackCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    private void displayDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ── Thống kê cơ bản ──
        request.setAttribute("deptSize", departmentDAO.getAllActiveDepartments().size());
        request.setAttribute("employeeSize", employeeDAO.getAllEmployees().size());

        // ── Năm được chọn ──
        int currentYear = java.time.LocalDate.now().getYear();
        int salaryYear = currentYear;
        String yearParam = request.getParameter("salaryYear");
        if (yearParam != null && !yearParam.isBlank()) {
            try {
                salaryYear = Integer.parseInt(yearParam.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        // ── Lấy data qua DAO ──
        long[] monthlyCosts = payrollDAO.getMonthlySalaryCosts(salaryYear);

        // ── Build JSON cho Chart.js ──
        java.time.YearMonth now = java.time.YearMonth.now();
        String[] monthNames = {"", "Th1", "Th2", "Th3", "Th4", "Th5", "Th6",
            "Th7", "Th8", "Th9", "Th10", "Th11", "Th12"};
        StringBuilder labelsJson = new StringBuilder("[");
        StringBuilder dataJson = new StringBuilder("[");
        long yearTotal = 0L;

        for (int m = 1; m <= 12; m++) {
            labelsJson.append("\"").append(monthNames[m]).append("\"");

            boolean isFuture = java.time.YearMonth.of(salaryYear, m).isAfter(now);
            if (isFuture) {
                dataJson.append("null");
            } else {
                long valMillion = Math.round(monthlyCosts[m] / 1_000_000.0);
                dataJson.append(valMillion);
                yearTotal += valMillion;
            }

            if (m < 12) {
                labelsJson.append(",");
                dataJson.append(",");
            }
        }
        labelsJson.append("]");
        dataJson.append("]");

        // ── Tháng gần nhất ──
        java.time.YearMonth latest = now.minusMonths(1);
        long latestCount = payrollDAO.countPayrollsInMonth(
                latest.getYear(), latest.getMonthValue());

        //tất cả năm
        List<Integer> availableYears = payrollDAO.getAvailableSalaryYears();

        // ── Chi phí lương theo phòng ban (kỳ được chọn, mặc định tháng gần nhất) ──
        YearMonth deptPeriod = latest;
        String deptYearParam = request.getParameter("deptYear");
        String deptMonthParam = request.getParameter("deptMonth");
        if (deptYearParam != null && !deptYearParam.isBlank()
                && deptMonthParam != null && !deptMonthParam.isBlank()) {
            try {
                deptPeriod = YearMonth.of(Integer.parseInt(deptYearParam.trim()), Integer.parseInt(deptMonthParam.trim()));
            } catch (Exception ignored) {
            }
        }
        LinkedHashMap<String, Long> deptCosts = payrollDAO.getDepartmentSalaryCosts(
                deptPeriod.getYear(), deptPeriod.getMonthValue());
        StringBuilder deptLabelsJson = new StringBuilder("[");
        StringBuilder deptDataJson = new StringBuilder("[");
        boolean firstDept = true;
        for (Map.Entry<String, Long> entry : deptCosts.entrySet()) {
            if (!firstDept) {
                deptLabelsJson.append(",");
                deptDataJson.append(",");
            }
            deptLabelsJson.append("\"").append(entry.getKey().replace("\"", "'")).append("\"");
            deptDataJson.append(Math.round(entry.getValue() / 1_000_000.0));
            firstDept = false;
        }
        deptLabelsJson.append("]");
        deptDataJson.append("]");

        // ── Bảng lương chờ duyệt (kỳ gần nhất) ──
        java.sql.Date pendingPeriodStart = java.sql.Date.valueOf(latest.atDay(1));
        java.sql.Date pendingPeriodEnd = java.sql.Date.valueOf(latest.atEndOfMonth());
        long pendingPayrollCount = payrollDAO.countPendingApproval(pendingPeriodStart, pendingPeriodEnd, null, null);

        // ── Đơn chờ duyệt (đơn từ + cấu hình lương) ──
        List<FormRequestDTO> pendingForms = formRequestDAO.getAllFormRequests(null, null, null, null)
                .stream()
                .filter(f -> "OVERTIME".equals(f.getFormTypeCode())
                        || "TRANSFER".equals(f.getFormTypeCode())
                        || "PROMOTION_DEMOTION".equals(f.getFormTypeCode()))
                .filter(f -> f.getStatus() == 0)
                .collect(java.util.stream.Collectors.toList());
        long pendingOtCount = pendingForms.stream()
                .filter(f -> "OVERTIME".equals(f.getFormTypeCode())).count();
        long pendingOtherFormsCount = pendingForms.size() - pendingOtCount;
        long pendingPayrollConfigCount = payrollConfigWorkflowService.getPendingRequests().size();
        long pendingTotalCount = pendingForms.size() + pendingPayrollConfigCount;

        request.setAttribute("salaryLabelsJson", labelsJson.toString());
        request.setAttribute("salaryDataJson", dataJson.toString());
        request.setAttribute("salaryYear", salaryYear);
        request.setAttribute("availableYears", availableYears);
        request.setAttribute("currentYear", currentYear);
        request.setAttribute("salaryYearTotal", yearTotal);
        request.setAttribute("latestPaidCount", latestCount);
        request.setAttribute("latestPayMonth",
                String.format("%02d/%d", latest.getMonthValue(), latest.getYear()));
        request.setAttribute("deptLabelsJson", deptLabelsJson.toString());
        request.setAttribute("deptDataJson", deptDataJson.toString());
        request.setAttribute("deptYear", deptPeriod.getYear());
        request.setAttribute("deptMonth", deptPeriod.getMonthValue());
        request.setAttribute("pendingTotalCount", pendingTotalCount);
        request.setAttribute("pendingOtCount", pendingOtCount);
        request.setAttribute("pendingOtherFormsCount", pendingOtherFormsCount);
        request.setAttribute("pendingPayrollConfigCount", pendingPayrollConfigCount);
        request.setAttribute("pendingPayrollCount", pendingPayrollCount);

        request.getRequestDispatcher("/public/businessadmin/dashboard.jsp")
                .forward(request, response);
    }

    private void displayMyProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User sessionUser = (session != null) ? (User) session.getAttribute("user") : null;
        if (sessionUser == null) {
            response.sendRedirect(request.getContextPath() + "/v1/auth/login?required=1");
            return;
        }
        User currentUser = userDAO.getUserById(sessionUser.getUserId());
        request.setAttribute("currentUser", currentUser);
        request.getRequestDispatcher("/public/businessadmin/employee/my_profile.jsp").forward(request, response);
    }

    private void handleUpdateMyProfile(HttpServletRequest request, HttpServletResponse response, User sessionUser)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String fullName = request.getParameter("fullName");
        String dob = request.getParameter("dob");
        String address = request.getParameter("address");

        if (isBlank(username) || isBlank(fullName)) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ tên đăng nhập và họ tên");
            request.setAttribute("currentUser", userDAO.getUserById(sessionUser.getUserId()));
            request.getRequestDispatcher("/public/businessadmin/employee/my_profile.jsp").forward(request, response);
            return;
        }

        username = username.trim();
        fullName = fullName.trim();
        dob = isBlank(dob) ? null : dob.trim();
        address = isBlank(address) ? null : address.trim();

        if (userDAO.isUsernameExists(username, sessionUser.getUserId())) {
            request.setAttribute("error", "Tên đăng nhập đã tồn tại");
            request.setAttribute("currentUser", userDAO.getUserById(sessionUser.getUserId()));
            request.getRequestDispatcher("/public/businessadmin/employee/my_profile.jsp").forward(request, response);
            return;
        }

        boolean updated = userDAO.updateMyProfile(sessionUser.getUserId(), username, fullName, dob, address);
        if (!updated) {
            request.setAttribute("error", "Cập nhật hồ sơ thất bại. Vui lòng thử lại");
            request.setAttribute("currentUser", userDAO.getUserById(sessionUser.getUserId()));
            request.getRequestDispatcher("/public/businessadmin/employee/my_profile.jsp").forward(request, response);
            return;
        }

        User updatedUser = userDAO.getUserById(sessionUser.getUserId());
        request.getSession().setAttribute("user", updatedUser);
        request.getSession().setAttribute("success", "Cập nhật hồ sơ thành công");
        response.sendRedirect(request.getContextPath() + "/v1/businessadmin/my-profile");
    }

    private void displayDepartmentList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Department> departments = departmentDAO.getAllDepartments();

        // Build manager map: departmentId -> EmployeeDetailDTO
        Map<Integer, EmployeeDetailDTO> managerMap = new HashMap<>();
        for (Department d : departments) {
            if (d.getManagerId() != null) {
                EmployeeDetailDTO mgr = departmentDAO.getCurrentManager(d.getDepartmentId());
                if (mgr != null) {
                    managerMap.put(d.getDepartmentId(), mgr);
                }
            }
        }

        request.setAttribute("departments", departments);
        request.setAttribute("managerMap", managerMap);

        // Flash messages set by redirect from assign/unassign
        HttpSession session = request.getSession(false);
        if (session != null) {
            String success = (String) session.getAttribute("deptSuccess");
            String error = (String) session.getAttribute("deptError");
            if (success != null) {
                request.setAttribute("success", success);
                session.removeAttribute("deptSuccess");
            }
            if (error != null) {
                request.setAttribute("error", error);
                session.removeAttribute("deptError");
            }
        }

        request.getRequestDispatcher("/public/businessadmin/department/department_list.jsp").forward(request, response);
    }

    private void displayAssignPage(HttpServletRequest request, HttpServletResponse response, User ba)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department");
            return;
        }
        int departmentId;
        try {
            departmentId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department");
            return;
        }

        Department dept = departmentDAO.getDepartmentById(departmentId);
        if (dept == null || dept.getStatus() == 0) {
            request.getSession().setAttribute("deptError", "Phòng ban không tồn tại hoặc đã bị vô hiệu hóa.");
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department");
            return;
        }

        EmployeeDetailDTO currentManager = departmentDAO.getCurrentManager(departmentId);
        List<EmployeeDetailDTO> candidates = departmentDAO.getAssignableManagerDTOs(ba.getUserId(), departmentId);

        request.setAttribute("dept", dept);
        request.setAttribute("currentManager", currentManager);
        request.setAttribute("candidates", candidates);

        request.getRequestDispatcher("/public/businessadmin/department/department_assign.jsp").forward(request, response);
    }

    private void handleAssignManager(HttpServletRequest request, HttpServletResponse response, User ba)
            throws IOException {
        String deptIdParam = request.getParameter("departmentId");
        String empIdParam = request.getParameter("employeeId");

        if (isBlank(deptIdParam) || isBlank(empIdParam)) {
            request.getSession().setAttribute("deptError", "Dữ liệu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department");
            return;
        }

        int departmentId, employeeId;
        try {
            departmentId = Integer.parseInt(deptIdParam.trim());
            employeeId = Integer.parseInt(empIdParam.trim());
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("deptError", "Dữ liệu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department");
            return;
        }

        Department dept = departmentDAO.getDepartmentById(departmentId);
        if (dept == null || dept.getStatus() == 0) {
            request.getSession().setAttribute("deptError", "Phòng ban không tồn tại.");
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department");
            return;
        }

        List<EmployeeDetailDTO> candidates = departmentDAO.getAssignableManagerDTOs(ba.getUserId(), departmentId);
        boolean isValid = candidates.stream().anyMatch(c -> c.getEmployeeId() == employeeId);
        if (!isValid) {
            request.getSession().setAttribute("deptError", "Nhân viên này không đủ điều kiện làm quản lý.");
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department/assign?id=" + departmentId);
            return;
        }

        boolean ok = departmentDAO.assignManager(departmentId, employeeId);
        if (ok) {
            request.getSession().setAttribute("deptSuccess",
                    "Assign manager thành công cho phòng ban " + dept.getDepartmentName() + ".");
        } else {
            request.getSession().setAttribute("deptError", "Assign thất bại. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department");
    }

    private void handleUnassignManager(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String deptIdParam = request.getParameter("departmentId");

        if (isBlank(deptIdParam)) {
            request.getSession().setAttribute("deptError", "Dữ liệu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department");
            return;
        }

        int departmentId;
        try {
            departmentId = Integer.parseInt(deptIdParam.trim());
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("deptError", "Dữ liệu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department");
            return;
        }

        Department dept = departmentDAO.getDepartmentById(departmentId);
        if (dept == null) {
            request.getSession().setAttribute("deptError", "Phòng ban không tồn tại.");
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department");
            return;
        }

        boolean ok = departmentDAO.unassignManager(departmentId);
        if (ok) {
            request.getSession().setAttribute("deptSuccess",
                    "Đã gỡ manager khỏi phòng ban " + dept.getDepartmentName() + ".");
        } else {
            request.getSession().setAttribute("deptError", "Gỡ manager thất bại. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department");
    }

    private void displayEmployeeList(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        List<EmployeeDetailDTO> employees = employeeDAO.getAllEmployees();
        request.setAttribute("employees", employees);
        request.getRequestDispatcher("/public/businessadmin/employee/employee_list.jsp").forward(request, response);
    }

    private void displayEmployeeDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (isBlank(idParam)) {
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/employee-list");
            return;
        }
        int employeeId;
        try {
            employeeId = Integer.parseInt(idParam.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/employee-list");
            return;
        }
        EmployeeDetailDTO employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null) {
            request.getSession().setAttribute("error", "Không tìm thấy nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/employee-list");
            return;
        }
        request.setAttribute("employee", employee);
        request.setAttribute("dependents", dependentDAO.getActiveByEmployeeId(employee.getEmployeeId()));
        request.getRequestDispatcher("/public/businessadmin/employee/employee_detail.jsp").forward(request, response);
    }

    private void displayDepartmentEmployees(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (isBlank(idParam)) {
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department");
            return;
        }
        int departmentId;
        try {
            departmentId = Integer.parseInt(idParam.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department");
            return;
        }
        Department department = departmentDAO.getDepartmentById(departmentId);
        if (department == null) {
            request.getSession().setAttribute("deptError", "Không tìm thấy phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department");
            return;
        }
        List<EmployeeDetailDTO> employees = employeeDAO.getEmployeesByDepartmentId(departmentId);
        request.setAttribute("department", department);
        request.setAttribute("employees", employees);
        request.getRequestDispatcher("/public/businessadmin/department/department_employees.jsp").forward(request, response);
    }

    private void displayAssignDepartmentForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        request.setAttribute("availableEmployees", employeeDAO.getEmployees(user.getUserId()));
        request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
        request.setAttribute("positions", departmentDAO.getAllPositions());
        request.getRequestDispatcher("/public/businessadmin/department/assign_department.jsp").forward(request, response);
    }

    private void handleAssignDepartment(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String rawUserId = request.getParameter("userId");
        String rawDepartmentId = request.getParameter("departmentId");
        String rawPositionId = request.getParameter("positionId");
        String phoneNumber = request.getParameter("phoneNumber");
        String skills = request.getParameter("skills");
        String experience = request.getParameter("experience");
        String degree = request.getParameter("degree");

        if (isBlank(rawUserId) || isBlank(rawDepartmentId) || isBlank(rawPositionId)) {
            repopulateAssignForm(request, response, user, "Vui lòng chọn đầy đủ nhân viên, phòng ban và vị trí.");
            return;
        }

        int userId, departmentId, positionId;
        try {
            userId = Integer.parseInt(rawUserId);
            departmentId = Integer.parseInt(rawDepartmentId);
            positionId = Integer.parseInt(rawPositionId);
        } catch (NumberFormatException e) {
            repopulateAssignForm(request, response, user, "Dữ liệu không hợp lệ.");
            return;
        }

        if (employeeDAO.isUserAssignedToDepartment(userId)) {
            repopulateAssignForm(request, response, user, "Người dùng này đã được phân công phòng ban rồi.");
            return;
        }

        int userRoleId = userDAO.getRoleIdByUserId(userId);
        if (!departmentDAO.isRoleAllowedForDepartment(departmentId, userRoleId)) {
            Department dept = departmentDAO.getDepartmentById(departmentId);
            String deptName = (dept != null) ? dept.getDepartmentName() : "phòng ban này";
            List<String> allowed = departmentDAO.getAllowedRoleNames(departmentId);
            String msg = "Vai trò hiện tại của nhân viên không phù hợp với phòng \"" + deptName + "\". "
                    + "Phòng này chỉ nhận vai trò: " + String.join(", ", allowed) + ". "
                    + "Vui lòng đổi vai trò của người dùng trước khi phân công.";
            repopulateAssignForm(request, response, user, msg);
            return;
        }

        boolean success = employeeDAO.assignEmployeeToDepartment(
                userId, departmentId, positionId,
                isBlank(phoneNumber) ? null : phoneNumber.trim(),
                isBlank(skills) ? null : skills.trim(),
                isBlank(experience) ? null : experience.trim(),
                isBlank(degree) ? null : degree.trim());

        if (!success) {
            repopulateAssignForm(request, response, user, "Phân công thất bại. Vui lòng thử lại.");
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
                // Phòng đã có manager -> người mới (kể cả role manager) làm cấp dưới
                // của manager hiện tại, không ghi đè manager.
                employeeDAO.setEmployeeManager(assigned.getEmployeeId(), assignedDept.getManagerId());
            }
        }

        request.getSession().setAttribute("success", "Phân công nhân viên vào phòng ban thành công.");
        response.sendRedirect(request.getContextPath() + "/v1/businessadmin/employee-list");
    }

    private void repopulateAssignForm(HttpServletRequest request, HttpServletResponse response,
            User user, String errorMsg) throws ServletException, IOException {
        request.setAttribute("error", errorMsg);
        request.setAttribute("availableEmployees", employeeDAO.getEmployees(user.getUserId()));
        request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
        request.setAttribute("positions", departmentDAO.getAllPositions());
        request.getRequestDispatcher("/public/businessadmin/department/assign_department.jsp").forward(request, response);
    }

    // =========================================================
    // Add / Update department (kế thừa từ Employee)
    // =========================================================
    private void displayAddDepartmentForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("roles", roleDAO.getAllActiveRoles());
        request.getRequestDispatcher("/public/businessadmin/department/add_department.jsp").forward(request, response);
    }

    private void handleAddDepartment(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
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
            request.getRequestDispatcher("/public/businessadmin/department/add_department.jsp").forward(request, response);
            return;
        }

        Department dept = new Department();
        dept.setDepartmentCode(code.trim());
        dept.setDepartmentName(name.trim());
        dept.setDescription(isBlank(description) ? null : description.trim());

        int newDeptId = departmentDAO.addDepartment(dept);
        if (newDeptId <= 0) {
            request.setAttribute("error", "Thêm phòng ban thất bại. Vui lòng thử lại.");
            request.setAttribute("input_code", code);
            request.setAttribute("input_name", name);
            request.setAttribute("input_description", description);
            request.setAttribute("roles", roleDAO.getAllActiveRoles());
            request.setAttribute("selectedRoleIds", roleIds);
            request.getRequestDispatcher("/public/businessadmin/department/add_department.jsp").forward(request, response);
            return;
        }

        if (!roleIds.isEmpty()) {
            departmentDAO.replaceDepartmentRoles(newDeptId, roleIds);
        }

        request.getSession().setAttribute("deptSuccess", "Thêm phòng ban \"" + name.trim() + "\" thành công.");
        response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department");
    }

    private void displayUpdateDepartmentForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (isBlank(idParam)) {
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department");
            return;
        }
        int deptId;
        try {
            deptId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department");
            return;
        }
        Department dept = departmentDAO.getDepartmentById(deptId);
        if (dept == null) {
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department");
            return;
        }

        request.setAttribute("department", dept);
        List<Role> activeRoles = roleDAO.getAllActiveRoles();
        request.setAttribute("roles", activeRoles);
        List<String> allowedRoles = departmentDAO.getAllowedRoleNames(deptId);
        List<Integer> selectedRoleIds = new ArrayList<>();
        for (Role r : activeRoles) {
            if (allowedRoles.contains(r.getRoleName())) {
                selectedRoleIds.add(r.getRoleId());
            }
        }
        request.setAttribute("selectedRoleIds", selectedRoleIds);
        request.getRequestDispatcher("/public/businessadmin/department/update_department.jsp").forward(request, response);
    }

    private void handleUpdateDepartment(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("departmentId");
        String name = request.getParameter("departmentName");
        String description = request.getParameter("description");
        List<Integer> roleIds = parseRoleIds(request.getParameterValues("roleIds"));

        if (isBlank(idParam) || isBlank(name)) {
            request.getSession().setAttribute("error", "Tên phòng ban là bắt buộc.");
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/update-department?id="
                    + (idParam != null ? idParam : ""));
            return;
        }

        int deptId;
        try {
            deptId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department");
            return;
        }

        Department dept = departmentDAO.getDepartmentById(deptId);
        if (dept == null) {
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department");
            return;
        }

        dept.setDepartmentName(name.trim());
        dept.setDescription(isBlank(description) ? null : description.trim());

        boolean success = departmentDAO.updateDepartmentInfo(dept);
        if (success) {
            departmentDAO.replaceDepartmentRoles(deptId, roleIds);
            request.getSession().setAttribute("deptSuccess", "Cập nhật phòng ban thành công.");
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/department");
        } else {
            request.getSession().setAttribute("error", "Cập nhật thất bại. Vui lòng thử lại.");
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/update-department?id=" + deptId);
        }
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

    // ===================== Attendance Dashboard (Overview / Detail / Export) =====================
    /**
     * HR/Business Admin: 0 hoặc rỗng = toàn công ty; ngược lại lọc theo phòng
     * ban.
     */
    private Integer resolveDepartmentFilter(HttpServletRequest request) {
        String raw = request.getParameter("departmentId");
        if (raw != null && !raw.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(raw.trim());
                return id > 0 ? id : null;
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
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

    private void displayAttendanceClosing(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer departmentId = resolveDepartmentFilter(request);
        java.time.LocalDate prev = java.time.LocalDate.now().minusMonths(1);
        int month = paramOr(request, "month", prev.getMonthValue());
        int year = paramOr(request, "year", prev.getYear());

        // Cùng dữ liệu tổng hợp như trang overview của employee.
        java.util.List<dto.AttendanceSummaryDTO> summaries
                = attendanceService.getMonthlySummaries(departmentId, month, year);
        request.setAttribute("summaries", summaries);
        request.setAttribute("pagedSummaries", utils.Paging.page(request, summaries));
        request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
        request.setAttribute("canViewAll", true);
        request.setAttribute("selectedDepartmentId", departmentId);
        if (departmentId != null) {
            model.Department dept = departmentDAO.getDepartmentById(departmentId);
            request.setAttribute("departmentName", dept != null ? dept.getDepartmentName() : "");
        }
        request.setAttribute("selectedMonth", month);
        request.setAttribute("selectedYear", year);

        // Trạng thái chốt của toàn bộ phòng ban trong kỳ (bỏ qua bộ lọc phòng).
        java.util.List<model.AttendancePeriod> closingPeriods
                = attendanceClosingService.getClosingOverview(year, month);
        boolean hasData = !closingPeriods.isEmpty();
        boolean allLocked = hasData;
        for (model.AttendancePeriod p : closingPeriods) {
            if (p.getStatus() != 4) {
                allLocked = false;
            }
        }
        request.setAttribute("closingPeriods", closingPeriods);
        request.setAttribute("closingHasData", hasData);
        request.setAttribute("closingLocked", allLocked);
        setBusinessAdminAttendanceLayout(request);
        request.getRequestDispatcher("/public/businessadmin/attendance/attendance_closing.jsp").forward(request, response);
    }


    private void setBusinessAdminAttendanceLayout(HttpServletRequest request) {
        request.setAttribute("sidebarPath", "/public/components/businessAdminSideBar.jsp");
        request.setAttribute("topbarPath", "/public/components/businessAdminTopBar.jsp");
        request.setAttribute("baseUrl", request.getContextPath() + "/v1/businessadmin/attendance");
    }

    private void displayAttendanceDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer departmentId = resolveDepartmentFilter(request);
        int employeeId = paramOr(request, "employeeId", -1);
        java.time.LocalDate now = java.time.LocalDate.now();
        int month = paramOr(request, "month", now.getMonthValue());
        int year = paramOr(request, "year", now.getYear());

        dto.AttendanceDetailDTO detail = (employeeId > 0)
                ? attendanceService.getEmployeeDetail(employeeId, departmentId, month, year)
                : null;
        setBusinessAdminAttendanceLayout(request);
        request.setAttribute("canViewAll", true);
        if (detail == null) {
            request.setAttribute("error", "Không tìm thấy dữ liệu chấm công của nhân viên.");
            request.getRequestDispatcher("/public/businessadmin/attendance/attendance_detail.jsp").forward(request, response);
            return;
        }
        java.util.List<Integer> approvedOTDays
                = overtimeDAO.getApprovedOTDaysInMonth(employeeId, month, year);
        request.setAttribute("approvedOTDays", approvedOTDays);

        int day = paramOr(request, "day", 0);
        List<Attendance> filtered = detail.getDailyRows();
        if (day >= 1 && day <= 31) {
            filtered = new ArrayList<>();
            for (model.Attendance a : detail.getDailyRows()) {
                if (a.getWorkDate() != null && a.getWorkDate().toLocalDate().getDayOfMonth() == day) {
                    filtered.add(a);
                }
            }
        }
        request.setAttribute("selectedDay", day);
        request.setAttribute("pagedRows", utils.Paging.page(request, filtered));
        request.setAttribute("detail", detail);
        request.setAttribute("selectedDepartmentId", departmentId);
        request.setAttribute("selectedMonth", month);
        request.setAttribute("selectedYear", year);
        request.getRequestDispatcher("/public/businessadmin/attendance/attendance_detail.jsp").forward(request, response);
    }

    private void exportAttendanceReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer departmentId = resolveDepartmentFilter(request);
        java.time.LocalDate now = java.time.LocalDate.now();
        int month = paramOr(request, "month", now.getMonthValue());
        int year = paramOr(request, "year", now.getYear());

        dto.AttendanceReportDTO report = attendanceService.getReport(departmentId, month, year);
        String scope = (departmentId == null) ? "company" : "dept" + departmentId;
        String fileName = "attendance_" + scope + "_" + year + "_" + String.format("%02d", month) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        attendanceExporter.write(report, response.getOutputStream());
    }

    private Integer parseIntParam(String raw) {
        if (isBlank(raw)) {
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void displayPayrollConfig(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<PayrollSetting> settings = payrollConfigDAO.getConfigurablePayrollSettings();
        for (PayrollSetting setting : settings) {
            setting.setDisplayValue(payrollConfigWorkflowService.displayPayrollSettingValue(setting));
        }
        request.setAttribute("settings", settings);
        request.setAttribute("deductionRules", payrollConfigDAO.getDeductionRules(false));
        request.setAttribute("taxBrackets", payrollConfigDAO.getTaxBrackets(false));
        request.setAttribute("allowanceTypes", payrollConfigDAO.getAllowanceTypes(false));
        request.setAttribute("pendingRequests", payrollConfigWorkflowService.getPendingRequests());
        request.setAttribute("payrollConfigBaseUrl", request.getContextPath() + "/v1/businessadmin/payroll-config");
        request.setAttribute("canEditPayrollConfig", false);
        request.setAttribute("canApprovePayrollConfig", true);
        request.setAttribute("sidebarPath", "/public/components/businessAdminSideBar.jsp");
        request.setAttribute("topbarPath", "/public/components/businessAdminTopBar.jsp");
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
        request.getRequestDispatcher("/public/businessadmin/salary/payroll_config.jsp").forward(request, response);
    }

    private void displayPayrollConfigHistory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer status = parseIntParam(request.getParameter("status"));
        status = status != null && (status == PayrollConfigChangeRequest.STATUS_APPROVED
                || status == PayrollConfigChangeRequest.STATUS_REJECTED) ? status : null;
        String q = trim(request.getParameter("q"));
        Integer requestedPage = parseIntParam(request.getParameter("page"));
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
        request.setAttribute("pageBase", request.getContextPath() + "/v1/businessadmin/payroll-config/history?status="
                + (status == null ? "" : status) + "&q=" + q.replace(" ", "+"));
        request.setAttribute("payrollConfigBaseUrl", request.getContextPath() + "/v1/businessadmin/payroll-config");
        request.getRequestDispatcher("/public/businessadmin/salary/payroll_config_history.jsp").forward(request, response);
    }

    private void handleSavePayrollSetting(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        setPayrollConfigError(request, "Quản trị doanh nghiệp chỉ duyệt yêu cầu thay đổi cấu hình lương, không chỉnh trực tiếp.");
        response.sendRedirect(request.getContextPath() + "/v1/businessadmin/payroll-config");
    }

    private void handleSavePayrollDeduction(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        setPayrollConfigError(request, "Quản trị doanh nghiệp chỉ duyệt yêu cầu thay đổi cấu hình lương, không chỉnh trực tiếp.");
        response.sendRedirect(request.getContextPath() + "/v1/businessadmin/payroll-config");
    }

    private void handleDeletePayrollDeduction(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        setPayrollConfigError(request, "Quản trị doanh nghiệp chỉ duyệt yêu cầu thay đổi cấu hình lương, không chỉnh trực tiếp.");
        response.sendRedirect(request.getContextPath() + "/v1/businessadmin/payroll-config");
    }

    private void handleSavePayrollTaxBracket(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        setPayrollConfigError(request, "Quản trị doanh nghiệp chỉ duyệt yêu cầu thay đổi cấu hình lương, không chỉnh trực tiếp.");
        response.sendRedirect(request.getContextPath() + "/v1/businessadmin/payroll-config");
    }

    private void handleSavePayrollAllowance(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        setPayrollConfigError(request, "Quản trị doanh nghiệp chỉ duyệt yêu cầu thay đổi cấu hình lương, không chỉnh trực tiếp.");
        response.sendRedirect(request.getContextPath() + "/v1/businessadmin/payroll-config");
    }

    private void handleDeletePayrollAllowance(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        setPayrollConfigError(request, "Quản trị doanh nghiệp chỉ duyệt yêu cầu thay đổi cấu hình lương, không chỉnh trực tiếp.");
        response.sendRedirect(request.getContextPath() + "/v1/businessadmin/payroll-config");
    }

    private void handleApprovePayrollConfigRequest(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        Integer requestId = parseIntParam(request.getParameter("requestId"));
        String note = trim(request.getParameter("note"));
        boolean ok = requestId != null && payrollConfigWorkflowService.approveRequest(requestId, user, note);
        if (ok) {
            setPayrollConfigSuccess(request, "Đã duyệt và áp dụng thay đổi cấu hình lương.");
        } else {
            setPayrollConfigError(request, "Duyệt yêu cầu thay đổi cấu hình lương thất bại.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/businessadmin/payroll-config");
    }

    private void handleRejectPayrollConfigRequest(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        Integer requestId = parseIntParam(request.getParameter("requestId"));
        String note = trim(request.getParameter("note"));
        boolean ok = requestId != null && payrollConfigWorkflowService.rejectRequest(requestId, user, note);
        if (ok) {
            setPayrollConfigSuccess(request, "Đã từ chối yêu cầu thay đổi cấu hình lương.");
        } else {
            setPayrollConfigError(request, "Từ chối yêu cầu thay đổi cấu hình lương thất bại.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/businessadmin/payroll-config");
    }

    private String trim(String raw) {
        return raw == null ? "" : raw.trim();
    }

    private void setPayrollConfigSuccess(HttpServletRequest request, String message) {
        request.getSession().setAttribute("payrollConfigSuccess", message);
    }

    private void setPayrollConfigError(HttpServletRequest request, String message) {
        request.getSession().setAttribute("payrollConfigError", message);
    }

    private void displayAllSalaryForBa(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {

        java.time.YearMonth latestClosed = java.time.YearMonth.now().minusMonths(1);
        Integer month = parseIntParam(request.getParameter("month"));
        Integer year = parseIntParam(request.getParameter("year"));
        if (month == null || month < 1 || month > 12 || year == null || year < 2000) {
            month = latestClosed.getMonthValue();
            year = latestClosed.getYear();
        }
        // Không cho xem tháng tương lai
        java.time.YearMonth selected = java.time.YearMonth.of(year, month);
        if (selected.isAfter(latestClosed)) {
            selected = latestClosed;
            month = selected.getMonthValue();
            year = selected.getYear();
        }

        Integer departmentId = parseIntParam(request.getParameter("departmentId"));

        // Tái dụng PayrollService đã có
        service.PayrollService payrollService = new service.PayrollService();
        service.AttendanceClosingService closingService = new service.AttendanceClosingService();

        boolean attendanceLocked = departmentId == null
                ? closingService.isPeriodLocked(year, month)
                : closingService.isDepartmentLocked(year, month, departmentId);

        java.util.List<dto.PayrollPreviewDTO> previews
                = payrollService.getAllPayrollForHr(user, year, month, departmentId);

        if (previews.isEmpty()) {
            request.setAttribute("salaryError",
                    "Chưa có bảng lương cho tháng " + String.format("%02d/%d", month, year)
                    + ". Có thể kỳ này chưa được tính lương.");
        }

        request.setAttribute("payrollPreviews", previews);
        request.setAttribute("selectedYear", year);
        request.setAttribute("selectedMonth", month);
        request.setAttribute("selectedDepartmentId", departmentId);
        request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
        request.setAttribute("attendanceLocked", attendanceLocked);
        request.setAttribute("canGeneratePayroll", false);
        request.setAttribute("canApprovePayroll", false);
        request.setAttribute("canExportPayroll", attendanceLocked); // chỉ xuất khi đã chốt
        request.setAttribute("canFinalizePayroll", true);
        // Chốt luôn áp dụng cho toàn công ty, không bị giới hạn bởi bộ lọc phòng ban đang xem.
        request.setAttribute("countAwaitingFinalization",
                payrollService.countAwaitingFinalizationForPeriod(year, month, null));

        request.getRequestDispatcher("/public/businessadmin/salary/salary_list.jsp")
                .forward(request, response);
    }

    private void handleFinalizePayroll(HttpServletRequest request, HttpServletResponse response,
            User user) throws IOException {
        Integer month = parseIntParam(request.getParameter("month"));
        Integer year = parseIntParam(request.getParameter("year"));
        if (month == null || year == null) {
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/salary/all");
            return;
        }

        service.PayrollService payrollService = new service.PayrollService();
        int finalizedCount = payrollService.finalizePayrollForPeriod(user, year, month, null);
        if (finalizedCount > 0) {
            request.getSession().setAttribute("success",
                    "Đã chốt " + finalizedCount + " bảng lương cho kỳ lương "
                    + String.format("%02d/%d", month, year) + ".");
        } else {
            request.getSession().setAttribute("error",
                    "Không có bảng lương nào đang chờ chốt để xử lý.");
        }

        response.sendRedirect(request.getContextPath() + "/v1/businessadmin/salary/all?month=" + month
                + "&year=" + year);
    }

    private void displaySalaryDetailForBa(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {

        service.PayrollService payrollService = new service.PayrollService();
        Integer payrollId = parseIntParam(request.getParameter("id"));
        dto.PayrollPreviewDTO preview = (payrollId != null)
                ? payrollService.getPayrollDetail(user, payrollId)
                : null;

        if (preview == null) {
            request.setAttribute("salaryError", "Không tìm thấy bảng lương cần xem chi tiết.");
        }
        request.setAttribute("payrollPreview", preview);
        request.setAttribute("allowanceTypes", payrollConfigDAO.getAllowanceTypes(true));
        request.getRequestDispatcher("/public/businessadmin/salary/salary_detail.jsp")
                .forward(request, response);
    }

    private void exportSalaryForBa(HttpServletRequest request, HttpServletResponse response,
            User user) throws IOException {

        service.PayrollService payrollService = new service.PayrollService();
        service.AttendanceClosingService closingService = new service.AttendanceClosingService();

        java.time.YearMonth latestClosed = java.time.YearMonth.now().minusMonths(1);
        Integer month = parseIntParam(request.getParameter("month"));
        Integer year = parseIntParam(request.getParameter("year"));
        if (month == null || month < 1 || month > 12 || year == null || year < 2000) {
            month = latestClosed.getMonthValue();
            year = latestClosed.getYear();
        }
        Integer departmentId = parseIntParam(request.getParameter("departmentId"));

        boolean locked = departmentId == null
                ? closingService.isPeriodLocked(year, month)
                : closingService.isDepartmentLocked(year, month, departmentId);

        if (!locked) {
            request.getSession().setAttribute("error",
                    "Bảng chấm công kỳ này chưa được chốt. Chưa thể xuất bảng lương.");
            response.sendRedirect(request.getContextPath()
                    + "/v1/businessadmin/salary/all?month=" + month + "&year=" + year
                    + (departmentId == null ? "" : "&departmentId=" + departmentId));
            return;
        }

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=payroll_" + String.format("%04d_%02d", year, month) + ".xlsx");

        try (java.io.OutputStream out = response.getOutputStream()) {
            payrollService.exportPayrollWorkbook(user, year, month, departmentId, out);
        }
    }

    // =========================================================
    // Quản lý Đơn từ (Form Requests)
    // =========================================================
    private void displayFormRequests(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String dayStr = request.getParameter("day");
        String monthStr = request.getParameter("month");
        String yearStr = request.getParameter("year");
        String keyword = request.getParameter("keyword");
        String statusStr = request.getParameter("status");

        Integer day = parseIntParam(dayStr);
        Integer month = parseIntParam(monthStr);
        Integer year = parseIntParam(yearStr);
        Integer statusFilter = parseIntParam(statusStr);

        List<FormRequestDTO> forms = formRequestDAO.getAllFormRequests(day, month, year, keyword)
                .stream()
                .filter(f -> {
                    if (!"OVERTIME".equals(f.getFormTypeCode())
                            && !"TRANSFER".equals(f.getFormTypeCode())
                            && !"PROMOTION_DEMOTION".equals(f.getFormTypeCode())) {
                        return false;
                    }
                    if (statusFilter != null && f.getStatus() != statusFilter) {
                        return false;
                    }
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());

        // Phân trang
        List<FormRequestDTO> pagedForms = utils.Paging.page(request, forms);

        request.setAttribute("forms", pagedForms);
        request.setAttribute("statusFilter", statusStr);
        request.setAttribute("filterDay", day);
        request.setAttribute("filterMonth", month);
        request.setAttribute("filterYear", year);
        request.setAttribute("keyword", keyword);

        request.getRequestDispatcher("/public/businessadmin/overtime/form_requests.jsp").forward(request, response);
    }

    private void displayOTDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/forms");
            return;
        }
        try {
            int formId = Integer.parseInt(idParam.trim());
            OvertimeRequestDTO otRequest = overtimeDAO.getOvertimeRequestById(formId);
            if (otRequest == null) {
                request.getSession().setAttribute("error", "Không tìm thấy đơn OT.");
                response.sendRedirect(request.getContextPath() + "/v1/businessadmin/forms");
                return;
            }

            List<EmployeeDetailDTO> assignees = overtimeDAO.getOvertimeAssignees(formId);
            request.setAttribute("otRequest", otRequest);
            request.setAttribute("assignees", assignees);

            request.getRequestDispatcher("/public/businessadmin/overtime/ot_detail.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/forms");
        }
    }

    private void displayTransferDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/forms");
            return;
        }
        try {
            int formId = Integer.parseInt(idParam.trim());
            FormRequestDTO form = formRequestDAO.getFormRequestById(formId);
            if (form == null || !"TRANSFER".equals(form.getFormTypeCode())) {
                request.getSession().setAttribute("error", "Không tìm thấy đơn chuyển phòng ban.");
                response.sendRedirect(request.getContextPath() + "/v1/businessadmin/forms");
                return;
            }

            request.setAttribute("form", form);
            request.getRequestDispatcher("/public/businessadmin/overtime/transfer_detail.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/forms");
        }
    }

    private void handleApproveForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        try {
            Integer formId = parseIntParam(request.getParameter("formId"));
            String note = request.getParameter("note");
            if (formId == null) {
                request.getSession().setAttribute("error", "Không tìm thấy mã đơn.");
                response.sendRedirect(request.getContextPath() + "/v1/businessadmin/forms");
                return;
            }
            FormRequestDTO form = formRequestDAO.getFormRequestById(formId);
            if (form == null) {
                request.getSession().setAttribute("error", "Không tìm thấy đơn.");
                response.sendRedirect(request.getContextPath() + "/v1/businessadmin/forms");
                return;
            }

            EmployeeDetailDTO approver = employeeDAO.getEmployeeByUserId(user.getUserId());
            int approverId = approver != null ? approver.getEmployeeId() : 0;

            int newStatus = ("TRANSFER".equals(form.getFormTypeCode()) || "PROMOTION_DEMOTION".equals(form.getFormTypeCode())) ? 3 : 1;
            boolean ok = formRequestDAO.updateFormRequest(formId, newStatus, approverId, note != null ? note.trim() : "");

            if (ok) {
                switch (form.getFormTypeCode()) {
                    case "TRANSFER":
                        onApproveTransfer(form, approverId);
                        break;
                    case "PROMOTION_DEMOTION":
                        onApprovePromotion(form, approverId);
                        break;
                    default:
                        break;
                }
                request.getSession().setAttribute("success", "Đã duyệt đơn thành công.");
            } else {
                request.getSession().setAttribute("error", "Lỗi khi duyệt đơn.");
            }
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error approving form", e);
            request.getSession().setAttribute("error", "Lỗi hệ thống khi duyệt đơn.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/businessadmin/forms");
    }

    private void onApproveTransfer(FormRequestDTO form, int approverId) {
        if (!(form instanceof TransferRequestDTO)) {
            return;
        }
        TransferRequestDTO tf = (TransferRequestDTO) form;

        if (tf.getTargetDepartmentId() != null) {
            EmployeeDetailDTO emp = employeeDAO.getEmployeeById(tf.getEmployeeId());
            if (emp != null) {
                // Update position and department
                int currentPosId = emp.getPositionId(); //!= null) ? emp.getPositionId() : 0;
                employeeDAO.reassignEmployeeDepartment(tf.getEmployeeId(), tf.getTargetDepartmentId(), currentPosId);

                // Update role if targetRoleId exists
                if (tf.getTargetRoleId() != null) {
                    userDAO.updateUserRole(emp.getUserId(), tf.getTargetRoleId());
                }
            }
        }
    }

    private void onApprovePromotion(FormRequestDTO form, int approverId) {
        if (!(form instanceof TransferRequestDTO)) {
            return;
        }
        TransferRequestDTO tf = (TransferRequestDTO) form;

        EmployeeDetailDTO emp = employeeDAO.getEmployeeById(tf.getEmployeeId());
        if (emp != null) {
            // Update role
            if (tf.getTargetRoleId() != null) {
                userDAO.updateUserRole(emp.getUserId(), tf.getTargetRoleId());

                // If it's a manager role, also set them as the department manager
                Role newRole = roleDAO.getRoleById(tf.getTargetRoleId());
                if (newRole != null && newRole.getRoleName() != null && newRole.getRoleName().toLowerCase().contains("manager")) {
                    departmentDAO.assignManager(emp.getDepartmentId(), emp.getEmployeeId());
                }
            }
        }
    }

    private void handleRejectForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        try {
            Integer formId = parseIntParam(request.getParameter("formId"));
            String note = request.getParameter("note");
            if (formId == null) {
                request.getSession().setAttribute("error", "Không tìm thấy mã đơn.");
                response.sendRedirect(request.getContextPath() + "/v1/businessadmin/forms");
                return;
            }

            EmployeeDetailDTO approver = employeeDAO.getEmployeeByUserId(user.getUserId());
            int approverId = approver != null ? approver.getEmployeeId() : 0;

            boolean ok = formRequestDAO.rejectFormRequest(formId, approverId, note != null ? note.trim() : "");
            if (ok) {
                request.getSession().setAttribute("success", "Đã từ chối đơn thành công.");
            } else {
                request.getSession().setAttribute("error", "Lỗi khi từ chối đơn.");
            }
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error rejecting form", e);
            request.getSession().setAttribute("error", "Lỗi hệ thống khi từ chối đơn.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/businessadmin/forms");
    }

}
