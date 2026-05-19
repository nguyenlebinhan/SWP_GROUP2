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
    private static final UserDAO userDAO = new UserDAO();
    private static final EmailService emailService = new EmailService();
    private static final ConfigManager configManager = ConfigManager.getInstance();
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
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
            out.println("<h1>Servlet AuthController at " + request.getContextPath () + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

//        if (user == null || user.getRoleName()== null || !"ADMIN".equalsIgnoreCase(user.getRoleName())) {
//            response.sendRedirect(request.getContextPath() + "/login");
//            return;
//        }        
        String action = request.getPathInfo();
        LOGGER.log(Level.INFO,"Action received in AuthController (GET): {0}", action);
        switch (action != null ? action : "") {
            case "/register":
                displayRegisterForm(request, response,user);
                break;
            case "/login":
                displayLoginForm(request, response,user);
                break;
            case "/dashboard":
                displayDashboard(request, response, user);
                break;
            case "/google":
                handleGoogleLoginRequest(request, response);
                break;
            case "/google/callback":
                handleGoogleCallback(request, response);
                break;
            case "/forget-password":
                displayForgetPasswordForm(request, response,user);
                break;
            case "/change-password":
                displayChangePassword(request,response);
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
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        //processRequest(request, response);
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        String action = request.getPathInfo();
        LOGGER.log(Level.INFO,"Action received in AuthController (POST): {0}", action);
        switch (action != null ? action : "") {
            case "/register":
                handleRegisterRequest(request, response,user);
                break;
            case "/login":
                handleLoginRequest(request, response,user);
                break;
            case "/forget-password":
                handleForgetPasswordRequest(request, response);
                break;
            case "/change-password":
                handleChangePassword(request,response,user);
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
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void displayRegisterForm(HttpServletRequest request, HttpServletResponse response,User user) throws ServletException, IOException {
        request.getRequestDispatcher("/public/auth/register.jsp").forward(request, response);
    }
    private void displayLoginForm(HttpServletRequest request, HttpServletResponse response,User user) throws IOException, IOException, ServletException  {
        if (user != null) {
            response.sendRedirect(request.getContextPath() + "/v1/auth/dashboard");
            return;
        }
        request.getRequestDispatcher("/public/auth/login.jsp").forward(request, response);
        
    }

    private void displayDashboard(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/v1/auth/login");
            return;
        }
        request.getRequestDispatcher("/public/admin/dashboard.jsp").forward(request, response);
    }

    private void displayChangePassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean isAlreadyChanged = userDAO.isPasswordChanged((String)request.getSession().getAttribute("email"));
        if(isAlreadyChanged){
            request.getRequestDispatcher("/public/auth/change_password.jsp").forward(request, response);
            request.getSession().invalidate();
        }else{
            response.sendRedirect(request.getContextPath() + "/");
        }
    }

    private void displayForgetPasswordForm(HttpServletRequest request, HttpServletResponse response,User user) throws ServletException, IOException {
        request.getRequestDispatcher("/public/auth/forget_password.jsp").forward(request, response);
        
    }

    private void handleRegisterRequest(HttpServletRequest request, HttpServletResponse response,User user) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void handleLoginRequest(HttpServletRequest request, HttpServletResponse response,User currentUser) throws ServletException, IOException {
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
            response.sendRedirect(request.getContextPath() + "/v1/auth/change-password");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/v1/auth/dashboard");
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
            response.sendRedirect(request.getContextPath() + "/v1/auth/dashboard");
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
                + request.getContextPath() + "/v1/auth/google/callback";
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void handleLogoutRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/v1/auth/login");
    }

    private void handleForgetPasswordRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {     
        String email = request.getParameter("email");
        LOGGER.log(Level.INFO,"Processing request for email : {0}",email);
        if(email == null || email.isEmpty()){        
            LOGGER.log(Level.WARNING, "Reset request failed: Email is empty");
            request.setAttribute("error", "Please enter an email");
            request.getRequestDispatcher("/public/auth/forget_password.jsp").forward(request, response);
            return;                
        }

        User user = userDAO.getUserByEmail(email);
        if(user == null){
            LOGGER.log(Level.INFO, "User not found for email: {0} (not revealing to user)", email);
            request.setAttribute("success", "If email exists, you can get a password to reset");
            request.getRequestDispatcher("/public/auth/forget_password.jsp").forward(request, response);
            return;            
        }

        LOGGER.log(Level.INFO,"User found with userId: {0} and username : {1}",new Object[]{user.getUserId(),user.getFullName()});

        String password = userDAO.createResetPassword(user.getUserId());
        if (password != null) {
            LOGGER.log(Level.INFO, "Reset password created successfully for user id: {0}", user.getUserId());
            boolean isAlreadyChanged = userDAO.updateIsTemporaryPassword(user.getUserId());
            if(isAlreadyChanged){
                emailService.sendResetPasswordEmailAsync(user.getEmail(),password);
                request.setAttribute("success", "A reset password has been sent to your email for a few second.");      
                request.setAttribute("redirect", true); 
                request.getSession().setAttribute("email", email);
    
                
            }else{
                LOGGER.log(Level.SEVERE,"Failed to updated isTemporaryPassword for userId: {0} ",user.getUserId());
                request.setAttribute("error", "Error");
                return;
            }
        } else {
            LOGGER.log(Level.SEVERE, "Failed to create reset password for user id: {0}", user.getUserId());
            request.setAttribute("error", "Error");
        }

        request.getRequestDispatcher("/public/auth/forget_password.jsp").forward(request, response);
    }

    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/v1/auth/login");
            return;
        }

        LOGGER.log(Level.INFO, "Processing request for changing password for userId : {0}", user.getUserId());

        String sysPassword = request.getParameter("sysPassword");
        String newPassword = request.getParameter("yourPassword");

        if (sysPassword == null || sysPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ thông tin");
            request.getRequestDispatcher("/public/auth/change_password.jsp").forward(request, response);
            return;
        }

        if (!sysPassword.equals(user.getPassword())) {
            request.setAttribute("error", "Mật khẩu hệ thống không đúng");
            request.getRequestDispatcher("/public/auth/change_password.jsp").forward(request, response);
            return;
        }

        boolean isSuccess = userDAO.updatePassword(user.getUserId(), newPassword);
        if (isSuccess) {
            user.setPassword(newPassword);
            request.getSession().setAttribute("user", user);
            LOGGER.log(Level.INFO, "Password changed successfully for userId: {0}", user.getUserId());
            request.setAttribute("success", "Đổi mật khẩu thành công");
        } else {
            LOGGER.log(Level.SEVERE, "Failed to change password for userId: {0}", user.getUserId());
            request.setAttribute("error", "Đổi mật khẩu thất bại. Vui lòng thử lại");
        }

        request.getRequestDispatcher("/public/auth/change_password.jsp").forward(request, response);
    }
}
