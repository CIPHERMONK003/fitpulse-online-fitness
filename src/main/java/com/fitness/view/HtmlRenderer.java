package com.fitness.view;

import com.fitness.model.*;

import java.util.List;
import java.util.Map;

/**
 * Responsive HTML/CSS UI View Generator for the Fitness Platform.
 * Built with pure Java backend rendering and responsive CSS/vanilla JS (No TypeScript).
 */
public class HtmlRenderer {

    public static String getCommonHeader(String title, User loggedUser) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n")
          .append("<html lang=\"en\">\n")
          .append("<head>\n")
          .append("    <meta charset=\"UTF-8\">\n")
          .append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
          .append("    <title>").append(escape(title)).append(" - FitPulse Platform</title>\n")
          .append("    <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n")
          .append("    <link href=\"https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap\" rel=\"stylesheet\">\n")
          .append("    <style>\n")
          .append(getCssStyles())
          .append("    </style>\n")
          .append("</head>\n")
          .append("<body>\n");

        // Navbar
        sb.append("<header class=\"navbar\">\n")
          .append("    <div class=\"nav-container\">\n")
          .append("        <a href=\"/dashboard\" class=\"nav-brand\">\n")
          .append("            <span class=\"brand-icon\">⚡</span>\n")
          .append("            <span class=\"brand-text\">FitPulse</span>\n")
          .append("        </a>\n");

        if (loggedUser != null) {
            sb.append("        <nav class=\"nav-links\">\n")
              .append("            <a href=\"/dashboard\" class=\"nav-item\">Dashboard</a>\n");

            if (loggedUser.getRole() == Role.CLIENT) {
                sb.append("            <a href=\"/client/plans\" class=\"nav-item\">Browse Plans</a>\n")
                  .append("            <a href=\"/progress/history\" class=\"nav-item\">Workout History</a>\n");
            } else if (loggedUser.getRole() == Role.COACH) {
                sb.append("            <a href=\"/dashboard#create-plan\" class=\"nav-item\">+ Create Plan</a>\n");
            } else if (loggedUser.getRole() == Role.ADMIN) {
                sb.append("            <a href=\"/dashboard#users\" class=\"nav-item\">Manage Users</a>\n")
                  .append("            <a href=\"/dashboard#system\" class=\"nav-item\">System Health</a>\n");
            }

            sb.append("        </nav>\n")
              .append("        <div class=\"nav-user\">\n")
              .append("            <div class=\"user-badge\">\n")
              .append("                <span class=\"role-tag role-").append(loggedUser.getRole().name().toLowerCase()).append("\">")
              .append(loggedUser.getRole().name()).append("</span>\n")
              .append("                <span class=\"user-name\">").append(escape(loggedUser.getName())).append("</span>\n")
              .append("            </div>\n")
              .append("            <a href=\"/auth/logout\" class=\"btn btn-outline btn-sm\">Logout</a>\n")
              .append("        </div>\n");
        } else {
            sb.append("        <div class=\"nav-user\">\n")
              .append("            <a href=\"/auth/login\" class=\"btn btn-outline btn-sm\">Sign In</a>\n")
              .append("            <a href=\"/auth/register\" class=\"btn btn-primary btn-sm\">Register</a>\n")
              .append("        </div>\n");
        }

        sb.append("    </div>\n")
          .append("</header>\n")
          .append("<main class=\"main-content\">\n");

