package controller;

import dao.EmploymentContractDAO;
import dao.RoleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.ContractOperationResult;
import model.User;
import service.EmploymentContractService;

/**
 * Servlet handling contract approval/rejection actions by HR Managers.
 * URL Pattern: /contracts/approve (POST only)
 * 
 * Actions supported:
 * - action=approve: Approve a PENDING_APPROVAL contract -> PENDING_ACTIVATION
 * - action=reject: Reject a PENDING_APPROVAL contract -> CANCELLED with rejectionReason
 */
public class ContractApprovalServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ContractApprovalServlet.class.getName());
    private EmploymentContractDAO contractDAO;
    private EmploymentContractService contractService;
    private RoleDAO roleDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        contractDAO = new EmploymentContractDAO();
        contractService = new EmploymentContractService(contractDAO, contractDAO.getDBContext());
        roleDAO = new RoleDAO();
        LOGGER.info("ContractApprovalServlet initialized. Approval workflow ready.");
    }

    private boolean isHrManager(User user) {
        String role = roleDAO.getRoleByUserId(user.getUserId());
        return "HRManager".equals(role);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/v1/auth/login");
            return;
        }
        
        String path = request.getServletPath();
        String action = request.getParameter("action");
        
        // Handle JSP view mappings
        if ("/v1/manager/contracts/pending".equals(path)) {
            // List pending contracts - HR Manager only
            if (!isHrManager(user)) {
                response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
                return;
            }
            try {
                request.setAttribute("pendingContracts", contractDAO.getPendingContracts());
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error fetching pending contracts", e);
                request.setAttribute("error", "Không thể tải danh sách hợp đồng chờ duyệt.");
            }
            request.getRequestDispatcher("/v1/manager/contract-pending-list.jsp").forward(request, response);
            return;
        }
        
        if ("/v1/manager/contracts/approve-preview".equals(path)) {
            // Preview contract for approval - HR Manager only
            if (!isHrManager(user)) {
                response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
                return;
            }
            String contractIdParam = request.getParameter("contractId");
            if (contractIdParam != null) {
                try {
                    int contractId = Integer.parseInt(contractIdParam);
                    request.setAttribute("contract", contractDAO.getContractById(contractId));
                } catch (NumberFormatException e) {
                    request.setAttribute("error", "ID hợp đồng không hợp lệ.");
                }
            }
            request.getRequestDispatcher("/v1/manager/contract-approval-preview.jsp").forward(request, response);
            return;
        }
        
        if ("/v1/manager/contracts/reject-dialog".equals(path)) {
            // Reject dialog - HR Manager only
            if (!isHrManager(user)) {
                response.sendRedirect(request.getContextPath() + "/v1/manager/dashboard");
                return;
            }
            String contractIdParam = request.getParameter("contractId");
            if (contractIdParam != null) {
                try {
                    int contractId = Integer.parseInt(contractIdParam);
                    request.setAttribute("contract", contractDAO.getContractById(contractId));
                } catch (NumberFormatException e) {
                    request.setAttribute("error", "ID hợp đồng không hợp lệ.");
                }
            }
            request.getRequestDispatcher("/v1/manager/contract-reject-dialog.jsp").forward(request, response);
            return;
        }
        
        // Default: Method not allowed for API endpoints
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"Method not allowed. Use POST.\"}");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Unauthorized. Please login.\"}");
            return;
        }

        if (!isHrManager(user)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"Chỉ HRManager mới được duyệt/từ chối hợp đồng.\"}");
            return;
        }

        String action = request.getParameter("action");
        String contractIdParam = request.getParameter("contractId");
        String reason = request.getParameter("reason");

        if (action == null || contractIdParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Missing required parameters: action and contractId.\"}");
            return;
        }

        int contractId;
        try {
            contractId = Integer.parseInt(contractIdParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Invalid contractId format.\"}");
            return;
        }

        ContractOperationResult result;

        try {
            switch (action) {
                case "approve":
                    result = contractService.approveContract(contractId, user.getUserId());
                    break;
                case "reject":
                    if (reason == null || reason.trim().isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\":\"Rejection reason is required for reject action.\"}");
                        return;
                    }
                    result = contractService.rejectContract(contractId, user.getUserId(), reason.trim());
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\":\"Invalid action. Supported: approve, reject.\"}");
                    return;
            }

            if (result.isSuccess()) {
                LOGGER.info("Contract " + action + " successful for contractId=" + contractId + " by userId=" + user.getUserId());
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"success\":true,\"message\":\"Hợp đồng đã được " 
                        + (action.equals("approve") ? "duyệt" : "từ chối") + " thành công.\"}");
            } else {
                LOGGER.warning("Contract " + action + " failed for contractId=" + contractId + ": " + result.getErrorCode());
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                String userMessage = mapErrorCodeToMessage(result.getErrorCode());
                response.getWriter().write("{\"success\":false,\"errorCode\":\"" + result.getErrorCode() 
                        + "\",\"message\":\"" + userMessage + "\"}");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during contract " + action + " for contractId=" + contractId, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Hệ thống gặp sự cố. Vui lòng thử lại sau.\"}");
        }
    }

    private String mapErrorCodeToMessage(String errorCode) {
        return switch (errorCode) {
            case ContractOperationResult.INVALID_STATUS -> 
                "Không thể thực hiện thao tác. Hợp đồng không ở trạng thái 'Chờ duyệt' hoặc đã được xử lý.";
            case ContractOperationResult.SQL_ERROR -> 
                "Lỗi cơ sở dữ liệu. Vui lòng thử lại.";
            case ContractOperationResult.SYSTEM_ERROR -> 
                "Hệ thống gặp sự cố. Vui lòng liên hệ quản trị viên.";
            default -> "Thao tác thất bại: " + errorCode;
        };
    }
}

