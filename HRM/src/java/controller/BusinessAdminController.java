package controller;

import dao.DepartmentDAO;
import dao.EmployeeDAO;
import dao.HolidayDAO;
import dao.PermissionDAO;
import dao.RoleDAO;
import dao.UserDAO;
import dto.EmployeeDetailDTO;
import dto.FormRequestDTO;
import dto.TransferRequestDTO;
import dao.FormRequestDAO;
import dao.OvertimeDAO;
import dto.OvertimeRequestDTO;
import java.sql.Date;
import model.Holiday;
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
import model.Role;
import model.User;
import static org.apache.tomcat.jakartaee.commons.lang3.StringUtils.isBlank;
import service.EmailService;
import utils.Paging;

public class BusinessAdminController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(BusinessAdminController.class.getName());
    private static final UserDAO userDAO = new UserDAO();
    private static final EmailService emailService = new EmailService();
    private static final RoleDAO roleDAO = new RoleDAO();
    private static final PermissionDAO permissionDAO = new PermissionDAO();
    private static final EmployeeDAO employeeDAO = new EmployeeDAO();
    private static final DepartmentDAO departmentDAO = new DepartmentDAO();
    private static final HolidayDAO holidayDAO = new HolidayDAO();
    private static final FormRequestDAO formRequestDAO = new FormRequestDAO();
    private static final OvertimeDAO overtimeDAO = new OvertimeDAO();
    private static final service.AttendanceService attendanceService = new service.AttendanceService();
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
            case "/holiday":
                displayHolidayList(request, response);
                break;
            case "/holiday/add":
                displayHolidayForm(request, response, false);
                break;
            case "/holiday/edit":
                displayHolidayForm(request, response, true);
                break;
            case "/attendance/overview":
                displayAttendanceOverview(request, response);
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
            case "/holiday/add":
                handleAddHoliday(request, response);
                break;
            case "/holiday/edit":
                handleUpdateHoliday(request, response);
                break;
            case "/holiday/delete":
                handleDeleteHoliday(request, response);
                break;
            case "/forms/approve":
                handleApproveForm(request, response, user);
                break;
            case "/forms/reject":
                handleRejectForm(request, response, user);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/");
                break;
        }
    }

    // =========================================================
    // Existing methods (unchanged)
    // =========================================================

    private void preventBackCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    private void displayDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int userSize = userDAO.countUsers("", "");
        request.setAttribute("userSize", userSize);

        int deptSize = departmentDAO.getAllActiveDepartments().size();
        request.setAttribute("deptSize", deptSize);

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
                if (mgr != null)
                    managerMap.put(d.getDepartmentId(), mgr);
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
        List<EmployeeDetailDTO> employees = employeeDAO.getAllEmployees(user.getUserId());
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
                // Phòng đã có manager → người mới (kể cả role manager) làm cấp dưới
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

    // =========================================================
    // Quản lý ngày lễ (Holiday)
    // =========================================================

    // ===================== Attendance Dashboard (Overview / Detail / Export) =====================

    /** HR/Business Admin: 0 hoặc rỗng = toàn công ty; ngược lại lọc theo phòng ban. */
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

    private void displayAttendanceOverview(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer departmentId = resolveDepartmentFilter(request);
        java.time.LocalDate now = java.time.LocalDate.now();
        int month = paramOr(request, "month", now.getMonthValue());
        int year = paramOr(request, "year", now.getYear());

        java.util.List<dto.AttendanceSummaryDTO> summaries =
                attendanceService.getMonthlySummaries(departmentId, month, year);

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
        setBusinessAdminAttendanceLayout(request);
        request.getRequestDispatcher("/public/businessadmin/attendance/attendance_overview.jsp").forward(request, response);
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

    private void displayHolidayList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("holidays", holidayDAO.getAllHolidays());

        HttpSession session = request.getSession(false);
        if (session != null) {
            String success = (String) session.getAttribute("holidaySuccess");
            String error = (String) session.getAttribute("holidayError");
            if (success != null) {
                request.setAttribute("success", success);
                session.removeAttribute("holidaySuccess");
            }
            if (error != null) {
                request.setAttribute("error", error);
                session.removeAttribute("holidayError");
            }
        }
        request.getRequestDispatcher("/public/businessadmin/holiday/holiday_list.jsp").forward(request, response);
    }

    private void displayHolidayForm(HttpServletRequest request, HttpServletResponse response, boolean editMode)
            throws ServletException, IOException {
        if (editMode) {
            Integer holidayId = parseIntParam(request.getParameter("id"));
            Holiday holiday = (holidayId != null) ? holidayDAO.getHolidayById(holidayId) : null;
            if (holiday == null) {
                request.getSession().setAttribute("holidayError", "Không tìm thấy ngày lễ.");
                response.sendRedirect(request.getContextPath() + "/v1/businessadmin/holiday");
                return;
            }
            request.setAttribute("holiday", holiday);
        }
        request.setAttribute("editMode", editMode);
        request.getRequestDispatcher("/public/businessadmin/holiday/holiday_form.jsp").forward(request, response);
    }

    private void handleAddHoliday(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Holiday holiday = new Holiday();
        String error = bindAndValidateHoliday(request, holiday);
        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("editMode", false);
            request.setAttribute("holiday", holiday);
            request.getRequestDispatcher("/public/businessadmin/holiday/holiday_form.jsp").forward(request, response);
            return;
        }

        int newId = holidayDAO.addHoliday(holiday);
        if (newId > 0) {
            request.getSession().setAttribute("holidaySuccess",
                    "Đã thêm ngày lễ \"" + holiday.getHolidayName() + "\".");
        } else {
            request.getSession().setAttribute("holidayError", "Thêm ngày lễ thất bại. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/businessadmin/holiday");
    }

    private void handleUpdateHoliday(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer holidayId = parseIntParam(request.getParameter("holidayId"));
        if (holidayId == null || holidayDAO.getHolidayById(holidayId) == null) {
            request.getSession().setAttribute("holidayError", "Không tìm thấy ngày lễ.");
            response.sendRedirect(request.getContextPath() + "/v1/businessadmin/holiday");
            return;
        }

        Holiday holiday = new Holiday();
        holiday.setHolidayId(holidayId);
        String error = bindAndValidateHoliday(request, holiday);
        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("editMode", true);
            request.setAttribute("holiday", holiday);
            request.getRequestDispatcher("/public/businessadmin/holiday/holiday_form.jsp").forward(request, response);
            return;
        }

        if (holidayDAO.updateHoliday(holiday)) {
            request.getSession().setAttribute("holidaySuccess",
                    "Đã cập nhật ngày lễ \"" + holiday.getHolidayName() + "\".");
        } else {
            request.getSession().setAttribute("holidayError", "Cập nhật ngày lễ thất bại. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/businessadmin/holiday");
    }

    private void handleDeleteHoliday(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer holidayId = parseIntParam(request.getParameter("holidayId"));
        if (holidayId == null) {
            request.getSession().setAttribute("holidayError", "Dữ liệu không hợp lệ.");
        } else if (holidayDAO.deleteHoliday(holidayId)) {
            request.getSession().setAttribute("holidaySuccess", "Đã xóa ngày lễ.");
        } else {
            request.getSession().setAttribute("holidayError", "Xóa ngày lễ thất bại. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/businessadmin/holiday");
    }

    /**
     * Đọc tham số form vào holiday và validate. Trả về null nếu hợp lệ, ngược lại trả về
     * thông báo lỗi (đồng thời giữ lại giá trị đã nhập trong holiday để hiển thị lại form).
     */
    private String bindAndValidateHoliday(HttpServletRequest request, Holiday holiday) {
        String name = request.getParameter("holidayName");
        String startRaw = request.getParameter("startDate");
        String endRaw = request.getParameter("endDate");
        boolean active = request.getParameter("isActive") != null;

        holiday.setHolidayName(name != null ? name.trim() : null);
        holiday.setActive(active);

        if (isBlank(name)) {
            return "Vui lòng nhập tên ngày lễ.";
        }
        if (isBlank(startRaw) || isBlank(endRaw)) {
            return "Vui lòng chọn ngày bắt đầu và ngày kết thúc.";
        }
        Date start, end;
        try {
            start = Date.valueOf(startRaw.trim());
            end = Date.valueOf(endRaw.trim());
        } catch (IllegalArgumentException e) {
            return "Ngày không hợp lệ (yêu cầu yyyy-MM-dd).";
        }
        if (end.before(start)) {
            return "Ngày kết thúc phải bằng hoặc sau ngày bắt đầu.";
        }
        holiday.setStartDate(start);
        holiday.setEndDate(end);
        return null;
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

    // =========================================================
    // Quản lý Đơn từ (Form Requests)
    // =========================================================

    private void displayFormRequests(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String dayStr = request.getParameter("day");
        String monthStr = request.getParameter("month");
        String yearStr = request.getParameter("year");
        String keyword = request.getParameter("keyword");
        
        Integer day = parseIntParam(dayStr);
        Integer month = parseIntParam(monthStr);
        Integer year = parseIntParam(yearStr);
        
        List<FormRequestDTO> forms = formRequestDAO.getAllFormRequests(day, month, year, keyword)
                .stream()
                .filter(f -> "OVERTIME".equals(f.getFormTypeCode()) 
                          || "TRANSFER".equals(f.getFormTypeCode())
                          || "PROMOTION_DEMOTION".equals(f.getFormTypeCode()))
                .collect(java.util.stream.Collectors.toList());
        
        request.setAttribute("forms", forms);
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
        if (!(form instanceof TransferRequestDTO)) return;
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
        if (!(form instanceof TransferRequestDTO)) return;
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