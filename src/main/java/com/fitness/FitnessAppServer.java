package com.fitness;

import com.fitness.config.AppConfig;
import com.fitness.config.DatabaseManager;
import com.fitness.concurrency.MaintenanceScheduler;
import com.fitness.concurrency.NotificationQueueService;
import com.fitness.filter.AuthenticationFilter;
import com.fitness.servlet.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 * Main Entry Point for the Online Fitness Coaching Platform.
 * Demonstrates:
 * - Embedded Servlet Container (Jetty)
 * - Servlet and Filter Configuration
 * - Multithreading background worker initialization
 * - Clean shutdown hooks
 */
public class FitnessAppServer {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("   FITPULSE - ONLINE FITNESS COACHING ENTERPRISE PLATFORM (JAVA)  ");
        System.out.println("==================================================================");

        AppConfig config = AppConfig.getInstance();
        int port = config.getServerPort();

        try {
            // 1. Initialize JDBC Database & Connection Pool
            System.out.println("[Bootstrap] Initializing JDBC Database Connection & Schema...");
            DatabaseManager dbManager = DatabaseManager.getInstance();

            // 2. Start Multithreaded Services (Producer-Consumer & Scheduled Maintenance)
            System.out.println("[Bootstrap] Starting Concurrency & Multithreading Engine...");
            NotificationQueueService.getInstance().start();
            MaintenanceScheduler.getInstance().start();

            // 3. Initialize Embedded Jetty Web Server
            Server server = new Server(port);
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);

            // 4. Register Filters
            context.addFilter(new FilterHolder(new AuthenticationFilter()), "/*", EnumSet.of(DispatcherType.REQUEST));

            // 5. Register Java Servlets
            context.addServlet(new ServletHolder(new HomeServlet()), "/");
            context.addServlet(new ServletHolder(new AuthServlet()), "/auth/*");
            context.addServlet(new ServletHolder(new DashboardServlet()), "/dashboard/*");
            context.addServlet(new ServletHolder(new DashboardServlet()), "/dashboard");
            context.addServlet(new ServletHolder(new ClientServlet()), "/client/*");
            context.addServlet(new ServletHolder(new ProgressServlet()), "/progress/*");
            context.addServlet(new ServletHolder(new CoachServlet()), "/coach/*");
            context.addServlet(new ServletHolder(new AdminServlet()), "/admin/*");
            context.addServlet(new ServletHolder(new ApiServlet()), "/api/*");

            // 6. Graceful Shutdown Hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n[Shutdown] Stopping background services and database connections...");
                try {
                    NotificationQueueService.getInstance().shutdown();
                    MaintenanceScheduler.getInstance().shutdown();
                    dbManager.close();
                    if (server.isRunning()) {
                        server.stop();
                    }
                    System.out.println("[Shutdown] Platform shutdown gracefully.");
                } catch (Exception e) {
                    System.err.println("[Shutdown] Error during shutdown: " + e.getMessage());
                }
            }));

            // 7. Start Server
            server.start();
            System.out.println("\n==================================================================");
            System.out.printf("   🚀 Server is LIVE on: http://localhost:%d%n", port);
            System.out.println("==================================================================");
            System.out.println("   Pre-seeded Demo Accounts:");
            System.out.println("   • Client: alice.client@fitness.com | Password: client123");
            System.out.println("   • Coach:  sarah.coach@fitness.com  | Password: coach123");
            System.out.println("   • Admin:  admin@fitness.com        | Password: admin123");
            System.out.println("==================================================================\n");

            server.join();

        } catch (Exception e) {
            System.err.println("[Fatal] Server failed to start: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
