# Build stage
FROM maven:3.9-eclipse-temurin-23 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package

# Runtime stage
FROM eclipse-temurin:23-jre-alpine
WORKDIR /app

# Copy the WAR file from build stage
COPY --from=build /app/target/frugalfox-*.war app.war

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.war"]
