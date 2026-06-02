# ---- Build stage: compile the jar ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Cache dependencies first (only re-runs if pom changes)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -q -B dependency:go-offline

# Build the app
COPY src/ src/
RUN ./mvnw -q -B clean package -DskipTests

# ---- Runtime stage: slim image, just the jar ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Non-root user
RUN useradd -r -u 1001 appuser
COPY --from=build /app/target/*.jar app.jar
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
