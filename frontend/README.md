# Frugal Fox Frontend

Modern expense tracking web application built with React, TypeScript, and shadcn/ui.

## Tech Stack

- **Framework**: React 19 with TypeScript
- **Build Tool**: Vite
- **Routing**: React Router v7
- **State Management**: TanStack Query (React Query)
- **HTTP Client**: Axios
- **UI Components**: shadcn/ui + Radix UI
- **Styling**: Tailwind CSS v4
- **Icons**: Lucide React

## Getting Started

### Prerequisites

- Node.js 18+ (or compatible runtime)
- pnpm (recommended) or npm

### Installation

1. Install dependencies:
```bash
pnpm install
```

2. Create environment file:
```bash
cp .env.example .env
```

3. Configure the API base URL in `.env`:
```env
VITE_API_BASE_URL=http://localhost:8080
```

### Development

Start the development server:
```bash
pnpm dev
```

The app will be available at `http://localhost:5173`

### Build

Build for production:
```bash
pnpm build
```

Preview the production build:
```bash
pnpm preview
```

## Project Structure

```
frontend/
├── src/
│   ├── components/
│   │   ├── ui/                 # shadcn/ui components
│   │   ├── app-sidebar.tsx     # Application sidebar
│   │   └── ProtectedRoute.tsx  # Route protection wrapper
│   ├── contexts/
│   │   └── AuthContext.tsx     # Authentication context
│   ├── pages/
│   │   ├── AuthPage.tsx        # Login/Register page
│   │   ├── DashboardLayout.tsx # Dashboard layout with sidebar
│   │   ├── DashboardHome.tsx   # Dashboard home page
│   │   ├── ExpensesPage.tsx    # Expense list (placeholder)
│   │   ├── AddExpensePage.tsx  # Add expense form (placeholder)
│   │   └── SettingsPage.tsx    # Settings (placeholder)
│   ├── lib/
│   │   ├── api.ts              # API client with axios
│   │   └── utils.ts            # Utility functions
│   ├── App.tsx                 # Main app with routing
│   ├── main.tsx                # App entry point
│   └── index.css               # Global styles
├── public/                      # Static assets
├── components.json              # shadcn/ui configuration
├── tailwind.config.js           # Tailwind configuration
├── tsconfig.json                # TypeScript configuration
└── vite.config.ts               # Vite configuration
```

## Features

### Authentication
- JWT-based authentication
- Login and registration forms
- Persistent sessions via localStorage
- Automatic token injection in API requests
- Protected routes with redirect to login

### Dashboard
- Responsive sidebar navigation
- Dashboard home with expense metrics
- Protected route system
- Clean, modern UI following shadcn design patterns

### API Integration
- Centralized axios client with interceptors
- Automatic JWT token management
- Error handling with 401 redirect
- Type-safe API methods for auth and expenses

## Available Routes

- `/login` - Authentication page (login/register)
- `/dashboard` - Dashboard home (protected)
- `/dashboard/expenses` - Expense list (protected, placeholder)
- `/dashboard/add-expense` - Add new expense (protected, placeholder)
- `/dashboard/settings` - User settings (protected, placeholder)

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `VITE_API_BASE_URL` | Backend API base URL | `http://localhost:8080` |

## Design System

The UI follows the [shadcn/ui authentication example](https://ui.shadcn.com/examples/authentication) and [dashboard example](https://ui.shadcn.com/examples/dashboard) design patterns:

- Split-screen authentication layout
- Sidebar navigation with collapsible menu
- Card-based dashboard metrics
- Responsive design with mobile support
- Consistent spacing and typography

## Development Notes

### Adding New shadcn Components

```bash
pnpm dlx shadcn@latest add [component-name]
```

### Type Safety

The project uses TypeScript with strict mode enabled. All API responses and component props should be properly typed.

### Code Style

- Use functional components with hooks
- Prefer named exports for pages and contexts
- Use default exports for page components
- Follow existing file naming conventions (PascalCase for components, kebab-case for utilities)
- Always follow ESLint directives / config

## TODO

The following features are planned but not yet implemented:

- [ ] Expense list view with filtering and sorting
- [ ] Add/edit expense forms
- [ ] Delete expense confirmation
- [ ] Settings page with profile management
- [ ] Responsive mobile optimizations
- [ ] Loading states and error boundaries
- [ ] Toast notifications
- [ ] Date range pickers for filtering
- [ ] Chart visualizations for expense analytics

## License

This project is part of the Frugal Fox expense tracking application.
