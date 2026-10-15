package com.fitness.dao;

import com.fitness.config.DatabaseManager;
import com.fitness.exception.DatabaseOperationException;
import com.fitness.model.ProgressRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Progress Records.
 * Implements GenericDao<ProgressRecord, Integer>.
 */
public class ProgressRecordDao implements GenericDao<ProgressRecord, Integer> {

    private final DatabaseManager dbManager;

    public ProgressRecordDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public ProgressRecordDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public ProgressRecord create(ProgressRecord record) {
        String sql = "INSERT INTO progress_records (client_id, workout_plan_id, current_weight_kg, duration_minutes, calories_burned, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, record.getClientId());
            if (record.getWorkoutPlanId() != null) {
                ps.setInt(2, record.getWorkoutPlanId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setDouble(3, record.getCurrentWeightKg());
            ps.setInt(4, record.getDurationMinutes());
            ps.setDouble(5, record.getCaloriesBurned());
            ps.setString(6, record.getNotes());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    record.setId(rs.getInt(1));
                }
            }
            return record;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to log progress record", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    @Override
    public Optional<ProgressRecord> findById(Integer id) {
        String sql = "SELECT pr.*, wp.title as plan_title FROM progress_records pr " +
                "LEFT JOIN workout_plans wp ON pr.workout_plan_id = wp.id WHERE pr.id = ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to query progress record by id: " + id, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return Optional.empty();
    }

    @Override
    public List<ProgressRecord> findAll() {
        List<ProgressRecord> list = new ArrayList<>();
        String sql = "SELECT pr.*, wp.title as plan_title FROM progress_records pr " +
                "LEFT JOIN workout_plans wp ON pr.workout_plan_id = wp.id ORDER BY pr.recorded_at DESC";
        Connection conn = dbManager.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to list all progress records", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return list;
    }

    public List<ProgressRecord> findByClientId(int clientId) {
        List<ProgressRecord> list = new ArrayList<>();
        String sql = "SELECT pr.*, wp.title as plan_title FROM progress_records pr " +
                "LEFT JOIN workout_plans wp ON pr.workout_plan_id = wp.id " +
                "WHERE pr.client_id = ? ORDER BY pr.recorded_at DESC";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find progress records for client: " + clientId, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return list;
    }

    public double calculateTotalCaloriesBurned(int clientId) {
        String sql = "SELECT SUM(calories_burned) FROM progress_records WHERE client_id = ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to calculate total calories for client: " + clientId, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return 0.0;
    }

    public int countWorkoutsByClientId(int clientId) {
        String sql = "SELECT COUNT(*) FROM progress_records WHERE client_id = ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count workouts for client: " + clientId, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return 0;
    }

    @Override
    public boolean update(ProgressRecord record) {
        String sql = "UPDATE progress_records SET current_weight_kg = ?, duration_minutes = ?, calories_burned = ?, notes = ? WHERE id = ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, record.getCurrentWeightKg());
            ps.setInt(2, record.getDurationMinutes());
            ps.setDouble(3, record.getCaloriesBurned());
            ps.setString(4, record.getNotes());
            ps.setInt(5, record.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update progress record: " + record.getId(), e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM progress_records WHERE id = ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete progress record: " + id, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    private ProgressRecord mapRow(ResultSet rs) throws SQLException {
        ProgressRecord record = new ProgressRecord(
                rs.getInt("id"),
                rs.getInt("client_id"),
                (Integer) rs.getObject("workout_plan_id"),
                rs.getString("plan_title"),
                rs.getDouble("current_weight_kg"),
                rs.getInt("duration_minutes"),
                rs.getDouble("calories_burned"),
                rs.getString("notes")
        );
        record.setRecordedAt(rs.getTimestamp("recorded_at"));
        return record;
    }
}
