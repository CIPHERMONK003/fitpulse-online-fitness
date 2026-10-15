package com.fitness.model;

/**
 * Concrete subclass representing a Fitness Platform Client / Member.
 * Demonstrates:
 * - Inheritance from User
 * - Method Overriding (Polymorphism)
 * - Specialized fields (fitness goals, target weight, current weight, height, assigned coach)
 */
public class ClientUser extends User {
    private String fitnessGoal;
    private double currentWeightKg;
    private double targetWeightKg;
    private double heightCm;
    private Integer assignedCoachId;
    private String assignedCoachName;
    private String activityLevel; // Sedentary, Moderate, Active

    public ClientUser() {
        super();
        setRole(Role.CLIENT);
    }

    public ClientUser(Integer id, String name, String email, String passwordHash,
                      String fitnessGoal, double currentWeightKg, double targetWeightKg,
                      double heightCm, Integer assignedCoachId, String activityLevel) {
        super(id, name, email, passwordHash, Role.CLIENT);
        this.fitnessGoal = fitnessGoal;
        this.currentWeightKg = currentWeightKg;
        this.targetWeightKg = targetWeightKg;
        this.heightCm = heightCm;
        this.assignedCoachId = assignedCoachId;
        this.activityLevel = activityLevel != null ? activityLevel : "Moderate";
    }

    public String getFitnessGoal() {
        return fitnessGoal;
    }

    public void setFitnessGoal(String fitnessGoal) {
        this.fitnessGoal = fitnessGoal;
    }

    public double getCurrentWeightKg() {
        return currentWeightKg;
    }

    public void setCurrentWeightKg(double currentWeightKg) {
        this.currentWeightKg = currentWeightKg;
    }

    public double getTargetWeightKg() {
        return targetWeightKg;
    }

    public void setTargetWeightKg(double targetWeightKg) {
        this.targetWeightKg = targetWeightKg;
    }

    public double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(double heightCm) {
        this.heightCm = heightCm;
    }

    public Integer getAssignedCoachId() {
        return assignedCoachId;
    }

    public void setAssignedCoachId(Integer assignedCoachId) {
        this.assignedCoachId = assignedCoachId;
    }

    public String getAssignedCoachName() {
        return assignedCoachName;
    }

    public void setAssignedCoachName(String assignedCoachName) {
        this.assignedCoachName = assignedCoachName;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    /**
     * Calculates Body Mass Index (BMI).
     */
    public double calculateBMI() {
        if (heightCm <= 0) return 0.0;
        double heightInMeters = heightCm / 100.0;
        return currentWeightKg / (heightInMeters * heightInMeters);
    }

    public String getBMICategory() {
        double bmi = calculateBMI();
        if (bmi <= 0) return "Unknown";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal Weight";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    @Override
    public String getDashboardUrl() {
        return "/dashboard?view=client";
    }

    @Override
    public String getProfileSummary() {
        return String.format("Client [Goal: %s | Weight: %.1fkg / Target: %.1fkg | BMI: %.1f (%s)]",
                fitnessGoal, currentWeightKg, targetWeightKg, calculateBMI(), getBMICategory());
    }
}
