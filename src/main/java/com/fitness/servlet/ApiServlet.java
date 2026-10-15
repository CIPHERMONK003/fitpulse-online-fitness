package com.fitness.servlet;

import com.fitness.concurrency.NotificationQueueService;
import com.fitness.concurrency.PlatformMetricsManager;
import com.fitness.model.ProgressRecord;
import com.fitness.model.User;
import com.fitness.service.ProgressService;
import com.fitness.util.ApiResponse;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RESTful JSON API Servlet.
 * Demonstrates:
 * - Generics: ApiResponse<T>
 * - JSON responses
 * - Concurrency metrics inspection
 */
public class ApiServlet extends HttpServlet {

    private final ProgressService progressService = new ProgressService();
    private final PlatformMetricsManager metricsManager = PlatformMetricsManager.getInstance();
    private final NotificationQueueService notificationQueue = NotificationQueueService.getInstance();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/";

        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedUser") : null;

        if ("/system-stats".equals(path)) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("activeSessions", metricsManager.getActiveUserSessions());
            stats.put("totalRequests", metricsManager.getTotalRequestsHandled());
            stats.put("totalWorkouts", metricsManager.getTotalWorkoutsLogged());
            stats.put("cumulativeCalories", metricsManager.getCumulativeCaloriesBurned());
            stats.put("queuePending", notificationQueue.getQueueSize());
            stats.put("notificationsProcessed", notificationQueue.getProcessedCount());

            ApiResponse<Map<String, Object>> response = ApiResponse.success("Platform statistics", stats);
            out.print(gson.toJson(response));
            out.flush();

        } else if ("/progress-chart".equals(path)) {
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print(gson.toJson(ApiResponse.error("Authentication required.")));
                out.flush();
                return;
            }

            List<ProgressRecord> records = progressService.getClientHistory(user.getId());
            ApiResponse<List<ProgressRecord>> response = ApiResponse.success("Client workout history", records);
            out.print(gson.toJson(response));
            out.flush();

        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print(gson.toJson(ApiResponse.error("Unknown API endpoint: " + path)));
            out.flush();
        }
    }
}
