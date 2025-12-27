<img width="1024" height="1024" alt="image" src="https://github.com/user-attachments/assets/7132ffdf-4362-44ad-9ca2-da1115cc2f1c" />

# Frugal Fox

JWT-authenticated expense tracking API with user isolation, advanced search, and Flyway migrations.

**Stack**: Spring Boot 4.0.1 • PostgreSQL 17 • Spring Security • JPA • Flyway • Docker

## Quick Start

```bash
# Start everything (Postgres + Backend)
docker compose up --build

# Application runs at http://localhost:8080
# Database runs at localhost:5432 (frugalfox/frugalfox/frugalfox)
```

Health check: `curl http://localhost:8080/actuator/health`

## Development Workflows

### Docker (Recommended)
```bash
# Full rebuild
docker compose down && docker compose up --build --force-recreate

# View logs
docker compose logs -f backend

# Database only (for local development)
docker compose up -d postgres

# Teardown with data removal
docker compose down -v
```

### Local Maven
```bash
cd backend

# Build & test
mvn clean package

# Skip tests
mvn clean package -DskipTests

# Run tests only
mvn test
mvn test -Dtest=ExpenseControllerTest

# Run locally (requires Postgres on localhost:5432)
mvn spring-boot:run
```

## API Usage

### Getting Started: Authentication

All expense endpoints require JWT authentication via the `Authorization: Bearer <token>` header.

```bash
# 1. Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "john", "password": "password123", "email": "john@example.com"}'
# Returns: {"token": "eyJ...", "username": "john", "email": "john@example.com"}

# 2. Save the token
export TOKEN="eyJ..."

# 3. Use it for all expense operations
curl http://localhost:8080/expenses -H "Authorization: Bearer $TOKEN"
```

Login endpoint: `POST /auth/login` (same request/response format as register)

### Expense Operations

#### CRUD
```bash
# Create
curl -X POST http://localhost:8080/expenses \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"date": "2025-12-26", "merchant": "Whole Foods", "amount": 125.50, "bank": "Chase", "category": "Groceries"}'

# Read (by ID)
curl http://localhost:8080/expenses/1 -H "Authorization: Bearer $TOKEN"

# Update
curl -X PUT http://localhost:8080/expenses/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"date": "2025-12-26", "merchant": "Whole Foods Market", "amount": 130.00, "bank": "Chase", "category": "Groceries"}'

# Delete (returns 204 No Content)
curl -X DELETE http://localhost:8080/expenses/1 -H "Authorization: Bearer $TOKEN"
```

#### Search & Filter
```bash
# List all (paginated, default 20/page, sorted by date desc)
curl "http://localhost:8080/expenses" -H "Authorization: Bearer $TOKEN"

# Filter by category (exact match)
curl "http://localhost:8080/expenses?category=Groceries" -H "Authorization: Bearer $TOKEN"

# Filter by merchant (partial, case-insensitive)
curl "http://localhost:8080/expenses?merchant=whole" -H "Authorization: Bearer $TOKEN"

# Filter by date range
curl "http://localhost:8080/expenses?startDate=2025-01-01&endDate=2025-12-31" -H "Authorization: Bearer $TOKEN"

# Filter by amount range
curl "http://localhost:8080/expenses?minAmount=50&maxAmount=200" -H "Authorization: Bearer $TOKEN"

# Combine filters with custom pagination
curl "http://localhost:8080/expenses?category=Groceries&bank=Chase&page=0&size=50&sort=amount,desc" -H "Authorization: Bearer $TOKEN"
```

**Available Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `category` | string | Exact match |
| `bank` | string | Exact match |
| `merchant` | string | Partial, case-insensitive |
| `startDate` | date | ISO 8601 (YYYY-MM-DD) |
| `endDate` | date | ISO 8601 (YYYY-MM-DD) |
| `minAmount` | decimal | Inclusive |
| `maxAmount` | decimal | Inclusive |
| `page` | int | 0-indexed (default: 0) |
| `size` | int | Default: 20 |
| `sort` | string | Format: `field,direction` (e.g., `date,desc`) |

### Response Formats

**Success Response (Expense)**
```json
{
  "id": 1,
  "date": "2025-12-26",
  "merchant": "Whole Foods",
  "amount": 125.50,
  "bank": "Chase",
  "category": "Groceries",
  "createdAt": "2025-12-26T10:30:00",
  "updatedAt": "2025-12-26T10:30:00"
}
```

**Paginated Response**
```json
{
  "content": [/* array of expense objects */],
  "pageable": {"pageNumber": 0, "pageSize": 20},
  "totalElements": 42,
  "totalPages": 3,
  "first": true,
  "last": false
}
```

