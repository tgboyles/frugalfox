<img width="300" height="300" alt="image" src="frontend/public/fox.png" />

# Frugal Fox

A full-stack expense tracking application demonstrating modern Java, Spring Boot, React, and AI technologies including agentic behaviors, Model Context Protocol (MCP), and chat capabilities.

## Overview

Frugal Fox is a demonstrative budgeting application that showcases:
- **Full-stack architecture** with Spring Boot backend and React frontend
- **JWT authentication** with multi-tenant data isolation
- **Model Context Protocol (MCP)** server for AI assistant integration
- **Modern development practices** with comprehensive testing and documentation

**Key Features:**
- Secure user registration and authentication
- Expense CRUD operations with advanced filtering
- Bulk CSV import for expenses (up to 1000 rows)
- CSV export with filters for backup and analysis
- Real-time search across categories, merchants, dates, and amounts
- Responsive web interface built with React and shadcn/ui
- MCP integration for AI-assisted expense management

## Technology Stack

**Backend:**
- Spring Boot 4.0.1 (Spring MVC, Spring Data JPA, Spring Security)
- PostgreSQL 17 (production) / H2 (testing)
- Flyway database migrations
- JWT authentication (jjwt 0.12.6)
- Java 23

**Frontend:**
- React 19 with TypeScript
- Vite build tool
- React Router v7 (routing)
- TanStack Query v5 (server state)
- shadcn/ui + Radix UI (components)
- Tailwind CSS v4 (styling)
- pnpm (package manager)

**MCP Server:**
- Spring Boot 3.5.9
- Spring AI MCP Server 1.1.2
- SSE transport for web-based clients
- Java 25

**Infrastructure:**
- Docker & Docker Compose for deployment
- CORS configured for frontend-backend communication

## Quick Start

### Full Stack with Docker Compose

Start all services (PostgreSQL + Backend + MCP Server + Frontend):

```bash
docker compose up --build
```

This starts:
- **PostgreSQL database** on port 5432
- **Backend API** on port 8080: http://localhost:8080
- **MCP Server** on port 8081: http://localhost:8081
- **Frontend** on port 3000: http://localhost:3000

Health checks:
```bash
curl http://localhost:8080/actuator/health  # Backend API
curl http://localhost:8081/actuator/health  # MCP Server
curl http://localhost:3000                   # Frontend
```

### Frontend Development Server

For frontend development with hot reload, run the dev server locally in a separate terminal:

```bash
cd frontend
pnpm install
pnpm dev
```

Access the development server at http://localhost:5173

### Backend Only (Local Development)

For backend development without Docker:

```bash
# Start PostgreSQL only
docker compose up -d postgres

# Run backend locally
cd backend
mvn spring-boot:run
```

## Project Architecture

```
frugal_fox/
├── backend/              # Spring Boot REST API
│   ├── src/
│   │   ├── main/java/com/tgboyles/frugalfox/
│   │   │   ├── expense/      # Expense domain (CRUD + search)
│   │   │   ├── user/         # User management
│   │   │   ├── security/     # JWT auth & filters
│   │   │   └── common/       # Exception handling, DTOs
│   │   ├── main/resources/
│   │   │   └── db/migration/ # Flyway migrations
│   │   └── test/
│   └── README.md         # Backend documentation
│
├── frontend/             # React web application
│   ├── src/
│   │   ├── components/   # React components (UI + custom)
│   │   ├── contexts/     # React contexts (Auth)
│   │   ├── pages/        # Page components
│   │   ├── lib/          # API client, types, utilities
│   │   └── App.tsx       # Main app with routing
│   └── README.md         # Frontend documentation
│
├── mcp/                  # Model Context Protocol server
│   ├── src/main/java/com/tgboyles/frugalfoxmcp/
│   │   ├── config/       # MCP tool definitions
│   │   ├── dto/          # Data transfer objects
│   │   └── service/      # API client
│   └── README.md         # MCP documentation (in root)
│
├── docker-compose.yml    # Service orchestration
├── CLAUDE.md            # AI assistant integration guide
└── README.md            # This file
```

**Design Principles:**
- **Domain-Driven Design**: Organized by feature/domain, not layer
- **Security First**: JWT authentication, user data isolation, input validation
- **API-First**: RESTful endpoints with comprehensive OpenAPI support
- **Type Safety**: TypeScript frontend, strong Java typing
- **Test Coverage**: Unit and integration tests for all layers
- **Accessibility**: Automated WCAG compliance testing with ESLint

## Component Documentation

Detailed documentation for each component:

- **[Backend](backend/README.md)** - How to build, test, and extend the Spring Boot API
- **[Frontend](frontend/README.md)** - React app architecture, development guide, and accessibility testing
- **[MCP Server](mcp/README.md)** - AI assistant integration (documented below)
- **[CLAUDE.md](CLAUDE.md)** - AI coding assistant integration guide

## License

MIT
