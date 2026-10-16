package com.fitness.filter;

import com.fitness.concurrency.PlatformMetricsManager;
import com.fitness.model.Role;
import com.fitness.model.User;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet Filter for Request Auditing and Role-Based Access Control.
 * Demonstrates Java Web Integration with Servlets & Filters.
 */
public class AuthenticationFilter implements Filter {

    private final PlatformMetricsManager metricsManager = PlatformMetricsManager.getInstance();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        metricsManager.recordRequest(uri);

        // Public resources, auth endpoints, spa showcase, exercise catalog, and API health checks
        if (uri.startsWith("/auth") || uri.startsWith("/api") || uri.startsWith("/public") || uri.equals("/favicon.ico") || uri.equals("/") || uri.startsWith("/spa") || uri.startsWith("/exercises") || uri.equals("/slots")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User loggedUser = session != null ? (User) session.getAttribute("loggedUser") : null;

        if (loggedUser == null) {
            resp.sendRedirect("/auth/login?msg=Please%20log%20in%20to%20continue");
            return;
        }

        // Role-based route enforcement
        if (uri.startsWith("/client") && loggedUser.getRole() != Role.CLIENT && loggedUser.getRole() != Role.ADMIN) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access restricted to Clients only.");
            return;
        }

        if (uri.startsWith("/coach") && loggedUser.getRole() != Role.COACH && loggedUser.getRole() != Role.ADMIN) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access restricted to Coaches only.");
            return;
        }

        if (uri.startsWith("/admin") && loggedUser.getRole() != Role.ADMIN) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access restricted to Administrators only.");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
