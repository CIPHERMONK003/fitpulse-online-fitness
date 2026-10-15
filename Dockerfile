# ==============================================================================
# Multi-stage Dockerfile for FitPulse Online Fitness Coaching Platform
# Ultra-lightweight, production-ready, globally deployable container image
# ==============================================================================

# Stage 1: Build Environment
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production shaded fat JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal Runtime Environment
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S fitness && adduser -S fitness -G fitness

# Copy the standalone executable Fat JAR
COPY --from=builder /build/target/fitpulse-global-app.jar /app/app.jar

# Setup data directory with write permissions for persistent H2 database
RUN mkdir -p /app/data && chown -R fitness:fitness /app

USER fitness

# Standard Cloud Port (Overridable via $PORT environment variable)
ENV PORT=8080
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:${PORT}/api/system-stats || exit 1

# Launch standalone enterprise server
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
