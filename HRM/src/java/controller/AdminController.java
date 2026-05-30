/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.*;
import dto.EmployeeDetailDTO;
import dto.UserUpdateRequestDTO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.logging.*;
import model.*;
import service.*;
import java.util.*;

/**
 *
 * @author ADMIN
 */
public class AdminController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminController.class.getName());
    private static final UserDAO userDAO = new UserDAO();
    private static final EmailService emailService = new EmailService();
    private static final RoleDAO roleDAO = new RoleDAO();
    private static final PermissionDAO permissionDAO = new PermissionDAO();
    private static final EmployeeDAO employeeDAO = new EmployeeDAO();
    private static final DepartmentDAO departmentDAO = new DepartmentDAO();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {

            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet AdminController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet AdminController at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        preventBackCache(response);
        String action = request.getPathInfo();

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || user.getRoleName() == null || !"ADMIN".equalsIgnoreCase(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/v1/auth/login");
            return;
        }
        if (action == null || action.equals("/")) {
            displayDashboard(request, response);
            return;
        }
        switch (action) {
            case "/dashboard":
                displayDashboard(request, response);
                break;
            case "/user-list":
                displayUserList(request, response);
                break;
            case "/add-user":
                displayAddUserForm(request, response);
                break;
            case "/update-user":
                displayUpdateUserForm(request, response);
                break;
            case "/view-user-detail":
                displayUserDetail(request, response);
                break;
            case "/my-profile":
                displayMyProfile(request, response);
                break;
            case "/role-list":
                displayRoleList(request, response);
                break;
            case "/role-detail":
                displayRoleDetail(request, response);
                break;
            case "/update-role":
                displayUpdateRoleForm(request, response);
                break;
            case "/delete-role":
                handleDeleteRole(request, response);
                break;
            case "/add-role":
                displayAddRoleForm(request, response);
                break;
            case "/edit-role-permissions":
                displayEditRolePermissionsForm(request, response);
                break;
            case "/change-status":
                handleChangingStatus(request, response, user);
                break;
            case "/change-status-role":
                handleChangingStatusRole(request, response);
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

        if (user == null || user.getRoleName() == null || !"ADMIN".equalsIgnoreCase(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/v1/auth/login");
            return;
        }
        if (action == null || action.equals("/")) {
            displayDashboard(request, response);
            return;
        }
        switch (action) {
            case "/add-user":
                handleAddUser(request, response);
                break;
            case "/update-user":
                handleUpdateUserInfo(request, response);
                break;
            case "/my-profile":
                handleUpdateMyProfile(request, response, user);
                break;
            case "/update-role":
                handleUpdateRole(request, response);
                break;
            case "/add-role":
                handleAddRole(request, response);
                break;
            case "/edit-role-permissions":
                handleUpdateRolePermissions(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/");
                break;
        }
    }

    private void displayAddUserForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Role> roles = roleDAO.getAllActiveRoles();
        request.setAttribute("roles", roles);
        request.getRequestDispatcher("/public/admin/add_user.jsp").forward(request, response);
    }

    private void displayUpdateUserForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Role> roles = roleDAO.getAllActiveRoles();
        int userId = Integer.parseInt(request.getParameter("id"));
        UserUpdateRequestDTO user = userDAO.getUserDTOById(userId);
        request.setAttribute("userId", userId);
        request.setAttribute("roles", roles);
        request.setAttribute("user", user);
        request.getRequestDispatcher("/public/admin/update_user.jsp").forward(request, response);
    }

    private void displayUserList(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("keyword") != null
                ? request.getParameter("keyword").trim() : "";
        String role = request.getParameter("role") != null
                ? request.getParameter("role").trim() : "";

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

        // countUsers chỉ chạy COUNT(*) — rất nhẹ dù có nghìn người
        int totalUsers = userDAO.countUsers(keyword, role);
        int totalPages = (int) Math.ceil((double) totalUsers / PAGE_SIZE);
        if (totalPages < 1) {
            totalPages = 1;
        }

        // getUsersFiltered chỉ lấy đúng 5 người của trang hiện tại
        List<User> list = userDAO.getUsersFiltered(keyword, role, offset, PAGE_SIZE);

        List<Role> roles = roleDAO.getAllRoles();

        request.setAttribute("list", list);
        request.setAttribute("roles", roles);
        request.setAttribute("keyword", keyword);
        request.setAttribute("role", role);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalUsers", totalUsers);

        request.getRequestDispatcher("/public/admin/user_list.jsp")
                .forward(request, response);
    }

// Sửa dashboard: dùng COUNT thay vì lấy toàn bộ list
    private void displayDashboard(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        // Thay vì getAllUsers() rồi .size(), chỉ đếm trực tiếp
        int userSize = userDAO.countUsers("", "");
        request.setAttribute("userSize", userSize);
        request.getRequestDispatcher("/public/admin/dashboard.jsp")
                .forward(request, response);
    }

    private void displayUserDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String rawUserId = request.getParameter("id");
        if (rawUserId == null || rawUserId.trim().isEmpty()) {
            request.setAttribute("error", "Khhông thể hiển thị");
            request.getRequestDispatcher("/public/admin/user_detail.jsp").forward(request, response);
        }

        int userId = Integer.parseInt(rawUserId);

        User selectedUser = userDAO.getUserById(userId);
        if (selectedUser == null) {
            request.setAttribute("error", "User not found.");
        }
        request.setAttribute("selectedUser", selectedUser);
        request.getRequestDispatcher("/public/admin/user_detail.jsp").forward(request, response);
    }

    private void handleAddUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String fullName = request.getParameter("fullName");
        String dob = request.getParameter("dob");
        String gender = request.getParameter("gender");
        String address = request.getParameter("address");
        int roleId = Integer.parseInt(request.getParameter("role_selection"));

        boolean isSuccess = userDAO.addUserAndEmpployee(username, email, password, fullName, dob, gender, address, roleId);
        if (!isSuccess) {
            request.setAttribute("roles", roleDAO.getAllRoles());
            request.setAttribute("error", "Thêm người dùng thất bại. Email hoặc username có thể đã tồn tại.");
            request.getRequestDispatcher("/public/admin/add_user.jsp").forward(request, response);
            return;
        }
        emailService.sendResetPasswordEmailAsync(email, password);
        LOGGER.log(Level.INFO, "User added and password sent to: {0}", email);
        request.getSession().setAttribute("success", "Thêm người dùng thành công. Mật khẩu tạm thời đã được gửi đến email.");
        response.sendRedirect(request.getContextPath() + "/v1/admin/user-list");
    }

    private void handleUpdateUserInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userId = Integer.parseInt(request.getParameter("userId"));
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String fullName = request.getParameter("fullName");
        String dob = request.getParameter("dob");
        String gender = request.getParameter("gender");
        String address = request.getParameter("address");
        int roleId = Integer.parseInt(request.getParameter("role_selection"));

        // Chặn chiều ngược (đối xứng với assign-department): nếu user đã là nhân viên
        // thuộc một phòng ban, vai trò mới phải hợp lệ với phòng ban đó.
        EmployeeDetailDTO emp = employeeDAO.getEmployeeByUserId(userId);
        if (emp != null && !departmentDAO.isRoleAllowedForDepartment(emp.getDepartmentId(), roleId)) {
            List<String> allowed = departmentDAO.getAllowedRoleNames(emp.getDepartmentId());
            request.setAttribute("error", "Vai trò mới không phù hợp với phòng \"" + emp.getDepartmentName()
                    + "\" mà nhân viên đang thuộc. Phòng này chỉ nhận: " + String.join(", ", allowed)
                    + ". Hãy chuyển nhân viên sang phòng phù hợp trước khi đổi vai trò.");
            request.setAttribute("userId", userId);
            request.setAttribute("user", userDAO.getUserDTOById(userId));
            request.setAttribute("roles", roleDAO.getAllActiveRoles());
            request.getRequestDispatcher("/public/admin/update_user.jsp").forward(request, response);
            return;
        }

        boolean isSuccess = userDAO.updateUser(userId, username, email, password, fullName, dob, gender, address, roleId);
        if (!isSuccess) {
            request.setAttribute("roles", roleDAO.getAllRoles());
            request.setAttribute("error", "Cập nhật người dùng thất bại. Email có thể đã tồn tại.");
            request.getRequestDispatcher("/public/admin/update_user.jsp").forward(request, response);
            return;
        }
        emailService.sendResetPasswordEmailAsync(email, password);
        LOGGER.log(Level.INFO, "User updated  and password sent to: {0}", email);
        request.getSession().setAttribute("success", "Cập nhật người dùng thành công. Mật khẩu tạm thời đã được gửi đến email.");
        response.sendRedirect(request.getContextPath() + "/v1/admin/user-list");
    }

    private void handleChangingStatus(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        int status = Integer.parseInt(request.getParameter("status"));
        int userId = Integer.parseInt(request.getParameter("id"));
        if (userId == user.getUserId()) {
            request.getSession().setAttribute("error", "Không thể tự activate/deactive bản thân");
            response.sendRedirect(request.getContextPath() + "/v1/admin/user-list");
            return;
        }
        boolean isUpdated = userDAO.handleStatus(status, userId);

        if (isUpdated) {
            request.getSession().setAttribute("success", "Cập nhật trạng thái thành công");
        } else {
            request.getSession().setAttribute("error", "Cập nhật trạng thái không thành công");
        }
        response.sendRedirect(request.getContextPath() + "/v1/admin/user-list");
    }

    private void handleChangingStatusRole(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int status = Integer.parseInt(request.getParameter("status"));
        int roleId = Integer.parseInt(request.getParameter("id"));
        boolean isUpdated = roleDAO.handleStatus(status, roleId);
        if (isUpdated) {
            request.getSession().setAttribute("success", "Cập nhật trạng thái vai trò thành công");
        } else {
            request.getSession().setAttribute("error", "Cập nhật trạng thái vai trò không thành công");
        }
        response.sendRedirect(request.getContextPath() + "/v1/admin/role-list");
    }

    private void displayUpdateRoleForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String rawRoleId = request.getParameter("id");
        if (rawRoleId == null || rawRoleId.trim().isEmpty()) {
            request.setAttribute("error", "Không thể xác định vai trò cần cập nhật");
            request.getRequestDispatcher("/public/admin/update_role.jsp").forward(request, response);
            return;
        }

        int roleId;
        try {
            roleId = Integer.parseInt(rawRoleId);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Mã vai trò không hợp lệ");
            request.getRequestDispatcher("/public/admin/update_role.jsp").forward(request, response);
            return;
        }

        Role selectedRole = roleDAO.getRoleById(roleId);
        if (selectedRole == null) {
            request.setAttribute("error", "Không tìm thấy vai trò");
            request.getRequestDispatcher("/public/admin/update_role.jsp").forward(request, response);
            return;
        }

        request.setAttribute("selectedRole", selectedRole);
        request.getRequestDispatcher("/public/admin/update_role.jsp").forward(request, response);
    }

    private void handleUpdateRole(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String rawRoleId = request.getParameter("roleId");
        String roleCode = request.getParameter("roleCode").trim();
        String roleName = request.getParameter("roleName").trim();
        String description = request.getParameter("description").trim();

        if (isBlank(roleCode) || isBlank(roleName)) {
            Role fallback = null;
            try {
                fallback = roleDAO.getRoleById(Integer.parseInt(rawRoleId));
            } catch (Exception ignored) {
            }
            request.setAttribute("error", "Mã vai trò và tên vai trò không được để trống");
            request.setAttribute("selectedRole", fallback);
            request.getRequestDispatcher("/public/admin/update_role.jsp").forward(request, response);
            return;
        }

        if (isBlank(rawRoleId)) {
            request.getSession().setAttribute("error", "Thiếu mã vai trò");
            response.sendRedirect(request.getContextPath() + "/v1/admin/role-list");
            return;
        }

        int roleId;
        try {
            roleId = Integer.parseInt(rawRoleId);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã vai trò không hợp lệ");
            response.sendRedirect(request.getContextPath() + "/v1/admin/role-list");
            return;
        }

        description = isBlank(description) ? null : description.trim();

        boolean updated = roleDAO.updateRole(roleId, roleCode, roleName, description);
        if (!updated) {
            Role fallback = roleDAO.getRoleById(roleId);
            request.setAttribute("error", "Cập nhật vai trò thất bại. Mã vai trò có thể đã tồn tại.");
            request.setAttribute("selectedRole", fallback);
            request.getRequestDispatcher("/public/admin/update_role.jsp").forward(request, response);
            return;
        }

        LOGGER.log(Level.INFO, "Role updated successfully: roleId={0}", roleId);
        request.getSession().setAttribute("success", "Cập nhật vai trò thành công");
        response.sendRedirect(request.getContextPath() + "/v1/admin/role-list");
    }

    private void handleDeleteRole(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String rawRoleId = request.getParameter("id");
        if (isBlank(rawRoleId)) {
            request.getSession().setAttribute("error", "Thiếu mã vai trò");
            response.sendRedirect(request.getContextPath() + "/v1/admin/role-list");
            return;
        }

        int roleId;
        try {
            roleId = Integer.parseInt(rawRoleId);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã vai trò không hợp lệ");
            response.sendRedirect(request.getContextPath() + "/v1/admin/role-list");
            return;
        }
        boolean deleted = roleDAO.deleteRole(roleId);
        if (!deleted) {
            request.getSession().setAttribute("error", "Xóa vai trò thất bại");
            response.sendRedirect(request.getContextPath() + "/v1/admin/role-detail?id=" + roleId);
            return;
        }
        LOGGER.log(Level.INFO, "Role deleted successfully: roleId={0}", roleId);
        request.getSession().setAttribute("success", "Xóa vai trò thành công");
        response.sendRedirect(request.getContextPath() + "/v1/admin/role-list");
    }

    private void displayAddRoleForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/public/admin/add_role.jsp").forward(request, response);
    }

    private void handleAddRole(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String roleCode = request.getParameter("roleCode");
        String roleName = request.getParameter("roleName");
        String description = request.getParameter("description");

        if (isBlank(roleCode) || isBlank(roleName)) {
            request.setAttribute("error", "Mã vai trò và tên vai trò không được để trống");
            request.getRequestDispatcher("/public/admin/add_role.jsp").forward(request, response);
            return;
        }

        roleCode = roleCode.trim().toUpperCase();
        roleName = roleName.trim();
        description = isBlank(description) ? null : description.trim();

        boolean added = roleDAO.addRole(roleCode, roleName, description);
        if (!added) {
            request.setAttribute("error", "Thêm vai trò thất bại. Mã vai trò có thể đã tồn tại.");
            request.getRequestDispatcher("/public/admin/add_role.jsp").forward(request, response);
            return;
        }

        LOGGER.log(Level.INFO, "Role added successfully: roleCode={0}", roleCode);
        request.getSession().setAttribute("success", "Thêm vai trò thành công");
        response.sendRedirect(request.getContextPath() + "/v1/admin/role-list");
    }

    private void displayRoleList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Role> roles = roleDAO.getAllRoles();
        Map<Integer, Integer> userCounts = new HashMap<>();
        Map<Integer, Integer> permissionCounts = new HashMap<>();
        int activeRoleCount = 0;
        int totalUserAssignments = 0;
        int totalPermissionAssignments = 0;

        for (Role role : roles) {
            if (role.getIsActive() == 1) {
                activeRoleCount++;
            }
            int userCount = roleDAO.countUsersByRoleId(role.getRoleId());
            int permissionCount = roleDAO.countPermissionsByRoleId(role.getRoleId());
            userCounts.put(role.getRoleId(), userCount);
            permissionCounts.put(role.getRoleId(), permissionCount);
            totalUserAssignments += userCount;
            totalPermissionAssignments += permissionCount;
        }

        request.setAttribute("roles", roles);
        request.setAttribute("userCounts", userCounts);
        request.setAttribute("permissionCounts", permissionCounts);
        request.setAttribute("activeRoleCount", activeRoleCount);
        request.setAttribute("totalUserAssignments", totalUserAssignments);
        request.setAttribute("totalPermissionAssignments", totalPermissionAssignments);
        request.getRequestDispatcher("/public/admin/role_list.jsp").forward(request, response);
    }

    private void displayRoleDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String rawRoleId = request.getParameter("id");
        if (rawRoleId == null || rawRoleId.trim().isEmpty()) {
            request.setAttribute("error", "Không thể hiển thị vai trò");
            request.getRequestDispatcher("/public/admin/role_detail.jsp").forward(request, response);
            return;
        }

        int roleId = Integer.parseInt(rawRoleId);

        Role selectedRole = roleDAO.getRoleById(roleId);
        if (selectedRole == null) {
            request.setAttribute("error", "Không tìm thấy vai trò");
            request.getRequestDispatcher("/public/admin/role_detail.jsp").forward(request, response);
            return;
        }

        Set<Permission> assignedPerms = permissionDAO.getAllPermissionByRoleId(roleId);
        Set<Integer> assignedIds = new HashSet<>();
        for (model.Permission p : assignedPerms) {
            assignedIds.add(p.getPermissionId());
        }

        request.setAttribute("selectedRole", selectedRole);
        request.setAttribute("allPermissions", permissionDAO.getAllPermissions());
        request.setAttribute("assignedPermissionIds", assignedIds);
        request.setAttribute("assignedCount", assignedPerms.size());
        request.setAttribute("roleUsers", userDAO.getUsersByRoleId(roleId));
        request.getRequestDispatcher("/public/admin/role_detail.jsp").forward(request, response);
    }

    private void displayMyProfile(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User sessionUser = (session != null) ? (User) session.getAttribute("user") : null;
        if (sessionUser == null) {
            response.sendRedirect(request.getContextPath() + "/v1/auth/login?required=1");
            return;
        }

        User currentUser = userDAO.getUserById(sessionUser.getUserId());
        request.setAttribute("currentUser", currentUser);
        request.getRequestDispatcher("/public/admin/my_profile.jsp").forward(request, response);
    }

    private void handleUpdateMyProfile(HttpServletRequest request, HttpServletResponse response, User sessionUser) throws ServletException, IOException {
        String username = request.getParameter("username");
        String fullName = request.getParameter("fullName");
        String dob = request.getParameter("dob");
        String address = request.getParameter("address");

        if (isBlank(username) || isBlank(fullName)) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ tên đăng nhập và họ tên");
            request.setAttribute("currentUser", userDAO.getUserById(sessionUser.getUserId()));
            request.getRequestDispatcher("/public/admin/my_profile.jsp").forward(request, response);
            return;
        }

        username = username.trim();
        fullName = fullName.trim();
        dob = isBlank(dob) ? null : dob.trim();
        address = isBlank(address) ? null : address.trim();

        if (userDAO.isUsernameExists(username, sessionUser.getUserId())) {
            request.setAttribute("error", "Tên đăng nhập đã tồn tại");
            request.setAttribute("currentUser", userDAO.getUserById(sessionUser.getUserId()));
            request.getRequestDispatcher("/public/admin/my_profile.jsp").forward(request, response);
            return;
        }

        boolean updated = userDAO.updateMyProfile(sessionUser.getUserId(), username, fullName, dob, address);
        if (!updated) {
            request.setAttribute("error", "Cập nhật hồ sơ thất bại. Vui lòng thử lại");
            request.setAttribute("currentUser", userDAO.getUserById(sessionUser.getUserId()));
            request.getRequestDispatcher("/public/admin/my_profile.jsp").forward(request, response);
            return;
        }

        User updatedUser = userDAO.getUserById(sessionUser.getUserId());
        request.getSession().setAttribute("user", updatedUser);
        request.getSession().setAttribute("success", "Cập nhật hồ sơ thành công");
        response.sendRedirect(request.getContextPath() + "/v1/admin/my-profile");
    }

    private void displayEditRolePermissionsForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String rawRoleId = request.getParameter("id");
        if (isBlank(rawRoleId)) {
            request.getSession().setAttribute("error", "Thiếu mã vai trò");
            response.sendRedirect(request.getContextPath() + "/v1/admin/role-list");
            return;
        }

        int roleId;
        try {
            roleId = Integer.parseInt(rawRoleId);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã vai trò không hợp lệ");
            response.sendRedirect(request.getContextPath() + "/v1/admin/role-list");
            return;
        }

        Role selectedRole = roleDAO.getRoleById(roleId);
        if (selectedRole == null) {
            request.getSession().setAttribute("error", "Không tìm thấy vai trò");
            response.sendRedirect(request.getContextPath() + "/v1/admin/role-list");
            return;
        }

        List<Permission> allPermissions = permissionDAO.getAllPermissions();
        Set<Permission> rolePermissions = permissionDAO.getAllPermissionByRoleId(roleId);

        Set<Integer> assignedIds = new HashSet<>();
        for (Permission p : rolePermissions) {
            assignedIds.add(p.getPermissionId());
        }

        request.setAttribute("selectedRole", selectedRole);
        request.setAttribute("allPermissions", allPermissions);
        request.setAttribute("assignedPermissionIds", assignedIds);
        request.getRequestDispatcher("/public/admin/edit_role_permissions.jsp").forward(request, response);
    }

    private void handleUpdateRolePermissions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String rawRoleId = request.getParameter("roleId");
        if (isBlank(rawRoleId)) {
            request.getSession().setAttribute("error", "Thiếu mã vai trò");
            response.sendRedirect(request.getContextPath() + "/v1/admin/role-list");
            return;
        }

        int roleId;
        try {
            roleId = Integer.parseInt(rawRoleId);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã vai trò không hợp lệ");
            response.sendRedirect(request.getContextPath() + "/v1/admin/role-list");
            return;
        }

        String[] permissionValues = request.getParameterValues("permissionIds");
        List<Integer> permissionIds = new ArrayList<>();
        if (permissionValues != null) {
            for (String val : permissionValues) {
                permissionIds.add(Integer.parseInt(val));
            }
        }

        boolean updated = permissionDAO.updateRolePermissions(roleId, permissionIds);
        if (!updated) {
            request.getSession().setAttribute("error", "Cập nhật quyền thất bại. Vui lòng thử lại.");
        } else {
            LOGGER.log(Level.INFO, "Role permissions updated: roleId={0}, count={1}", new Object[]{roleId, permissionIds.size()});
            request.getSession().setAttribute("success", "Cập nhật quyền cho vai trò thành công");
        }
        response.sendRedirect(request.getContextPath() + "/v1/admin/role-list");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void preventBackCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

}
