package com.fitness.servlet;

import com.fitness.concurrency.PlatformMetricsManager;
import com.fitness.exception.AuthenticationException;
import com.fitness.exception.UserAlreadyExistsException;
import com.fitness.model.ClientUser;
import com.fitness.model.CoachUser;
import com.fitness.model.Role;
import com.fitness.model.User;
import com.fitness.service.UserService;
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
 * Servlet handling user authentication (Login, Register, Logout).
 * Demonstrates Java Servlets, Sessions, HTTP GET/POST handling.
 */
public class AuthServlet extends HttpServlet {

    private final UserService userService = new UserService();
    private final PlatformMetricsManager metricsManager = PlatformMetricsManager.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/login";

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        switch (path) {
            case "/logout":
                handleLogout(req, resp);
                break;
            case "/register":
                renderRegisterPage(req, out);
                break;
            case "/login":
            default:
                renderLoginPage(req, out);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/login";

        resp.setContentType("text/html;charset=UTF-8");

        switch (path) {
            case "/login":
                handleLoginPost(req, resp);
                break;
            case "/register":
                handleRegisterPost(req, resp);
                break;
            default:
                resp.sendRedirect("/auth/login");
                break;
        }
    }

    private void renderLoginPage(HttpServletRequest req, PrintWriter out) {
        String msg = req.getParameter("msg");
        String error = req.getParameter("error");

        out.println(HtmlRenderer.getCommonHeader("Sign In", null));

        out.println("<div class=\"form-card\">");
        out.println("    <h2 style=\"margin-bottom: 0.5rem; text-align: center;\">Welcome to FitPulse</h2>");
        out.println("    <p style=\"color: var(--text-muted); text-align: center; margin-bottom: 1.5rem;\">Sign in to your account</p>");

        if (msg != null) out.println(HtmlRenderer.renderAlert("success", msg));
        if (error != null) out.println(HtmlRenderer.renderAlert("error", error));

        out.println("    <form method=\"POST\" action=\"/auth/login\">");
        out.println("        <div class=\"form-group\">");
        out.println("            <label class=\"form-label\">Email Address</label>");
        out.println("            <input type=\"email\" name=\"email\" class=\"form-control\" required placeholder=\"alice.client@fitness.com\" />");
        out.println("        </div>");
        out.println("        <div class=\"form-group\">");
        out.println("            <label class=\"form-label\">Password</label>");
        out.println("            <input type=\"password\" name=\"password\" class=\"form-control\" required placeholder=\"••••••••\" />");
        out.println("        </div>");
        out.println("        <button type=\"submit\" class=\"btn btn-primary btn-block\">Sign In</button>");
        out.println("    </form>");

        out.println("    <div style=\"margin-top: 1.5rem; text-align: center; font-size: 0.9rem; color: var(--text-muted);\">");
        out.println("        Don't have an account? <a href=\"/auth/register\" style=\"color: var(--accent);\">Register here</a>");
        out.println("    </div>");

        out.println("    <div style=\"margin-top: 1.5rem; padding: 1rem; background: rgba(0,0,0,0.25); border-radius: 0.5rem; font-size: 0.82rem;\">");
        out.println("        <strong>Demo Accounts (Pre-seeded):</strong><br/>");
        out.println("        • Client: <code>alice.client@fitness.com</code> / <code>client123</code><br/>");
        out.println("        • Coach: <code>sarah.coach@fitness.com</code> / <code>coach123</code><br/>");
        out.println("        • Admin: <code>admin@fitness.com</code> / <code>admin123</code>");
        out.println("    </div>");

        out.println("</div>");
        out.println(HtmlRenderer.getCommonFooter());
    }

