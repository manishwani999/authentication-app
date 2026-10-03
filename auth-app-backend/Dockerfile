# =========================
# 1. Build Stage
# =========================
FROM maven:3.9.16-eclipse-temurin-21 AS build

# set working directory
WORKDIR /app

# Copy Maven configuration first
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the Spring Boot application
RUN mvn clean package -DskipTests

# =========================
# 2. Run Stage
# =========================
FROM eclipse-temurin:21-jre

# set working directory
WORKDIR /app

# Copy only the generated JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Spring Boot default port
EXPOSE 8080

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]