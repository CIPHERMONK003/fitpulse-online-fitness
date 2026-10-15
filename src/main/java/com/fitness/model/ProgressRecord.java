package com.fitness.model;

import java.sql.Timestamp;

/**
 * Model representing a single workout / progress log entry for a client.
 * Tracks date, weight, workout duration, calories burned, notes, and plan reference.
 */
public class ProgressRecord implements Identifiable<Integer> {
    private Integer id;
    private Integer clientId;
    private Integer workoutPlanId;
    private String workoutPlanTitle;
    private double currentWeightKg;
    private int durationMinutes;
    private double caloriesBurned;
    private String notes;
    private Timestamp recordedAt;

    public ProgressRecord() {
        this.recordedAt = new Timestamp(System.currentTimeMillis());
    }

    public ProgressRecord(Integer id, Integer clientId, Integer workoutPlanId,
                          String workoutPlanTitle, double currentWeightKg,
                          int durationMinutes, double caloriesBurned, String notes) {
        this.id = id;
        this.clientId = clientId;
        this.workoutPlanId = workoutPlanId;
        this.workoutPlanTitle = workoutPlanTitle;
        this.currentWeightKg = currentWeightKg;
        this.durationMinutes = durationMinutes;
        this.caloriesBurned = caloriesBurned;
        this.notes = notes;
        this.recordedAt = new Timestamp(System.currentTimeMillis());
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public Integer getWorkoutPlanId() {
        return workoutPlanId;
    }

    public void setWorkoutPlanId(Integer workoutPlanId) {
        this.workoutPlanId = workoutPlanId;
    }

    public String getWorkoutPlanTitle() {
        return workoutPlanTitle;
    }

    public void setWorkoutPlanTitle(String workoutPlanTitle) {
        this.workoutPlanTitle = workoutPlanTitle;
    }

    public double getCurrentWeightKg() {
        return currentWeightKg;
    }

    public void setCurrentWeightKg(double currentWeightKg) {
        this.currentWeightKg = currentWeightKg;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public double getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(double caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Timestamp getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Timestamp recordedAt) {
        this.recordedAt = recordedAt;
    }

    @Override
    public String toString() {
        return String.format("ProgressRecord[id=%d, client=%d, weight=%.1f, duration=%d min, calories=%.0f, date=%s]",
                id, clientId, currentWeightKg, durationMinutes, caloriesBurned, recordedAt);
    }
}
