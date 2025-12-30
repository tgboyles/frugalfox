<img width="1024" height="1024" alt="image" src="https://github.com/user-attachments/assets/7132ffdf-4362-44ad-9ca2-da1115cc2f1c" />

# Frugal Fox

Frugal Fox is a demonstrative budgeting app that leverages Java, Spring Boot, and various commodity AI technologies such as agentic behaviors, Model Context Protocol (MCP) usage, and chat capabilities.

## Table of Contents

- [Quick Start](#quick-start)
- [API Usage](#api-usage)
  - [Postman Collection](#option-1-postman-collection-recommended)
  - [cURL Commands](#option-2-curl-commands)
- [MCP Server](#mcp-server)
  - [Overview](#mcp-overview)
  - [Quick Start](#mcp-quick-start)
  - [Available Tools](#available-mcp-tools)
  - [Claude Desktop Setup](#configuring-claude-desktop)
  - [Other MCP Clients](#other-mcp-clients)
- [Development](#development)
  - [Workflows](#development-workflows)
  - [Architecture](#architecture)
  - [Testing](#testing-strategy)
- [AI Assistant Integration](#working-with-ai-assistants-claude-code)
- [Contributing](#contributing)

## Quick Start

Start the full stack (PostgreSQL + Backend API + MCP Server):

```bash
docker compose up --build
```

This starts:
- **PostgreSQL database** on port 5432
- **Frugal Fox backend API** on port 8080
- **Frugal Fox MCP server** on port 8081

Health checks:
```bash
curl http://localhost:8080/actuator/health  # Backend API
curl http://localhost:8081/actuator/health  # MCP Server
```

## API Usage

### Option 1: Postman Collection (Recommended)

The easiest way to explore and test the API is using the included Postman collection.

**Setup:**

1. **Install Postman**
   - Download from [postman.com/downloads](https://www.postman.com/downloads/)
   - Or use the web version at [web.postman.com](https://web.postman.com/) (requires sign-in)

2. **Import the Collection**
   - Open Postman and sign in (required to use collections)
   - Click **Import** button (top left)
   - Select `backend/postman_collection.json` from the project root
   - Collection will appear in your workspace sidebar

3. **Start Testing**
   - Ensure backend is running: `docker compose up --build`
   - Run **Authentication → Register** to create an account (JWT token auto-saves)
   - Use any expense endpoint - authentication is automatic!
   - Optional: Run all requests in **Sample Data Setup** folder for test data

**Collection Features:**
- 🔐 Automatic JWT token management (no manual copying)
- 📁 Organized folders: Authentication, CRUD, Search & Filters, Health Check, Sample Data
- 📝 Pre-configured requests with realistic examples
- 💡 Inline documentation for all parameters and validation rules
- 🔄 Collection variables for easy environment switching

**Note**: You must be signed in to Postman to import and use collections.

### Option 2: cURL Commands

For command-line testing, use cURL:

#### Getting Started: Authentication

All expense endpoints require JWT authentication via the `Authorization: Bearer <token>` header.

```bash
# 1. Register
curl -X POST http://localhost:8080/auth/register \\
  -H "Content-Type: application/json" \\
  -d '{"username": "john", "password": "password123", "email": "john@example.com"}'
# Returns: {"token": "eyJ...", "username": "john", "email": "john@example.com"}

# 2. Save the token
export TOKEN="eyJ..."

# 3. Use it for all expense operations
curl http://localhost:8080/expenses -H "Authorization: Bearer $TOKEN"
```

Login endpoint: `POST /auth/login` (same request/response format as register)

#### Expense Operations

**CRUD:**
```bash
# Create
curl -X POST http://localhost:8080/expenses \\
  -H "Authorization: Bearer $TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"date": "2025-12-26", "merchant": "Whole Foods", "amount": 125.50, "bank": "Chase", "category": "Groceries"}'

# Read (by ID)
curl http://localhost:8080/expenses/1 -H "Authorization: Bearer $TOKEN"

# Update
curl -X PUT http://localhost:8080/expenses/1 \\
  -H "Authorization: Bearer $TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"date": "2025-12-26", "merchant": "Whole Foods Market", "amount": 130.00, "bank": "Chase", "category": "Groceries"}'

# Delete (returns 204 No Content)
curl -X DELETE http://localhost:8080/expenses/1 -H "Authorization: Bearer $TOKEN"
```

**Search & Filter:**
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

#### Response Formats

**Success Response (Expense):**
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

**Paginated Response:**
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

**Error Response:**
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

## MCP Server

### MCP Overview

The Frugal Fox MCP (Model Context Protocol) server provides programmatic access to the expense tracking API for AI assistants and other MCP clients. It exposes all API endpoints as tools that can be used by Claude Desktop, IDEs, and custom applications.

**Key Features:**
- 🤖 9 tools covering authentication, CRUD operations, and search
- 🌐 Web service using SSE (Server-Sent Events) transport
- 🐳 Docker-ready for containerized deployment
- 🔌 Compatible with Claude Desktop, Cline, Continue, Zed, and more

### MCP Quick Start

The MCP server is included in the main Docker Compose stack:

```bash
docker compose up --build
```

The MCP server will be available at `http://localhost:8081/sse`

**For local development:**
```bash
cd mcp
mvn clean package
java -jar target/frugalfoxmcp-0.0.1-SNAPSHOT.jar
```

**Configuration:**
```properties
# mcp/src/main/resources/application.properties
server.port=8081
spring.ai.mcp.server.type=SYNC
spring.ai.mcp.server.name=frugalfoxmcp
spring.ai.mcp.server.version=1.0.0
spring.ai.mcp.server.protocol=sse

frugalfox.api.base-url=${FRUGALFOX_API_BASE_URL:http://localhost:8080}
frugalfox.api.timeout=${FRUGALFOX_API_TIMEOUT:30000}
```

**Environment Variables:**
- `FRUGALFOX_API_BASE_URL`: URL of the backend API (default: `http://localhost:8080`)
- `FRUGALFOX_API_TIMEOUT`: HTTP client timeout in milliseconds (default: `30000`)

### Available MCP Tools

The MCP server provides 9 tools:

#### Authentication Tools

**`registerUser`** - Register a new user account
- Parameters: `username`, `password`, `email`
- Returns: `{success, token, username, email}`

**`loginUser`** - Login with existing credentials
- Parameters: `username`, `password`
- Returns: Same as `registerUser`

**`healthCheck`** - Check if the Frugal Fox API is healthy
- Returns: `{success, status}`

#### Expense Management Tools

**`createExpense`** - Create a new expense
- Parameters: `token`, `date`, `merchant`, `amount`, `bank`, `category`
- Returns: `{success, expense}`

**`getExpense`** - Get a specific expense by ID
- Parameters: `token`, `id`
- Returns: `{success, expense}`

**`updateExpense`** - Update an existing expense
- Parameters: `token`, `id`, `date`, `merchant`, `amount`, `bank`, `category`
- Returns: `{success, expense}`

**`deleteExpense`** - Delete an expense
- Parameters: `token`, `id`
- Returns: `{success, message}`

#### Search and Filter Tools

**`searchExpenses`** - Search and filter expenses with pagination
- Parameters: `token`, `category`, `bank`, `merchant`, `startDate`, `endDate`, `minAmount`, `maxAmount`, `page`, `size`, `sort`
- Returns: `{success, data}` with paginated results

**`listAllExpenses`** - List all expenses with pagination
- Parameters: `token`, `page`, `size`
- Returns: Same as `searchExpenses`

### Configuring Claude Desktop

#### Step 1: Start the MCP Server

```bash
docker compose up mcp
```

The server will be available at `http://localhost:8081/sse`

#### Step 2: Ensure Node.js is Installed

The `mcp-remote` bridge requires Node.js:

```bash
node --version
```

If not installed, download from [nodejs.org](https://nodejs.org/)

#### Step 3: Configure Claude Desktop

Edit `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "frugalfox": {
      "command": "npx",
      "args": [
        "mcp-remote",
        "http://localhost:8081/sse"
      ]
    }
  }
}
```

**Configuration file locations:**
- **macOS:** `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Windows:** `%APPDATA%\\Claude\\claude_desktop_config.json`
- **Linux:** `~/.config/Claude/claude_desktop_config.json`

**How it works:**
- `npx mcp-remote` acts as a bridge between Claude Desktop (STDIO) and the SSE web service
- Allows the MCP server to run as a containerized service
- Bridge automatically installs on first use via `npx`

#### Step 4: Restart Claude Desktop

1. Completely quit Claude Desktop (Cmd/Ctrl+Q)
2. Relaunch Claude Desktop
3. MCP server tools are now available

#### Verification

In Claude Desktop, start a new conversation and ask:
```
Can you check if the Frugal Fox API is healthy?
```

Claude should use the `healthCheck` tool and report the API status.

#### Troubleshooting

**MCP Server not starting:**
- Check logs: `docker compose logs mcp` or `tail -f ~/.frugalfoxmcp/logs/frugalfoxmcp.log`
- Verify Java version: `java -version` (must be 25+)
- Ensure port 8081 is available: `lsof -i :8081`

**Claude Desktop connection issues:**
- Verify MCP server is running: `curl http://localhost:8081/actuator/health`
- Ensure Node.js is installed: `node --version`
- Check Claude Desktop logs (View → Developer → Developer Tools)
- Verify config uses `npx mcp-remote http://localhost:8081/sse`
- Test SSE endpoint: `curl http://localhost:8081/sse` (should hang, indicating connection)

**Tools not appearing:**
- Check MCP server logs for "Registered tools: 9"
- Rebuild and restart: `docker compose up --build mcp`

### Other MCP Clients

The MCP server works with any MCP-compatible client:

**Common MCP Clients:**
- **Cline** (VS Code extension)
- **Continue** (VS Code extension)
- **Zed** editor
- Custom applications using MCP SDKs

For STDIO-based clients, configure them to execute:
```bash
java -jar /path/to/frugal_fox/mcp/target/frugalfoxmcp-0.0.1-SNAPSHOT.jar
```

Refer to your client's documentation for specific configuration steps.

### MCP Usage Example

Here's a typical workflow:

1. **Register:** `registerUser(username="alice", password="secure123", email="alice@example.com")`
   - Returns token: `"eyJhbGc..."`

2. **Create expense:** `createExpense(token="eyJhbGc...", date="2025-12-28", merchant="Starbucks", amount="5.50", bank="Chase", category="Dining")`
   - Returns expense with id=1

3. **Search:** `searchExpenses(token="eyJhbGc...", category="Dining", startDate="2025-12-01", endDate="2025-12-31")`
   - Returns paginated dining expenses

4. **Update:** `updateExpense(token="eyJhbGc...", id=1, date="2025-12-28", merchant="Starbucks Coffee", amount="6.00", bank="Chase", category="Dining")`

5. **Delete:** `deleteExpense(token="eyJhbGc...", id=1)`

## Development

### Development Workflows

#### Docker (Recommended)
```bash
# Full rebuild
docker compose down && docker compose up --build --force-recreate

# View logs
docker compose logs -f backend
docker compose logs -f mcp

# Database only (for local development)
docker compose up -d postgres

# Teardown with data removal
docker compose down -v
```

#### Local Maven (Backend)
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

#### Local Maven (MCP Server)
```bash
cd mcp

# Build
mvn clean package

# Run standalone
java -jar target/frugalfoxmcp-0.0.1-SNAPSHOT.jar

# With custom config
FRUGALFOX_API_BASE_URL=http://localhost:8080 \\
java -jar target/frugalfoxmcp-0.0.1-SNAPSHOT.jar
```

### Architecture

#### Codebase Structure

**Backend (`backend/src/main/java/com/tgboyles/frugalfox/`):**
```
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

**MCP Server (`mcp/src/main/java/com/tgboyles/frugalfoxmcp/`):**
```
├── config/
│   ├── FrugalFoxApiConfig.java      # API configuration
│   └── McpToolsConfig.java          # MCP tool definitions (@McpTool)
├── dto/                             # Data transfer objects
├── service/
│   └── FrugalFoxApiClient.java      # HTTP client for API
└── FrugalfoxmcpApplication.java     # Main application
```

#### Database Schema

Managed by Flyway migrations in `backend/src/main/resources/db/migration/`

**V1__create_users_table.sql:**
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

**V2__create_expenses_table.sql:**
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

#### Security Model

- **Authentication**: JWT tokens (HS512, 24hr expiration configurable via `jwt.expiration`)
- **Password Storage**: BCrypt with 10+ rounds
- **User Isolation**: All expense queries automatically scoped to authenticated user via `@AuthenticationPrincipal User`
- **Validation**: Bean Validation (`@Valid`) on all request bodies
- **CORS**: Configured in SecurityConfig (currently permissive for development)
- **Public Endpoints**: `/auth/**`, `/actuator/health`, `/` (no authentication required)

#### Key Design Patterns

- **Repository Pattern**: Spring Data JPA repositories with custom specifications for dynamic queries
- **DTO Pattern**: Separate request/response objects in security package
- **Service Layer**: Business logic isolated from controllers
- **Global Exception Handling**: Consistent error responses via `@ControllerAdvice`
- **Lazy Loading**: `@Lazy` on PasswordEncoder to break circular dependency in SecurityConfig
- **User Scoping**: All expense operations filtered by authenticated user to prevent data leakage

#### Technology Stack

**Backend:**
- Spring Boot 4.0.1 (Spring MVC, Spring Data JPA, Spring Security)
- PostgreSQL 17 (production) / H2 (testing)
- Flyway for database migrations
- JWT authentication (jjwt 0.12.6)
- Java 23

**MCP Server:**
- Spring Boot 3.5.9
- Spring AI MCP Server 1.1.2
- Spring WebFlux (HTTP client)
- Java 25

### Testing Strategy

- **Unit Tests**: Service layer with Mockito
- **Integration Tests**: Controller tests with `@SpringBootTest` + MockMvc
- **Test Database**: H2 in-memory with PostgreSQL compatibility mode
- **Flyway**: Enabled in test profile to validate migrations

Run tests: `mvn test` (includes compilation → test → report generation)

### Configuration

**Application Properties:**
- `backend/src/main/resources/application.properties` - Production (PostgreSQL)
- `backend/src/test/resources/application.properties` - Test (H2 + Flyway)
- `mcp/src/main/resources/application.properties` - MCP Server

**Environment Variables (Docker):**
```bash
# Backend
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/frugalfox
SPRING_DATASOURCE_USERNAME=frugalfox
SPRING_DATASOURCE_PASSWORD=frugalfox

# MCP Server
FRUGALFOX_API_BASE_URL=http://backend:8080
FRUGALFOX_API_TIMEOUT=30000
```

**JWT Configuration (application.properties):**
```properties
jwt.secret=<256-bit-secret>
jwt.expiration=86400000  # 24 hours in milliseconds
```

## Working with AI Assistants (Claude Code)

This project includes [CLAUDE.md](CLAUDE.md), a comprehensive instruction file that helps AI coding assistants understand the codebase and contribute effectively.

### What is CLAUDE.md?

`CLAUDE.md` is a project context file that provides AI assistants with:
- **Project architecture** and structure
- **Development guidelines** and coding conventions
- **Security requirements** (critical for JWT authentication and user isolation)
- **Common patterns** used throughout the codebase
- **Anti-patterns** to avoid
- **Testing requirements** and best practices

### Using Claude Code Effectively

When working with [Claude Code](https://claude.com/claude-code) or similar AI assistants:

1. **Reference CLAUDE.md in your prompts**
   ```
   "Reference CLAUDE.md and add a new category filter endpoint"
   "Following CLAUDE.md patterns, implement expense analytics"
   ```

2. **The AI automatically reads it** - Claude Code and compatible editors automatically load `CLAUDE.md` as project context, ensuring consistent, high-quality contributions

3. **Key benefits:**
   - ✅ Follows existing patterns (service-repository-controller)
   - ✅ Enforces security requirements (user isolation, validation)
   - ✅ Creates proper Flyway migrations for schema changes
   - ✅ Writes tests following project conventions
   - ✅ Uses correct naming conventions and package structure

4. **Example tasks the AI can help with:**
   - "Add expense categories endpoint with search"
   - "Implement monthly spending analytics"
   - "Add expense notes field with migration"
   - "Create integration test for date filtering"

### Maintaining CLAUDE.md

When making significant architectural changes:
- Update `CLAUDE.md` to reflect new patterns
- Document new security requirements
- Add examples of new conventions
- Update the Postman collection reference if API changes

This ensures future AI-assisted development remains consistent with your evolving codebase.

## Contributing

This project follows:
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Spring Boot Best Practices](https://docs.spring.io/spring-boot/reference/using/structuring-your-code.html)
- [REST API Best Practices](https://blog.postman.com/rest-api-best-practices/)

**Prerequisites**: Java 23+ (backend), Java 25+ (MCP server), Maven 3.x, Docker

## License

MIT

## Related Resources

- [Model Context Protocol](https://modelcontextprotocol.io/) - MCP specification
- [Spring AI](https://docs.spring.io/spring-ai/reference/) - Spring AI documentation
- [Claude Code](https://claude.com/claude-code) - AI-powered coding assistant
