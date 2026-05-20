/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dal.DBContext;
import dao.*;
import dto.UserUpdateRequestDTO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
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
    private static final DBContext dbContext = new DBContext();
    private static final List<PermissionActionColumn> PERMISSION_ACTIONS = Collections.unmodifiableList(Arrays.asList(
            new PermissionActionColumn("view", "Xem"),
            new PermissionActionColumn("create", "T\u1ea1o"),
            new PermissionActionColumn("edit", "S\u1eeda"),
            new PermissionActionColumn("delete", "X\u00f3a")
    ));
    private static final List<FeatureDefinition> PERMISSION_FEATURES = Collections.unmodifiableList(Arrays.asList(
            new FeatureDefinition("employees", "Qu\u1ea3n l\u00fd nh\u00e2n vi\u00ean", 100,
                    "employee", "employees", "staff", "user", "users", "nhan vien", "nguoi dung", "tai khoan"),
            new FeatureDefinition("attendance", "Ch\u1ea5m c\u00f4ng", 200,
                    "attendance", "timekeeping", "timesheet", "cham cong"),
            new FeatureDefinition("leave", "Ngh\u1ec9 ph\u00e9p", 300,
                    "leave", "nghi phep", "xin nghi"),
            new FeatureDefinition("payroll", "B\u1ea3ng l\u01b0\u01a1ng", 400,
                    "payroll", "salary", "wage", "bang luong", "luong"),
            new FeatureDefinition("departments", "Ph\u00f2ng ban", 500,
                    "department", "departments", "phong ban", "phongban"),
            new FeatureDefinition("reports", "B\u00e1o c\u00e1o", 600,
                    "report", "reports", "analytics", "bao cao", "thong ke"),
            new FeatureDefinition("jobs", "C\u00f4ng vi\u1ec7c", 700,
                    "job", "jobs", "task", "tasks", "cong viec"),
            new FeatureDefinition("roles", "Ph\u00e2n quy\u1ec1n", 800,
                    "role", "roles", "permission", "permissions", "phan quyen", "vai tro")
    ));

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
            response.sendRedirect(request.getContextPath() + "/v1/auth/login?required=1");
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
                displayUserDetail(request,response);
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
            case "/role-permissions":
                displayRolePermissions(request, response);
                break;
            case "/change-status":
                handleChangingStatus(request, response,user);
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
            response.sendRedirect(request.getContextPath() + "/v1/auth/login?required=1");
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
                handleUpdateUserInfo(request,response);
                break;  
            case "/my-profile":
                handleUpdateMyProfile(request, response, user);
                break;
            case "/role-permissions":
                handleRolePermissions(request, response);
                break;
