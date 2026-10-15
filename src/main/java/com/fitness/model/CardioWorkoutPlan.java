package com.fitness.model;

/**
 * Concrete WorkoutPlan subclass for Aerobic & Cardiovascular Training.
 * Demonstrates:
 * - Inheritance from WorkoutPlan
 * - Polymorphic implementation of calculateCaloriesBurned()
 * - Specialized fields: targetHeartRateZone, distanceKm, targetPaceMinPerKm
 */
public class CardioWorkoutPlan extends WorkoutPlan {
    private String cardioType; // Running, Cycling, Rowing, Swimming
    private String targetHeartRateZone; // Zone 2 (Aerobic), Zone 3 (Tempo), Zone 4 (Threshold)
    private double distanceKm;
    private double targetPaceMinPerKm;

    public CardioWorkoutPlan() {
        super();
        setPlanType(PlanType.CARDIO);
    }

    public CardioWorkoutPlan(Integer id, Integer coachId, String coachName, String title,
                             String description, DifficultyLevel difficultyLevel, int durationWeeks,
                             int estimatedDurationMinutes, double baseCaloriesBurned,
                             String cardioType, String targetHeartRateZone, double distanceKm,
                             double targetPaceMinPerKm) {
        super(id, coachId, coachName, title, description, PlanType.CARDIO, difficultyLevel,
                durationWeeks, estimatedDurationMinutes, baseCaloriesBurned);
        this.cardioType = cardioType;
        this.targetHeartRateZone = targetHeartRateZone;
        this.distanceKm = distanceKm;
        this.targetPaceMinPerKm = targetPaceMinPerKm;
    }

    public String getCardioType() {
        return cardioType;
    }

    public void setCardioType(String cardioType) {
        this.cardioType = cardioType;
    }

    public String getTargetHeartRateZone() {
        return targetHeartRateZone;
    }

    public void setTargetHeartRateZone(String targetHeartRateZone) {
        this.targetHeartRateZone = targetHeartRateZone;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public double getTargetPaceMinPerKm() {
        return targetPaceMinPerKm;
    }

    public void setTargetPaceMinPerKm(double targetPaceMinPerKm) {
        this.targetPaceMinPerKm = targetPaceMinPerKm;
    }

    /**
     * Polymorphic calorie calculation tailored for Cardio / Aerobic workouts:
     * High oxygen demand and continuous aerobic work.
     */
    @Override
    public double calculateCaloriesBurned(int durationMinutes, double userWeightKg) {
        if (durationMinutes <= 0 || userWeightKg <= 0) return 0.0;
        // Base MET for moderate cardio ~7.5 to 10.0
        double baseMet = 8.0 * getDifficultyLevel().getIntensityMultiplier();
        if (targetHeartRateZone != null && targetHeartRateZone.contains("4")) {
            baseMet += 2.0;
        }
        return Math.round((baseMet * 3.5 * userWeightKg / 200.0) * durationMinutes);
    }

    @Override
    public String generateWorkoutSummary() {
        return String.format("Cardio Plan: %s | Distance: %.1f km | HR Zone: %s (Pace: %.1f min/km)",
                cardioType, distanceKm, targetHeartRateZone, targetPaceMinPerKm);
    }

    @Override
    public String getSpecificAttributeLabel() {
        return "Cardio Target & Zone";
    }

    @Override
    public String getSpecificAttributeValue() {
        return String.format("%s (%.1f km @ %.1f min/km, HR %s)", cardioType, distanceKm, targetPaceMinPerKm, targetHeartRateZone);
    }
}
