package com.fitness.service;

import com.fitness.concurrency.NotificationQueueService;
import com.fitness.concurrency.PlatformMetricsManager;
import com.fitness.config.DatabaseManager;
import com.fitness.dao.AuditLogDao;
import com.fitness.dao.UserDao;
import com.fitness.dao.WorkoutPlanDao;
import com.fitness.model.AuditLog;
import com.fitness.model.User;
import com.fitness.model.WorkoutPlan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service managing administrative oversight, user moderation, and system settings.
 */
public class AdminService {

    private final UserDao userDao;
    private final WorkoutPlanDao planDao;
    private final AuditLogDao auditLogDao;
    private final PlatformMetricsManager metricsManager;
    private final NotificationQueueService notificationQueue;
    private final DatabaseManager dbManager;

    public AdminService() {
        this.userDao = new UserDao();
        this.planDao = new WorkoutPlanDao();
        this.auditLogDao = new AuditLogDao();
        this.metricsManager = PlatformMetricsManager.getInstance();
        this.notificationQueue = NotificationQueueService.getInstance();
        this.dbManager = DatabaseManager.getInstance();
    }

    public AdminService(UserDao userDao, WorkoutPlanDao planDao, AuditLogDao auditLogDao,
                        PlatformMetricsManager metricsManager, NotificationQueueService notificationQueue,
                        DatabaseManager dbManager) {
        this.userDao = userDao;
        this.planDao = planDao;
        this.auditLogDao = auditLogDao;
        this.metricsManager = metricsManager;
        this.notificationQueue = notificationQueue;
        this.dbManager = dbManager;
    }

    public boolean toggleUserStatus(int adminId, String adminEmail, int targetUserId, boolean active, String ip) {
        boolean updated = userDao.toggleActiveStatus(targetUserId, active);
        if (updated) {
            auditLogDao.create(new AuditLog(
                    null, adminId, adminEmail,
                    active ? "USER_ACTIVATE" : "USER_DEACTIVATE",
                    "User ID " + targetUserId + " active status set to " + active,
                    ip
            ));
        }
        return updated;
    }

    public boolean deleteWorkoutPlan(int adminId, String adminEmail, int planId, String ip) {
        boolean deleted = planDao.delete(planId);
        if (deleted) {
            auditLogDao.create(new AuditLog(
                    null, adminId, adminEmail,
                    "PLAN_DELETE",
                    "Deleted workout plan with ID " + planId,
                    ip
            ));
        }
        return deleted;
    }

    public List<AuditLog> getRecentAuditLogs(int limit) {
        return auditLogDao.findRecent(limit);
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public List<WorkoutPlan> getAllPlans() {
        return planDao.findAll();
    }

    public Map<String, Object> getSystemDiagnostics() {
        Map<String, Object> diag = new HashMap<>();
        diag.put("activeSessions", metricsManager.getActiveUserSessions());
        diag.put("totalRequests", metricsManager.getTotalRequestsHandled());
        diag.put("totalWorkoutsLogged", metricsManager.getTotalWorkoutsLogged());
        diag.put("cumulativeCalories", metricsManager.getCumulativeCaloriesBurned());
        diag.put("notificationQueuePending", notificationQueue.getQueueSize());
        diag.put("notificationsProcessed", notificationQueue.getProcessedCount());
        diag.put("dbTables", dbManager.getDatabaseMetadataTables());
        diag.put("endpointHits", metricsManager.getEndpointHits());
        return diag;
    }
}
