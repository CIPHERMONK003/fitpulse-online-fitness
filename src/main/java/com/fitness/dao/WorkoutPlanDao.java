package com.fitness.dao;

import com.fitness.config.DatabaseManager;
import com.fitness.exception.DatabaseOperationException;
import com.fitness.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for WorkoutPlans with polymorphic persistence.
 * Implements GenericDao<WorkoutPlan, Integer>.
 */
public class WorkoutPlanDao implements GenericDao<WorkoutPlan, Integer> {

    private final DatabaseManager dbManager;

    public WorkoutPlanDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public WorkoutPlanDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public WorkoutPlan create(WorkoutPlan plan) {
        Connection conn = dbManager.getConnection();
        try {
            conn.setAutoCommit(false); // Transaction

            String sql = "INSERT INTO workout_plans (coach_id, title, description, plan_type, difficulty_level, " +
                    "duration_weeks, est_duration_minutes, base_calories, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                if (plan.getCoachId() != null) {
                    ps.setInt(1, plan.getCoachId());
                } else {
                    ps.setNull(1, Types.INTEGER);
                }
                ps.setString(2, plan.getTitle());
                ps.setString(3, plan.getDescription());
                ps.setString(4, plan.getPlanType().name());
                ps.setString(5, plan.getDifficultyLevel().name());
                ps.setInt(6, plan.getDurationWeeks());
                ps.setInt(7, plan.getEstimatedDurationMinutes());
                ps.setDouble(8, plan.getBaseCaloriesBurned());
                ps.setBoolean(9, plan.isActive());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        plan.setId(rs.getInt(1));
                    }
                }
            }

            // Insert specialized subclass records
            if (plan instanceof StrengthWorkoutPlan strength) {
                String subSql = "INSERT INTO strength_plans (plan_id, target_muscle_group, recommended_sets, recommended_reps, rest_sec) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(subSql)) {
                    ps.setInt(1, strength.getId());
                    ps.setString(2, strength.getTargetMuscleGroup());
                    ps.setInt(3, strength.getRecommendedSets());
                    ps.setInt(4, strength.getRecommendedReps());
                    ps.setInt(5, strength.getRestBetweenSetsSec());
                    ps.executeUpdate();
                }
            } else if (plan instanceof CardioWorkoutPlan cardio) {
                String subSql = "INSERT INTO cardio_plans (plan_id, cardio_type, hr_zone, distance_km, target_pace) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(subSql)) {
                    ps.setInt(1, cardio.getId());
                    ps.setString(2, cardio.getCardioType());
                    ps.setString(3, cardio.getTargetHeartRateZone());
                    ps.setDouble(4, cardio.getDistanceKm());
                    ps.setDouble(5, cardio.getTargetPaceMinPerKm());
                    ps.executeUpdate();
                }
            } else if (plan instanceof HIITWorkoutPlan hiit) {
                String subSql = "INSERT INTO hiit_plans (plan_id, work_sec, rest_sec, num_rounds, intensity_rating) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(subSql)) {
                    ps.setInt(1, hiit.getId());
                    ps.setInt(2, hiit.getWorkIntervalSec());
                    ps.setInt(3, hiit.getRestIntervalSec());
                    ps.setInt(4, hiit.getNumberOfRounds());
                    ps.setInt(5, hiit.getIntensityRating());
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return plan;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {}
            throw new DatabaseOperationException("Failed to save workout plan: " + plan.getTitle(), e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    @Override
    public Optional<WorkoutPlan> findById(Integer id) {
        String sql = "SELECT p.*, u.name as coach_name FROM workout_plans p LEFT JOIN users u ON p.coach_id = u.id WHERE p.id = ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(mapRowToWorkoutPlanPolymorphic(conn, rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find workout plan by id: " + id, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return Optional.empty();
    }

    @Override
    public List<WorkoutPlan> findAll() {
        List<WorkoutPlan> list = new ArrayList<>();
        String sql = "SELECT p.*, u.name as coach_name FROM workout_plans p LEFT JOIN users u ON p.coach_id = u.id ORDER BY p.id DESC";
        Connection conn = dbManager.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                WorkoutPlan plan = mapRowToWorkoutPlanPolymorphic(conn, rs);
                if (plan != null) {
                    list.add(plan);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to list all workout plans", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return list;
    }

    public List<WorkoutPlan> findAllActive() {
        List<WorkoutPlan> list = new ArrayList<>();
        String sql = "SELECT p.*, u.name as coach_name FROM workout_plans p LEFT JOIN users u ON p.coach_id = u.id WHERE p.is_active = TRUE ORDER BY p.id DESC";
        Connection conn = dbManager.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                WorkoutPlan plan = mapRowToWorkoutPlanPolymorphic(conn, rs);
                if (plan != null) {
                    list.add(plan);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to list active plans", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return list;
    }

    public List<WorkoutPlan> findByCoachId(int coachId) {
        List<WorkoutPlan> list = new ArrayList<>();
        String sql = "SELECT p.*, u.name as coach_name FROM workout_plans p LEFT JOIN users u ON p.coach_id = u.id WHERE p.coach_id = ? ORDER BY p.id DESC";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, coachId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WorkoutPlan plan = mapRowToWorkoutPlanPolymorphic(conn, rs);
                    if (plan != null) {
                        list.add(plan);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to list plans for coach: " + coachId, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return list;
    }

    public List<WorkoutPlan> findEnrolledPlansByClientId(int clientId) {
        List<WorkoutPlan> list = new ArrayList<>();
        String sql = "SELECT p.*, u.name as coach_name FROM client_enrolled_plans ep " +
                "JOIN workout_plans p ON ep.plan_id = p.id " +
                "LEFT JOIN users u ON p.coach_id = u.id " +
                "WHERE ep.client_id = ? AND ep.status = 'ACTIVE' ORDER BY ep.enrolled_at DESC";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WorkoutPlan plan = mapRowToWorkoutPlanPolymorphic(conn, rs);
                    if (plan != null) {
                        list.add(plan);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to list enrolled plans for client: " + clientId, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return list;
    }

    public boolean isClientEnrolled(int clientId, int planId) {
        String sql = "SELECT COUNT(*) FROM client_enrolled_plans WHERE client_id = ? AND plan_id = ? AND status = 'ACTIVE'";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            ps.setInt(2, planId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to check enrollment status", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    public boolean enrollClient(int clientId, int planId) {
        unenrollClient(clientId, planId);
        String sql = "INSERT INTO client_enrolled_plans (client_id, plan_id, enrolled_at, status) VALUES (?, ?, CURRENT_TIMESTAMP, 'ACTIVE')";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            ps.setInt(2, planId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to enroll client into plan", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    public boolean unenrollClient(int clientId, int planId) {
        String sql = "DELETE FROM client_enrolled_plans WHERE client_id = ? AND plan_id = ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            ps.setInt(2, planId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to unenroll client", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    @Override
    public boolean update(WorkoutPlan plan) {
        String sql = "UPDATE workout_plans SET title = ?, description = ?, difficulty_level = ?, duration_weeks = ?, est_duration_minutes = ?, base_calories = ?, is_active = ? WHERE id = ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plan.getTitle());
            ps.setString(2, plan.getDescription());
            ps.setString(3, plan.getDifficultyLevel().name());
            ps.setInt(4, plan.getDurationWeeks());
            ps.setInt(5, plan.getEstimatedDurationMinutes());
            ps.setDouble(6, plan.getBaseCaloriesBurned());
            ps.setBoolean(7, plan.isActive());
            ps.setInt(8, plan.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update workout plan: " + plan.getId(), e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM workout_plans WHERE id = ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete workout plan: " + id, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Polymorphically maps a database row to its specialized concrete WorkoutPlan subclass.
     */
    private WorkoutPlan mapRowToWorkoutPlanPolymorphic(Connection conn, ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        Integer coachId = (Integer) rs.getObject("coach_id");
        String coachName = rs.getString("coach_name");
        String title = rs.getString("title");
        String desc = rs.getString("description");
        PlanType planType = PlanType.fromString(rs.getString("plan_type"));
        DifficultyLevel level = DifficultyLevel.fromString(rs.getString("difficulty_level"));
        int durationWeeks = rs.getInt("duration_weeks");
        int estMinutes = rs.getInt("est_duration_minutes");
        double baseCalories = rs.getDouble("base_calories");
        boolean active = rs.getBoolean("is_active");
        Timestamp createdAt = rs.getTimestamp("created_at");

        switch (planType) {
            case STRENGTH: {
                String subSql = "SELECT * FROM strength_plans WHERE plan_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(subSql)) {
                    ps.setInt(1, id);
                    try (ResultSet subRs = ps.executeQuery()) {
                        if (subRs.next()) {
                            StrengthWorkoutPlan p = new StrengthWorkoutPlan(
                                    id, coachId, coachName, title, desc, level, durationWeeks, estMinutes, baseCalories,
                                    subRs.getString("target_muscle_group"),
                                    subRs.getInt("recommended_sets"),
                                    subRs.getInt("recommended_reps"),
                                    subRs.getInt("rest_sec")
                            );
                            p.setActive(active);
                            p.setCreatedAt(createdAt);
                            return p;
                        }
                    }
                }
                StrengthWorkoutPlan p = new StrengthWorkoutPlan(id, coachId, coachName, title, desc, level, durationWeeks, estMinutes, baseCalories, "Full Body", 3, 10, 60);
                p.setActive(active);
                p.setCreatedAt(createdAt);
                return p;
            }
            case CARDIO: {
                String subSql = "SELECT * FROM cardio_plans WHERE plan_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(subSql)) {
                    ps.setInt(1, id);
                    try (ResultSet subRs = ps.executeQuery()) {
                        if (subRs.next()) {
                            CardioWorkoutPlan p = new CardioWorkoutPlan(
                                    id, coachId, coachName, title, desc, level, durationWeeks, estMinutes, baseCalories,
                                    subRs.getString("cardio_type"),
                                    subRs.getString("hr_zone"),
                                    subRs.getDouble("distance_km"),
                                    subRs.getDouble("target_pace")
                            );
                            p.setActive(active);
                            p.setCreatedAt(createdAt);
                            return p;
                        }
                    }
                }
                CardioWorkoutPlan p = new CardioWorkoutPlan(id, coachId, coachName, title, desc, level, durationWeeks, estMinutes, baseCalories, "Running", "Zone 2", 5.0, 5.5);
                p.setActive(active);
                p.setCreatedAt(createdAt);
                return p;
            }
            case HIIT: {
                String subSql = "SELECT * FROM hiit_plans WHERE plan_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(subSql)) {
                    ps.setInt(1, id);
                    try (ResultSet subRs = ps.executeQuery()) {
                        if (subRs.next()) {
                            HIITWorkoutPlan p = new HIITWorkoutPlan(
                                    id, coachId, coachName, title, desc, level, durationWeeks, estMinutes, baseCalories,
                                    subRs.getInt("work_sec"),
                                    subRs.getInt("rest_sec"),
                                    subRs.getInt("num_rounds"),
                                    subRs.getInt("intensity_rating")
                            );
                            p.setActive(active);
                            p.setCreatedAt(createdAt);
                            return p;
                        }
                    }
                }
                HIITWorkoutPlan p = new HIITWorkoutPlan(id, coachId, coachName, title, desc, level, durationWeeks, estMinutes, baseCalories, 45, 15, 6, 8);
                p.setActive(active);
                p.setCreatedAt(createdAt);
                return p;
            }
            default:
                return null;
        }
    }
}