//            case "/change-status-role":
//                handleChangingStatusRole(request,response);
//                break;
            default:
                response.sendRedirect(request.getContextPath() + "/");
                break;
        }
    }

    private void displayAddUserForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Role> roles = roleDAO.getAllRoles();
        request.setAttribute("roles", roles);
        request.getRequestDispatcher("/public/admin/add_user.jsp").forward(request, response);
    }

    private void displayChangeStatus(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/public/admin/change_status.jsp").forward(request, response);
    }
     

    private void displayUpdateUserForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Role> roles = roleDAO.getAllRoles();
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
        
        UserDAO dao = new UserDAO();
        List<User> list = dao.getAllUsers();
        
        request.setAttribute("list", list);
        
        request.getRequestDispatcher("/public/admin/user_list.jsp")
                .forward(request, response);
    }    
     
    
    private void displayDashboard(HttpServletRequest request,HttpServletResponse response)throws ServletException, IOException {
        request.getRequestDispatcher("/public/admin/dashboard.jsp").forward(request, response);
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
        String username  = request.getParameter("username");
        String email     = request.getParameter("email");
        String password  = request.getParameter("password");
        String fullName  = request.getParameter("fullName");
        String dob       = request.getParameter("dob");
        String gender    = request.getParameter("gender");
        String address   = request.getParameter("address");
        int roleId = Integer.parseInt(request.getParameter("role_selection"));
        

        boolean isSuccess = userDAO.addUser(username,email, password, fullName, dob, gender ,address, roleId);
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
        String username  = request.getParameter("username");
        String email     = request.getParameter("email");
        String password  = request.getParameter("password");
        String fullName  = request.getParameter("fullName");
        String dob       = request.getParameter("dob");
        String gender    = request.getParameter("gender");
        String address   = request.getParameter("address");
        int roleId = Integer.parseInt(request.getParameter("role_selection"));
        

        boolean isSuccess = userDAO.updateUser(userId,username,email, password, fullName, dob, gender ,address, roleId);
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
        if(userId == user.getUserId()){
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


//    private void handleChangingStatusRole(HttpServletRequest request, HttpServletResponse response) {
//        int roleStatus = Integer.parseInt(request.getParameter("roleStatus"));
//        boolean isUpdated = roleDAO.handleStatus(roleStatus, );
//        if(isUpdated){
//            request.getSession().setAttribute("success", "Cập nhật trạng thái thành công");
//            response.sendRedirect(request.getContextPath() +"/");
//        }else{
//            request.setAttribute("error", "Cập nhật trạng thái không thành công");
//            request.getRequestDispatcher("/public/admin/add_user.jsp").forward(request, response);
//        }
//    }



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

        int roleId;
        try {
            roleId = Integer.parseInt(rawRoleId);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Mã vai trò không hợp lệ");
            request.getRequestDispatcher("/public/admin/role_detail.jsp").forward(request, response);
            return;
        }

        Role selectedRole = roleDAO.getRoleById(roleId);
        if (selectedRole == null) {
            request.setAttribute("error", "Không tìm thấy vai trò");
            request.getRequestDispatcher("/public/admin/role_detail.jsp").forward(request, response);
            return;
        }

        request.setAttribute("selectedRole", selectedRole);
        request.setAttribute("permissions", permissionDAO.getPermissionsByRoleId(roleId));
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

        if (userDAO.isUsernameExistsForOtherUser(username, sessionUser.getUserId())) {
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void preventBackCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
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

        int roleId;
        try {
            roleId = Integer.parseInt(rawRoleId);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Mã vai trò không hợp lệ");
            request.getRequestDispatcher("/public/admin/role_detail.jsp").forward(request, response);
            return;
        }

        Role selectedRole = roleDAO.getRoleById(roleId);
        if (selectedRole == null) {
            request.setAttribute("error", "Không tìm thấy vai trò");
            request.getRequestDispatcher("/public/admin/role_detail.jsp").forward(request, response);
            return;
        }

        List<Permission> grantedPermissions = permissionDAO.getPermissionsByRoleId(roleId);
        request.setAttribute("selectedRole", selectedRole);
        request.setAttribute("permissions", grantedPermissions);
        request.setAttribute("permissions", permissionDAO.getPermissionsByRoleId(roleId));
        request.setAttribute("roleUsers", userDAO.getUsersByRoleId(roleId));
        setPermissionMatrixAttributes(request, getAllPermissions(), getGrantedPermissionIds(roleId));
        request.getRequestDispatcher("/public/admin/role_detail.jsp").forward(request, response);
    }

    private void displayRolePermissions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Role> roles = getRolesForPermissionView();
        Role selectedRole = resolveSelectedRole(request.getParameter("roleId"), roles);
        List<Permission> allPermissions = getAllPermissions();
        Set<Integer> grantedPermissionIds = selectedRole == null
                ? Collections.emptySet()
                : getGrantedPermissionIds(selectedRole.getRoleId());

        Map<Integer, Integer> permissionCounts = new HashMap<>();
        for (Role role : roles) {
            permissionCounts.put(role.getRoleId(), countPermissionsByRoleId(role.getRoleId()));
        }

        consumeFlashMessages(request);
        request.setAttribute("roles", roles);
        request.setAttribute("selectedRole", selectedRole);
        request.setAttribute("permissionCounts", permissionCounts);
        request.setAttribute("roleDescriptions", buildRoleDescriptionMap(roles));
        setPermissionMatrixAttributes(request, allPermissions, grantedPermissionIds);
        request.getRequestDispatcher("/public/admin/role_permission.jsp").forward(request, response);
    }

    private void handleRolePermissions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        List<Role> roles = getRolesForPermissionView();
        Role selectedRole = resolveSelectedRole(request.getParameter("roleId"), roles);
        if (selectedRole == null) {
            session.setAttribute("error", "Không tìm thấy vai trò cần cập nhật.");
            response.sendRedirect(request.getContextPath() + "/v1/admin/role-permissions");
            return;
        }

        try {
            Set<Integer> permissionIds = parsePermissionIds(request.getParameterValues("permissionIds"));
            permissionIds.retainAll(getAllPermissionIds());
            updateRolePermissions(selectedRole.getRoleId(), permissionIds);
            session.setAttribute("success", "Đã cập nhật phân quyền cho vai trò " + selectedRole.getRoleName() + ".");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot update permissions for roleId: " + selectedRole.getRoleId(), e);
            session.setAttribute("error", "Không thể lưu phân quyền. Vui lòng thử lại.");
        }

        response.sendRedirect(request.getContextPath() + "/v1/admin/role-permissions?roleId=" + selectedRole.getRoleId());
    }

    private Role resolveSelectedRole(String rawRoleId, List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return null;
        }

        if (rawRoleId != null && !rawRoleId.trim().isEmpty()) {
            try {
                int roleId = Integer.parseInt(rawRoleId.trim());
                for (Role role : roles) {
                    if (role.getRoleId() == roleId) {
                        return role;
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return roles.get(0);
    }

    private List<Permission> getAllPermissions() {
        List<Permission> permissions = new ArrayList<>();
        String sql = "SELECT permissionId, permissionCode, permissionName, description FROM permissions ORDER BY permissionName";

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                permissions.add(mapPermission(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve permissions", e);
        }

        return permissions;
    }

    private List<Role> getRolesForPermissionView() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT roleId, roleCode, roleName FROM roles ORDER BY roleId";

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Role role = new Role();
                role.setRoleId(rs.getInt("roleId"));
                role.setRoleCode(rs.getString("roleCode"));
                role.setRoleName(rs.getString("roleName"));
                role.setIsActive(1);
                roles.add(role);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve roles for permission view", e);
        }

        return roles;
    }

    private Set<Integer> getAllPermissionIds() throws SQLException {
        Set<Integer> permissionIds = new HashSet<>();
        String sql = "SELECT permissionId FROM permissions";

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                permissionIds.add(rs.getInt("permissionId"));
            }
        }

        return permissionIds;
    }

    private Set<Integer> getGrantedPermissionIds(int roleId) {
        Set<Integer> permissionIds = new HashSet<>();
        String sql = "SELECT permissionId FROM role_permissions WHERE roleId = ?";

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permissionIds.add(rs.getInt("permissionId"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve granted permission ids for roleId: " + roleId, e);
        }

        return permissionIds;
    }

    private int countPermissionsByRoleId(int roleId) {
        String sql = "SELECT COUNT(*) FROM role_permissions WHERE roleId = ?";

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot count permissions for roleId: " + roleId, e);
            return 0;
        }
    }

    private Map<Integer, Boolean> buildGrantedPermissionMap(List<Permission> allPermissions, Set<Integer> grantedPermissionIds) {
        Map<Integer, Boolean> grantedPermissionMap = new HashMap<>();
        for (Permission permission : allPermissions) {
            grantedPermissionMap.put(permission.getPermissionId(), grantedPermissionIds.contains(permission.getPermissionId()));
        }
        return grantedPermissionMap;
    }

    private void setPermissionMatrixAttributes(HttpServletRequest request, List<Permission> allPermissions, Set<Integer> grantedPermissionIds) {
        request.setAttribute("allPermissions", allPermissions);
        request.setAttribute("permissionActions", PERMISSION_ACTIONS);
        request.setAttribute("permissionMatrixRows", buildPermissionMatrixRows(allPermissions));
        request.setAttribute("grantedPermissionMap", buildGrantedPermissionMap(allPermissions, grantedPermissionIds));
    }

    private List<PermissionMatrixRow> buildPermissionMatrixRows(List<Permission> permissions) {
        Map<String, PermissionMatrixRow> rowsByKey = new LinkedHashMap<>();
        if (permissions == null || permissions.isEmpty()) {
            return new ArrayList<>();
        }

        for (Permission permission : permissions) {
            FeatureDefinition feature = resolvePermissionFeature(permission);
            String actionKey = resolvePermissionAction(permission);
            PermissionMatrixRow row = rowsByKey.get(feature.getKey());

            if (row == null) {
                row = new PermissionMatrixRow(feature.getKey(), feature.getName(), feature.getSortOrder());
                rowsByKey.put(feature.getKey(), row);
            }

            if (row.getPermissionsByAction().containsKey(actionKey)) {
                String fallbackKey = feature.getKey() + "-" + permission.getPermissionId();
                row = new PermissionMatrixRow(fallbackKey, buildFallbackFeatureName(permission), feature.getSortOrder() + permission.getPermissionId());
                rowsByKey.put(fallbackKey, row);
            }

            row.addPermission(actionKey, permission);
        }

        List<PermissionMatrixRow> rows = new ArrayList<>(rowsByKey.values());
        rows.sort(Comparator
                .comparingInt(PermissionMatrixRow::getSortOrder)
                .thenComparing(PermissionMatrixRow::getFeatureName, String.CASE_INSENSITIVE_ORDER));
        return rows;
    }

    private FeatureDefinition resolvePermissionFeature(Permission permission) {
        String text = normalizePermissionText(permission);
        for (FeatureDefinition feature : PERMISSION_FEATURES) {
            if (containsAny(text, feature.getKeywords())) {
                return feature;
            }
        }

        return new FeatureDefinition(
                "permission-" + permission.getPermissionId(),
                buildFallbackFeatureName(permission),
                9000 + permission.getPermissionId()
        );
    }

    private String resolvePermissionAction(Permission permission) {
        String text = normalizePermissionText(permission);
        if (containsAny(text, "delete", "remove", "destroy", "xoa")) {
            return "delete";
        }
        if (containsAny(text, "create", "add", "insert", "new", "tao", "them")) {
            return "create";
        }
        if (containsAny(text, "edit", "update", "modify", "change", "sua", "cap nhat")) {
            return "edit";
        }
        return "view";
    }

    private String normalizePermissionText(Permission permission) {
        if (permission == null) {
            return "";
        }
        return normalizeText(permission.getPermissionCode() + " " + permission.getPermissionName() + " " + permission.getDescription());
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null || keywords == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (!isBlank(keyword) && text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('\u0111', 'd')
                .replace('\u0110', 'D')
                .replace('_', ' ')
                .replace('-', ' ')
                .replace('.', ' ');
        return normalized.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

    private String buildFallbackFeatureName(Permission permission) {
        if (permission == null) {
            return "Kh\u00e1c";
        }
        if (!isBlank(permission.getPermissionName())) {
            return permission.getPermissionName();
        }
        if (!isBlank(permission.getPermissionCode())) {
            return permission.getPermissionCode();
        }
        return "Quy\u1ec1n #" + permission.getPermissionId();
    }

    private Map<Integer, String> buildRoleDescriptionMap(List<Role> roles) {
        Map<Integer, String> descriptions = new HashMap<>();
        if (roles == null) {
            return descriptions;
        }
        for (Role role : roles) {
            descriptions.put(role.getRoleId(), describeRole(role));
        }
        return descriptions;
    }

    private String describeRole(Role role) {
        if (role == null) {
            return "";
        }
        String code = role.getRoleCode() == null ? "" : role.getRoleCode().trim().toUpperCase(Locale.ROOT);
        String text = normalizeText(role.getRoleName() + " " + role.getRoleCode());
        if ("AD".equals(code) || text.contains("admin")) {
            return "To\u00e0n quy\u1ec1n h\u1ec7 th\u1ed1ng";
        }
        if ("MA".equals(code) || text.contains("manager") || text.contains("quan ly")) {
            return "Qu\u1ea3n l\u00fd nh\u00e2n vi\u00ean";
        }
        if ("EM".equals(code) || text.contains("employee") || text.contains("nhan vien")) {
            return "Quy\u1ec1n c\u01a1 b\u1ea3n";
        }
        return !isBlank(role.getRoleCode()) ? role.getRoleCode() : "Vai tr\u00f2 h\u1ec7 th\u1ed1ng";
    }

    private Set<Integer> parsePermissionIds(String[] rawPermissionIds) {
        Set<Integer> permissionIds = new LinkedHashSet<>();
        if (rawPermissionIds == null) {
            return permissionIds;
        }

        for (String rawPermissionId : rawPermissionIds) {
            if (rawPermissionId == null || rawPermissionId.trim().isEmpty()) {
                continue;
            }
            try {
                int permissionId = Integer.parseInt(rawPermissionId.trim());
                if (permissionId > 0) {
                    permissionIds.add(permissionId);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return permissionIds;
    }


    private void updateRolePermissions(int roleId, Set<Integer> permissionIds) throws SQLException {
        String deleteSql = "DELETE FROM role_permissions WHERE roleId = ?";
        String insertSql = "INSERT INTO role_permissions (roleId, permissionId) VALUES (?, ?)";

        try (Connection conn = dbContext.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
                    deletePs.setInt(1, roleId);
                    deletePs.executeUpdate();
                }

                if (!permissionIds.isEmpty()) {
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        for (Integer permissionId : permissionIds) {
                            insertPs.setInt(1, roleId);
                            insertPs.setInt(2, permissionId);
                            insertPs.addBatch();
                        }
                        insertPs.executeBatch();
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }
    }

    private Permission mapPermission(ResultSet rs) throws SQLException {
        Permission permission = new Permission();
        permission.setPermissionId(rs.getInt("permissionId"));
        permission.setPermissionCode(rs.getString("permissionCode"));
        permission.setPermissionName(rs.getString("permissionName"));
        permission.setDescription(rs.getString("description"));
        return permission;
    }

    private void consumeFlashMessages(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        Object success = session.getAttribute("success");
        Object error = session.getAttribute("error");
        if (success != null) {
            request.setAttribute("success", success);
            session.removeAttribute("success");
        }
        if (error != null) {
            request.setAttribute("error", error);
            session.removeAttribute("error");
        }
    }

    public static class PermissionActionColumn {
        private final String key;
        private final String label;

        public PermissionActionColumn(String key, String label) {
            this.key = key;
            this.label = label;
        }

        public String getKey() {
            return key;
        }

        public String getLabel() {
            return label;
        }
    }

    public static class PermissionMatrixRow {
        private final String key;
        private final String featureName;
        private final int sortOrder;
        private final Map<String, Permission> permissionsByAction = new LinkedHashMap<>();

        public PermissionMatrixRow(String key, String featureName, int sortOrder) {
            this.key = key;
            this.featureName = featureName;
            this.sortOrder = sortOrder;
        }

        public String getKey() {
            return key;
        }

        public String getFeatureName() {
            return featureName;
        }

        public int getSortOrder() {
            return sortOrder;
        }

        public Map<String, Permission> getPermissionsByAction() {
            return permissionsByAction;
        }

        public void addPermission(String actionKey, Permission permission) {
            permissionsByAction.put(actionKey, permission);
        }
    }

    private static class FeatureDefinition {
        private final String key;
        private final String name;
        private final int sortOrder;
        private final String[] keywords;

        FeatureDefinition(String key, String name, int sortOrder, String... keywords) {
            this.key = key;
            this.name = name;
            this.sortOrder = sortOrder;
            this.keywords = keywords == null ? new String[0] : keywords;
        }

        String getKey() {
            return key;
        }

        String getName() {
            return name;
        }

        int getSortOrder() {
            return sortOrder;
        }

        String[] getKeywords() {
            return keywords;
        }
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

        if (userDAO.isUsernameExistsForOtherUser(username, sessionUser.getUserId())) {
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void preventBackCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }
}
