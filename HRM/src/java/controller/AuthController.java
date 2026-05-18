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
        //processRequest(request, response);
        String action = request.getPathInfo();
        LOGGER.log(Level.INFO,"Action received in AuthController (GET): {0}", action);
        switch (action != null ? action : "") {
            case "/register":
                displayRegisterForm(request, response);
                break;
            case "/login":
                displayLoginForm(request, response);
                break;
            case "/forget-password":
                displayForgetPasswordForm(request, response);
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
        String action = request.getPathInfo();
        LOGGER.log(Level.INFO,"Action received in AuthController (GET): {0}", action);
        switch (action != null ? action : "") {
            case "/register":
                handleRegisterRequest(request, response);
                break;
            case "/login":
                handleLoginRequest(request, response);
                break;
            case "/forget-password":
                handleForgetPasswordRequest(request, response);
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

    private void displayRegisterForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/public/auth/register.jsp").forward(request, response);
    }
    private void displayLoginForm(HttpServletRequest request, HttpServletResponse response) throws IOException, IOException, ServletException  {
        request.getRequestDispatcher("/public/auth/login.jsp").forward(request, response);
        
    }


    private void displayForgetPasswordForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/public/auth/forget_password.jsp").forward(request, response);
        
    }

    private void handleRegisterRequest(HttpServletRequest request, HttpServletResponse response) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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
            request.getRequestDispatcher("/jsp/auth/forget_password.jsp").forward(request, response);
            return;            
        }

        LOGGER.log(Level.INFO,"User found with userId: {0} and username : {1}",new Object[]{user.getUserId(),user.getFullName()});

        String password = userDAO.createResetPassword(user.getUserId());
        if (password != null) {
            LOGGER.log(Level.INFO, "Reset password created successfully for user id: {0}", user.getUserId());


            emailService.sendPasswordResetEmail(user.getEmail(),user.getFullName(),password);
            request.setAttribute("success", "A reset password has been sent to your email for a few second.");       
        } else {
            LOGGER.log(Level.SEVERE, "Failed to create reset token for user id: {0}", user.getUserId());
            request.setAttribute("error", "Error");
        }

        request.getRequestDispatcher("/public/auth/forget_password.jsp").forward(request, response);
    }
}
