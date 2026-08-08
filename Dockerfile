# =============================================================================
# URL Shortener API — production image for Render (Docker runtime)
#
# Required Render environment variables:
#   SPRING_PROFILES_ACTIVE=prod
#   JDBC_URL          — Neon PostgreSQL JDBC URL (pooled)
#   JDBC_USERNAME     — Neon username
#   JDBC_PASSWORD     — Neon password
#   REDIS_HOST        — Upstash endpoint (hostname only, no https://)
#   REDIS_PASSWORD    — Upstash password
#   REDIS_PORT        — 6379
#   REDIS_SSL         — true
#   CORS_ALLOWED_ORIGINS — https://url-sharpener.netlify.app
#
# Render sets PORT automatically; application-prod.properties binds to it.
# =============================================================================

# --- Build stage ---
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline

COPY src src
RUN ./mvnw -q -B -DskipTests package

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /app/target/*.jar app.jar
RUN chown spring:spring app.jar

USER spring:spring

# Production profile; JVM tuned for Render free-tier containers (~512 MB RAM)
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Render overrides PORT at runtime (often 10000); 8080 is the local default
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
