package controller;

import dao.PermissionDAO;
import dal.DBContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;

/**
 * Authorization Filter for Contract Administrative Endpoints.
 * 
 * This filter intercepts requests to sensitive contract management URLs
 * and verifies that the authenticated user possesses the required permission
 * by dynamically querying the database RBAC system.
 * 
 * Configuration (in web.xml):
 * - <init-param> permissionCode: The PermCode required to access the mapped URLs
 * - Mapped to: /contracts/approve, /contracts/all, etc.
 * 
 * Security Features:
 * - No hardcoded role checks (dynamic DB lookup)
 * - Session-based user extraction (IDOR protection)
 * - Fail-secure: deny by default on any error
 * - HTTP 403 Forbidden for unauthorized access
 */
public class ContractAuthorizationFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(ContractAuthorizationFilter.class.getName());
    private static final String ACCESS_DENIED_PAGE = "/access-denied.jsp";
    
    private String requiredPermissionCode;
    private PermissionDAO permissionDAO;
    private DBContext dbContext;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.requiredPermissionCode = filterConfig.getInitParameter("permissionCode");
        this.permissionDAO = new PermissionDAO();
        this.dbContext = new DBContext();
        
        if (requiredPermissionCode == null || requiredPermissionCode.trim().isEmpty()) {
            throw new ServletException("ContractAuthorizationFilter requires 'permissionCode' init-param");
        }
        
        LOGGER.info("ContractAuthorizationFilter initialized. Required permission: " + requiredPermissionCode);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 1. Extract authenticated user from session
        HttpSession session = httpRequest.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            LOGGER.warning("Unauthorized access attempt to " + httpRequest.getRequestURI() + " - No user in session");
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập để truy cập.");
            return;
        }

        // 2. Check permission via database RBAC
        boolean hasPermission = false;
        Connection conn = null;
        
        try {
            conn = dbContext.getConnection();
            Set<String> userPermissions = permissionDAO.getPermissionCodesByUserId(conn, user.getUserId());
            
            hasPermission = userPermissions.contains(requiredPermissionCode);
            
            if (hasPermission) {
                LOGGER.fine("Access granted: userId=" + user.getUserId() + " has permission=" + requiredPermissionCode 
                        + " for " + httpRequest.getRequestURI());
            } else {
                LOGGER.warning("Access denied: userId=" + user.getUserId() + " lacks permission=" + requiredPermissionCode 
                        + " for " + httpRequest.getRequestURI() + ". User permissions: " + userPermissions);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during permission check for userId=" + user.getUserId(), e);
            // Fail-secure: deny access on DB error
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi hệ thống khi kiểm tra quyền truy cập.");
            return;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }

        // 3. Enforce authorization
        if (hasPermission) {
            chain.doFilter(request, response);
        } else {
            // Send 403 Forbidden
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            
            // If AJAX/fetch request, return JSON
            String acceptHeader = httpRequest.getHeader("Accept");
            String xRequestedWith = httpRequest.getHeader("X-Requested-With");
            boolean isAjax = "XMLHttpRequest".equals(xRequestedWith) 
                    || (acceptHeader != null && acceptHeader.contains("application/json"));
            
            if (isAjax) {
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.getWriter().write("{\"error\":\"Bạn không có quyền truy cập tính năng này.\"}");
            } else {
                // Forward to access denied page
                request.getRequestDispatcher(ACCESS_DENIED_PAGE).forward(request, response);
            }
        }
    }

    @Override
    public void destroy() {
        // Cleanup if needed
        LOGGER.info("ContractAuthorizationFilter destroyed.");
    }
}

