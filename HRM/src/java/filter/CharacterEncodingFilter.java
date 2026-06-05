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
import java.io.IOException;

/**
 *
 * @author admin
 */
public class CharacterEncodingFilter implements Filter {

    private static final String ENCODING = "UTF-8";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!isExcluded(request)) {
            request.setCharacterEncoding(ENCODING);
            response.setCharacterEncoding(ENCODING);
        }
        chain.doFilter(request, response);
    }

    private boolean isExcluded(ServletRequest request) {
        if (request instanceof HttpServletRequest) {
            String uri = ((HttpServletRequest) request).getRequestURI();
            return uri != null && uri.contains("/v1/auth/");
        }
        return false;
    }
}
