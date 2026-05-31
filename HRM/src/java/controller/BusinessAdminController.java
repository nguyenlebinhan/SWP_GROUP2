package controller;

import dao.DepartmentDAO;
import dao.EmployeeDAO;
import dao.PermissionDAO;
import dao.RoleDAO;
import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;
import model.User;
import static org.apache.tomcat.jakartaee.commons.lang3.StringUtils.isBlank;
import service.EmailService;

public class BusinessAdminController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(BusinessAdminController.class.getName());
    private static final UserDAO userDAO = new UserDAO();
    private static final EmailService emailService = new EmailService();
    private static final RoleDAO roleDAO = new RoleDAO();
    private static final PermissionDAO permissionDAO = new PermissionDAO();
    private static final EmployeeDAO employeeDAO = new EmployeeDAO();
    private static final DepartmentDAO departmentDAO = new DepartmentDAO();
    
//    @Override
//    protected void doGet(HttpServletRequest request,
//                         HttpServletResponse response)
//            throws ServletException, IOException {
//
//        String action = request.getPathInfo();
//
//        if (action == null || "/dashboard".equals(action)) {
//
//            request.getRequestDispatcher(
//                    "/public/businessadmin/dashboard.jsp")
//                    .forward(request, response);
//
//        } else {
//            response.sendRedirect(request.getContextPath() + "/");
//        }
//    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        preventBackCache(response);
        String action = request.getPathInfo();

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || user.getRoleName() == null || !"BUSINESSADMIN".equalsIgnoreCase(user.getRoleName())) {
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
            case "/my-profile":
                displayMyProfile(request, response);
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

        if (user == null || user.getRoleName() == null || !"BUSINESSADMIN".equalsIgnoreCase(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/v1/auth/login");
            return;
        }
        if (action == null || action.equals("/")) {
            displayDashboard(request, response);
            return;
        }
        switch (action) {

            case "/my-profile":
                handleUpdateMyProfile(request, response, user);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/");
                break;
        }
    }
    
    private void preventBackCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }
    
    private void displayDashboard(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        // Thay vì getAllUsers() rồi .size(), chỉ đếm trực tiếp
        int userSize = userDAO.countUsers("", "");
        request.setAttribute("userSize", userSize);
        request.getRequestDispatcher("/public/businessadmin/dashboard.jsp")
                .forward(request, response);
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
        request.getRequestDispatcher("/public/businessadmin/my_profile.jsp").forward(request, response);
    }
    
    private void handleUpdateMyProfile(HttpServletRequest request, HttpServletResponse response, User sessionUser) throws ServletException, IOException {
        String username = request.getParameter("username");
        String fullName = request.getParameter("fullName");
        String dob = request.getParameter("dob");
        String address = request.getParameter("address");

        if (isBlank(username) || isBlank(fullName)) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ tên đăng nhập và họ tên");
            request.setAttribute("currentUser", userDAO.getUserById(sessionUser.getUserId()));
            request.getRequestDispatcher("/public/businessadmin/my_profile.jsp").forward(request, response);
            return;
        }

        username = username.trim();
        fullName = fullName.trim();
        dob = isBlank(dob) ? null : dob.trim();
        address = isBlank(address) ? null : address.trim();

        if (userDAO.isUsernameExists(username, sessionUser.getUserId())) {
            request.setAttribute("error", "Tên đăng nhập đã tồn tại");
            request.setAttribute("currentUser", userDAO.getUserById(sessionUser.getUserId()));
            request.getRequestDispatcher("/public/businessadmin/my_profile.jsp").forward(request, response);
            return;
        }

        boolean updated = userDAO.updateMyProfile(sessionUser.getUserId(), username, fullName, dob, address);
        if (!updated) {
            request.setAttribute("error", "Cập nhật hồ sơ thất bại. Vui lòng thử lại");
            request.setAttribute("currentUser", userDAO.getUserById(sessionUser.getUserId()));
            request.getRequestDispatcher("/public/businessadmin/my_profile.jsp").forward(request, response);
            return;
        }

        User updatedUser = userDAO.getUserById(sessionUser.getUserId());
        request.getSession().setAttribute("user", updatedUser);
        request.getSession().setAttribute("success", "Cập nhật hồ sơ thành công");
        response.sendRedirect(request.getContextPath() + "/v1/businessadmin/my-profile");
    }
}