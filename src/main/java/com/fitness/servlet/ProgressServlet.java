package com.fitness.servlet;

import com.fitness.model.ProgressRecord;
import com.fitness.model.User;
import com.fitness.service.ProgressService;
import com.fitness.view.HtmlRenderer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Servlet handling client workout logging and progress analytics.
 */
public class ProgressServlet extends HttpServlet {

    private final ProgressService progressService = new ProgressService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedUser") : null;

        if (user == null) {
            resp.sendRedirect("/auth/login");
            return;
        }

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        List<ProgressRecord> history = progressService.getClientHistory(user.getId());
        List<ProgressRecord> topWorkouts = progressService.getTopCalorieWorkouts(user.getId(), 5);
        double totalCalories = progressService.getTotalCaloriesBurned(user.getId());

        out.println(HtmlRenderer.getCommonHeader("Workout & Progress History", user));

        out.println("<div class=\"hero-banner\">");
        out.println("    <h1 class=\"hero-title\">Workout History & Progress Analytics 📈</h1>");
        out.printf("    <p class=\"hero-sub\">Total Workouts: <strong>%d</strong> • Total Energy Burned: <strong>%.0f kcal</strong></p>%n",
                history.size(), totalCalories);
        out.println("</div>");

        // Top Burn Sessions (Sorted via custom comparator)
        out.println("<div class=\"section-header\"><h3 class=\"section-title\">🔥 Top Calorie Burn Sessions (Custom Comparator Sort)</h3></div>");
        if (topWorkouts.isEmpty()) {
            out.println("<p style=\"color: var(--text-muted); margin-bottom: 2rem;\">No workout logs yet.</p>");
        } else {
            out.println("<div class=\"table-container\">");
            out.println("    <table>");
            out.println("        <thead><tr><th>Rank</th><th>Date</th><th>Workout Plan</th><th>Duration</th><th>Calories Burned</th><th>Notes</th></tr></thead>");
            out.println("        <tbody>");
            int rank = 1;
            for (ProgressRecord r : topWorkouts) {
                out.printf("        <tr><td><strong>#%d</strong></td><td>%s</td><td>%s</td><td>%d min</td><td style=\"color: #f59e0b; font-weight:700;\">%.0f kcal</td><td>%s</td></tr>%n",
                        rank++, r.getRecordedAt().toString().substring(0, 16),
                        r.getWorkoutPlanTitle() != null ? HtmlRenderer.escape(r.getWorkoutPlanTitle()) : "Free Workout",
                        r.getDurationMinutes(), r.getCaloriesBurned(),
                        r.getNotes() != null ? HtmlRenderer.escape(r.getNotes()) : "-");
            }
            out.println("        </tbody>");
            out.println("    </table>");
            out.println("</div>");
        }

        // Full History
        out.println("<div class=\"section-header\"><h3 class=\"section-title\">Complete Workout History</h3></div>");
        if (history.isEmpty()) {
            out.println("<p style=\"color: var(--text-muted);\">No workout logs recorded yet.</p>");
        } else {
            out.println("<div class=\"table-container\">");
            out.println("    <table>");
            out.println("        <thead><tr><th>Date & Time</th><th>Workout Plan</th><th>Duration</th><th>Calories Burned</th><th>Body Weight</th><th>Notes</th></tr></thead>");
            out.println("        <tbody>");
            for (ProgressRecord r : history) {
                out.printf("        <tr><td>%s</td><td>%s</td><td>%d min</td><td style=\"color: var(--accent); font-weight: 700;\">%.0f kcal</td><td>%.1f kg</td><td>%s</td></tr>%n",
                        r.getRecordedAt().toString().substring(0, 19),
                        r.getWorkoutPlanTitle() != null ? HtmlRenderer.escape(r.getWorkoutPlanTitle()) : "Free Workout",
                        r.getDurationMinutes(), r.getCaloriesBurned(), r.getCurrentWeightKg(),
                        r.getNotes() != null ? HtmlRenderer.escape(r.getNotes()) : "-");
            }
            out.println("        </tbody>");
            out.println("    </table>");
            out.println("</div>");
        }

        out.println(HtmlRenderer.getCommonFooter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/log";

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedUser") : null;

        if (user == null) {
            resp.sendRedirect("/auth/login");
            return;
        }

        try {
            if ("/log".equals(path)) {
                String planIdStr = req.getParameter("workoutPlanId");
                Integer planId = (planIdStr != null && !planIdStr.trim().isEmpty()) ? Integer.parseInt(planIdStr) : null;
                double weight = Double.parseDouble(req.getParameter("currentWeightKg"));
                int duration = Integer.parseInt(req.getParameter("durationMinutes"));
                double calories = Double.parseDouble(req.getParameter("caloriesBurned"));
                String notes = req.getParameter("notes");

                // Invoking overloaded logProgress method
                progressService.logProgress(user.getId(), weight, calories, planId, duration, notes);

                resp.sendRedirect("/dashboard?msg=" + URLEncoder.encode("Workout successfully logged and metrics updated!", StandardCharsets.UTF_8));
            } else {
                resp.sendRedirect("/dashboard");
            }
        } catch (Exception e) {
            resp.sendRedirect("/dashboard?error=" + URLEncoder.encode("Failed to log workout: " + e.getMessage(), StandardCharsets.UTF_8));
        }
    }
}
