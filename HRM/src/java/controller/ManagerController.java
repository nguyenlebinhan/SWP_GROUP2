package controller;

import dao.*;
import dto.EmployeeDTO;
import dto.EmployeeDetailDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.*;
import model.Attendance;
import dto.OvertimeRequestDTO;
import model.Department;
import model.Employee;
import model.EmploymentContract;
import model.Position;
import model.Role;
import model.User;

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
    private static final dao.OvertimeDAO overtimeDAO = new dao.OvertimeDAO();
    private static final dao.FormTypeDAO formTypeDAO = new dao.FormTypeDAO();

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
            case "/my-department-list":
                displayMyDepartmentEmployees(request, response, user);
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
            case "/add-contract":
                displayAddContractForm(request, response, user);
                break;
            case "/contract-preview":
                displayContractPreview(request, response, user);
                break;
            case "/department-detail":
                displayEmployeeDepartmentDetail(request, response, user);
                break;
            case "/assign-department":
                displayAssignDepartmentForm(request, response, user);
                break;
            case "/reassign-department":
                displayReassignDepartmentForm(request, response, user);
                break;
            case "/department-list":
                displayDepartmentList(request, response, user);
                break;
            case "/add-department":
                displayAddDepartmentForm(request, response, user);
                break;
            case "/update-department":
                displayUpdateDepartmentForm(request, response, user);
                break;
            case "/my-profile":
                displayMyProfile(request, response, user);
                break;
            case "/all-forms":
                displayAllForms(request, response, user);
                break;
            case "/form-detail":
                displayFormDetail(request, response, user);
                break;
            case "/dept-forms":
                displayDeptForms(request, response, user);
                break;
            case "/department-attendance":
                displayDepartmentAttendance(request, response, user);
                break;
            case "/own-attendance":
                displayOwnAttendance(request, response, user);
                break;
            case "/ot-requests":
                displayOTRequests(request, response, user);
                break;
            case "/create-ot":
                displayCreateOTForm(request, response, user);
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
            case "/assign-department":
                handleAssignDepartment(request, response, user);
                break;
            case "/update-employee":
                handleUpdateEmployee(request, response, user);
                break;
            case "/add-contract":
                handleAddContract(request, response, user);
                break;
            case "/reassign-department":
                handleReassignDepartment(request, response, user);
                break;
            case "/unassign-department":
                handleUnassignDepartment(request, response, user);
                break;
            case "/add-department":
                handleAddDepartment(request, response, user);
                break;
            case "/update-department":
                handleUpdateDepartment(request, response, user);
                break;
            case "/update-my-profile":
                handleUpdateMyProfile(request, response, user);
                break;
            case "/update-employee-detail":
                handleUpdateEmployeeDetail(request, response, user);
                break;
            case "/approve-form":
                handleApproveForm(request, response, user);
                break;
            case "/reject-form":
                handleRejectForm(request, response, user);
                break;
            case "/create-ot":
                handleCreateOT(request, response, user);
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
            request.setAttribute("error", "Bạn chưa được phân công vào phòng ban nào.");
            setPermissionFlags(request, perms);
            request.getRequestDispatcher("/public/manager/employee_list.jsp").forward(request, response);
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
        request.getRequestDispatcher("/public/manager/employee_list.jsp").forward(request, response);
    }

    private void displayDepartmentAttendance(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);

        EmployeeDetailDTO manager = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (manager == null || manager.getDepartmentId() <= 0) {
            request.setAttribute("error", "Bạn chưa được phân công vào phòng ban nào.");
            request.getRequestDispatcher("/public/manager/department_attendance.jsp").forward(request, response);
            return;
        }

        String rawMonth = request.getParameter("month");
        String rawYear = request.getParameter("year");
        String employeeCode = request.getParameter("employeeCode");
        if (employeeCode != null) employeeCode = employeeCode.trim();

        Integer month = null;
        Integer year = null;
        try {
            if (rawMonth != null && !rawMonth.trim().isEmpty() && !rawMonth.equals("0")) {
                month = Integer.parseInt(rawMonth.trim());
            }
            if (rawYear != null && !rawYear.trim().isEmpty()) {
                year = Integer.parseInt(rawYear.trim());
            }
        } catch (NumberFormatException ignored) {}

        List<Attendance> attendances = attendanceDAO.getAttendanceList(
                manager.getDepartmentId(), month, year, employeeCode, null);

        request.setAttribute("attendances", attendances);
        request.setAttribute("filterMonth", month);
        request.setAttribute("filterYear", year);
        request.setAttribute("filterEmployeeCode", employeeCode);
        request.setAttribute("departmentName", manager.getDepartmentName());

        request.getRequestDispatcher("/public/manager/department_attendance.jsp").forward(request, response);
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
        request.getRequestDispatcher("/public/manager/own_attendance.jsp").forward(request, response);
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
        List<EmployeeDetailDTO> employees = employeeDAO.getAllEmployees(user.getUserId());
        request.setAttribute("employees", employees);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/employee_list.jsp").forward(request, response);
    }

    private void displayEmployeeDetail(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        EmployeeDetailDTO employee = getEmployeeFromRequest(request, response);
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee-list");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("employee", employee);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/employee_detail.jsp").forward(request, response);
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
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee-list");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("employee", employee);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/update_employee.jsp").forward(request, response);
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
        request.setAttribute("employees", employeeDAO.getAllEmployees(user.getUserId()));
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/add_contract.jsp").forward(request, response);
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
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee-list");
            return;
        }

        EmployeeDetailDTO employee = employeeDAO.getEmployeeById(contract.getEmployeeId());
        if (employee == null) {
            request.getSession().setAttribute("error", "Không tìm thấy nhân viên của hợp đồng.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee-list");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("contract", contract);
        request.setAttribute("employee", employee);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/contract_preview.jsp").forward(request, response);
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
            response.sendRedirect(request.getContextPath() + "/v1/manager/department-list");
            return;
        }

        int departmentId;
        try {
            departmentId = Integer.parseInt(rawDepartmentId);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã phòng ban không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department-list");
            return;
        }

        Department department = departmentDAO.getDepartmentById(departmentId);
        if (department == null) {
            request.getSession().setAttribute("error", "Không tìm thấy phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department-list");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        List<EmployeeDetailDTO> employees = employeeDAO.getEmployeesByDepartmentId(departmentId);
        request.setAttribute("department", department);
        request.setAttribute("employees", employees);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/department_employee_detail.jsp").forward(request, response);
    }

    private void displayAssignDepartmentForm(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {

        if (!isHrStaff(user) || !hasPermission(user, "ASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền phân công phòng ban.");
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
        request.getRequestDispatcher("/public/manager/assign_department.jsp").forward(request, response);
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
        request.getRequestDispatcher("/public/manager/department_list.jsp").forward(request, response);
    }

    private void displayMyProfile(HttpServletRequest request, HttpServletResponse response,
            User sessionUser) throws ServletException, IOException {
        User currentUser = userDAO.getUserById(sessionUser.getUserId());
        request.setAttribute("currentUser", currentUser);
        EmployeeDetailDTO myEmployee = employeeDAO.getEmployeeByUserId(sessionUser.getUserId());
        request.setAttribute("myEmployee", myEmployee);
        request.getRequestDispatcher("/public/manager/my_profile.jsp").forward(request, response);
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
            request.setAttribute("error", "Vui lòng chọn đầy đủ nhân viên, phòng ban và vị trí.");
            request.setAttribute("availableEmployees", employeeDAO.getEmployees(user.getUserId()));
            request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
            request.setAttribute("positions", departmentDAO.getAllPositions());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/assign_department.jsp").forward(request, response);
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
            request.getRequestDispatcher("/public/manager/assign_department.jsp").forward(request, response);
            return;
        }

        if (employeeDAO.isUserAssignedToDepartment(userId)) {
            request.setAttribute("error", "Người dùng này đã được phân công phòng ban rồi.");
            request.setAttribute("availableEmployees", employeeDAO.getEmployees(user.getUserId()));
            request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
            request.setAttribute("positions", departmentDAO.getAllPositions());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/assign_department.jsp").forward(request, response);
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
            request.setAttribute("error", msg);
            request.setAttribute("availableEmployees", employeeDAO.getEmployees(user.getUserId()));
            request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
            request.setAttribute("positions", departmentDAO.getAllPositions());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/assign_department.jsp").forward(request, response);
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
            request.setAttribute("error", "Phân công thất bại. Vui lòng thử lại.");
            request.setAttribute("availableEmployees", employeeDAO.getEmployees(user.getUserId()));
            request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
            request.setAttribute("positions", departmentDAO.getAllPositions());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/assign_department.jsp").forward(request, response);
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

        request.getSession().setAttribute("success", "Phân công nhân viên vào phòng ban thành công.");
        response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
    }

    private void displayReassignDepartmentForm(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {

        if (!isHrStaff(user) || !hasPermission(user, "REASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền chuyển phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);

        String rawEmployeeId = request.getParameter("id");
        if (!isBlank(rawEmployeeId)) {
            try {
                int employeeId = Integer.parseInt(rawEmployeeId);
                EmployeeDetailDTO selected = employeeDAO.getEmployeeById(employeeId);
                if (selected != null && selected.getDepartmentId() > 0) {
                    request.setAttribute("selectedEmployeeId", employeeId);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        List<EmployeeDetailDTO> assignedEmployees = employeeDAO.getAssignedEmployees(user.getUserId());
        List<Department> departments = departmentDAO.getAllActiveDepartments();
        List<Position> positions = departmentDAO.getAllPositions();

        request.setAttribute("assignedEmployees", assignedEmployees);
        request.setAttribute("departments", departments);
        request.setAttribute("positions", positions);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/reassign_department.jsp").forward(request, response);
    }

    private void handleReassignDepartment(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "REASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền chuyển phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        String rawEmployeeId = request.getParameter("employeeId");
        String rawDepartmentId = request.getParameter("departmentId");
        String rawPositionId = request.getParameter("positionId");

        if (isBlank(rawEmployeeId) || isBlank(rawDepartmentId) || isBlank(rawPositionId)) {
            reloadReassignFormWithError(request, response, user,
                    "Vui lòng chọn đầy đủ nhân viên, phòng ban và vị trí.");
            return;
        }

        int employeeId, departmentId, positionId;
        try {
            employeeId = Integer.parseInt(rawEmployeeId);
            departmentId = Integer.parseInt(rawDepartmentId);
            positionId = Integer.parseInt(rawPositionId);
        } catch (NumberFormatException e) {
            reloadReassignFormWithError(request, response, user, "Dữ liệu không hợp lệ.");
            return;
        }

        EmployeeDetailDTO employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null || employee.getDepartmentId() <= 0) {
            reloadReassignFormWithError(request, response, user,
                    "Nhân viên không hợp lệ hoặc chưa được phân công phòng ban.");
            return;
        }

        if (employee.getDepartmentId() == departmentId) {
            reloadReassignFormWithError(request, response, user,
                    "Nhân viên đã thuộc phòng ban này. Vui lòng chọn phòng ban khác.");
            return;
        }

        int userRoleId = userDAO.getRoleIdByUserId(employee.getUserId());
        if (!departmentDAO.isRoleAllowedForDepartment(departmentId, userRoleId)) {
            Department dept = departmentDAO.getDepartmentById(departmentId);
            String deptName = (dept != null) ? dept.getDepartmentName() : "phòng ban này";
            List<String> allowed = departmentDAO.getAllowedRoleNames(departmentId);
            String msg = "Vai trò hiện tại của nhân viên không phù hợp với phòng \"" + deptName + "\". "
                    + "Phòng này chỉ nhận vai trò: " + String.join(", ", allowed) + ". "
                    + "Vui lòng đổi vai trò của người dùng trước khi chuyển phòng.";
            reloadReassignFormWithError(request, response, user, msg);
            return;
        }

        boolean success = employeeDAO.reassignEmployeeDepartment(employeeId, departmentId, positionId);
        if (!success) {
            reloadReassignFormWithError(request, response, user, "Chuyển phòng ban thất bại. Vui lòng thử lại.");
            return;
        }

        String roleName = roleDAO.getRoleByUserId(employee.getUserId());
        Department newDept = departmentDAO.getDepartmentById(departmentId);
        boolean deptHasManager = newDept != null && newDept.getManagerId() != null;
        boolean isManagerRole = roleName != null && roleName.toLowerCase().contains("manager");

        if (isManagerRole && !deptHasManager) {
            employeeDAO.assignAsManager(departmentId, employeeId);
        } else if (deptHasManager) {
            employeeDAO.setEmployeeManager(employeeId, newDept.getManagerId());
        }

        LOGGER.log(Level.INFO, "Employee reassigned: employeeId={0} → deptId={1}",
                new Object[]{employeeId, departmentId});

        request.getSession().setAttribute("success", "Chuyển nhân viên sang phòng ban mới thành công.");
        response.sendRedirect(request.getContextPath() + "/v1/manager/department-detail?id=" + departmentId);
    }

    private void handleUnassignDepartment(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "REASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền gỡ phân công.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        String rawEmployeeId = request.getParameter("employeeId");
        if (isBlank(rawEmployeeId)) {
            request.getSession().setAttribute("error", "Thiếu mã nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee-list");
            return;
        }

        int employeeId;
        try {
            employeeId = Integer.parseInt(rawEmployeeId);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã nhân viên không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee-list");
            return;
        }

        EmployeeDetailDTO employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null || employee.getDepartmentId() <= 0) {
            request.getSession().setAttribute("error", "Nhân viên không hợp lệ hoặc chưa được phân công phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee-detail?id=" + employeeId);
            return;
        }

        boolean success = employeeDAO.unassignEmployee(employeeId);
        if (success) {
            LOGGER.log(Level.INFO, "Employee unassigned: employeeId={0}", employeeId);
            request.getSession().setAttribute("success",
                    "Đã gỡ phân công nhân viên. Hệ thống admin có thể đổi vai trò, sau đó phân công lại vào phòng phù hợp.");
        } else {
            request.getSession().setAttribute("error", "Gỡ phân công thất bại. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/manager/employee-detail?id=" + employeeId);
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
        request.getRequestDispatcher("/public/manager/add_department.jsp").forward(request, response);
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
            request.getRequestDispatcher("/public/manager/add_department.jsp").forward(request, response);
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
            setPermissionFlags(request, getPermissions((User) request.getSession().getAttribute("user")));
            request.getRequestDispatcher("/public/manager/add_department.jsp").forward(request, response);
            return;
        }

        // Gắn luật vai trò cho phòng ban. Để trống = phòng ban nhận mọi vai trò.
        if (!roleIds.isEmpty()) {
            departmentDAO.replaceDepartmentRoles(newDeptId, roleIds);
        }

        LOGGER.log(Level.INFO, "Department created: code={0} by userId={1}", new Object[]{code, user.getUserId()});
        request.getSession().setAttribute("success", "Thêm phòng ban \"" + name.trim() + "\" thành công.");
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
            response.sendRedirect(request.getContextPath() + "/v1/manager/department-list");
            return;
        }
        int deptId;
        try {
            deptId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/department-list");
            return;
        }
        Department dept = departmentDAO.getDepartmentById(deptId);
        if (dept == null) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/department-list");
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
        request.getRequestDispatcher("/public/manager/update_department.jsp").forward(request, response);
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
            response.sendRedirect(request.getContextPath() + "/v1/manager/update-department?id=" + (idParam != null ? idParam : ""));
            return;
        }

        int deptId;
        try {
            deptId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/department-list");
            return;
        }

        Department dept = departmentDAO.getDepartmentById(deptId);
        if (dept == null) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/department-list");
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
            request.getSession().setAttribute("success", "Cập nhật phòng ban thành công.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department-list");
        } else {
            request.getSession().setAttribute("error", "Cập nhật thất bại. Vui lòng thử lại.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/update-department?id=" + deptId);
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
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee-list");
            return;
        }

        if (!isValidEmployeeStatus(status)) {
            request.getSession().setAttribute("error", "Trạng thái nhân viên không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/update-employee?id=" + employeeId);
            return;
        }

        EmployeeDetailDTO current = employeeDAO.getEmployeeById(employeeId);
        if (current == null) {
            request.getSession().setAttribute("error", "Không tìm thấy nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee-list");
            return;
        }

        int departmentId = current.getDepartmentId();
        int positionId = current.getPositionId();
        int userRoleId = userDAO.getRoleIdByUserId(current.getUserId());
        if (!departmentDAO.isRoleAllowedForDepartment(departmentId, userRoleId)) {
            request.getSession().setAttribute("error", "Vai trò hiện tại của nhân viên không phù hợp với phòng ban đã chọn.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/update-employee?id=" + employeeId);
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
            request.getSession().setAttribute("success", "Cập nhật thông tin nhân viên thành công.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee-detail?id=" + employeeId);
        } else {
            request.getSession().setAttribute("error", "Cập nhật nhân viên thất bại. Vui lòng thử lại.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/update-employee?id=" + employeeId);
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
            request.setAttribute("error", "Vui lòng nhập đầy đủ mã hợp đồng, nhân viên, loại hợp đồng, ngày bắt đầu và lương.");
            request.setAttribute("employees", employeeDAO.getAllEmployees(user.getUserId()));
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/add_contract.jsp").forward(request, response);
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
            request.setAttribute("employees", employeeDAO.getAllEmployees(user.getUserId()));
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/add_contract.jsp").forward(request, response);
            return;
        }

        if (!isValidContractType(type) || contract.getSalary().compareTo(BigDecimal.ZERO) < 0) {
            request.setAttribute("error", "Loại hợp đồng hoặc lương không hợp lệ.");
            request.setAttribute("employees", employeeDAO.getAllEmployees(user.getUserId()));
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/add_contract.jsp").forward(request, response);
            return;
        }

        if (employeeDAO.getEmployeeById(contract.getEmployeeId()) == null) {
            request.setAttribute("error", "Nhân viên được chọn không tồn tại.");
            request.setAttribute("employees", employeeDAO.getAllEmployees(user.getUserId()));
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/add_contract.jsp").forward(request, response);
            return;
        }

        if (contractDAO.hasActiveContract(contract.getEmployeeId())) {
            request.setAttribute("error", "Hợp đồng của nhân viên vẫn còn hiệu lực");
            request.setAttribute("employees", employeeDAO.getAllEmployees(user.getUserId()));
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/add_contract.jsp").forward(request, response);
            return;
        }

        if (contract.getEndDate() != null && contract.getEndDate().before(contract.getStartDate())) {
            request.setAttribute("error", "Ngày kết thúc không được trước ngày bắt đầu.");
            request.setAttribute("employees", employeeDAO.getAllEmployees(user.getUserId()));
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/add_contract.jsp").forward(request, response);
            return;
        }

        contract.setContractCode(code);
        contract.setContractType(type);
        contract.setStatus(1);
        contract.setNote(trimToNull(request.getParameter("note")));
        contract.setCreatedBy(user.getUserId());

        boolean success = contractDAO.addContract(contract);
        if (success) {
            request.getSession().setAttribute("success", "Thêm hợp đồng lao động thành công.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/contract-preview?employeeId=" + contract.getEmployeeId());
        } else {
            request.setAttribute("error", "Thêm hợp đồng thất bại. Mã hợp đồng có thể đã tồn tại.");
            request.setAttribute("employees", employeeDAO.getAllEmployees(user.getUserId()));
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/manager/add_contract.jsp").forward(request, response);
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
        request.getRequestDispatcher("/public/manager/all_form_list.jsp").forward(request, response);
    }
   private void displayFormDetail(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String formIdRaw = request.getParameter("id");
        if (isBlank(formIdRaw)) {
            request.getSession().setAttribute("error", "Thiếu mã đơn.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dept-forms");
            return;
        }
        try {
            int formId = Integer.parseInt(formIdRaw);
            dto.FormRequestDTO form = formRequestDAO.getFormRequestById(formId);
            if (form == null) {
                request.getSession().setAttribute("error", "Không tìm thấy đơn yêu cầu.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/dept-forms");
                return;
            }
            
            request.setAttribute("form", form);
            request.getRequestDispatcher("/public/manager/form_detail.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã đơn không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dept-forms");
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
        request.getRequestDispatcher("/public/manager/dept_form_list.jsp").forward(request, response);
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
            response.sendRedirect(request.getContextPath() + "/v1/manager/dept-forms");
            return;
        }
        try {
            int formId = Integer.parseInt(rawId);
            boolean ok = formRequestDAO.approveFormRequest(formId, me.getEmployeeId(), note);
            request.getSession().setAttribute(ok ? "success" : "error",
                    ok ? "Duyệt đơn thành công." : "Duyệt đơn thất bại.");
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã đơn không hợp lệ.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/manager/dept-forms");
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
            response.sendRedirect(request.getContextPath() + "/v1/manager/dept-forms");
            return;
        }
        try {
            int formId = Integer.parseInt(rawId);
            boolean ok = formRequestDAO.rejectFormRequest(formId, me.getEmployeeId(), note);
            request.getSession().setAttribute(ok ? "success" : "error",
                    ok ? "Từ chối đơn thành công." : "Từ chối đơn thất bại.");
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã đơn không hợp lệ.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/manager/dept-forms");
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
            request.getSession().setAttribute("success", "Cập nhật hồ sơ thành công.");
        } else {
            request.getSession().setAttribute("error", "Cập nhật thất bại. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/manager/my-profile");
    }

    private boolean isBlank(String v) {
        return v == null || v.trim().isEmpty();
    }

    private void preventBackCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

    }

    private void handleUpdateEmployeeDetail(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        String rawEmployeeId = request.getParameter("employeeId");
        String rawStatus = request.getParameter("status");
        String phoneNumber = request.getParameter("phoneNumber");
        String degree = request.getParameter("degree");
        String experience = request.getParameter("experience");
        String skills = request.getParameter("skills");

        if (isBlank(rawEmployeeId) || isBlank(rawStatus)) {
            request.getSession().setAttribute("error", "Dữ liệu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department-list");
            return;
        }

        int employeeId;
        int status;
        try {
            employeeId = Integer.parseInt(rawEmployeeId);
            status = Integer.parseInt(rawStatus);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Dữ liệu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department-list");
            return;
        }

        EmployeeDetailDTO employeeDetail = employeeDAO.getEmployeeById(employeeId);
        if (employeeDetail == null) {
            request.getSession().setAttribute("error", "Không tìm thấy nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department-list");
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
            request.getSession().setAttribute("success", "Cập nhật nhân viên thành công.");
        } else {
            request.getSession().setAttribute("error", "Cập nhật thất bại hoặc không có thay đổi.");
        }

        if (employeeDetail.getDepartmentId() > 0) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/department-detail?id=" + employeeDetail.getDepartmentId());
        } else {
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee-detail?id=" + employeeId);
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

    private void reloadReassignFormWithError(HttpServletRequest request, HttpServletResponse response,
            User user, String message) throws ServletException, IOException {
        request.setAttribute("error", message);
        request.setAttribute("assignedEmployees", employeeDAO.getAssignedEmployees(user.getUserId()));
        request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
        request.setAttribute("positions", departmentDAO.getAllPositions());
        setPermissionFlags(request, getPermissions(user));
        request.getRequestDispatcher("/public/manager/reassign_department.jsp").forward(request, response);
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
        request.getRequestDispatcher("/public/manager/ot_create.jsp").forward(request, response);
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
        request.getRequestDispatcher("/public/manager/ot_requests.jsp").forward(request, response);
    }

    private void handleCreateOT(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        try {
            EmployeeDetailDTO manager = employeeDAO.getEmployeeByUserId(user.getUserId());
            if (manager == null || manager.getDepartmentId() <= 0) {
                request.getSession().setAttribute("error", "Bạn chưa được phân công vào phòng ban nào.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
                return;
            }

            String otDate = request.getParameter("otDate");
            String startTime = request.getParameter("startTime");
            String endTime = request.getParameter("endTime");
            String dayTypeStr = request.getParameter("dayType");
            String reason = request.getParameter("reason");
            String[] assigneeIds = request.getParameterValues("assignees");

            if (otDate == null || otDate.isEmpty() || startTime == null || startTime.isEmpty() ||
                endTime == null || endTime.isEmpty() || dayTypeStr == null || assigneeIds == null || assigneeIds.length == 0) {
                request.getSession().setAttribute("error", "Vui lòng điền đầy đủ thông tin và chọn ít nhất 1 nhân viên.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/create-ot");
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
                response.sendRedirect(request.getContextPath() + "/v1/manager/create-ot");
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
                    request.getSession().setAttribute("success", "Đã tạo đơn Overtime thành công (Mã đơn: " + formCode + ") và gửi chờ duyệt.");
                    response.sendRedirect(request.getContextPath() + "/v1/manager/ot-requests");
                    return;
                }
            }
            
            request.getSession().setAttribute("error", "Đã xảy ra lỗi trong quá trình tạo đơn OT.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/create-ot");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tạo đơn OT", e);
            request.getSession().setAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/v1/manager/create-ot");
        }
    }
}
