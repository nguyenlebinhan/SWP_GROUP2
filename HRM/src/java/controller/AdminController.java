/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.*;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.logging.*;
import model.*;
import service.*;
import java.util.*;

/**
 *
 * @author ADMIN
 */
public class AdminController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminController.class.getName());
    private static final UserDAO userDAO = new UserDAO();
    private static final EmailService emailService = new EmailService();
    private static final RoleDAO roleDAO = new RoleDAO();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {

            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet AdminController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet AdminController at " + request.getContextPath() + "</h1>");
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

        if (user == null || user.getRoleName() == null || !"ADMIN".equalsIgnoreCase(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/login");
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
            case "/user-list":
                displayUserList(request, response);
                break;
            case "/add-user":
                displayAddUserForm(request, response);
                break;
            case "/view-user-detail":
                displayUserDetail(request,response);
                break;
            case "/change-status":
                handleChangingStatus(request, response);
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

        if (user == null || user.getRoleName() == null || !"ADMIN".equalsIgnoreCase(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (action == null || action.equals("/")) {
            displayDashboard(request, response);
            return;
        }
        switch (action) {
            case "/add-user":
                handleAddUser(request, response);
                break;  
//            case "/change-status-role":
//                handleChangingStatusRole(request,response);
//                break;
            default:
                response.sendRedirect(request.getContextPath() + "/");
                break;
        }
    }

    private void displayAddUserForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Role> roles = roleDAO.getAllRoles();
        request.setAttribute("roles", roles);
        request.getRequestDispatcher("/public/admin/add_user.jsp").forward(request, response);
    }

    private void displayChangeStatus(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/public/admin/change_status.jsp").forward(request, response);
    }


      

    private void handleAddUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username  = request.getParameter("username");
        String email     = request.getParameter("email");
        String password  = request.getParameter("password");
        String fullName  = request.getParameter("fullName");
        String dob       = request.getParameter("dob");
        String gender    = request.getParameter("gender");
        String address   = request.getParameter("address");
        int roleId = Integer.parseInt(request.getParameter("role_selection"));
        

        boolean isSuccess = userDAO.addUser(username,email, password, fullName, dob, gender ,address, roleId);
        if (!isSuccess) {
            request.setAttribute("roles", roleDAO.getAllRoles());
            request.setAttribute("error", "Thêm người dùng thất bại. Email có thể đã tồn tại.");
            request.getRequestDispatcher("/public/admin/add_user.jsp").forward(request, response);
            return;
        }
        emailService.sendResetPasswordEmailAsync(email, password);
        LOGGER.log(Level.INFO, "User added and password sent to: {0}", email);
        request.getSession().setAttribute("success", "Thêm người dùng thành công. Mật khẩu tạm thời đã được gửi đến email.");
        response.sendRedirect(request.getContextPath() + "/v1/admin/user-list");
    }
    
    

    private void handleChangingStatus(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int status = Integer.parseInt(request.getParameter("status"));
        int userId = Integer.parseInt(request.getParameter("id"));
        boolean isUpdated = userDAO.handleStatus(status, userId);

        if (isUpdated) {
            request.getSession().setAttribute("success", "Cập nhật trạng thái thành công");
        } else {
            request.getSession().setAttribute("error", "Cập nhật trạng thái không thành công");
        }
        response.sendRedirect(request.getContextPath() + "/v1/admin/user-list");
    }

    private void displayDashboard(HttpServletRequest request,HttpServletResponse response)throws ServletException, IOException {
        request.getRequestDispatcher("/public/admin/dashboard.jsp").forward(request, response);
    }
    
    private void displayUserList(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        
        UserDAO dao = new UserDAO();
        List<User> list = dao.getAllUsers();
        
        request.setAttribute("list", list);
        
        request.getRequestDispatcher("/public/admin/user_list.jsp")
                .forward(request, response);
    }

//    private void handleChangingStatusRole(HttpServletRequest request, HttpServletResponse response) {
//        int roleStatus = Integer.parseInt(request.getParameter("roleStatus"));
//        boolean isUpdated = roleDAO.handleStatus(roleStatus, );
//        if(isUpdated){
//            request.getSession().setAttribute("success", "Cập nhật trạng thái thành công");
//            response.sendRedirect(request.getContextPath() +"/");
//        }else{
//            request.setAttribute("error", "Cập nhật trạng thái không thành công");
//            request.getRequestDispatcher("/public/admin/add_user.jsp").forward(request, response);
//        }
//    }

    private void displayUserDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String rawUserId = request.getParameter("id");
        if (rawUserId == null || rawUserId.trim().isEmpty()) {
            request.setAttribute("error", "Khhông thể hiển thị");
            request.getRequestDispatcher("/public/admin/user_detail.jsp").forward(request, response);
        }

        int userId = Integer.parseInt(rawUserId);

        User selectedUser = userDAO.getUserById(userId);
        if (selectedUser == null) {
            request.setAttribute("error", "User not found.");
        }
        request.setAttribute("selectedUser", selectedUser);        
        request.getRequestDispatcher("/public/admin/user_detail.jsp").forward(request, response);
    }
}
