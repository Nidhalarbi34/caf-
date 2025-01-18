FROM openjdk:17-slim-buster

# Create app user and group
RUN groupadd appgroup && useradd -g appgroup -s /bin/sh -m appuser

# Set working directory
WORKDIR /app

# Copy the jar and necessary resources into the container
COPY target/*.jar /app/cafe.jar
COPY src/main/resources/serviceAccountKey.json /app/src/main/resources/serviceAccountKey.json

# Change ownership of the files
RUN chown appuser:appgroup /app/cafe.jar

# Switch to the app user
USER appuser

# Expose the port
EXPOSE 8112

# Start the application
ENTRYPOINT ["java", "-jar", "/app/cafe.jar"]
