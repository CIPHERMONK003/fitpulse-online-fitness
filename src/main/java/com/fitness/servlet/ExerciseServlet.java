package com.fitness.servlet;

import com.fitness.model.Exercise;
import com.fitness.model.User;
import com.fitness.service.ExerciseService;
import com.fitness.view.HtmlRenderer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Servlet handling the interactive Movement and Exercise Library.
 * Demonstrates Collections, Generics, Stream API filtering, and dynamic HTML rendering.
 */
public class ExerciseServlet extends HttpServlet {

    private final ExerciseService exerciseService = new ExerciseService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedUser") : null;

        String query = req.getParameter("q");
        String category = req.getParameter("category");
        String difficulty = req.getParameter("difficulty");

        List<Exercise> exercises = exerciseService.filter(query, category, difficulty);

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println(HtmlRenderer.getCommonHeader("Movement & Exercise Library", user));

        out.println("<div class=\"hero-banner\">");
        out.println("    <h1 class=\"hero-title\">Interactive Exercise Library 📖</h1>");
        out.println("    <p class=\"hero-sub\">Explore over 20+ specialized training movements, compound lifts, HIIT intervals, yoga posture flows, and restorative spa recovery techniques.</p>");
        out.println("</div>");

        // Filter Controls
        out.println("<form method=\"GET\" action=\"/exercises\" style=\"background: var(--card-bg); padding: 1.25rem; border-radius: 0.75rem; border: 1px solid var(--card-border); margin-bottom: 2rem;\">");
        out.println("    <div style=\"display: grid; grid-template-columns: 2fr 1fr 1fr auto; gap: 1rem; align-items: end;\">");
        out.println("        <div>");
        out.println("            <label class=\"form-label\">Search Movement or Target Muscle</label>");
        out.println("            <input type=\"text\" name=\"q\" class=\"form-control\" placeholder=\"e.g. Squat, Hamstrings, Foam Roller...\" value=\"" + (query != null ? HtmlRenderer.escape(query) : "") + "\"/>");
        out.println("        </div>");
        out.println("        <div>");
        out.println("            <label class=\"form-label\">Category</label>");
        out.println("            <select name=\"category\" class=\"form-control\">");
        out.println("                <option value=\"ALL\">All Categories</option>");
        out.println("                <option value=\"Strength\" " + ("Strength".equalsIgnoreCase(category) ? "selected" : "") + ">Strength</option>");
        out.println("                <option value=\"Cardio\" " + ("Cardio".equalsIgnoreCase(category) ? "selected" : "") + ">Cardio</option>");
        out.println("                <option value=\"HIIT\" " + ("HIIT".equalsIgnoreCase(category) ? "selected" : "") + ">HIIT</option>");
        out.println("                <option value=\"Mobility / Yoga\" " + ("Mobility / Yoga".equalsIgnoreCase(category) ? "selected" : "") + ">Mobility / Yoga</option>");
        out.println("                <option value=\"Spa Recovery\" " + ("Spa Recovery".equalsIgnoreCase(category) ? "selected" : "") + ">Spa Recovery</option>");
        out.println("            </select>");
        out.println("        </div>");
        out.println("        <div>");
        out.println("            <label class=\"form-label\">Difficulty</label>");
        out.println("            <select name=\"difficulty\" class=\"form-control\">");
        out.println("                <option value=\"ALL\">All Levels</option>");
        out.println("                <option value=\"Beginner\" " + ("Beginner".equalsIgnoreCase(difficulty) ? "selected" : "") + ">Beginner</option>");
        out.println("                <option value=\"Intermediate\" " + ("Intermediate".equalsIgnoreCase(difficulty) ? "selected" : "") + ">Intermediate</option>");
        out.println("                <option value=\"Advanced\" " + ("Advanced".equalsIgnoreCase(difficulty) ? "selected" : "") + ">Advanced</option>");
        out.println("            </select>");
        out.println("        </div>");
        out.println("        <div>");
        out.println("            <button type=\"submit\" class=\"btn btn-primary\" style=\"height: 42px;\">🔍 Filter</button>");
        out.println("        </div>");
        out.println("    </div>");
        out.println("</form>");

        // Exercise Cards Grid
        if (exercises.isEmpty()) {
            out.println("<div class=\"card\" style=\"text-align: center; padding: 3rem;\">");
            out.println("    <h3>No exercises match your search criteria.</h3>");
            out.println("    <p style=\"color: var(--text-muted); margin-top: 0.5rem;\">Try clearing your filters or searching for another muscle group.</p>");
            out.println("    <a href=\"/exercises\" class=\"btn btn-outline\" style=\"margin-top: 1rem;\">Reset Filters</a>");
            out.println("</div>");
        } else {
            out.println("<p style=\"color: var(--text-muted); margin-bottom: 1.25rem;\">Displaying <strong>" + exercises.size() + "</strong> movements found:</p>");
            out.println("<div class=\"grid-3\">");
            for (Exercise ex : exercises) {
                out.println("<div class=\"card\">");
                out.println("    <div class=\"card-header\">");
                out.println("        <span class=\"badge badge-strength\">" + HtmlRenderer.escape(ex.getCategory()) + "</span>");
                out.println("        <span style=\"font-size: 1.8rem;\">" + ex.getIconEmoji() + "</span>");
                out.println("    </div>");

                out.println("    <h3 class=\"card-title\">" + HtmlRenderer.escape(ex.getName()) + "</h3>");
                out.println("    <p class=\"card-desc\" style=\"margin-top: 0.5rem;\">" + HtmlRenderer.escape(ex.getInstructions()) + "</p>");

                out.println("    <div class=\"card-meta-list\">");
                out.println("        <div class=\"card-meta-item\"><span class=\"card-meta-label\">Target Muscles:</span><span class=\"card-meta-value\">" + HtmlRenderer.escape(ex.getTargetMuscles()) + "</span></div>");
                out.println("        <div class=\"card-meta-item\"><span class=\"card-meta-label\">Difficulty:</span><span class=\"card-meta-value\">" + HtmlRenderer.escape(ex.getDifficulty()) + "</span></div>");
                out.println("        <div class=\"card-meta-item\"><span class=\"card-meta-label\">Equipment:</span><span class=\"card-meta-value\">" + HtmlRenderer.escape(ex.getEquipment()) + "</span></div>");
                out.println("        <div class=\"card-meta-item\"><span class=\"card-meta-label\">Est. Burn:</span><span class=\"card-meta-value\" style=\"color: #f59e0b;\">~" + ex.getCalorieBurnPerMin() + " kcal/min</span></div>");
                out.println("    </div>");

                out.println("</div>");
            }
            out.println("</div>");
        }

        out.println(HtmlRenderer.getCommonFooter());
    }
}