        return sb.toString();
    }

    public static String getCommonFooter() {
        return "</main>\n" +
                "<footer class=\"footer\">\n" +
                "    <div class=\"footer-container\">\n" +
                "        <p>© 2026 FitPulse Online Fitness Coaching Platform • Pure Java Enterprise Edition • Multi-threaded Core • JDBC H2</p>\n" +
                "    </div>\n" +
                "</footer>\n" +
                "</body>\n" +
                "</html>";
    }

    public static String renderAlert(String type, String message) {
        if (message == null || message.trim().isEmpty()) return "";
        return "<div class=\"alert alert-" + escape(type) + "\">" + escape(message) + "</div>\n";
    }

    private static String getCssStyles() {
        return """
            :root {
                --primary: #4f46e5;
                --primary-hover: #4338ca;
                --accent: #06b6d4;
                --success: #10b981;
                --warning: #f59e0b;
                --danger: #ef4444;
                --bg: #0f172a;
                --card-bg: #1e293b;
                --card-border: #334155;
                --text-main: #f8fafc;
                --text-muted: #94a3b8;
                --font: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
            }
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body {
                font-family: var(--font);
                background-color: var(--bg);
                color: var(--text-main);
                min-height: 100vh;
                display: flex;
                flex-direction: column;
                line-height: 1.6;
            }
            .navbar {
                background: rgba(30, 41, 59, 0.95);
                backdrop-filter: blur(8px);
                border-bottom: 1px solid var(--card-border);
                position: sticky;
                top: 0;
                z-index: 100;
            }
            .nav-container {
                max-width: 1280px;
                margin: 0 auto;
                padding: 1rem 1.5rem;
                display: flex;
                align-items: center;
                justify-content: space-between;
            }
            .nav-brand {
                display: flex;
                align-items: center;
                gap: 0.5rem;
                text-decoration: none;
                color: var(--text-main);
                font-weight: 800;
                font-size: 1.35rem;
            }
            .brand-icon {
                font-size: 1.6rem;
                background: linear-gradient(135deg, #6366f1, #06b6d4);
                -webkit-background-clip: text;
                -webkit-text-fill-color: transparent;
            }
            .nav-links {
                display: flex;
                gap: 1.25rem;
            }
            .nav-item {
                color: var(--text-muted);
                text-decoration: none;
                font-weight: 500;
                font-size: 0.95rem;
                transition: color 0.2s;
            }
            .nav-item:hover { color: var(--text-main); }
            .nav-user {
                display: flex;
                align-items: center;
                gap: 1rem;
            }
            .user-badge {
                display: flex;
                align-items: center;
                gap: 0.6rem;
                font-size: 0.9rem;
            }
            .role-tag {
                font-size: 0.75rem;
                font-weight: 700;
                text-transform: uppercase;
                padding: 0.2rem 0.6rem;
                border-radius: 9999px;
                letter-spacing: 0.05em;
            }
            .role-client { background: rgba(16, 185, 129, 0.15); color: #34d399; border: 1px solid rgba(16, 185, 129, 0.3); }
            .role-coach { background: rgba(6, 182, 212, 0.15); color: #38bdf8; border: 1px solid rgba(6, 182, 212, 0.3); }
            .role-admin { background: rgba(239, 68, 68, 0.15); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.3); }

            .main-content {
                max-width: 1280px;
                width: 100%;
                margin: 0 auto;
                padding: 2rem 1.5rem;
                flex: 1;
            }
            .hero-banner {
                background: linear-gradient(135deg, #312e81 0%, #1e1b4b 100%);
                border: 1px solid #4338ca;
                border-radius: 1rem;
                padding: 2.5rem;
                margin-bottom: 2rem;
                position: relative;
                overflow: hidden;
            }
            .hero-title { font-size: 2.2rem; font-weight: 800; margin-bottom: 0.5rem; }
            .hero-sub { color: #c7d2fe; font-size: 1.1rem; max-width: 700px; }

            .stats-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
                gap: 1.25rem;
                margin-bottom: 2rem;
            }
            .stat-card {
                background: var(--card-bg);
                border: 1px solid var(--card-border);
                border-radius: 0.75rem;
                padding: 1.5rem;
                display: flex;
                flex-direction: column;
                gap: 0.5rem;
            }
            .stat-label { color: var(--text-muted); font-size: 0.85rem; font-weight: 600; text-transform: uppercase; }
            .stat-value { font-size: 1.85rem; font-weight: 800; color: #fff; }
            .stat-meta { font-size: 0.8rem; color: var(--accent); }

            .cards-grid {
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
                gap: 1.5rem;
                margin-bottom: 2.5rem;
            }
            .card {
                background: var(--card-bg);
                border: 1px solid var(--card-border);
                border-radius: 0.875rem;
                padding: 1.5rem;
                display: flex;
                flex-direction: column;
                justify-content: space-between;
                box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
                transition: transform 0.2s, border-color 0.2s;
            }
            .card:hover {
                transform: translateY(-2px);
                border-color: #6366f1;
            }
            .card-header {
                display: flex;
                justify-content: space-between;
                align-items: flex-start;
                margin-bottom: 1rem;
            }
            .card-title { font-size: 1.25rem; font-weight: 700; }
            .card-desc { color: var(--text-muted); font-size: 0.92rem; margin-bottom: 1.25rem; flex-grow: 1; }
            .card-meta-list {
                display: flex;
                flex-direction: column;
                gap: 0.4rem;
                margin-bottom: 1.25rem;
                font-size: 0.88rem;
                background: rgba(15, 23, 42, 0.6);
                padding: 0.75rem 1rem;
                border-radius: 0.5rem;
            }
            .card-meta-item { display: flex; justify-content: space-between; }
            .card-meta-label { color: var(--text-muted); }
            .card-meta-value { font-weight: 600; }

            .badge {
                font-size: 0.75rem;
                padding: 0.25rem 0.5rem;
                border-radius: 0.375rem;
                font-weight: 600;
            }
            .badge-strength { background: #3730a3; color: #a5b4fc; }
            .badge-cardio { background: #065f46; color: #6ee7b7; }
            .badge-hiit { background: #991b1b; color: #fca5a5; }

            .btn {
                display: inline-flex;
                align-items: center;
                justify-content: center;
                gap: 0.5rem;
                font-family: inherit;
                font-size: 0.95rem;
                font-weight: 600;
                padding: 0.65rem 1.25rem;
                border-radius: 0.5rem;
                border: none;
                cursor: pointer;
                text-decoration: none;
                transition: all 0.15s ease-in-out;
            }
            .btn-primary { background: var(--primary); color: #fff; }
            .btn-primary:hover { background: var(--primary-hover); }
            .btn-success { background: var(--success); color: #fff; }
            .btn-danger { background: var(--danger); color: #fff; }
            .btn-outline { background: transparent; border: 1px solid var(--card-border); color: var(--text-main); }
            .btn-outline:hover { background: rgba(255, 255, 255, 0.05); border-color: var(--text-muted); }
            .btn-sm { padding: 0.4rem 0.75rem; font-size: 0.85rem; }
            .btn-block { width: 100%; }

            .table-container {
                background: var(--card-bg);
                border: 1px solid var(--card-border);
                border-radius: 0.75rem;
                overflow-x: auto;
                margin-bottom: 2rem;
            }
            table { width: 100%; border-collapse: collapse; text-align: left; font-size: 0.92rem; }
            th {
                background: #0f172a;
                padding: 0.85rem 1.25rem;
                font-weight: 600;
                color: var(--text-muted);
                text-transform: uppercase;
                font-size: 0.75rem;
                letter-spacing: 0.05em;
                border-bottom: 1px solid var(--card-border);
            }
            td {
                padding: 1rem 1.25rem;
                border-bottom: 1px solid var(--card-border);
                color: var(--text-main);
            }
            tr:last-child td { border-bottom: none; }
            tr:hover td { background: rgba(255, 255, 255, 0.02); }

            .form-card {
                background: var(--card-bg);
                border: 1px solid var(--card-border);
                border-radius: 0.875rem;
                padding: 2rem;
                max-width: 600px;
                margin: 0 auto 2rem auto;
            }
            .form-group { margin-bottom: 1.25rem; }
            .form-label { display: block; font-size: 0.88rem; font-weight: 600; margin-bottom: 0.4rem; color: #cbd5e1; }
            .form-control {
                width: 100%;
                padding: 0.65rem 0.85rem;
                background: #0f172a;
                border: 1px solid var(--card-border);
                border-radius: 0.5rem;
                color: #fff;
                font-family: inherit;
                font-size: 0.95rem;
            }
            .form-control:focus { outline: none; border-color: var(--primary); }
            .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }

            .alert {
                padding: 1rem 1.25rem;
                border-radius: 0.5rem;
                margin-bottom: 1.5rem;
                font-weight: 500;
            }
            .alert-success { background: rgba(16, 185, 129, 0.2); border: 1px solid var(--success); color: #6ee7b7; }
            .alert-error { background: rgba(239, 68, 68, 0.2); border: 1px solid var(--danger); color: #fca5a5; }

            .section-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 1.25rem;
            }
            .section-title { font-size: 1.4rem; font-weight: 700; }

            .footer {
                border-top: 1px solid var(--card-border);
                background: #0f172a;
                padding: 1.5rem 0;
                margin-top: auto;
                text-align: center;
                color: var(--text-muted);
                font-size: 0.85rem;
            }
            .tab-nav {
                display: flex;
                gap: 0.5rem;
                margin-bottom: 1.5rem;
                border-bottom: 1px solid var(--card-border);
                padding-bottom: 0.5rem;
            }
            .tab-btn {
                background: transparent;
                border: none;
                color: var(--text-muted);
                font-family: inherit;
                font-size: 1rem;
                font-weight: 600;
                padding: 0.5rem 1rem;
                cursor: pointer;
                border-radius: 0.375rem;
            }
            .tab-btn.active { background: var(--primary); color: #fff; }
        """;
    }

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
