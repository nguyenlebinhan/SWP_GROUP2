package controller;

import dao.AttendanceDAO;
import dao.DepartmentDAO;
import dao.EmployeeDAO;
import dao.EmploymentContractDAO;
import dao.FormRequestDAO;
import dao.FormTypeDAO;
import dao.PermissionDAO;
import dao.RoleDAO;
import dao.UploadedFileDAO;
import dao.UserDAO;
import dto.AttendanceImportResultDTO;
import dto.EmployeeDetailDTO;
import dto.FormRequestDTO;
import enums.FileStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.*;
import service.AttendanceImportService;
import utils.ConfigManager;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, // 1MB ghi ra đĩa
        maxFileSize = 10L * 1024 * 1024, // 10MB / file
        maxRequestSize = 11L * 1024 * 1024 // 11MB / request
)

public class EmployeeController extends HttpServlet {

    private final ConfigManager config = ConfigManager.getInstance();
    private final Logger LOGGER = Logger.getLogger(EmployeeController.class.getName());
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final EmploymentContractDAO contractDAO = new EmploymentContractDAO();
    private final UserDAO userDAO = new UserDAO();
    private final PermissionDAO permissionDAO = new PermissionDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    private final FormRequestDAO formRequestDAO = new FormRequestDAO();
    private final FormTypeDAO formTypeDAO = new FormTypeDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final UploadedFileDAO uploadedFileDAO = new UploadedFileDAO();
    private final AttendanceImportService importService = new AttendanceImportService();
    private final String UPLOAD_DIR = config.getProperty("UPLOAD_DIR");
    private final String FILE_PART = config.getProperty("FILE_PART");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
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
            case "/dashboard":
                displayDashboard(request, response, user);
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
            case "/contract/add":
                displayAddContractForm(request, response, user);
                break;
            case "/contract/preview":
                displayContractPreview(request, response, user);
                break;
            case "/department/detail":
                displayEmployeeDepartmentDetail(request, response, user);
                break;
            case "/department/assign":
                displayAssignDepartmentForm(request, response, user);
                break;
            case "/department/list":
                displayDepartmentList(request, response, user);
                break;
            case "/department/add":
                displayAddDepartmentForm(request, response, user);
                break;
            case "/department/update":
                displayUpdateDepartmentForm(request, response, user);
                break;
            case "/my-profile":
                displayMyProfile(request, response, user);
                break;
            case "/attendance/import":
                displayImportForm(request, response, user);
                break;
            case "/attendance/list":
                displayAttendanceList(request, response, user);
                break;
            case "/attendance/update":
                displayUpdateAttendanceForm(request, response, user);
                break;
            case "/attendance/own-attendance":
                displayOwnAttendanceList(request, response, user);
                break;
            case "/forms/my-forms":
                displayMyForms(request, response, user);
                break;
            case "/forms/leave/new":
                displayLeaveForm(request, response, user);
                break;
            case "/forms/complaint/new":
                displayComplaintForm(request, response, user);
                break;
            case "/forms/all":
                displayAllForms(request, response, user);
                break;
            case "/forms/detail":
                displayFormDetail(request, response, user);
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

        String action = request.getPathInfo();
        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        switch (action) {
            case "/department/assign":
                handleAssignDepartment(request, response, user);
                break;
            case "/update-employee":
                handleUpdateEmployee(request, response, user);
                break;
            case "/contract/add":
                handleAddContract(request, response, user);
                break;
            case "/department/unassign":
                handleUnassignDepartment(request, response, user);
                break;
            case "/department/add":
                handleAddDepartment(request, response, user);
                break;
            case "/department/update":
                handleUpdateDepartment(request, response, user);
                break;
            case "/my-profile/update":
                handleUpdateMyProfile(request, response, user);
                break;
            case "/attendance/import":
                handleImportAttendance(request, response, user);
                break;
            case "/attendance/update":
                handleUpdateAttendance(request, response, user);
                break;
            case "/update-employee-detail":
                handleUpdateEmployeeDetail(request, response, user);
                break;
            case "/forms/leave/submit":
                handleLeaveFormSubmit(request, response, user);
                break;
            case "/forms/complaint/submit":
                handleComplaintFormSubmit(request, response, user);
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
            request.getSession().setAttribute("error",
                    "Bạn không có quyền xem danh sách nhân viên bởi bạn không phải HR");
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
        request.getRequestDispatcher("/public/employee/employee_info/employee_list.jsp").forward(request, response);
    }

