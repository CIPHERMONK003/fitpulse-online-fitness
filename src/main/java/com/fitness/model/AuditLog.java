package com.fitness.model;

import java.sql.Timestamp;

/**
 * Model representing audit events for administrators: user updates, moderation actions, security events.
 */
public class AuditLog implements Identifiable<Integer> {
    private Integer id;
    private Integer adminId;
    private String adminEmail;
    private String actionType; // USER_DEACTIVATE, PLAN_DELETE, SETTING_UPDATE, DB_MAINTENANCE
    private String details;
    private String ipAddress;
    private Timestamp timestamp;

    public AuditLog() {
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public AuditLog(Integer id, Integer adminId, String adminEmail, String actionType,
                    String details, String ipAddress) {
        this.id = id;
        this.adminId = adminId;
        this.adminEmail = adminEmail;
        this.actionType = actionType;
        this.details = details;
        this.ipAddress = ipAddress;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("AuditLog[%s by %s: %s at %s]", actionType, adminEmail, details, timestamp);
    }
}
