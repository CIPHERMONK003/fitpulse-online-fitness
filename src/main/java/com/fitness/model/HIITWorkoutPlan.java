package com.fitness.model;

/**
 * Concrete WorkoutPlan subclass for High Intensity Interval Training (HIIT).
 * Demonstrates:
 * - Inheritance from WorkoutPlan
 * - Polymorphic implementation of calculateCaloriesBurned()
 * - Specialized fields: workIntervalSec, restIntervalSec, numberOfRounds, intensityRating
 */
public class HIITWorkoutPlan extends WorkoutPlan {
    private int workIntervalSec;
    private int restIntervalSec;
    private int numberOfRounds;
    private int intensityRating; // 1-10 Scale

    public HIITWorkoutPlan() {
        super();
        setPlanType(PlanType.HIIT);
    }

    public HIITWorkoutPlan(Integer id, Integer coachId, String coachName, String title,
                           String description, DifficultyLevel difficultyLevel, int durationWeeks,
                           int estimatedDurationMinutes, double baseCaloriesBurned,
                           int workIntervalSec, int restIntervalSec, int numberOfRounds,
                           int intensityRating) {
        super(id, coachId, coachName, title, description, PlanType.HIIT, difficultyLevel,
                durationWeeks, estimatedDurationMinutes, baseCaloriesBurned);
        this.workIntervalSec = workIntervalSec;
        this.restIntervalSec = restIntervalSec;
        this.numberOfRounds = numberOfRounds;
        this.intensityRating = intensityRating;
    }

    public int getWorkIntervalSec() {
        return workIntervalSec;
    }

    public void setWorkIntervalSec(int workIntervalSec) {
        this.workIntervalSec = workIntervalSec;
    }

    public int getRestIntervalSec() {
        return restIntervalSec;
    }

    public void setRestIntervalSec(int restIntervalSec) {
        this.restIntervalSec = restIntervalSec;
    }

    public int getNumberOfRounds() {
        return numberOfRounds;
    }

    public void setNumberOfRounds(int numberOfRounds) {
        this.numberOfRounds = numberOfRounds;
    }

    public int getIntensityRating() {
        return intensityRating;
    }

    public void setIntensityRating(int intensityRating) {
        this.intensityRating = intensityRating;
    }

    /**
     * Polymorphic calorie calculation tailored for HIIT:
     * Combines anaerobic interval burn with EPOC (Excess Post-Exercise Oxygen Consumption).
     */
    @Override
    public double calculateCaloriesBurned(int durationMinutes, double userWeightKg) {
        if (durationMinutes <= 0 || userWeightKg <= 0) return 0.0;
        // HIIT has highest MET + EPOC afterburn factor
        double baseMet = 11.0 * getDifficultyLevel().getIntensityMultiplier();
        double dutyCycle = (double) workIntervalSec / (workIntervalSec + Math.max(1, restIntervalSec));
        double epocBonus = 1.20; // 20% post-workout calorie burn bonus
        return Math.round((baseMet * dutyCycle * 3.5 * userWeightKg / 200.0) * durationMinutes * epocBonus);
    }

    @Override
    public String generateWorkoutSummary() {
        return String.format("HIIT Plan: %d Rounds | Work %ds / Rest %ds (Intensity: %d/10)",
                numberOfRounds, workIntervalSec, restIntervalSec, intensityRating);
    }

    @Override
    public String getSpecificAttributeLabel() {
        return "HIIT Interval & Rounds";
    }

    @Override
    public String getSpecificAttributeValue() {
        return String.format("%d rounds (%ds work / %ds rest, intensity %d/10)", numberOfRounds, workIntervalSec, restIntervalSec, intensityRating);
    }
}
