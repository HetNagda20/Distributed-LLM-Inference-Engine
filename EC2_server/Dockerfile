# Build stage
FROM hseeberger/scala-sbt:11.0.12_1.5.5_2.13.6 AS builder

WORKDIR /app
COPY build.sbt ./
COPY project ./project/
COPY src ./src/

# Build the application
RUN sbt clean compile assembly

# Runtime stage
FROM openjdk:11-jre-slim

WORKDIR /app
# Create logs directory
RUN mkdir -p /app/logs

# Copy the built jar and configs
COPY --from=builder /app/target/scala-2.13/*assembly*.jar ./app.jar
COPY src/main/resources/application.conf ./application.conf
COPY src/main/resources/logback.xml ./logback.xml

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application with explicit config
CMD ["java", "-Dconfig.file=application.conf", "-Dlogback.configurationFile=logback.xml", "-jar", "app.jar"]