**Error Response**
```json
{
  "timestamp": "2025-12-26T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [{"field": "amount", "message": "must be greater than zero"}]
}
```

Common error codes: `400` (validation), `401` (invalid credentials), `403` (missing/invalid token), `404` (not found)

## Architecture

### Codebase Structure
```
backend/src/main/java/com/tgboyles/frugalfox/
├── expense/                 # Core domain: expense tracking
│   ├── Expense.java         # Entity with validation (@NotNull, @Positive, @PastOrPresent)
│   ├── ExpenseController.java       # REST endpoints (CRUD + search)
│   ├── ExpenseService.java          # Business logic with JPA Specifications
│   ├── ExpenseRepository.java       # JpaRepository + JpaSpecificationExecutor
│   ├── ExpenseSearchCriteria.java   # DTO for dynamic queries
│   └── ExpenseNotFoundException.java
│
├── user/                    # User management
│   ├── User.java            # Entity implementing UserDetails for Spring Security
│   ├── UserRepository.java  # JpaRepository<User, Long>
│   └── UserService.java     # UserDetailsService implementation
│
├── security/                # Authentication & authorization
│   ├── SecurityConfig.java          # Spring Security filter chain configuration
│   ├── JwtUtil.java                 # Token generation & validation
│   ├── JwtAuthenticationFilter.java # OncePerRequestFilter for JWT extraction
│   ├── AuthController.java          # Public endpoints: /auth/register, /auth/login
│   └── [DTOs: AuthRequest, RegisterRequest, AuthResponse]
│
└── common/                  # Cross-cutting concerns
    ├── ErrorResponse.java           # Standardized error DTO
    └── GlobalExceptionHandler.java  # @ControllerAdvice for consistent error handling
```

### Database Schema

Managed by Flyway migrations in `backend/src/main/resources/db/migration/`

**V1__create_users_table.sql**
```sql
users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,  -- BCrypt hashed
  email VARCHAR(255) NOT NULL,
  enabled BOOLEAN DEFAULT true,
  created_at, updated_at TIMESTAMP
)
```

**V2__create_expenses_table.sql**
```sql
expenses (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  expense_date DATE NOT NULL,
  merchant VARCHAR(255) NOT NULL,
  amount NUMERIC(12,2) CHECK (amount >= 0),
  bank VARCHAR(100) NOT NULL,
  category VARCHAR(100) NOT NULL,
  created_at, updated_at TIMESTAMP
)
-- Indexes: user_id, (user_id, expense_date DESC), (user_id, category), merchant
```

### Security Model

- **Authentication**: JWT tokens (HS512, 24hr expiration configurable via `jwt.expiration`)
- **Password Storage**: BCrypt with 10+ rounds
- **User Isolation**: All expense queries automatically scoped to authenticated user via `@AuthenticationPrincipal User`
- **Validation**: Bean Validation (`@Valid`) on all request bodies
- **CORS**: Configured in SecurityConfig (currently permissive for development)
- **Public Endpoints**: `/auth/**`, `/actuator/health`, `/` (no authentication required)

### Key Design Patterns

- **Repository Pattern**: Spring Data JPA repositories with custom specifications for dynamic queries
- **DTO Pattern**: Separate request/response objects in security package
- **Service Layer**: Business logic isolated from controllers
- **Global Exception Handling**: Consistent error responses via `@ControllerAdvice`
- **Lazy Loading**: `@Lazy` on PasswordEncoder to break circular dependency in SecurityConfig
- **User Scoping**: All expense operations filtered by authenticated user to prevent data leakage

### Testing Strategy

- **Unit Tests**: Service layer with Mockito
- **Integration Tests**: Controller tests with `@SpringBootTest` + MockMvc
- **Test Database**: H2 in-memory with PostgreSQL compatibility mode
- **Flyway**: Enabled in test profile to validate migrations

Run tests: `mvn test` (includes compilation → test → report generation)

### Configuration

**Application Properties**
- `backend/src/main/resources/application.properties` - Production (PostgreSQL)
- `backend/src/test/resources/application.properties` - Test (H2 + Flyway)

**Environment Variables** (Docker)
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/frugalfox
SPRING_DATASOURCE_USERNAME=frugalfox
SPRING_DATASOURCE_PASSWORD=frugalfox
```

**JWT Configuration** (application.properties)
```properties
jwt.secret=<256-bit-secret>
jwt.expiration=86400000  # 24 hours in milliseconds
```

## Contributing

This project follows:
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Spring Boot Best Practices](https://docs.spring.io/spring-boot/reference/using/structuring-your-code.html)
- [REST API Best Practices](https://blog.postman.com/rest-api-best-practices/)

**Prerequisites**: Java 23+, Maven 3.x, Docker

## License

MIT
