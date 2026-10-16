package com.fitness.view;

import com.fitness.model.*;

import java.util.List;
import java.util.Map;

/**
 * Responsive HTML/CSS UI View Generator for the Fitness Platform.
 * Built with pure Java backend rendering and responsive CSS/vanilla JS (No TypeScript).
 * Features:
 * - Dynamic SVG Brand Logo
 * - Real-Time Color Customizer (User-configurable UI theme & accent colors)
 * - Glassmorphism UI styling with modern micro-interactions
 * - Spa & Wellness Sanctuary suite
 * - Session Slot Booking UI
 * - Interactive Exercise Library
 */
public class HtmlRenderer {

    public static String getCommonHeader(String title, User loggedUser) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n")
          .append("<html lang=\"en\">\n")
          .append("<head>\n")
          .append("    <meta charset=\"UTF-8\">\n")
          .append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
          .append("    <title>").append(escape(title)).append(" - FitPulse Enterprise</title>\n")
          .append("    <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n")
          .append("    <link href=\"https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap\" rel=\"stylesheet\">\n")
          .append("    <script>\n")
          .append("        // Early Theme Apply to eliminate FOUC\n")
          .append("        (function() {\n")
          .append("            const saved = localStorage.getItem('fitpulse_custom_color');\n")
          .append("            if (saved) {\n")
          .append("                document.documentElement.style.setProperty('--primary', saved);\n")
          .append("                document.documentElement.style.setProperty('--primary-hover', saved);\n")
          .append("                document.documentElement.style.setProperty('--primary-glow', saved + '44');\n")
          .append("            }\n")
          .append("            const savedAcc = localStorage.getItem('fitpulse_custom_accent');\n")
          .append("            if (savedAcc) {\n")
          .append("                document.documentElement.style.setProperty('--accent', savedAcc);\n")
          .append("                document.documentElement.style.setProperty('--accent-glow', savedAcc + '44');\n")
          .append("            }\n")
          .append("        })();\n")
          .append("    </script>\n")
          .append("    <style>\n")
          .append(getCssStyles())
          .append("    </style>\n")
          .append("</head>\n")
          .append("<body>\n");

        // Navbar
        sb.append("<header class=\"navbar\">\n")
          .append("    <div class=\"nav-container\">\n")
          .append("        <a href=\"/dashboard\" class=\"nav-brand\">\n")
          .append("            <span class=\"brand-logo-wrap\">\n")
          .append("                <svg class=\"brand-svg-logo\" width=\"34\" height=\"34\" viewBox=\"0 0 100 100\" fill=\"none\">\n")
          .append("                    <defs>\n")
          .append("                        <linearGradient id=\"fpLogoGrad\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"100%\">\n")
          .append("                            <stop offset=\"0%\" stop-color=\"var(--primary)\" />\n")
          .append("                            <stop offset=\"100%\" stop-color=\"var(--accent)\" />\n")
          .append("                        </linearGradient>\n")
          .append("                        <filter id=\"fpglow\" x=\"-20%\" y=\"-20%\" width=\"140%\" height=\"140%\">\n")
          .append("                            <feGaussianBlur stdDeviation=\"3\" result=\"blur\" />\n")
          .append("                            <feComposite in=\"SourceGraphic\" in2=\"blur\" operator=\"over\" />\n")
          .append("                        </filter>\n")
          .append("                    </defs>\n")
          .append("                    <rect x=\"6\" y=\"6\" width=\"88\" height=\"88\" rx=\"24\" fill=\"url(#fpLogoGrad)\" filter=\"url(#fpglow)\"/>\n")
          .append("                    <path d=\"M18 52H32L40 28L52 72L60 44L68 54H82\" stroke=\"#ffffff\" stroke-width=\"7\" stroke-linecap=\"round\" stroke-linejoin=\"round\"/>\n")
          .append("                    <circle cx=\"40\" cy=\"28\" r=\"4.5\" fill=\"#ffffff\"/>\n")
          .append("                    <circle cx=\"52\" cy=\"72\" r=\"4.5\" fill=\"#ffffff\"/>\n")
          .append("                </svg>\n")
          .append("            </span>\n")
          .append("            <span class=\"brand-title-wrap\">Fit<span class=\"brand-title-highlight\">Pulse</span></span>\n")
          .append("        </a>\n");

