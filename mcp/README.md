# Frugal Fox MCP Server

Model Context Protocol (MCP) server providing AI assistant access to the Frugal Fox expense tracking API.

## Overview

The Frugal Fox MCP server exposes the expense tracking API as a set of tools that can be used by AI assistants like Claude Desktop, Cline, Continue, Zed, and other MCP-compatible clients. It acts as a bridge between AI assistants and the Frugal Fox backend, enabling natural language interaction with expense data.

**Key Features:**
- 9 MCP tools covering authentication, CRUD operations, and search
- SSE (Server-Sent Events) transport for web-based deployment
- Docker-ready for containerized deployment
- Compatible with all major MCP clients (Claude Desktop, Cline, Continue, Zed)
- Type-safe Java implementation with Spring Boot

## Technology Stack

- **Framework**: Spring Boot 3.5.9
- **MCP Library**: Spring AI MCP Server 1.1.2
- **HTTP Client**: Spring WebFlux (reactive)
- **Build Tool**: Maven
- **Language**: Java 25
- **Transport**: SSE (Server-Sent Events)

## Quick Start

### Using Docker (Recommended)

Start the MCP server with the full stack:

```bash
# From project root
docker compose up --build mcp
```

The MCP server will be available at `http://localhost:8081/sse`

Health check:
```bash
curl http://localhost:8081/actuator/health
```

### Local Development

