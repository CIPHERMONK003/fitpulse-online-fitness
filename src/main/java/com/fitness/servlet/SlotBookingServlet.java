package com.fitness.servlet;

import com.fitness.model.Role;
import com.fitness.model.SessionSlot;
import com.fitness.model.SlotBooking;
import com.fitness.model.User;
import com.fitness.service.SlotBookingService;
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
 * Servlet handling Real-Time Slot Bookings and Spa Wellness Experience.
 * Demonstrates Java Servlets, Session Management, and Role-Based Action Validation.
 */
public class SlotBookingServlet extends HttpServlet {

    private final SlotBookingService bookingService = new SlotBookingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        String pathInfo = req.getPathInfo();

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedUser") : null;

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        if ("/spa".equals(servletPath)) {
            renderSpaLanding(req, out, user);
            return;
        }

        // /slots routes
        if (pathInfo == null || "/".equals(pathInfo) || "/catalog".equals(pathInfo)) {
            renderSlotsCatalog(req, out, user);
        } else if ("/my-bookings".equals(pathInfo)) {
            if (user == null) {
                resp.sendRedirect("/auth/login?msg=" + URLEncoder.encode("Please log in to view your bookings", StandardCharsets.UTF_8));
                return;
            }
            renderMyBookings(req, out, user);
        } else {
            resp.sendRedirect("/slots");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedUser") : null;

        if (user == null) {
            resp.sendRedirect("/auth/login?msg=" + URLEncoder.encode("Please log in to book a session slot", StandardCharsets.UTF_8));
            return;
        }

        try {
            if ("/book".equals(pathInfo)) {
                int slotId = Integer.parseInt(req.getParameter("slotId"));
                String notes = req.getParameter("notes");
                bookingService.bookSlot(slotId, user.getId(), notes);
                resp.sendRedirect("/slots/my-bookings?msg=" + URLEncoder.encode("Session slot successfully booked! A confirmation notification has been queued.", StandardCharsets.UTF_8));
            } else if ("/cancel".equals(pathInfo)) {
                int bookingId = Integer.parseInt(req.getParameter("bookingId"));
                bookingService.cancelBooking(bookingId, user.getId());
                resp.sendRedirect("/slots/my-bookings?msg=" + URLEncoder.encode("Booking cancelled and slot capacity restored.", StandardCharsets.UTF_8));
            } else {
                resp.sendRedirect("/slots");
            }
        } catch (Exception e) {
            resp.sendRedirect("/slots?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    private void renderSlotsCatalog(HttpServletRequest req, PrintWriter out, User user) {
        String filterType = req.getParameter("type");
        List<SessionSlot> slots = bookingService.getAvailableSlots(filterType);

        out.println(HtmlRenderer.getCommonHeader("Session Slot Booking", user));

        String msg = req.getParameter("msg");
        String err = req.getParameter("error");
        if (msg != null) out.println(HtmlRenderer.renderAlert("success", msg));
        if (err != null) out.println(HtmlRenderer.renderAlert("error", err));

        out.println("<div class=\"hero-banner\">");
        out.println("    <h1 class=\"hero-title\">Live Session & Slot Booking 📅</h1>");
        out.println("    <p class=\"hero-sub\">Reserve guaranteed coaching sessions, group athletic classes, and restorative spa appointments with real-time member slot tracking.</p>");
        out.println("</div>");

        // Filter Bar & Action buttons
        out.println("<div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;\">");
        out.println("    <div class=\"tab-nav\" style=\"margin-bottom: 0;\">");
        out.println("        <a href=\"/slots\" class=\"tab-btn " + (filterType == null || "ALL".equalsIgnoreCase(filterType) ? "active" : "") + "\">All Slots (" + slots.size() + ")</a>");
        out.println("        <a href=\"/slots?type=SPA\" class=\"tab-btn " + ("SPA".equalsIgnoreCase(filterType) ? "active" : "") + "\">🧖‍♀️ Spa & Recovery</a>");
        out.println("        <a href=\"/slots?type=COACHING\" class=\"tab-btn " + ("COACHING".equalsIgnoreCase(filterType) ? "active" : "") + "\">🏋️ Coaching & Classes</a>");
        out.println("    </div>");

        if (user != null) {
            out.println("    <a href=\"/slots/my-bookings\" class=\"btn btn-primary btn-sm\">🎟️ My Booked Slots</a>");
        }
        out.println("</div>");

        if (slots.isEmpty()) {
            out.println("<div class=\"card\" style=\"text-align: center; padding: 3rem;\">");
            out.println("    <h3>No session slots match your filter.</h3>");
            out.println("    <p style=\"color: var(--text-muted); margin-top: 0.5rem;\">Check back soon as new coach and therapist slots are added daily.</p>");
            out.println("</div>");
        } else {
            out.println("<div class=\"grid-3\">");
            for (SessionSlot s : slots) {
                boolean isFull = s.isFullyBooked();
                int avail = s.getAvailableSlots();

                String typeBadgeClass = "SPA".equalsIgnoreCase(s.getSessionType()) ? "badge-spa" : "badge-strength";
                String typeEmoji = "SPA".equalsIgnoreCase(s.getSessionType()) ? "🧖‍♀️" : "🏋️";

                out.println("<div class=\"card session-slot-card\">");
                out.println("    <div class=\"card-header\">");
                out.println("        <span class=\"badge " + typeBadgeClass + "\">" + typeEmoji + " " + HtmlRenderer.escape(s.getSessionType()) + "</span>");
                if (isFull) {
                    out.println("        <span class=\"badge\" style=\"background: #ef4444; color: #fff;\">🔴 FULL</span>");
                } else if (avail <= 2) {
                    out.println("        <span class=\"badge\" style=\"background: #f59e0b; color: #fff;\">⚠️ " + avail + " left</span>");
                } else {
                    out.println("        <span class=\"badge\" style=\"background: #10b981; color: #fff;\">🟢 " + avail + " spots</span>");
                }
                out.println("    </div>");

                out.println("    <h3 class=\"card-title\">" + HtmlRenderer.escape(s.getTitle()) + "</h3>");
                out.println("    <p class=\"card-desc\" style=\"margin-top: 0.25rem;\">Instructor / Specialist: <strong style=\"color: #fff;\">" + HtmlRenderer.escape(s.getCoachName()) + "</strong></p>");

                out.println("    <div class=\"card-meta-list\">");
                out.println("        <div class=\"card-meta-item\"><span class=\"card-meta-label\">Date & Time:</span><span class=\"card-meta-value\">" + HtmlRenderer.escape(s.getSessionDate()) + " • " + HtmlRenderer.escape(s.getStartTime()) + " - " + HtmlRenderer.escape(s.getEndTime()) + "</span></div>");
                out.println("        <div class=\"card-meta-item\"><span class=\"card-meta-label\">Location / Room:</span><span class=\"card-meta-value\">📍 " + HtmlRenderer.escape(s.getRoomLocation()) + "</span></div>");
                out.println("        <div class=\"card-meta-item\"><span class=\"card-meta-label\">Member Slots:</span><span class=\"card-meta-value\">" + s.getBookedCount() + " / " + s.getMaxCapacity() + " booked</span></div>");
                out.println("    </div>");

                // Booking form
                if (user != null) {
                    if (isFull) {
                        out.println("    <button class=\"btn btn-outline btn-block\" disabled style=\"opacity: 0.5; cursor: not-allowed;\">Session Full</button>");
                    } else {
                        out.println("    <form method=\"POST\" action=\"/slots/book\">");
                        out.println("        <input type=\"hidden\" name=\"slotId\" value=\"" + s.getId() + "\"/>");
                        out.println("        <button type=\"submit\" class=\"btn btn-primary btn-block\">⚡ Reserve This Slot</button>");
                        out.println("    </form>");
                    }
                } else {
                    out.println("    <a href=\"/auth/login\" class=\"btn btn-outline btn-block\">Sign in to Reserve</a>");
                }

                out.println("</div>");
            }
            out.println("</div>");
        }

        out.println(HtmlRenderer.getCommonFooter());
    }

    private void renderMyBookings(HttpServletRequest req, PrintWriter out, User user) {
        List<SlotBooking> bookings = bookingService.getClientBookings(user.getId());

        out.println(HtmlRenderer.getCommonHeader("My Booked Slots", user));

        String msg = req.getParameter("msg");
        String err = req.getParameter("error");
        if (msg != null) out.println(HtmlRenderer.renderAlert("success", msg));
        if (err != null) out.println(HtmlRenderer.renderAlert("error", err));

        out.println("<div class=\"section-header\">");
        out.println("    <div>");
        out.println("        <h1 class=\"section-title\">🎟️ My Confirmed Session Slots</h1>");
        out.println("        <p style=\"color: var(--text-muted); font-size: 0.95rem;\">All your active and upcoming wellness appointments and coaching sessions.</p>");
        out.println("    </div>");
        out.println("    <a href=\"/slots\" class=\"btn btn-outline btn-sm\">← Browse More Slots</a>");
        out.println("</div>");

        if (bookings.isEmpty()) {
            out.println("<div class=\"card\" style=\"text-align: center; padding: 3rem;\">");
            out.println("    <h3>You have no session bookings yet.</h3>");
            out.println("    <p style=\"color: var(--text-muted); margin: 0.75rem 0 1.5rem 0;\">Reserve a restorative spa treatment or personal coaching session in just 1 click.</p>");
            out.println("    <a href=\"/slots\" class=\"btn btn-primary\">Browse Available Slots</a>");
            out.println("</div>");
        } else {
            out.println("<div class=\"table-container\">");
            out.println("<table>");
            out.println("    <thead>");
            out.println("        <tr>");
            out.println("            <th>Session Type</th>");
            out.println("            <th>Session Title</th>");
            out.println("            <th>Specialist / Coach</th>");
            out.println("            <th>Scheduled Time</th>");
            out.println("            <th>Room</th>");
            out.println("            <th>Status</th>");
            out.println("            <th>Action</th>");
            out.println("        </tr>");
            out.println("    </thead>");
            out.println("    <tbody>");
            for (SlotBooking b : bookings) {
                boolean isConfirmed = "CONFIRMED".equalsIgnoreCase(b.getStatus());
                String statusColor = isConfirmed ? "var(--success)" : "var(--text-muted)";

                out.println("        <tr>");
                out.println("            <td><span class=\"badge " + ("SPA".equalsIgnoreCase(b.getSessionType()) ? "badge-spa" : "badge-strength") + "\">" + HtmlRenderer.escape(b.getSessionType()) + "</span></td>");
                out.println("            <td><strong>" + HtmlRenderer.escape(b.getSessionTitle()) + "</strong></td>");
                out.println("            <td>" + HtmlRenderer.escape(b.getCoachName()) + "</td>");
                out.println("            <td>" + HtmlRenderer.escape(b.getSessionDate()) + " • " + HtmlRenderer.escape(b.getStartTime()) + "</td>");
                out.println("            <td>📍 " + HtmlRenderer.escape(b.getRoomLocation()) + "</td>");
                out.println("            <td><span style=\"color: " + statusColor + "; font-weight: 700;\">" + HtmlRenderer.escape(b.getStatus()) + "</span></td>");
                out.println("            <td>");
                if (isConfirmed) {
                    out.println("                <form method=\"POST\" action=\"/slots/cancel\" style=\"margin:0;\" onsubmit=\"return confirm('Cancel this slot reservation?');\">");
                    out.println("                    <input type=\"hidden\" name=\"bookingId\" value=\"" + b.getId() + "\"/>");
                    out.println("                    <button type=\"submit\" class=\"btn btn-danger btn-sm\">Cancel</button>");
                    out.println("                </form>");
                } else {
                    out.println("                <span style=\"color: var(--text-muted); font-size: 0.85rem;\">Released</span>");
                }
                out.println("            </td>");
                out.println("        </tr>");
            }
            out.println("    </tbody>");
            out.println("</table>");
            out.println("</div>");
        }

        out.println(HtmlRenderer.getCommonFooter());
    }

    private void renderSpaLanding(HttpServletRequest req, PrintWriter out, User user) {
        out.println(HtmlRenderer.getCommonHeader("Spa & Wellness Sanctuaries", user));

        out.println("<div class=\"hero-banner spa-hero\">");
        out.println("    <h1 class=\"hero-title\">🧖‍♀️ FitPulse Spa & Thermal Sanctuaries</h1>");
        out.println("    <p class=\"hero-sub\">Accelerate biological recovery, flush systemic inflammation, and lower cortisol with luxury Swedish massage, Nordic hydrotherapy, sub-zero cryotherapy, and infrared thermal suites.</p>");
        out.println("    <div style=\"margin-top: 1.5rem; display: flex; gap: 1rem; justify-content: center; flex-wrap: wrap;\">");
        out.println("        <a href=\"/slots?type=SPA\" class=\"btn btn-primary\">📅 Book Spa Session Slots</a>");
        out.println("        <a href=\"/client/plans?type=SPA_WELLNESS\" class=\"btn btn-outline\">Browse Spa Recovery Plans</a>");
        out.println("    </div>");
        out.println("</div>");

        // Spa Services Showcase Grid
        out.println("<h2 class=\"section-title\" style=\"margin-bottom: 1.5rem;\">Our Luxury Recovery Treatments</h2>");
        out.println("<div class=\"grid-3\">");

        // Card 1
        out.println("<div class=\"card\">");
        out.println("    <div class=\"card-header\"><span class=\"badge badge-spa\">Hydrotherapy</span><span style=\"font-size: 1.8rem;\">🛁</span></div>");
        out.println("    <h3 class=\"card-title\">Nordic Contrast Hydrotherapy Soak</h3>");
        out.println("    <p class=\"card-desc\">Cycle between 82°C Finnish dry sauna and 10°C sub-zero cold plunge baths. Causes dynamic vasodilation and constriction, flushing lactic acid and resetting the autonomic nervous system.</p>");
        out.println("    <div class=\"card-meta-list\">");
        out.println("        <div class=\"card-meta-item\"><span class=\"card-meta-label\">Duration:</span><span class=\"card-meta-value\">60 Minutes</span></div>");
        out.println("        <div class=\"card-meta-item\"><span class=\"card-meta-label\">Relaxation Rating:</span><span class=\"card-meta-value\">9.5 / 10 ⭐</span></div>");
        out.println("        <div class=\"card-meta-item\"><span class=\"card-meta-label\">Suite:</span><span class=\"card-meta-value\">Nordic Thermal Bath Suite</span></div>");
        out.println("    </div>");
        out.println("    <a href=\"/slots?type=SPA\" class=\"btn btn-primary btn-block\">Book Slot Now</a>");
        out.println("</div>");

        // Card 2
        out.println("<div class=\"card\">");
        out.println("    <div class=\"card-header\"><span class=\"badge badge-spa\">Deep Tissue</span><span style=\"font-size: 1.8rem;\">💆‍♂️</span></div>");
        out.println("    <h3 class=\"card-title\">Swedish Deep Tissue & Heated Stone</h3>");
        out.println("    <p class=\"card-desc\">Targeted deep pressure therapy administered by Licensed Massage Therapists (LMT) using warm volcanic basalt stones and organic lavender-infused arnica botanicals to dissolve deep fascial adhesions.</p>");
        out.println("    <div class=\"card-meta-list\">");
        out.println("        <div class=\"card-meta-item\"><span class=\"card-meta-label\">Duration:</span><span class=\"card-meta-value\">60 Minutes</span></div>");
        out.println("        <div class=\"card-meta-item\"><span class=\"card-meta-label\">Relaxation Rating:</span><span class=\"card-meta-value\">10 / 10 ⭐</span></div>");
        out.println("        <div class=\"card-meta-item\"><span class=\"card-meta-label\">Suite:</span><span class=\"card-meta-value\">Zen Luxury Suite 1</span></div>");
        out.println("    </div>");
        out.println("    <a href=\"/slots?type=SPA\" class=\"btn btn-primary btn-block\">Book Slot Now</a>");
        out.println("</div>");

        // Card 3
        out.println("<div class=\"card\">");
        out.println("    <div class=\"card-header\"><span class=\"badge badge-spa\">Cryotherapy</span><span style=\"font-size: 1.8rem;\">❄️</span></div>");
        out.println("    <h3 class=\"card-title\">Sub-Zero Whole Body Cryotherapy (-110°C)</h3>");
        out.println("    <p class=\"card-desc\">Step into our state-of-the-art sub-zero vapor chamber for a rapid 3-minute exposure. Triggers an instant anti-inflammatory endorphin surge, speeds cellular recovery, and elevates metabolism.</p>");
        out.println("    <div class=\"card-meta-list\">");
        out.println("        <div class=\"card-meta-item\"><span class=\"card-meta-label\">Duration:</span><span class=\"card-meta-value\">20 Minutes</span></div>");
        out.println("        <div class=\"card-meta-item\"><span class=\"card-meta-label\">Relaxation Rating:</span><span class=\"card-meta-value\">8.8 / 10 ⭐</span></div>");
        out.println("        <div class=\"card-meta-item\"><span class=\"card-meta-label\">Suite:</span><span class=\"card-meta-value\">Cryo Chamber A</span></div>");
        out.println("    </div>");
        out.println("    <a href=\"/slots?type=SPA\" class=\"btn btn-primary btn-block\">Book Slot Now</a>");
        out.println("</div>");

        out.println("</div>");

        out.println(HtmlRenderer.getCommonFooter());
    }
}
