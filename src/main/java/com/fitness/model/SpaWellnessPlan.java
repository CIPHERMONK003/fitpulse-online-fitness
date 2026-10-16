package com.fitness.model;

/**
 * Concrete WorkoutPlan subclass for Spa, Recovery, and Wellness Sessions.
 * Demonstrates:
 * - OOP Inheritance from WorkoutPlan
 * - Polymorphic implementation of calculateCaloriesBurned() & generateWorkoutSummary()
 * - Specialized wellness metrics (relaxation score, thermal hydrotherapy, aromatherapy)
 */
public class SpaWellnessPlan extends WorkoutPlan {
    private String spaCategory;
    private int relaxationScore; // 1 to 10
    private String benefits;
    private String roomType;
    private double temperatureCelsius;
    private String aromatherapyOil;

    public SpaWellnessPlan() {
        super();
        setPlanType(PlanType.SPA_WELLNESS);
    }

    public SpaWellnessPlan(Integer id, Integer coachId, String coachName, String title,
                           String description, DifficultyLevel difficultyLevel, int durationWeeks,
                           int estimatedDurationMinutes, double baseCaloriesBurned,
                           String spaCategory, int relaxationScore, String benefits,
                           String roomType, double temperatureCelsius, String aromatherapyOil) {
        super(id, coachId, coachName, title, description, PlanType.SPA_WELLNESS, difficultyLevel,
                durationWeeks, estimatedDurationMinutes, baseCaloriesBurned);
        this.spaCategory = spaCategory;
        this.relaxationScore = relaxationScore;
        this.benefits = benefits;
        this.roomType = roomType;
        this.temperatureCelsius = temperatureCelsius;
        this.aromatherapyOil = aromatherapyOil;
    }

    @Override
    public double calculateCaloriesBurned(int durationMinutes, double userWeightKg) {
        // Spa sessions focus on metabolic elevation via thermal regulation and circulation
        double durationHours = durationMinutes / 60.0;
        double thermalFactor = (temperatureCelsius > 35.0) ? 1.35 : 1.1;
        return getBaseCaloriesBurned() + (userWeightKg * 0.85 * durationHours * thermalFactor);
    }

    @Override
    public String generateWorkoutSummary() {
        return String.format("🧖‍♀️ [Spa & Wellness] %s - %s | Relaxation: %d/10 | Room: %s | Oil: %s",
                getTitle(), spaCategory, relaxationScore, roomType, aromatherapyOil != null ? aromatherapyOil : "Eucalyptus");
    }

    @Override
    public String getSpecificAttributeLabel() {
        return "Spa Category & Suite";
    }

    @Override
    public String getSpecificAttributeValue() {
        return spaCategory + " (" + roomType + ", " + relaxationScore + "/10 Relaxation)";
    }

    public String getSpaCategory() {
        return spaCategory;
    }

    public void setSpaCategory(String spaCategory) {
        this.spaCategory = spaCategory;
    }

    public int getRelaxationScore() {
        return relaxationScore;
    }

    public void setRelaxationScore(int relaxationScore) {
        this.relaxationScore = relaxationScore;
    }

    public String getBenefits() {
        return benefits;
    }

    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public void setTemperatureCelsius(double temperatureCelsius) {
        this.temperatureCelsius = temperatureCelsius;
    }

    public String getAromatherapyOil() {
        return aromatherapyOil;
    }

    public void setAromatherapyOil(String aromatherapyOil) {
        this.aromatherapyOil = aromatherapyOil;
    }
}
