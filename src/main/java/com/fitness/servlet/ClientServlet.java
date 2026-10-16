package com.fitness.servlet;

import com.fitness.model.ClientUser;
import com.fitness.model.PlanType;
import com.fitness.model.User;
import com.fitness.model.WorkoutPlan;
import com.fitness.service.WorkoutPlanService;
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
 * Servlet handling client workout catalog, search, and enrollment.
 */
public class ClientServlet extends HttpServlet {

    private final WorkoutPlanService planService = new WorkoutPlanService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/plans";

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedUser") : null;

        if (user == null) {
            resp.sendRedirect("/auth/login");
            return;
        }

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        if ("/plans".equals(path)) {
            renderPlansCatalog(req, out, user);
        } else {
            resp.sendRedirect("/dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedUser") : null;

        if (user == null) {
            resp.sendRedirect("/auth/login");
            return;
        }

        try {
            if ("/enroll".equals(path)) {
                int planId = Integer.parseInt(req.getParameter("planId"));
                planService.enrollClient(user.getId(), planId);
                resp.sendRedirect("/dashboard?msg=" + URLEncoder.encode("Successfully enrolled in workout plan!", StandardCharsets.UTF_8));
            } else if ("/unenroll".equals(path)) {
                int planId = Integer.parseInt(req.getParameter("planId"));
                planService.unenrollClient(user.getId(), planId);
                resp.sendRedirect("/dashboard?msg=" + URLEncoder.encode("Unenrolled from workout plan.", StandardCharsets.UTF_8));
            } else {
                resp.sendRedirect("/dashboard");
            }
        } catch (Exception e) {
            resp.sendRedirect("/client/plans?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    private void renderPlansCatalog(HttpServletRequest req, PrintWriter out, User user) {
        String keyword = req.getParameter("q");
        String difficulty = req.getParameter("difficulty");
        String typeStr = req.getParameter("type");
        PlanType planType = (typeStr != null && !typeStr.isEmpty() && !"ALL".equalsIgnoreCase(typeStr))
                ? PlanType.fromString(typeStr) : null;

        // Uses overloaded filterPlans method
        List<WorkoutPlan> plans = planService.filterPlans(keyword, difficulty, planType);

        out.println(HtmlRenderer.getCommonHeader("Workout Plans Catalog", user));

        out.println("<div class=\"hero-banner\">");
        out.println("    <h1 class=\"hero-title\">Workout Plans Catalog 🏋️‍♂️</h1>");
        out.println("    <p class=\"hero-sub\">Browse specialized training regimens created by our elite coaches. Enroll with a single click.</p>");
        out.println("</div>");

        // Filter / Search Toolbar
        out.println("<form method=\"GET\" action=\"/client/plans\" style=\"background: var(--card-bg); padding: 1.25rem; border-radius: 0.75rem; border: 1px solid var(--card-border); margin-bottom: 2rem;\">");
        out.println("    <div style=\"display: grid; grid-template-columns: 2fr 1fr 1fr auto; gap: 1rem; align-items: end;\">");
        out.println("        <div>");
        out.println("            <label class=\"form-label\">Search Keyword</label>");
        out.printf("            <input type=\"text\" name=\"q\" value=\"%s\" placeholder=\"e.g. Strength, 5K, HIIT, Coach Name\" class=\"form-control\" />%n",
                keyword != null ? HtmlRenderer.escape(keyword) : "");
        out.println("        </div>");

        out.println("        <div>");
        out.println("            <label class=\"form-label\">Difficulty</label>");
        out.println("            <select name=\"difficulty\" class=\"form-control\">");
        out.println("                <option value=\"ALL\">All Difficulties</option>");
        out.println("                <option value=\"BEGINNER\"" + ("BEGINNER".equalsIgnoreCase(difficulty) ? " selected" : "") + ">Beginner</option>");
        out.println("                <option value=\"INTERMEDIATE\"" + ("INTERMEDIATE".equalsIgnoreCase(difficulty) ? " selected" : "") + ">Intermediate</option>");
        out.println("                <option value=\"ADVANCED\"" + ("ADVANCED".equalsIgnoreCase(difficulty) ? " selected" : "") + ">Advanced</option>");
        out.println("            </select>");
        out.println("        </div>");

        out.println("        <div>");
        out.println("            <label class=\"form-label\">Plan Type</label>");
        out.println("            <select name=\"type\" class=\"form-control\">");
        out.println("                <option value=\"ALL\">All Types</option>");
        out.println("                <option value=\"STRENGTH\"" + ("STRENGTH".equalsIgnoreCase(typeStr) ? " selected" : "") + ">Strength</option>");
        out.println("                <option value=\"CARDIO\"" + ("CARDIO".equalsIgnoreCase(typeStr) ? " selected" : "") + ">Cardio</option>");
        out.println("                <option value=\"HIIT\"" + ("HIIT".equalsIgnoreCase(typeStr) ? " selected" : "") + ">HIIT</option>");
        out.println("                <option value=\"SPA_WELLNESS\"" + ("SPA_WELLNESS".equalsIgnoreCase(typeStr) ? " selected" : "") + ">🧖‍♀️ Spa & Recovery</option>");
        out.println("                <option value=\"YOGA_MOBILITY\"" + ("YOGA_MOBILITY".equalsIgnoreCase(typeStr) ? " selected" : "") + ">🧘 Yoga & Mobility</option>");
        out.println("            </select>");
        out.println("        </div>");

        out.println("        <div><button type=\"submit\" class=\"btn btn-primary\">Filter Plans</button></div>");
        out.println("    </div>");
        out.println("</form>");

        // Plans Grid
        if (plans.isEmpty()) {
            out.println("<div class=\"card\" style=\"text-align: center; padding: 3rem;\">");
            out.println("    <p style=\"color: var(--text-muted); font-size: 1.1rem;\">No workout plans matched your search criteria.</p>");
            out.println("    <a href=\"/client/plans\" class=\"btn btn-outline\" style=\"margin-top: 1rem;\">Clear Filters</a>");
            out.println("</div>");
        } else {
            out.println("<div class=\"cards-grid\">");
            for (WorkoutPlan p : plans) {
                boolean enrolled = planService.isClientEnrolled(user.getId(), p.getId());
                double weight = (user instanceof ClientUser c) ? c.getCurrentWeightKg() : 70.0;
                double estimatedBurn = p.calculateCaloriesBurned(p.getEstimatedDurationMinutes(), weight);

                out.println("    <div class=\"card\">");
                out.println("        <div>");
                out.println("            <div class=\"card-header\">");
                out.println("                <h3 class=\"card-title\">" + HtmlRenderer.escape(p.getTitle()) + "</h3>");
                String badgeClass = (p.getPlanType() == PlanType.SPA_WELLNESS) ? "badge-spa" : ("badge-" + p.getPlanType().name().toLowerCase());
                out.println("                <span class=\"badge " + badgeClass + "\">" + p.getPlanType() + "</span>");
                out.println("            </div>");
                out.printf("            <p class=\"card-desc\">%s</p>%n", HtmlRenderer.escape(p.getDescription()));
                out.println("            <div class=\"card-meta-list\">");
                out.printf("                <div class=\"card-meta-item\"><span class=\"card-meta-label\">Difficulty:</span> <span class=\"card-meta-value\">%s</span></div>%n", p.getDifficultyLevel());
                out.printf("                <div class=\"card-meta-item\"><span class=\"card-meta-label\">Program Duration:</span> <span class=\"card-meta-value\">%d Weeks (%d min/session)</span></div>%n", p.getDurationWeeks(), p.getEstimatedDurationMinutes());
                out.printf("                <div class=\"card-meta-item\"><span class=\"card-meta-label\">%s:</span> <span class=\"card-meta-value\">%s</span></div>%n",
                        p.getSpecificAttributeLabel(), HtmlRenderer.escape(p.getSpecificAttributeValue()));
                out.printf("                <div class=\"card-meta-item\"><span class=\"card-meta-label\">Polymorphic Burn (for your %.1fkg):</span> <span class=\"card-meta-value\" style=\"color: var(--accent); font-weight: 700;\">~%.0f kcal</span></div>%n",
                        weight, estimatedBurn);
                out.printf("                <div class=\"card-meta-item\"><span class=\"card-meta-label\">Coach:</span> <span class=\"card-meta-value\">%s</span></div>%n",
                        p.getCoachName() != null ? HtmlRenderer.escape(p.getCoachName()) : "FitPulse Master Coach");
                out.println("            </div>");
                out.println("        </div>");

                if (enrolled) {
                    out.println("        <button class=\"btn btn-outline btn-block\" disabled style=\"color: var(--success); border-color: var(--success);\">✓ Already Enrolled</button>");
                } else {
                    out.println("        <form method=\"POST\" action=\"/client/enroll\">");
                    out.printf("            <input type=\"hidden\" name=\"planId\" value=\"%d\" />%n", p.getId());
                    out.println("            <button type=\"submit\" class=\"btn btn-primary btn-block\">Enroll in this Plan</button>");
                    out.println("        </form>");
                }

                out.println("    </div>");
            }
            out.println("</div>");
        }

        out.println(HtmlRenderer.getCommonFooter());
    }
}
