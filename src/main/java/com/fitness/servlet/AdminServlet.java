package com.fitness.servlet;

import com.fitness.concurrency.PlatformMetricsManager;
import com.fitness.model.Role;
import com.fitness.model.User;
import com.fitness.service.AdminService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Servlet handling Administrative actions: user activation/deactivation, content moderation, and metrics reset.
 */
public class AdminServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();
    private final PlatformMetricsManager metricsManager = PlatformMetricsManager.getInstance();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedUser") : null;

        if (user == null || user.getRole() != Role.ADMIN) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Administrator role required.");
            return;
        }

        String clientIp = req.getRemoteAddr();

        try {
            if ("/users/toggle".equals(path)) {
                int targetUserId = Integer.parseInt(req.getParameter("userId"));
                boolean targetState = Boolean.parseBoolean(req.getParameter("targetState"));
                adminService.toggleUserStatus(user.getId(), user.getEmail(), targetUserId, targetState, clientIp);
                resp.sendRedirect("/dashboard?msg=" + URLEncoder.encode("User status updated successfully.", StandardCharsets.UTF_8));
            } else if ("/plans/delete".equals(path)) {
                int planId = Integer.parseInt(req.getParameter("planId"));
                adminService.deleteWorkoutPlan(user.getId(), user.getEmail(), planId, clientIp);
                resp.sendRedirect("/dashboard?msg=" + URLEncoder.encode("Workout plan deleted by administrator.", StandardCharsets.UTF_8));
            } else if ("/metrics/reset".equals(path)) {
                metricsManager.resetDailyMetrics();
                resp.sendRedirect("/dashboard?msg=" + URLEncoder.encode("Daily platform metrics reset synchronized successfully.", StandardCharsets.UTF_8));
            } else {
                resp.sendRedirect("/dashboard");
            }
        } catch (Exception e) {
            resp.sendRedirect("/dashboard?error=" + URLEncoder.encode("Admin operation failed: " + e.getMessage(), StandardCharsets.UTF_8));
        }
    }
}
