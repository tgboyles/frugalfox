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

## Running Integration Tests

Integration tests are separate from unit tests and can be run independently.

### Prerequisites

The tests use `@SpringBootTest` with `RANDOM_PORT`, which starts the Spring Boot application automatically. No manual server startup is required.

### Run Integration Tests Only

```bash
# From the backend directory
mvn verify

# Or run integration tests explicitly
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

## Test Structure

### Base Classes

- **BaseIntegrationTest**: Abstract base class providing:
  - Spring Boot test configuration with random port
  - REST Assured setup and configuration
  - Data cleanup before each test
  - Utility methods for authentication and test data creation

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
- CI/CD pipelines
- Staging environments

The tests are self-contained and handle their own data setup/teardown, making them suitable for automated execution.

## Troubleshooting

### Port Already in Use

If you see port conflicts, the tests use `RANDOM_PORT` mode to avoid conflicts. The port is automatically assigned by Spring Boot.

### Test Failures

- Check logs for detailed error messages
- REST Assured enables logging of failed requests automatically
- Ensure the database is clean before running tests (handled by `@BeforeEach`)

### Running Against a Real Server

The tests are configured to start their own embedded server. If you need to run against an external server:

1. Modify `BaseIntegrationTest` to use `DEFINED_PORT` instead of `RANDOM_PORT`
2. Set the server URL manually in the `setUp()` method
3. Ensure the external server is running and accessible

## Best Practices

- Each test is independent and can run in any order
- Tests generate unique usernames to avoid conflicts
- Tests clean up their own data
- Tests verify both positive and negative scenarios
- Tests check user isolation to ensure security
- Tests use descriptive names that explain what is being tested
