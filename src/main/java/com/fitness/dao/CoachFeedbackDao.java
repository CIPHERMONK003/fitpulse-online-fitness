package com.fitness.dao;

import com.fitness.config.DatabaseManager;
import com.fitness.exception.DatabaseOperationException;
import com.fitness.model.CoachFeedback;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Coach Feedback & Client Interaction.
 * Implements GenericDao<CoachFeedback, Integer>.
 */
public class CoachFeedbackDao implements GenericDao<CoachFeedback, Integer> {

    private final DatabaseManager dbManager;

    public CoachFeedbackDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public CoachFeedbackDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public CoachFeedback create(CoachFeedback feedback) {
        String sql = "INSERT INTO coach_feedback (coach_id, client_id, message, plan_recommendation, rating_given) VALUES (?, ?, ?, ?, ?)";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, feedback.getCoachId());
            ps.setInt(2, feedback.getClientId());
            ps.setString(3, feedback.getMessage());
            ps.setString(4, feedback.getPlanRecommendation());
            ps.setInt(5, feedback.getRatingGiven());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    feedback.setId(rs.getInt(1));
                }
            }
            return feedback;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to post coach feedback", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    @Override
    public Optional<CoachFeedback> findById(Integer id) {
        String sql = "SELECT f.*, coach.name as coach_name, client.name as client_name " +
                "FROM coach_feedback f " +
                "JOIN users coach ON f.coach_id = coach.id " +
                "JOIN users client ON f.client_id = client.id " +
                "WHERE f.id = ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to query feedback by id: " + id, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return Optional.empty();
    }

    @Override
    public List<CoachFeedback> findAll() {
        List<CoachFeedback> list = new ArrayList<>();
        String sql = "SELECT f.*, coach.name as coach_name, client.name as client_name " +
                "FROM coach_feedback f " +
                "JOIN users coach ON f.coach_id = coach.id " +
                "JOIN users client ON f.client_id = client.id " +
                "ORDER BY f.created_at DESC";
        Connection conn = dbManager.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to list all coach feedback", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return list;
    }

    public List<CoachFeedback> findByClientId(int clientId) {
        List<CoachFeedback> list = new ArrayList<>();
        String sql = "SELECT f.*, coach.name as coach_name, client.name as client_name " +
                "FROM coach_feedback f " +
                "JOIN users coach ON f.coach_id = coach.id " +
                "JOIN users client ON f.client_id = client.id " +
                "WHERE f.client_id = ? ORDER BY f.created_at DESC";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to query feedback for client: " + clientId, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return list;
    }

    public List<CoachFeedback> findByCoachId(int coachId) {
        List<CoachFeedback> list = new ArrayList<>();
        String sql = "SELECT f.*, coach.name as coach_name, client.name as client_name " +
                "FROM coach_feedback f " +
                "JOIN users coach ON f.coach_id = coach.id " +
                "JOIN users client ON f.client_id = client.id " +
                "WHERE f.coach_id = ? ORDER BY f.created_at DESC";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, coachId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to query feedback by coach: " + coachId, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return list;
    }

    @Override
    public boolean update(CoachFeedback feedback) {
        String sql = "UPDATE coach_feedback SET message = ?, plan_recommendation = ?, rating_given = ? WHERE id = ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, feedback.getMessage());
            ps.setString(2, feedback.getPlanRecommendation());
            ps.setInt(3, feedback.getRatingGiven());
            ps.setInt(4, feedback.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update coach feedback", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM coach_feedback WHERE id = ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete coach feedback: " + id, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    private CoachFeedback mapRow(ResultSet rs) throws SQLException {
        CoachFeedback fb = new CoachFeedback(
                rs.getInt("id"),
                rs.getInt("coach_id"),
                rs.getString("coach_name"),
                rs.getInt("client_id"),
                rs.getString("client_name"),
                rs.getString("message"),
                rs.getString("plan_recommendation"),
                rs.getInt("rating_given")
        );
        fb.setCreatedAt(rs.getTimestamp("created_at"));
        return fb;
    }
}
