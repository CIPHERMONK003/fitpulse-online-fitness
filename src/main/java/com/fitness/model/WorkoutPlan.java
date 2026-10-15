package com.fitness.model;

import java.sql.Timestamp;

/**
 * Abstract Base Class for Workout Plans.
 * Demonstrates:
 * - Abstraction & Inheritance
 * - Polymorphism via abstract calculateCaloriesBurned and generateWorkoutSummary
 * - Interface implementation (Identifiable<Integer>, WorkoutCalculable)
 */
public abstract class WorkoutPlan implements Identifiable<Integer>, WorkoutCalculable {
    private Integer id;
    private Integer coachId;
    private String coachName;
    private String title;
    private String description;
    private PlanType planType;
    private DifficultyLevel difficultyLevel;
    private int durationWeeks;
    private int estimatedDurationMinutes;
    private double baseCaloriesBurned;
    private Timestamp createdAt;
    private boolean active;

    public WorkoutPlan() {
        this.active = true;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public WorkoutPlan(Integer id, Integer coachId, String coachName, String title,
                       String description, PlanType planType, DifficultyLevel difficultyLevel,
                       int durationWeeks, int estimatedDurationMinutes, double baseCaloriesBurned) {
        this.id = id;
        this.coachId = coachId;
        this.coachName = coachName;
        this.title = title;
        this.description = description;
        this.planType = planType;
        this.difficultyLevel = difficultyLevel;
        this.durationWeeks = durationWeeks;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
        this.baseCaloriesBurned = baseCaloriesBurned;
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

    public Integer getCoachId() {
        return coachId;
    }

    public void setCoachId(Integer coachId) {
        this.coachId = coachId;
    }

    public String getCoachName() {
        return coachName;
    }

    public void setCoachName(String coachName) {
        this.coachName = coachName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PlanType getPlanType() {
        return planType;
    }

    public void setPlanType(PlanType planType) {
        this.planType = planType;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public int getDurationWeeks() {
        return durationWeeks;
    }

    public void setDurationWeeks(int durationWeeks) {
        this.durationWeeks = durationWeeks;
    }

    public int getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    public double getBaseCaloriesBurned() {
        return baseCaloriesBurned;
    }

    public void setBaseCaloriesBurned(double baseCaloriesBurned) {
        this.baseCaloriesBurned = baseCaloriesBurned;
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
     * Polymorphic method: specialized summary describing plan-specific attributes.
     */
    public abstract String generateWorkoutSummary();

    /**
     * Polymorphic method: returns specialized metric parameters for display.
     */
    public abstract String getSpecificAttributeLabel();

    public abstract String getSpecificAttributeValue();

    @Override
    public String toString() {
        return String.format("%s [Title='%s', Level=%s, Weeks=%d, Coach='%s']",
                getClass().getSimpleName(), title, difficultyLevel, durationWeeks, coachName);
    }
}
