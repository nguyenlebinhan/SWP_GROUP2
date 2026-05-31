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
                displayDashboard(request, response);
                break;
            case "/employee-list":
                displayEmployeeList(request, response, user);
                break;
            case "/employee-detail":
                displayEmployeeDetail(request, response, user);
                break;
            case "/department":
                // Chỉ HRMANAGER được quản lý phòng ban
                if (!isHrManager(user.getRoleName())) {
                    response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
                    return;
                }
                displayDepartmentList(request, response);
                break;
            case "/department/assign":
                if (!isHrManager(user.getRoleName())) {
                    response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
                    return;
                }
                displayAssignPage(request, response, user);
                break;
            case "/department/assign-employee":
                if (!isHrManager(user.getRoleName())) {
                    response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
                    return;
                }
                displayAssignEmployeePage(request, response, user);
                break;
            case "/department/employees":
                if (!isHrManager(user.getRoleName())) {
                    response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
                    return;
                }
                displayDepartmentEmployeesById(request, response);
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
            displayDashboard(request, response);
            return;
        }
        switch (action) {
            case "/department/assign":
                if (!isHrManager(user.getRoleName())) {
                    response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
                    return;
                }
                handleAssignManager(request, response, user);
                break;
            case "/department/assign-employee":
                if (!isHrManager(user.getRoleName())) {
                    response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
                    return;
                }
                handleAssignEmployee(request, response, user);
                break;
            case "/department/unassign":
                if (!isHrManager(user.getRoleName())) {
                    response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
                    return;
                }
                handleUnassignManager(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
                break;
        }
    }

    private void displayDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int totalEmployees = employeeDAO.countTotal();
        int activeEmployees = employeeDAO.countActive();
        int inactiveEmployees = employeeDAO.countInactive();
        Map<String, Integer> deptChart = employeeDAO.countByDepartment();

        request.setAttribute("totalEmployees", totalEmployees);
        request.setAttribute("activeEmployees", activeEmployees);
        request.setAttribute("inactiveEmployees", inactiveEmployees);
        request.setAttribute("pendingLeaves", 0);
        request.setAttribute("deptChart", deptChart);
        request.getRequestDispatcher("/public/manager/dashboard.jsp").forward(request, response);
    }

    private void displayEmployeeList(HttpServletRequest request, HttpServletResponse response,
                                      User user) throws ServletException, IOException {
        if (isHrManager(user.getRoleName())) {
            // HRMANAGER: xem tất cả nhân viên
            List<EmployeeDetailDTO> employees = employeeDAO.getAllEmployees(user.getUserId());
            request.setAttribute("employees", employees);
            request.getRequestDispatcher("/public/manager/employee_all_list.jsp").forward(request, response);
        } else {
            // Department Manager: chỉ xem nhân viên phòng ban mình
            EmployeeDetailDTO manager = employeeDAO.getEmployeeByUserId(user.getUserId());
            if (manager == null) {
                request.setAttribute("departmentName", null);
                request.setAttribute("employees", java.util.Collections.emptyList());
                request.setAttribute("error", "Bạn chưa được phân công vào phòng ban nào.");
                request.getRequestDispatcher("/public/manager/employee_list.jsp").forward(request, response);
                return;
            }
            List<EmployeeDetailDTO> employees = employeeDAO.getEmployeesByDepartmentId(manager.getDepartmentId());
            request.setAttribute("departmentName", manager.getDepartmentName());
            request.setAttribute("employees", employees);
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

        request.setAttribute("employee", employee);
        request.getRequestDispatcher("/public/manager/employee_detail.jsp").forward(request, response);
    }

    private void displayDepartmentEmployeesById(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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

        List<EmployeeDetailDTO> employees = employeeDAO.getEmployeesByDepartmentId(departmentId);
        request.setAttribute("departmentName", dept.getDepartmentName());
        request.setAttribute("employees", employees);
        request.getRequestDispatcher("/public/manager/employee_list.jsp").forward(request, response);
    }

    private void preventBackCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    /**
     * Kiểm tra role có chứa "manager" (không phân biệt hoa thường).
     * Cho phép tất cả các loại manager dùng chung controller này.
     */
    private boolean isManagerRole(String roleName) {
        return roleName != null && roleName.trim().toLowerCase().contains("manager");
    }

    /**
     * Kiểm tra role có phải HRMANAGER không.
     * Chỉ HRMANAGER mới được phép quản lý phòng ban (assign/unassign).
     */
    private boolean isHrManager(String roleName) {
        return "HRMANAGER".equalsIgnoreCase(roleName != null ? roleName.trim() : "");
    }

    // =========================================================
    // Department management methods
    // =========================================================

    private void displayDepartmentList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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

        request.getRequestDispatcher("/public/manager/department_list.jsp").forward(request, response);
    }

    private void displayAssignPage(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
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

        EmployeeDetailDTO currentManager = departmentDAO.getCurrentManager(departmentId);
        List<EmployeeDetailDTO> candidates = departmentDAO.getAssignableManagerDTOs(user.getUserId(), departmentId);

        request.setAttribute("dept", dept);
        request.setAttribute("currentManager", currentManager);
        request.setAttribute("candidates", candidates);

        request.getRequestDispatcher("/public/manager/department_assign.jsp").forward(request, response);
    }
    private void displayAssignEmployeePage(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
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

        List<EmployeeDetailDTO> candidates = employeeDAO.getAllEmployeesByDepartmentId(departmentId);

        request.setAttribute("dept", dept);
        request.setAttribute("candidates", candidates);

        request.getRequestDispatcher("/public/manager/employee_assign.jsp").forward(request, response);
    }

    private void handleAssignManager(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
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

    private void handleUnassignManager(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
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
}
