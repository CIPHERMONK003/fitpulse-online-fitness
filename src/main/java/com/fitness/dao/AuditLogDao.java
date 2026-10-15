package com.fitness.dao;

import com.fitness.config.DatabaseManager;
import com.fitness.exception.DatabaseOperationException;
import com.fitness.model.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Administrative Audit Logs.
 * Implements GenericDao<AuditLog, Integer>.
 */
public class AuditLogDao implements GenericDao<AuditLog, Integer> {

    private final DatabaseManager dbManager;

    public AuditLogDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public AuditLogDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public AuditLog create(AuditLog log) {
        String sql = "INSERT INTO audit_logs (admin_id, admin_email, action_type, details, ip_address) VALUES (?, ?, ?, ?, ?)";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (log.getAdminId() != null) {
                ps.setInt(1, log.getAdminId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, log.getAdminEmail());
            ps.setString(3, log.getActionType());
            ps.setString(4, log.getDetails());
            ps.setString(5, log.getIpAddress());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    log.setId(rs.getInt(1));
                }
            }
            return log;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to insert audit log", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    @Override
    public Optional<AuditLog> findById(Integer id) {
        String sql = "SELECT * FROM audit_logs WHERE id = ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find audit log: " + id, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return Optional.empty();
    }

    @Override
    public List<AuditLog> findAll() {
        return findRecent(50);
    }

    public List<AuditLog> findRecent(int limit) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve audit logs", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return list;
    }

    @Override
    public boolean update(AuditLog entity) {
        // Audit logs are append-only by design
        return false;
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM audit_logs WHERE id = ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete audit log: " + id, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog(
                rs.getInt("id"),
                (Integer) rs.getObject("admin_id"),
                rs.getString("admin_email"),
                rs.getString("action_type"),
                rs.getString("details"),
                rs.getString("ip_address")
        );
        log.setTimestamp(rs.getTimestamp("timestamp"));
        return log;
    }
}
