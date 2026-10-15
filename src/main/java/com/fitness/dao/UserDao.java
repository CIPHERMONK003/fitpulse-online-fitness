package com.fitness.dao;

import com.fitness.config.DatabaseManager;
import com.fitness.exception.DatabaseOperationException;
import com.fitness.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Users with polymorphic entity mapping.
 * Implements GenericDao<User, Integer>.
 */
public class UserDao implements GenericDao<User, Integer> {

    private final DatabaseManager dbManager;

    public UserDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public UserDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public User create(User user) {
        Connection conn = dbManager.getConnection();
        try {
            conn.setAutoCommit(false); // Transaction

            String sql = "INSERT INTO users (name, email, password_hash, role, is_active) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getName());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPasswordHash());
                ps.setString(4, user.getRole().name());
                ps.setBoolean(5, user.isActive());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
            }

            // Insert role-specific profile in the same transaction
            if (user instanceof ClientUser client) {
                String clientSql = "INSERT INTO client_profiles (user_id, fitness_goal, current_weight_kg, target_weight_kg, height_cm, assigned_coach_id, activity_level) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(clientSql)) {
                    ps.setInt(1, client.getId());
                    ps.setString(2, client.getFitnessGoal());
                    ps.setDouble(3, client.getCurrentWeightKg());
                    ps.setDouble(4, client.getTargetWeightKg());
                    ps.setDouble(5, client.getHeightCm());
                    if (client.getAssignedCoachId() != null) {
                        ps.setInt(6, client.getAssignedCoachId());
                    } else {
                        ps.setNull(6, Types.INTEGER);
                    }
                    ps.setString(7, client.getActivityLevel());
                    ps.executeUpdate();
                }
            } else if (user instanceof CoachUser coach) {
                String coachSql = "INSERT INTO coach_profiles (user_id, specialization, certification, experience_years, rating, bio) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(coachSql)) {
                    ps.setInt(1, coach.getId());
                    ps.setString(2, coach.getSpecialization());
                    ps.setString(3, coach.getCertification());
                    ps.setInt(4, coach.getExperienceYears());
                    ps.setDouble(5, coach.getRating());
                    ps.setString(6, coach.getBio());
                    ps.executeUpdate();
                }
            } else if (user instanceof AdminUser admin) {
                String adminSql = "INSERT INTO admin_profiles (user_id, department, admin_level, can_manage_admins) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(adminSql)) {
                    ps.setInt(1, admin.getId());
                    ps.setString(2, admin.getDepartment());
                    ps.setInt(3, admin.getAdminLevel());
                    ps.setBoolean(4, admin.isCanManageAdmins());
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return user;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {}
            throw new DatabaseOperationException("Failed to create user: " + user.getEmail(), e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    @Override
    public Optional<User> findById(Integer id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(mapRowToUserPolymorphic(conn, rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to query user by id: " + id, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(mapRowToUserPolymorphic(conn, rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to query user by email: " + email, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id ASC";
        Connection conn = dbManager.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = mapRowToUserPolymorphic(conn, rs);
                if (user != null) {
                    list.add(user);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to list all users", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return list;
    }

    public List<CoachUser> findAllCoaches() {
        List<CoachUser> coaches = new ArrayList<>();
        String sql = "SELECT u.*, c.specialization, c.certification, c.experience_years, c.rating, c.bio " +
                "FROM users u JOIN coach_profiles c ON u.id = c.user_id WHERE u.role = 'COACH' AND u.is_active = TRUE";
        Connection conn = dbManager.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                CoachUser coach = new CoachUser(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("specialization"),
                        rs.getString("certification"),
                        rs.getInt("experience_years"),
                        rs.getDouble("rating"),
                        rs.getString("bio")
                );
                coach.setActive(rs.getBoolean("is_active"));
                coach.setCreatedAt(rs.getTimestamp("created_at"));
                coaches.add(coach);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to query coaches", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return coaches;
    }

    public List<ClientUser> findClientsByCoachId(int coachId) {
        List<ClientUser> clients = new ArrayList<>();
        String sql = "SELECT u.*, cp.fitness_goal, cp.current_weight_kg, cp.target_weight_kg, cp.height_cm, cp.assigned_coach_id, cp.activity_level, coach.name as coach_name " +
                "FROM users u " +
                "JOIN client_profiles cp ON u.id = cp.user_id " +
                "LEFT JOIN users coach ON cp.assigned_coach_id = coach.id " +
                "WHERE cp.assigned_coach_id = ? AND u.is_active = TRUE";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, coachId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ClientUser client = new ClientUser(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getString("fitness_goal"),
                            rs.getDouble("current_weight_kg"),
                            rs.getDouble("target_weight_kg"),
                            rs.getDouble("height_cm"),
                            rs.getInt("assigned_coach_id"),
                            rs.getString("activity_level")
                    );
                    client.setAssignedCoachName(rs.getString("coach_name"));
                    client.setActive(rs.getBoolean("is_active"));
                    client.setCreatedAt(rs.getTimestamp("created_at"));
                    clients.add(client);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to query clients for coach: " + coachId, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
        return clients;
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET name = ?, email = ?, is_active = ? WHERE id = ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setBoolean(3, user.isActive());
            ps.setInt(4, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update user: " + user.getId(), e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    public boolean updateClientWeight(int clientId, double newWeightKg) {
        String sql = "UPDATE client_profiles SET current_weight_kg = ? WHERE user_id = ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newWeightKg);
            ps.setInt(2, clientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update client weight", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    public boolean assignCoachToClient(int clientId, int coachId) {
        String sql = "UPDATE client_profiles SET assigned_coach_id = ? WHERE user_id = ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, coachId);
            ps.setInt(2, clientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to assign coach", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    public boolean toggleActiveStatus(int userId, boolean active) {
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to toggle user status", e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM users WHERE id = ?";
        Connection conn = dbManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete user: " + id, e);
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Polymorphically maps a database row to its corresponding concrete User subclass.
     */
    private User mapRowToUserPolymorphic(Connection conn, ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        Role role = Role.fromString(rs.getString("role"));
        boolean active = rs.getBoolean("is_active");
        Timestamp createdAt = rs.getTimestamp("created_at");

        switch (role) {
            case CLIENT: {
                String subSql = "SELECT cp.*, u.name as coach_name FROM client_profiles cp " +
                        "LEFT JOIN users u ON cp.assigned_coach_id = u.id WHERE cp.user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(subSql)) {
                    ps.setInt(1, id);
                    try (ResultSet subRs = ps.executeQuery()) {
                        if (subRs.next()) {
                            Integer coachId = (Integer) subRs.getObject("assigned_coach_id");
                            ClientUser client = new ClientUser(
                                    id, name, email, passwordHash,
                                    subRs.getString("fitness_goal"),
                                    subRs.getDouble("current_weight_kg"),
                                    subRs.getDouble("target_weight_kg"),
                                    subRs.getDouble("height_cm"),
                                    coachId,
                                    subRs.getString("activity_level")
                            );
                            client.setAssignedCoachName(subRs.getString("coach_name"));
                            client.setActive(active);
                            client.setCreatedAt(createdAt);
                            return client;
                        }
                    }
                }
                // Fallback default client
                ClientUser client = new ClientUser(id, name, email, passwordHash, "General Fitness", 70, 65, 170, null, "Moderate");
                client.setActive(active);
                client.setCreatedAt(createdAt);
                return client;
            }
            case COACH: {
                String subSql = "SELECT * FROM coach_profiles WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(subSql)) {
                    ps.setInt(1, id);
                    try (ResultSet subRs = ps.executeQuery()) {
                        if (subRs.next()) {
                            CoachUser coach = new CoachUser(
                                    id, name, email, passwordHash,
                                    subRs.getString("specialization"),
                                    subRs.getString("certification"),
                                    subRs.getInt("experience_years"),
                                    subRs.getDouble("rating"),
                                    subRs.getString("bio")
                            );
                            coach.setActive(active);
                            coach.setCreatedAt(createdAt);
                            return coach;
                        }
                    }
                }
                CoachUser coach = new CoachUser(id, name, email, passwordHash, "Personal Trainer", "Certified", 3, 5.0, "Fitness Trainer");
                coach.setActive(active);
                coach.setCreatedAt(createdAt);
                return coach;
            }
            case ADMIN: {
                String subSql = "SELECT * FROM admin_profiles WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(subSql)) {
                    ps.setInt(1, id);
                    try (ResultSet subRs = ps.executeQuery()) {
                        if (subRs.next()) {
                            AdminUser admin = new AdminUser(
                                    id, name, email, passwordHash,
                                    subRs.getString("department"),
                                    subRs.getInt("admin_level"),
                                    subRs.getBoolean("can_manage_admins")
                            );
                            admin.setActive(active);
                            admin.setCreatedAt(createdAt);
                            return admin;
                        }
                    }
                }
                AdminUser admin = new AdminUser(id, name, email, passwordHash, "System Administration", 1, false);
                admin.setActive(active);
                admin.setCreatedAt(createdAt);
                return admin;
            }
            default:
                return null;
        }
    }
}
