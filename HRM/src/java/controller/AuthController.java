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
import java.util.logging.*;
import model.User;
import service.EmailService;
/**
 *
 * @author ADMIN
 */
public class AuthController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());
    private static final UserDAO userDAO = new UserDAO();
    private static final EmailService emailService = new EmailService();
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
        String action = request.getPathInfo();
        LOGGER.log(Level.INFO,"Action received in AuthController (GET): {0}", action);
        switch (action != null ? action : "") {
            case "/login":
                displayLoginForm(request, response);
                break;
            case "/forget-password":
                displayForgetPasswordForm(request, response);
                break;
            case "/change-password":
                displayChangePassword(request,response);
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
            case "/login":
                handleLoginRequest(request, response);
                break;
            case "/forget-password":
                handleForgetPasswordRequest(request, response);
                break;
            case "/change-password":
                handleChangePassword(request,response);
                break;                
            default:
                response.sendRedirect(request.getContextPath() + "/");
                break;
        }        
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }


    private void displayLoginForm(HttpServletRequest request, HttpServletResponse response) throws IOException, IOException, ServletException  {
        request.getRequestDispatcher("/public/auth/login.jsp").forward(request, response);
        
    }

    private void displayChangePassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean isAlreadyChanged = userDAO.isPasswordChanged((String)request.getSession().getAttribute("email"));
        if(isAlreadyChanged){
            request.getRequestDispatcher("/public/auth/change_password.jsp").forward(request, response);

        }else{
            response.sendRedirect(request.getContextPath() + "/");
        }
    }

    private void displayForgetPasswordForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/public/auth/forget_password.jsp").forward(request, response);
        
    }

    private void handleLoginRequest(HttpServletRequest request, HttpServletResponse response) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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
            boolean isAlreadyChanged = userDAO.updateIsTemporaryPassword(email,1);
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

    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        LOGGER.log(Level.INFO, "Processing request for changing password for email : {0}", request.getSession().getAttribute("email"));

        String sysPassword = request.getParameter("sysPassword");
        String newPassword = request.getParameter("yourPassword");
        
        User user = userDAO.getUserByEmail((String)request.getSession().getAttribute("email"));
        if(user == null){
            LOGGER.log(Level.INFO, "User not found for email: {0} (not revealing to user)", user.getEmail());
            request.setAttribute("success", "If email exists, you can get a password to reset");
            request.getRequestDispatcher("/public/auth/forget_password.jsp").forward(request, response);
            return;            
        }        

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

        boolean isSuccess = userDAO.updatePassword((String)request.getSession().getAttribute("email"), newPassword);
        request.getSession().invalidate();
        if (isSuccess) {
            boolean isAlreadyChanged = userDAO.updateIsTemporaryPassword((String)request.getSession().getAttribute("email"),0);
            if(isAlreadyChanged){
                request.setAttribute("success", "Your password has been reset");     
            }else{
                LOGGER.log(Level.SEVERE,"Failed to updated isTemporaryPassword for userId: {0} ",(String)request.getSession().getAttribute("email"));
                request.setAttribute("error", "Error");
                return;
            }            
            user.setPassword(newPassword);
            LOGGER.log(Level.INFO, "Password changed successfully for userId: {0}", user.getUserId());
            request.getSession().setAttribute("success", "Đổi mật khẩu thành công");
            response.sendRedirect(request.getPathInfo()+"/v1/auth/login");
        } else {
            LOGGER.log(Level.SEVERE, "Failed to change password for userId: {0}", user.getUserId());
            request.setAttribute("error", "Đổi mật khẩu thất bại. Vui lòng thử lại");
        }

        request.getRequestDispatcher("/public/auth/change_password.jsp").forward(request, response);
    }
}
