# Frontend Integration Tests (E2E)

This directory contains end-to-end integration tests for the Frugal Fox frontend using Playwright.

## Overview

These tests verify the complete user journey through the application, testing:
- User authentication (registration, login, logout)
- Dashboard navigation and data display
- Expense management (create, view, update, delete)
- Search and filtering
- CSV import/export
- Settings management (email, password)

## Prerequisites

Before running the tests, ensure you have:

1. **Backend API running** on `http://localhost:8080`
2. **Frontend running** on `http://localhost:5173` (dev mode) or `http://localhost:3000` (production)
3. **PostgreSQL database** accessible to the backend

### Quick Start with Docker

```bash
# From project root, start backend and database
docker compose up -d postgres backend

# From frontend directory, start dev server
cd frontend
pnpm dev
```

## Running Tests

### Run all tests (headless mode)
```bash
pnpm test:e2e
```

### Run tests with UI (interactive mode)
```bash
pnpm test:e2e:ui
```

### Run tests in headed mode (see browser)
```bash
pnpm test:e2e:headed
```

### Run tests in debug mode
```bash
pnpm test:e2e:debug
```

### Run specific test file
```bash
pnpm test:e2e auth.spec.ts
```

### Run tests with custom base URL
```bash
PLAYWRIGHT_BASE_URL=http://localhost:3000 pnpm test:e2e
```

## Test Structure

```
e2e/
├── fixtures.ts              # Custom test fixtures (authenticated user)
├── helpers/
│   └── test-data.ts         # Test data creation and cleanup utilities
├── auth.spec.ts             # Authentication tests
├── dashboard.spec.ts        # Dashboard home page tests
├── add-expense.spec.ts      # Add expense form tests
├── expenses.spec.ts         # Expense list, search, filter tests
├── settings.spec.ts         # User settings tests
└── import-export.spec.ts    # CSV import/export tests
```

## Key Features

### Test Data Management

Tests automatically:
- Create unique test users for each test
- Set up required test data (expenses)
- Clean up test data after test completion
- Isolate tests from each other (no shared state)

### Authenticated Tests

Use the `authenticatedUser` fixture for tests requiring authentication:

```typescript
import { test, expect } from './fixtures';

test('should do something', async ({ page, authenticatedUser }) => {
  // User is already logged in
  await page.goto('/dashboard');
  // ... test code
});
```

### API Helpers

Helper functions for test data setup:

- `registerTestUser()` - Create a new test user
- `loginTestUser()` - Login an existing user
- `createTestExpense()` - Create a single expense
- `createMultipleTestExpenses()` - Create multiple expenses
- `deleteAllExpenses()` - Clean up all expenses for a user

## Configuration

Edit `playwright.config.ts` to customize:

- Test timeout
- Number of retries
- Screenshot/video settings
- Browser configurations
- Base URL
- Parallel execution

## Continuous Integration

These tests are designed to run in CI/CD pipelines:

1. Tests automatically retry on failure (configurable)
2. Tests generate GitHub-formatted reports in CI
3. Screenshots and videos captured on failure
4. Sequential execution to avoid race conditions

### Example GitHub Actions

```yaml
- name: Start services
  run: docker compose up -d postgres backend

- name: Wait for backend
  run: |
    timeout 60 bash -c 'until curl -f http://localhost:8080/actuator/health; do sleep 2; done'

- name: Install dependencies
  run: |
    cd frontend
    pnpm install
    pnpm exec playwright install chromium

- name: Run E2E tests
  run: |
    cd frontend
    pnpm test:e2e
```

## Debugging Failed Tests

### View test report
```bash
pnpm exec playwright show-report
```

### Re-run failed tests
```bash
pnpm test:e2e --last-failed
```

### Run with trace viewer
```bash
pnpm test:e2e --trace on
```

### Screenshots and videos

Failed tests automatically capture:
- Screenshots in `test-results/`
- Videos in `test-results/`
- Traces for debugging (when enabled)

## Writing New Tests

1. Create a new `.spec.ts` file in the `e2e/` directory
2. Import the test fixture: `import { test, expect } from './fixtures';`
3. Use `authenticatedUser` fixture for authenticated tests
4. Follow the AAA pattern (Arrange, Act, Assert)
5. Clean up test data in teardown

Example:

```typescript
import { test, expect } from './fixtures';
import { createTestExpense } from './helpers/test-data';

test.describe('My Feature', () => {
  test('should do something', async ({ page, authenticatedUser, request }) => {
    // Arrange
    await createTestExpense(request, authenticatedUser.token!, {
      merchant: 'Test Store',
      amount: 100,
    });

    // Act
    await page.goto('/dashboard/expenses');
    await page.click('text=Test Store');

    // Assert
    await expect(page.locator('text=$100')).toBeVisible();
  });
});
```

## Troubleshooting

### Tests fail with "Navigation timeout"
- Ensure frontend is running on the expected port
- Check `PLAYWRIGHT_BASE_URL` environment variable
- Verify backend API is accessible

### Tests fail with "API request failed"
- Ensure backend is running on `http://localhost:8080`
- Check database is running and accessible
- Verify API endpoints are working (e.g., `curl http://localhost:8080/actuator/health`)

### Import tests fail
- Ensure Node.js has permission to write to temp directory
- Check CSV file format matches expected schema

### Tests are flaky
- Increase timeout for slow operations
- Add explicit waits for dynamic content
- Check for race conditions in test data setup

## Best Practices

1. **Isolation**: Each test should be independent and not rely on other tests
2. **Cleanup**: Always clean up test data after test completion
3. **Selectors**: Use data-testid attributes for reliable element selection
4. **Waits**: Use explicit waits (`waitFor`) instead of arbitrary timeouts
5. **Assertions**: Make assertions specific and meaningful
6. **Coverage**: Focus on user journeys, not implementation details

## Resources

- [Playwright Documentation](https://playwright.dev/)
- [Best Practices Guide](https://playwright.dev/docs/best-practices)
- [API Reference](https://playwright.dev/docs/api/class-test)
