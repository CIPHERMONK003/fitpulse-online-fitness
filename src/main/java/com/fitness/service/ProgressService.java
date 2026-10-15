package com.fitness.service;

import com.fitness.concurrency.NotificationQueueService;
import com.fitness.concurrency.NotificationTask;
import com.fitness.concurrency.PlatformMetricsManager;
import com.fitness.dao.ProgressRecordDao;
import com.fitness.dao.UserDao;
import com.fitness.dao.WorkoutPlanDao;
import com.fitness.model.ClientUser;
import com.fitness.model.ProgressRecord;
import com.fitness.model.User;
import com.fitness.model.WorkoutPlan;
import com.fitness.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service managing client progress tracking and workout analytics.
 * Demonstrates:
 * - OOP: Method Overloading (logProgress methods)
 * - Collections & Generics (Stream groupingBy, custom Comparators, generic maps)
 * - Multithreading & Synchronization: PlatformMetricsManager record, async notification queue
 */
public class ProgressService {

    private final ProgressRecordDao progressDao;
    private final UserDao userDao;
    private final WorkoutPlanDao planDao;
    private final PlatformMetricsManager metricsManager;
    private final NotificationQueueService notificationQueue;

    public ProgressService() {
        this.progressDao = new ProgressRecordDao();
        this.userDao = new UserDao();
        this.planDao = new WorkoutPlanDao();
        this.metricsManager = PlatformMetricsManager.getInstance();
        this.notificationQueue = NotificationQueueService.getInstance();
    }

    public ProgressService(ProgressRecordDao progressDao, UserDao userDao, WorkoutPlanDao planDao,
                           PlatformMetricsManager metricsManager, NotificationQueueService notificationQueue) {
        this.progressDao = progressDao;
        this.userDao = userDao;
        this.planDao = planDao;
        this.metricsManager = metricsManager;
        this.notificationQueue = notificationQueue;
    }

    // ==========================================
    // Method Overloading for Logging Progress
    // ==========================================

    /**
     * Overloaded method 1: Quick log with weight and calories only.
     */
    public ProgressRecord logProgress(int clientId, double currentWeightKg, double caloriesBurned) {
        return logProgress(clientId, currentWeightKg, caloriesBurned, null, 30, "Quick progress log");
    }

    /**
     * Overloaded method 2: Detailed workout progress log.
     */
    public ProgressRecord logProgress(int clientId, double currentWeightKg, double caloriesBurned,
                                     Integer workoutPlanId, int durationMinutes, String notes) {

        String planTitle = null;
        if (workoutPlanId != null) {
            Optional<WorkoutPlan> optPlan = planDao.findById(workoutPlanId);
            if (optPlan.isPresent()) {
                planTitle = optPlan.get().getTitle();
            }
        }

        ProgressRecord record = new ProgressRecord(
                null, clientId, workoutPlanId, planTitle,
                currentWeightKg, durationMinutes, caloriesBurned, notes
        );

        ProgressRecord saved = progressDao.create(record);

        // Update client's current weight in profile
        userDao.updateClientWeight(clientId, currentWeightKg);

        // Multithreaded thread-safe metric update (synchronization with ReentrantReadWriteLock)
        metricsManager.recordWorkoutLogged(caloriesBurned);

        // Asynchronous notification to assigned coach
        notifyAssignedCoach(clientId, saved);

        return saved;
    }

    private void notifyAssignedCoach(int clientId, ProgressRecord record) {
        Optional<User> optClient = userDao.findById(clientId);
        if (optClient.isPresent() && optClient.get() instanceof ClientUser client) {
            if (client.getAssignedCoachId() != null) {
                Optional<User> optCoach = userDao.findById(client.getAssignedCoachId());
                if (optCoach.isPresent()) {
                    User coach = optCoach.get();
                    notificationQueue.enqueue(new NotificationTask(
                            coach.getEmail(), coach.getName(),
                            "Client Logged Workout: " + client.getName(),
                            String.format("Client %s logged a workout (%d mins, %.0f kcal burned). Current weight: %.1f kg.",
                                    client.getName(), record.getDurationMinutes(), record.getCaloriesBurned(), record.getCurrentWeightKg()),
                            "IN_APP"
                    ));
                }
            }
        }
    }

    public List<ProgressRecord> getClientHistory(int clientId) {
        return progressDao.findByClientId(clientId);
    }

    /**
     * Demonstrates Collections & Generics with custom Comparator:
     * Sorts client progress records by calories burned descending.
     */
    public List<ProgressRecord> getTopCalorieWorkouts(int clientId, int limit) {
        List<ProgressRecord> records = progressDao.findByClientId(clientId);

        Comparator<ProgressRecord> byCaloriesDesc = (r1, r2) ->
                Double.compare(r2.getCaloriesBurned(), r1.getCaloriesBurned());

        return CollectionUtils.sorted(records, byCaloriesDesc).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Demonstrates Java Collections Stream grouping and summarization:
     * Groups workout records by workout plan and calculates total calories per plan.
     */
    public Map<String, Double> getCalorieBurnByPlan(int clientId) {
        List<ProgressRecord> records = progressDao.findByClientId(clientId);

        return records.stream()
                .filter(r -> r.getWorkoutPlanTitle() != null && !r.getWorkoutPlanTitle().isEmpty())
                .collect(Collectors.groupingBy(
                        ProgressRecord::getWorkoutPlanTitle,
                        Collectors.summingDouble(ProgressRecord::getCaloriesBurned)
                ));
    }

    public double getTotalCaloriesBurned(int clientId) {
        return progressDao.calculateTotalCaloriesBurned(clientId);
    }

    public int getWorkoutCount(int clientId) {
        return progressDao.countWorkoutsByClientId(clientId);
    }

    /**
     * Computes weight change statistics (starting weight, latest weight, diff).
     */
    public Map<String, Double> getWeightTrendSummary(int clientId) {
        List<ProgressRecord> records = progressDao.findByClientId(clientId);
        Map<String, Double> summary = new HashMap<>();

        if (records.isEmpty()) {
            Optional<User> optUser = userDao.findById(clientId);
            if (optUser.isPresent() && optUser.get() instanceof ClientUser client) {
                summary.put("initialWeight", client.getCurrentWeightKg());
                summary.put("latestWeight", client.getCurrentWeightKg());
                summary.put("weightDiff", 0.0);
            }
            return summary;
        }

        // Sorted chronologically
        List<ProgressRecord> chronological = records.stream()
                .sorted(Comparator.comparing(ProgressRecord::getRecordedAt))
                .collect(Collectors.toList());

        double initialWeight = chronological.get(0).getCurrentWeightKg();
        double latestWeight = chronological.get(chronological.size() - 1).getCurrentWeightKg();

        summary.put("initialWeight", initialWeight);
        summary.put("latestWeight", latestWeight);
        summary.put("weightDiff", latestWeight - initialWeight);
        return summary;
    }
}
