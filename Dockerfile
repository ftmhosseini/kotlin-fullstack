# ─────────────────────────────────────────────────────────────────────────────
# Stage 1 — BUILD
# Uses a Gradle + JDK image to compile the project
# ─────────────────────────────────────────────────────────────────────────────
FROM gradle:8.8-jdk21 AS build

WORKDIR /app

# Copy Gradle config files first (cached layer — only re-runs if these change)
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle
COPY shared/build.gradle.kts ./shared/build.gradle.kts
COPY server/build.gradle.kts ./server/build.gradle.kts

# Copy source code
COPY shared/src ./shared/src
COPY server/src ./server/src

# Build the server JAR (skip tests for faster build)
RUN ./gradlew :server:bootJar -x test --no-daemon

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2 — RUN
# Lightweight JRE image — no build tools, much smaller final image
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy only the built JAR from the build stage
COPY --from=build /app/server/build/libs/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Start the server
ENTRYPOINT ["java", "-jar", "app.jar"]
