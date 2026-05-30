package controller;

import dao.*;
import dto.EmployeeDTO;
import dto.EmployeeDetailDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.*;
import model.User;

public class ManagerController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ManagerController.class.getName());
    private static final UserDAO userDAO = new UserDAO();
    private static final RoleDAO roleDAO = new RoleDAO();
    private static final EmployeeDAO employeeDAO = new EmployeeDAO();
    private static final DepartmentDAO departmentDAO = new DepartmentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        preventBackCache(response);

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || user.getRoleName() == null || !"HRMANAGER".equalsIgnoreCase(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/v1/auth/login");
            return;
        }

        String action = request.getPathInfo();
        if (action == null || action.equals("/")) {
            action = "/dashboard";
        }
        switch (action) {
            case "/dashboard":
                displayDashboard(request, response);
                break;
            case "/employee-list":
                displayDepartmentEmployees(request, response, user);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        preventBackCache(response);

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || user.getRoleName() == null || !"HRMANAGER".equalsIgnoreCase(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/v1/auth/login");
            return;
        }
    }

    private void displayDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int totalEmployees = employeeDAO.countTotal();
        int activeEmployees = employeeDAO.countActive();
        int inactiveEmployees = employeeDAO.countInactive();
        Map<String, Integer> deptChart = employeeDAO.countByDepartment();

        request.setAttribute("totalEmployees", totalEmployees);
        request.setAttribute("activeEmployees", activeEmployees);
        request.setAttribute("inactiveEmployees", inactiveEmployees);
        request.setAttribute("pendingLeaves", 0);
        request.setAttribute("deptChart", deptChart);
        request.getRequestDispatcher("/public/manager/dashboard.jsp").forward(request, response);
    }

    private void displayDepartmentEmployees(HttpServletRequest request, HttpServletResponse response,
                                             User user) throws ServletException, IOException {
        EmployeeDetailDTO manager = employeeDAO.getEmployeeByUserId(user.getUserId());

        // Manager chưa được gắn vào phòng ban nào => không có dữ liệu để xem.
        if (manager == null) {
            request.setAttribute("departmentName", null);
            request.setAttribute("employees", java.util.Collections.emptyList());
            request.setAttribute("error", "Bạn chưa được phân công vào phòng ban nào.");
            request.getRequestDispatcher("/public/manager/employee_list.jsp").forward(request, response);
            return;
        }

        List<EmployeeDetailDTO> employees = employeeDAO.getEmployeesByDepartmentId(manager.getDepartmentId());
        request.setAttribute("departmentName", manager.getDepartmentName());
        request.setAttribute("employees", employees);
        request.getRequestDispatcher("/public/manager/employee_list.jsp").forward(request, response);
    }

    private void preventBackCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }
}