**Prerequisites:**
- Java 25+
- Maven 3.x
- Frugal Fox backend running (default: http://localhost:8080)

**Steps:**

1. Build the project:
```bash
cd mcp
mvn clean package
```

2. Run the server:
```bash
java -jar target/frugalfoxmcp-0.0.1-SNAPSHOT.jar
```

3. Verify it's running:
```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8081/sse  # Should hang (SSE connection)
```

## Building and Testing

### Build Commands

```bash
# Full build
mvn clean package

# Build without tests
mvn clean package -DskipTests

# Compile only
mvn clean compile
```

Build artifacts are created in `target/` directory.

### Running the Server

```bash
# Run with default configuration
java -jar target/frugalfoxmcp-0.0.1-SNAPSHOT.jar

# Run with custom API URL
FRUGALFOX_API_BASE_URL=http://backend:8080 \
java -jar target/frugalfoxmcp-0.0.1-SNAPSHOT.jar

# Run with custom port
SERVER_PORT=8082 \
java -jar target/frugalfoxmcp-0.0.1-SNAPSHOT.jar
```

## Project Architecture

### Directory Structure

```
mcp/
├── src/
│   ├── main/
│   │   ├── java/com/tgboyles/frugalfoxmcp/
│   │   │   ├── config/
│   │   │   │   ├── FrugalFoxApiConfig.java    # API client configuration
│   │   │   │   └── McpToolsConfig.java        # MCP tool definitions
│   │   │   ├── dto/
│   │   │   │   ├── ExpenseDto.java            # Expense data transfer object
│   │   │   │   ├── AuthRequestDto.java        # Auth request DTO
│   │   │   │   ├── AuthResponseDto.java       # Auth response DTO
│   │   │   │   └── ...                        # Other DTOs
│   │   │   ├── service/
│   │   │   │   └── FrugalFoxApiClient.java    # HTTP client for backend API
│   │   │   └── FrugalfoxmcpApplication.java   # Main application class
│   │   └── resources/
│   │       └── application.properties         # Configuration
│   └── test/                                   # Tests (if any)
├── pom.xml                                     # Maven configuration
└── README.md                                   # This file
```

### Architectural Components

**MCP Tool Configuration** (`McpToolsConfig.java`):
- Defines all 9 MCP tools using `@McpTool` annotations
- Maps tool parameters to API client methods
- Handles response formatting and error handling

**API Client** (`FrugalFoxApiClient.java`):
- Reactive HTTP client using Spring WebFlux
- Calls Frugal Fox backend REST API
- Handles authentication, requests, and responses
- Configurable base URL and timeout

**DTOs** (`dto/` package):
- Type-safe data transfer objects
- Match backend API request/response formats
- Used for serialization/deserialization

### How It Works

```
AI Assistant (Claude Desktop)
    ↓ STDIO
mcp-remote bridge (npx)
    ↓ HTTP/SSE
MCP Server (port 8081)
    ↓ HTTP/REST
Backend API (port 8080)
    ↓ SQL
PostgreSQL Database
```

1. **AI Assistant** invokes MCP tool (e.g., "create expense")
2. **mcp-remote bridge** translates STDIO to HTTP/SSE
3. **MCP Server** receives tool invocation, calls API client
4. **API Client** makes REST call to backend
5. **Backend** processes request, returns response
6. **MCP Server** formats response and returns to AI

## Available MCP Tools

### Authentication Tools

**`registerUser`** - Register a new user account
- **Parameters:**
  - `username` (string, required) - Unique username
  - `password` (string, required) - User password
  - `email` (string, required) - User email address
- **Returns:** `{success: boolean, token: string, username: string, email: string}`

**`loginUser`** - Login with existing credentials
- **Parameters:**
  - `username` (string, required) - Username
  - `password` (string, required) - Password
- **Returns:** `{success: boolean, token: string, username: string, email: string}`

**`healthCheck`** - Check if the Frugal Fox API is healthy
- **Parameters:** None
- **Returns:** `{success: boolean, status: string}`

### Expense Management Tools

**`createExpense`** - Create a new expense
- **Parameters:**
  - `token` (string, required) - JWT authentication token
  - `date` (string, required) - Expense date (ISO 8601: YYYY-MM-DD)
  - `merchant` (string, required) - Merchant name
  - `amount` (string, required) - Amount (decimal)
  - `bank` (string, required) - Bank name
  - `category` (string, required) - Expense category
- **Returns:** `{success: boolean, expense: object}`

**`getExpense`** - Get a specific expense by ID
- **Parameters:**
  - `token` (string, required) - JWT authentication token
  - `id` (string, required) - Expense ID
- **Returns:** `{success: boolean, expense: object}`

**`updateExpense`** - Update an existing expense
- **Parameters:**
  - `token` (string, required) - JWT authentication token
  - `id` (string, required) - Expense ID
  - `date` (string, required) - Expense date
  - `merchant` (string, required) - Merchant name
  - `amount` (string, required) - Amount
  - `bank` (string, required) - Bank name
  - `category` (string, required) - Category
- **Returns:** `{success: boolean, expense: object}`

**`deleteExpense`** - Delete an expense
- **Parameters:**
  - `token` (string, required) - JWT authentication token
  - `id` (string, required) - Expense ID
- **Returns:** `{success: boolean, message: string}`

### Search and Filter Tools

**`searchExpenses`** - Search and filter expenses with pagination
- **Parameters:**
  - `token` (string, required) - JWT authentication token
  - `category` (string, optional) - Filter by category (exact match)
  - `bank` (string, optional) - Filter by bank (exact match)
  - `merchant` (string, optional) - Filter by merchant (partial, case-insensitive)
  - `startDate` (string, optional) - Start date (ISO 8601)
  - `endDate` (string, optional) - End date (ISO 8601)
  - `minAmount` (string, optional) - Minimum amount (inclusive)
  - `maxAmount` (string, optional) - Maximum amount (inclusive)
  - `page` (string, optional) - Page number (0-indexed, default: 0)
  - `size` (string, optional) - Page size (default: 20)
  - `sort` (string, optional) - Sort field and direction (e.g., "date,desc")
- **Returns:** `{success: boolean, data: object}` with paginated results

**`listAllExpenses`** - List all expenses with pagination
- **Parameters:**
  - `token` (string, required) - JWT authentication token
  - `page` (string, optional) - Page number (default: 0)
  - `size` (string, optional) - Page size (default: 20)
- **Returns:** `{success: boolean, data: object}` with paginated results

## Configuration

### Application Properties

**Default Configuration** (`src/main/resources/application.properties`):
```properties
# Server
server.port=8081

# MCP Server
spring.ai.mcp.server.type=SYNC
spring.ai.mcp.server.name=frugalfoxmcp
spring.ai.mcp.server.version=1.0.0
spring.ai.mcp.server.protocol=sse

# Frugal Fox API
frugalfox.api.base-url=${FRUGALFOX_API_BASE_URL:http://localhost:8080}
frugalfox.api.timeout=${FRUGALFOX_API_TIMEOUT:30000}
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | MCP server port | `8081` |
| `FRUGALFOX_API_BASE_URL` | Backend API base URL | `http://localhost:8080` |
| `FRUGALFOX_API_TIMEOUT` | HTTP client timeout (ms) | `30000` |

**Docker Compose:**
When running via Docker, the backend URL is automatically set to `http://backend:8080` via environment variables in `docker-compose.yml`.

## Client Configuration

### Claude Desktop

1. **Start MCP Server:**
   ```bash
   docker compose up mcp
   ```

2. **Edit Claude Desktop config:**

   **Location:**
   - macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
   - Windows: `%APPDATA%\Claude\claude_desktop_config.json`
   - Linux: `~/.config/Claude/claude_desktop_config.json`

   **Configuration:**
   ```json
   {
     "mcpServers": {
       "frugalfox": {
         "command": "npx",
         "args": ["mcp-remote", "http://localhost:8081/sse"]
       }
     }
   }
   ```

3. **Restart Claude Desktop**

4. **Test:** Ask Claude "Can you check if the Frugal Fox API is healthy?"

**How it works:**
- `npx mcp-remote` acts as a bridge between Claude Desktop (STDIO) and the MCP server (SSE)
- This allows the MCP server to run as a containerized web service
- The bridge automatically installs on first use via `npx`

### Cline, Continue, Zed (STDIO Clients)

For STDIO-based MCP clients, configure them to directly execute the JAR:

```json
{
  "mcpServers": {
    "frugalfox": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/frugal_fox/mcp/target/frugalfoxmcp-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

Replace `/path/to/frugal_fox` with the actual path to your project.

### Custom MCP Clients

For custom applications using MCP SDKs:

**SSE Transport (Web):**
```
URL: http://localhost:8081/sse
Transport: Server-Sent Events (SSE)
```

**STDIO Transport (Local):**
```bash
java -jar target/frugalfoxmcp-0.0.1-SNAPSHOT.jar
```

## Usage Example

Typical workflow with an AI assistant:

```
User: "Register me as alice with email alice@example.com"
AI: Uses registerUser(username="alice", password="secure123", email="alice@example.com")
    Returns: {success: true, token: "eyJ..."}

User: "Add a $50 expense from Starbucks yesterday"
AI: Uses createExpense(token="eyJ...", date="2025-12-31", merchant="Starbucks",
                       amount="50.00", bank="Chase", category="Dining")
    Returns: {success: true, expense: {...}}

User: "Show me all my dining expenses from December"
AI: Uses searchExpenses(token="eyJ...", category="Dining",
                        startDate="2025-12-01", endDate="2025-12-31")
    Returns: {success: true, data: {...}}

User: "Delete expense ID 1"
AI: Uses deleteExpense(token="eyJ...", id="1")
    Returns: {success: true, message: "Expense deleted successfully"}
```

## Development Workflows

### Adding New Tools

To add a new MCP tool:

1. **Add method to API Client** (`FrugalFoxApiClient.java`):
   ```java
   public Mono<String> callNewEndpoint(String token, String param) {
       return webClient.get()
           .uri("/new-endpoint?param=" + param)
           .header("Authorization", "Bearer " + token)
           .retrieve()
           .bodyToMono(String.class);
   }
   ```

2. **Define MCP tool** in `McpToolsConfig.java`:
   ```java
   @McpTool(description = "Description of new tool")
   public String newTool(
       @McpToolParameter(required = true, description = "Token") String token,
       @McpToolParameter(required = true, description = "Param") String param
   ) {
       return apiClient.callNewEndpoint(token, param).block();
   }
   ```

3. **Create DTOs** (if needed) in `dto/` package

4. **Rebuild:**
   ```bash
   mvn clean package
   ```

5. **Test:** Restart server and test with MCP client

### Debugging

**View logs:**
```bash
# Docker logs
docker compose logs -f mcp

# Local logs (if configured)
tail -f ~/.frugalfoxmcp/logs/frugalfoxmcp.log
```

**Enable debug logging:**

Add to `application.properties`:
```properties
logging.level.com.tgboyles.frugalfoxmcp=DEBUG
logging.level.org.springframework.ai.mcp=DEBUG
```

**Test SSE endpoint:**
```bash
curl -N http://localhost:8081/sse
# Should hang, indicating SSE connection is active
```

**Test health endpoint:**
```bash
curl http://localhost:8081/actuator/health
# Should return: {"status":"UP"}
```

## Troubleshooting

**MCP Server won't start:**
```bash
# Check Java version
java -version  # Must be 25+

# Check if port is available
lsof -i :8081

# View logs
docker compose logs mcp
```

**Backend connection issues:**
```bash
# Verify backend is running
curl http://localhost:8080/actuator/health

# Check API base URL configuration
docker compose exec mcp env | grep FRUGALFOX_API_BASE_URL

# Test API call from MCP container
docker compose exec mcp curl http://backend:8080/actuator/health
```

**Claude Desktop connection issues:**
```bash
# Verify Node.js is installed (for mcp-remote)
node --version

# Test SSE endpoint
curl -N http://localhost:8081/sse

# Check Claude Desktop logs
# View → Developer → Developer Tools → Console
```

**Tools not appearing in Claude Desktop:**
```bash
# Rebuild and restart
mvn clean package
docker compose up --build mcp

# Check logs for "Registered tools: 9"
docker compose logs mcp | grep "Registered tools"
```

**API timeout issues:**
```bash
# Increase timeout in application.properties
frugalfox.api.timeout=60000  # 60 seconds

# Or via environment variable
FRUGALFOX_API_TIMEOUT=60000 java -jar target/frugalfoxmcp-0.0.1-SNAPSHOT.jar
```

## Contribution Guidelines

### Prerequisites

- Java 25+
- Maven 3.x
- Understanding of Spring Boot and MCP concepts

### Code Style

Follow **Google Java Style Guide**:
- 2-space indentation
- Line length: 100 characters
- Use `var` sparingly (prefer explicit types)

### Adding Features

**Before submitting:**
1. Build succeeds: `mvn clean package`
2. Server starts without errors
3. Tools are registered correctly (check logs)
4. Test with actual MCP client (Claude Desktop, etc.)
5. Update this README if new tools added

### Pull Request Checklist

- [ ] Code builds: `mvn clean package`
- [ ] No compiler warnings
- [ ] New tools documented in README
- [ ] Environment variables documented (if added)
- [ ] Tested with at least one MCP client
- [ ] Logs indicate successful tool registration

## Related Documentation

- [Root README](../README.md) - Full project overview
- [Backend README](../backend/README.md) - Backend API documentation
- [Frontend README](../frontend/README.md) - Frontend application
- [CLAUDE.md](../CLAUDE.md) - AI assistant integration guide

## External Resources

- [Model Context Protocol](https://modelcontextprotocol.io/) - Official MCP specification
- [Spring AI](https://docs.spring.io/spring-ai/reference/) - Spring AI documentation
- [Spring Boot](https://docs.spring.io/spring-boot/reference/) - Spring Boot reference
- [Claude Desktop](https://claude.ai/download) - Download Claude Desktop

## License

MIT
