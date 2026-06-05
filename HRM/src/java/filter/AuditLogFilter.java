/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Filter.java to edit this template
 */
package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.User;
import service.AuditLogService;

/**

 *
 * @author admin
 */
public class AuditLogFilter implements Filter {

    private static final AuditLogService auditLogService = new AuditLogService();

    private static final String[] ID_PARAMS = {"id", "employeeId", "userId", "departmentId", "roleId", "contractId"};

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

       
        Integer userId = currentUserId(request);
        Integer recordId = extractRecordId(request);
        String action = deriveAction(request.getMethod(), request.getPathInfo(), request.getServletPath());
        String endpoint = truncate(safe(request.getServletPath()) + safe(request.getPathInfo()), 50);
        String detail = truncate(request.getMethod() + " " + request.getRequestURI()
                + (request.getQueryString() != null ? "?" + request.getQueryString() : ""), 1000);
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        try {
            chain.doFilter(req, res);
        } finally {
            if (request.getAttribute("auditLogged") == null) {
                String status = response.getStatus() >= 400 ? "FAILED" : "SUCCESS";
                auditLogService.logAsync(userId, action, endpoint, recordId,
                        null, detail, ip, userAgent, status);
            }
        }
    }

    private Integer currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object u = session.getAttribute("user");
            if (u instanceof User) {
                return ((User) u).getUserId();
            }
        }
        return null;
    }

    private Integer extractRecordId(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
            return null;
        }
        for (String name : ID_PARAMS) {
            String v = request.getParameter(name);
            if (v != null && v.matches("\\d+")) {
                try {
                    return Integer.valueOf(v);
                } catch (NumberFormatException ignored) {
                    // bỏ qua, thử param tiếp theo
                }
            }
        }
        return null;
    }

    private String deriveAction(String method, String pathInfo, String servletPath) {
        String p = (safe(servletPath) + safe(pathInfo)).toLowerCase();
        if (p.contains("/login")) {
            return "LOGIN";
        }
        if (p.contains("/logout")) {
            return "LOGOUT";
        }
        if (p.contains("import")) {
            return "IMPORT";
        }
        if ("GET".equalsIgnoreCase(method)) {
            return "VIEW";
        }
        
        if (p.contains("add") || p.contains("create") || p.contains("assign")) {
            return "CREATE";
        }
        if (p.contains("update") || p.contains("edit") || p.contains("reassign") || p.contains("change")) {
            return "UPDATE";
        }
        if (p.contains("delete") || p.contains("deactivate") || p.contains("unassign") || p.contains("remove")) {
            return "DELETE";
        }
        return "ACTION";
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }

    private String truncate(String v, int max) {
        if (v == null) {
            return null;
        }
        return v.length() <= max ? v : v.substring(0, max);
    }
}
