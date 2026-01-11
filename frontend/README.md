# Frugal Fox Frontend

Modern expense tracking web application built with React 19, TypeScript, and shadcn/ui.

## Technology Stack

- **Framework**: React 19 with TypeScript
- **Build Tool**: Vite
- **Routing**: React Router v7
- **State Management**: TanStack Query v5 (React Query)
- **HTTP Client**: Axios
- **UI Components**: shadcn/ui + Radix UI
- **Styling**: Tailwind CSS v4
- **Icons**: Lucide React
- **Charts**: Recharts
- **Package Manager**: pnpm

## Quick Start

### Prerequisites

- Node.js 18+ (or compatible runtime)
- pnpm (recommended) or npm
- Docker & Docker Compose (for containerized deployment)

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

The app will be available at http://localhost:5173

### Build

Build for production:
```bash
pnpm build
```

Preview the production build:
```bash
pnpm preview
```

### Docker Deployment

The frontend application is fully containerized and can be run as part of the full-stack Docker Compose setup:

```bash
# From project root
docker compose up --build
```

This starts the frontend on http://localhost:3000 along with the backend, database, and MCP server.

The Docker setup uses:
- **Single-stage build** with Node.js for building and serving
- **Vite preview server** for serving the built application

### Testing

Run TypeScript type checking:
```bash
pnpm build
```

Run linter:
```bash
pnpm lint
```

## Project Architecture

### Directory Structure

```
frontend/
├── src/
│   ├── components/
│   │   ├── ui/                 # shadcn/ui base components
│   │   │   ├── button.tsx
│   │   │   ├── card.tsx
│   │   │   ├── input.tsx
│   │   │   └── ...
│   │   ├── app-sidebar.tsx     # Application sidebar navigation
│   │   └── ProtectedRoute.tsx  # Route protection wrapper
│   │
│   ├── contexts/
│   │   └── AuthContext.tsx     # Authentication context provider
│   │
│   ├── pages/
│   │   ├── AuthPage.tsx        # Login/Register page
│   │   ├── DashboardLayout.tsx # Dashboard layout with sidebar
│   │   ├── DashboardHome.tsx   # Dashboard home page
│   │   ├── ExpensesPage.tsx    # Expense list (placeholder)
│   │   ├── AddExpensePage.tsx  # Add expense form (placeholder)
│   │   └── SettingsPage.tsx    # Settings (placeholder)
│   │
│   ├── lib/
│   │   ├── api.ts              # Axios API client with interceptors
│   │   ├── types.ts            # Reusable TypeScript types
│   │   └── utils.ts            # Utility functions (cn helper)
│   │
│   ├── App.tsx                 # Main app with routing
│   ├── main.tsx                # App entry point
│   └── index.css               # Global styles & Tailwind
│
├── public/                      # Static assets (logo, etc.)
├── components.json              # shadcn/ui configuration
├── tailwind.config.js           # Tailwind CSS configuration
├── tsconfig.json                # TypeScript configuration
├── vite.config.ts               # Vite configuration
├── .env.example                 # Environment variables template
└── package.json                 # Dependencies and scripts
```

### Architectural Patterns

**Component Architecture:**
- **Functional components** with React hooks (no class components)
- **Default exports** for page components
- **Named exports** for contexts, utilities, and reusable components
- **PascalCase** for component files (e.g., `DashboardHome.tsx`)
- **kebab-case** for utility files (e.g., `api.ts`, `utils.ts`)

**State Management Strategy:**
- **TanStack Query** for server state (data from API)
- **React Context** for global client state (authentication)
- **Local component state** (`useState`) for UI-only state

**Routing:**
- React Router v7 with BrowserRouter
- Protected routes using `ProtectedRoute` wrapper
- Automatic redirect to `/login` for unauthenticated users

**API Integration:**
- Centralized axios client in [lib/api.ts](src/lib/api.ts)
- Automatic JWT token injection via request interceptor
- Automatic 401 handling via response interceptor
- Type-safe API methods

### Key Components

**Authentication System:**
- `AuthContext` - Provides authentication state and methods
- `AuthPage` - Split-screen login/register interface
- `ProtectedRoute` - Route wrapper that requires authentication
- JWT token stored in localStorage

**Layout System:**
- `DashboardLayout` - Sidebar navigation wrapper
- `app-sidebar` - Collapsible sidebar with navigation items
- Responsive design with mobile support (via SidebarProvider)

