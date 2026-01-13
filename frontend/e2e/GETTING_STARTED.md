# Quick Start Guide - Running E2E Tests

This guide will help you run the Playwright integration tests for the first time.

## Prerequisites Check

Before running tests, verify you have everything installed:

```bash
# Check Node.js version (should be 18+)
node -v

# Check pnpm is installed
pnpm -v

# If pnpm is not installed
npm install -g pnpm
```

## Step-by-Step Setup

### 1. Start the Backend Services

From the project root directory:

```bash
# Start PostgreSQL and backend API
docker compose up -d postgres backend

# Verify backend is running
curl http://localhost:8080/actuator/health
```

Expected output: `{"status":"UP"}`

### 2. Start the Frontend

Open a new terminal and navigate to the frontend directory:

```bash
cd frontend

# Install dependencies (first time only)
pnpm install

# Install Playwright browsers (first time only)
pnpm exec playwright install chromium

# Start development server
pnpm dev
```

The frontend should start on http://localhost:5173

### 3. Run the Tests

Open another terminal and navigate to the frontend directory:

```bash
cd frontend

# Run all tests
pnpm test:e2e
```

## First Test Run

On your first test run, you should see:

```
Running 30+ tests using 1 worker

✓  smoke.spec.ts:6:3 › Smoke Tests › should load the frontend application
✓  smoke.spec.ts:12:3 › Smoke Tests › should connect to backend API
✓  smoke.spec.ts:20:3 › Smoke Tests › should be able to register a new user
✓  auth.spec.ts:10:3 › Authentication › should register a new user
...

30 passed (45s)
```

## Interactive Mode (Recommended for Development)

For a better development experience, use the UI mode:

```bash
pnpm test:e2e:ui
```

This opens an interactive browser where you can:
- Select specific tests to run
- Watch tests execute in real-time
- Debug failed tests
- View test traces and screenshots

## Running Specific Tests

Run only authentication tests:
```bash
pnpm test:e2e auth.spec.ts
```

Run only smoke tests (quick verification):
```bash
pnpm test:e2e smoke.spec.ts
```

## Troubleshooting

### "Connection refused" or "Network error"

**Problem**: Tests can't connect to backend
**Solution**: 
```bash
# Check if backend is running
curl http://localhost:8080/actuator/health

# If not, start it
docker compose up -d postgres backend
```

### "Navigation timeout"

**Problem**: Frontend not accessible
**Solution**:
```bash
# Check if frontend is running
curl http://localhost:5173

# If not, start it
pnpm dev
```

### "Test failed: 403 or 404"

**Problem**: API endpoint might have changed
**Solution**: 
- Check the test file for correct endpoint
- Verify backend API is working: `curl http://localhost:8080/expenses`
- Check backend logs: `docker compose logs backend`

### "Browser not installed"

**Problem**: Playwright browsers not installed
**Solution**:
```bash
pnpm exec playwright install chromium
```

## Next Steps

Once you've successfully run the tests:

1. **Explore the test files** in `e2e/` to understand what's being tested
2. **Try UI mode** for interactive debugging: `pnpm test:e2e:ui`
3. **Read the detailed README**: [e2e/README.md](README.md)
4. **Write your own tests** following the existing patterns

## Clean Up

When you're done testing:

```bash
# Stop the frontend dev server (Ctrl+C in the terminal)

# Stop backend services
docker compose down
```

## Summary of Commands

```bash
# Start services
docker compose up -d postgres backend
cd frontend && pnpm dev

# Run tests (in another terminal)
cd frontend
pnpm test:e2e           # All tests
pnpm test:e2e:ui        # Interactive mode
pnpm test:e2e:headed    # See browser
pnpm test:e2e smoke     # Quick smoke test

# Stop services
docker compose down
```

## Getting Help

- Check the [detailed E2E README](README.md) for more information
- Review [Playwright documentation](https://playwright.dev/)
- Look at existing test files for patterns and examples
