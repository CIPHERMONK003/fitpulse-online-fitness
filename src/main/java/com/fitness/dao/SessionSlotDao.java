package com.fitness.dao;

import com.fitness.config.DatabaseManager;
import com.fitness.exception.DatabaseOperationException;
import com.fitness.model.SessionSlot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Session Slots.
 * Implements GenericDao<SessionSlot, Integer>.
 */
public class SessionSlotDao implements GenericDao<SessionSlot, Integer> {

    private final DatabaseManager dbManager;

    public SessionSlotDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public SessionSlotDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public SessionSlot create(SessionSlot slot) {
        Connection conn = dbManager.getConnection();
        String sql = "INSERT INTO session_slots (session_type, title, coach_id, session_date, " +
                "start_time, end_time, max_capacity, booked_count, room_location, price_credits, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, slot.getSessionType());
            ps.setString(2, slot.getTitle());
            if (slot.getCoachOrTherapistId() != null) {
                ps.setInt(3, slot.getCoachOrTherapistId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, slot.getSessionDate());
            ps.setString(5, slot.getStartTime());
            ps.setString(6, slot.getEndTime());
            ps.setInt(7, slot.getMaxCapacity());
            ps.setInt(8, slot.getBookedCount());
            ps.setString(9, slot.getRoomLocation());
            ps.setInt(10, slot.getPriceCredits());
            ps.setBoolean(11, slot.isActive());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    slot.setId(rs.getInt(1));
                }
            }
            return slot;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to create session slot: " + slot.getTitle(), e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    @Override
    public Optional<SessionSlot> findById(Integer id) {
        Connection conn = dbManager.getConnection();
        String sql = "SELECT s.*, u.name as coach_name FROM session_slots s " +
                "LEFT JOIN users u ON s.coach_id = u.id WHERE s.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch session slot by id: " + id, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return Optional.empty();
    }

    @Override
    public List<SessionSlot> findAll() {
        return findByType(null);
    }

    public List<SessionSlot> findByType(String type) {
        List<SessionSlot> list = new ArrayList<>();
        Connection conn = dbManager.getConnection();
        StringBuilder sql = new StringBuilder(
                "SELECT s.*, u.name as coach_name FROM session_slots s " +
                "LEFT JOIN users u ON s.coach_id = u.id WHERE s.is_active = TRUE ");
        if (type != null && !type.trim().isEmpty() && !"ALL".equalsIgnoreCase(type)) {
            sql.append("AND s.session_type = ? ");
        }
        sql.append("ORDER BY s.session_date ASC, s.start_time ASC");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (type != null && !type.trim().isEmpty() && !"ALL".equalsIgnoreCase(type)) {
                ps.setString(1, type.toUpperCase());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch session slots", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return list;
    }

    public List<SessionSlot> findByCoachId(int coachId) {
        List<SessionSlot> list = new ArrayList<>();
        Connection conn = dbManager.getConnection();
        String sql = "SELECT s.*, u.name as coach_name FROM session_slots s " +
                "LEFT JOIN users u ON s.coach_id = u.id WHERE s.coach_id = ? AND s.is_active = TRUE " +
                "ORDER BY s.session_date ASC, s.start_time ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, coachId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch slots for coach: " + coachId, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return list;
    }

    @Override
    public boolean update(SessionSlot slot) {
        Connection conn = dbManager.getConnection();
        String sql = "UPDATE session_slots SET session_type = ?, title = ?, coach_id = ?, " +
                "session_date = ?, start_time = ?, end_time = ?, max_capacity = ?, booked_count = ?, " +
                "room_location = ?, price_credits = ?, is_active = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slot.getSessionType());
            ps.setString(2, slot.getTitle());
            if (slot.getCoachOrTherapistId() != null) {
                ps.setInt(3, slot.getCoachOrTherapistId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, slot.getSessionDate());
            ps.setString(5, slot.getStartTime());
            ps.setString(6, slot.getEndTime());
            ps.setInt(7, slot.getMaxCapacity());
            ps.setInt(8, slot.getBookedCount());
            ps.setString(9, slot.getRoomLocation());
            ps.setInt(10, slot.getPriceCredits());
            ps.setBoolean(11, slot.isActive());
            ps.setInt(12, slot.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update session slot: " + slot.getId(), e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(Integer id) {
        Connection conn = dbManager.getConnection();
        String sql = "UPDATE session_slots SET is_active = FALSE WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to deactivate session slot: " + id, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    private SessionSlot mapResultSet(ResultSet rs) throws SQLException {
        SessionSlot s = new SessionSlot();
        s.setId(rs.getInt("id"));
        s.setSessionType(rs.getString("session_type"));
        s.setTitle(rs.getString("title"));
        int coachId = rs.getInt("coach_id");
        if (!rs.wasNull()) {
            s.setCoachOrTherapistId(coachId);
        }
        s.setCoachName(rs.getString("coach_name") != null ? rs.getString("coach_name") : "Dedicated Specialist");
        s.setSessionDate(rs.getString("session_date"));
        s.setStartTime(rs.getString("start_time"));
        s.setEndTime(rs.getString("end_time"));
        s.setMaxCapacity(rs.getInt("max_capacity"));
        s.setBookedCount(rs.getInt("booked_count"));
        s.setRoomLocation(rs.getString("room_location"));
        s.setPriceCredits(rs.getInt("price_credits"));
        s.setActive(rs.getBoolean("is_active"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        return s;
    }
}
