package controller;

import dao.DepartmentDAO;
import dao.EmployeeDAO;
import dao.PermissionDAO;
import dao.RoleDAO;
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
    private static final RoleDAO roleDAO = new RoleDAO();

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
            case "/department-detail":
                displayEmployeeDepartmentDetail(request, response, user);
                break;
            case "/assign-department":
                displayAssignDepartmentForm(request, response, user);
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
            case "/add-department":
                handleAddDepartment(request, response, user);
                break;
            case "/update-department":
                handleUpdateDepartment(request, response, user);
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
            request.getSession().setAttribute("error", "Bạn không có quyền xem danh sách nhân viên.");
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
        List<EmployeeDetailDTO> employees = employeeDAO.getAllEmployees(user.getUserId());
        request.setAttribute("employees", employees);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/employee_list.jsp").forward(request, response);
    }

    private void displayEmployeeDepartmentDetail(HttpServletRequest request, HttpServletResponse response,
                                                  User user) throws ServletException, IOException {
        
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem danh sách nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;            
        }        
        if (!hasPermission(user, "VIEW_DEPARTMENT_EMPLOYEES_DETAIL")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem nhân viên của phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        String rawDepartmentId = request.getParameter("id");
        if (isBlank(rawDepartmentId)) {
            request.getSession().setAttribute("error", "Thiếu mã phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/department-list");
            return;
        }

        int departmentId;
        try {
            departmentId = Integer.parseInt(rawDepartmentId);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã phòng ban không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/department-list");
            return;
        }

        Department department = departmentDAO.getDepartmentById(departmentId);
        if (department == null) {
            request.getSession().setAttribute("error", "Không tìm thấy phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/department-list");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        List<EmployeeDetailDTO> employees = employeeDAO.getEmployeesByDepartmentId(departmentId);
        request.setAttribute("department", department);
        request.setAttribute("employees", employees);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/department_employee_detail.jsp").forward(request, response);
    }

    private void displayAssignDepartmentForm(HttpServletRequest request, HttpServletResponse response,
                                              User user) throws ServletException, IOException {
        
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem danh sách nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;            
        }
        
        if (!hasPermission(user, "ASSIGN_DEPARTMENT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền phân công phòng ban.");
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
        request.getRequestDispatcher("/public/employee/assign_department.jsp").forward(request, response);
    }

    private void displayDepartmentList(HttpServletRequest request, HttpServletResponse response,
                                        User user) throws ServletException, IOException {
        
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem danh sách phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;            
        }
        
        if (!hasPermission(user, "VIEW_DEPARTMENTS")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem danh sách phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

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
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm phòng ban.");
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


    private void displayAddDepartmentForm(HttpServletRequest request, HttpServletResponse response,
                                           User user) throws ServletException, IOException {
        
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm phòng ban.");
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
        request.getRequestDispatcher("/public/employee/add_department.jsp").forward(request, response);
    }

    private void handleAddDepartment(HttpServletRequest request, HttpServletResponse response,
                                      User user) throws ServletException, IOException {
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
            request.getRequestDispatcher("/public/employee/add_department.jsp").forward(request, response);            
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
            request.getRequestDispatcher("/public/employee/add_department.jsp").forward(request, response);              
            return;
        }

        // Gắn luật vai trò cho phòng ban. Để trống = phòng ban nhận mọi vai trò.
        if (!roleIds.isEmpty()) {
            departmentDAO.replaceDepartmentRoles(newDeptId, roleIds);
        }

        LOGGER.log(Level.INFO, "Department created: code={0} by userId={1}", new Object[]{code, user.getUserId()});
        request.getSession().setAttribute("success", "Thêm phòng ban \"" + name.trim() + "\" thành công.");
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
            response.sendRedirect(request.getContextPath() + "/v1/employee/department-list");
            return;
        }
        int deptId;
        try {
            deptId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/department-list");
            return;
        }
        Department dept = departmentDAO.getDepartmentById(deptId);
        if (dept == null) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/department-list");
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
        request.getRequestDispatcher("/public/employee/update_department.jsp").forward(request, response);
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
            response.sendRedirect(request.getContextPath() + "/v1/employee/update-department?id=" + (idParam != null ? idParam : ""));
            return;
        }
        
        int deptId;
        try {
            deptId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/department-list");
            return;
        }
        
        Department dept = departmentDAO.getDepartmentById(deptId);
        if (dept == null) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/department-list");
            return;
        }
        
        dept.setDepartmentName(name.trim());
        dept.setDescription(isBlank(description) ? null : description.trim());
        
        boolean success = departmentDAO.updateDepartmentInfo(dept);
        if (success) {
            departmentDAO.replaceDepartmentRoles(deptId, roleIds);
            request.getSession().setAttribute("success", "Cập nhật phòng ban thành công.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/department-list");
        } else {
            request.getSession().setAttribute("error", "Cập nhật thất bại. Vui lòng thử lại.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/update-department?id=" + deptId);
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

    private void repopulateAssignForm(HttpServletRequest request, HttpServletResponse response,
                                       User user, String errorMsg) throws ServletException, IOException {
        request.setAttribute("error", errorMsg);
        request.setAttribute("availableEmployees", employeeDAO.getEmployees(user.getUserId()));
        request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
        request.setAttribute("positions", departmentDAO.getAllPositions());
        setPermissionFlags(request, getPermissions(user));
        request.getRequestDispatcher("/public/employee/assign_department.jsp").forward(request, response);
    }

    @SuppressWarnings("unchecked")
    private Set<String> getPermissions(User user) {
        Set<String> hs = permissionDAO.getPermissionCodeByUserId(user.getUserId());
        return hs;
    }

    private boolean hasPermission(User user, String code) {
        return getPermissions(user).contains(code);
    }

    private boolean isHrStaff(User user) {
        String role = roleDAO.getRoleByUserId(user.getUserId());
        return "HREmployee".equalsIgnoreCase(role) || "HRManager".equalsIgnoreCase(role);
    }

    private void setPermissionFlags(HttpServletRequest request, Set<String> perms) {
        request.setAttribute("canViewEmployees",   perms.contains("VIEW_EMPLOYEES"));
        request.setAttribute("canAddEmployee",     perms.contains("ADD_EMPLOYEE"));
        request.setAttribute("canEditEmployee",    perms.contains("EDIT_EMPLOYEE"));
        request.setAttribute("canDeleteEmployee",  perms.contains("DELETE_EMPLOYEE"));
        request.setAttribute("canViewDepartments", perms.contains("VIEW_DEPARTMENTS"));
        request.setAttribute("canEditDepts",     perms.contains("EDIT_DEPARTMENTS"));
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
