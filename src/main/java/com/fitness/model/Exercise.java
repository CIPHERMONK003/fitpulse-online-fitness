package com.fitness.model;

/**
 * Model representing an exercise in the interactive exercise library.
 */
public class Exercise implements Identifiable<Integer> {
    private Integer id;
    private String name;
    private String category;       // "Strength", "Cardio", "HIIT", "Mobility / Yoga", "Spa Recovery"
    private String targetMuscles;  // "Quadriceps, Glutes", "Chest, Triceps", "Full Body"
    private String difficulty;     // "Beginner", "Intermediate", "Advanced"
    private String equipment;      // "Barbell", "Dumbbells", "Bodyweight", "Kettlebell", "Foam Roller"
    private String instructions;
    private double calorieBurnPerMin;
    private String iconEmoji;

    public Exercise() {}

    public Exercise(Integer id, String name, String category, String targetMuscles,
                    String difficulty, String equipment, String instructions,
                    double calorieBurnPerMin, String iconEmoji) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.targetMuscles = targetMuscles;
        this.difficulty = difficulty;
        this.equipment = equipment;
        this.instructions = instructions;
        this.calorieBurnPerMin = calorieBurnPerMin;
        this.iconEmoji = iconEmoji;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTargetMuscles() {
        return targetMuscles;
    }

    public void setTargetMuscles(String targetMuscles) {
        this.targetMuscles = targetMuscles;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public double getCalorieBurnPerMin() {
        return calorieBurnPerMin;
    }

    public void setCalorieBurnPerMin(double calorieBurnPerMin) {
        this.calorieBurnPerMin = calorieBurnPerMin;
    }

    public String getIconEmoji() {
        return iconEmoji != null ? iconEmoji : "⚡";
    }

    public void setIconEmoji(String iconEmoji) {
        this.iconEmoji = iconEmoji;
    }
}
