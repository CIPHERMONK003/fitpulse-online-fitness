package com.fitness.model;

/**
 * Interface for entities or services capable of receiving notifications.
 */
public interface Notifiable {
    void onNotificationReceived(String subject, String message);
    String getNotificationTarget();
}
