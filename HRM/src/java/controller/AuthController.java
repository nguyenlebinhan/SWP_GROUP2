/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.UserDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.*;
import model.User;
import service.EmailService;
import utils.ConfigManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *
 * @author ADMIN
 */
public class AuthController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());
    private static final String AUTH_BASE_PATH = "/v1/auth";
    private static final UserDAO userDAO = new UserDAO();
    private static final EmailService emailService = new EmailService();
    private static final ConfigManager configManager = ConfigManager.getInstance();
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet AuthController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet AuthController at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        LOGGER.log(Level.INFO, "Action received in AuthController (GET): {0}", action);
        switch (action != null ? action : "") {
            case "/login":
                displayLoginForm(request, response);
                break;
            case "/dashboard":
                displayDashboard(request, response);
                break;
            case "/google":
                handleGoogleLoginRequest(request, response);
                break;
            case "/google/callback":
                handleGoogleCallback(request, response);
                break;
            case "/forget-password":
                displayForgetPasswordForm(request, response);
                break;
            case "/change-password":
                displayChangePassword(request, response);
                break;
            case "/logout":
                handleLogoutRequest(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/");
                break;
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //processRequest(request, response);
        String action = request.getPathInfo();
        LOGGER.log(Level.INFO, "Action received in AuthController (POST): {0}", action);
        switch (action != null ? action : "") {
            case "/login":
                handleLoginRequest(request, response);
                break;
            case "/forget-password":
                handleForgetPasswordRequest(request, response);
                break;
            case "/change-password":
                handleChangePassword(request, response);
                break;
            case "/logout":
                handleLogoutRequest(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/");
                break;
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void displayLoginForm(HttpServletRequest request, HttpServletResponse response) throws IOException, IOException, ServletException {
        request.getRequestDispatcher("/public/auth/login.jsp").forward(request, response);

    }

    private void displayDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = (User) request.getSession().getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/v1/auth/login");
            return;
        }

        String role = user.getRoleName();

        if (role == null) {
            response.sendRedirect(request.getContextPath() + "/v1/auth/login");
            return;
        }

        role = role.trim().replaceAll("[^A-Za-z0-9]", "").toLowerCase();

        if (role.equals("systemadmin")) {

            request.getRequestDispatcher("/public/systemadmin/dashboard.jsp")
                    .forward(request, response);

        } else if (role.equals("businessadmin")) {

            request.getRequestDispatcher("/public/businessadmin/dashboard.jsp")
                    .forward(request, response);

        } else {

            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "You don't have permission to access this page");
        }
    }

    private void displayChangePassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean isAlreadyChanged = userDAO.isPasswordChanged((String) request.getSession().getAttribute("email"));
        if (isAlreadyChanged) {
            request.getRequestDispatcher("/public/auth/change_password.jsp").forward(request, response);

        } else {
            response.sendRedirect(request.getContextPath() + "/");
        }
    }

    private void displayForgetPasswordForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/public/auth/forget_password.jsp").forward(request, response);

    }

    private void handleLoginRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String identifier = request.getParameter("username");
        String password = request.getParameter("password");

        if (identifier == null || identifier.trim().isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập username và mật khẩu");
            request.getRequestDispatcher("/public/auth/login.jsp").forward(request, response);
            return;
        }

        User user = userDAO.authenticate(identifier.trim(), password);
        if (user == null) {
            LOGGER.log(Level.WARNING, "Login failed for identifier: {0}", identifier);
            request.setAttribute("error", "Username hoặc mật khẩu không đúng");
            request.getRequestDispatcher("/public/auth/login.jsp").forward(request, response);
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);
        session.setAttribute("email", user.getEmail());
        LOGGER.log(Level.INFO, "Login successful for userId: {0}", user.getUserId());

        if (user.getIsTemporaryPassword()) {
            response.sendRedirect(request.getContextPath() + AUTH_BASE_PATH + "/change-password");
            return;
        }
        response.sendRedirect(request.getContextPath() + getDashboardPathByRole(user));
    }

    private void handleGoogleLoginRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String clientId = configManager.getProperty("google.client.id");
        if (clientId == null || clientId.trim().isEmpty()) {
            request.setAttribute("error", "Đăng nhập Google chưa được cấu hình OAuth client id");
            request.getRequestDispatcher("/public/auth/login.jsp").forward(request, response);
            return;
        }

        HttpSession session = request.getSession(true);
        String state = UUID.randomUUID().toString();
        session.setAttribute("googleOAuthState", state);

        String redirectUri = getGoogleRedirectUri(request);
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + encode(clientId)
                + "&redirect_uri=" + encode(redirectUri)
                + "&response_type=code"
                + "&scope=" + encode("openid email profile")
                + "&state=" + encode(state)
                + "&prompt=select_account";
        response.sendRedirect(authUrl);
    }

    private void handleGoogleCallback(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        String expectedState = (session != null) ? (String) session.getAttribute("googleOAuthState") : null;
        String actualState = request.getParameter("state");
        String code = request.getParameter("code");
        String oauthError = request.getParameter("error");

        if (oauthError != null) {
            request.setAttribute("error", "Google từ chối đăng nhập: " + oauthError);
            request.getRequestDispatcher("/public/auth/login.jsp").forward(request, response);
            return;
        }
        if (expectedState == null || actualState == null || !expectedState.equals(actualState) || code == null || code.isEmpty()) {
            request.setAttribute("error", "Phiên đăng nhập Google không hợp lệ");
            request.getRequestDispatcher("/public/auth/login.jsp").forward(request, response);
            return;
        }

        try {
            String accessToken = exchangeGoogleCode(request, code);
            String email = fetchGoogleEmail(accessToken);
            User user = userDAO.getUserByEmail(email);
            if (user == null) {
                request.setAttribute("error", "Tài khoản Google này chưa được cấp quyền trong hệ thống");
                request.getRequestDispatcher("/public/auth/login.jsp").forward(request, response);
                return;
            }

            session.removeAttribute("googleOAuthState");
            session.setAttribute("user", user);
            session.setAttribute("email", user.getEmail());
            response.sendRedirect(request.getContextPath() + getDashboardPathByRole(user));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Google login failed", e);
            request.setAttribute("error", "Không thể đăng nhập bằng Google. Vui lòng thử lại sau");
            request.getRequestDispatcher("/public/auth/login.jsp").forward(request, response);
        }
    }

    private String exchangeGoogleCode(HttpServletRequest request, String code) throws IOException, InterruptedException {
        String clientId = configManager.getProperty("google.client.id");
        String clientSecret = configManager.getProperty("google.client.secret");
        if (clientSecret == null || clientSecret.trim().isEmpty()) {
            throw new IOException("Missing Google OAuth client secret");
        }

        String body = "code=" + encode(code)
                + "&client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret)
                + "&redirect_uri=" + encode(getGoogleRedirectUri(request))
                + "&grant_type=authorization_code";

        HttpRequest tokenRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth2.googleapis.com/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> tokenResponse = httpClient.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
        if (tokenResponse.statusCode() < 200 || tokenResponse.statusCode() >= 300) {
            throw new IOException("Google token endpoint returned " + tokenResponse.statusCode());
        }

        JsonObject tokenJson = JsonParser.parseString(tokenResponse.body()).getAsJsonObject();
        return tokenJson.get("access_token").getAsString();
    }

    private String fetchGoogleEmail(String accessToken) throws IOException, InterruptedException {
        HttpRequest userInfoRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://openidconnect.googleapis.com/v1/userinfo"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        HttpResponse<String> userInfoResponse = httpClient.send(userInfoRequest, HttpResponse.BodyHandlers.ofString());
        if (userInfoResponse.statusCode() < 200 || userInfoResponse.statusCode() >= 300) {
            throw new IOException("Google userinfo endpoint returned " + userInfoResponse.statusCode());
        }

        JsonObject userInfo = JsonParser.parseString(userInfoResponse.body()).getAsJsonObject();
        boolean verified = userInfo.has("email_verified") && userInfo.get("email_verified").getAsBoolean();
        if (!userInfo.has("email") || !verified) {
            throw new IOException("Google account email is not verified");
        }
        return userInfo.get("email").getAsString();
    }

    private String getGoogleRedirectUri(HttpServletRequest request) {
        String configured = configManager.getProperty("google.redirect.uri");
        if (configured != null && !configured.trim().isEmpty()) {
            return configured.trim();
        }
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                + request.getContextPath() + AUTH_BASE_PATH + "/google/callback";
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void handleLogoutRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + AUTH_BASE_PATH + "/login");
    }

    private void handleForgetPasswordRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        LOGGER.log(Level.INFO, "Processing request for email : {0}", email);
        if (email == null || email.isEmpty()) {
            LOGGER.log(Level.WARNING, "Reset request failed: Email is empty");
            request.setAttribute("error", "Please enter an email");
            request.getRequestDispatcher("/public/auth/forget_password.jsp").forward(request, response);
            return;
        }

        User user = userDAO.getUserByEmail(email);
        if (user == null) {
            LOGGER.log(Level.INFO, "User not found for email: {0} (not revealing to user)", email);
            request.setAttribute("success", "If email exists, you can get a password to reset");
            request.getRequestDispatcher("/public/auth/forget_password.jsp").forward(request, response);
            return;
        }

        LOGGER.log(Level.INFO, "User found with userId: {0} and username : {1}", new Object[]{user.getUserId(), user.getFullName()});

        String password = userDAO.createResetPassword(user.getUserId());
        if (password != null) {
            LOGGER.log(Level.INFO, "Reset password created successfully for user id: {0}", user.getUserId());
            boolean isAlreadyChanged = userDAO.updateIsTemporaryPassword(email, 1);
            if (isAlreadyChanged) {
                emailService.sendResetPasswordEmailAsync(user.getEmail(), password);
                request.setAttribute("success", "A reset password has been sent to your email for a few second.");
                request.setAttribute("redirect", true);
                request.getSession().setAttribute("email", email);

            } else {
                LOGGER.log(Level.SEVERE, "Failed to updated isTemporaryPassword for userId: {0} ", user.getUserId());
                request.setAttribute("error", "Có lỗi xảy ra khi xử lý yêu cầu. Vui lòng thử lại");
            }
        } else {
            LOGGER.log(Level.SEVERE, "Failed to create reset password for user id: {0}", user.getUserId());
            request.setAttribute("error", "Error");
        }

        request.getRequestDispatcher("/public/auth/forget_password.jsp").forward(request, response);
    }

    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        LOGGER.log(Level.INFO, "Processing request for changing password for email : {0}", request.getSession().getAttribute("email"));

        String sysPassword = request.getParameter("sysPassword");
        String newPassword = request.getParameter("yourPassword");
        String confirmationPassword = request.getParameter("confirmationPassword");

        String email = (String) request.getSession().getAttribute("email");
        User user = userDAO.getUserByEmail(email);
        if (user == null) {
            LOGGER.log(Level.INFO, "User not found for email: {0}", email);
            request.setAttribute("error", "Không tìm thấy tài khoản. Vui lòng đăng nhập lại");
            request.getRequestDispatcher("/public/auth/change_password.jsp").forward(request, response);
            return;
        }

        if (sysPassword == null || sysPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ thông tin");
            request.getRequestDispatcher("/public/auth/change_password.jsp").forward(request, response);
            return;
        }

        boolean sysPasswordMatch = UserDAO.verifyPassword(sysPassword, user.getPassword())
                || sysPassword.equals(user.getPassword());
        if (!sysPasswordMatch) {
            request.setAttribute("error", "Mật khẩu hệ thống không đúng");
            request.getRequestDispatcher("/public/auth/change_password.jsp").forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmationPassword)) {
            request.setAttribute("error", "Mật khẩu xác thực đang không đúng");
            request.getRequestDispatcher("/public/auth/change_password.jsp").forward(request, response);
            return;
        }

        boolean isSuccess = userDAO.updatePassword(email, newPassword);
        if (isSuccess) {
            boolean updated = userDAO.updateIsTemporaryPassword(email, 0);
            if (!updated) {
                LOGGER.log(Level.SEVERE, "Failed to update isTemporaryPassword for email: {0}", email);
                request.setAttribute("error", "Lỗi hệ thống. Vui lòng thử lại");
                request.getRequestDispatcher("/public/auth/change_password.jsp").forward(request, response);
                return;
            }
            LOGGER.log(Level.INFO, "Password changed successfully for userId: {0}", user.getUserId());
            request.getSession().invalidate();
            response.sendRedirect(request.getContextPath() + "/v1/auth/login");
        } else {
            LOGGER.log(Level.SEVERE, "Failed to change password for userId: {0}", user.getUserId());
            request.setAttribute("error", "Đổi mật khẩu thất bại. Vui lòng thử lại");
            request.getRequestDispatcher("/public/auth/change_password.jsp").forward(request, response);
        }
    }

    private String getDashboardPathByRole(User user) {
        if (user == null || user.getRoleName() == null) {
            return AUTH_BASE_PATH + "/login";
        }
        String role = user.getRoleName().trim().replaceAll("[^A-Za-z0-9]", "").toLowerCase();
        if (role.equalsIgnoreCase("systemadmin")) {
            return "/v1/systemadmin/dashboard";
        } else if (role.equalsIgnoreCase("businessadmin")) {
            return "/v1/businessadmin/dashboard";
        } else if (role.substring(2).equalsIgnoreCase("manager")) {
            return "/v1/manager/dashboard";
        } else if (role.substring(2).equalsIgnoreCase("employee")) {
            return "/v1/employee/dashboard";
        } else {
            LOGGER.log(Level.WARNING, "Unknown role for userId {0}: {1}", new Object[]{user.getUserId(), user.getRoleName()});
            return AUTH_BASE_PATH + "/login";

        }
    }
}
