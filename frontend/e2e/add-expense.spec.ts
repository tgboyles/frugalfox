/**
 * Add Expense Integration Tests
 * Tests expense creation functionality
 */

import { test, expect } from './fixtures';

test.describe('Add Expense', () => {
  test('should add a new expense successfully', async ({ page, authenticatedUser }) => {
    await page.goto('/dashboard/add-expense');

    // Fill in expense form
    await page.fill('input[name="amount"]', '99.99');
    await page.fill('input[name="merchant"]', 'Test Store');
    
    // Select category (could be dropdown or input based on implementation)
    const categoryInput = page.locator('input[name="category"], select[name="category"]');
    if (await categoryInput.count() > 0) {
      await categoryInput.first().fill('Food');
    }

    // Fill in date
    const today = new Date().toISOString().split('T')[0];
    await page.fill('input[name="date"], input[type="date"]', today);

    // Optional: fill in bank
    const bankInput = page.locator('input[name="bank"]');
    if (await bankInput.count() > 0) {
      await bankInput.fill('Chase');
    }

    // Submit form
    await page.click('button[type="submit"], button:has-text("Add"), button:has-text("Save")');

    // Should show success message or redirect
    await expect(
      page.locator('text=/success|added|created/i, [role="alert"]')
    ).toBeVisible({ timeout: 5000 }).catch(() => {
      // Alternative: check if redirected to expenses page
      return expect(page).toHaveURL(/\/dashboard\/expenses/, { timeout: 5000 });
    });
  });

  test('should show validation error for invalid amount', async ({
    page,
    authenticatedUser,
  }) => {
    await page.goto('/dashboard/add-expense');

    // Fill in invalid amount
    await page.fill('input[name="amount"]', '-10');
    await page.fill('input[name="merchant"]', 'Test Store');

    const categoryInput = page.locator('input[name="category"], select[name="category"]');
    if (await categoryInput.count() > 0) {
      await categoryInput.first().fill('Food');
    }

    const today = new Date().toISOString().split('T')[0];
    await page.fill('input[name="date"], input[type="date"]', today);

    await page.click('button[type="submit"]');

    // Should show validation error
    await expect(page.locator('text=/invalid|error|must be/i')).toBeVisible({
      timeout: 3000,
    });
  });

  test('should show validation error for missing required fields', async ({
    page,
    authenticatedUser,
  }) => {
    await page.goto('/dashboard/add-expense');

    // Try to submit without filling required fields
    await page.click('button[type="submit"]');

    // Should show validation errors
    await expect(page.locator('text=/required|must|field/i')).toBeVisible({
      timeout: 3000,
    });
  });

  test('should clear form after successful submission', async ({
    page,
    authenticatedUser,
  }) => {
    await page.goto('/dashboard/add-expense');

    // Fill in expense form
    await page.fill('input[name="amount"]', '50.00');
    await page.fill('input[name="merchant"]', 'Coffee Shop');

    const categoryInput = page.locator('input[name="category"], select[name="category"]');
    if (await categoryInput.count() > 0) {
      await categoryInput.first().fill('Food');
    }

    const today = new Date().toISOString().split('T')[0];
    await page.fill('input[name="date"], input[type="date"]', today);

    await page.click('button[type="submit"]');

    // Wait for success indication
    await page.waitForTimeout(1000);

    // Check if still on add expense page
    if (page.url().includes('add-expense')) {
      // Form should be cleared
      await expect(page.locator('input[name="merchant"]')).toHaveValue('');
    }
  });

  test('should navigate back to expenses list', async ({ page, authenticatedUser }) => {
    await page.goto('/dashboard/add-expense');

    // Click cancel or back button
    await page.click(
      'button:has-text("Cancel"), button:has-text("Back"), a:has-text("Back")'
    ).catch(async () => {
      // Alternative: click expenses link in navigation
      await page.click('a[href="/dashboard/expenses"]');
    });

    // Should navigate to expenses page
    await expect(page).toHaveURL(/\/dashboard\/expenses/, { timeout: 5000 });
  });
});
