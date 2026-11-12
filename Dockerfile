# Stage 1: Build the application with Maven
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy Maven files first for optimal dependency caching
COPY pom.xml ./
COPY src ./src

# Build the production executable JAR
RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests

# Stage 2: Lightweight runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create a non-privileged system user for security
RUN groupadd -r campus && useradd -r -g campus campus

# Copy the built JAR from the builder stage
COPY --from=build /app/target/campus-connect-*.jar app.jar

# Ensure upload directory exists and is writable
RUN mkdir -p uploads && chown -R campus:campus /app

USER campus

# Cloud platforms (Render, Railway, Cloud Run, Heroku) supply PORT via environment variable
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dserver.port=${PORT}", "-jar", "app.jar"]
