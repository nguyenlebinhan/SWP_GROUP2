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
public class AdminController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AdminController.class.getName());
    private static final UserDAO userDAO = new UserDAO();
    private static final EmailService emailService = new EmailService();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet AdminController</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet AdminController at " + request.getContextPath () + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    } 

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        String action = request.getPathInfo();

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || user.getRoleName()== null || !"ADMIN".equalsIgnoreCase(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        switch (action != null ? action : "") {
            case "/add-user":
                displayAddUserForm(request,response);
                break;
            case "/change-status":
                displayChangeStatus(request,response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        String action = request.getPathInfo();

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || user.getRoleName()== null || !"ADMIN".equalsIgnoreCase(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        switch (action != null ? action : "") {
            case "/add-user":
                handleAddUser(request, response);
                break;
            case "/change-status":
                handleChangingStatus(request,response,user);
                break;                
            default:
                response.sendRedirect(request.getContextPath() + "/");
                break;
        }
    }
    private void displayAddUserForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/public/admin/add_user.jsp").forward(request, response);
    }    

    private void displayChangeStatus(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/public/admin/change_status.jsp").forward(request, response);
    }    

    private void handleAddUser(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
        String email     = request.getParameter("email");
        String password  = request.getParameter("password");
        String fullName  = request.getParameter("fullName");
        String dob       = request.getParameter("dob");
        String gender    = request.getParameter("gender");
        String address   = request.getParameter("address");
        int roleId = Integer.parseInt(request.getParameter("roleId"));

        boolean isSuccess = userDAO.addUser(email, password, fullName, dob, gender ,address, roleId);
        if (!isSuccess){
            request.setAttribute("error", "Thêm người dùng thất bại. Email có thể đã tồn tại.");
            request.getRequestDispatcher("/public/admin/add_user.jsp").forward(request, response);
            return;
        }
        emailService.sendResetPasswordEmailAsync(email, password);
        LOGGER.log(Level.INFO, "User added and password sent to: {0}", email);
        request.setAttribute("success", "Thêm người dùng thành công. Mật khẩu tạm thời đã được gửi đến email.");
        request.getRequestDispatcher("/public/admin/add_user.jsp").forward(request, response);
    }


    private void handleChangingStatus(HttpServletRequest request, HttpServletResponse response,User user) throws ServletException, IOException {
        int status = Integer.parseInt(request.getParameter("status"));
        boolean isUpdated = userDAO.handleStatus(status, user.getUserId());
        if(isUpdated){
            request.setAttribute("success", "Cập nhật trạng thái thành công");
        }else{
            request.setAttribute("error", "Cập nhật trạng thái không thành công");
        }
        
        request.getRequestDispatcher("/public/admin/add_user.jsp").forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
