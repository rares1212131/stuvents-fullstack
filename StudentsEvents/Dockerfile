# Stage 1: Build the application using a Maven image
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Create the final, smaller image
FROM openjdk:17.0.1-jdk-slim
WORKDIR /app
# Copy the built JAR file from the 'build' stage
COPY --from=build /app/target/*.jar app.jar
# Expose the port the app runs on
EXPOSE 8080
# The command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
