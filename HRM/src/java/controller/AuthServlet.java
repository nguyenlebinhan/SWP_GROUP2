package controller;

import com.google.gson.Gson;
import dal.UserDAO;
import entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/gmail-login"})
public class AuthServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        try {
            User user;
            if (request.getServletPath().equals("/gmail-login")) {
                user = handleGmailLogin(request);
            } else {
                user = handleAccountLogin(request);
            }

            if (user == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                writeJson(response, false, "Invalid login information", null);
                return;
            }

            request.getSession(true).setAttribute("authUser", user);
            writeJson(response, true, "Login successful", user);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, false, "Database error while logging in", null);
        }
    }

    private User handleAccountLogin(HttpServletRequest request) throws SQLException {
        String account = trimToNull(request.getParameter("account"));
        String password = trimToNull(request.getParameter("password"));

        if (account == null || password == null) {
            return null;
        }

        return userDAO.loginWithAccount(account, password);
    }

    private User handleGmailLogin(HttpServletRequest request) throws SQLException {
        String email = trimToNull(request.getParameter("email"));
        String googleId = trimToNull(request.getParameter("googleId"));

        if (email == null || !email.toLowerCase().endsWith("@gmail.com")) {
            return null;
        }

        return userDAO.loginWithGmail(email, googleId);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void writeJson(HttpServletResponse response, boolean success, String message, User user)
            throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("success", success);
        body.put("message", message);
        body.put("user", user);
        response.getWriter().write(gson.toJson(body));
    }
}
