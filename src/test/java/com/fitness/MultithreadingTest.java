package com.fitness;

import com.fitness.concurrency.NotificationQueueService;
import com.fitness.concurrency.NotificationTask;
import com.fitness.concurrency.PlatformMetricsManager;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Unit Tests for Multithreading & Synchronization (4 Marks):
 * - Producer-Consumer Notification Queue
 * - Thread-safe Concurrent Platform Metrics Manager
 * - ReentrantReadWriteLock & Atomic variables under concurrent load
 */
public class MultithreadingTest {

    @Test
    public void testConcurrentMetricsManagerSynchronization() throws InterruptedException {
        PlatformMetricsManager manager = PlatformMetricsManager.getInstance();
        manager.resetDailyMetrics();

        int threadCount = 20;
        int operationsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        manager.recordRequest("/dashboard");
                        manager.incrementActiveSessions();
                        manager.recordWorkoutLogged(10.0);
                        manager.decrementActiveSessions();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        Assert.assertTrue("All concurrent worker threads should complete", completed);

        int expectedWorkouts = threadCount * operationsPerThread;
        double expectedCalories = expectedWorkouts * 10.0;

        Assert.assertEquals(expectedWorkouts, manager.getTotalWorkoutsLogged());
        Assert.assertEquals(expectedCalories, manager.getCumulativeCaloriesBurned(), 0.01);
    }

    @Test
    public void testNotificationQueueProducerConsumer() throws InterruptedException {
        NotificationQueueService queueService = NotificationQueueService.getInstance();
        queueService.start();

        int initialProcessed = queueService.getProcessedCount();

        // Producer: Enqueue 3 tasks
        queueService.enqueue(new NotificationTask("test1@fit.com", "User 1", "Goal reached", "Congrats", "IN_APP"));
        queueService.enqueue(new NotificationTask("test2@fit.com", "User 2", "Plan assigned", "New plan", "EMAIL_SIMULATION"));
        queueService.enqueue(new NotificationTask("test3@fit.com", "User 3", "Workout logged", "Nice burn", "PUSH"));

        // Wait brief moment for background consumer thread to process tasks
        Thread.sleep(800);

        int processedNow = queueService.getProcessedCount();
        Assert.assertTrue("Background worker should have processed tasks", processedNow > initialProcessed);
    }
}
