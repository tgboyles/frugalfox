# Claude Code Instructions - Frugal Fox

This document provides AI coding assistants with context about the Frugal Fox project to ensure consistent, high-quality contributions.

## Project Overview

Frugal Fox is a JWT-authenticated expense tracking REST API built with Spring Boot 4.0.1, PostgreSQL 17, and Spring Security. The application provides secure user registration, authentication, and multi-tenant expense management with advanced search capabilities.

**Key Technologies:**
- Spring Boot 4.0.1 (Spring MVC, Spring Data JPA, Spring Security)
- PostgreSQL 17 (production) / H2 (testing)
- Flyway for database migrations
- JWT authentication (jjwt 0.12.6)
- Bean Validation for input validation
- Docker & Docker Compose for deployment
- Maven for build management
- Java 23

## Project Structure

```
frugal_fox/
├── backend/
│   ├── src/main/java/com/tgboyles/frugalfox/
│   │   ├── expense/              # Core domain: expense CRUD + search
│   │   ├── user/                 # User entity and repository
│   │   ├── security/             # JWT auth, filters, security config
│   │   ├── common/               # Global exception handling, DTOs
│   │   └── [Application classes]
│   ├── src/main/resources/
│   │   ├── db/migration/         # Flyway SQL migrations (versioned)
│   │   └── application.properties
│   ├── src/test/                 # Unit and integration tests
│   └── pom.xml
├── docker-compose.yml            # PostgreSQL + Backend orchestration
└── README.md                     # Full API documentation
```

## Development Guidelines

### Code Style and Conventions

1. **Follow Existing Patterns**
   - Use the existing service-repository-controller layering
   - Place business logic in service classes, not controllers
   - Use DTOs for request/response objects (see [security/](backend/src/main/java/com/tgboyles/frugalfox/security/) package)
   - Follow Google Java Style Guide conventions

2. **Naming Conventions**
   - Entities: singular nouns (e.g., `User`, `Expense`)
   - Controllers: `[Entity]Controller` (e.g., [ExpenseController.java](backend/src/main/java/com/tgboyles/frugalfox/expense/ExpenseController.java))
   - Services: `[Entity]Service` (e.g., [ExpenseService.java](backend/src/main/java/com/tgboyles/frugalfox/expense/ExpenseService.java))
   - Repositories: `[Entity]Repository` (e.g., [ExpenseRepository.java](backend/src/main/java/com/tgboyles/frugalfox/expense/ExpenseRepository.java))
   - DTOs: descriptive names ending in Request/Response/Criteria

3. **Package Organization**
   - Group by feature/domain, not by layer (e.g., all expense-related classes in `expense/`)
   - Cross-cutting concerns go in `common/`
   - Security-related classes in `security/`

### Security Requirements

**CRITICAL**: This application handles user authentication and data isolation. Always adhere to these security principles:

