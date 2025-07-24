# ---- Build Stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies first for better caching
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the application (skipping tests)
RUN mvn clean package -DskipTests

# ---- Runtime Stage ----
FROM eclipse-temurin:17-jdk-jammy

# Set working directory inside container
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/cycletime-service-0.0.1-SNAPSHOT.jar app.jar

# Expose service port
EXPOSE 8088

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
