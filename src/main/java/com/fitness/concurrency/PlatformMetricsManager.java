package com.fitness.concurrency;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe Platform Metrics Manager.
 * Demonstrates:
 * - Multithreading & Synchronization
 * - Synchronized blocks & methods
 * - ReentrantReadWriteLock for concurrent access
 * - AtomicInteger & AtomicLong
 * - ConcurrentHashMap
 */
public class PlatformMetricsManager {

    private static volatile PlatformMetricsManager instance;

    private final AtomicInteger activeUserSessions;
    private final AtomicLong totalRequestsHandled;
    private final AtomicInteger totalWorkoutsLogged;
    private final ConcurrentHashMap<String, AtomicInteger> endpointHits;

    private double cumulativeCaloriesBurned;
    private final ReentrantReadWriteLock calorieLock;

    private PlatformMetricsManager() {
        this.activeUserSessions = new AtomicInteger(0);
        this.totalRequestsHandled = new AtomicLong(0);
        this.totalWorkoutsLogged = new AtomicInteger(0);
        this.endpointHits = new ConcurrentHashMap<>();
        this.cumulativeCaloriesBurned = 0.0;
        this.calorieLock = new ReentrantReadWriteLock();
    }

    public static PlatformMetricsManager getInstance() {
        if (instance == null) {
            synchronized (PlatformMetricsManager.class) {
                if (instance == null) {
                    instance = new PlatformMetricsManager();
                }
            }
        }
        return instance;
    }

    public void incrementActiveSessions() {
        activeUserSessions.incrementAndGet();
    }

    public void decrementActiveSessions() {
        activeUserSessions.updateAndGet(val -> Math.max(0, val - 1));
    }

    public void recordRequest(String uri) {
        totalRequestsHandled.incrementAndGet();
        if (uri != null) {
            endpointHits.computeIfAbsent(uri, k -> new AtomicInteger(0)).incrementAndGet();
        }
    }

    public void recordWorkoutLogged(double calories) {
        totalWorkoutsLogged.incrementAndGet();

        // Write lock for thread-safe update of double precision floating-point metric
        calorieLock.writeLock().lock();
        try {
            this.cumulativeCaloriesBurned += calories;
        } finally {
            calorieLock.writeLock().unlock();
        }
    }

    public double getCumulativeCaloriesBurned() {
        // Read lock allowing concurrent thread reads without blocking each other
        calorieLock.readLock().lock();
        try {
            return this.cumulativeCaloriesBurned;
        } finally {
            calorieLock.readLock().unlock();
        }
    }

    public int getActiveUserSessions() {
        return activeUserSessions.get();
    }

    public long getTotalRequestsHandled() {
        return totalRequestsHandled.get();
    }

    public int getTotalWorkoutsLogged() {
        return totalWorkoutsLogged.get();
    }

    public Map<String, AtomicInteger> getEndpointHits() {
        return Collections.unmodifiableMap(endpointHits);
    }

    /**
     * Synchronized method: resets daily statistics safely.
     */
    public synchronized void resetDailyMetrics() {
        calorieLock.writeLock().lock();
        try {
            this.cumulativeCaloriesBurned = 0.0;
            this.totalWorkoutsLogged.set(0);
            System.out.println("[Synchronization] Daily platform metrics reset synchronized successfully.");
        } finally {
            calorieLock.writeLock().unlock();
        }
    }
}
