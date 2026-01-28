# ===============================
# BUILD STAGE
# ===============================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the Spring Boot fat jar
RUN mvn clean package -DskipTests




# ===============================
# RUNTIME STAGE
# ===============================
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy only the runnable jar (NOT .original)
COPY --from=build /app/target/ip-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]