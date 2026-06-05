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

/**
 *
 * @author admin
 */
public class AuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = safe(request.getServletPath()) + safe(request.getPathInfo());
        String lower = path.toLowerCase();

        if (lower.contains("/v1/auth/")) {
            chain.doFilter(req, res);
            return;
        }

        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/v1/auth/login");
            return;
        }
        if (!isAllowed(lower, user.getRoleName())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập trang này");
            return;
        }

        chain.doFilter(req, res);
    }

    private boolean isAllowed(String lowerPath, String roleName) {
        String role = roleName == null ? "" : roleName.trim().toLowerCase();
        if (lowerPath.contains("/v1/systemadmin/")) {
            return role.equals("systemadmin");
        }
        if (lowerPath.contains("/v1/businessadmin/")) {
            return role.equals("businessadmin");
        }
        if (lowerPath.contains("/v1/manager/")) {
            return role.contains("manager");
        }
        if(lowerPath.contains("/v1/employee/")){
            return role.contains("employee");
        }
        return true;
    }

    private User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object u = session.getAttribute("user");
            if (u instanceof User) {
                return (User) u;
            }
        }
        return null;
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }
}
