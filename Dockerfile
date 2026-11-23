# Multi-stage Dockerfile for User Authentication System
# Java 21 LTS runtime optimized for Render Free Tier (512MB RAM limits)

# Stage 1: Build application JAR with Java 21 LTS
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and POM for dependency pre-caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B || true

# Copy application source and build JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Minimal production runtime image with Java 21 LTS
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security best practice: Run as non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy built artifact from builder stage
COPY --from=builder --chown=spring:spring /app/target/auth-system-*.jar app.jar

# Dynamic port binding and production profile activation
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

# Native container healthcheck probe against /api/health
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/api/health || exit 1

# SerialGC and heap tuning strictly within 512MB RAM allocation
ENTRYPOINT ["java", "-XX:+UseSerialGC", "-Xmx384m", "-Xms128m", "-Xss512k", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