**UI Components:**
All UI components from shadcn/ui are in `components/ui/`:
- Built on Radix UI primitives
- Fully accessible with ARIA support
- Customizable with Tailwind CSS
- Type-safe with TypeScript

### Type System

**Centralized Types** ([lib/types.ts](src/lib/types.ts)):
```typescript
export type Expense = {
  id: number;
  amount: number;
  category: string;
  merchant: string;
  expenseDate: string;
  bank?: string;
  createdAt: string;
  updatedAt: string;
};

export type PageableResponse<T> = {
  content: T[];
  totalPages: number;
  totalElements: number;
  // ... pagination metadata
};

export type AuthResponse = {
  token: string;
  username: string;
};
```

**Type Usage:**
- Always import types with `type` keyword: `import { type Expense } from '@/lib/types'`
- Never use `any` - use proper types or `unknown`
- Define reusable types in `lib/types.ts`, not inline in components

### Styling Architecture

**Tailwind CSS v4:**
- Utility-first CSS framework
- Custom theme configuration in `index.css`
- Accent color: `oklch(0.67 0.16 58)` (vibrant orange/amber)
- Responsive breakpoints: `sm`, `md`, `lg`, `xl`, `2xl`

**Component Styling:**
- Use Tailwind utility classes directly in JSX
- Use `cn()` utility from `lib/utils.ts` for conditional classes
- Never write custom CSS unless absolutely necessary
- Follow shadcn/ui design patterns

**Example:**
```tsx
import { cn } from '@/lib/utils';

<div className={cn(
  "flex items-center gap-2",
  isActive && "text-primary",
  isDisabled && "opacity-50 cursor-not-allowed"
)}>
```

## Features

### Authentication
- JWT-based authentication
- Login and registration forms with validation
- Persistent sessions via localStorage
- Automatic token injection in API requests
- Protected routes with redirect to login
- Automatic logout on 401 responses

### Dashboard
- Responsive sidebar navigation
- Dashboard home with expense metrics:
  - Total expenses
  - Average expense
  - Category count
  - Pie chart visualizations (Spend by Category, Spend by Bank, Income vs Expenses)
- Protected route system
- Clean, modern UI following shadcn design patterns

### API Integration
- Centralized axios client with interceptors
- Automatic JWT token management
- Error handling with 401 redirect
- Type-safe API methods for auth and expenses
- TanStack Query for data fetching and caching

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

## Development Guidelines

### Adding New Features

Follow these steps when adding new features:

1. **Create Types** (if needed) in `lib/types.ts`
   - Define API response types
   - Export with `export type`

2. **Add API Methods** in `lib/api.ts`
   - Use type-safe parameters and return types
   - Follow existing patterns

3. **Create Page Component** in `pages/`
   - Use functional component with hooks
   - Import types with `type` keyword
   - Use TanStack Query for data fetching

4. **Add Route** in `App.tsx`
   - Wrap with `<ProtectedRoute>` if authentication required
   - Add to sidebar navigation if needed

5. **Update Documentation**
   - Update this README if architecture changes
   - Update CLAUDE.md if new patterns introduced

### Adding shadcn Components

To add a new shadcn/ui component:

```bash
pnpm dlx shadcn@latest add [component-name]
```

Examples:
```bash
pnpm dlx shadcn@latest add dialog
pnpm dlx shadcn@latest add table
pnpm dlx shadcn@latest add form
```

Components are added to `src/components/ui/` and automatically configured.

### Type Safety Guidelines

**ALWAYS:**
- Use TypeScript strict mode
- Define types in `lib/types.ts` for reusability
- Import types with `type` keyword
- Type all component props, function parameters, and return values

**NEVER:**
- Use `any` type (use `unknown` or proper types)
- Define types inline in components if they could be reused
- Skip type annotations on function parameters

**Example:**
```typescript
// Good
import { type Expense } from '@/lib/types';

function ExpenseCard({ expense }: { expense: Expense }) {
  return <div>{expense.merchant}</div>;
}

// Bad
function ExpenseCard({ expense }: { expense: any }) {
  return <div>{expense.merchant}</div>;
}
```

### Code Style

**Component Patterns:**
- Use functional components with hooks (NO class components)
- Prefer named exports for contexts, utilities, and non-page components
- Use default exports for page components
- Always follow ESLint directives / config

