# Frugal Fox Backend

REST API for expense tracking with JWT authentication and multi-tenant data isolation.

## Technology Stack

- **Framework**: Spring Boot 4.0.1
- **Language**: Java 23
- **Database**: PostgreSQL 17 (production) / H2 (testing)
- **Authentication**: JWT (jjwt 0.12.6)
- **Migrations**: Flyway
- **Build Tool**: Maven
- **Key Dependencies**:
  - Spring MVC (REST API)
  - Spring Data JPA (persistence)
  - Spring Security (authentication/authorization)
  - Bean Validation (input validation)
  - Apache Commons CSV 1.11.0 (bulk import)

## Quick Start

### Using Docker (Recommended)

Start the backend with PostgreSQL:

```bash
# From project root
docker compose up --build backend
```

The API will be available at `http://localhost:8080`

### Local Development

**Prerequisites:**
- Java 23+
- Maven 3.x
- PostgreSQL running on localhost:5432

**Steps:**

1. Start PostgreSQL (or use Docker for database only):
```bash
# From project root
docker compose up -d postgres
```

2. Build and run:
```bash
cd backend
mvn spring-boot:run
```

3. Verify health:
```bash
curl http://localhost:8080/actuator/health
```

## Building and Testing

### Build Commands

```bash
# Full build with tests
mvn clean package

# Build without tests
mvn clean package -DskipTests

# Compile only
mvn clean compile
```

Build artifacts are created in `target/` directory.

### Running Tests

```bash
# Run unit tests only
mvn test

# Run unit tests + integration tests
mvn verify

# Run specific test class
mvn test -Dtest=ExpenseControllerTest

# Run specific test method
mvn test -Dtest=ExpenseServiceTest#shouldFilterExpensesByCategory

# Run specific integration test
mvn verify -Dit.test=AuthenticationIT

# Skip all tests
mvn clean package -DskipTests

# Skip integration tests only
mvn clean package -DskipITs
```

### Test Configuration

Tests use:
- **H2 in-memory database** with PostgreSQL compatibility mode
- **Flyway migrations** enabled to validate schema
- **Test profile** with configuration in `src/test/resources/application.properties`

The test suite includes:
- **Unit tests**: Service layer with Mockito (e.g., `ExpenseServiceTest`) - named `*Test.java`
- **Integration tests (MockMvc)**: Controller tests with `@SpringBootTest` + MockMvc (e.g., `ExpenseControllerTest`) - named `*Test.java`
- **Integration tests (REST Assured)**: Full API tests with REST Assured (e.g., `AuthenticationIT`) - named `*IT.java`

#### Integration Tests with REST Assured

The `/src/test/java/com/tgboyles/frugalfox/integration/` directory contains comprehensive REST Assured integration tests that validate the API from an end-to-end perspective.

**What they test:**
- User authentication (registration and login)
- Expense CRUD operations
- Advanced search and filtering
- CSV import and export functionality
- User data isolation
- Error handling and validation

**Key features:**
- Tests start their own Spring Boot application instance (no external server needed)
- Each test gets a clean H2 database
- Tests generate unique test data to avoid conflicts
- All major API endpoints are covered

**Running integration tests:**
```bash
# Run all integration tests
mvn verify

# Run specific integration test class
mvn verify -Dit.test=ExpenseCrudIT

# Run in Docker (if Java 23 not available locally)
docker run --rm -v "$(pwd)":/app -w /app maven:3.9-eclipse-temurin-23 mvn verify
```

**Learn more:** See [Integration Tests README](src/test/java/com/tgboyles/frugalfox/integration/README.md) for detailed documentation.

## Project Architecture

### Package Structure

```
src/main/java/com/tgboyles/frugalfox/
├── expense/                 # Core domain: expense tracking
│   ├── Expense.java         # Entity with validation
│   ├── ExpenseController.java       # REST endpoints (CRUD + search + import)
│   ├── ExpenseService.java          # Business logic with JPA Specifications + CSV import
│   ├── ExpenseRepository.java       # JpaRepository + JpaSpecificationExecutor
│   ├── ExpenseSearchCriteria.java   # DTO for dynamic queries
│   ├── ImportResult.java            # DTO for CSV import response
│   ├── CsvImportException.java      # CSV import validation exception
│   └── ExpenseNotFoundException.java
│
├── user/                    # User management
│   ├── User.java            # Entity implementing UserDetails
│   ├── UserRepository.java  # JpaRepository<User, Long>
│   └── UserService.java     # UserDetailsService implementation
│
├── security/                # Authentication & authorization
│   ├── SecurityConfig.java          # Spring Security filter chain
│   ├── JwtUtil.java                 # Token generation & validation
│   ├── JwtAuthenticationFilter.java # OncePerRequestFilter for JWT
│   ├── AuthController.java          # /auth/register, /auth/login
│   └── [DTOs: AuthRequest, RegisterRequest, AuthResponse]
│
└── common/                  # Cross-cutting concerns
    ├── ErrorResponse.java           # Standardized error DTO
    └── GlobalExceptionHandler.java  # @ControllerAdvice
```

