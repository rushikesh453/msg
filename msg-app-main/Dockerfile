# Stage 1: Build the JAR
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies first (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Run the app
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java","-Dserver.port=${PORT}","-jar","/app.jar"]
