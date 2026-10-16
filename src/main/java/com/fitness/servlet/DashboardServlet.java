package com.fitness.servlet;

import com.fitness.concurrency.NotificationQueueService;
import com.fitness.concurrency.PlatformMetricsManager;
import com.fitness.model.*;
import com.fitness.service.*;
import com.fitness.view.HtmlRenderer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Main Dynamic Dashboard Servlet router with role-based rendering.
 * Demonstrates:
 * - Servlets & Web Integration
 * - Polymorphism across User and WorkoutPlan objects
 * - Dynamic HTML generation with interactive components
 */
public class DashboardServlet extends HttpServlet {

    private final UserService userService = new UserService();
    private final WorkoutPlanService planService = new WorkoutPlanService();
    private final ProgressService progressService = new ProgressService();
    private final CoachFeedbackService feedbackService = new CoachFeedbackService();
    private final AdminService adminService = new AdminService();
    private final PlatformMetricsManager metricsManager = PlatformMetricsManager.getInstance();
    private final NotificationQueueService notificationQueue = NotificationQueueService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedUser") : null;

        if (user == null) {
            resp.sendRedirect("/auth/login");
            return;
        }

        // Re-fetch latest user profile
        try {
            user = userService.getUserById(user.getId());
            session.setAttribute("loggedUser", user);
        } catch (Exception ignored) {}

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println(HtmlRenderer.getCommonHeader("Dashboard", user));

        String msg = req.getParameter("msg");
        String error = req.getParameter("error");
        if (msg != null) out.println(HtmlRenderer.renderAlert("success", msg));
        if (error != null) out.println(HtmlRenderer.renderAlert("error", error));

        switch (user.getRole()) {
            case CLIENT:
                renderClientDashboard((ClientUser) user, out);
                break;
            case COACH:
                renderCoachDashboard((CoachUser) user, out);
                break;
            case ADMIN:
                renderAdminDashboard((AdminUser) user, out);
                break;
        }

