package controller;

import dao.AdminUserDAO;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

@WebServlet(name = "AdminUserInformationController", urlPatterns = {"/v1/admin/users/*"})
public class AdminUserInformationController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AdminUserInformationController.class.getName());
    private static final String LOGIN_PATH = "/v1/login/login";
    private final AdminUserDAO adminUserDAO = new AdminUserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = getCurrentUser(request);
        if (!isAdmin(currentUser)) {
            response.sendRedirect(request.getContextPath() + LOGIN_PATH);
            return;
        }

        String action = request.getPathInfo();
        LOGGER.log(Level.INFO, "Admin user information action: {0}", action);

        switch (action != null ? action : "") {
            case "/detail":
            case "/view":
                showUserDetail(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute("user");
    }

    private boolean isAdmin(User user) {
        if (user == null || user.getRoleName() == null) {
            return false;
        }

        String role = user.getRoleName().trim().replaceAll("[^A-Za-z0-9]", "").toLowerCase();
        return "sysadmin".equals(role) || "admin".equals(role) || "administrator".equals(role);
    }

    private void showUserDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String rawUserId = request.getParameter("id");
        if (rawUserId == null || rawUserId.trim().isEmpty()) {
            forwardUserDetailWithError(request, response, "Missing user id.");
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(rawUserId.trim());
        } catch (NumberFormatException e) {
            forwardUserDetailWithError(request, response, "Invalid user id.");
            return;
        }

        User selectedUser = adminUserDAO.getUserById(userId);
        request.setAttribute("selectedUser", selectedUser);
        if (selectedUser == null) {
            request.setAttribute("error", "User not found.");
        }
        request.getRequestDispatcher("/public/admin/user_detail.jsp").forward(request, response);
    }

    private void forwardUserDetailWithError(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        request.setAttribute("error", error);
        request.getRequestDispatcher("/public/admin/user_detail.jsp").forward(request, response);
    }
}
