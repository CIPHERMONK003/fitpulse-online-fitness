package com.fitness.servlet;

import com.fitness.model.*;
import com.fitness.service.CoachFeedbackService;
import com.fitness.service.WorkoutPlanService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Servlet handling Coach actions: plan creation (Polymorphism) and client feedback.
 */
public class CoachServlet extends HttpServlet {

    private final WorkoutPlanService planService = new WorkoutPlanService();
    private final CoachFeedbackService feedbackService = new CoachFeedbackService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedUser") : null;

        if (user == null || (user.getRole() != Role.COACH && user.getRole() != Role.ADMIN)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only coaches may perform this action.");
            return;
        }

        try {
            if ("/plans/new".equals(path)) {
                handleCreatePlan(req, user);
                resp.sendRedirect("/dashboard?msg=" + URLEncoder.encode("Workout plan created successfully!", StandardCharsets.UTF_8));
            } else if ("/feedback".equals(path)) {
                handleSendFeedback(req, user);
                resp.sendRedirect("/dashboard?msg=" + URLEncoder.encode("Feedback sent to client and notification queued!", StandardCharsets.UTF_8));
            } else {
                resp.sendRedirect("/dashboard");
            }
        } catch (Exception e) {
            resp.sendRedirect("/dashboard?error=" + URLEncoder.encode("Error: " + e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    private void handleCreatePlan(HttpServletRequest req, User user) throws Exception {
        String title = req.getParameter("title");
        String desc = req.getParameter("description");
        PlanType planType = PlanType.fromString(req.getParameter("planType"));
        DifficultyLevel level = DifficultyLevel.fromString(req.getParameter("difficultyLevel"));
        int weeks = Integer.parseInt(req.getParameter("durationWeeks"));
        int estMin = Integer.parseInt(req.getParameter("estDurationMinutes"));
        double baseCal = Double.parseDouble(req.getParameter("baseCalories"));

        WorkoutPlan plan;
        switch (planType) {
            case STRENGTH:
                String muscle = req.getParameter("targetMuscleGroup");
                int sets = Integer.parseInt(req.getParameter("recommendedSets"));
                int reps = Integer.parseInt(req.getParameter("recommendedReps"));
                int rest = Integer.parseInt(req.getParameter("restBetweenSetsSec"));
                plan = new StrengthWorkoutPlan(null, user.getId(), user.getName(), title, desc, level, weeks, estMin, baseCal, muscle, sets, reps, rest);
                break;
            case CARDIO:
                String cType = req.getParameter("cardioType");
                String hrZone = req.getParameter("targetHeartRateZone");
                double dist = Double.parseDouble(req.getParameter("distanceKm"));
                double pace = Double.parseDouble(req.getParameter("targetPaceMinPerKm"));
                plan = new CardioWorkoutPlan(null, user.getId(), user.getName(), title, desc, level, weeks, estMin, baseCal, cType, hrZone, dist, pace);
                break;
            case HIIT:
                int workSec = Integer.parseInt(req.getParameter("workIntervalSec"));
                int restSec = Integer.parseInt(req.getParameter("restIntervalSec"));
                int rounds = Integer.parseInt(req.getParameter("numberOfRounds"));
                int intensity = Integer.parseInt(req.getParameter("intensityRating"));
                plan = new HIITWorkoutPlan(null, user.getId(), user.getName(), title, desc, level, weeks, estMin, baseCal, workSec, restSec, rounds, intensity);
                break;
            default:
                throw new IllegalArgumentException("Unknown plan type: " + planType);
        }

        planService.createPlan(plan);
    }

    private void handleSendFeedback(HttpServletRequest req, User user) throws Exception {
        int clientId = Integer.parseInt(req.getParameter("clientId"));
        String message = req.getParameter("message");
        String recommendation = req.getParameter("planRecommendation");
        int rating = Integer.parseInt(req.getParameter("rating"));

        feedbackService.sendFeedback(user.getId(), clientId, message, recommendation, rating);
    }
}
