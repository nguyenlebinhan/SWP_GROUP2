package controller;

import dal.DBContext;
import dao.EmploymentContractDAO;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;
import service.EmploymentContractService;
import model.ContractOperationResult;

/**
 * Servlet for HR Manual Contract Activation Flow (MVC2 Controller).
 * 
 * Handles POST requests from HR operators to manually activate DRAFT contracts.
 * Maps to URL pattern: /contracts/activate (registered in web.xml)
 * 
 * Architecture:
 * - Pure native Servlet (no annotations, registered in web.xml)
 * - Thread-safe: Service instantiated once in init(), stateless per-request execution
 * - Uses service layer for business logic separation
 * - Maps ContractOperationResult error codes to Vietnamese user-facing messages
 */
public class ContractActivationServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ContractActivationServlet.class.getName());
    private static final long serialVersionUID = 1L;

    // Service layer instance - thread-safe as EmploymentContractService is stateless
    private EmploymentContractService contractService;

    /**
     * Servlet initialization: construct the service layer with required dependencies.
     * Called once by container on servlet loading (load-on-startup=2 in web.xml).
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        // Construct DAO and DBContext (native JDBC, no DI framework)
        EmploymentContractDAO contractDAO = new EmploymentContractDAO();
        DBContext dbContext = new DBContext();
        
        // Initialize the service layer - stateless, safe for concurrent requests
        this.contractService = new EmploymentContractService(contractDAO, dbContext);
        
        LOGGER.log(Level.INFO, "ContractActivationServlet initialized. Service layer ready for HR manual activation flow.");
    }

    /**
     * Reject GET requests with HTTP 405 Method Not Allowed.
     * This endpoint only accepts POST for state-changing operations.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.log(Level.WARNING, "GET request rejected for /contracts/activate (POST only). IP: {0}", 
                   request.getRemoteAddr());
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, 
            "Phương thức GET không được hỗ trợ. Vui lòng sử dụng POST để kích hoạt hợp đồng.");
    }

    /**
     * Handles manual contract activation requests from HR UI.
     * 
     * Expected POST parameters:
     * - contractId (required): Integer ID of the DRAFT contract to activate
     * 
     * Flow:
     * 1. Validate contractId parameter (HTTP 400 if invalid)
     * 2. Call service.activateContract(contractId) 
     * 3. On SUCCESS: Redirect 302 to preview page with status=success
     * 4. On FAILURE: Forward to form page with Vietnamese error message as request attribute
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Prevent browser caching of sensitive operations
        preventBackCache(response);

        // Security: Validate user session (required for audit trail)
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user == null) {
            LOGGER.log(Level.WARNING, "Unauthorized activation attempt: no active session from IP: {0}", 
                       request.getRemoteAddr());
            response.sendRedirect(request.getContextPath() + "/v1/auth/login");
            return;
        }

        // Extract and validate contractId parameter
        String contractIdParam = request.getParameter("contractId");
        if (contractIdParam == null || contractIdParam.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Missing contractId parameter from user: {0} IP: {1}", 
                       new Object[]{user.getUserId(), request.getRemoteAddr()});
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu tham số contractId.");
            return;
        }

        int contractId;
        try {
            contractId = Integer.parseInt(contractIdParam.trim());
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid contractId format: {0} from user: {1} IP: {2}", 
                       new Object[]{contractIdParam, user.getUserId(), request.getRemoteAddr()});
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã hợp đồng không hợp lệ.");
            return;
        }

        LOGGER.log(Level.INFO, "HR Manual Activation Request: contractId={0} by userId={1}", 
                   new Object[]{contractId, user.getUserId()});

        // Execute the HR activation flow via Service Layer
        ContractOperationResult result;
        try {
            result = contractService.activateContract(contractId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected system error during HR activation for contract: " + contractId, e);
            // Map unexpected exceptions to SYSTEM_ERROR
            result = new ContractOperationResult(false, ContractOperationResult.SYSTEM_ERROR, 
                "Lỗi hệ thống không mong muốn: " + e.getMessage());
        }

        // Evaluate and respond based on operation result
        if (result.isSuccess()) {
            // SUCCESS: Redirect 302 to preview page with success indicator
            String redirectUrl = request.getContextPath() + "/v1/employee/contract-preview?id=" + contractId + "&status=success";
            LOGGER.log(Level.INFO, "HR Activation SUCCESS: contractId={0} redirected to {1}", 
                       new Object[]{contractId, redirectUrl});
            response.sendRedirect(redirectUrl);
        } else {
            // FAILURE: Map error code to Vietnamese message and FORWARD to form page
            String userMessage = mapErrorCodeToMessage(result.getErrorCode(), result.getMessage());
            LOGGER.log(Level.WARNING, "HR Activation FAILED: contractId={0} errorCode={1} message={2}", 
                       new Object[]{contractId, result.getErrorCode(), userMessage});
            
            // Set localized error message as request attribute for JSP display
            request.setAttribute("error", userMessage);
            request.setAttribute("contractId", contractId);
            
            // Forward back to the contract preview/form page so user sees the error
            // Using forward preserves request attributes (redirect would lose them)
            request.getRequestDispatcher("/v1/employee/contract-preview?id=" + contractId + "&status=error")
                   .forward(request, response);
        }
    }

    /**
     * Maps internal error codes from ContractOperationResult to Vietnamese user-facing messages.
     * 
     * @param errorCode the internal error code constant from ContractOperationResult
     * @param technicalDetails optional technical details for logging context
     * @return localized user-friendly Vietnamese message
     */
    private String mapErrorCodeToMessage(String errorCode, String technicalDetails) {
        if (errorCode == null) {
            return "Có lỗi không xác định xảy ra.";
        }
        
        switch (errorCode) {
            case ContractOperationResult.INVALID_STATUS:
                return "Hợp đồng không ở trạng thái cho phép kích hoạt (chỉ hợp đồng Nháp mới được kích hoạt thủ công).";
            case ContractOperationResult.DATE_MISMATCH:
                return "Ngày hiệu lực chưa đến hạn, không thể kích hoạt tự động.";
            case ContractOperationResult.SQL_ERROR:
                return "Lỗi cơ sở dữ liệu khi cập nhật hợp đồng. Vui lòng thử lại sau.";
            case ContractOperationResult.SYSTEM_ERROR:
                return "Lỗi hệ thống: " + (technicalDetails != null ? technicalDetails : "Lỗi không xác định.");
            default:
                LOGGER.log(Level.WARNING, "Unknown error code received: {0}", errorCode);
                return "Đã xảy ra lỗi không xác định: " + errorCode;
        }
    }

    /**
     * Helper: Prevent browser caching of sensitive POST responses.
     */
    private void preventBackCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }
}
