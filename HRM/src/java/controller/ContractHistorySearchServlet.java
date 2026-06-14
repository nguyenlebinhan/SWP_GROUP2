package controller;

import dao.EmploymentContractDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import model.ContractAuditLog;
import model.User;

public class ContractHistorySearchServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final EmploymentContractDAO contractDAO = new EmploymentContractDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/v1/auth/login");
            return;
        }

        // Session key MUST be "permissions" to match sidebar.jsp
        Set<String> perms = (Set<String>) session.getAttribute("permissions");
        boolean isHrManager = perms != null && perms.contains("PERM_VIEW_ALL_CONTRACTS");

        // Extract search parameters
        Integer targetEmpId = null;
        String keyword = null;
        Integer deptId = null;

        String empIdParam = request.getParameter("employeeId");
        if (empIdParam != null && !empIdParam.isBlank()) {
            try {
                targetEmpId = Integer.parseInt(empIdParam);
            } catch (NumberFormatException ignored) {}
        }

        keyword = request.getParameter("keyword");

        String deptIdParam = request.getParameter("deptId");
        if (deptIdParam != null && !deptIdParam.isBlank()) {
            try {
                deptId = Integer.parseInt(deptIdParam);
            } catch (NumberFormatException ignored) {}
        }

        List<ContractAuditLog> history = contractDAO.searchContractHistory(
            targetEmpId, keyword, deptId, user.getUserId(), isHrManager
        );

        request.setAttribute("history", history);
        request.setAttribute("keyword", keyword);
        request.setAttribute("employeeId", targetEmpId);
        request.setAttribute("deptId", deptId);
        request.getRequestDispatcher("/web/v1/employee/contract-audit-history.jsp").forward(request, response);
    }
}