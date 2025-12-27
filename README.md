# frugal_fox
An intelligent, lightweight budgeting app.

## Prerequisites

- Java 25 or higher
- Maven 3.x
- Docker and Docker Compose

## Development Setup

### Running with Docker Compose (Recommended)

#### General

The entire stack is configured to build and deploy via Docker Compose. You can start it thusly:

```bash
docker compose down
docker compose up --build --force-recreate 
```

This will start:
- **PostgreSQL 17** container on port `5432`
  - Database: `frugalfox`
  - Username: `frugalfox`
  - Password: `frugalfox`
- **Backend application** on port `8080`
  - Container will be completely rebuild
  - Tests will be run in first stage

#### Granular Docker Operations

View logs:

```bash
docker compose logs -f backend
```

Stop all services:

```bash
docker compose down
```

Stop and remove all data:

```bash
docker compose down -v
```

#### Database Only

If you want to run just the database (for local development):

```bash
docker compose up -d postgres
```

### Local Build Operations, Backend

#### Build

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

#### Test

Run all tests:

```bash
cd backend
mvn test
```

Run a specific test class:

```bash
mvn test -Dtest=HelloControllerTest
```

#### Run

You can start the backend app via Maven:

```bash
cd backend
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

#### Verify the Application

Once running, test the API:

```bash
curl http://localhost:8080/
```

Check actuator health endpoint:

```bash
curl http://localhost:8080/actuator/health
```