**File Naming:**
- PascalCase for component files: `DashboardHome.tsx`, `AuthPage.tsx`
- kebab-case for utility files: `api.ts`, `utils.ts`, `types.ts`
- Components in `components/ui/` use kebab-case: `button.tsx`, `card.tsx`

**Import Organization:**
- External packages first
- Internal components second
- Types third (with `type` keyword)
- Utilities last

**Example:**
```typescript
import { useQuery } from '@tanstack/react-query';
import { Card } from '@/components/ui/card';
import { type Expense } from '@/lib/types';
import { expenseApi } from '@/lib/api';
```

## Contribution Guidelines

### Prerequisites

- Node.js 18+
- pnpm (install with `npm install -g pnpm`)
- Understanding of React, TypeScript, and Tailwind CSS

### Code Quality Standards

**Before submitting code:**
1. Run type checking: `pnpm build`
2. Run linter: `pnpm lint`
3. Test in browser (dev server: `pnpm dev`)
4. Verify responsive design (mobile, tablet, desktop)

**Required:**
- All new components must be TypeScript
- Follow existing patterns in similar components
- Use shadcn/ui components where possible
- Ensure accessibility (ARIA labels, keyboard navigation)
- Mobile-first responsive design

### Pull Request Checklist

- [ ] Code builds without TypeScript errors: `pnpm build`
- [ ] Code passes linting: `pnpm lint`
- [ ] Types defined in `lib/types.ts` (if reusable)
- [ ] API methods added to `lib/api.ts` (if needed)
- [ ] Component follows existing patterns
- [ ] Responsive design tested (mobile/tablet/desktop)
- [ ] Documentation updated (README.md, CLAUDE.md)

### Common Patterns

**Data Fetching with TanStack Query:**
```typescript
import { useQuery } from '@tanstack/react-query';
import { expenseApi } from '@/lib/api';

const { data, isLoading, error } = useQuery({
  queryKey: ['expenses', { page: 0, size: 20 }],
  queryFn: () => expenseApi.getExpenses({ page: 0, size: 20 }),
});

const expenses = data?.data.content || [];
```

**Protected Routes:**
```typescript
<Route
  path="/dashboard"
  element={
    <ProtectedRoute>
      <DashboardLayout />
    </ProtectedRoute>
  }
/>
```

**Authentication Context:**
```typescript
import { useAuth } from '@/contexts/AuthContext';

function MyComponent() {
  const { user, login, logout } = useAuth();

  // Use authentication state and methods
}
```

## Design System

The UI follows the [shadcn/ui design patterns](https://ui.shadcn.com):

- **Authentication**: [Split-screen layout](https://ui.shadcn.com/examples/authentication)
- **Dashboard**: [Sidebar navigation](https://ui.shadcn.com/examples/dashboard)
- **Components**: Accessible, customizable, type-safe

**Design Principles:**
- Consistent spacing using Tailwind scale
- Semantic color scheme (primary, accent, muted)
- Responsive design with mobile-first approach
- Accessibility as a priority (ARIA, keyboard nav)

## Troubleshooting

**Development server won't start:**
```bash
# Clean install dependencies
rm -rf node_modules pnpm-lock.yaml
pnpm install

# Check Node version
node -v  # Must be 18+

# Check port availability
lsof -i :5173
```

**TypeScript errors:**
```bash
# Run type checking
pnpm build

# Check tsconfig.json is valid
# Ensure all types are properly imported
```

**API connection issues:**
```bash
# Verify backend is running
curl http://localhost:8080/actuator/health

# Check .env file exists and is correct
cat .env

# Ensure VITE_API_BASE_URL is set correctly
```

**Build errors:**
```bash
# Clear Vite cache
rm -rf node_modules/.vite

# Rebuild
pnpm build
```

## Related Documentation

- [Root README](../README.md) - Full project overview and quick start
- [Backend README](../backend/README.md) - Backend API documentation
- [CLAUDE.md](../CLAUDE.md) - AI assistant integration guide

## External Resources

- [React](https://react.dev) - React documentation
- [TypeScript](https://www.typescriptlang.org/docs/) - TypeScript handbook
- [Vite](https://vitejs.dev) - Vite documentation
- [React Router v7](https://reactrouter.com) - Router documentation
- [TanStack Query](https://tanstack.com/query/latest) - Query documentation
- [shadcn/ui](https://ui.shadcn.com) - Component library
- [Tailwind CSS v4](https://tailwindcss.com) - Tailwind documentation
- [Radix UI](https://www.radix-ui.com) - Primitives documentation

## License

MIT
