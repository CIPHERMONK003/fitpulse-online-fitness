package com.fitness.config;

import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

/**
 * Platform Application Configuration.
 * Supports Embedded H2 Database (local/offline) and Neon PostgreSQL (cloud serverless).
 * Automatically parses Neon DATABASE_URL, POSTGRES_URL, or db.properties.
 */
public class AppConfig {

    private static final String DEFAULT_DB_URL = "jdbc:h2:./fitness_db;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE";
    private static final String DEFAULT_DB_USER = "sa";
    private static final String DEFAULT_DB_PASS = "";
    private static final int DEFAULT_PORT = 8080;

    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private int serverPort;

    private static final AppConfig INSTANCE = new AppConfig();

    private AppConfig() {
        loadProperties();
    }

    public static AppConfig getInstance() {
        return INSTANCE;
    }

    private void loadProperties() {
        this.dbUrl = DEFAULT_DB_URL;
        this.dbUser = DEFAULT_DB_USER;
        this.dbPassword = DEFAULT_DB_PASS;
        this.serverPort = DEFAULT_PORT;

        // 1. Load from db.properties if available
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                this.dbUrl = props.getProperty("db.url", DEFAULT_DB_URL);
                this.dbUser = props.getProperty("db.user", DEFAULT_DB_USER);
                this.dbPassword = props.getProperty("db.password", DEFAULT_DB_PASS);
                this.serverPort = Integer.parseInt(props.getProperty("server.port", String.valueOf(DEFAULT_PORT)));
            }
        } catch (Exception e) {
            System.out.println("[AppConfig] Using default configuration: " + e.getMessage());
        }

        // 2. Global Cloud Port Override ($PORT for Render, Railway, Heroku, Docker)
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            try {
                this.serverPort = Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {}
        }

        // 3. Neon Serverless Postgres / Cloud Database URL Overrides
        String rawDbUrl = System.getenv("DATABASE_URL");
        if (rawDbUrl == null || rawDbUrl.trim().isEmpty()) {
            rawDbUrl = System.getenv("NEON_DATABASE_URL");
        }
        if (rawDbUrl == null || rawDbUrl.trim().isEmpty()) {
            rawDbUrl = System.getenv("POSTGRES_URL");
        }

        if (rawDbUrl != null && !rawDbUrl.trim().isEmpty()) {
            parseAndSetDatabaseUrl(rawDbUrl.trim());
        }

        String envDbUser = System.getenv("DATABASE_USER");
        if (envDbUser != null && !envDbUser.trim().isEmpty()) this.dbUser = envDbUser;

        String envDbPass = System.getenv("DATABASE_PASSWORD");
        if (envDbPass != null && !envDbPass.trim().isEmpty()) this.dbPassword = envDbPass;
    }

    /**
     * Normalizes Neon PostgreSQL connection strings into JDBC format:
     * Converts "postgres://user:pass@ep-xyz.aws.neon.tech/neondb?sslmode=require"
     * into "jdbc:postgresql://ep-xyz.aws.neon.tech/neondb?sslmode=require"
     */
    private void parseAndSetDatabaseUrl(String rawUrl) {
        try {
            if (rawUrl.startsWith("jdbc:")) {
                this.dbUrl = rawUrl;
                return;
            }

            if (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://")) {
                String normalizedHttp = rawUrl.replaceFirst("^(postgres|postgresql)://", "http://");
                URI uri = new URI(normalizedHttp);

                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String[] creds = userInfo.split(":", 2);
                    this.dbUser = creds[0];
                    this.dbPassword = creds[1];
                }

                String host = uri.getHost();
                int port = uri.getPort();
                String hostPort = (port > 0) ? (host + ":" + port) : host;
                String path = (uri.getPath() != null && !uri.getPath().isEmpty()) ? uri.getPath() : "/neondb";
                String query = uri.getQuery();

                String queryPart = (query != null && !query.isEmpty()) ? ("?" + query) : "?sslmode=require";
                if (!queryPart.contains("sslmode=")) {
                    queryPart += (queryPart.contains("?") ? "&" : "?") + "sslmode=require";
                }

                this.dbUrl = "jdbc:postgresql://" + hostPort + path + queryPart;
                System.out.println("[AppConfig] Configured Neon PostgreSQL JDBC connection successfully.");
            } else {
                this.dbUrl = rawUrl;
            }
        } catch (Exception e) {
            System.err.println("[AppConfig] Error parsing database URL, using as-is: " + e.getMessage());
            this.dbUrl = rawUrl;
        }
    }

    public boolean isPostgres() {
        return dbUrl != null && dbUrl.startsWith("jdbc:postgresql:");
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public int getServerPort() {
        return serverPort;
    }
}
