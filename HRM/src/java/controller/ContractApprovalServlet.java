package controller;

import dao.EmploymentContractDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.ContractOperationResult;
import model.User;

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

    @Override
    public void init() throws ServletException {
        super.init();
        contractDAO = new EmploymentContractDAO();
        LOGGER.info("ContractApprovalServlet initialized. Approval workflow ready.");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // POST only - return 405 Method Not Allowed
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
                    result = contractDAO.approveContract(contractId);
                    break;
                case "reject":
                    if (reason == null || reason.trim().isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\":\"Rejection reason is required for reject action.\"}");
                        return;
                    }
                    result = contractDAO.rejectContract(contractId, reason.trim());
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