        sb.append("        <nav class=\"nav-links\">\n")
          .append("            <a href=\"/dashboard\" class=\"nav-item\">Dashboard</a>\n")
          .append("            <a href=\"/client/plans\" class=\"nav-item\">Workout Plans</a>\n")
          .append("            <a href=\"/spa\" class=\"nav-item nav-spa-item\">🧖‍♀️ Spa & Wellness</a>\n")
          .append("            <a href=\"/slots\" class=\"nav-item nav-slots-item\">📅 Book Slots</a>\n")
          .append("            <a href=\"/exercises\" class=\"nav-item\">📖 Exercises</a>\n");

        if (loggedUser != null && loggedUser.getRole() == Role.CLIENT) {
            sb.append("            <a href=\"/slots/my-bookings\" class=\"nav-item\">🎟️ My Slots</a>\n")
              .append("            <a href=\"/progress/history\" class=\"nav-item\">History</a>\n");
        }
        sb.append("        </nav>\n");

        // Right side: Theme Customizer + User info
        sb.append("        <div class=\"nav-user\">\n")
          .append("            <div class=\"theme-picker-wrap\">\n")
          .append("                <button id=\"themeToggleBtn\" class=\"theme-btn\" type=\"button\" title=\"Customize UI Theme Color\">\n")
          .append("                    🎨 <span class=\"theme-btn-text\">Theme</span>\n")
          .append("                </button>\n")
          .append("                <div id=\"themeDropdown\" class=\"theme-dropdown\">\n")
          .append("                    <div class=\"theme-title\">Custom UI Theme Colors</div>\n")
          .append("                    <div class=\"theme-presets\">\n")
          .append("                        <button type=\"button\" class=\"theme-chip\" data-color=\"#6366f1\" data-accent=\"#38bdf8\" style=\"background: #6366f1;\" title=\"Neon Indigo (Default)\"></button>\n")
          .append("                        <button type=\"button\" class=\"theme-chip\" data-color=\"#10b981\" data-accent=\"#34d399\" style=\"background: #10b981;\" title=\"Cyber Emerald\"></button>\n")
          .append("                        <button type=\"button\" class=\"theme-chip\" data-color=\"#f97316\" data-accent=\"#fbbf24\" style=\"background: #f97316;\" title=\"Sunset Blaze\"></button>\n")
          .append("                        <button type=\"button\" class=\"theme-chip\" data-color=\"#a855f7\" data-accent=\"#ec4899\" style=\"background: #a855f7;\" title=\"Electric Violet\"></button>\n")
          .append("                        <button type=\"button\" class=\"theme-chip\" data-color=\"#06b6d4\" data-accent=\"#3b82f6\" style=\"background: #06b6d4;\" title=\"Ocean Cyan\"></button>\n")
          .append("                        <button type=\"button\" class=\"theme-chip\" data-color=\"#e11d48\" data-accent=\"#fb7185\" style=\"background: #e11d48;\" title=\"Crimson Luxury\"></button>\n")
          .append("                        <button type=\"button\" class=\"theme-chip\" data-color=\"#eab308\" data-accent=\"#f59e0b\" style=\"background: #eab308;\" title=\"Amber Horizon\"></button>\n")
          .append("                    </div>\n")
          .append("                    <div class=\"theme-custom-row\">\n")
          .append("                        <label for=\"customColorInput\" style=\"font-size: 0.8rem; color: var(--text-muted);\">Custom Hex:</label>\n")
          .append("                        <input type=\"color\" id=\"customColorInput\" value=\"#6366f1\" title=\"Pick any custom HEX color\">\n")
          .append("                    </div>\n")
          .append("                </div>\n")
          .append("            </div>\n");

        if (loggedUser != null) {
            sb.append("            <div class=\"user-badge\">\n")
              .append("                <span class=\"role-tag role-").append(loggedUser.getRole().name().toLowerCase()).append("\">")
              .append(loggedUser.getRole().name()).append("</span>\n")
              .append("                <span class=\"user-name\">").append(escape(loggedUser.getName())).append("</span>\n")
              .append("            </div>\n")
              .append("            <a href=\"/auth/logout\" class=\"btn btn-outline btn-sm\">Logout</a>\n");
        } else {
            sb.append("            <a href=\"/auth/login\" class=\"btn btn-outline btn-sm\">Sign In</a>\n")
              .append("            <a href=\"/auth/register\" class=\"btn btn-primary btn-sm\">Register</a>\n");
        }

