package com.fitness.model;

import java.sql.Timestamp;

/**
 * Model representing a bookable session slot.
 * Demonstrates:
 * - OOP Encapsulation
 * - Identifiable<Integer> interface implementation
 * - Dynamic slot availability & capacity tracking
 */
public class SessionSlot implements Identifiable<Integer> {
    private Integer id;
    private String sessionType; // "SPA", "COACHING", "GROUP_CLASS", "YOGA"
    private String title;
    private Integer coachOrTherapistId;
    private String coachName;
    private String sessionDate;   // "YYYY-MM-DD"
    private String startTime;     // "10:00 AM"
    private String endTime;       // "11:00 AM"
    private int maxCapacity;      // 1 for private, >1 for group
    private int bookedCount;
    private String roomLocation;  // "Zen Suite A", "Hydro Room 1"
    private int priceCredits;
    private boolean active;
    private Timestamp createdAt;

    public SessionSlot() {
        this.active = true;
        this.bookedCount = 0;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public SessionSlot(Integer id, String sessionType, String title, Integer coachOrTherapistId,
                       String coachName, String sessionDate, String startTime, String endTime,
                       int maxCapacity, int bookedCount, String roomLocation, int priceCredits) {
        this.id = id;
        this.sessionType = sessionType;
        this.title = title;
        this.coachOrTherapistId = coachOrTherapistId;
        this.coachName = coachName;
        this.sessionDate = sessionDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxCapacity = maxCapacity;
        this.bookedCount = bookedCount;
        this.roomLocation = roomLocation;
        this.priceCredits = priceCredits;
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

    public boolean isFullyBooked() {
        return bookedCount >= maxCapacity;
    }

    public int getAvailableSlots() {
        return Math.max(0, maxCapacity - bookedCount);
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getCoachOrTherapistId() {
        return coachOrTherapistId;
    }

    public void setCoachOrTherapistId(Integer coachOrTherapistId) {
        this.coachOrTherapistId = coachOrTherapistId;
    }

    public String getCoachName() {
        return coachName;
    }

    public void setCoachName(String coachName) {
        this.coachName = coachName;
    }

    public String getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(String sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public int getBookedCount() {
        return bookedCount;
    }

    public void setBookedCount(int bookedCount) {
        this.bookedCount = bookedCount;
    }

    public String getRoomLocation() {
        return roomLocation;
    }

    public void setRoomLocation(String roomLocation) {
        this.roomLocation = roomLocation;
    }

    public int getPriceCredits() {
        return priceCredits;
    }

    public void setPriceCredits(int priceCredits) {
        this.priceCredits = priceCredits;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
