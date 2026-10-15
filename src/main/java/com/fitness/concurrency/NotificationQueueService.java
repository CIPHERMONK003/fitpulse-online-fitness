package com.fitness.concurrency;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Producer-Consumer Multithreaded Notification Queue.
 * Demonstrates:
 * - Multithreading & Synchronization (Thread creation, BlockingQueue, worker thread lifecycle)
 * - Safe concurrent state management
 * - Asynchronous background notification delivery
 */
public class NotificationQueueService {

    private static volatile NotificationQueueService instance;

    private final BlockingQueue<NotificationTask> queue;
    private final List<NotificationTask> recentProcessedTasks;
    private final AtomicInteger processedCount;
    private final AtomicBoolean running;
    private Thread workerThread;

    private NotificationQueueService() {
        this.queue = new LinkedBlockingQueue<>(500);
        this.recentProcessedTasks = new CopyOnWriteArrayList<>();
        this.processedCount = new AtomicInteger(0);
        this.running = new AtomicBoolean(false);
    }

    public static NotificationQueueService getInstance() {
        if (instance == null) {
            synchronized (NotificationQueueService.class) {
                if (instance == null) {
                    instance = new NotificationQueueService();
                }
            }
        }
        return instance;
    }

    /**
     * Starts the background worker thread.
     */
    public synchronized void start() {
        if (running.get()) return;
        running.set(true);

        workerThread = new Thread(new NotificationWorker(), "Notification-Worker-Thread");
        workerThread.setDaemon(true);
        workerThread.start();
        System.out.println("[Multithreading] Notification Worker Thread started successfully.");
    }

    /**
     * Gracefully stops the worker thread.
     */
    public synchronized void shutdown() {
        if (!running.get()) return;
        running.set(false);
        if (workerThread != null) {
            workerThread.interrupt();
        }
        System.out.println("[Multithreading] Notification Worker Thread stopped.");
    }

    /**
     * Producer method: queues a new notification asynchronously.
     * Non-blocking or bounded offer.
     */
    public boolean enqueue(NotificationTask task) {
        if (task == null) return false;
        boolean added = queue.offer(task);
        if (added) {
            System.out.printf("[Multithreading] Enqueued notification for: %s (Pending in queue: %d)%n",
                    task.getRecipient(), queue.size());
        }
        return added;
    }

    public int getQueueSize() {
        return queue.size();
    }

    public int getProcessedCount() {
        return processedCount.get();
    }

    public List<NotificationTask> getRecentProcessedTasks() {
        return Collections.unmodifiableList(recentProcessedTasks);
    }

    public boolean isRunning() {
        return running.get();
    }

    /**
     * Consumer Worker Runnable.
     */
    private class NotificationWorker implements Runnable {
        @Override
        public void run() {
            while (running.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    // Blocking take with timeout so worker can check running status periodically
                    NotificationTask task = queue.poll(1, TimeUnit.SECONDS);
                    if (task != null) {
                        processNotification(task);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.printf("[Multithreading] Error processing notification: %s%n", e.getMessage());
                }
            }
        }

        private void processNotification(NotificationTask task) {
            try {
                // Simulate network latency for dispatching email or push message
                Thread.sleep(150);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            processedCount.incrementAndGet();

            // Maintain rolling buffer of last 50 processed notifications
            if (recentProcessedTasks.size() >= 50) {
                recentProcessedTasks.remove(0);
            }
            recentProcessedTasks.add(task);

            System.out.printf("[Multithreading Worker] DISPATCHED: %s -> %s%n",
                    task.getRecipient(), task.getSubject());
        }
    }
}
