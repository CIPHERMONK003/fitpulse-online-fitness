package com.fitness;

import com.fitness.exception.*;
import com.fitness.model.*;
import com.fitness.service.ProgressService;
import com.fitness.service.WorkoutPlanService;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Unit Tests for OOP Implementation (10 Marks):
 * - Inheritance (User -> ClientUser, CoachUser, AdminUser; WorkoutPlan -> Strength, Cardio, HIIT)
 * - Polymorphism (Method Overriding, Method Overloading, Polymorphic References)
 * - Exception Handling (Custom checked & unchecked exceptions, chaining)
 * - Interfaces (WorkoutCalculable, Identifiable, Notifiable)
 */
public class OopAndPolymorphismTest {

    @Test
    public void testInheritanceAndSpecialization() {
        ClientUser client = new ClientUser(1, "Alice", "alice@test.com", "hash",
                "Weight Loss", 70.0, 65.0, 170.0, null, "Moderate");
        CoachUser coach = new CoachUser(2, "Sarah", "sarah@test.com", "hash",
                "Strength", "CSCS", 5, 4.9, "Strength Trainer");
        AdminUser admin = new AdminUser(3, "Admin", "admin@test.com", "hash",
                "Operations", 3, true);

        // Verify base class polymorphism
        User user1 = client;
        User user2 = coach;
        User user3 = admin;

        Assert.assertEquals("Alice", user1.getName());
        Assert.assertEquals(Role.CLIENT, user1.getRole());
        Assert.assertEquals(Role.COACH, user2.getRole());
        Assert.assertEquals(Role.ADMIN, user3.getRole());

        // Verify BMI calculation in ClientUser
        double bmi = client.calculateBMI();
        Assert.assertTrue("BMI should be positive", bmi > 20.0 && bmi < 26.0);
        Assert.assertEquals("Normal Weight", client.getBMICategory());
    }

    @Test
    public void testPolymorphicMethodOverriding_WorkoutCalorieCalculations() {
        double clientWeightKg = 75.0;
        int durationMins = 45;

        WorkoutPlan strength = new StrengthWorkoutPlan(1, 10, "Coach", "Bench & Squats",
                "Strength desc", DifficultyLevel.INTERMEDIATE, 6, durationMins, 350.0,
                "Chest & Quads", 4, 10, 60);

        WorkoutPlan cardio = new CardioWorkoutPlan(2, 10, "Coach", "5K Run",
                "Cardio desc", DifficultyLevel.INTERMEDIATE, 6, durationMins, 350.0,
                "Running", "Zone 3", 5.0, 5.5);

        WorkoutPlan hiit = new HIITWorkoutPlan(3, 10, "Coach", "Tabata Blast",
                "HIIT desc", DifficultyLevel.INTERMEDIATE, 6, durationMins, 350.0,
                40, 20, 6, 9);

        // Polymorphic invocation of calculateCaloriesBurned()
        double strengthBurn = strength.calculateCaloriesBurned(durationMins, clientWeightKg);
        double cardioBurn = cardio.calculateCaloriesBurned(durationMins, clientWeightKg);
        double hiitBurn = hiit.calculateCaloriesBurned(durationMins, clientWeightKg);

        Assert.assertTrue("Strength calories should be positive", strengthBurn > 0);
        Assert.assertTrue("Cardio calories should be positive", cardioBurn > 0);
        Assert.assertTrue("HIIT calories should be positive", hiitBurn > 0);

        // Verify polymorphic summary generation
        Assert.assertTrue(strength.generateWorkoutSummary().contains("Strength Plan"));
        Assert.assertTrue(cardio.generateWorkoutSummary().contains("Cardio Plan"));
        Assert.assertTrue(hiit.generateWorkoutSummary().contains("HIIT Plan"));
    }

    @Test
    public void testInterfaceAndDefaultMethods() {
        WorkoutCalculable plan = new StrengthWorkoutPlan();
        double calories = 300.0;
        double weight = 70.0;
        int duration = 60;

        // Test default method defined on WorkoutCalculable interface
        double met = plan.calculateMET(duration, calories, weight);
        Assert.assertTrue("MET should be greater than zero", met > 0);
    }

    @Test
    public void testExceptionHandlingHierarchy() {
        // 1. UserNotFoundException
        try {
            throw new UserNotFoundException(999);
        } catch (FitnessPlatformException e) {
            Assert.assertEquals("USER_NOT_FOUND", e.getErrorCode());
            Assert.assertTrue(e.getMessage().contains("999"));
        }

        // 2. UserAlreadyExistsException
        try {
            throw new UserAlreadyExistsException("duplicate@fitness.com");
        } catch (FitnessPlatformException e) {
            Assert.assertEquals("USER_ALREADY_EXISTS", e.getErrorCode());
            Assert.assertTrue(e.getMessage().contains("duplicate@fitness.com"));
        }

        // 3. Exception Chaining in DatabaseOperationException
        Exception sqlEx = new java.sql.SQLException("Connection refused");
        DatabaseOperationException dbEx = new DatabaseOperationException("Query failed", sqlEx);
        Assert.assertEquals(sqlEx, dbEx.getCause());
        Assert.assertEquals("Query failed", dbEx.getMessage());
    }

    @Test
    public void testMethodOverloading() {
        WorkoutPlanService planService = new WorkoutPlanService();

        // Method overloading: 1 arg vs 2 args vs 3 args
        List<WorkoutPlan> res1 = planService.filterPlans("INTERMEDIATE");
        List<WorkoutPlan> res2 = planService.filterPlans("INTERMEDIATE", PlanType.STRENGTH);
        List<WorkoutPlan> res3 = planService.filterPlans("Accelerator", "INTERMEDIATE", PlanType.STRENGTH);

        Assert.assertNotNull(res1);
        Assert.assertNotNull(res2);
        Assert.assertNotNull(res3);
    }
}
