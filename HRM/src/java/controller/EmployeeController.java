package controller;

import dao.DepartmentDAO;
import dao.EmployeeDAO;
import dao.PermissionDAO;
import dao.UserDAO;
import dto.EmployeeDetailDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.*;

public class EmployeeController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(EmployeeController.class.getName());
    private static final EmployeeDAO employeeDAO = new EmployeeDAO();
    private static final DepartmentDAO departmentDAO = new DepartmentDAO();
    private static final UserDAO userDAO = new UserDAO();
    private static final PermissionDAO permissionDAO = new PermissionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        preventBackCache(response);

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/v1/auth/login");
            return;
        }

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
            case "/assign-department":
                displayAssignDepartmentForm(request, response, user);
                break;
            case "/department-list":
                displayDepartmentList(request, response, user);
                break;
            case "/my-profile":
                displayMyProfile(request, response, user);
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
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/v1/auth/login");
            return;
        }

        String action = request.getPathInfo();
        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        switch (action) {
            case "/assign-department":
                handleAssignDepartment(request, response, user);
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
        if (!hasPermission(user, "VIEW_EMPLOYEES")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem danh sách nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        Set<String> perms = getPermissions(user);
        List<EmployeeDetailDTO> employees = employeeDAO.getAllEmployees();
        request.setAttribute("employees", employees);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/employee_list.jsp").forward(request, response);
    }

    private void displayAssignDepartmentForm(HttpServletRequest request, HttpServletResponse response,
                                              User user) throws ServletException, IOException {
        if (!hasPermission(user, "ASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền phân công phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        Set<String> perms = getPermissions(user);
        List<User> availableUsers = employeeDAO.getUsersNotYetEmployees();
        List<Department> departments = departmentDAO.getAllActiveDepartments();
        List<Position> positions = departmentDAO.getAllPositions();

        request.setAttribute("availableUsers", availableUsers);
        request.setAttribute("departments", departments);
        request.setAttribute("positions", positions);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/assign_department.jsp").forward(request, response);
    }

    private void displayDepartmentList(HttpServletRequest request, HttpServletResponse response,
                                        User user) throws ServletException, IOException {
        if (!hasPermission(user, "VIEW_DEPARTMENTS")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem danh sách phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        Set<String> perms = getPermissions(user);
        List<Department> departments = departmentDAO.getAllDepartments();

        Map<Integer, Integer> empCounts = new HashMap<>();
        for (Department d : departments) {
            empCounts.put(d.getDepartmentId(), employeeDAO.countByDepartmentId(d.getDepartmentId()));
        }

        request.setAttribute("departments", departments);
        request.setAttribute("empCounts", empCounts);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/department_list.jsp").forward(request, response);
    }

    private void displayMyProfile(HttpServletRequest request, HttpServletResponse response,
                                   User sessionUser) throws ServletException, IOException {
        User currentUser = userDAO.getUserById(sessionUser.getUserId());
        request.setAttribute("currentUser", currentUser);
        request.getRequestDispatcher("/public/employee/my_profile.jsp").forward(request, response);
    }


    private void handleAssignDepartment(HttpServletRequest request, HttpServletResponse response,
                                         User user) throws ServletException, IOException {
        if (!hasPermission(user, "ASSIGN_DEPARTMENT")) {
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

        if (employeeDAO.isUserAlreadyEmployee(userId)) {
            repopulateAssignForm(request, response, user, "Người dùng này đã được ghi nhận là nhân viên rồi.");
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
            repopulateAssignForm(request, response, user, "Phân công thất bại. Vui lòng thử lại.");
            return;
        }

        LOGGER.log(Level.INFO, "Employee assigned: userId={0} → deptId={1}", new Object[]{userId, departmentId});
        request.getSession().setAttribute("success", "Phân công nhân viên vào phòng ban thành công.");
        response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
    }


    private void repopulateAssignForm(HttpServletRequest request, HttpServletResponse response,
                                       User user, String errorMsg) throws ServletException, IOException {
        request.setAttribute("error", errorMsg);
        request.setAttribute("availableUsers", employeeDAO.getUsersNotYetEmployees());
        request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
        request.setAttribute("positions", departmentDAO.getAllPositions());
        setPermissionFlags(request, getPermissions(user));
        request.getRequestDispatcher("/public/employee/assign_department.jsp").forward(request, response);
    }

    @SuppressWarnings("unchecked")
    private Set<String> getPermissions(User user) {
        Set<String> hs = permissionDAO.getPermissionCodesByUserId(user.getUserId());
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
        request.setAttribute("canManageDepts",     perms.contains("MANAGE_DEPARTMENTS"));
        request.setAttribute("canAssignDept",      perms.contains("ASSIGN_DEPARTMENT"));
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
