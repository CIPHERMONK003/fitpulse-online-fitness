package com.fitness.service;

import com.fitness.concurrency.NotificationQueueService;
import com.fitness.concurrency.NotificationTask;
import com.fitness.dao.UserDao;
import com.fitness.dao.WorkoutPlanDao;
import com.fitness.exception.InvalidWorkoutPlanException;
import com.fitness.exception.UserNotFoundException;
import com.fitness.model.*;
import com.fitness.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * Service managing Workout Plans with polymorphic handling and method overloading.
 * Demonstrates:
 * - OOP: Polymorphic dispatch (calculateCaloriesBurned())
 * - OOP: Method Overloading (filterPlans methods)
 * - Collections & Generics (stream filtering, CollectionUtils)
 * - Custom Exception Handling (InvalidWorkoutPlanException)
 */
public class WorkoutPlanService {

    private final WorkoutPlanDao planDao;
    private final UserDao userDao;
    private final NotificationQueueService notificationQueue;

    public WorkoutPlanService() {
        this.planDao = new WorkoutPlanDao();
        this.userDao = new UserDao();
        this.notificationQueue = NotificationQueueService.getInstance();
    }

    public WorkoutPlanService(WorkoutPlanDao planDao, UserDao userDao, NotificationQueueService notificationQueue) {
        this.planDao = planDao;
        this.userDao = userDao;
        this.notificationQueue = notificationQueue;
    }

    public WorkoutPlan createPlan(WorkoutPlan plan) throws InvalidWorkoutPlanException {
        validatePlan(plan);
        return planDao.create(plan);
    }

    private void validatePlan(WorkoutPlan plan) throws InvalidWorkoutPlanException {
        if (plan == null) {
            throw new InvalidWorkoutPlanException("Workout plan cannot be null.");
        }
        if (plan.getTitle() == null || plan.getTitle().trim().length() < 3) {
            throw new InvalidWorkoutPlanException("Plan title must be at least 3 characters long.");
        }
        if (plan.getDurationWeeks() <= 0 || plan.getDurationWeeks() > 52) {
            throw new InvalidWorkoutPlanException("Duration must be between 1 and 52 weeks.");
        }
        if (plan.getEstimatedDurationMinutes() <= 0) {
            throw new InvalidWorkoutPlanException("Estimated session duration must be greater than zero.");
        }

        // Subclass validation
        if (plan instanceof StrengthWorkoutPlan strength) {
            if (strength.getRecommendedSets() <= 0 || strength.getRecommendedReps() <= 0) {
                throw new InvalidWorkoutPlanException("Strength plans require positive sets and reps.");
            }
        } else if (plan instanceof CardioWorkoutPlan cardio) {
            if (cardio.getDistanceKm() <= 0) {
                throw new InvalidWorkoutPlanException("Cardio plans require a distance greater than 0 km.");
            }
        } else if (plan instanceof HIITWorkoutPlan hiit) {
            if (hiit.getWorkIntervalSec() <= 0 || hiit.getNumberOfRounds() <= 0) {
                throw new InvalidWorkoutPlanException("HIIT plans require positive work interval and rounds.");
            }
        }
    }

    public Optional<WorkoutPlan> getPlanById(int id) {
        return planDao.findById(id);
    }

    public List<WorkoutPlan> getAllActivePlans() {
        return planDao.findAllActive();
    }

    public List<WorkoutPlan> getPlansByCoach(int coachId) {
        return planDao.findByCoachId(coachId);
    }

    public List<WorkoutPlan> getEnrolledPlans(int clientId) {
        return planDao.findEnrolledPlansByClientId(clientId);
    }

    public boolean isClientEnrolled(int clientId, int planId) {
        return planDao.isClientEnrolled(clientId, planId);
    }

    public void enrollClient(int clientId, int planId) throws UserNotFoundException {
        User user = userDao.findById(clientId)
                .orElseThrow(() -> new UserNotFoundException(clientId));
        Optional<WorkoutPlan> optPlan = planDao.findById(planId);

        planDao.enrollClient(clientId, planId);

        if (optPlan.isPresent()) {
            WorkoutPlan plan = optPlan.get();
            notificationQueue.enqueue(new NotificationTask(
                    user.getEmail(), user.getName(),
                    "Enrolled in: " + plan.getTitle(),
                    "You have successfully enrolled in " + plan.getTitle() + " (" + plan.getPlanType() + "). Ready to crush your goals!",
                    "IN_APP"
            ));
        }
    }

    public void unenrollClient(int clientId, int planId) {
        planDao.unenrollClient(clientId, planId);
    }

    /**
     * Polymorphic calorie estimation:
     * Dispatches calculateCaloriesBurned() dynamically depending on whether plan is
     * StrengthWorkoutPlan, CardioWorkoutPlan, or HIITWorkoutPlan!
     */
    public double estimateCaloriesForClient(int planId, int clientId, int durationMinutes) {
        Optional<WorkoutPlan> optPlan = planDao.findById(planId);
        if (optPlan.isEmpty()) return 0.0;

        WorkoutPlan plan = optPlan.get();
        double weightKg = 70.0; // default
        Optional<User> optUser = userDao.findById(clientId);
        if (optUser.isPresent() && optUser.get() instanceof ClientUser client) {
            if (client.getCurrentWeightKg() > 0) {
                weightKg = client.getCurrentWeightKg();
            }
        }

        // Polymorphic invocation!
        return plan.calculateCaloriesBurned(durationMinutes, weightKg);
    }

    // ==========================================
    // Method Overloading for Plan Search & Filter
    // ==========================================

    /**
     * Overloaded method 1: filter by difficulty only.
     */
    public List<WorkoutPlan> filterPlans(String difficulty) {
        return filterPlans(null, difficulty, null);
    }

    /**
     * Overloaded method 2: filter by difficulty and plan type.
     */
    public List<WorkoutPlan> filterPlans(String difficulty, PlanType planType) {
        return filterPlans(null, difficulty, planType);
    }

    /**
     * Overloaded method 3: filter by keyword, difficulty, and plan type.
     * Uses Generics and CollectionUtils.filter().
     */
    public List<WorkoutPlan> filterPlans(String keyword, String difficulty, PlanType planType) {
        List<WorkoutPlan> all = planDao.findAllActive();

        return CollectionUtils.filter(all, plan -> {
            boolean match = true;

            if (keyword != null && !keyword.trim().isEmpty()) {
                String k = keyword.trim().toLowerCase();
                boolean titleMatch = plan.getTitle().toLowerCase().contains(k);
                boolean descMatch = plan.getDescription() != null && plan.getDescription().toLowerCase().contains(k);
                boolean coachMatch = plan.getCoachName() != null && plan.getCoachName().toLowerCase().contains(k);
                if (!titleMatch && !descMatch && !coachMatch) {
                    match = false;
                }
            }

            if (match && difficulty != null && !difficulty.trim().isEmpty() && !difficulty.equalsIgnoreCase("ALL")) {
                if (!plan.getDifficultyLevel().name().equalsIgnoreCase(difficulty.trim())) {
                    match = false;
                }
            }

            if (match && planType != null) {
                if (plan.getPlanType() != planType) {
                    match = false;
                }
            }

            return match;
        });
    }
}
