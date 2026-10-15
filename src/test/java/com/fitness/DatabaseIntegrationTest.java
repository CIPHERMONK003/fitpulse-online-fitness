package com.fitness;

import com.fitness.config.DatabaseManager;
import com.fitness.dao.*;
import com.fitness.model.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Unit & Integration Tests for Database Operations & JDBC (18 Marks):
 * - Database Connectivity (JDBC)
 * - PreparedStatements
 * - Transactions (commit/rollback)
 * - Polymorphic DAO CRUD operations
 * - DatabaseMetaData inspection
 */
public class DatabaseIntegrationTest {

    private static DatabaseManager dbManager;
    private static UserDao userDao;
    private static WorkoutPlanDao planDao;
    private static ProgressRecordDao progressDao;

    @BeforeClass
    public static void setup() {
        dbManager = DatabaseManager.getInstance();
        userDao = new UserDao(dbManager);
        planDao = new WorkoutPlanDao(dbManager);
        progressDao = new ProgressRecordDao(dbManager);
    }

    @Test
    public void testDatabaseConnectionAndMetadata() {
        Connection conn = dbManager.getConnection();
        Assert.assertNotNull("JDBC connection should be non-null", conn);
        try {
            Assert.assertFalse("Connection should be open", conn.isClosed());
        } catch (SQLException e) {
            Assert.fail("SQL Exception checking connection: " + e.getMessage());
        } finally {
            dbManager.releaseConnection(conn);
        }

        // Test metadata inspection
        List<String> tables = dbManager.getDatabaseMetadataTables();
        Assert.assertNotNull(tables);
        Assert.assertTrue("Users table should exist in metadata", tables.contains("USERS"));
        Assert.assertTrue("Workout plans table should exist in metadata", tables.contains("WORKOUT_PLANS"));
    }

    @Test
    public void testPolymorphicUserDaoOperations() {
        String testEmail = "test_client_" + System.currentTimeMillis() + "@fitness.com";
        ClientUser newClient = new ClientUser(null, "Test Client", testEmail, "hashedPass",
                "Marathon Training", 68.0, 64.0, 172.0, null, "Active");

        ClientUser created = (ClientUser) userDao.create(newClient);
        Assert.assertNotNull("Created client should have an ID assigned", created.getId());

        // Polymorphic findById
        Optional<User> fetchedOpt = userDao.findById(created.getId());
        Assert.assertTrue(fetchedOpt.isPresent());
        User fetchedUser = fetchedOpt.get();
        Assert.assertTrue("Fetched user should be instance of ClientUser", fetchedUser instanceof ClientUser);
        Assert.assertEquals(testEmail, fetchedUser.getEmail());

        // Clean up
        userDao.delete(created.getId());
        Assert.assertTrue(userDao.findById(created.getId()).isEmpty());
    }

    @Test
    public void testPolymorphicWorkoutPlanDaoAndEnrollment() {
        StrengthWorkoutPlan strength = new StrengthWorkoutPlan(
                null, null, "Test Coach", "Powerlifting Peaking",
                "8-week heavy strength block", DifficultyLevel.ADVANCED, 8, 60, 420.0,
                "Posterior Chain", 5, 5, 120
        );

        WorkoutPlan saved = planDao.create(strength);
        Assert.assertNotNull(saved.getId());

        Optional<WorkoutPlan> fetchedOpt = planDao.findById(saved.getId());
        Assert.assertTrue(fetchedOpt.isPresent());
        Assert.assertTrue(fetchedOpt.get() instanceof StrengthWorkoutPlan);

        StrengthWorkoutPlan fetchedStrength = (StrengthWorkoutPlan) fetchedOpt.get();
        Assert.assertEquals("Posterior Chain", fetchedStrength.getTargetMuscleGroup());
        Assert.assertEquals(5, fetchedStrength.getRecommendedSets());

        // Test Enrollment
        int testClientId = 1;
        planDao.enrollClient(testClientId, saved.getId());
        Assert.assertTrue(planDao.isClientEnrolled(testClientId, saved.getId()));

        planDao.unenrollClient(testClientId, saved.getId());
        Assert.assertFalse(planDao.isClientEnrolled(testClientId, saved.getId()));

        // Clean up
        planDao.delete(saved.getId());
    }

    @Test
    public void testProgressLoggingAndCalorieAggregation() {
        int clientId = 1;
        double initialCalories = progressDao.calculateTotalCaloriesBurned(clientId);

        ProgressRecord record = new ProgressRecord(
                null, clientId, null, "Custom Workout", 70.5, 45, 360.0, "Felt energetic"
        );
        ProgressRecord created = progressDao.create(record);
        Assert.assertNotNull(created.getId());

        double newCalories = progressDao.calculateTotalCaloriesBurned(clientId);
        Assert.assertEquals(initialCalories + 360.0, newCalories, 0.01);

        // Clean up
        progressDao.delete(created.getId());
    }
}