        out.println(HtmlRenderer.getCommonFooter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    private void renderClientDashboard(ClientUser client, PrintWriter out) {
        double totalCalories = progressService.getTotalCaloriesBurned(client.getId());
        int workoutCount = progressService.getWorkoutCount(client.getId());
        List<WorkoutPlan> enrolledPlans = planService.getEnrolledPlans(client.getId());
        List<ProgressRecord> history = progressService.getClientHistory(client.getId());
        List<CoachFeedback> feedbackList = feedbackService.getFeedbackForClient(client.getId());
        Map<String, Double> weightSummary = progressService.getWeightTrendSummary(client.getId());

        out.println("<div class=\"hero-banner\">");
        out.println("    <h1 class=\"hero-title\">Welcome back, " + HtmlRenderer.escape(client.getName()) + "! 🚀</h1>");
        out.printf("    <p class=\"hero-sub\">Goal: <strong>%s</strong> • Activity Level: <strong>%s</strong> • BMI: <strong>%.1f (%s)</strong></p>%n",
                HtmlRenderer.escape(client.getFitnessGoal()), client.getActivityLevel(), client.calculateBMI(), client.getBMICategory());
        out.println("</div>");

        // Stat cards
        out.println("<div class=\"stats-grid\">");
        out.println("    <div class=\"stat-card\">");
        out.println("        <span class=\"stat-label\">Current Weight</span>");
        out.printf("        <span class=\"stat-value\">%.1f <small style=\"font-size: 1rem;\">kg</small></span>%n", client.getCurrentWeightKg());
        out.printf("        <span class=\"stat-meta\">Target: %.1f kg</span>%n", client.getTargetWeightKg());
        out.println("    </div>");

        out.println("    <div class=\"stat-card\">");
        out.println("        <span class=\"stat-label\">Workouts Completed</span>");
        out.printf("        <span class=\"stat-value\">%d</span>%n", workoutCount);
        out.println("        <span class=\"stat-meta\">All-time logged</span>");
        out.println("    </div>");

        out.println("    <div class=\"stat-card\">");
        out.println("        <span class=\"stat-label\">Total Energy Burned</span>");
        out.printf("        <span class=\"stat-value\">%.0f <small style=\"font-size: 1rem;\">kcal</small></span>%n", totalCalories);
        out.println("        <span class=\"stat-meta\">Synchronized Metric</span>");
        out.println("    </div>");

        out.println("    <div class=\"stat-card\">");
        out.println("        <span class=\"stat-label\">Assigned Coach</span>");
        out.printf("        <span class=\"stat-value\" style=\"font-size: 1.3rem;\">%s</span>%n",
                client.getAssignedCoachName() != null ? HtmlRenderer.escape(client.getAssignedCoachName()) : "Self-Guided");
        out.println("        <span class=\"stat-meta\">Personal Mentor</span>");
        out.println("</div>");

        // Fast Action Buttons: Spa, Slot Booking, Exercise Library
        out.println("<div style=\"display: flex; gap: 1rem; margin-bottom: 2rem; flex-wrap: wrap;\">");
        out.println("    <a href=\"/slots\" class=\"btn btn-primary\">📅 Book Live Training & Spa Slots</a>");
        out.println("    <a href=\"/spa\" class=\"btn btn-outline\" style=\"border-color: #f472b6; color: #f472b6;\">🧖‍♀️ Spa & Thermal Recovery</a>");
        out.println("    <a href=\"/exercises\" class=\"btn btn-outline\">📖 Interactive Exercise Library</a>");
        out.println("</div>");

        // Two Column: Quick Log Workout + Active Enrolled Plans
        out.println("<div style=\"display: grid; grid-template-columns: 1fr 1fr; gap: 2rem; margin-bottom: 2.5rem;\">");

        // Column 1: Log Workout Form
        out.println("    <div class=\"card\" style=\"box-shadow: none;\">");
        out.println("        <div class=\"card-header\"><h3 class=\"card-title\">📝 Log Today's Workout</h3></div>");
        out.println("        <form method=\"POST\" action=\"/progress/log\">");
        out.println("            <div class=\"form-group\">");
        out.println("                <label class=\"form-label\">Associated Workout Plan</label>");
        out.println("                <select name=\"workoutPlanId\" class=\"form-control\" id=\"planSelect\" onchange=\"updateCalorieEstimate()\">");
        out.println("                    <option value=\"\">-- Free Workout (No specific plan) --</option>");
        for (WorkoutPlan p : enrolledPlans) {
            out.printf("                    <option value=\"%d\" data-basecal=\"%.0f\">%s (%s)</option>%n",
                    p.getId(), p.getBaseCaloriesBurned(), HtmlRenderer.escape(p.getTitle()), p.getPlanType());
        }
        out.println("                </select>");
        out.println("            </div>");

        out.println("            <div class=\"form-row\">");
        out.println("                <div class=\"form-group\">");
        out.println("                    <label class=\"form-label\">Duration (Minutes)</label>");
        out.println("                    <input type=\"number\" name=\"durationMinutes\" id=\"durationInput\" value=\"45\" min=\"5\" max=\"300\" class=\"form-control\" oninput=\"updateCalorieEstimate()\" required />");
        out.println("                </div>");
        out.println("                <div class=\"form-group\">");
        out.println("                    <label class=\"form-label\">Current Weight (kg)</label>");
        out.printf("                    <input type=\"number\" step=\"0.1\" name=\"currentWeightKg\" value=\"%.1f\" class=\"form-control\" required />%n", client.getCurrentWeightKg());
        out.println("                </div>");
        out.println("            </div>");

        out.println("            <div class=\"form-group\">");
        out.println("                <label class=\"form-label\">Calories Burned (Estimated)</label>");
        out.println("                <input type=\"number\" step=\"1\" name=\"caloriesBurned\" id=\"caloriesInput\" value=\"350\" class=\"form-control\" required />");
        out.println("                <small style=\"color: var(--accent);\">Dynamically computed via OOP Polymorphic Formula</small>");
        out.println("            </div>");

        out.println("            <div class=\"form-group\">");
        out.println("                <label class=\"form-label\">Workout Notes / Feedback</label>");
        out.println("                <input type=\"text\" name=\"notes\" placeholder=\"Felt energized, completed all sets\" class=\"form-control\" />");
        out.println("            </div>");

        out.println("            <button type=\"submit\" class=\"btn btn-primary btn-block\">Save Workout & Update Metrics</button>");
        out.println("        </form>");
        out.println("    </div>");

        // Column 2: Active Enrolled Plans
        out.println("    <div>");
        out.println("        <div class=\"section-header\">");
        out.println("            <h3 class=\"section-title\">My Enrolled Plans (" + enrolledPlans.size() + ")</h3>");
        out.println("            <a href=\"/client/plans\" class=\"btn btn-outline btn-sm\">Browse More Plans</a>");
        out.println("        </div>");

        if (enrolledPlans.isEmpty()) {
            out.println("        <div class=\"card\" style=\"text-align: center; padding: 2rem;\">");
            out.println("            <p style=\"color: var(--text-muted); margin-bottom: 1rem;\">You are not enrolled in any workout plans yet.</p>");
            out.println("            <a href=\"/client/plans\" class=\"btn btn-primary\">Explore Plans Catalog</a>");
            out.println("        </div>");
        } else {
            for (WorkoutPlan p : enrolledPlans) {
                out.println("        <div class=\"card\" style=\"margin-bottom: 1rem;\">");
                out.println("            <div class=\"card-header\">");
                out.println("                <h4 class=\"card-title\">" + HtmlRenderer.escape(p.getTitle()) + "</h4>");
                out.println("                <span class=\"badge badge-" + p.getPlanType().name().toLowerCase() + "\">" + p.getPlanType() + "</span>");
                out.println("            </div>");
                out.printf("            <p class=\"card-desc\">%s</p>%n", HtmlRenderer.escape(p.getDescription()));
                out.println("            <div class=\"card-meta-list\">");
                out.printf("                <div class=\"card-meta-item\"><span class=\"card-meta-label\">%s:</span> <span class=\"card-meta-value\">%s</span></div>%n",
                        p.getSpecificAttributeLabel(), HtmlRenderer.escape(p.getSpecificAttributeValue()));
                out.printf("                <div class=\"card-meta-item\"><span class=\"card-meta-label\">Polymorphic Summary:</span> <span class=\"card-meta-value\">%s</span></div>%n",
                        HtmlRenderer.escape(p.generateWorkoutSummary()));
                out.println("            </div>");
                out.println("            <form method=\"POST\" action=\"/client/unenroll\" style=\"display: inline;\">");
                out.printf("                <input type=\"hidden\" name=\"planId\" value=\"%d\" />%n", p.getId());
                out.println("                <button type=\"submit\" class=\"btn btn-outline btn-sm\" style=\"color: var(--danger);\">Unenroll</button>");
                out.println("            </form>");
                out.println("        </div>");
            }
        }
        out.println("    </div>");
        out.println("</div>");

        // Workout History & Coach Feedback
        out.println("<div class=\"section-header\"><h3 class=\"section-title\">Recent Workout Logs</h3></div>");
        if (history.isEmpty()) {
            out.println("<p style=\"color: var(--text-muted); margin-bottom: 2rem;\">No workout logs yet. Log your first workout above!</p>");
        } else {
            out.println("<div class=\"table-container\">");
            out.println("    <table>");
            out.println("        <thead><tr><th>Date</th><th>Associated Plan</th><th>Duration</th><th>Calories Burned</th><th>Weight</th><th>Notes</th></tr></thead>");
            out.println("        <tbody>");
            for (ProgressRecord r : history) {
                out.printf("        <tr><td>%s</td><td><strong>%s</strong></td><td>%d mins</td><td style=\"color: var(--accent); font-weight: 700;\">%.0f kcal</td><td>%.1f kg</td><td>%s</td></tr>%n",
                        r.getRecordedAt().toString().substring(0, 16),
                        r.getWorkoutPlanTitle() != null ? HtmlRenderer.escape(r.getWorkoutPlanTitle()) : "Free Workout",
                        r.getDurationMinutes(), r.getCaloriesBurned(), r.getCurrentWeightKg(),
                        r.getNotes() != null ? HtmlRenderer.escape(r.getNotes()) : "-");
            }
            out.println("        </tbody>");
            out.println("    </table>");
            out.println("</div>");
        }

        // Coach Feedback Section
        out.println("<div class=\"section-header\"><h3 class=\"section-title\">💬 Coach Feedback & Recommendations</h3></div>");
        if (feedbackList.isEmpty()) {
            out.println("<p style=\"color: var(--text-muted);\">No coach feedback received yet.</p>");
        } else {
            out.println("<div class=\"cards-grid\">");
            for (CoachFeedback f : feedbackList) {
                out.println("    <div class=\"card\">");
                out.printf("        <div class=\"card-header\"><h4 class=\"card-title\">%s</h4><span class=\"badge badge-strength\">★ %d/5</span></div>%n",
                        HtmlRenderer.escape(f.getCoachName()), f.getRatingGiven());
                out.printf("        <p class=\"card-desc\" style=\"font-style: italic;\">\"%s\"</p>%n", HtmlRenderer.escape(f.getMessage()));
                if (f.getPlanRecommendation() != null && !f.getPlanRecommendation().isEmpty()) {
                    out.printf("        <div class=\"card-meta-list\"><span class=\"card-meta-label\">Recommendation:</span> <strong>%s</strong></div>%n",
                            HtmlRenderer.escape(f.getPlanRecommendation()));
                }
                out.printf("        <small style=\"color: var(--text-muted);\">%s</small>%n", f.getCreatedAt().toString().substring(0, 16));
                out.println("    </div>");
            }
            out.println("</div>");
        }

        out.println("""
            <script>
                function updateCalorieEstimate() {
                    var sel = document.getElementById('planSelect');
                    var duration = parseInt(document.getElementById('durationInput').value) || 30;
                    var baseCal = 300;
                    if (sel.selectedIndex > 0) {
                        var opt = sel.options[sel.selectedIndex];
                        baseCal = parseFloat(opt.getAttribute('data-basecal')) || 300;
                    }
                    var est = Math.round((baseCal / 45.0) * duration);
                    document.getElementById('caloriesInput').value = est;
                }
            </script>
        """);
    }

    private void renderCoachDashboard(CoachUser coach, PrintWriter out) {
        List<ClientUser> clients = userService.getClientsForCoach(coach.getId());
        List<WorkoutPlan> myPlans = planService.getPlansByCoach(coach.getId());

        out.println("<div class=\"hero-banner\">");
        out.println("    <h1 class=\"hero-title\">Coach Portal: " + HtmlRenderer.escape(coach.getName()) + " 🏆</h1>");
        out.printf("    <p class=\"hero-sub\">Specialization: <strong>%s</strong> • Certifications: <strong>%s</strong> • Experience: <strong>%d Years</strong> • Rating: <strong>%.1f/5.0</strong></p>%n",
                HtmlRenderer.escape(coach.getSpecialization()), HtmlRenderer.escape(coach.getCertification()), coach.getExperienceYears(), coach.getRating());
        out.println("</div>");

        // Stats
        out.println("<div class=\"stats-grid\">");
        out.println("    <div class=\"stat-card\"><span class=\"stat-label\">Assigned Clients</span><span class=\"stat-value\">" + clients.size() + "</span></div>");
        out.println("    <div class=\"stat-card\"><span class=\"stat-label\">Plans Created</span><span class=\"stat-value\">" + myPlans.size() + "</span></div>");
        out.println("    <div class=\"stat-card\"><span class=\"stat-label\">Coach Rating</span><span class=\"stat-value\">★ " + coach.getRating() + "</span></div>");
        out.println("</div>");

        // Clients Table
        out.println("<div class=\"section-header\"><h3 class=\"section-title\">Assigned Clients</h3></div>");
        if (clients.isEmpty()) {
            out.println("<p style=\"color: var(--text-muted); margin-bottom: 2rem;\">No clients assigned currently.</p>");
        } else {
            out.println("<div class=\"table-container\">");
            out.println("    <table>");
            out.println("        <thead><tr><th>Client Name</th><th>Email</th><th>Fitness Goal</th><th>Current / Target Weight</th><th>BMI Status</th><th>Action</th></tr></thead>");
            out.println("        <tbody>");
            for (ClientUser c : clients) {
                out.printf("        <tr><td><strong>%s</strong></td><td>%s</td><td>%s</td><td>%.1f kg / %.1f kg</td><td>%.1f (%s)</td>" +
                        "<td><button class=\"btn btn-primary btn-sm\" onclick=\"openFeedbackModal(%d, '%s')\">Send Feedback</button></td></tr>%n",
                        HtmlRenderer.escape(c.getName()), HtmlRenderer.escape(c.getEmail()),
                        HtmlRenderer.escape(c.getFitnessGoal()), c.getCurrentWeightKg(), c.getTargetWeightKg(),
                        c.calculateBMI(), c.getBMICategory(), c.getId(), HtmlRenderer.escape(c.getName()));
            }
            out.println("        </tbody>");
            out.println("    </table>");
            out.println("</div>");
        }

        // Send Feedback Form
        out.println("<div class=\"card\" id=\"feedback-section\" style=\"margin-bottom: 2.5rem; max-width: 650px;\">");
        out.println("    <div class=\"card-header\"><h3 class=\"card-title\">💬 Send Coach Feedback to Client</h3></div>");
        out.println("    <form method=\"POST\" action=\"/coach/feedback\">");
        out.println("        <div class=\"form-group\">");
        out.println("            <label class=\"form-label\">Select Client</label>");
        out.println("            <select name=\"clientId\" class=\"form-control\" id=\"feedbackClientSelect\" required>");
        for (ClientUser c : clients) {
            out.printf("                <option value=\"%d\">%s (%s)</option>%n", c.getId(), HtmlRenderer.escape(c.getName()), HtmlRenderer.escape(c.getEmail()));
        }
        out.println("            </select>");
        out.println("        </div>");
        out.println("        <div class=\"form-group\">");
        out.println("            <label class=\"form-label\">Message / Advice</label>");
        out.println("            <textarea name=\"message\" class=\"form-control\" rows=\"3\" required placeholder=\"Great form on your recent logs. Ensure you maintain adequate hydration...\"></textarea>");
        out.println("        </div>");
        out.println("        <div class=\"form-row\">");
        out.println("            <div class=\"form-group\">");
        out.println("                <label class=\"form-label\">Recommended Action / Plan Adjustment</label>");
        out.println("                <input type=\"text\" name=\"planRecommendation\" class=\"form-control\" placeholder=\"e.g. Increase rest to 90s, add 10 min stretch\" />");
        out.println("            </div>");
        out.println("            <div class=\"form-group\">");
        out.println("                <label class=\"form-label\">Rating Given (1-5)</label>");
        out.println("                <select name=\"rating\" class=\"form-control\">");
        out.println("                    <option value=\"5\">5 - Exceptional Effort</option>");
        out.println("                    <option value=\"4\">4 - Very Good</option>");
        out.println("                    <option value=\"3\">3 - Consistent</option>");
        out.println("                </select>");
        out.println("            </div>");
        out.println("        </div>");
        out.println("        <button type=\"submit\" class=\"btn btn-primary\">Post Feedback & Dispatch Asynchronous Alert</button>");
        out.println("    </form>");
        out.println("</div>");

        // Create New Workout Plan Form (Polymorphic creation)
        out.println("<div class=\"card\" id=\"create-plan\" style=\"margin-bottom: 2.5rem; max-width: 750px;\">");
        out.println("    <div class=\"card-header\"><h3 class=\"card-title\">➕ Create New Workout Plan (Polymorphic Design)</h3></div>");
        out.println("    <form method=\"POST\" action=\"/coach/plans/new\">");
        out.println("        <div class=\"form-row\">");
        out.println("            <div class=\"form-group\">");
        out.println("                <label class=\"form-label\">Plan Type (OOP Polymorphism)</label>");
        out.println("                <select name=\"planType\" class=\"form-control\" id=\"createPlanType\" onchange=\"updatePlanTypeFields()\">");
        out.println("                    <option value=\"STRENGTH\">StrengthWorkoutPlan (Resistance, Sets/Reps)</option>");
        out.println("                    <option value=\"CARDIO\">CardioWorkoutPlan (Aerobic, HR Zone, Distance)</option>");
        out.println("                    <option value=\"HIIT\">HIITWorkoutPlan (Anaerobic Intervals, EPOC)</option>");
        out.println("                </select>");
        out.println("            </div>");
        out.println("            <div class=\"form-group\">");
        out.println("                <label class=\"form-label\">Difficulty Level</label>");
        out.println("                <select name=\"difficultyLevel\" class=\"form-control\">");
        out.println("                    <option value=\"BEGINNER\">Beginner</option>");
        out.println("                    <option value=\"INTERMEDIATE\" selected>Intermediate</option>");
        out.println("                    <option value=\"ADVANCED\">Advanced</option>");
        out.println("                </select>");
        out.println("            </div>");
        out.println("        </div>");

        out.println("        <div class=\"form-group\">");
        out.println("            <label class=\"form-label\">Plan Title</label>");
        out.println("            <input type=\"text\" name=\"title\" class=\"form-control\" required placeholder=\"e.g. Upper Body Hypertrophy Blitz\" />");
        out.println("        </div>");
        out.println("        <div class=\"form-group\">");
        out.println("            <label class=\"form-label\">Description</label>");
        out.println("            <textarea name=\"description\" class=\"form-control\" rows=\"2\" required placeholder=\"Detailed overview of the training methodology...\"></textarea>");
        out.println("        </div>");

        out.println("        <div class=\"form-row\">");
        out.println("            <div class=\"form-group\">");
        out.println("                <label class=\"form-label\">Duration (Weeks)</label>");
        out.println("                <input type=\"number\" name=\"durationWeeks\" value=\"6\" class=\"form-control\" required />");
        out.println("            </div>");
        out.println("            <div class=\"form-group\">");
        out.println("                <label class=\"form-label\">Est. Session Time (Mins)</label>");
        out.println("                <input type=\"number\" name=\"estDurationMinutes\" value=\"45\" class=\"form-control\" required />");
        out.println("            </div>");
        out.println("            <div class=\"form-group\">");
        out.println("                <label class=\"form-label\">Base Calories</label>");
        out.println("                <input type=\"number\" name=\"baseCalories\" value=\"350\" class=\"form-control\" required />");
        out.println("            </div>");
        out.println("        </div>");

        // Polymorphic Type Specific Inputs
        out.println("        <div id=\"strengthSection\" style=\"background: rgba(0,0,0,0.25); padding: 1rem; border-radius: 0.5rem; margin-bottom: 1rem;\">");
        out.println("            <strong>Strength Attributes:</strong>");
        out.println("            <div class=\"form-row\" style=\"margin-top: 0.5rem;\">");
        out.println("                <div class=\"form-group\"><label class=\"form-label\">Target Muscle</label><input type=\"text\" name=\"targetMuscleGroup\" value=\"Chest & Triceps\" class=\"form-control\" /></div>");
        out.println("                <div class=\"form-group\"><label class=\"form-label\">Sets</label><input type=\"number\" name=\"recommendedSets\" value=\"4\" class=\"form-control\" /></div>");
        out.println("                <div class=\"form-group\"><label class=\"form-label\">Reps</label><input type=\"number\" name=\"recommendedReps\" value=\"10\" class=\"form-control\" /></div>");
        out.println("                <div class=\"form-group\"><label class=\"form-label\">Rest (Sec)</label><input type=\"number\" name=\"restBetweenSetsSec\" value=\"60\" class=\"form-control\" /></div>");
        out.println("            </div>");
        out.println("        </div>");

        out.println("        <div id=\"cardioSection\" style=\"display: none; background: rgba(0,0,0,0.25); padding: 1rem; border-radius: 0.5rem; margin-bottom: 1rem;\">");
        out.println("            <strong>Cardio Attributes:</strong>");
        out.println("            <div class=\"form-row\" style=\"margin-top: 0.5rem;\">");
        out.println("                <div class=\"form-group\"><label class=\"form-label\">Cardio Type</label><input type=\"text\" name=\"cardioType\" value=\"Running\" class=\"form-control\" /></div>");
        out.println("                <div class=\"form-group\"><label class=\"form-label\">Target HR Zone</label><input type=\"text\" name=\"targetHeartRateZone\" value=\"Zone 3 (Tempo)\" class=\"form-control\" /></div>");
        out.println("                <div class=\"form-group\"><label class=\"form-label\">Distance (km)</label><input type=\"number\" step=\"0.5\" name=\"distanceKm\" value=\"5.0\" class=\"form-control\" /></div>");
        out.println("                <div class=\"form-group\"><label class=\"form-label\">Target Pace (min/km)</label><input type=\"number\" step=\"0.1\" name=\"targetPaceMinPerKm\" value=\"5.5\" class=\"form-control\" /></div>");
        out.println("            </div>");
        out.println("        </div>");

        out.println("        <div id=\"hiitSection\" style=\"display: none; background: rgba(0,0,0,0.25); padding: 1rem; border-radius: 0.5rem; margin-bottom: 1rem;\">");
        out.println("            <strong>HIIT Attributes:</strong>");
        out.println("            <div class=\"form-row\" style=\"margin-top: 0.5rem;\">");
        out.println("                <div class=\"form-group\"><label class=\"form-label\">Work Interval (s)</label><input type=\"number\" name=\"workIntervalSec\" value=\"40\" class=\"form-control\" /></div>");
        out.println("                <div class=\"form-group\"><label class=\"form-label\">Rest Interval (s)</label><input type=\"number\" name=\"restIntervalSec\" value=\"20\" class=\"form-control\" /></div>");
        out.println("                <div class=\"form-group\"><label class=\"form-label\">Rounds</label><input type=\"number\" name=\"numberOfRounds\" value=\"6\" class=\"form-control\" /></div>");
        out.println("                <div class=\"form-group\"><label class=\"form-label\">Intensity (1-10)</label><input type=\"number\" name=\"intensityRating\" value=\"9\" class=\"form-control\" /></div>");
        out.println("            </div>");
        out.println("        </div>");

        out.println("        <button type=\"submit\" class=\"btn btn-primary\">Publish Workout Plan</button>");
        out.println("    </form>");
        out.println("</div>");

        // Script for switching plan fields
        out.println("""
            <script>
                function updatePlanTypeFields() {
                    var type = document.getElementById('createPlanType').value;
                    document.getElementById('strengthSection').style.display = (type === 'STRENGTH') ? 'block' : 'none';
                    document.getElementById('cardioSection').style.display = (type === 'CARDIO') ? 'block' : 'none';
                    document.getElementById('hiitSection').style.display = (type === 'HIIT') ? 'block' : 'none';
                }
                function openFeedbackModal(clientId, clientName) {
                    var sel = document.getElementById('feedbackClientSelect');
                    if (sel) {
                        sel.value = clientId;
                        document.getElementById('feedback-section').scrollIntoView({ behavior: 'smooth' });
                    }
                }
            </script>
        """);
    }

    private void renderAdminDashboard(AdminUser admin, PrintWriter out) {
        List<User> users = adminService.getAllUsers();
        List<WorkoutPlan> plans = adminService.getAllPlans();
        List<AuditLog> auditLogs = adminService.getRecentAuditLogs(30);
        Map<String, Object> diag = adminService.getSystemDiagnostics();

        out.println("<div class=\"hero-banner\">");
        out.println("    <h1 class=\"hero-title\">System Administrator Console 🛡️</h1>");
        out.printf("    <p class=\"hero-sub\">Admin: <strong>%s</strong> • Department: <strong>%s</strong> • Privilege Level: <strong>Tier %d</strong></p>%n",
                HtmlRenderer.escape(admin.getName()), HtmlRenderer.escape(admin.getDepartment()), admin.getAdminLevel());
        out.println("</div>");

        // Platform Diagnostics & Multithreaded Queue Stats
        out.println("<div class=\"section-header\" id=\"system\"><h3 class=\"section-title\">⚡ Concurrency & Platform Diagnostics</h3></div>");
        out.println("<div class=\"stats-grid\">");
        out.printf("    <div class=\"stat-card\"><span class=\"stat-label\">Active User Sessions</span><span class=\"stat-value\">%s</span><span class=\"stat-meta\">Live Atomic Counter</span></div>%n", diag.get("activeSessions"));
        out.printf("    <div class=\"stat-card\"><span class=\"stat-label\">Total HTTP Requests</span><span class=\"stat-value\">%s</span><span class=\"stat-meta\">Processed via Filter</span></div>%n", diag.get("totalRequests"));
        out.printf("    <div class=\"stat-card\"><span class=\"stat-label\">Workouts Logged</span><span class=\"stat-value\">%s</span><span class=\"stat-meta\">Total Platform Count</span></div>%n", diag.get("totalWorkoutsLogged"));
        out.printf("    <div class=\"stat-card\"><span class=\"stat-label\">Platform Calorie Burn</span><span class=\"stat-value\">%.0f kcal</span><span class=\"stat-meta\">RW-Locked Aggregation</span></div>%n", diag.get("cumulativeCalories"));
        out.printf("    <div class=\"stat-card\"><span class=\"stat-label\">Notification Queue</span><span class=\"stat-value\">%s pending</span><span class=\"stat-meta\">Worker Thread: %s processed</span></div>%n", diag.get("notificationQueuePending"), diag.get("notificationsProcessed"));
        out.println("</div>");

        // Database Tables Inspection
        @SuppressWarnings("unchecked")
        List<String> tables = (List<String>) diag.get("dbTables");
        out.println("<div class=\"card\" style=\"margin-bottom: 2rem;\">");
        out.println("    <div class=\"card-header\"><h4 class=\"card-title\">🗄️ JDBC Database Tables & Metadata</h4></div>");
        out.println("    <div style=\"display: flex; flex-wrap: wrap; gap: 0.5rem;\">");
        for (String t : tables) {
            out.printf("        <span style=\"background: #334155; padding: 0.3rem 0.75rem; border-radius: 0.375rem; font-family: monospace; font-size: 0.85rem;\">%s</span>%n", t);
        }
        out.println("    </div>");
        out.println("</div>");

        // User Accounts Management Table
        out.println("<div class=\"section-header\" id=\"users\"><h3 class=\"section-title\">Manage User Accounts (" + users.size() + ")</h3></div>");
        out.println("<div class=\"table-container\">");
        out.println("    <table>");
        out.println("        <thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Profile Summary (Polymorphic)</th><th>Action</th></tr></thead>");
        out.println("        <tbody>");
        for (User u : users) {
            out.printf("        <tr><td>%d</td><td><strong>%s</strong></td><td>%s</td><td><span class=\"role-tag role-%s\">%s</span></td><td>%s</td><td><small>%s</small></td><td>",
                    u.getId(), HtmlRenderer.escape(u.getName()), HtmlRenderer.escape(u.getEmail()),
                    u.getRole().name().toLowerCase(), u.getRole().name(),
                    u.isActive() ? "<span style='color: var(--success); font-weight:700;'>ACTIVE</span>" : "<span style='color: var(--danger); font-weight:700;'>INACTIVE</span>",
                    HtmlRenderer.escape(u.getProfileSummary()));

            if (u.getId() != admin.getId()) {
                out.println("            <form method=\"POST\" action=\"/admin/users/toggle\" style=\"display: inline;\">");
                out.printf("                <input type=\"hidden\" name=\"userId\" value=\"%d\" />%n", u.getId());
                out.printf("                <input type=\"hidden\" name=\"targetState\" value=\"%b\" />%n", !u.isActive());
                out.printf("                <button type=\"submit\" class=\"btn btn-%s btn-sm\">%s</button>%n",
                        u.isActive() ? "danger" : "success", u.isActive() ? "Deactivate" : "Activate");
                out.println("            </form>");
            } else {
                out.println("<small style=\"color: var(--text-muted);\">Current User</small>");
            }
            out.println("        </td></tr>");
        }
        out.println("        </tbody>");
        out.println("    </table>");
        out.println("</div>");

        // Content Moderation: Workout Plans
        out.println("<div class=\"section-header\"><h3 class=\"section-title\">Moderate Workout Plans (" + plans.size() + ")</h3></div>");
        out.println("<div class=\"table-container\">");
        out.println("    <table>");
        out.println("        <thead><tr><th>ID</th><th>Title</th><th>Type</th><th>Difficulty</th><th>Coach</th><th>Duration</th><th>Action</th></tr></thead>");
        out.println("        <tbody>");
        for (WorkoutPlan p : plans) {
            out.printf("        <tr><td>%d</td><td><strong>%s</strong></td><td><span class=\"badge badge-%s\">%s</span></td><td>%s</td><td>%s</td><td>%d wks (%d min)</td><td>" +
                    "            <form method=\"POST\" action=\"/admin/plans/delete\" style=\"display: inline;\" onsubmit=\"return confirm('Delete this plan?');\">" +
                    "                <input type=\"hidden\" name=\"planId\" value=\"%d\" />" +
                    "                <button type=\"submit\" class=\"btn btn-danger btn-sm\">Delete</button>" +
                    "            </form>" +
                    "        </td></tr>%n",
                    p.getId(), HtmlRenderer.escape(p.getTitle()), p.getPlanType().name().toLowerCase(),
                    p.getPlanType(), p.getDifficultyLevel(), HtmlRenderer.escape(p.getCoachName()),
                    p.getDurationWeeks(), p.getEstimatedDurationMinutes(), p.getId());
        }
        out.println("        </tbody>");
        out.println("    </table>");
        out.println("</div>");

        // Audit Logs Table
        out.println("<div class=\"section-header\"><h3 class=\"section-title\">System Audit Trail</h3></div>");
        out.println("<div class=\"table-container\">");
        out.println("    <table>");
        out.println("        <thead><tr><th>Timestamp</th><th>Action Type</th><th>Admin Email</th><th>Details</th><th>IP Address</th></tr></thead>");
        out.println("        <tbody>");
        for (AuditLog l : auditLogs) {
            out.printf("        <tr><td>%s</td><td><code>%s</code></td><td>%s</td><td>%s</td><td>%s</td></tr>%n",
                    l.getTimestamp().toString().substring(0, 19),
                    HtmlRenderer.escape(l.getActionType()),
                    HtmlRenderer.escape(l.getAdminEmail()),
                    HtmlRenderer.escape(l.getDetails()),
                    HtmlRenderer.escape(l.getIpAddress()));
        }
        out.println("        </tbody>");
        out.println("    </table>");
        out.println("</div>");
    }
}
