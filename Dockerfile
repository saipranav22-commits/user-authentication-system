# Multi-stage Dockerfile for User Authentication System
# Optimized for Render Free Tier (512MB RAM limits)

# Stage 1: Build application JAR
FROM eclipse-temurin:23-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and POM for dependency pre-caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B || true

# Copy application source and build JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Minimal production runtime image
FROM eclipse-temurin:23-jre-alpine
WORKDIR /app

# Security best practice: Run as non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy built artifact from builder stage
COPY --from=builder --chown=spring:spring /app/target/auth-system-*.jar app.jar

# Dynamic port binding for Render
ENV PORT=8080
EXPOSE 8080

# SerialGC and heap tuning strictly within 512MB RAM allocation
ENTRYPOINT ["java", "-XX:+UseSerialGC", "-Xmx384m", "-Xms128m", "-Xss512k", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
