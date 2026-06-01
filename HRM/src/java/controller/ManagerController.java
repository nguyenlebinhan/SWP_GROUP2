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
            case "/employee-detail":
                displayEmployeeDetail(request, response, user);
                break;
            case "/department":
                displayDepartmentList(request, response, user);
                break;
            case "/department/assign":
                displayAssignPage(request, response, user);
                break;
            case "/department/assign-employee":
                displayAssignEmployeePage(request, response, user);
                break;
            case "/department/employees":
                displayDepartmentEmployeesById(request, response, user);
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
            case "/department/assign":
                handleAssignManager(request, response, user);
                break;
            case "/department/assign-employee":
                handleAssignEmployee(request, response, user);
                break;
            case "/department/unassign":
                handleUnassignManager(request, response, user);
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

    private void displayEmployeeList(HttpServletRequest request, HttpServletResponse response,
                                      User user) throws ServletException, IOException {
        if (!hasPermission(user, "VIEW_EMPLOYEES")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem danh sách nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
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

        if (isHrManager(user.getRoleName())) {
            int totalEmployees = employeeDAO.countEmployeesFiltered(user.getUserId(), null, keyword, dept, status);
            int totalPages = (int) Math.ceil((double) totalEmployees / PAGE_SIZE);
            if (totalPages < 1) totalPages = 1;

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
        } else {
            EmployeeDetailDTO manager = employeeDAO.getEmployeeByUserId(user.getUserId());
            if (manager == null) {
                request.setAttribute("departmentName", null);
                request.setAttribute("employees", java.util.Collections.emptyList());
                request.setAttribute("error", "Bạn chưa được phân công vào phòng ban nào.");
                setPermissionFlags(request, perms);
                request.getRequestDispatcher("/public/manager/employee_list.jsp").forward(request, response);
                return;
            }

            int totalEmployees = employeeDAO.countEmployeesFiltered(null, manager.getDepartmentId(), keyword, null, status);
            int totalPages = (int) Math.ceil((double) totalEmployees / PAGE_SIZE);
            if (totalPages < 1) totalPages = 1;

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
    }

    private void displayEmployeeDetail(HttpServletRequest request, HttpServletResponse response,
                                       User user) throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (isBlank(idParam)) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee-list");
            return;
        }

        int employeeId;
        try {
            employeeId = Integer.parseInt(idParam.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee-list");
            return;
        }

        EmployeeDetailDTO employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null) {
            request.getSession().setAttribute("error", "Không tìm thấy nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/employee-list");
            return;
        }

        // Department Manager chỉ được xem nhân viên phòng ban mình
        if (!isHrManager(user.getRoleName())) {
            EmployeeDetailDTO manager = employeeDAO.getEmployeeByUserId(user.getUserId());
            if (manager == null || manager.getDepartmentId() != employee.getDepartmentId()) {
                request.getSession().setAttribute("error", "Bạn không có quyền xem nhân viên này.");
                response.sendRedirect(request.getContextPath() + "/v1/manager/employee-list");
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

    private void preventBackCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }


    private boolean isManagerRole(String roleName) {
        return roleName != null && roleName.trim().toLowerCase().contains("manager");
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

    private void displayAssignPage(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!isHrManager(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        if (!hasPermission(user, "ASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền phân công phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }
        int departmentId;
        try {
            departmentId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        Department dept = departmentDAO.getDepartmentById(departmentId);
        if (dept == null || dept.getStatus() == 0) {
            request.getSession().setAttribute("deptError", "Phòng ban không tồn tại hoặc đã bị vô hiệu hóa.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);

        EmployeeDetailDTO currentManager = departmentDAO.getCurrentManager(departmentId);
        List<EmployeeDetailDTO> candidates = departmentDAO.getAssignableManagerDTOs(user.getUserId(), departmentId);

        request.setAttribute("dept", dept);
        request.setAttribute("currentManager", currentManager);
        request.setAttribute("candidates", candidates);

        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/department_assign.jsp").forward(request, response);
    }

    private void displayAssignEmployeePage(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!isHrManager(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        if (!hasPermission(user, "ASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền phân công phòng ban.");
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
        if (dept == null || dept.getStatus() == 0) {
            request.getSession().setAttribute("deptError", "Phòng ban không tồn tại hoặc đã bị vô hiệu hóa.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);

        List<EmployeeDetailDTO> candidates = employeeDAO.getAllEmployeesByDepartmentId(departmentId);

        request.setAttribute("dept", dept);
        request.setAttribute("candidates", candidates);

        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/manager/employee_assign.jsp").forward(request, response);
    }

    private void handleAssignManager(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (!isHrManager(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        if (!hasPermission(user, "ASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền phân công phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        String deptIdParam = request.getParameter("departmentId");
        String empIdParam  = request.getParameter("employeeId");

        if (isBlank(deptIdParam) || isBlank(empIdParam)) {
            request.getSession().setAttribute("deptError", "Dữ liệu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        int departmentId, employeeId;
        try {
            departmentId = Integer.parseInt(deptIdParam.trim());
            employeeId   = Integer.parseInt(empIdParam.trim());
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("deptError", "Dữ liệu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        Department dept = departmentDAO.getDepartmentById(departmentId);
        if (dept == null || dept.getStatus() == 0) {
            request.getSession().setAttribute("deptError", "Phòng ban không tồn tại.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        // Server-side guard: kiểm tra candidate hợp lệ, không chỉ dựa vào UI
        List<EmployeeDetailDTO> candidates = departmentDAO.getAssignableManagerDTOs(user.getUserId(), departmentId);
        boolean isValid = candidates.stream().anyMatch(c -> c.getEmployeeId() == employeeId);
        if (!isValid) {
            request.getSession().setAttribute("deptError", "Nhân viên này không đủ điều kiện làm quản lý.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department/assign?id=" + departmentId);
            return;
        }

        boolean ok = departmentDAO.assignManager(departmentId, employeeId);
        if (ok) {
            request.getSession().setAttribute("deptSuccess",
                    "Assign manager thành công cho phòng ban " + dept.getDepartmentName() + ".");
        } else {
            request.getSession().setAttribute("deptError", "Assign thất bại. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/manager/department");
    }

    private void handleUnassignManager(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (!isHrManager(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        if (!hasPermission(user, "ASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền phân công phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        String deptIdParam = request.getParameter("departmentId");

        if (isBlank(deptIdParam)) {
            request.getSession().setAttribute("deptError", "Dữ liệu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        int departmentId;
        try {
            departmentId = Integer.parseInt(deptIdParam.trim());
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("deptError", "Dữ liệu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        Department dept = departmentDAO.getDepartmentById(departmentId);
        if (dept == null) {
            request.getSession().setAttribute("deptError", "Phòng ban không tồn tại.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        boolean ok = departmentDAO.unassignManager(departmentId);
        if (ok) {
            request.getSession().setAttribute("deptSuccess",
                    "Đã gỡ manager khỏi phòng ban " + dept.getDepartmentName() + ".");
        } else {
            request.getSession().setAttribute("deptError", "Gỡ manager thất bại. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/manager/department");
    }

    private void handleAssignEmployee(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (!isHrManager(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }
        if (!hasPermission(user, "ASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền phân công phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
            return;
        }

        String deptIdParam = request.getParameter("departmentId");
        String empIdParam  = request.getParameter("employeeId");

        if (isBlank(deptIdParam) || isBlank(empIdParam)) {
            request.getSession().setAttribute("deptError", "Dữ liệu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        int departmentId, employeeId;
        try {
            departmentId = Integer.parseInt(deptIdParam.trim());
            employeeId   = Integer.parseInt(empIdParam.trim());
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("deptError", "Dữ liệu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        Department dept = departmentDAO.getDepartmentById(departmentId);
        if (dept == null || dept.getStatus() == 0) {
            request.getSession().setAttribute("deptError", "Phòng ban không tồn tại.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
            return;
        }

        boolean ok = employeeDAO.updateEmployeeDepartment(employeeId, departmentId);
        if (ok) {
            request.getSession().setAttribute("success", 
                    "Assign nhân viên thành công cho phòng ban " + dept.getDepartmentName() + ".");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department");
        } else {
            request.getSession().setAttribute("deptError", "Assign thất bại. Vui lòng thử lại.");
            response.sendRedirect(request.getContextPath() + "/v1/manager/department/assign-employee?id=" + departmentId);
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
