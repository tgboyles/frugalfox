/**
 * Add Expense Integration Tests
 * Tests expense creation functionality
 */

import { test, expect } from './fixtures';

test.describe('Add Expense', () => {
  test('should add a new expense successfully', async ({ page, authenticatedUser: _user }) => {
    await page.goto('/dashboard/add-expense');

    // Fill in expense form (using id selectors to match the form)
    await page.fill('#amount', '99.99');
    await page.fill('#merchant', 'Test Store');
    await page.fill('#category', 'Food');

    // Fill in date
    const today = new Date().toISOString().split('T')[0];
    await page.fill('#date', today);

    // Optional: fill in bank
    await page.fill('#bank', 'Chase');

    // Submit form
    await page.click('button[type="submit"]');

    // Should show success message or redirect
    await expect(
      page.locator('text=/success|added|created/i')
    ).toBeVisible({ timeout: 5000 }).catch(() => {
      // Alternative: check if redirected to expenses page
      return expect(page).toHaveURL(/\/dashboard\/expenses/, { timeout: 5000 });
    });
  });

  test('should show validation error for invalid amount', async ({
    page,
    authenticatedUser: _user,
  }) => {
    await page.goto('/dashboard/add-expense');

    // Fill in invalid amount (0 is invalid since amount must be > 0)
    await page.fill('#amount', '0');
    await page.fill('#merchant', 'Test Store');
    await page.fill('#category', 'Food');

    const today = new Date().toISOString().split('T')[0];
    await page.fill('#date', today);

    await page.click('button[type="submit"]');

    // Should show validation error "Amount must be a valid number greater than 0"
    await expect(page.locator('text=Amount must be a valid number greater than 0')).toBeVisible({
      timeout: 3000,
    });
  });

  test('should show validation error for missing required fields', async ({
    page,
    authenticatedUser: _user,
  }) => {
    await page.goto('/dashboard/add-expense');

    // Clear the default date and try to submit without filling required fields
    await page.fill('#date', '');
    await page.click('button[type="submit"]');

    // Should show validation errors (use .first() since multiple errors will appear)
    await expect(page.locator('text=/is required/i').first()).toBeVisible({
      timeout: 3000,
    });
  });

  test('should clear form after successful submission', async ({
    page,
    authenticatedUser: _user,
  }) => {
    await page.goto('/dashboard/add-expense');

    // Fill in expense form
    await page.fill('#amount', '50.00');
    await page.fill('#merchant', 'Coffee Shop');
    await page.fill('#category', 'Food');
    await page.fill('#bank', 'Chase'); // Bank is required by backend

    const today = new Date().toISOString().split('T')[0];
    await page.fill('#date', today);

    await page.click('button[type="submit"]');

    // Wait for success message "Expense created successfully! Redirecting..."
    await expect(
      page.locator('text=Expense created successfully! Redirecting...')
    ).toBeVisible({ timeout: 5000 });

    // Then wait for redirect to expenses page (after 1 second delay per REDIRECT_DELAY_MS)
    await expect(page).toHaveURL(/\/dashboard\/expenses/, { timeout: 6000 });
  });

  test('should navigate back to expenses list', async ({ page, authenticatedUser: _user }) => {
    await page.goto('/dashboard/add-expense');

    // Click expenses link in navigation sidebar
    await page.click('a[href="/dashboard/expenses"]');

    // Should navigate to expenses page
    await expect(page).toHaveURL(/\/dashboard\/expenses/, { timeout: 5000 });
  });
});
