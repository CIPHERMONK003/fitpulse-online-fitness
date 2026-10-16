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
                        ")",

                // Spa & Wellness specialized plan table
                "CREATE TABLE IF NOT EXISTS spa_plans (" +
                        "  plan_id INT PRIMARY KEY," +
                        "  spa_category VARCHAR(100)," +
                        "  relaxation_score INT DEFAULT 8," +
                        "  benefits VARCHAR(500)," +
                        "  room_type VARCHAR(100)," +
                        "  temperature_c " + dbl + " DEFAULT 37.0," +
                        "  aromatherapy_oil VARCHAR(100)," +
                        "  FOREIGN KEY (plan_id) REFERENCES workout_plans(id) ON DELETE CASCADE" +
                        ")",

                // Session Slots table for real-time slot booking
                "CREATE TABLE IF NOT EXISTS session_slots (" +
                        "  " + idCol + "," +
                        "  session_type VARCHAR(50) NOT NULL," +
                        "  title VARCHAR(150) NOT NULL," +
                        "  coach_id INT," +
                        "  session_date VARCHAR(20) NOT NULL," +
                        "  start_time VARCHAR(20) NOT NULL," +
                        "  end_time VARCHAR(20) NOT NULL," +
                        "  max_capacity INT NOT NULL DEFAULT 1," +
                        "  booked_count INT NOT NULL DEFAULT 0," +
                        "  room_location VARCHAR(100)," +
                        "  price_credits INT DEFAULT 0," +
                        "  is_active BOOLEAN DEFAULT TRUE," +
                        "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "  FOREIGN KEY (coach_id) REFERENCES users(id) ON DELETE SET NULL" +
                        ")",

                // Slot Bookings table
                "CREATE TABLE IF NOT EXISTS slot_bookings (" +
                        "  " + idCol + "," +
                        "  slot_id INT NOT NULL," +
                        "  client_id INT NOT NULL," +
                        "  status VARCHAR(20) DEFAULT 'CONFIRMED'," +
                        "  notes VARCHAR(500)," +
                        "  booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "  FOREIGN KEY (slot_id) REFERENCES session_slots(id) ON DELETE CASCADE," +
                        "  FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE CASCADE" +
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
            seedSessionSlotsIfEmpty(conn);
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

    public void seedSessionSlotsIfEmpty(Connection conn) {
        try {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM session_slots")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return; // Already seeded
                }
            }

            Integer coachSarah = findUserIdByEmail(conn, "sarah.coach@fitness.com");
            Integer coachMarcus = findUserIdByEmail(conn, "marcus.coach@fitness.com");
            Integer therapistMaya = findUserIdByEmail(conn, "maya.therapist@fitness.com");
            Integer coachElena = findUserIdByEmail(conn, "elena.coach@fitness.com");
            Integer coachDavid = findUserIdByEmail(conn, "david.coach@fitness.com");

            if (therapistMaya == null) {
                therapistMaya = insertUser(conn, "Therapist Maya Patel", "maya.therapist@fitness.com",
                        PasswordUtil.hashPassword("coach123"), "COACH");
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO coach_profiles (user_id, specialization, certification, experience_years, rating, bio) VALUES (?, ?, ?, ?, ?, ?)")) {
                    ps.setInt(1, therapistMaya);
                    ps.setString(2, "Licensed Spa, Cryo & Hydrotherapy Recovery Specialist");
                    ps.setString(3, "LMT, NCBTMB Certified");
                    ps.setInt(4, 9);
                    ps.setDouble(5, 5.0);
                    ps.setString(6, "Specializing in athletic muscle restoration, deep tissue myofascial release, and contrast hydrotherapy.");
                    ps.executeUpdate();
                }
            }

            if (coachMarcus == null) {
                coachMarcus = insertUser(conn, "Coach Marcus Thorne", "marcus.coach@fitness.com",
                        PasswordUtil.hashPassword("coach123"), "COACH");
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO coach_profiles (user_id, specialization, certification, experience_years, rating, bio) VALUES (?, ?, ?, ?, ?, ?)")) {
                    ps.setInt(1, coachMarcus);
                    ps.setString(2, "HIIT & Tactical Conditioning");
                    ps.setString(3, "CrossFit L3, CSCS");
                    ps.setInt(4, 6);
                    ps.setDouble(5, 4.9);
                    ps.setString(6, "High-intensity athletic conditioning, kettlebell power, and metabolic acceleration.");
                    ps.executeUpdate();
                }
            }

            if (coachElena == null) {
                coachElena = insertUser(conn, "Coach Elena Rostova", "elena.coach@fitness.com",
                        PasswordUtil.hashPassword("coach123"), "COACH");
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO coach_profiles (user_id, specialization, certification, experience_years, rating, bio) VALUES (?, ?, ?, ?, ?, ?)")) {
                    ps.setInt(1, coachElena);
                    ps.setString(2, "Master Yoga, Mobility & Postural Alignment");
                    ps.setString(3, "E-RYT 500, FMS Certified");
                    ps.setInt(4, 8);
                    ps.setDouble(5, 4.9);
                    ps.setString(6, "Expert in spinal decompressive yoga, deep hip mobility, and mindful athletic recovery.");
                    ps.executeUpdate();
                }
            }

            if (coachDavid == null) {
                coachDavid = insertUser(conn, "Coach David Vance", "david.coach@fitness.com",
                        PasswordUtil.hashPassword("coach123"), "COACH");
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO coach_profiles (user_id, specialization, certification, experience_years, rating, bio) VALUES (?, ?, ?, ?, ?, ?)")) {
                    ps.setInt(1, coachDavid);
                    ps.setString(2, "Endurance Running & VO2 Max Coaching");
                    ps.setString(3, "USATF, UESCA Ultra Running");
                    ps.setInt(4, 7);
                    ps.setDouble(5, 4.8);
                    ps.setString(6, "Marathoner and endurance physiologist optimizing aerobic thresholds and running biomechanics.");
                    ps.executeUpdate();
                }
            }

            // Seed Spa Plans
            int spaPlan1 = insertPlan(conn, therapistMaya, "Nordic Contrast Hydrotherapy & Finnish Sauna",
                    "Alternating 80°C Finnish dry sauna with 10°C sub-zero cold plunge pools to induce vascular flushing and deep parasympathetic recovery.",
                    "SPA_WELLNESS", "BEGINNER", 4, 60, 240.0);
            insertSpaPlan(conn, spaPlan1, "Hydrotherapy & Sauna", 9, "Vascular flushing, cortisol reduction, lactic clearance", "Nordic Thermal Bath Suite", 82.0, "Nordic Pine & Eucalyptus");

            int spaPlan2 = insertPlan(conn, therapistMaya, "Swedish Deep Tissue Muscle Reset",
                    "60-minute targeted deep pressure therapy with heated basalt stones, myofascial trigger point release, and organic lavender botanicals.",
                    "SPA_WELLNESS", "BEGINNER", 2, 60, 180.0);
            insertSpaPlan(conn, spaPlan2, "Deep Tissue Massage", 10, "Fascial realignment, muscle knot dissolution, joint mobility", "Zen Luxury Suite 1", 37.5, "French Lavender & Arnica");

            int spaPlan3 = insertPlan(conn, therapistMaya, "Sub-Zero Whole Body Cryotherapy (-110°C)",
                    "3-minute liquid nitrogen vapor immersion triggering acute vasoconstriction, systemic anti-inflammatory endorphin rush, and tissue repair.",
                    "SPA_WELLNESS", "INTERMEDIATE", 3, 20, 290.0);
            insertSpaPlan(conn, spaPlan3, "Cryotherapy", 8, "Acute inflammation suppression, metabolic rate surge, endorphin elevation", "Cryo Recovery Chamber A", -110.0, "Menthol & Peppermint");

            // Seed Session Slots
            int slot1 = insertSlot(conn, "SPA", "Swedish Deep Tissue Recovery Massage", therapistMaya, "Today", "10:00 AM", "11:00 AM", 1, 0, "Zen Luxury Suite 1", 50);
            int slot2 = insertSlot(conn, "SPA", "Nordic Contrast Hydrotherapy & Sauna Soak", therapistMaya, "Today", "02:00 PM", "03:00 PM", 6, 2, "Nordic Thermal Bath Suite", 35);
            int slot3 = insertSlot(conn, "SPA", "Sub-Zero Whole Body Cryo Therapy Session", therapistMaya, "Tomorrow", "11:30 AM", "12:00 PM", 2, 1, "Cryo Recovery Chamber A", 45);
            int slot4 = insertSlot(conn, "SPA", "Hot Basalt Stone Spinal Release Therapy", therapistMaya, "Tomorrow", "04:30 PM", "05:30 PM", 1, 0, "Zen Luxury Suite 2", 55);

            int slot5 = insertSlot(conn, "COACHING", "1-on-1 Olympic Barbell Technique & Form Clinic", coachSarah, "Today", "09:00 AM", "10:00 AM", 1, 0, "Powerlifting Platform 1", 60);
            int slot6 = insertSlot(conn, "COACHING", "Tactical HIIT Athletic Conditioning (Small Group)", coachMarcus, "Today", "05:30 PM", "06:30 PM", 8, 4, "Functional Turf Arena", 30);
            int slot7 = insertSlot(conn, "COACHING", "Spinal Decompression & Vinyasa Flow Yoga", coachElena, "Tomorrow", "08:00 AM", "09:00 AM", 12, 6, "Harmony Sky Studio", 25);
            int slot8 = insertSlot(conn, "COACHING", "Endurance VO2 Max Aerobic Threshold Run", coachDavid, "Tomorrow", "06:00 PM", "07:00 PM", 6, 2, "Indoor High-Speed Track", 35);

            // Seed sample bookings for clients
            Integer clientAlice = findUserIdByEmail(conn, "alice.client@fitness.com");
            Integer clientBob = findUserIdByEmail(conn, "bob.client@fitness.com");
            if (clientAlice != null) {
                insertBooking(conn, slot2, clientAlice, "CONFIRMED", "Looking forward to recovery after running training.");
                insertBooking(conn, slot7, clientAlice, "CONFIRMED", "Morning mobility practice.");
            }
            if (clientBob != null) {
                insertBooking(conn, slot2, clientBob, "CONFIRMED", "Post heavy squat day soak.");
                insertBooking(conn, slot6, clientBob, "CONFIRMED", "MetCon conditioning.");
            }

            System.out.println("[JDBC] Seeded Spa plans, diverse coaches, and real-time session slots successfully.");
        } catch (SQLException e) {
            System.err.println("[JDBC] Warning during session slot seeding: " + e.getMessage());
        }
    }

    private Integer findUserIdByEmail(Connection conn, String email) throws SQLException {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }

    private void insertSpaPlan(Connection conn, int planId, String cat, int relaxScore,
                               String benefits, String room, double tempC, String oil) throws SQLException {
        String sql = "INSERT INTO spa_plans (plan_id, spa_category, relaxation_score, benefits, room_type, temperature_c, aromatherapy_oil) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planId);
            ps.setString(2, cat);
            ps.setInt(3, relaxScore);
            ps.setString(4, benefits);
            ps.setString(5, room);
            ps.setDouble(6, tempC);
            ps.setString(7, oil);
            ps.executeUpdate();
        }
    }

    private int insertSlot(Connection conn, String type, String title, Integer coachId,
                           String date, String start, String end, int maxCap, int booked,
                           String room, int price) throws SQLException {
        String sql = "INSERT INTO session_slots (session_type, title, coach_id, session_date, start_time, end_time, max_capacity, booked_count, room_location, price_credits) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, type);
            ps.setString(2, title);
            if (coachId != null) {
                ps.setInt(3, coachId);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, date);
            ps.setString(5, start);
            ps.setString(6, end);
            ps.setInt(7, maxCap);
            ps.setInt(8, booked);
            ps.setString(9, room);
            ps.setInt(10, price);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to retrieve generated slot ID");
    }

    private void insertBooking(Connection conn, int slotId, int clientId, String status, String notes) throws SQLException {
        String sql = "INSERT INTO slot_bookings (slot_id, client_id, status, notes) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            ps.setInt(2, clientId);
            ps.setString(3, status);
            ps.setString(4, notes);
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
