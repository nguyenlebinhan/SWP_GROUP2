package controller;

import dao.*;
import dto.EmployeeDTO;
import dto.EmployeeDetailDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.*;
import model.Department;
import model.Position;
import model.User;
import static org.apache.tomcat.jakartaee.commons.lang3.StringUtils.isBlank;

public class ManagerController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ManagerController.class.getName());
    private static final UserDAO userDAO = new UserDAO();
    private static final RoleDAO roleDAO = new RoleDAO();
    private static final EmployeeDAO employeeDAO = new EmployeeDAO();
    private static final DepartmentDAO departmentDAO = new DepartmentDAO();
    private static final PermissionDAO permissionDAO = new PermissionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        preventBackCache(response);

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || !isManagerRole(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/v1/auth/login");
            return;
        }

        String action = request.getPathInfo();
        if (action == null || action.equals("/")) {
            action = "/dashboard";
        }
        switch (action) {
            case "/dashboard":
                displayDashboard(request, response, user);
                break;
            case "/employee-list":
                displayEmployeeList(request, response, user);
                break;
            case "/my-department-list":
            case "/my-department-employees":
                displayMyDepartmentEmployees(request, response, user);
                break;
            case "/employee-detail":
                displayEmployeeDetail(request, response, user);
                break;
            case "/department":
                displayDepartmentList(request, response, user);
                break;
            case "/assign-department":
                displayAssignDepartmentForm(request, response, user);
                break;
            case "/reassign-department":
                displayReassignDepartmentForm(request, response, user);
                break;
            case "/department/employees":
                displayDepartmentEmployeesById(request, response, user);
                break;
            case "/department/assign":
                displayAssignManagerForm(request, response, user);
                break;
            case "/my-profile":
                displayMyProfile(request, response, user);
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

        if (user == null || !isManagerRole(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/v1/auth/login");
            return;
        }

        String action = request.getPathInfo();
        if (action == null || action.equals("/")) {
            displayDashboard(request, response, user);
            return;
        }
        switch (action) {
            case "/assign-department":
                handleAssignDepartment(request, response, user);
                break;
            case "/reassign-department":
                handleReassignDepartment(request, response, user);
                break;
            case "/unassign-department":
                handleUnassignDepartment(request, response, user);
                break;
            case "/department/assign":
                handleAssignManager(request, response, user);
                break;
            case "/department/unassign":
                handleUnassignManager(request, response, user);
                break;
            case "/update-my-profile":
                handleUpdateMyProfile(request, response, user);
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

    private void displayMyProfile(HttpServletRequest request, HttpServletResponse response,
                                  User sessionUser) throws ServletException, IOException {
        User currentUser = userDAO.getUserById(sessionUser.getUserId());
        request.setAttribute("currentUser", currentUser);
        EmployeeDetailDTO myEmployee = employeeDAO.getEmployeeByUserId(sessionUser.getUserId());
        request.setAttribute("myEmployee", myEmployee);

        Set<String> perms = getPermissions(sessionUser);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);

        request.getRequestDispatcher("/public/manager/my_profile.jsp").forward(request, response);
    }

    private void displayEmployeeList(HttpServletRequest request, HttpServletResponse response,
                                      User user) throws ServletException, IOException {
        if (!isHrManager(user.getRoleName())) {
            displayMyDepartmentEmployees(request, response, user);
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);

        String keyword = request.getParameter("keyword") != null ? request.getParameter("keyword").trim() : "";
        String dept = request.getParameter("dept") != null ? request.getParameter("dept").trim() : "";
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

        int totalEmployees = employeeDAO.countEmployeesFiltered(user.getUserId(), null, keyword, dept, status);
        int totalPages = (int) Math.ceil((double) totalEmployees / PAGE_SIZE);
        if (totalPages < 1) {
            totalPages = 1;
        }

        List<EmployeeDetailDTO> employees = employeeDAO.getEmployeesFiltered(user.getUserId(), null, keyword, dept, status, offset, PAGE_SIZE);

        request.setAttribute("employees", employees);
        request.setAttribute("keyword", keyword);
        request.setAttribute("dept", dept);
        request.setAttribute("status", status);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalEmployees", totalEmployees);
        request.setAttribute("departments", departmentDAO.getAllDepartments());

        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/employee_all_list.jsp").forward(request, response);
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
    private void displayEmployeeDetail(HttpServletRequest request, HttpServletResponse response,
                                       User user) throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (isBlank(idParam)) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/my-department-employees");
            return;
        }

        int employeeId;
        try {
            employeeId = Integer.parseInt(idParam.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/my-department-employees");
            return;
        }

        EmployeeDetailDTO employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null) {
            request.getSession().setAttribute("error", "Không tìm thấy nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/my-department-employees");
            return;
        }

        if (!isHrManager(user.getRoleName())) {
            EmployeeDetailDTO manager = employeeDAO.getEmployeeByUserId(user.getUserId());
            if (manager == null || manager.getDepartmentId() != employee.getDepartmentId()) {
                request.getSession().setAttribute("error", "Bạn không có quyền xem nhân viên này.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/my-department-employees");
                return;
            }
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);

        request.setAttribute("employee", employee);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/employee_detail.jsp").forward(request, response);
    }

    private void displayDepartmentEmployeesById(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!isHrManager(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        if (!hasPermission(user, "VIEW_DEPARTMENT_EMPLOYEES_DETAIL")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem nhân viên của phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        String idParam = request.getParameter("id");
        if (isBlank(idParam)) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        int departmentId;
        try {
            departmentId = Integer.parseInt(idParam.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        Department dept = departmentDAO.getDepartmentById(departmentId);
        if (dept == null) {
            request.getSession().setAttribute("deptError", "Phòng ban không tồn tại.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);

        List<EmployeeDetailDTO> employees = employeeDAO.getEmployeesByDepartmentId(departmentId);
        request.setAttribute("departmentName", dept.getDepartmentName());
        request.setAttribute("employees", employees);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/employee_list.jsp").forward(request, response);
    }

    // ===== Quản lý manager của phòng ban (Assign MGR / Gỡ MGR) =====

    private void displayAssignManagerForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!isHrManager(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        if (!hasPermission(user, "ASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("deptError", "Bạn không có quyền phân công manager.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        String idParam = request.getParameter("id");
        if (isBlank(idParam)) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }
        int deptId;
        try {
            deptId = Integer.parseInt(idParam.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        Department dept = departmentDAO.getDepartmentById(deptId);
        if (dept == null) {
            request.getSession().setAttribute("deptError", "Phòng ban không tồn tại.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("dept", dept);
        request.setAttribute("currentManager", departmentDAO.getCurrentManager(deptId));
        request.setAttribute("candidates", employeeDAO.getAllEmployees(user.getUserId()));
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/department_assign.jsp").forward(request, response);
    }

    private void handleAssignManager(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!isHrManager(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        if (!hasPermission(user, "ASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("deptError", "Bạn không có quyền phân công manager.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        String rawDept = request.getParameter("departmentId");
        String rawEmp = request.getParameter("employeeId");
        if (isBlank(rawDept) || isBlank(rawEmp)) {
            request.getSession().setAttribute("deptError", "Vui lòng chọn nhân viên làm manager.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        int departmentId, employeeId;
        try {
            departmentId = Integer.parseInt(rawDept);
            employeeId = Integer.parseInt(rawEmp);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("deptError", "Dữ liệu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        boolean ok = employeeDAO.assignAsManager(departmentId, employeeId);
        request.getSession().setAttribute(ok ? "deptSuccess" : "deptError",
                ok ? "Đã phân công manager cho phòng ban." : "Phân công manager thất bại.");
        response.sendRedirect(request.getContextPath() + "/v1/manager/department");
    }

    private void handleUnassignManager(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!isHrManager(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        if (!hasPermission(user, "ASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("deptError", "Bạn không có quyền gỡ manager.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        String rawDept = request.getParameter("departmentId");
        if (isBlank(rawDept)) {
            request.getSession().setAttribute("deptError", "Thiếu mã phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        int departmentId;
        try {
            departmentId = Integer.parseInt(rawDept);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("deptError", "Mã phòng ban không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        boolean ok = departmentDAO.unassignManager(departmentId);
        request.getSession().setAttribute(ok ? "deptSuccess" : "deptError",
                ok ? "Đã gỡ manager khỏi phòng ban." : "Gỡ manager thất bại.");
        response.sendRedirect(request.getContextPath() + "/v1/manager/department");
    }

    private void preventBackCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }


    private boolean isManagerRole(String roleName) {
        if (roleName == null) {
            return false;
        }
        String normalized = roleName.trim().toLowerCase();
        return normalized.contains("manager");
    }


    private boolean isHrManager(String roleName) {
        return "HRMANAGER".equalsIgnoreCase(roleName != null ? roleName.trim() : "");
    }



    private void displayDepartmentList(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!isHrManager(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        if (!hasPermission(user, "VIEW_DEPARTMENTS")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem danh sách phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);

        List<Department> departments = departmentDAO.getAllDepartments();

        // Build manager map: departmentId -> EmployeeDetailDTO
        Map<Integer, EmployeeDetailDTO> managerMap = new HashMap<>();
        for (Department d : departments) {
            if (d.getManagerId() != null) {
                EmployeeDetailDTO mgr = departmentDAO.getCurrentManager(d.getDepartmentId());
                if (mgr != null) managerMap.put(d.getDepartmentId(), mgr);
            }
        }

        request.setAttribute("departments", departments);
        request.setAttribute("managerMap", managerMap);

        // Flash messages set by redirect from assign/unassign
        HttpSession session = request.getSession(false);
        if (session != null) {
            String success = (String) session.getAttribute("deptSuccess");
            String error   = (String) session.getAttribute("deptError");
            if (success != null) { request.setAttribute("success", success); session.removeAttribute("deptSuccess"); }
            if (error   != null) { request.setAttribute("error",   error);   session.removeAttribute("deptError");   }
        }

        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/department_list.jsp").forward(request, response);
    }

    private void displayAssignDepartmentForm(HttpServletRequest request, HttpServletResponse response,
                                              User user) throws ServletException, IOException {

        if (!isHrManager(user.getRoleName())) {
            request.getSession().setAttribute("error", "Bạn không có quyền phân công phòng ban bởi bạn không phải HR");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        if (!hasPermission(user, "ASSIGN_DEPARTMENT")) {
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

       private void displayReassignDepartmentForm(HttpServletRequest request, HttpServletResponse response,
                                                User user) throws ServletException, IOException {

        if (!isHrManager(user.getRoleName())) {
            request.getSession().setAttribute("error", "Bạn không có quyền chuyển phòng ban bởi bạn không phải HR");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        if (!hasPermission(user, "REASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền chuyển phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
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
        request.getRequestDispatcher("/public/employee/reassign_department.jsp").forward(request, response);
    }

  private void handleReassignDepartment(HttpServletRequest request, HttpServletResponse response,
                                           User user) throws ServletException, IOException {
        if (!isHrManager(user.getRoleName())) {
            request.getSession().setAttribute("error", "Bạn không có quyền chuyển phòng ban bởi bạn không phải HR");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        if (!hasPermission(user, "REASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền chuyển phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
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
        response.sendRedirect(request.getContextPath() + "/v1/employee/department-detail?id=" + departmentId);
    }

    private void handleUnassignDepartment(HttpServletRequest request, HttpServletResponse response,
                                           User user) throws ServletException, IOException {
        if (!isHrManager(user.getRoleName())) {
            request.getSession().setAttribute("error", "Bạn không có quyền gỡ phân công bởi bạn không phải HR");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        if (!hasPermission(user, "REASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền gỡ phân công.");
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
            request.getSession().setAttribute("error", "Nhân viên không hợp lệ hoặc chưa được phân công phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/employee-detail?id=" + employeeId);
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
        response.sendRedirect(request.getContextPath() + "/v1/employee/employee-detail?id=" + employeeId);
    }
    private void reloadReassignFormWithError(HttpServletRequest request, HttpServletResponse response,
                                              User user, String message) throws ServletException, IOException {
        request.setAttribute("error", message);
        request.setAttribute("assignedEmployees", employeeDAO.getAssignedEmployees(user.getUserId()));
        request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
        request.setAttribute("positions", departmentDAO.getAllPositions());
        setPermissionFlags(request, getPermissions(user));
        request.getRequestDispatcher("/public/employee/reassign_department.jsp").forward(request, response);
    }

       private void handleAssignDepartment(HttpServletRequest request, HttpServletResponse response,
                                         User user) throws ServletException, IOException {
        if (!isHrManager(user.getRoleName())) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm phòng ban bởi bạn không phải HR");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        if (!hasPermission(user, "ASSIGN_DEPARTMENT")) {
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
            request.setAttribute("error",  "Vui lòng chọn đầy đủ nhân viên, phòng ban và vị trí.");
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

        // Thiết lập quan hệ quản lý sau khi phân công:
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

        request.getSession().setAttribute("deptSuccess", "Phân công nhân viên vào phòng ban thành công.");
        response.sendRedirect(request.getContextPath() + "/v1/manager/department");
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

    @SuppressWarnings("unchecked")
    private Set<String> getPermissions(User user) {
        Set<String> hs = permissionDAO.getPermissionCodeByUserId(user.getUserId());
        return hs;
    }

    private boolean hasPermission(User user, String code) {
        return getPermissions(user).contains(code);
    }

    private void setPermissionFlags(HttpServletRequest request, Set<String> perms) {
        request.setAttribute("canViewEmployees",   perms.contains("VIEW_EMPLOYEES"));
        request.setAttribute("canAddEmployee",     perms.contains("ADD_EMPLOYEE"));
        request.setAttribute("canEditEmployee",    perms.contains("EDIT_EMPLOYEE"));
        request.setAttribute("canDeleteEmployee",  perms.contains("DELETE_EMPLOYEE"));
        request.setAttribute("canViewDepartments", perms.contains("VIEW_DEPARTMENTS"));
        request.setAttribute("canEditDepts",       perms.contains("EDIT_DEPARTMENTS"));
        request.setAttribute("canAssignDept",      perms.contains("ASSIGN_DEPARTMENT"));
    }
}
