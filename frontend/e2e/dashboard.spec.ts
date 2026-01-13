/**
 * Dashboard Integration Tests
 * Tests dashboard home page with expense overview and charts
 */

import { test, expect } from './fixtures';
import { createMultipleTestExpenses } from './helpers/test-data';

test.describe('Dashboard Home', () => {
  test('should display empty state when no expenses exist', async ({
    page,
    authenticatedUser,
  }) => {
    await page.goto('/dashboard');

    // Should show welcome message or empty state
    await expect(
      page.locator('text=/welcome|no expenses|get started/i')
    ).toBeVisible({ timeout: 5000 });
  });

  test('should display expense summary when expenses exist', async ({
    page,
    authenticatedUser,
    request,
  }) => {
    // Create some test expenses
    await createMultipleTestExpenses(request, authenticatedUser.token!, 5);

    await page.goto('/dashboard');

    // Should display expense metrics/summary
    // Adjust selectors based on your dashboard implementation
    await expect(page.locator('text=/total|spent|expense/i').first()).toBeVisible({
      timeout: 5000,
    });
  });

  test('should display charts when expenses exist', async ({
    page,
    authenticatedUser,
    request,
  }) => {
    // Create test expenses with different categories
    await createMultipleTestExpenses(request, authenticatedUser.token!, 10);

    await page.goto('/dashboard');

    // Wait for charts to render (Recharts uses SVG)
    const charts = page.locator('svg').first();
    await expect(charts).toBeVisible({ timeout: 5000 });
  });

  test('should navigate to expenses page from dashboard', async ({
    page,
    authenticatedUser,
  }) => {
    await page.goto('/dashboard');

    // Click on expenses link in sidebar or navigation
    await page.click('a[href="/dashboard/expenses"], text=Expenses');

    // Should navigate to expenses page
    await expect(page).toHaveURL(/\/dashboard\/expenses/);
  });

  test('should navigate to add expense page from dashboard', async ({
    page,
    authenticatedUser,
  }) => {
    await page.goto('/dashboard');

    // Click on add expense button/link
    await page.click(
      'a[href="/dashboard/add-expense"], button:has-text("Add Expense"), text=/add.*expense/i'
    ).catch(async () => {
      // Alternative: click sidebar link
      await page.click('[href*="add-expense"]');
    });

    // Should navigate to add expense page
    await expect(page).toHaveURL(/\/dashboard\/add-expense/);
  });
});
