package com.fitness.dao;

import com.fitness.config.DatabaseManager;
import com.fitness.exception.DatabaseOperationException;
import com.fitness.model.SlotBooking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Slot Bookings.
 * Demonstrates JDBC PreparedStatement, relational joins, and transaction rollback protection.
 */
public class SlotBookingDao implements GenericDao<SlotBooking, Integer> {

    private final DatabaseManager dbManager;

    public SlotBookingDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public SlotBookingDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public SlotBooking create(SlotBooking booking) {
        Connection conn = dbManager.getConnection();
        String sql = "INSERT INTO slot_bookings (slot_id, client_id, status, notes) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, booking.getSlotId());
            ps.setInt(2, booking.getClientId());
            ps.setString(3, booking.getStatus());
            ps.setString(4, booking.getNotes());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    booking.setId(rs.getInt(1));
                }
            }
            return booking;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to insert slot booking", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    @Override
    public Optional<SlotBooking> findById(Integer id) {
        Connection conn = dbManager.getConnection();
        String sql = "SELECT b.*, u.name as client_name, u.email as client_email, " +
                "s.title as session_title, s.session_type, s.session_date, s.start_time, s.room_location, " +
                "co.name as coach_name " +
                "FROM slot_bookings b " +
                "JOIN users u ON b.client_id = u.id " +
                "JOIN session_slots s ON b.slot_id = s.id " +
                "LEFT JOIN users co ON s.coach_id = co.id " +
                "WHERE b.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch booking by id: " + id, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return Optional.empty();
    }

    @Override
    public List<SlotBooking> findAll() {
        List<SlotBooking> list = new ArrayList<>();
        Connection conn = dbManager.getConnection();
        String sql = "SELECT b.*, u.name as client_name, u.email as client_email, " +
                "s.title as session_title, s.session_type, s.session_date, s.start_time, s.room_location, " +
                "co.name as coach_name " +
                "FROM slot_bookings b " +
                "JOIN users u ON b.client_id = u.id " +
                "JOIN session_slots s ON b.slot_id = s.id " +
                "LEFT JOIN users co ON s.coach_id = co.id " +
                "ORDER BY b.booked_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch all bookings", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return list;
    }

    public List<SlotBooking> findByClientId(int clientId) {
        List<SlotBooking> list = new ArrayList<>();
        Connection conn = dbManager.getConnection();
        String sql = "SELECT b.*, u.name as client_name, u.email as client_email, " +
                "s.title as session_title, s.session_type, s.session_date, s.start_time, s.room_location, " +
                "co.name as coach_name " +
                "FROM slot_bookings b " +
                "JOIN users u ON b.client_id = u.id " +
                "JOIN session_slots s ON b.slot_id = s.id " +
                "LEFT JOIN users co ON s.coach_id = co.id " +
                "WHERE b.client_id = ? " +
                "ORDER BY s.session_date DESC, s.start_time DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch bookings for client: " + clientId, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return list;
    }

    public boolean isClientAlreadyBooked(int slotId, int clientId) {
        Connection conn = dbManager.getConnection();
        String sql = "SELECT COUNT(*) FROM slot_bookings WHERE slot_id = ? AND client_id = ? AND status = 'CONFIRMED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            ps.setInt(2, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to check existing booking", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return false;
    }

    @Override
    public boolean update(SlotBooking booking) {
        Connection conn = dbManager.getConnection();
        String sql = "UPDATE slot_bookings SET status = ?, notes = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, booking.getStatus());
            ps.setString(2, booking.getNotes());
            ps.setInt(3, booking.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update booking: " + booking.getId(), e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(Integer id) {
        Connection conn = dbManager.getConnection();
        String sql = "DELETE FROM slot_bookings WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete booking: " + id, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    private SlotBooking mapResultSet(ResultSet rs) throws SQLException {
        SlotBooking b = new SlotBooking();
        b.setId(rs.getInt("id"));
        b.setSlotId(rs.getInt("slot_id"));
        b.setClientId(rs.getInt("client_id"));
        b.setClientName(rs.getString("client_name"));
        b.setClientEmail(rs.getString("client_email"));
        b.setSessionTitle(rs.getString("session_title"));
        b.setSessionType(rs.getString("session_type"));
        b.setCoachName(rs.getString("coach_name") != null ? rs.getString("coach_name") : "Dedicated Specialist");
        b.setSessionDate(rs.getString("session_date"));
        b.setStartTime(rs.getString("start_time"));
        b.setRoomLocation(rs.getString("room_location"));
        b.setStatus(rs.getString("status"));
        b.setNotes(rs.getString("notes"));
        b.setBookedAt(rs.getTimestamp("booked_at"));
        return b;
    }
}
