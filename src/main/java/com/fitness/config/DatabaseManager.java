package com.fitness.config;

import com.fitness.exception.DatabaseOperationException;
import com.fitness.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Database Connection Manager & Lightweight Connection Pool.
 * Demonstrates:
 * - Database Connectivity (JDBC)
 * - Connection pooling and lifecycle management
 * - Transaction support (commit, rollback, savepoints)
 * - Schema initialization & automated seed data
 * - JDBC Metadata inspection
 */
public class DatabaseManager {

    private static volatile DatabaseManager instance;
    private final AppConfig config;
    private final BlockingQueue<Connection> connectionPool;
    private static final int POOL_SIZE = 10;

    private DatabaseManager() {
        this.config = AppConfig.getInstance();
        this.connectionPool = new ArrayBlockingQueue<>(POOL_SIZE);
        try {
            if (config.isPostgres()) {
                Class.forName("org.postgresql.Driver");
                System.out.println("[JDBC] Loaded PostgreSQL Driver for Neon Serverless Database.");
            } else {
                Class.forName("org.h2.Driver");
                System.out.println("[JDBC] Loaded H2 Embedded Database Driver.");
            }
            initializePool();
            initSchema();
            seedInitialDataIfEmpty();
        } catch (ClassNotFoundException e) {
            throw new DatabaseOperationException("JDBC Driver not found: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to initialize database connection or schema", e);
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    private void initializePool() throws SQLException {
        for (int i = 0; i < POOL_SIZE; i++) {
            Connection conn = DriverManager.getConnection(
                    config.getDbUrl(), config.getDbUser(), config.getDbPassword());
            connectionPool.offer(conn);
        }
        System.out.println("[JDBC] Initialized connection pool with " + POOL_SIZE + " connections.");
    }

    /**
     * Obtains a JDBC Connection from the pool.
     */
    public Connection getConnection() {
        try {
            Connection conn = connectionPool.poll();
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(
                        config.getDbUrl(), config.getDbUser(), config.getDbPassword());
            }
            return conn;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not obtain JDBC connection from pool", e);
        }
    }

    /**
     * Returns a connection back to the pool.
     */
    public void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    if (!conn.getAutoCommit()) {
                        conn.setAutoCommit(true);
                    }
                    if (!connectionPool.offer(conn)) {
                        conn.close();
                    }
                }
            } catch (SQLException e) {
                try {
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Initializes relational schema using standard DDL statements.
     */
    public void initSchema() throws SQLException {
        boolean isPg = config.isPostgres();
        String idCol = isPg ? "id SERIAL PRIMARY KEY" : "id INT AUTO_INCREMENT PRIMARY KEY";
        String dbl = isPg ? "DOUBLE PRECISION" : "DOUBLE";

        String[] ddlStatements = {
                // Users core table
                "CREATE TABLE IF NOT EXISTS users (" +
                        "  " + idCol + "," +
                        "  name VARCHAR(100) NOT NULL," +
                        "  email VARCHAR(150) NOT NULL UNIQUE," +
                        "  password_hash VARCHAR(255) NOT NULL," +
                        "  role VARCHAR(20) NOT NULL," +
                        "  is_active BOOLEAN DEFAULT TRUE," +
                        "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")",

                // Client specialized table
                "CREATE TABLE IF NOT EXISTS client_profiles (" +
                        "  user_id INT PRIMARY KEY," +
                        "  fitness_goal VARCHAR(100)," +
                        "  current_weight_kg " + dbl + " DEFAULT 70.0," +
                        "  target_weight_kg " + dbl + " DEFAULT 65.0," +
                        "  height_cm " + dbl + " DEFAULT 170.0," +
                        "  assigned_coach_id INT," +
                        "  activity_level VARCHAR(50) DEFAULT 'Moderate'," +
                        "  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                        ")",

                // Coach specialized table
                "CREATE TABLE IF NOT EXISTS coach_profiles (" +
                        "  user_id INT PRIMARY KEY," +
                        "  specialization VARCHAR(100)," +
                        "  certification VARCHAR(150)," +
                        "  experience_years INT DEFAULT 3," +
                        "  rating " + dbl + " DEFAULT 4.8," +
                        "  bio VARCHAR(500)," +
                        "  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                        ")",

                // Admin specialized table
                "CREATE TABLE IF NOT EXISTS admin_profiles (" +
                        "  user_id INT PRIMARY KEY," +
                        "  department VARCHAR(100)," +
                        "  admin_level INT DEFAULT 1," +
                        "  can_manage_admins BOOLEAN DEFAULT FALSE," +
                        "  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                        ")",

                // Workout plans core table
                "CREATE TABLE IF NOT EXISTS workout_plans (" +
                        "  " + idCol + "," +
                        "  coach_id INT," +
                        "  title VARCHAR(150) NOT NULL," +
                        "  description VARCHAR(1000)," +
                        "  plan_type VARCHAR(20) NOT NULL," +
                        "  difficulty_level VARCHAR(20) NOT NULL," +
                        "  duration_weeks INT NOT NULL," +
                        "  est_duration_minutes INT NOT NULL," +
                        "  base_calories " + dbl + " NOT NULL," +
                        "  is_active BOOLEAN DEFAULT TRUE," +
                        "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "  FOREIGN KEY (coach_id) REFERENCES users(id) ON DELETE SET NULL" +
                        ")",

                // Strength plan details
                "CREATE TABLE IF NOT EXISTS strength_plans (" +
                        "  plan_id INT PRIMARY KEY," +
                        "  target_muscle_group VARCHAR(100)," +
                        "  recommended_sets INT DEFAULT 3," +
                        "  recommended_reps INT DEFAULT 12," +
                        "  rest_sec INT DEFAULT 60," +
                        "  FOREIGN KEY (plan_id) REFERENCES workout_plans(id) ON DELETE CASCADE" +
                        ")",

                // Cardio plan details
                "CREATE TABLE IF NOT EXISTS cardio_plans (" +
                        "  plan_id INT PRIMARY KEY," +
                        "  cardio_type VARCHAR(50)," +
                        "  hr_zone VARCHAR(50)," +
                        "  distance_km " + dbl + " DEFAULT 5.0," +
                        "  target_pace " + dbl + " DEFAULT 5.5," +
                        "  FOREIGN KEY (plan_id) REFERENCES workout_plans(id) ON DELETE CASCADE" +
                        ")",

                // HIIT plan details
                "CREATE TABLE IF NOT EXISTS hiit_plans (" +
                        "  plan_id INT PRIMARY KEY," +
                        "  work_sec INT DEFAULT 45," +
                        "  rest_sec INT DEFAULT 15," +
                        "  num_rounds INT DEFAULT 5," +
                        "  intensity_rating INT DEFAULT 8," +
                        "  FOREIGN KEY (plan_id) REFERENCES workout_plans(id) ON DELETE CASCADE" +
                        ")",

                // Enrolled plans table
                "CREATE TABLE IF NOT EXISTS client_enrolled_plans (" +
                        "  client_id INT NOT NULL," +
                        "  plan_id INT NOT NULL," +
                        "  enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "  status VARCHAR(20) DEFAULT 'ACTIVE'," +
                        "  PRIMARY KEY (client_id, plan_id)," +
                        "  FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE CASCADE," +
                        "  FOREIGN KEY (plan_id) REFERENCES workout_plans(id) ON DELETE CASCADE" +
                        ")",

                // Progress logs table
                "CREATE TABLE IF NOT EXISTS progress_records (" +
                        "  " + idCol + "," +
                        "  client_id INT NOT NULL," +
                        "  workout_plan_id INT," +
                        "  current_weight_kg " + dbl + " NOT NULL," +
                        "  duration_minutes INT NOT NULL," +
                        "  calories_burned " + dbl + " NOT NULL," +
                        "  notes VARCHAR(500)," +
                        "  recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "  FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE CASCADE," +
                        "  FOREIGN KEY (workout_plan_id) REFERENCES workout_plans(id) ON DELETE SET NULL" +
                        ")",

                // Coach feedback messages
                "CREATE TABLE IF NOT EXISTS coach_feedback (" +
                        "  " + idCol + "," +
                        "  coach_id INT NOT NULL," +
                        "  client_id INT NOT NULL," +
                        "  message VARCHAR(1000) NOT NULL," +
                        "  plan_recommendation VARCHAR(255)," +
                        "  rating_given INT DEFAULT 5," +
                        "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "  FOREIGN KEY (coach_id) REFERENCES users(id) ON DELETE CASCADE," +
                        "  FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE CASCADE" +
                        ")",

                // Audit logs
                "CREATE TABLE IF NOT EXISTS audit_logs (" +
                        "  " + idCol + "," +
                        "  admin_id INT," +
                        "  admin_email VARCHAR(150)," +
                        "  action_type VARCHAR(50) NOT NULL," +
                        "  details VARCHAR(1000)," +
                        "  ip_address VARCHAR(50)," +
                        "  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")"
        };

        Connection conn = getConnection();
        try {
            for (String sql : ddlStatements) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                }
            }
            System.out.println("[JDBC] Database schema verified and initialized.");
        } finally {
            releaseConnection(conn);
        }
    }

    /**
     * Seeds initial records (Admin, Coaches, Clients, Workout Plans, Feedback) if users table is empty.
     */
    private void seedInitialDataIfEmpty() {
        Connection conn = getConnection();
        try {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return; // Already seeded
                }
            }

            conn.setAutoCommit(false); // Transaction demo

            // 1. Seed Admin
            int adminId = insertUser(conn, "Administrator", "admin@fitness.com",
                    PasswordUtil.hashPassword("admin123"), "ADMIN");
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO admin_profiles (user_id, department, admin_level, can_manage_admins) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, adminId);
                ps.setString(2, "Operations & Security");
                ps.setInt(3, 3);
                ps.setBoolean(4, true);
                ps.executeUpdate();
            }

            // 2. Seed Coaches
            int coach1Id = insertUser(conn, "Coach Sarah Jenkins", "sarah.coach@fitness.com",
                    PasswordUtil.hashPassword("coach123"), "COACH");
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO coach_profiles (user_id, specialization, certification, experience_years, rating, bio) VALUES (?, ?, ?, ?, ?, ?)")) {
                ps.setInt(1, coach1Id);
                ps.setString(2, "Strength & Hypertrophy Conditioning");
                ps.setString(3, "CSCS, NASM-CPT");
                ps.setInt(4, 7);
                ps.setDouble(5, 4.9);
                ps.setString(6, "Former Olympic weightlifting trainer passionate about strength and functional mobility.");
                ps.executeUpdate();
            }

            int coach2Id = insertUser(conn, "Coach Mike Reynolds", "mike.coach@fitness.com",
                    PasswordUtil.hashPassword("coach123"), "COACH");
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO coach_profiles (user_id, specialization, certification, experience_years, rating, bio) VALUES (?, ?, ?, ?, ?, ?)")) {
                ps.setInt(1, coach2Id);
                ps.setString(2, "Cardio Endurance & HIIT Specialist");
                ps.setString(3, "ACE, USATF Level 2");
                ps.setInt(4, 5);
                ps.setDouble(5, 4.8);
                ps.setString(6, "Marathoner and triathlon coach specializing in VO2 max and rapid fat loss.");
                ps.executeUpdate();
            }

            // 3. Seed Clients
            int client1Id = insertUser(conn, "Alice Green", "alice.client@fitness.com",
                    PasswordUtil.hashPassword("client123"), "CLIENT");
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO client_profiles (user_id, fitness_goal, current_weight_kg, target_weight_kg, height_cm, assigned_coach_id, activity_level) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                ps.setInt(1, client1Id);
                ps.setString(2, "Weight Loss & Core Toning");
                ps.setDouble(3, 72.5);
                ps.setDouble(4, 62.0);
                ps.setDouble(5, 168.0);
                ps.setInt(6, coach2Id);
                ps.setString(7, "Moderate");
                ps.executeUpdate();
            }

            int client2Id = insertUser(conn, "Bob Martin", "bob.client@fitness.com",
                    PasswordUtil.hashPassword("client123"), "CLIENT");
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO client_profiles (user_id, fitness_goal, current_weight_kg, target_weight_kg, height_cm, assigned_coach_id, activity_level) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                ps.setInt(1, client2Id);
                ps.setString(2, "Muscle Hypertrophy & Powerlifting");
                ps.setDouble(3, 80.0);
                ps.setDouble(4, 85.0);
                ps.setDouble(5, 180.0);
                ps.setInt(6, coach1Id);
                ps.setString(7, "Active");
                ps.executeUpdate();
            }

            // 4. Seed Workout Plans
            // Plan 1: Strength
            int plan1Id = insertPlan(conn, coach1Id, "Full-Body Strength Accelerator",
                    "A systematic compound movement program designed to maximize muscle fiber recruitment and tendon density.",
                    "STRENGTH", "INTERMEDIATE", 8, 55, 380.0);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO strength_plans (plan_id, target_muscle_group, recommended_sets, recommended_reps, rest_sec) VALUES (?, ?, ?, ?, ?)")) {
                ps.setInt(1, plan1Id);
                ps.setString(2, "Full Body & Posterior Chain");
                ps.setInt(3, 4);
                ps.setInt(4, 10);
                ps.setInt(5, 75);
                ps.executeUpdate();
            }

            // Plan 2: Cardio
            int plan2Id = insertPlan(conn, coach2Id, "5K Aerobic Endurance & Fat Burn",
                    "Progressive cardiovascular interval and steady-state runs to develop mitochondrial efficiency.",
                    "CARDIO", "BEGINNER", 6, 40, 320.0);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO cardio_plans (plan_id, cardio_type, hr_zone, distance_km, target_pace) VALUES (?, ?, ?, ?, ?)")) {
                ps.setInt(1, plan2Id);
                ps.setString(2, "Running");
                ps.setString(3, "Zone 2 (Aerobic Base)");
                ps.setDouble(4, 5.0);
                ps.setDouble(5, 6.0);
                ps.executeUpdate();
            }

            // Plan 3: HIIT
            int plan3Id = insertPlan(conn, coach2Id, "Metabolic Inferno HIIT",
                    "Explosive bodyweight and kettlebell intervals triggering maximum Excess Post-Exercise Oxygen Consumption (EPOC).",
                    "HIIT", "ADVANCED", 4, 30, 420.0);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO hiit_plans (plan_id, work_sec, rest_sec, num_rounds, intensity_rating) VALUES (?, ?, ?, ?, ?)")) {
                ps.setInt(1, plan3Id);
                ps.setInt(2, 45);
                ps.setInt(3, 15);
                ps.setInt(4, 6);
                ps.setInt(5, 9);
                ps.executeUpdate();
            }

            // 5. Enrollments
            enroll(conn, client1Id, plan2Id);
            enroll(conn, client2Id, plan1Id);

            // 6. Progress Records
            insertProgress(conn, client1Id, plan2Id, 72.0, 40, 330.0, "Great morning run, maintained steady breathing.");
            insertProgress(conn, client1Id, plan2Id, 71.6, 42, 350.0, "Increased pace in final kilometer.");
            insertProgress(conn, client2Id, plan1Id, 80.2, 55, 410.0, "Hit personal best on deadlifts 4x10.");

            // 7. Coach Feedback
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO coach_feedback (coach_id, client_id, message, plan_recommendation, rating_given) VALUES (?, ?, ?, ?, ?)")) {
                ps.setInt(1, coach2Id);
                ps.setInt(2, client1Id);
                ps.setString(3, "Superb pacing on your recent runs, Alice! Remember to hydrate thoroughly and stretch your hamstrings.");
                ps.setString(4, "Continue 5K Aerobic Endurance; add 5 min cool-down walks.");
                ps.setInt(5, 5);
                ps.executeUpdate();

                ps.setInt(1, coach1Id);
                ps.setInt(2, client2Id);
                ps.setString(3, "Great deadlift numbers, Bob! Keep your spinal alignment neutral during the 4th set.");
                ps.setString(4, "Focus on explosive drive through heels.");
                ps.setInt(5, 5);
                ps.executeUpdate();
            }

            // 8. Initial Audit Log
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO audit_logs (admin_id, admin_email, action_type, details, ip_address) VALUES (?, ?, ?, ?, ?)")) {
                ps.setInt(1, adminId);
                ps.setString(2, "admin@fitness.com");
                ps.setString(3, "SYSTEM_INIT");
                ps.setString(4, "Platform initialized with seed coaches, clients, and workout plans.");
                ps.setString(5, "127.0.0.1");
                ps.executeUpdate();
            }

            conn.commit(); // Transaction commit!
            System.out.println("[JDBC] Initial platform data seeded successfully via JDBC transaction.");

        } catch (SQLException e) {
            try {
                conn.rollback();
                System.err.println("[JDBC Transaction] Rollback executed due to error: " + e.getMessage());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw new DatabaseOperationException("Failed to seed initial data", e);
        } finally {
            releaseConnection(conn);
        }
    }

    private int insertUser(Connection conn, String name, String email, String passwordHash, String role) throws SQLException {
        String sql = "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.setString(4, role);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to retrieve generated user ID");
    }

    private int insertPlan(Connection conn, int coachId, String title, String desc,
                           String planType, String difficulty, int weeks, int estMin, double cal) throws SQLException {
        String sql = "INSERT INTO workout_plans (coach_id, title, description, plan_type, difficulty_level, duration_weeks, est_duration_minutes, base_calories) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, coachId);
            ps.setString(2, title);
            ps.setString(3, desc);
            ps.setString(4, planType);
            ps.setString(5, difficulty);
            ps.setInt(6, weeks);
            ps.setInt(7, estMin);
            ps.setDouble(8, cal);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to retrieve generated plan ID");
    }

    private void enroll(Connection conn, int clientId, int planId) throws SQLException {
        String sql = "INSERT INTO client_enrolled_plans (client_id, plan_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            ps.setInt(2, planId);
            ps.executeUpdate();
        }
    }

    private void insertProgress(Connection conn, int clientId, int planId, double weight,
                                int durationMin, double cal, String notes) throws SQLException {
        String sql = "INSERT INTO progress_records (client_id, workout_plan_id, current_weight_kg, duration_minutes, calories_burned, notes) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            ps.setInt(2, planId);
            ps.setDouble(3, weight);
            ps.setInt(4, durationMin);
            ps.setDouble(5, cal);
            ps.setString(6, notes);
            ps.executeUpdate();
        }
    }

    /**
     * Inspects JDBC metadata for administrative diagnostics.
     */
    public List<String> getDatabaseMetadataTables() {
        List<String> tables = new ArrayList<>();
        Connection conn = getConnection();
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to read JDBC DatabaseMetaData", e);
        } finally {
            releaseConnection(conn);
        }
        return tables;
    }

    public void close() {
        for (Connection conn : connectionPool) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ignored) {}
        }
        System.out.println("[JDBC] Database connection pool closed.");
    }
}
