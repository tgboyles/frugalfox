# frugal_fox
An intelligent, lightweight budgeting app.

## Prerequisites

- Java 25
- Maven 3.x
- Docker and Docker Compose

## Development Setup

### Database

Start the PostgreSQL database using Docker Compose:

```bash
docker compose up -d
```

This will start a PostgreSQL 17 container with:
- Database: `frugalfox`
- Username: `frugalfox`
- Password: `frugalfox`
- Port: `5432`

Stop the database:

```bash
docker compose down
```

Stop and remove all data:

```bash
docker compose down -v
```

## Building the Application

From the `backend` directory:

```bash
cd backend
mvn clean package
```

This will:
- Compile the source code
- Run all tests
- Create a WAR file in `target/frugalfox-0.0.1-SNAPSHOT.war`

To build without running tests:

```bash
mvn clean package -DskipTests
```

## Running Tests

Run all tests:

```bash
cd backend
mvn test
```

Run a specific test class:

```bash
mvn test -Dtest=HelloControllerTest
```

## Running the Application

### Option 1: Using Maven Spring Boot Plugin

```bash
cd backend
mvn spring-boot:run
```

### Option 2: Running the WAR file

First build the application, then run:

```bash
cd backend
java -jar target/frugalfox-0.0.1-SNAPSHOT.war
```

The application will start on `http://localhost:8080`

### Verify the Application

Once running, test the API:

```bash
curl http://localhost:8080/
```

Expected response: `Welcome to the Frugal Fox API!`

Check actuator health endpoint:

```bash
curl http://localhost:8080/actuator/health
```
