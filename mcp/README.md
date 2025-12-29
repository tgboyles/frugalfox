# Frugal Fox MCP Server

A Model Context Protocol (MCP) server that provides programmatic access to the Frugal Fox expense tracking API. This MCP server exposes all Frugal Fox API endpoints as tools that can be used by AI assistants and other MCP clients.

## Overview

This MCP server acts as a bridge between MCP clients (like Claude Desktop, IDEs, or custom AI applications) and the Frugal Fox backend API. It provides 9 tools covering all aspects of expense management:

- User authentication (register, login)
- Expense CRUD operations (create, read, update, delete)
- Advanced expense search and filtering
- Health monitoring

## Prerequisites

- Java 25 or higher
- Maven 3.x
- Frugal Fox backend API running (default: http://localhost:8080)

## Building

```bash
mvn clean package
```

This creates an executable JAR at `target/frugalfoxmcp-0.0.1-SNAPSHOT.jar`.

## Running

### Standalone Execution

```bash
java -jar target/frugalfoxmcp-0.0.1-SNAPSHOT.jar
```

### With Custom API URL

```bash
java -jar target/frugalfoxmcp-0.0.1-SNAPSHOT.jar \
  --frugalfox.api.base-url=http://your-api-host:port
```

### As an MCP Server

The server uses STDIO for communication, following the MCP protocol. Configure your MCP client to execute:

```bash
java -jar /path/to/frugalfoxmcp-0.0.1-SNAPSHOT.jar
```

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Frugal Fox API Configuration
frugalfox.api.base-url=http://localhost:8080
frugalfox.api.timeout=30000

# MCP Server Configuration
spring.ai.mcp.server.type=SYNC
spring.ai.mcp.server.name=frugalfoxmcp
spring.ai.mcp.server.version=1.0.0
```

## Available Tools

### Authentication Tools

#### `registerUser`
Register a new user account.

**Parameters:**
- `username` (string, required): Username for the new account
- `password` (string, required): Password for the new account
- `email` (string, required): Email address for the new account

**Returns:**
```json
{
  "success": true,
  "token": "eyJhbGc...",
  "username": "john",
  "email": "john@example.com"
}
```

#### `loginUser`
Login with existing credentials.

**Parameters:**
- `username` (string, required): Username
- `password` (string, required): Password

**Returns:** Same as `registerUser`

#### `healthCheck`
Check if the Frugal Fox API is running and healthy.

**Returns:**
```json
{
  "success": true,
  "status": "{\"status\":\"UP\"}"
}
```

### Expense Management Tools

#### `createExpense`
Create a new expense.

**Parameters:**
- `token` (string, required): JWT token from authentication
- `date` (string, required): Expense date in YYYY-MM-DD format
- `merchant` (string, required): Merchant name
- `amount` (string, required): Expense amount (positive number)
- `bank` (string, required): Bank name
- `category` (string, required): Expense category

**Returns:**
```json
{
  "success": true,
  "expense": {
    "id": 1,
    "date": "2025-12-26",
    "merchant": "Whole Foods",
    "amount": "125.50",
    "bank": "Chase",
    "category": "Groceries",
    "createdAt": "2025-12-26T10:30:00",
    "updatedAt": "2025-12-26T10:30:00"
  }
}
```

#### `getExpense`
Get a specific expense by ID.

**Parameters:**
- `token` (string, required): JWT token from authentication
- `id` (number, required): Expense ID

**Returns:** Same format as `createExpense`

#### `updateExpense`
Update an existing expense.

**Parameters:**
- `token` (string, required): JWT token from authentication
- `id` (number, required): Expense ID to update
- `date` (string, required): Expense date in YYYY-MM-DD format
- `merchant` (string, required): Merchant name
- `amount` (string, required): Expense amount (positive number)
- `bank` (string, required): Bank name
- `category` (string, required): Expense category

**Returns:** Same format as `createExpense`

#### `deleteExpense`
Delete an expense by ID.

**Parameters:**
- `token` (string, required): JWT token from authentication
- `id` (number, required): Expense ID to delete

**Returns:**
```json
{
  "success": true,
  "message": "Expense deleted successfully"
}
```

### Search and Filter Tools

#### `searchExpenses`
Search and filter expenses with pagination.

**Parameters:**
- `token` (string, required): JWT token from authentication
- `category` (string, optional): Category to filter by (exact match)
- `bank` (string, optional): Bank to filter by (exact match)
- `merchant` (string, optional): Merchant to filter by (partial, case-insensitive)
- `startDate` (string, optional): Start date for date range (YYYY-MM-DD)
- `endDate` (string, optional): End date for date range (YYYY-MM-DD)
- `minAmount` (string, optional): Minimum amount
- `maxAmount` (string, optional): Maximum amount
- `page` (number, optional): Page number (0-indexed)
- `size` (number, optional): Page size (default 20)
- `sort` (string, optional): Sort field and direction (e.g., 'date,desc')

**Returns:**
```json
{
  "success": true,
  "data": {
    "content": [/* array of expense objects */],
    "pageable": {"pageNumber": 0, "pageSize": 20},
    "totalElements": 42,
    "totalPages": 3
  }
}
```

#### `listAllExpenses`
List all expenses for the authenticated user with pagination.

**Parameters:**
- `token` (string, required): JWT token from authentication
- `page` (number, optional): Page number (0-indexed)
- `size` (number, optional): Page size (default 20)

**Returns:** Same format as `searchExpenses`

## Usage Example

Here's a typical workflow using the MCP tools:

1. **Register a new user:**
```
registerUser(username="alice", password="secure123", email="alice@example.com")
→ Returns token: "eyJhbGc..."
```

2. **Create an expense:**
```
createExpense(
  token="eyJhbGc...",
  date="2025-12-28",
  merchant="Starbucks",
  amount="5.50",
  bank="Chase",
  category="Dining"
)
→ Returns expense with id=1
```

3. **Search expenses:**
```
searchExpenses(
  token="eyJhbGc...",
  category="Dining",
  startDate="2025-12-01",
  endDate="2025-12-31"
)
→ Returns paginated list of dining expenses for December 2025
```

4. **Update an expense:**
```
updateExpense(
  token="eyJhbGc...",
  id=1,
  date="2025-12-28",
  merchant="Starbucks Coffee",
  amount="6.00",
  bank="Chase",
  category="Dining"
)
```

5. **Delete an expense:**
```
deleteExpense(token="eyJhbGc...", id=1)
```

## Configuring MCP Clients

### Claude Desktop

Claude Desktop can use MCP servers to extend Claude's capabilities with custom tools. Here's how to configure it:

#### Step 1: Build the MCP Server

First, build the JAR file if you haven't already:

```bash
cd mcp
mvn clean package
```

This creates `target/frugalfoxmcp-0.0.1-SNAPSHOT.jar`.

#### Step 2: Locate the Configuration File

The Claude Desktop configuration file location depends on your operating system:

**macOS:**
```
~/Library/Application Support/Claude/claude_desktop_config.json
```

**Windows:**
```
%APPDATA%\Claude\claude_desktop_config.json
```

**Linux:**
```
~/.config/Claude/claude_desktop_config.json
```

If the file doesn't exist, create it.

#### Step 3: Add the Frugal Fox MCP Server

Edit `claude_desktop_config.json` and add the Frugal Fox server configuration:

```json
{
  "mcpServers": {
    "frugalfox": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/frugal_fox/mcp/target/frugalfoxmcp-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

**Important Notes:**
- Replace `/absolute/path/to/frugal_fox` with the actual absolute path to your project directory
- Use forward slashes `/` even on Windows
- The path must be absolute, not relative

**Example for macOS/Linux:**
```json
{
  "mcpServers": {
    "frugalfox": {
      "command": "java",
      "args": [
        "-jar",
        "/Users/yourname/projects/frugal_fox/mcp/target/frugalfoxmcp-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

**Example for Windows:**
```json
{
  "mcpServers": {
    "frugalfox": {
      "command": "java",
      "args": [
        "-jar",
        "C:/Users/yourname/projects/frugal_fox/mcp/target/frugalfoxmcp-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

#### Step 4: Configure API Connection (Optional)

If your Frugal Fox API is not running on `http://localhost:8080`, you can specify a custom URL:

```json
{
  "mcpServers": {
    "frugalfox": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/frugal_fox/mcp/target/frugalfoxmcp-0.0.1-SNAPSHOT.jar",
        "--frugalfox.api.base-url=http://your-api-host:port"
      ]
    }
  }
}
```

#### Step 5: Restart Claude Desktop

1. Completely quit Claude Desktop (not just close the window)
2. Relaunch Claude Desktop
3. The MCP server will start automatically when needed

#### Step 6: Verify the Connection

In Claude Desktop, you can verify the MCP server is working by:

1. Starting a new conversation
2. Asking Claude to check if the Frugal Fox API is available:
   ```
   Can you check if the Frugal Fox API is healthy?
   ```
3. Claude should use the `healthCheck` tool and report the API status

#### Troubleshooting Claude Desktop Integration

**Tools not appearing:**
- Check that Java 25+ is installed: `java -version`
- Verify the JAR file exists at the specified path
- Check the logs in Claude Desktop's developer console (View → Developer → Developer Tools)
- Ensure the JSON syntax is correct (no trailing commas, proper quotes)

**Connection errors:**
- Make sure the Frugal Fox backend API is running: `curl http://localhost:8080/actuator/health`
- Check the MCP server logs: `~/.frugalfoxmcp/logs/frugalfoxmcp.log`
- Verify the `base-url` points to your running API instance

**Permission issues on macOS/Linux:**
- Ensure the JAR file is readable: `chmod +r /path/to/frugalfoxmcp-0.0.1-SNAPSHOT.jar`

### Other MCP Clients

For other MCP-compatible clients (IDEs, custom applications, etc.):

1. Configure the client to execute: `java -jar /path/to/frugalfoxmcp-0.0.1-SNAPSHOT.jar`
2. The server communicates via STDIO following the MCP protocol specification
3. Refer to your client's documentation for specific configuration steps

**Common MCP clients:**
- Cline (VS Code extension)
- Continue (VS Code extension)
- Zed editor
- Custom applications using MCP SDKs

## Error Handling

All tools return a `success` field indicating whether the operation succeeded:

**Success:**
```json
{
  "success": true,
  "...": "...other fields..."
}
```

**Error:**
```json
{
  "success": false,
  "error": "Error message describing what went wrong"
}
```

Common errors:
- `401 Unauthorized`: Invalid or missing JWT token
- `403 Forbidden`: Token valid but user doesn't have access to requested resource
- `404 Not Found`: Expense ID doesn't exist or doesn't belong to the user
- `400 Bad Request`: Invalid input (e.g., negative amount, future date)

## Development

### Project Structure

```
mcp/
├── src/main/java/com/tgboyles/frugalfoxmcp/
│   ├── config/
│   │   ├── FrugalFoxApiConfig.java      # API configuration
│   │   └── McpToolsConfig.java          # MCP tool definitions
│   ├── dto/                             # Data transfer objects
│   ├── service/
│   │   └── FrugalFoxApiClient.java      # HTTP client for API
│   └── FrugalfoxmcpApplication.java     # Main application
└── src/main/resources/
    └── application.properties            # Configuration
```

### Adding New Tools

1. Define request/response records in `McpToolsConfig.java`
2. Implement a Function class
3. Create a `@Bean` method with `@Description` annotation
4. Rebuild with `mvn clean package`

### Technologies

- **Spring Boot 3.5.9**: Application framework
- **Spring AI MCP Server 1.1.2**: MCP protocol implementation
- **Spring WebFlux**: Reactive HTTP client
- **Java 25**: Language and runtime

## Troubleshooting

**Server won't start:**
- Check Java version: `java -version` (must be 25+)
- Verify Frugal Fox API is running: `curl http://localhost:8080/actuator/health`
- Check logs in `~/.frugalfoxmcp/logs/frugalfoxmcp.log`

**Tools return errors:**
- Ensure you're using a valid JWT token from `registerUser` or `loginUser`
- Verify the Frugal Fox API is accessible at the configured URL
- Check that all required parameters are provided

**Connection timeouts:**
- Increase timeout in `application.properties`: `frugalfox.api.timeout=60000`
- Verify network connectivity to the API

## License

MIT

## Related Projects

- [Frugal Fox Backend](../backend) - The REST API this MCP server connects to
- [Model Context Protocol](https://modelcontextprotocol.io/) - MCP specification
- [Spring AI](https://docs.spring.io/spring-ai/reference/) - Spring AI documentation

## Support

For issues related to:
- **MCP Server**: Open an issue in this repository
- **Frugal Fox API**: See the [backend README](../README.md)
- **MCP Protocol**: Visit [Model Context Protocol documentation](https://modelcontextprotocol.io/)