        sb.append("        </div>\n")
          .append("    </div>\n")
          .append("</header>\n")
          .append("<main class=\"main-content\">\n");

        return sb.toString();
    }

    public static String getCommonFooter() {
        return "</main>\n" +
                "<footer class=\"footer\">\n" +
                "    <div class=\"footer-container\">\n" +
                "        <p>© 2026 FitPulse Online Fitness & Spa Sanctuary • Pure Java Enterprise • Multithreaded Core • JDBC Engine</p>\n" +
                "    </div>\n" +
                "</footer>\n" +
                "<script>\n" +
                "    // Theme Switcher & Persistence Engine\n" +
                "    document.addEventListener('DOMContentLoaded', function() {\n" +
                "        const btn = document.getElementById('themeToggleBtn');\n" +
                "        const drop = document.getElementById('themeDropdown');\n" +
                "        const picker = document.getElementById('customColorInput');\n" +
                "        if (btn && drop) {\n" +
                "            btn.addEventListener('click', function(e) {\n" +
                "                e.stopPropagation();\n" +
                "                drop.classList.toggle('show');\n" +
                "            });\n" +
                "            document.addEventListener('click', function() {\n" +
                "                drop.classList.remove('show');\n" +
                "            });\n" +
                "            drop.addEventListener('click', function(e) {\n" +
                "                e.stopPropagation();\n" +
                "            });\n" +
                "        }\n" +
                "        document.querySelectorAll('.theme-chip').forEach(function(chip) {\n" +
                "            chip.addEventListener('click', function() {\n" +
                "                const color = chip.getAttribute('data-color');\n" +
                "                const accent = chip.getAttribute('data-accent');\n" +
                "                applyTheme(color, accent);\n" +
                "            });\n" +
                "        });\n" +
                "        if (picker) {\n" +
                "            picker.addEventListener('input', function() {\n" +
                "                applyTheme(picker.value, picker.value);\n" +
                "            });\n" +
                "        }\n" +
                "        function applyTheme(color, accent) {\n" +
                "            document.documentElement.style.setProperty('--primary', color);\n" +
                "            document.documentElement.style.setProperty('--primary-hover', color);\n" +
                "            document.documentElement.style.setProperty('--primary-glow', color + '44');\n" +
                "            if (accent) {\n" +
                "                document.documentElement.style.setProperty('--accent', accent);\n" +
                "                document.documentElement.style.setProperty('--accent-glow', accent + '44');\n" +
                "            }\n" +
                "            localStorage.setItem('fitpulse_custom_color', color);\n" +
                "            if (accent) localStorage.setItem('fitpulse_custom_accent', accent);\n" +
                "        }\n" +
                "    });\n" +
                "</script>\n" +
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
                --primary: #6366f1;
                --primary-hover: #4f46e5;
                --primary-glow: rgba(99, 102, 241, 0.35);
                --accent: #06b6d4;
                --accent-glow: rgba(6, 182, 212, 0.35);
                --success: #10b981;
                --warning: #f59e0b;
                --danger: #ef4444;
                --bg: #090d16;
                --card-bg: rgba(22, 30, 48, 0.78);
                --card-border: rgba(255, 255, 255, 0.08);
                --text-main: #f8fafc;
                --text-muted: #94a3b8;
                --font: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
            }
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body {
                font-family: var(--font);
                background-color: var(--bg);
                background-image: radial-gradient(circle at 15% 15%, var(--primary-glow) 0%, transparent 40%),
                                  radial-gradient(circle at 85% 85%, var(--accent-glow) 0%, transparent 45%);
                background-attachment: fixed;
                color: var(--text-main);
                min-height: 100vh;
                display: flex;
                flex-direction: column;
                line-height: 1.6;
            }
            .navbar {
                background: rgba(15, 23, 42, 0.85);
                backdrop-filter: blur(14px);
                border-bottom: 1px solid var(--card-border);
                position: sticky;
                top: 0;
                z-index: 100;
            }
            .nav-container {
                max-width: 1320px;
                margin: 0 auto;
                padding: 0.9rem 1.5rem;
                display: flex;
                align-items: center;
                justify-content: space-between;
                gap: 1rem;
            }
            .nav-brand {
                display: flex;
                align-items: center;
                gap: 0.75rem;
                text-decoration: none;
                color: var(--text-main);
                font-weight: 800;
                font-size: 1.38rem;
            }
            .brand-logo-wrap {
                display: flex;
                align-items: center;
                filter: drop-shadow(0 2px 8px var(--primary-glow));
                transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
            }
            .nav-brand:hover .brand-logo-wrap {
                transform: scale(1.08) rotate(4deg);
            }
            .brand-title-wrap {
                font-weight: 800;
                letter-spacing: -0.02em;
            }
            .brand-title-highlight {
                background: linear-gradient(135deg, var(--primary), var(--accent));
                -webkit-background-clip: text;
                -webkit-text-fill-color: transparent;
            }
            .nav-links {
                display: flex;
                align-items: center;
                gap: 1.25rem;
                flex-wrap: wrap;
            }
            .nav-item {
                color: var(--text-muted);
                text-decoration: none;
                font-weight: 600;
                font-size: 0.92rem;
                transition: color 0.18s ease;
                padding: 0.35rem 0.6rem;
                border-radius: 0.375rem;
            }
            .nav-item:hover {
                color: #fff;
                background: rgba(255, 255, 255, 0.05);
            }
            .nav-spa-item {
                color: #f472b6 !important;
            }
            .nav-slots-item {
                color: #38bdf8 !important;
            }
            .nav-user {
                display: flex;
                align-items: center;
                gap: 0.85rem;
            }

            /* Theme Picker Dropdown */
            .theme-picker-wrap {
                position: relative;
            }
            .theme-btn {
                background: rgba(255, 255, 255, 0.06);
                border: 1px solid var(--card-border);
                color: #fff;
                padding: 0.4rem 0.75rem;
                border-radius: 0.5rem;
                font-family: inherit;
                font-size: 0.85rem;
                font-weight: 600;
                cursor: pointer;
                display: inline-flex;
                align-items: center;
                gap: 0.4rem;
                transition: all 0.2s;
            }
            .theme-btn:hover {
                background: rgba(255, 255, 255, 0.12);
                border-color: var(--primary);
                box-shadow: 0 0 12px var(--primary-glow);
            }
            .theme-dropdown {
                display: none;
                position: absolute;
                top: calc(100% + 8px);
                right: 0;
                background: #1e293b;
                border: 1px solid rgba(255, 255, 255, 0.12);
                border-radius: 0.75rem;
                padding: 1rem;
                width: 220px;
                box-shadow: 0 10px 25px rgba(0, 0, 0, 0.5);
                z-index: 200;
            }
            .theme-dropdown.show {
                display: block;
                animation: fadeInDown 0.2s ease;
            }
            @keyframes fadeInDown {
                from { opacity: 0; transform: translateY(-6px); }
                to { opacity: 1; transform: translateY(0); }
            }
            .theme-title {
                font-size: 0.75rem;
                font-weight: 700;
                text-transform: uppercase;
                letter-spacing: 0.05em;
                color: var(--text-muted);
                margin-bottom: 0.75rem;
            }
            .theme-presets {
                display: grid;
                grid-template-columns: repeat(7, 1fr);
                gap: 0.4rem;
                margin-bottom: 0.85rem;
            }
            .theme-chip {
                width: 22px;
                height: 22px;
                border-radius: 50%;
                border: 2px solid rgba(255, 255, 255, 0.3);
                cursor: pointer;
                transition: transform 0.15s, border-color 0.15s;
            }
            .theme-chip:hover {
                transform: scale(1.25);
                border-color: #fff;
            }
            .theme-custom-row {
                display: flex;
                align-items: center;
                justify-content: space-between;
                border-top: 1px solid rgba(255, 255, 255, 0.08);
                padding-top: 0.65rem;
            }
            #customColorInput {
                cursor: pointer;
                background: none;
                border: none;
                width: 28px;
                height: 28px;
                padding: 0;
            }

            .user-badge {
                display: flex;
                align-items: center;
                gap: 0.6rem;
                font-size: 0.9rem;
            }
            .role-tag {
                font-size: 0.72rem;
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
                max-width: 1320px;
                width: 100%;
                margin: 0 auto;
                padding: 2.25rem 1.5rem;
                flex: 1;
            }
            .hero-banner {
                background: linear-gradient(135deg, rgba(30, 27, 75, 0.85) 0%, rgba(15, 23, 42, 0.9) 100%);
                border: 1px solid rgba(99, 102, 241, 0.35);
                border-radius: 1.25rem;
                padding: 2.75rem;
                margin-bottom: 2.25rem;
                box-shadow: 0 15px 35px -5px rgba(0, 0, 0, 0.4), 0 0 25px var(--primary-glow);
                position: relative;
                overflow: hidden;
            }
            .spa-hero {
                background: linear-gradient(135deg, rgba(74, 15, 53, 0.85) 0%, rgba(15, 23, 42, 0.92) 100%) !important;
                border-color: rgba(244, 114, 182, 0.4) !important;
                box-shadow: 0 15px 35px -5px rgba(0, 0, 0, 0.4), 0 0 25px rgba(244, 114, 182, 0.25) !important;
            }
            .hero-title { font-size: 2.3rem; font-weight: 800; margin-bottom: 0.6rem; letter-spacing: -0.02em; }
            .hero-sub { color: #cbd5e1; font-size: 1.08rem; max-width: 780px; }

            .stats-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
                gap: 1.25rem;
                margin-bottom: 2.25rem;
            }
            .stat-card {
                background: var(--card-bg);
                border: 1px solid var(--card-border);
                backdrop-filter: blur(10px);
                border-radius: 0.875rem;
                padding: 1.5rem;
                display: flex;
                flex-direction: column;
                gap: 0.5rem;
                transition: transform 0.2s, border-color 0.2s, box-shadow 0.2s;
            }
            .stat-card:hover {
                transform: translateY(-3px);
                border-color: var(--primary);
                box-shadow: 0 10px 20px -5px var(--primary-glow);
            }
            .stat-label { color: var(--text-muted); font-size: 0.82rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; }
            .stat-value { font-size: 1.95rem; font-weight: 800; color: #fff; }
            .stat-meta { font-size: 0.82rem; color: var(--accent); }

            .grid-3 {
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
                gap: 1.5rem;
                margin-bottom: 2.5rem;
            }
            .card {
                background: var(--card-bg);
                border: 1px solid var(--card-border);
                backdrop-filter: blur(12px);
                border-radius: 1rem;
                padding: 1.6rem;
                display: flex;
                flex-direction: column;
                justify-content: space-between;
                box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
                transition: transform 0.22s cubic-bezier(0.34, 1.56, 0.64, 1), border-color 0.22s, box-shadow 0.22s;
            }
            .card:hover {
                transform: translateY(-4px);
                border-color: var(--primary);
                box-shadow: 0 14px 28px -6px rgba(0, 0, 0, 0.35), 0 0 20px var(--primary-glow);
            }
            .card-header {
                display: flex;
                justify-content: space-between;
                align-items: flex-start;
                margin-bottom: 1rem;
            }
            .card-title { font-size: 1.25rem; font-weight: 700; line-height: 1.35; }
            .card-desc { color: var(--text-muted); font-size: 0.92rem; margin-bottom: 1.25rem; flex-grow: 1; }
            .card-meta-list {
                display: flex;
                flex-direction: column;
                gap: 0.4rem;
                margin-bottom: 1.25rem;
                font-size: 0.88rem;
                background: rgba(10, 15, 28, 0.65);
                padding: 0.85rem 1rem;
                border-radius: 0.625rem;
                border: 1px solid rgba(255, 255, 255, 0.04);
            }
            .card-meta-item { display: flex; justify-content: space-between; }
            .card-meta-label { color: var(--text-muted); }
            .card-meta-value { font-weight: 600; color: #fff; }

            .badge {
                font-size: 0.75rem;
                padding: 0.25rem 0.55rem;
                border-radius: 0.4rem;
                font-weight: 700;
                letter-spacing: 0.02em;
            }
            .badge-strength { background: rgba(99, 102, 241, 0.2); color: #a5b4fc; border: 1px solid rgba(99, 102, 241, 0.35); }
            .badge-cardio { background: rgba(16, 185, 129, 0.2); color: #6ee7b7; border: 1px solid rgba(16, 185, 129, 0.35); }
            .badge-hiit { background: rgba(239, 68, 68, 0.2); color: #fca5a5; border: 1px solid rgba(239, 68, 68, 0.35); }
            .badge-spa { background: rgba(236, 72, 153, 0.2); color: #f472b6; border: 1px solid rgba(236, 72, 153, 0.4); }

            .btn {
                display: inline-flex;
                align-items: center;
                justify-content: center;
                gap: 0.5rem;
                font-family: inherit;
                font-size: 0.95rem;
                font-weight: 600;
                padding: 0.65rem 1.25rem;
                border-radius: 0.55rem;
                border: none;
                cursor: pointer;
                text-decoration: none;
                transition: all 0.18s ease-in-out;
            }
            .btn-primary {
                background: var(--primary);
                color: #fff;
                box-shadow: 0 4px 14px var(--primary-glow);
            }
            .btn-primary:hover {
                background: var(--primary-hover);
                transform: translateY(-1px);
                box-shadow: 0 6px 20px var(--primary-glow);
            }
            .btn-success { background: var(--success); color: #fff; }
            .btn-danger { background: var(--danger); color: #fff; }
            .btn-outline {
                background: transparent;
                border: 1px solid var(--card-border);
                color: var(--text-main);
            }
            .btn-outline:hover {
                background: rgba(255, 255, 255, 0.08);
                border-color: var(--primary);
            }
            .btn-sm { padding: 0.4rem 0.75rem; font-size: 0.85rem; }
            .btn-block { width: 100%; }

            .table-container {
                background: var(--card-bg);
                border: 1px solid var(--card-border);
                border-radius: 0.875rem;
                overflow-x: auto;
                margin-bottom: 2rem;
            }
            table { width: 100%; border-collapse: collapse; text-align: left; font-size: 0.92rem; }
            th {
                background: #0f172a;
                padding: 0.9rem 1.25rem;
                font-weight: 700;
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
                border-radius: 1rem;
                padding: 2.25rem;
                max-width: 600px;
                margin: 0 auto 2rem auto;
                box-shadow: 0 15px 35px rgba(0, 0, 0, 0.3);
            }
            .form-group { margin-bottom: 1.25rem; }
            .form-label { display: block; font-size: 0.88rem; font-weight: 600; margin-bottom: 0.4rem; color: #cbd5e1; }
            .form-control {
                width: 100%;
                padding: 0.65rem 0.85rem;
                background: #0b1120;
                border: 1px solid var(--card-border);
                border-radius: 0.5rem;
                color: #fff;
                font-family: inherit;
                font-size: 0.95rem;
                transition: border-color 0.2s, box-shadow 0.2s;
            }
            .form-control:focus {
                outline: none;
                border-color: var(--primary);
                box-shadow: 0 0 0 3px var(--primary-glow);
            }
            .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }

            .alert {
                padding: 1rem 1.25rem;
                border-radius: 0.6rem;
                margin-bottom: 1.5rem;
                font-weight: 500;
            }
            .alert-success { background: rgba(16, 185, 129, 0.15); border: 1px solid var(--success); color: #6ee7b7; }
            .alert-error { background: rgba(239, 68, 68, 0.15); border: 1px solid var(--danger); color: #fca5a5; }

            .section-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 1.5rem;
            }
            .section-title { font-size: 1.45rem; font-weight: 800; letter-spacing: -0.01em; }

            .footer {
                border-top: 1px solid var(--card-border);
                background: #0b0f19;
                padding: 1.75rem 0;
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
                flex-wrap: wrap;
            }
            .tab-btn {
                background: transparent;
                border: none;
                color: var(--text-muted);
                font-family: inherit;
                font-size: 0.95rem;
                font-weight: 600;
                padding: 0.5rem 1rem;
                cursor: pointer;
                border-radius: 0.5rem;
                text-decoration: none;
                transition: all 0.18s;
            }
            .tab-btn:hover { color: #fff; background: rgba(255, 255, 255, 0.05); }
            .tab-btn.active { background: var(--primary); color: #fff; box-shadow: 0 2px 8px var(--primary-glow); }
            """;
    }

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
