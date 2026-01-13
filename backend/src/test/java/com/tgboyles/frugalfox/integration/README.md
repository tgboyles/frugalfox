# Integration Tests

This directory contains REST Assured integration tests for the Frugal Fox backend API.

## Overview

The integration tests validate the API from an end-to-end perspective, testing:
- User authentication (registration and login)
- Expense CRUD operations
- Advanced search and filtering
- CSV import and export functionality
- User data isolation
- Error handling and validation

## Technology Stack

- **REST Assured 5.5.0**: API testing framework
- **JUnit 5**: Test runner
- **Spring Boot Test**: Integration with Spring Boot application
- **H2 Database**: In-memory database for testing

## Test Execution Modes

The integration tests support two execution modes:

### Mode 1: Embedded Server (Default)

Tests start their own Spring Boot application instance with an H2 database. This is the default mode and doesn't require any external services.

**Prerequisites:**
- Java 23 (required to compile the main application code)
- Maven

**Run Integration Tests:**

```bash
# From the backend directory
mvn verify

# Or run integration tests explicitly
mvn failsafe:integration-test failsafe:verify
```

### Mode 2: External Server (Staging/Production)

Tests can also run against an external running server (e.g., in Docker, staging environment).

**Note:** This mode requires modifying `BaseIntegrationTest` to disable `@SpringBootTest` auto-start and connect to an external URL. This is intentionally not the default to keep tests self-contained.

## Running Integration Tests

### Prerequisites

- Java 23
- Maven 3.6+

### Run Integration Tests Only

```bash
# From the backend directory
mvn verify

# Skip unit tests, run only integration tests
mvn verify -DskipTests

# Run integration tests explicitly
mvn failsafe:integration-test
```

### Run Both Unit and Integration Tests

```bash
mvn clean verify
```

### Run a Specific Integration Test

```bash
mvn verify -Dit.test=AuthenticationIT
mvn verify -Dit.test=ExpenseCrudIT
mvn verify -Dit.test=ExpenseSearchIT
mvn verify -Dit.test=CsvImportExportIT
```

### Skip Integration Tests

```bash
mvn clean package -DskipITs
```

## Using Docker for Testing

If you don't have Java 23 installed locally, you can use Docker:

```bash
# Build and run tests in Docker container
docker run --rm -v "$(pwd)":/app -w /app maven:3.9-eclipse-temurin-23 mvn verify

# Or use the backend Dockerfile to build the project (includes running tests)
cd .. && docker compose build backend
```

## Test Structure

### Base Classes

- **BaseIntegrationTest**: Abstract base class providing:
  - Spring Boot test configuration with random port
  - REST Assured setup and configuration
  - Data cleanup before each test
  - Utility methods for authentication and test data creation
  - Uses `@ActiveProfiles("test")` to load test-specific configuration

### Test Classes

- **AuthenticationIT**: Tests user registration and login flows
  - Valid registration
  - Duplicate username handling
  - Invalid credentials
  - Email validation

- **ExpenseCrudIT**: Tests expense CRUD operations
  - Create, read, update, delete expenses
  - User isolation (users can only access their own data)
  - Authorization checks
  - Validation errors

- **ExpenseSearchIT**: Tests expense search and filtering
  - Search by category, bank, merchant
  - Date range filtering
  - Amount range filtering
  - Multiple filter combinations
  - Pagination
  - User isolation

- **CsvImportExportIT**: Tests CSV import and export
  - Valid CSV import
  - Partial import with validation errors
  - Invalid file types
  - Export with filters
  - User isolation

## Test Patterns

### Authentication Pattern

```java
String username = generateUniqueUsername();
String token = registerAndGetToken(username, "password123", username + "@example.com");

given()
    .header("Authorization", "Bearer " + token)
    .when()
    .get("/expenses")
    .then()
    .statusCode(200);
```

### Data Setup and Cleanup

- Each test starts with a clean database (no leftover data from previous tests)
- Tests create their own test data using unique usernames to avoid conflicts
- The `@BeforeEach` method in `BaseIntegrationTest` cleans up all data

### User Isolation Testing

Many tests verify that users can only access their own data:

```java
// Create expense with user 1
String token1 = registerAndGetToken(username1, "password", email1);
Long expenseId = createExpense(token1, ...);

// Try to access with user 2
String token2 = registerAndGetToken(username2, "password", email2);
given()
    .header("Authorization", "Bearer " + token2)
    .when()
    .get("/expenses/" + expenseId)
    .then()
    .statusCode(404); // Not found - user isolation
```

## CI/CD Integration

These tests are designed to run in:
- Local development environments
- CI/CD pipelines (GitHub Actions, Jenkins, etc.)
- Docker containers
- Staging environments

The tests are self-contained and handle their own data setup/teardown, making them suitable for automated execution.

### GitHub Actions Example

```yaml
name: Integration Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 23
        uses: actions/setup-java@v3
        with:
          java-version: '23'
          distribution: 'temurin'
      - name: Run integration tests
        run: |
          cd backend
          mvn verify
```

## Troubleshooting

### Java Version Mismatch

**Error:** `release version 23 not supported`

**Solution:** Ensure Java 23 is installed and configured:
```bash
java -version  # Should show Java 23
export JAVA_HOME=/path/to/java23  # If needed
```

Or use Docker:
```bash
docker run --rm -v "$(pwd)":/app -w /app maven:3.9-eclipse-temurin-23 mvn verify
```

### Port Already in Use

If you see port conflicts, the tests use `RANDOM_PORT` mode to avoid conflicts. The port is automatically assigned by Spring Boot.

### Test Failures

- Check logs for detailed error messages
- REST Assured enables logging of failed requests automatically
- Ensure the database is clean before running tests (handled by `@BeforeEach`)
- Verify test application.properties is correctly configured

### Database Issues

The tests use H2 in-memory database with PostgreSQL compatibility mode. If you see Flyway migration errors, ensure:
- Migrations are compatible with both PostgreSQL and H2
- `MODE=PostgreSQL` is set in the H2 connection URL (already configured in test application.properties)

## Best Practices

- Each test is independent and can run in any order
- Tests generate unique usernames to avoid conflicts
- Tests clean up their own data
- Tests verify both positive and negative scenarios
- Tests check user isolation to ensure security
- Tests use descriptive names that explain what is being tested
- Integration test files end with `IT.java` (e.g., `AuthenticationIT.java`)

## Comparison with Unit Tests

| Aspect | Unit Tests | Integration Tests |
|--------|-----------|------------------|
| File naming | `*Test.java` | `*IT.java` |
| Maven phase | `test` | `integration-test` |
| Scope | Single class/method | Full API endpoint |
| Database | Mocked | Real H2 database |
| Spring Context | Partial or mocked | Full application context |
| Execution time | Fast (milliseconds) | Slower (seconds) |
| Purpose | Verify logic | Verify integration |

## Future Enhancements

Potential improvements for integration tests:

1. **Performance Tests**: Add load testing with multiple concurrent users
2. **Contract Tests**: Add Pact or Spring Cloud Contract tests
3. **External Server Mode**: Add configuration to test against external servers
4. **Test Data Builders**: Create fluent builders for complex test data
5. **Parallel Execution**: Configure tests to run in parallel for faster feedback