### Architectural Patterns

**Layered Architecture:**
- **Controller Layer**: REST endpoints, request/response handling
- **Service Layer**: Business logic, transaction management
- **Repository Layer**: Data access with Spring Data JPA
- **Entity Layer**: Domain models with validation

**Key Patterns:**
- **Repository Pattern**: Spring Data JPA with custom specifications
- **DTO Pattern**: Separate request/response objects in security package
- **Service Pattern**: Business logic isolated from controllers
- **Global Exception Handling**: Consistent error responses via `@ControllerAdvice`

**Design Principles:**
- **Domain-Driven Design**: Package by feature (expense, user, security)
- **Dependency Injection**: Constructor injection preferred
- **User Scoping**: All expense operations filtered by authenticated user
- **Lazy Loading**: `@Lazy` annotation to break circular dependencies

### Database Schema

Managed by Flyway migrations in `src/main/resources/db/migration/`

**Users Table** (`V1__create_users_table.sql`):
```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,  -- BCrypt hashed
  email VARCHAR(255) NOT NULL,
  enabled BOOLEAN DEFAULT true,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

**Expenses Table** (`V2__create_expenses_table.sql`):
```sql
CREATE TABLE expenses (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  expense_date DATE NOT NULL,
  merchant VARCHAR(255) NOT NULL,
  amount NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
  bank VARCHAR(100) NOT NULL,
  category VARCHAR(100) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_expenses_user_id ON expenses(user_id);
CREATE INDEX idx_expenses_user_date ON expenses(user_id, expense_date DESC);
CREATE INDEX idx_expenses_user_category ON expenses(user_id, category);
CREATE INDEX idx_expenses_merchant ON expenses(merchant);
```

**Migration Guidelines:**
- NEVER modify existing migrations
- Create new migrations with versioned names: `V3__description.sql`
- Test against both PostgreSQL (production) and H2 (test)
- Always include rollback considerations

### Security Model

**Authentication:**
- JWT tokens with HS512 algorithm
- 24-hour expiration (configurable via `jwt.expiration`)
- Token sent via `Authorization: Bearer <token>` header

**Authorization:**
- Public endpoints: `/auth/**`, `/actuator/health`, `/`
- Protected endpoints: `/expenses/**` (requires valid JWT)
- User isolation enforced at service layer via `@AuthenticationPrincipal User`

**Password Storage:**
- BCrypt hashing with 10+ rounds
- Never logged or exposed in responses

**Validation:**
- Bean Validation annotations on entities (`@NotNull`, `@Positive`, `@PastOrPresent`)
- `@Valid` on controller method parameters
- Global exception handler for validation errors

**CORS:**
- Configured in SecurityConfig (currently permissive for development)
- Should be restricted in production

## API Usage

The backend exposes a RESTful API for expense tracking. All expense endpoints require JWT authentication.

### Quick API Test

```bash
# 1. Register a user
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "john", "password": "password123", "email": "john@example.com"}'
# Returns: {"token": "eyJ...", "username": "john", "email": "john@example.com"}

# 2. Save the token
export TOKEN="eyJ..."

# 3. Create an expense
curl -X POST http://localhost:8080/expenses \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"date": "2025-12-26", "merchant": "Whole Foods", "amount": 125.50, "bank": "Chase", "category": "Groceries"}'

# 4. Or bulk import from CSV
curl -X POST http://localhost:8080/expenses/import \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@backend/sample-expenses.csv"

# 5. List all expenses
curl http://localhost:8080/expenses -H "Authorization: Bearer $TOKEN"
```

### Postman Collection (Recommended)

For comprehensive API testing, use the included Postman collection:

1. **Import** `backend/postman_collection.json` into Postman
2. **Run** Authentication → Register (JWT token auto-saves)
3. **Test** any endpoint - authentication is automatic!

Features:
- Automatic JWT token management
- Organized folders for all operations
- Pre-configured realistic examples
- Inline documentation

### API Endpoints

**Authentication** (Public):
```
POST /auth/register  # Register new user
POST /auth/login     # Login with credentials
```

**Expenses** (Protected - requires JWT):
```
GET    /expenses          # List/search expenses (with filters)
POST   /expenses          # Create expense
POST   /expenses/import   # Bulk import expenses from CSV
GET    /expenses/export   # Export expenses to CSV (with filters)
GET    /expenses/{id}     # Get expense by ID
PUT    /expenses/{id}     # Update expense
DELETE /expenses/{id}     # Delete expense
```

**Search Filters:**
- `category`, `bank` - Exact match
- `merchant` - Partial, case-insensitive
- `startDate`, `endDate` - Date range (ISO 8601)
- `minAmount`, `maxAmount` - Amount range
- `page`, `size`, `sort` - Pagination/sorting

**Example:**
```bash
curl "http://localhost:8080/expenses?category=Groceries&startDate=2025-01-01&page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

### Bulk Import from CSV

Import multiple expenses at once using a CSV file:

**CSV Format:**
```csv
date,merchant,amount,bank,category
2025-01-01,Whole Foods,125.50,Chase,Groceries
2025-01-02,Shell Gas Station,45.00,Chase,Transportation
2025-01-03,Target,75.00,BofA,Shopping
```

**Requirements:**
- Maximum 1000 rows per file
- CSV header must be: `date,merchant,amount,bank,category`
- Date format: ISO 8601 (YYYY-MM-DD)
- Amount must be a positive number
- All fields are required and cannot be blank
- Same validation rules as single expense creation

**Example Request:**
```bash
curl -X POST http://localhost:8080/expenses/import \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@expenses.csv"
```

**Response:**
```json
{
  "totalRows": 3,
  "successfulImports": 3,
  "failedImports": 0,
  "errors": []
}
```

**Partial Success Example:**
If some rows fail validation, valid rows are still imported:
```json
{
  "totalRows": 5,
  "successfulImports": 4,
  "failedImports": 1,
  "errors": [
    "Row 3: Invalid date format '2025-13-01'. Expected ISO format (YYYY-MM-DD)"
  ]
}
```

**Sample CSV File:**
A sample CSV file is available at `backend/sample-expenses.csv` for testing.

### Export Expenses to CSV

Export your expenses to CSV format for backup, analysis, or use with other tools.

**Endpoint:**
```
GET /expenses/export
```

**Features:**
- Exports all expenses matching your search criteria
- Same CSV format as import (compatible for round-trip)
- Supports all search filters (category, bank, merchant, date range, amount range)
- Returns a downloadable CSV file with proper headers

**CSV Format (same as import):**
```csv
date,merchant,amount,bank,category
2025-01-01,Whole Foods,125.50,Chase,Groceries
2025-01-02,Shell Gas Station,45.00,Chase,Transportation
2025-01-03,Target,75.00,BofA,Shopping
```

**Example Requests:**

Export all expenses:
```bash
curl -X GET http://localhost:8080/expenses/export \
  -H "Authorization: Bearer $TOKEN" \
  -o expenses.csv
```

Export with filters (e.g., only groceries from January 2025):
```bash
curl -X GET "http://localhost:8080/expenses/export?category=Groceries&startDate=2025-01-01&endDate=2025-01-31" \
  -H "Authorization: Bearer $TOKEN" \
  -o groceries-jan-2025.csv
```

Export sorted by date descending:
```bash
curl -X GET "http://localhost:8080/expenses/export?sort=date,desc" \
  -H "Authorization: Bearer $TOKEN" \
  -o expenses-latest.csv
```

**Response Headers:**
```
Content-Type: text/csv
Content-Disposition: attachment; filename="expenses.csv"
```

**Use Cases:**
- Backup your expense data
- Analyze expenses in Excel or Google Sheets
- Migrate data to another expense tracking system
- Share filtered expense reports with others
- Create periodic expense reports (monthly, quarterly, etc.)

## Configuration

### Application Properties

**Production** (`src/main/resources/application.properties`):
```properties
# Database (PostgreSQL)
spring.datasource.url=jdbc:postgresql://localhost:5432/frugalfox
spring.datasource.username=frugalfox
spring.datasource.password=frugalfox

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Flyway
spring.flyway.enabled=true

# JWT
jwt.secret=<256-bit-secret>
jwt.expiration=86400000  # 24 hours
```

**Test** (`src/test/resources/application.properties`):
```properties
# H2 in-memory database
spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL
spring.jpa.hibernate.ddl-auto=create-drop
spring.flyway.enabled=true
```

### Environment Variables

When running via Docker Compose:
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/frugalfox
SPRING_DATASOURCE_USERNAME=frugalfox
SPRING_DATASOURCE_PASSWORD=frugalfox
JWT_SECRET=<256-bit-secret>
JWT_EXPIRATION=86400000
```

## Development Workflows

### Adding New Features

Follow this pattern for new features:

1. **Create Entity** in appropriate package
   - Add validation annotations
   - Include audit fields (`createdAt`, `updatedAt`)

2. **Create Repository** extending `JpaRepository`
   - Add custom query methods if needed
   - Use `JpaSpecificationExecutor` for dynamic queries

3. **Create Service** with business logic
   - Inject repository
   - Implement business rules
   - Use specifications for complex queries

4. **Create Controller** with REST endpoints
   - Use `@AuthenticationPrincipal User` for user context
   - Add `@Valid` for request validation
   - Return appropriate HTTP status codes

5. **Write Tests** for all layers
   - Unit tests for service layer
   - Integration tests for controllers

6. **Create Migration** if schema changes needed
   - Version: `V{n+1}__description.sql`
   - Test with both PostgreSQL and H2

7. **Update Documentation**
   - Update API documentation in root README
   - Update Postman collection
   - Update CLAUDE.md if patterns change

### Database Migrations

To create a new migration:

1. Create file: `src/main/resources/db/migration/V{version}__{description}.sql`
2. Write forward migration SQL
3. Test locally: `mvn spring-boot:run`
4. Test in tests: `mvn test`
5. Verify in Docker: `docker compose up --build backend`

Example:
```sql
-- V3__add_expense_notes_column.sql
ALTER TABLE expenses ADD COLUMN notes TEXT;
CREATE INDEX idx_expenses_notes ON expenses USING gin(to_tsvector('english', notes));
```

### Troubleshooting

**Build Issues:**
```bash
# Clean all artifacts
mvn clean

# Update dependencies
mvn dependency:purge-local-repository

# Verify Java version
java -version  # Must be 23+
```

**Database Issues:**
```bash
# Check PostgreSQL is running
docker compose ps

# View database logs
docker compose logs postgres

# Connect to database
docker exec -it frugalfox-postgres psql -U frugalfox -d frugalfox

# Reset database
docker compose down -v && docker compose up -d postgres
```

**Test Failures:**
```bash
# Run tests with verbose output
mvn test -X

# Run single test for debugging
mvn test -Dtest=ExpenseControllerTest#shouldCreateExpense

# Check H2 compatibility issues
# Review src/test/resources/application.properties
```

## Contribution Guidelines

### Code Style

Follow **Google Java Style Guide**:
- 2-space indentation
- Line length: 100 characters
- Import order: static, java/javax, third-party, project
- Use `var` sparingly (prefer explicit types)

### Naming Conventions

- **Entities**: Singular nouns (e.g., `User`, `Expense`)
- **Controllers**: `[Entity]Controller`
- **Services**: `[Entity]Service`
- **Repositories**: `[Entity]Repository`
- **DTOs**: Descriptive names ending in Request/Response/Criteria

### Package Organization

Group by **feature/domain**, not by layer:
- Good: `expense/ExpenseController.java`, `expense/ExpenseService.java`
- Bad: `controllers/ExpenseController.java`, `services/ExpenseService.java`

### Security Requirements

**CRITICAL** - Always follow these security principles:

1. **User Isolation**: Always filter queries by authenticated user
   ```java
   @GetMapping
   public Page<Expense> getExpenses(
       @AuthenticationPrincipal User user,
       Pageable pageable
   ) {
       return expenseService.getExpensesForUser(user, pageable);
   }
   ```

2. **Validation**: Use `@Valid` on all request bodies
3. **Password Handling**: Never log or expose passwords
4. **Authorization**: Check user owns resource before modifications

### Testing Requirements

**All new features must include:**
- Unit tests for service layer (mock repositories)
- Integration tests for controllers (MockMvc)
- Tests for error cases (validation, not found, unauthorized)

**Test Naming:**
```java
// Good
shouldReturnExpenseWhenUserOwnsIt()
shouldThrowNotFoundExceptionWhenExpenseDoesNotExist()

// Bad
test1()
expenseTest()
```

### Pull Request Checklist

Before submitting:
- [ ] All tests pass: `mvn test`
- [ ] Build succeeds: `mvn clean package`
- [ ] Code follows Google Java Style Guide
- [ ] Security requirements enforced
- [ ] Database migrations created (if needed)
- [ ] Documentation updated (README, Postman, CLAUDE.md)
- [ ] No compiler warnings
- [ ] Commits are atomic and well-described

## Related Documentation

- [Root README](../README.md) - Full API documentation and quick start
- [Frontend README](../frontend/README.md) - Frontend application
- [CLAUDE.md](../CLAUDE.md) - AI assistant integration guide

## License

MIT
