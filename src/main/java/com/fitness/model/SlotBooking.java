package com.fitness.model;

import java.sql.Timestamp;

/**
 * Model representing a confirmed or cancelled booking for a SessionSlot.
 * Demonstrates:
 * - OOP Encapsulation
 * - Identifiable<Integer> interface
 */
public class SlotBooking implements Identifiable<Integer> {
    private Integer id;
    private int slotId;
    private int clientId;
    private String clientName;
    private String clientEmail;
    private String sessionTitle;
    private String sessionType;
    private String coachName;
    private String sessionDate;
    private String startTime;
    private String roomLocation;
    private String status; // "CONFIRMED", "CANCELLED", "COMPLETED"
    private String notes;
    private Timestamp bookedAt;

    public SlotBooking() {
        this.status = "CONFIRMED";
        this.bookedAt = new Timestamp(System.currentTimeMillis());
    }

    public SlotBooking(Integer id, int slotId, int clientId, String clientName, String clientEmail,
                       String sessionTitle, String sessionType, String coachName, String sessionDate,
                       String startTime, String roomLocation, String status, String notes) {
        this.id = id;
        this.slotId = slotId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientEmail = clientEmail;
        this.sessionTitle = sessionTitle;
        this.sessionType = sessionType;
        this.coachName = coachName;
        this.sessionDate = sessionDate;
        this.startTime = startTime;
        this.roomLocation = roomLocation;
        this.status = status;
        this.notes = notes;
        this.bookedAt = new Timestamp(System.currentTimeMillis());
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getSessionTitle() {
        return sessionTitle;
    }

    public void setSessionTitle(String sessionTitle) {
        this.sessionTitle = sessionTitle;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
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

    public String getRoomLocation() {
        return roomLocation;
    }

    public void setRoomLocation(String roomLocation) {
        this.roomLocation = roomLocation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Timestamp getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(Timestamp bookedAt) {
        this.bookedAt = bookedAt;
    }
}
