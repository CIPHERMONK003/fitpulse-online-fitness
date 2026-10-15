package com.fitness.concurrency;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background Scheduled Executor for periodic maintenance and health checks.
 * Demonstrates Java ScheduledExecutorService.
 */
public class MaintenanceScheduler {

    private static volatile MaintenanceScheduler instance;
    private final ScheduledExecutorService scheduler;
    private boolean running;

    private MaintenanceScheduler() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Fitness-Maintenance-Scheduler");
            t.setDaemon(true);
            return t;
        });
        this.running = false;
    }

    public static MaintenanceScheduler getInstance() {
        if (instance == null) {
            synchronized (MaintenanceScheduler.class) {
                if (instance == null) {
                    instance = new MaintenanceScheduler();
                }
            }
        }
        return instance;
    }

    public synchronized void start() {
        if (running) return;
        running = true;

        // Run background health audit every 2 minutes
        scheduler.scheduleAtFixedRate(() -> {
            try {
                PlatformMetricsManager metrics = PlatformMetricsManager.getInstance();
                NotificationQueueService queue = NotificationQueueService.getInstance();
                System.out.printf("[Maintenance Scheduler] Heartbeat - Active Sessions: %d | Workouts: %d | Queue Pending: %d | Total Cal: %.0f%n",
                        metrics.getActiveUserSessions(), metrics.getTotalWorkoutsLogged(),
                        queue.getQueueSize(), metrics.getCumulativeCaloriesBurned());
            } catch (Exception e) {
                System.err.println("[Maintenance Scheduler] Heartbeat error: " + e.getMessage());
            }
        }, 1, 2, TimeUnit.MINUTES);

        System.out.println("[Multithreading] Maintenance Scheduler started.");
    }

    public synchronized void shutdown() {
        if (!running) return;
        running = false;
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("[Multithreading] Maintenance Scheduler stopped.");
    }
}