1. **User Isolation**
   - NEVER expose data across users
   - Always filter queries by authenticated user: `@AuthenticationPrincipal User user`
   - See [ExpenseController.java:37-52](backend/src/main/java/com/tgboyles/frugalfox/expense/ExpenseController.java#L37-L52) for the pattern

2. **Authentication**
   - All `/expenses/**` endpoints require JWT authentication
   - Public endpoints: `/auth/**`, `/actuator/health`, `/`
   - JWT configuration in [JwtUtil.java](backend/src/main/java/com/tgboyles/frugalfox/security/JwtUtil.java)

3. **Validation**
   - Use Bean Validation annotations on entities (`@NotNull`, `@Positive`, `@PastOrPresent`)
   - Add `@Valid` to controller method parameters
   - See [Expense.java](backend/src/main/java/com/tgboyles/frugalfox/expense/Expense.java) for validation examples

4. **Password Storage**
   - Always use BCrypt via `PasswordEncoder`
   - Never log or expose passwords
   - Configuration in [SecurityConfig.java](backend/src/main/java/com/tgboyles/frugalfox/security/SecurityConfig.java)

5. **Input Validation**
   - Validate all user inputs at the controller level
   - Use `@Valid` annotations
   - Handle validation errors in [GlobalExceptionHandler.java](backend/src/main/java/com/tgboyles/frugalfox/common/GlobalExceptionHandler.java)

### Database and Persistence

1. **Flyway Migrations**
   - ALL schema changes MUST be versioned migrations in `backend/src/main/resources/db/migration/`
   - Naming: `V{version}__{description}.sql` (e.g., `V3__add_expense_notes_column.sql`)
   - Never modify existing migrations - create new ones
   - Test migrations in both PostgreSQL (production) and H2 (test)

2. **Entity Design**
   - Use `@GeneratedValue(strategy = GenerationType.IDENTITY)` for primary keys
   - Include `createdAt` and `updatedAt` audit fields with `@CreationTimestamp` and `@UpdateTimestamp`
   - Add foreign key constraints with `ON DELETE CASCADE` where appropriate
   - Example: [Expense.java](backend/src/main/java/com/tgboyles/frugalfox/expense/Expense.java)

3. **Repository Patterns**
   - Extend `JpaRepository<Entity, Long>`
   - Use `JpaSpecificationExecutor` for dynamic queries (see [ExpenseRepository.java](backend/src/main/java/com/tgboyles/frugalfox/expense/ExpenseRepository.java))
   - Create specifications in service layer for complex queries (see [ExpenseService.java:51-83](backend/src/main/java/com/tgboyles/frugalfox/expense/ExpenseService.java#L51-L83))

4. **Indexing Strategy**
   - Add indexes for foreign keys, frequently queried columns, and composite lookups
   - Current indexes on `expenses`: `user_id`, `(user_id, expense_date DESC)`, `(user_id, category)`, `merchant`

### API Design

1. **RESTful Conventions**
   - Use standard HTTP methods: GET (read), POST (create), PUT (update), DELETE (delete)
   - Return appropriate status codes: 200 (OK), 201 (Created), 204 (No Content), 400 (Bad Request), 401 (Unauthorized), 403 (Forbidden), 404 (Not Found)
   - Use plural nouns for collections: `/expenses`, not `/expense`

2. **Request/Response Format**
   - Accept and return JSON with `application/json` content type
   - Use DTOs for complex request bodies (validation, transformation)
   - Return consistent error format via [ErrorResponse.java](backend/src/main/java/com/tgboyles/frugalfox/common/ErrorResponse.java)

3. **Pagination and Sorting**
   - Use Spring Data's `Pageable` for collection endpoints
   - Default: 20 items per page, sorted by date descending
   - Query params: `page`, `size`, `sort` (e.g., `?page=0&size=50&sort=amount,desc`)

4. **Search and Filtering**
   - Support multiple query parameters for filtering
   - Use exact matches for categorical fields (`category`, `bank`)
   - Use partial, case-insensitive search for text fields (`merchant`)
   - Use range queries for numeric/date fields (`minAmount`, `maxAmount`, `startDate`, `endDate`)
   - Example: [ExpenseController.java:70-78](backend/src/main/java/com/tgboyles/frugalfox/expense/ExpenseController.java#L70-L78)

### Testing Requirements

1. **Test Coverage**
   - Write tests for all new features and bug fixes
   - Unit tests for service layer (mock repositories)
   - Integration tests for controllers (MockMvc + SpringBootTest)
   - See existing tests in `src/test/java/com/tgboyles/frugalfox/`

2. **Test Database**
   - Tests use H2 in-memory database with PostgreSQL compatibility mode
   - Flyway migrations run in test profile to validate schema
   - Configuration in `src/test/resources/application.properties`

3. **Test Organization**
   - Name tests clearly: `shouldReturnExpenseWhenUserOwnsIt()`
   - Use `@SpringBootTest` for integration tests
   - Use `@MockBean` for unit tests with mocked dependencies
   - Examples: [ExpenseControllerTest.java](backend/src/main/java/com/tgboyles/frugalfox/expense/ExpenseControllerTest.java), [ExpenseServiceTest.java](backend/src/main/java/com/tgboyles/frugalfox/expense/ExpenseServiceTest.java)

4. **Running Tests**
   - Run all tests: `mvn test`
   - Run specific test: `mvn test -Dtest=ExpenseControllerTest`
   - Build with tests: `mvn clean package`

### Development Workflow

1. **Environment Setup**
   - Prefer Docker Compose for full stack: `docker compose up --build`
   - Use database-only mode for local development: `docker compose up -d postgres`
   - Run backend locally: `cd backend && mvn spring-boot:run`

2. **Making Changes**
   - Read existing code before modifying (use Read tool on relevant files)
   - Follow existing patterns in similar classes
   - Update tests to reflect changes
   - Run tests before considering work complete: `mvn test`

3. **Database Changes**
   - Create new Flyway migration for schema changes
   - Test migration against both PostgreSQL and H2
   - Update entity classes to reflect schema changes
   - Update repositories if new queries are needed

4. **Adding New Features**
   - Create entity in appropriate package
   - Create repository extending `JpaRepository`
   - Create service with business logic
   - Create controller with REST endpoints
   - Add tests for all layers
   - Update README.md API documentation if adding public endpoints
   - Update Postman collection json for any relevant new changes

### Configuration Management

1. **Application Properties**
   - Production config: `backend/src/main/resources/application.properties`
   - Test config: `backend/src/test/resources/application.properties`
   - Use environment variables for sensitive data (database credentials, JWT secret)

2. **JWT Configuration**
   - Secret key: `jwt.secret` (256-bit minimum)
   - Expiration: `jwt.expiration` (milliseconds, default 24 hours)
   - Update in application.properties or via environment variables

3. **Docker Environment**
   - Database connection configured via environment variables in [docker-compose.yml](docker-compose.yml)
   - Override with `.env` file or inline in docker-compose

### Common Patterns and Idioms

1. **Dependency Injection**
   - Use constructor injection (preferred) or field injection with `@Autowired`
   - Use `@Lazy` to break circular dependencies (see [SecurityConfig.java:45](backend/src/main/java/com/tgboyles/frugalfox/security/SecurityConfig.java#L45))

2. **Exception Handling**
   - Create custom exceptions extending `RuntimeException` (see [ExpenseNotFoundException.java](backend/src/main/java/com/tgboyles/frugalfox/expense/ExpenseNotFoundException.java))
   - Handle in [GlobalExceptionHandler.java](backend/src/main/java/com/tgboyles/frugalfox/common/GlobalExceptionHandler.java) with `@ExceptionHandler`
   - Return consistent `ErrorResponse` objects

3. **Authentication Principal**
   - Access authenticated user: `@AuthenticationPrincipal User user`
   - Get user ID: `user.getId()`
   - This is automatically populated by Spring Security after JWT validation

4. **JPA Specifications**
   - Use for dynamic queries with multiple optional filters
   - Build specifications in service layer
   - Combine with `Specification.where()` and `.and()`
   - Example: [ExpenseService.java:51-83](backend/src/main/java/com/tgboyles/frugalfox/expense/ExpenseService.java#L51-L83)

### Anti-Patterns to Avoid

1. **Security**
   - Never expose other users' data
   - Never skip authentication for sensitive endpoints
   - Never log sensitive data (passwords, tokens)
   - Never trust client input without validation

2. **Database**
   - Never modify existing Flyway migrations
   - Never use SQL directly in controllers
   - Never expose database implementation details to clients
   - Never use N+1 queries (use JOIN FETCH or projections)

3. **Code Quality**
   - Don't put business logic in controllers
   - Don't repeat yourself (extract common logic to services/utilities)
   - Don't use magic numbers (use constants or application properties)
   - Don't ignore exceptions or use empty catch blocks

4. **API Design**
   - Don't break backwards compatibility without versioning
   - Don't return different response structures for success vs. error
   - Don't expose internal error details to clients (use generic messages)

### Useful Commands Reference

```bash
# Build and test
mvn clean package              # Full build with tests
mvn clean package -DskipTests  # Build without tests
mvn test                       # Run all tests
mvn test -Dtest=ClassName      # Run specific test

# Docker
docker compose up --build                            # Start all services
docker compose down -v                               # Stop and remove volumes
docker compose logs -f backend                       # View backend logs
docker compose up -d postgres                        # Database only
docker compose down && docker compose up --build --force-recreate  # Full rebuild

# Database
docker exec -it frugalfox-postgres psql -U frugalfox -d frugalfox  # Access PostgreSQL

# Health checks
curl http://localhost:8080/actuator/health  # Application health
```

### When to Ask for Clarification

Ask the user before:
- Making breaking changes to existing APIs
- Adding new dependencies to pom.xml
- Modifying security configurations
- Changing database schema in ways that affect existing data
- Adding new public endpoints
- Implementing features with multiple valid approaches

### References

- Full API documentation and examples: [README.md](README.md)
- Spring Boot reference: https://docs.spring.io/spring-boot/reference/
- Spring Security: https://docs.spring.io/spring-security/reference/
- Spring Data JPA: https://docs.spring.io/spring-data/jpa/reference/
- Flyway: https://documentation.red-gate.com/flyway
- Google Java Style Guide: https://google.github.io/styleguide/javaguide.html 