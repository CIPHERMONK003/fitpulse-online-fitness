package com.fitness.model;

import java.sql.Timestamp;

/**
 * Abstract Base Class for User hierarchy.
 * Demonstrates:
 * - Abstraction
 * - Encapsulation
 * - Inheritance (base class for ClientUser, CoachUser, AdminUser)
 * - Polymorphism (abstract method getDashboardUrl, polymorphic string representations)
 * - Interfaces (implements Identifiable<Integer>, Notifiable)
 */
public abstract class User implements Identifiable<Integer>, Notifiable {
    private Integer id;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private Timestamp createdAt;
    private boolean active;

    public User() {
        this.active = true;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public User(Integer id, String name, String email, String passwordHash, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = true;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Polymorphic method: each role subclass returns its specialized portal/dashboard URL.
     */
    public abstract String getDashboardUrl();

    /**
     * Polymorphic method: returns a role-specific profile summary.
     */
    public abstract String getProfileSummary();

    @Override
    public void onNotificationReceived(String subject, String message) {
        System.out.printf("[Notification Dispatcher] Sent to %s (%s): %s - %s%n",
                name, email, subject, message);
    }

    @Override
    public String getNotificationTarget() {
        return email;
    }

    @Override
    public String toString() {
        return String.format("User[id=%d, name='%s', email='%s', role=%s, active=%b]",
                id, name, email, role, active);
    }
}