    private void displayEmployeeDetail(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error",
                    "Bạn không có quyền xem chi tiết nhân viên bởi bạn không phải HR");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        if (!hasPermission(user, "VIEW_EMPLOYEES")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem chi tiết nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        EmployeeDetailDTO employee = getEmployeeFromRequest(request, response);
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/employee-list");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("employee", employee);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/employee_info/employee_detail.jsp").forward(request, response);
    }

    private void displayUpdateEmployeeForm(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền chỉnh sửa nhân viên bởi bạn không phải HR");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        if (!hasPermission(user, "EDIT_EMPLOYEE")) {
            request.getSession().setAttribute("error", "Bạn không có quyền chỉnh sửa nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        EmployeeDetailDTO employee = getEmployeeFromRequest(request, response);
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/employee-list");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("employee", employee);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/employee_info/update_employee.jsp").forward(request, response);
    }

    private void displayAddContractForm(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm hợp đồng bởi bạn không phải HR");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        if (!hasPermission(user, "ADD_EMPLOYMENT_CONTRACT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm hợp đồng lao động.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("employees", employeeDAO.getAllEmployees(user.getUserId()));
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/contract/add_contract.jsp").forward(request, response);
    }

    private void displayContractPreview(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem hợp đồng lao động.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        if (!hasPermission(user, "ADD_EMPLOYMENT_CONTRACT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem hợp đồng lao động.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        EmploymentContract contract = getContractFromRequest(request);
        if (contract == null) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/employee-list");
            return;
        }

        EmployeeDetailDTO employee = employeeDAO.getEmployeeById(contract.getEmployeeId());
        if (employee == null) {
            request.getSession().setAttribute("error", "Không tìm thấy nhân viên của hợp đồng.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/employee-list");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("contract", contract);
        request.setAttribute("employee", employee);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/contract/contract_preview.jsp").forward(request, response);
    }

    private void displayOwnAttendanceList(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);

        String rawMonth = request.getParameter("month");
        String rawYear = request.getParameter("year");
        Integer month = null;
        Integer year = null;
        try {
            if (rawMonth != null && !rawMonth.isEmpty() && !rawMonth.equals("0")) {
                month = Integer.parseInt(rawMonth);
            }
            if (rawYear != null && !rawYear.isEmpty()) {
                year = Integer.parseInt(rawYear);
            }
        } catch (NumberFormatException e) {
            // ignore
        }

        EmployeeDetailDTO me = employeeDAO.getEmployeeByUserId(user.getUserId());
        List<Attendance> attendances = (me != null)
                ? attendanceDAO.getAttendanceListByEmployeeId(me.getEmployeeId(), month, year)
                : new java.util.ArrayList<>();

        request.setAttribute("attendances", attendances);
        request.setAttribute("selectedMonth", month);
        request.setAttribute("selectedYear", year);
        request.getRequestDispatcher("/public/employee/attendance/own_attendance_list.jsp").forward(request, response);
    }

    private void displayEmployeeDepartmentDetail(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {

        if (!isHrStaff(user) || !hasPermission(user, "VIEW_DEPARTMENT_EMPLOYEES_DETAIL")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem nhân viên của phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        String rawDepartmentId = request.getParameter("id");
        if (isBlank(rawDepartmentId)) {
            request.getSession().setAttribute("error", "Thiếu mã phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
            return;
        }

        int departmentId;
        try {
            departmentId = Integer.parseInt(rawDepartmentId);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã phòng ban không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
            return;
        }

        Department department = departmentDAO.getDepartmentById(departmentId);
        if (department == null) {
            request.getSession().setAttribute("error", "Không tìm thấy phòng ban.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
            return;
        }

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        List<EmployeeDetailDTO> employees = employeeDAO.getEmployeesByDepartmentId(departmentId);
        request.setAttribute("department", department);
        request.setAttribute("employees", employees);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/employee_info/department_employee_detail.jsp").forward(request, response);
    }

    private void displayAssignDepartmentForm(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {

        if (!isHrStaff(user) || !hasPermission(user, "ASSIGN_DEPARTMENT")) {
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
        request.getRequestDispatcher("/public/employee/department/assign_department.jsp").forward(request, response);
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
        request.getRequestDispatcher("/public/employee/department/department_list.jsp").forward(request, response);
    }

    private void displayMyProfile(HttpServletRequest request, HttpServletResponse response,
            User sessionUser) throws ServletException, IOException {
        User currentUser = userDAO.getUserById(sessionUser.getUserId());
        request.setAttribute("currentUser", currentUser);
        EmployeeDetailDTO myEmployee = employeeDAO.getEmployeeByUserId(sessionUser.getUserId());
        request.setAttribute("myEmployee", myEmployee);
        request.getRequestDispatcher("/public/employee/employee_info/my_profile.jsp").forward(request, response);
    }

    private void displayImportForm(HttpServletRequest request, HttpServletResponse response,
            model.User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "IMPORT_ATTENDANCE")) {
            request.getSession().setAttribute("error", "Bạn không có quyền import chấm công.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
        request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
    }

    private void displayAttendanceList(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "VIEW_ATTENDANCE")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem dữ liệu chấm công.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);

        Integer month = parseIntOrNull(request.getParameter("month"));
        Integer year = parseIntOrNull(request.getParameter("year"));
        Integer departmentId = parseIntOrNull(request.getParameter("departmentId"));
        String employeeCode = trimToNull(request.getParameter("employeeCode"));
        Integer restrictEmployeeId = null;
        List<Attendance> attendances = attendanceDAO.getAttendanceList(
                departmentId, month, year, employeeCode, restrictEmployeeId);
        for (Attendance a : attendances) {
            a.setEditable(!isAttendanceEditLocked(a.getWorkDate()));
        }
        List<Department> activeDepartments = departmentDAO.getAllActiveDepartments();
        request.setAttribute("departments", activeDepartments);
        request.setAttribute("attendances", attendances);
        request.setAttribute("filterMonth", month);
        request.setAttribute("filterYear", year);
        request.setAttribute("filterDepartmentId", departmentId);
        request.setAttribute("filterEmployeeCode", employeeCode);
        request.setAttribute("canFilterDepartment", isHrStaff(user));
        request.getRequestDispatcher("/public/employee/attendance/attendance_list.jsp").forward(request, response);
    }

    private void displayUpdateAttendanceForm(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "EDIT_ATTENDANCE")) {
            request.getSession().setAttribute("error", "Bạn không có quyền chỉnh sửa dữ liệu chấm công.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);

        String backUrl = request.getContextPath() + "/v1/employee/attendance/list"
                + buildAttendanceFilterQuery(request);

        Integer attendanceId = parseIntOrNull(request.getParameter("id"));
        Attendance attendance = (attendanceId != null) ? attendanceDAO.getAttendanceById(attendanceId) : null;
        if (attendance == null) {
            request.getSession().setAttribute("error", "Không tìm thấy bản ghi chấm công.");
            response.sendRedirect(backUrl);
            return;
        }
        request.setAttribute("attendance", attendance);
        request.setAttribute("editLocked", isAttendanceEditLocked(attendance.getWorkDate()));
        request.setAttribute("adjustmentHistory", attendanceDAO.getAdjustmentHistory(attendanceId));
        request.setAttribute("backUrl", backUrl);
        request.setAttribute("filterMonth", trimToNull(request.getParameter("month")));
        request.setAttribute("filterYear", trimToNull(request.getParameter("year")));
        request.setAttribute("filterDepartmentId", trimToNull(request.getParameter("departmentId")));
        request.setAttribute("filterEmployeeCode", trimToNull(request.getParameter("employeeCode")));
        request.getRequestDispatcher("/public/employee/attendance/attendance_update.jsp").forward(request, response);
    }

    private void handleUpdateAttendance(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "EDIT_ATTENDANCE")) {
            request.getSession().setAttribute("error", "Bạn không có quyền chỉnh sửa dữ liệu chấm công.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        String redirectUrl = request.getContextPath() + "/v1/employee/attendance/list"
                + buildAttendanceFilterQuery(request);

        Integer attendanceId = parseIntOrNull(request.getParameter("attendanceId"));
        if (attendanceId == null) {
            request.getSession().setAttribute("error", "Dữ liệu chỉnh sửa chấm công không hợp lệ.");
            response.sendRedirect(redirectUrl);
            return;
        }

        // Mọi chỉnh sửa phải có lý do.
        String reason = trimToNull(request.getParameter("reason"));
        if (reason == null) {
            request.getSession().setAttribute("error", "Vui lòng nhập lý do chỉnh sửa chấm công.");
            response.sendRedirect(redirectUrl);
            return;
        }

        Attendance attendance = attendanceDAO.getAttendanceById(attendanceId);
        if (attendance == null) {
            request.getSession().setAttribute("error", "Không tìm thấy bản ghi chấm công.");
            response.sendRedirect(redirectUrl);
            return;
        }

        if (isAttendanceEditLocked(attendance.getWorkDate())) {
            request.getSession().setAttribute("error",
                    "Đã quá hạn chỉnh sửa. Chấm công chỉ được sửa đến hết ngày 5 của tháng kế tiếp.");
            response.sendRedirect(redirectUrl);
            return;
        }

        Time timeIn;
        Time timeOut;
        try {
            timeIn = parseTimeOrNull(request.getParameter("timeIn"));
            timeOut = parseTimeOrNull(request.getParameter("timeOut"));
        } catch (IllegalArgumentException e) {
            request.getSession().setAttribute("error", "Giờ vào / giờ ra không hợp lệ.");
            response.sendRedirect(redirectUrl);
            return;
        }

        // Giờ công chỉ tính khi có đủ cả 2 mốc giờ; còn lại = 0.
        BigDecimal hoursWorked;
        if (timeIn != null && timeOut != null) {
            long diffMillis = timeOut.getTime() - timeIn.getTime();
            if (diffMillis < 0) {
                request.getSession().setAttribute("error", "Giờ ra phải sau giờ vào.");
                response.sendRedirect(redirectUrl);
                return;
            }
            hoursWorked = new BigDecimal(diffMillis)
                    .divide(new BigDecimal(3600000), 2, RoundingMode.HALF_UP);
        } else {
            hoursWorked = BigDecimal.ZERO;
        }

        // Trạng thái được suy tự động từ giờ vào/ra (nhất quán với luồng import),
        // không lấy từ lựa chọn tay để tránh sai lệch (vd 08:30 phải là Đi muộn).
        int status = importService.resolveFinalStatusCode(timeIn, timeOut,
                attendance.getEmployeeId(), attendance.getWorkDate());

        String updateError = attendanceDAO.updateAttendanceWithHistory(attendanceId, timeIn, timeOut,
                hoursWorked, status, reason, user.getUserId());
        if (updateError == null) {
            request.getSession().setAttribute("success", "Đã cập nhật dữ liệu chấm công.");
        } else {
            request.getSession().setAttribute("error", updateError);
        }
        response.sendRedirect(redirectUrl);
    }

    private void handleImportAttendance(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user) || !hasPermission(user, "IMPORT_ATTENDANCE")) {
            request.getSession().setAttribute("error", "Bạn không có quyền import file chấm công");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
        }
        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);

        int month, year, departmentId;
        try {
            month = Integer.parseInt(request.getParameter("month").trim());
            year = Integer.parseInt(request.getParameter("year").trim());
            departmentId = Integer.parseInt(request.getParameter("departmentId").trim());
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Hệ thống bị lỗi. Vui lòng nhập lại");
            response.sendRedirect(request.getContextPath() + "/v1/employee/attendance/import");
            return;
        }

        if (month < 1 || month > 12) {
            request.setAttribute("error", "Vui lòng chọn tháng hợp lệ (1-12).");
            List<Department> activeDepartments = departmentDAO.getAllActiveDepartments();
            request.setAttribute("departments", activeDepartments);
            request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
        }
        if (year < 2000 || year > 2100) {
            request.setAttribute("error", "Vui lòng chọn năm hợp lệ");
            List<Department> activeDepartments = departmentDAO.getAllActiveDepartments();
            request.setAttribute("departments", activeDepartments);
            request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
            return;
        }

        Part filePart = request.getPart(FILE_PART);
        if (filePart == null || filePart.getSize() == 0) {
            request.setAttribute("error", "Vui lòng chọn file Excel .xlsx để import.");
            List<Department> activeDepartments = departmentDAO.getAllActiveDepartments();
            request.setAttribute("departments", activeDepartments);
            request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
            return;
        }

        String submittedName = filePart.getSubmittedFileName();
        if (submittedName == null || !submittedName.toLowerCase().endsWith(".xlsx")) {
            request.setAttribute("error", "File phải có định dạng .xlsx");
            List<Department> activeDepartments = departmentDAO.getAllActiveDepartments();
            request.setAttribute("departments", activeDepartments);
            request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
            return;
        }

        String contentType = filePart.getContentType();
        if (contentType != null && !isAcceptableXlsxContentType(contentType)) {
            request.setAttribute("error", "Loại file không hợp lệ. Yeaua cầu file excel .xlsx ");
            List<Department> activeDepartments = departmentDAO.getAllActiveDepartments();
            request.setAttribute("departments", activeDepartments);
            request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
            return;
        }
        if (departmentId <= 0) {
            request.setAttribute("error", "Vui lòng chọn phòng ban hợp lệ.");
            List<Department> activeDepartments = departmentDAO.getAllActiveDepartments();
            request.setAttribute("departments", activeDepartments);
            request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
            return;
        }

        EmployeeDetailDTO employee = employeeDAO.getEmployeeByUserId(user.getUserId());
        Integer submittedEmployeeId = (employee != null) ? employee.getEmployeeId() : null;
        String uploadPath = getServletContext().getRealPath("/" + UPLOAD_DIR);
        Path savedPath;
        String serverFileName = "ATT_" + departmentId
                + "_" + month + "_" + year + "_" + System.currentTimeMillis()
                + "_" + UUID.randomUUID().toString().substring(0, 8) + ".xlsx";
        try {
            Path dir = Paths.get(uploadPath);
            Files.createDirectories(dir);
            savedPath = dir.resolve(serverFileName);
            try (InputStream is = filePart.getInputStream()) {
                Files.copy(is, savedPath);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot save uploaded attendance file", e);
            List<Department> activeDepartments = departmentDAO.getAllActiveDepartments();
            request.setAttribute("departments", activeDepartments);
            request.setAttribute("error", "Không thể lưu file lên máy chủ. Vui lòng thử lại.");
            request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
            return;
        }

        UploadedFile uf = new UploadedFile();
        uf.setFileCode("UPF-" + System.currentTimeMillis());
        uf.setFileType("ATTENDANCE");
        uf.setDepartmentId(departmentId);
        uf.setEmployeeId(submittedEmployeeId);
        uf.setFileUrl(UPLOAD_DIR + "/" + serverFileName);
        uf.setFileName(sanitizeFileName(submittedName));
        uf.setMonth(month);
        uf.setYear(year);
        int fileId = uploadedFileDAO.createUploadedFile(uf);
        if (fileId <= 0) {
            request.setAttribute("error", "Không thể tạo bản ghi file. Vui lòng thử lại.");
            List<Department> activeDepartments = departmentDAO.getAllActiveDepartments();
            request.setAttribute("departments", activeDepartments);
            request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
            return;
        }

        AttendanceImportResultDTO result;
        try (InputStream is = Files.newInputStream(savedPath)) {
            result = importService.importAttendance(is, departmentId, month, year, fileId);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot read saved attendance file", e);
            uploadedFileDAO.updateImportResult(fileId, 0, 0, 0,
                    FileStatus.FILE_STATUS_FAILED.getRelatedNum(), "Không thể đọc lại file đã lưu.");
            request.setAttribute("error", "Không thể đọc file đã lưu để import.");
            List<Department> activeDepartments = departmentDAO.getAllActiveDepartments();
            request.setAttribute("departments", activeDepartments);
            request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
            return;
        }
        result.setFileName(uf.getFileName());
        LOGGER.log(Level.INFO, "Attendance import done by userId={0}: fileId={1}, total={2}, imported={3}, failed={4}",
                new Object[] { user.getUserId(), fileId, result.getTotalRows(),
                        result.getImportedRows(), result.getFailedRows() });

        request.setAttribute("auditLogged", Boolean.TRUE);
        List<Department> activeDepartments = departmentDAO.getAllActiveDepartments();
        request.setAttribute("departments", activeDepartments);
        request.setAttribute("importResult", result);
        request.setAttribute("selectedMonth", month);
        request.setAttribute("selectedYear", year);
        request.setAttribute("selectedDepartmentId", departmentId);
        request.getRequestDispatcher("/public/employee/attendance/attendance_import.jsp").forward(request, response);
    }

    private void handleAssignDepartment(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm phòng ban bởi bạn không phải HR");
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
            request.setAttribute("error", "Vui lòng chọn đầy đủ nhân viên, phòng ban và vị trí.");
            request.setAttribute("availableEmployees", employeeDAO.getEmployees(user.getUserId()));
            request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
            request.setAttribute("positions", departmentDAO.getAllPositions());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/department/assign_department.jsp").forward(request, response);
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
            request.getRequestDispatcher("/public/employee/department/assign_department.jsp").forward(request, response);
            return;
        }

        if (employeeDAO.isUserAssignedToDepartment(userId)) {
            request.setAttribute("error", "Người dùng này đã được phân công phòng ban rồi.");
            request.setAttribute("availableEmployees", employeeDAO.getEmployees(user.getUserId()));
            request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
            request.setAttribute("positions", departmentDAO.getAllPositions());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/department/assign_department.jsp").forward(request, response);
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
            request.getRequestDispatcher("/public/employee/department/assign_department.jsp").forward(request, response);
            return;
        }

        boolean success = employeeDAO.assignEmployeeToDepartment(
                userId, departmentId, positionId,
                isBlank(phoneNumber) ? null : phoneNumber.trim(),
                isBlank(skills) ? null : skills.trim(),
                isBlank(experience) ? null : experience.trim(),
                isBlank(degree) ? null : degree.trim());

        if (!success) {
            request.setAttribute("error", "Phân công thất bại. Vui lòng thử lại.");
            request.setAttribute("availableEmployees", employeeDAO.getEmployees(user.getUserId()));
            request.setAttribute("departments", departmentDAO.getAllActiveDepartments());
            request.setAttribute("positions", departmentDAO.getAllPositions());
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/department/assign_department.jsp").forward(request, response);
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

        LOGGER.log(Level.INFO, "Employee assigned: userId={0} → deptId={1}", new Object[] { userId, departmentId });

        request.getSession().setAttribute("success", "Phân công nhân viên vào phòng ban thành công.");
        response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
    }

    private void handleUnassignDepartment(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền gỡ phân công bởi bạn không phải HR");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        if (!hasPermission(user, "UNASSIGN_DEPARTMENT")) {
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

    private void displayAddDepartmentForm(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {

        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm phòng ban bởi bạn không phải HR");
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
        request.getRequestDispatcher("/public/employee/department/add_department.jsp").forward(request, response);
    }

    private void handleAddDepartment(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm phòng ban bởi bạn không phải HR");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
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
            request.getRequestDispatcher("/public/employee/department/add_department.jsp").forward(request, response);
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
            request.getRequestDispatcher("/public/employee/department/add_department.jsp").forward(request, response);
            return;
        }

        if (!roleIds.isEmpty()) {
            departmentDAO.replaceDepartmentRoles(newDeptId, roleIds);
        }

        LOGGER.log(Level.INFO, "Department created: code={0} by userId={1}", new Object[] { code, user.getUserId() });
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
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
            return;
        }
        int deptId;
        try {
            deptId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
            return;
        }
        Department dept = departmentDAO.getDepartmentById(deptId);
        if (dept == null) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
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
        request.getRequestDispatcher("/public/employee/department/update_department.jsp").forward(request, response);
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
            response.sendRedirect(
                    request.getContextPath() + "/v1/employee/department/update?id=" + (idParam != null ? idParam : ""));
            return;
        }

        int deptId;
        try {
            deptId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
            return;
        }

        Department dept = departmentDAO.getDepartmentById(deptId);
        if (dept == null) {
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
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
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
        } else {
            request.getSession().setAttribute("error", "Cập nhật thất bại. Vui lòng thử lại.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/update?id=" + deptId);
        }
    }

    private void handleUpdateEmployee(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền chỉnh sửa nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        if (!hasPermission(user, "EDIT_EMPLOYEE")) {
            request.getSession().setAttribute("error", "Bạn không có quyền chỉnh sửa nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
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
            response.sendRedirect(request.getContextPath() + "/v1/employee/employee-list");
            return;
        }

        if (!isValidEmployeeStatus(status)) {
            request.getSession().setAttribute("error", "Trạng thái nhân viên không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/update-employee?id=" + employeeId);
            return;
        }

        EmployeeDetailDTO current = employeeDAO.getEmployeeById(employeeId);
        if (current == null) {
            request.getSession().setAttribute("error", "Không tìm thấy nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/employee-list");
            return;
        }

        int departmentId = current.getDepartmentId();
        int positionId = current.getPositionId();
        int userRoleId = userDAO.getRoleIdByUserId(current.getUserId());
        if (!departmentDAO.isRoleAllowedForDepartment(departmentId, userRoleId)) {
            request.getSession().setAttribute("error",
                    "Vai trò hiện tại của nhân viên không phù hợp với phòng ban đã chọn.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/update-employee?id=" + employeeId);
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
            response.sendRedirect(request.getContextPath() + "/v1/employee/employee-detail?id=" + employeeId);
        } else {
            request.getSession().setAttribute("error", "Cập nhật nhân viên thất bại. Vui lòng thử lại.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/update-employee?id=" + employeeId);
        }
    }

    private void handleAddContract(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm hợp đồng lao động.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        if (!hasPermission(user, "ADD_EMPLOYMENT_CONTRACT")) {
            request.getSession().setAttribute("error", "Bạn không có quyền thêm hợp đồng lao động.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        String code = trimToNull(request.getParameter("contractCode"));
        String type = trimToNull(request.getParameter("contractType"));
        String employeeParam = request.getParameter("employeeId");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String salaryParam = request.getParameter("salary");

        if (code == null || type == null || isBlank(employeeParam) || isBlank(startDate) || isBlank(salaryParam)) {
            request.setAttribute("error",
                    "Vui lòng nhập đầy đủ mã hợp đồng, nhân viên, loại hợp đồng, ngày bắt đầu và lương.");
            request.setAttribute("employees", employeeDAO.getAllEmployees(user.getUserId()));
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/contract/add_contract.jsp").forward(request, response);
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
            request.getRequestDispatcher("/public/employee/contract/add_contract.jsp").forward(request, response);
            return;
        }

        if (!isValidContractType(type) || contract.getSalary().compareTo(BigDecimal.ZERO) < 0) {
            request.setAttribute("error", "Loại hợp đồng hoặc lương không hợp lệ.");
            request.setAttribute("employees", employeeDAO.getAllEmployees(user.getUserId()));
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/contract/add_contract.jsp").forward(request, response);
            return;
        }

        if (employeeDAO.getEmployeeById(contract.getEmployeeId()) == null) {
            request.setAttribute("error", "Nhân viên được chọn không tồn tại.");
            request.setAttribute("employees", employeeDAO.getAllEmployees(user.getUserId()));
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/contract/add_contract.jsp").forward(request, response);
            return;
        }

        if (contractDAO.hasActiveContract(contract.getEmployeeId())) {
            request.setAttribute("error", "Hợp đồng của nhân viên vẫn còn hiệu lực");
            request.setAttribute("employees", employeeDAO.getAllEmployees(user.getUserId()));
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/contract/add_contract.jsp").forward(request, response);
            return;
        }

        if (contract.getEndDate() != null && contract.getEndDate().before(contract.getStartDate())) {
            request.setAttribute("error", "Ngày kết thúc không được trước ngày bắt đầu.");
            request.setAttribute("employees", employeeDAO.getAllEmployees(user.getUserId()));
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/contract/add_contract.jsp").forward(request, response);
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
            response.sendRedirect(
                    request.getContextPath() + "/v1/employee/contract/preview?employeeId=" + contract.getEmployeeId());
        } else {
            request.setAttribute("error", "Thêm hợp đồng thất bại. Mã hợp đồng có thể đã tồn tại.");
            request.setAttribute("employees", employeeDAO.getAllEmployees(user.getUserId()));
            setPermissionFlags(request, getPermissions(user));
            request.getRequestDispatcher("/public/employee/contract/add_contract.jsp").forward(request, response);
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

    private void handleUpdateMyProfile(HttpServletRequest request, HttpServletResponse response,
            User user) throws ServletException, IOException {
        String phoneNumber = request.getParameter("phoneNumber");
        String skills = request.getParameter("skills");
        String experience = request.getParameter("experience");
        String degree = request.getParameter("degree");

        EmployeeDetailDTO myEmployee = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (myEmployee == null) {
            request.getSession().setAttribute("error", "Không tìm thấy hồ sơ nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/my-profile");
            return;
        }

        boolean success = employeeDAO.updateOwnProfile(
                myEmployee.getEmployeeId(),
                isBlank(phoneNumber) ? null : phoneNumber.trim(),
                isBlank(skills) ? null : skills.trim(),
                isBlank(experience) ? null : experience.trim(),
                isBlank(degree) ? null : degree.trim());

        if (success) {
            request.getSession().setAttribute("success", "Cập nhật hồ sơ thành công.");
        } else {
            request.getSession().setAttribute("error", "Cập nhật thất bại. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/v1/employee/my-profile");
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
        if (!isHrStaff(user)) {
            request.getSession().setAttribute("error", "Bạn không có quyền cập nhật nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        String rawEmployeeId = request.getParameter("employeeId");
        String rawStatus = request.getParameter("status");
        String phoneNumber = request.getParameter("phoneNumber");
        String degree = request.getParameter("degree");
        String experience = request.getParameter("experience");
        String skills = request.getParameter("skills");

        if (isBlank(rawEmployeeId) || isBlank(rawStatus)) {
            request.getSession().setAttribute("error", "Dữ liệu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
            return;
        }

        int employeeId;
        int status;
        try {
            employeeId = Integer.parseInt(rawEmployeeId);
            status = Integer.parseInt(rawStatus);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Dữ liệu không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
            return;
        }

        EmployeeDetailDTO employeeDetail = employeeDAO.getEmployeeById(employeeId);
        if (employeeDetail == null) {
            request.getSession().setAttribute("error", "Không tìm thấy nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/department/list");
            return;
        }

        boolean statusSuccess = employeeDAO.updateEmployeeStatus(employeeId, status);
        boolean profileSuccess = employeeDAO.updateOwnProfile(
                employeeId,
                isBlank(phoneNumber) ? null : phoneNumber.trim(),
                isBlank(skills) ? null : skills.trim(),
                isBlank(experience) ? null : experience.trim(),
                isBlank(degree) ? null : degree.trim());

        if (statusSuccess || profileSuccess) {
            request.getSession().setAttribute("success", "Cập nhật nhân viên thành công.");
        } else {
            request.getSession().setAttribute("error", "Cập nhật thất bại hoặc không có thay đổi.");
        }

        if (employeeDetail.getDepartmentId() > 0) {
            response.sendRedirect(
                    request.getContextPath() + "/v1/employee/department/detail?id=" + employeeDetail.getDepartmentId());
        } else {
            response.sendRedirect(request.getContextPath() + "/v1/employee/employee-detail?id=" + employeeId);
        }
    }

    private void displayMyForms(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        EmployeeDetailDTO em = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (em == null) {
            request.getSession().setAttribute("error", "Bạn chưa được gắn hồ sơ nhân viên");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        Integer day = parseIntOrNull(request.getParameter("day"));
        Integer month = parseIntOrNull(request.getParameter("month"));
        Integer year = parseIntOrNull(request.getParameter("year"));

        request.setAttribute("filterDay", day);
        request.setAttribute("filterMonth", month);
        request.setAttribute("filterYear", year);
        request.setAttribute("forms",
                formRequestDAO.getAllFormRequestsByEmployeeId(em.getEmployeeId(), day, month, year));
        request.getRequestDispatcher("/public/employee/forms/my_form_list.jsp").forward(request, response);
    }

    private void displayAllForms(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!hasPermission(user, "VIEW_ALL_FORMS")) {
            request.getSession().setAttribute("error", "Bạn không có quyền xem tất cả đơn");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
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
        request.getRequestDispatcher("/public/employee/forms/all_form_list.jsp").forward(request, response);
    }


    private void displayFormDetail(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String formIdRaw = request.getParameter("id");
        if (isBlank(formIdRaw)) {
            request.getSession().setAttribute("error", "Mã đơn không tồn tại");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        try {
            int id = Integer.parseInt(formIdRaw);
            FormRequestDTO form = formRequestDAO.getFormRequestById(id);
            if (form == null) {
                request.getSession().setAttribute("error", "Không tìm thấy đơn yêu cầu");
                response.sendRedirect(request.getContextPath() + "/v1/employee/forms/my-forms");
                return;
            }
            EmployeeDetailDTO me = employeeDAO.getEmployeeByUserId(user.getUserId());
            if (me == null) {
                request.getSession().setAttribute("error", "Bạn chưa được gán hồ sơ nhân viên");
                response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
                return;
            }
            EmployeeDetailDTO formOwner = employeeDAO.getEmployeeById(form.getEmployeeId());
            Department dept = (formOwner != null) ? departmentDAO.getDepartmentById(formOwner.getDepartmentId()) : null;

            boolean isMyForm = (form.getEmployeeId() == me.getEmployeeId());
            boolean canViewAll = getPermissions(user).contains("VIEW_ALL_FORMS");
            boolean isDeptManager = (dept != null && dept.getManagerId() != null
                    && dept.getManagerId() == me.getEmployeeId());
            if (!isMyForm && !canViewAll && !isDeptManager) {
                request.getSession().setAttribute("error", "Bạn không có quyền xem đơn này");
                response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
                return;
            }

            request.setAttribute("form", form);
            request.getRequestDispatcher("/public/employee/forms/form_detail.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã đơn không hợp lệ");
            response.sendRedirect(request.getContextPath() + "/v1/employee/forms/my-forms");
        }
    }

    private void displayLeaveForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/forms/leave_form.jsp").forward(request, response);
    }

    // Hiển thị trang Đơn Khiếu Nại
    private void displayComplaintForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        Set<String> perms = getPermissions(user);
        request.getSession().setAttribute("userPermissions", perms);
        setPermissionFlags(request, perms);
        request.getRequestDispatcher("/public/employee/forms/complaint_form.jsp").forward(request, response);
    }



    private void handleLeaveFormSubmit(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        
        FormType ft = formTypeDAO.getByCode("LEAVE");
        if (ft == null) {
            request.getSession().setAttribute("error", "Loại đơn LEAVE không tồn tại.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        int formTypeId = ft.getFormTypeId();

        EmployeeDetailDTO me = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (me == null) {
            request.getSession().setAttribute("error", "Bạn chưa được gắn hồ sơ nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        String reason = request.getParameter("reason");
        String rawStart = trimToNull(request.getParameter("startDate"));
        String rawEnd = trimToNull(request.getParameter("endDate"));

        if (rawStart == null || rawEnd == null) {
            request.setAttribute("error", "Đơn nghỉ phép yêu cầu nhập ngày bắt đầu và ngày kết thúc.");
            request.getRequestDispatcher("/public/employee/forms/leave_form.jsp").forward(request, response);
            return;
        }

        Date startDate, endDate;
        try {
            startDate = Date.valueOf(rawStart);
            endDate = Date.valueOf(rawEnd);
        } catch (IllegalArgumentException ex) {
            request.setAttribute("error", "Ngày không hợp lệ. Vui lòng nhập đúng định dạng.");
            request.getRequestDispatcher("/public/employee/forms/leave_form.jsp").forward(request, response);
            return;
        }

        LocalDate today = LocalDate.now();
        if (startDate.toLocalDate().isBefore(today)) {
            request.setAttribute("error", "Ngày bắt đầu không được là ngày trong quá khứ.");
            request.getRequestDispatcher("/public/employee/forms/leave_form.jsp").forward(request, response);
            return;
        }
        if (endDate.before(startDate)) {
            request.setAttribute("error", "Ngày kết thúc không được trước ngày bắt đầu.");
            request.getRequestDispatcher("/public/employee/forms/leave_form.jsp").forward(request, response);
            return;
        }

        long diffMs = endDate.getTime() - startDate.getTime();
        double totalDays = diffMs / (1000.0 * 60 * 60 * 24) + 1;

        // Xử lý file đính kèm
        String savedUrl = null, savedName = null;
        Part filePart = request.getPart("attachment");
        if (filePart != null && filePart.getSize() > 0) {
            String submitted = filePart.getSubmittedFileName();
            if (submitted != null && !submitted.isEmpty()) {
                String ext = submitted.contains(".") ? submitted.substring(submitted.lastIndexOf('.')).toLowerCase()
                        : "";
                String[] allowed = { ".xlsx", ".pdf", ".docx", ".doc", ".xls", ".jpg", ".png", ".zip" };
                boolean ok = false;
                for (String a : allowed) {
                    if (a.equals(ext)) {
                        ok = true;
                        break;
                    }
                }
                if (!ok) {
                    request.setAttribute("error", "Định dạng file không hợp lệ.");
                    request.getRequestDispatcher("/public/employee/forms/leave_form.jsp").forward(request, response);
                    return;
                }
                String uploadDir = "uploads/forms";
                String serverName = "FORM_" + me.getEmployeeId() + "_" + System.currentTimeMillis() + "_"
                        + java.util.UUID.randomUUID().toString().substring(0, 8) + ext;
                Path dir = Paths.get(getServletContext().getRealPath("/" + uploadDir));
                Files.createDirectories(dir);
                try (InputStream is = filePart.getInputStream()) {
                    Files.copy(is, dir.resolve(serverName));
                }
                savedUrl = uploadDir + "/" + serverName;
                savedName = sanitizeFileName(submitted);
            }
        }

        LeaveFormRequest fr = new LeaveFormRequest();
        fr.setFormCode("LEAVE-" + me.getEmployeeId() + "-" + System.currentTimeMillis());
        fr.setEmployeeId(me.getEmployeeId());
        fr.setFormTypeId(formTypeId);
        fr.setReason(isBlank(reason) ? null : reason.trim());
        fr.setStartDate(startDate);
        fr.setEndDate(endDate);
        fr.setTotalDays(totalDays);
        fr.setAttachmentUrl(savedUrl);
        fr.setAttachmentName(savedName);

        int id = formRequestDAO.addFormRequest(fr);
        if (id <= 0) {
            request.setAttribute("error", "Gửi đơn thất bại. Vui lòng thử lại.");
            request.getRequestDispatcher("/public/employee/forms/leave_form.jsp").forward(request, response);
            return;
        }
        request.getSession().setAttribute("success", "Đã gửi đơn nghỉ phép thành công.");
        response.sendRedirect(request.getContextPath() + "/v1/employee/forms/my-forms");
    }

    // Xử lý gửi Đơn Khiếu Nại
    private void handleComplaintFormSubmit(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        FormType ft = formTypeDAO.getByCode("COMPLAINT");
        if (ft == null) {
            request.getSession().setAttribute("error", "Loại đơn COMPLAINT không tồn tại.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }
        int formTypeId = ft.getFormTypeId();

        EmployeeDetailDTO me = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (me == null) {
            request.getSession().setAttribute("error", "Bạn chưa được gắn hồ sơ nhân viên.");
            response.sendRedirect(request.getContextPath() + "/v1/employee/dashboard");
            return;
        }

        String reason = request.getParameter("reason");
        if (isBlank(reason)) {
            request.setAttribute("error", "Vui lòng nhập nội dung khiếu nại.");
            request.getRequestDispatcher("/public/employee/forms/complaint_form.jsp").forward(request, response);
            return;
        }

        // Xử lý file đính kèm
        String savedUrl = null, savedName = null;
        Part filePart = request.getPart("attachment");
        if (filePart != null && filePart.getSize() > 0) {
            String submitted = filePart.getSubmittedFileName();
            if (submitted != null && !submitted.isEmpty()) {
                String ext = submitted.contains(".") ? submitted.substring(submitted.lastIndexOf('.')).toLowerCase()
                        : "";
                String[] allowed = { ".xlsx", ".pdf", ".docx", ".doc", ".xls", ".jpg", ".png", ".zip" };
                boolean ok = false;
                for (String a : allowed) {
                    if (a.equals(ext)) {
                        ok = true;
                        break;
                    }
                }
                if (!ok) {
                    request.setAttribute("error", "Định dạng file không hợp lệ.");
                    request.getRequestDispatcher("/public/employee/forms/complaint_form.jsp").forward(request, response);
                    return;
                }
                String uploadDir = "uploads/forms";
                String serverName = "FORM_" + me.getEmployeeId() + "_" + System.currentTimeMillis() + "_"
                        + java.util.UUID.randomUUID().toString().substring(0, 8) + ext;
                Path dir = Paths.get(getServletContext().getRealPath("/" + uploadDir));
                Files.createDirectories(dir);
                try (InputStream is = filePart.getInputStream()) {
                    Files.copy(is, dir.resolve(serverName));
                }
                savedUrl = uploadDir + "/" + serverName;
                savedName = sanitizeFileName(submitted);
            }
        }

        ComplaintFormRequest fr = new ComplaintFormRequest();
        fr.setFormCode("COMPLAINT-" + me.getEmployeeId() + "-" + System.currentTimeMillis());
        fr.setEmployeeId(me.getEmployeeId());
        fr.setFormTypeId(formTypeId);
        fr.setReason(reason.trim());
        fr.setAttachmentUrl(savedUrl);
        fr.setAttachmentName(savedName);
        // startDate, endDate, totalDays không có trong ComplaintFormRequest → DB lưu
        // NULL

        int id = formRequestDAO.addFormRequest(fr);
        if (id <= 0) {
            request.setAttribute("error", "Gửi đơn thất bại. Vui lòng thử lại.");
            request.getRequestDispatcher("/public/employee/forms/complaint_form.jsp").forward(request, response);
            return;
        }
        request.getSession().setAttribute("success", "Đã gửi đơn khiếu nại thành công.");
        response.sendRedirect(request.getContextPath() + "/v1/employee/forms/my-forms");
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

    private boolean isAcceptableXlsxContentType(String contentType) {
        String ct = contentType.toLowerCase();
        return ct.contains("openxmlformats-officedocument.spreadsheetml.sheet")
                || ct.contains("application/octet-stream")
                || ct.contains("application/zip");
    }

    private String sanitizeFileName(String name) {
        if (name == null) {
            return "attendance.xlsx";
        }

        String base = Paths.get(name).getFileName().toString();
        return base.replaceAll("[\\r\\n]", "");
    }

    private Time parseTimeOrNull(String raw) {
        String value = trimToNull(raw);
        if (value == null) {
            return null;
        }
        if (value.matches("\\d{2}:\\d{2}")) {
            value += ":00";
        }
        return Time.valueOf(value);
    }

    private String buildAttendanceFilterQuery(HttpServletRequest request) {
        StringBuilder qs = new StringBuilder();
        appendParamIfPresent(qs, request, "month");
        appendParamIfPresent(qs, request, "year");
        appendParamIfPresent(qs, request, "departmentId");
        appendParamIfPresent(qs, request, "employeeCode");
        return qs.toString();
    }

    private void appendParamIfPresent(StringBuilder qs, HttpServletRequest request, String name) {
        String value = trimToNull(request.getParameter(name));
        if (value != null) {
            qs.append(qs.length() == 0 ? '?' : '&').append(name).append('=')
                    .append(java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
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

    private boolean isAttendanceEditLocked(Date workDate) {
        if (workDate == null) {
            return false;
        }
        LocalDate deadline = workDate.toLocalDate()
                .withDayOfMonth(1)
                .plusMonths(1)
                .withDayOfMonth(5);
        return LocalDate.now().isAfter(deadline);
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
        request.setAttribute("canUnassignDept", perms.contains("UNASSIGN_DEPARTMENT"));
        request.setAttribute("canEditAttendance", perms.contains("EDIT_ATTENDANCE"));
    }

}
