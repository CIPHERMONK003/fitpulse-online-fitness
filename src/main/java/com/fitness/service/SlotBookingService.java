package com.fitness.service;

import com.fitness.config.DatabaseManager;
import com.fitness.concurrency.NotificationQueueService;
import com.fitness.concurrency.NotificationTask;
import com.fitness.concurrency.PlatformMetricsManager;
import com.fitness.dao.SessionSlotDao;
import com.fitness.dao.SlotBookingDao;
import com.fitness.exception.DatabaseOperationException;
import com.fitness.exception.FitnessPlatformException;
import com.fitness.model.SessionSlot;
import com.fitness.model.SlotBooking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Service for Session Slot reservations and booking management.
 * Demonstrates:
 * - Multithreading & Synchronization (Thread-safe atomic capacity checks)
 * - JDBC Transactions (setAutoCommit(false), commit, rollback)
 * - Producer-Consumer Notification Queue integration
 */
public class SlotBookingService {

    private final SessionSlotDao slotDao;
    private final SlotBookingDao bookingDao;
    private final DatabaseManager dbManager;
    private final NotificationQueueService notificationQueue;
    private final PlatformMetricsManager metricsManager;

    public SlotBookingService() {
        this.slotDao = new SessionSlotDao();
        this.bookingDao = new SlotBookingDao();
        this.dbManager = DatabaseManager.getInstance();
        this.notificationQueue = NotificationQueueService.getInstance();
        this.metricsManager = PlatformMetricsManager.getInstance();
    }

    public List<SessionSlot> getAvailableSlots(String type) {
        return slotDao.findByType(type);
    }

    public List<SessionSlot> getAllSlots() {
        return slotDao.findAll();
    }

    public List<SlotBooking> getClientBookings(int clientId) {
        return bookingDao.findByClientId(clientId);
    }

    /**
     * Atomically books a session slot for a client.
     * Uses synchronized method and JDBC transaction to prevent double-booking.
     */
    public synchronized SlotBooking bookSlot(int slotId, int clientId, String notes) throws FitnessPlatformException {
        if (bookingDao.isClientAlreadyBooked(slotId, clientId)) {
            throw new FitnessPlatformException("You already have an active reservation for this session slot.");
        }

        Connection conn = dbManager.getConnection();
        try {
            conn.setAutoCommit(false);

            // 1. Verify capacity with row lock / current count
            SessionSlot slot = slotDao.findById(slotId)
                    .orElseThrow(() -> new FitnessPlatformException("Selected session slot does not exist."));

            if (slot.isFullyBooked()) {
                throw new FitnessPlatformException("Sorry, this session slot is completely full (" +
                        slot.getMaxCapacity() + "/" + slot.getMaxCapacity() + ").");
            }

            // 2. Increment booked count on slot
            String updateSlotSql = "UPDATE session_slots SET booked_count = booked_count + 1 WHERE id = ? AND booked_count < max_capacity";
            try (PreparedStatement ps = conn.prepareStatement(updateSlotSql)) {
                ps.setInt(1, slotId);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new FitnessPlatformException("Failed to reserve slot: Session just reached full capacity.");
                }
            }

            // 3. Create booking record
            String insertBookingSql = "INSERT INTO slot_bookings (slot_id, client_id, status, notes) VALUES (?, ?, 'CONFIRMED', ?)";
            int newBookingId = 0;
            try (PreparedStatement ps = conn.prepareStatement(insertBookingSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, slotId);
                ps.setInt(2, clientId);
                ps.setString(3, notes != null ? notes : "");
                ps.executeUpdate();
                try (var rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        newBookingId = rs.getInt(1);
                    }
                }
            }

            conn.commit();

            // 4. Asynchronously notify client via producer-consumer queue
            notificationQueue.enqueue(new NotificationTask(
                    "client-" + clientId + "@fitness.com",
                    "Member #" + clientId,
                    "Confirmed Booking: " + slot.getTitle(),
                    "Your session has been confirmed for " + slot.getSessionDate() + " at " + slot.getStartTime() + " in " + slot.getRoomLocation(),
                    "IN_APP"
            ));

            return bookingDao.findById(newBookingId)
                    .orElseThrow(() -> new FitnessPlatformException("Booking created but failed to load."));

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {}
            throw new DatabaseOperationException("Booking transaction failed: " + e.getMessage(), e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Atomically cancels an existing booking and restores slot capacity.
     */
    public synchronized boolean cancelBooking(int bookingId, int clientId) throws FitnessPlatformException {
        SlotBooking booking = bookingDao.findById(bookingId)
                .orElseThrow(() -> new FitnessPlatformException("Booking not found: " + bookingId));

        if (booking.getClientId() != clientId) {
            throw new FitnessPlatformException("Unauthorized: You do not own this booking.");
        }

        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            return false;
        }

        Connection conn = dbManager.getConnection();
        try {
            conn.setAutoCommit(false);

            // 1. Mark booking CANCELLED
            String cancelSql = "UPDATE slot_bookings SET status = 'CANCELLED' WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(cancelSql)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }

            // 2. Decrement booked count on slot
            String decSlotSql = "UPDATE session_slots SET booked_count = GREATEST(0, booked_count - 1) WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(decSlotSql)) {
                ps.setInt(1, booking.getSlotId());
                ps.executeUpdate();
            }

            conn.commit();

            // Asynchronous notification
            notificationQueue.enqueue(new NotificationTask(
                    "client-" + clientId + "@fitness.com",
                    "Member #" + clientId,
                    "Cancelled Booking: " + booking.getSessionTitle(),
                    "Your reservation for " + booking.getSessionDate() + " has been released.",
                    "IN_APP"
            ));

            return true;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {}
            throw new DatabaseOperationException("Cancellation transaction failed", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    public SessionSlot createSlot(SessionSlot slot) {
        return slotDao.create(slot);
    }
}