    private void renderRegisterPage(HttpServletRequest req, PrintWriter out) {
        String error = req.getParameter("error");
        List<CoachUser> coaches = userService.getAllCoaches();

        out.println(HtmlRenderer.getCommonHeader("Register Account", null));

        out.println("<div class=\"form-card\" style=\"max-width: 650px;\">");
        out.println("    <h2 style=\"margin-bottom: 0.5rem; text-align: center;\">Create Your Account</h2>");
        out.println("    <p style=\"color: var(--text-muted); text-align: center; margin-bottom: 1.5rem;\">Join as a Client to reach your goals or as a Coach to guide others</p>");

        if (error != null) out.println(HtmlRenderer.renderAlert("error", error));

        out.println("    <form method=\"POST\" action=\"/auth/register\">");
        out.println("        <div class=\"form-group\">");
        out.println("            <label class=\"form-label\">I am registering as:</label>");
        out.println("            <select name=\"role\" class=\"form-control\" id=\"roleSelect\" onchange=\"toggleRoleFields()\">");
        out.println("                <option value=\"CLIENT\">Client (Member looking for workout plans)</option>");
        out.println("                <option value=\"COACH\">Coach (Fitness trainer & plan creator)</option>");
        out.println("            </select>");
        out.println("        </div>");

        out.println("        <div class=\"form-row\">");
        out.println("            <div class=\"form-group\">");
        out.println("                <label class=\"form-label\">Full Name</label>");
        out.println("                <input type=\"text\" name=\"name\" class=\"form-control\" required />");
        out.println("            </div>");
        out.println("            <div class=\"form-group\">");
        out.println("                <label class=\"form-label\">Email Address</label>");
        out.println("                <input type=\"email\" name=\"email\" class=\"form-control\" required />");
        out.println("            </div>");
        out.println("        </div>");

        out.println("        <div class=\"form-group\">");
        out.println("            <label class=\"form-label\">Password</label>");
        out.println("            <input type=\"password\" name=\"password\" class=\"form-control\" required />");
        out.println("        </div>");

        // Client fields
        out.println("        <div id=\"clientFields\">");
        out.println("            <div class=\"form-group\">");
        out.println("                <label class=\"form-label\">Fitness Goal</label>");
        out.println("                <input type=\"text\" name=\"fitnessGoal\" class=\"form-control\" placeholder=\"e.g. Weight Loss, Muscle Hypertrophy\" />");
        out.println("            </div>");
        out.println("            <div class=\"form-row\">");
        out.println("                <div class=\"form-group\">");
        out.println("                    <label class=\"form-label\">Current Weight (kg)</label>");
        out.println("                    <input type=\"number\" step=\"0.5\" name=\"currentWeightKg\" value=\"70\" class=\"form-control\" />");
        out.println("                </div>");
        out.println("                <div class=\"form-group\">");
        out.println("                    <label class=\"form-label\">Target Weight (kg)</label>");
        out.println("                    <input type=\"number\" step=\"0.5\" name=\"targetWeightKg\" value=\"65\" class=\"form-control\" />");
        out.println("                </div>");
        out.println("            </div>");
        out.println("            <div class=\"form-row\">");
        out.println("                <div class=\"form-group\">");
        out.println("                    <label class=\"form-label\">Height (cm)</label>");
        out.println("                    <input type=\"number\" step=\"1\" name=\"heightCm\" value=\"170\" class=\"form-control\" />");
        out.println("                </div>");
        out.println("                <div class=\"form-group\">");
        out.println("                    <label class=\"form-label\">Select Coach</label>");
        out.println("                    <select name=\"coachId\" class=\"form-control\">");
        out.println("                        <option value=\"\">-- No Coach (Self-guided) --</option>");
        for (CoachUser c : coaches) {
            out.printf("                        <option value=\"%d\">%s (%s)</option>%n", c.getId(), HtmlRenderer.escape(c.getName()), HtmlRenderer.escape(c.getSpecialization()));
        }
        out.println("                    </select>");
        out.println("                </div>");
        out.println("            </div>");
        out.println("        </div>");

        // Coach fields
        out.println("        <div id=\"coachFields\" style=\"display: none;\">");
        out.println("            <div class=\"form-row\">");
        out.println("                <div class=\"form-group\">");
        out.println("                    <label class=\"form-label\">Specialization</label>");
        out.println("                    <input type=\"text\" name=\"specialization\" class=\"form-control\" placeholder=\"Strength, HIIT, Cardio\" />");
        out.println("                </div>");
        out.println("                <div class=\"form-group\">");
        out.println("                    <label class=\"form-label\">Certifications</label>");
        out.println("                    <input type=\"text\" name=\"certification\" class=\"form-control\" placeholder=\"CSCS, NASM\" />");
        out.println("                </div>");
        out.println("            </div>");
        out.println("            <div class=\"form-group\">");
        out.println("                <label class=\"form-label\">Experience (Years)</label>");
        out.println("                <input type=\"number\" name=\"experienceYears\" value=\"3\" class=\"form-control\" />");
        out.println("            </div>");
        out.println("            <div class=\"form-group\">");
        out.println("                <label class=\"form-label\">Bio / Philosophy</label>");
        out.println("                <textarea name=\"bio\" class=\"form-control\" rows=\"2\"></textarea>");
        out.println("            </div>");
        out.println("        </div>");

        out.println("        <button type=\"submit\" class=\"btn btn-primary btn-block\">Complete Registration</button>");
        out.println("    </form>");

        out.println("    <script>");
        out.println("        function toggleRoleFields() {");
        out.println("            var role = document.getElementById('roleSelect').value;");
        out.println("            document.getElementById('clientFields').style.display = (role === 'CLIENT') ? 'block' : 'none';");
        out.println("            document.getElementById('coachFields').style.display = (role === 'COACH') ? 'block' : 'none';");
        out.println("        }");
        out.println("    </script>");

        out.println("</div>");
        out.println(HtmlRenderer.getCommonFooter());
    }

    private void handleLoginPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        try {
            User user = userService.authenticate(email, password);
            HttpSession session = req.getSession(true);
            session.setAttribute("loggedUser", user);
            metricsManager.incrementActiveSessions();

            resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
            resp.setHeader("Location", user.getDashboardUrl());
        } catch (AuthenticationException e) {
            resp.sendRedirect("/auth/login?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }

    private void handleRegisterPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String roleStr = req.getParameter("role");
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        try {
            if ("COACH".equalsIgnoreCase(roleStr)) {
                String spec = req.getParameter("specialization");
                String cert = req.getParameter("certification");
                int exp = Integer.parseInt(req.getParameter("experienceYears"));
                String bio = req.getParameter("bio");
                userService.registerCoach(name, email, password, spec, cert, exp, bio);
            } else {
                String goal = req.getParameter("fitnessGoal");
                double weight = Double.parseDouble(req.getParameter("currentWeightKg"));
                double target = Double.parseDouble(req.getParameter("targetWeightKg"));
                double height = Double.parseDouble(req.getParameter("heightCm"));
                String coachIdStr = req.getParameter("coachId");
                Integer coachId = (coachIdStr != null && !coachIdStr.isEmpty()) ? Integer.parseInt(coachIdStr) : null;
                userService.registerClient(name, email, password, goal, weight, target, height, coachId, "Moderate");
            }
            resp.sendRedirect("/auth/login?msg=Registration%20successful!%20Please%20log%20in.");
        } catch (UserAlreadyExistsException e) {
            resp.sendRedirect("/auth/register?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        } catch (Exception e) {
            resp.sendRedirect("/auth/register?error=" + java.net.URLEncoder.encode("Registration error: " + e.getMessage(), "UTF-8"));
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
            metricsManager.decrementActiveSessions();
        }
        resp.sendRedirect("/auth/login?msg=Logged%20out%20successfully.");
    }
}
