/**
 * Authentication Integration Tests
 * Tests user registration and login functionality
 */

import { test, expect } from '@playwright/test';
import { generateTestUsername } from './helpers/test-data';

test.describe('Authentication', () => {
  test.beforeEach(async ({ page }) => {
    // Clear localStorage before each test
    await page.goto('/');
    await page.evaluate(() => localStorage.clear());
  });

  test('should register a new user', async ({ page }) => {
    const username = generateTestUsername();
    const email = `${username}@test.com`;
    const password = 'TestPass123!';

    await page.goto('/login');

    // Switch to register tab
    await page.click('text=Register');

    // Fill in registration form
    await page.fill('input[name="username"]', username);
    await page.fill('input[name="email"]', email);
    await page.fill('input[name="password"]', password);

    // Submit form
    await page.click('button[type="submit"]');

    // Should redirect to dashboard after successful registration
    await expect(page).toHaveURL(/\/dashboard/);
  });

  test('should login with existing user', async ({ page, request }) => {
    // First, register a user via API
    const username = generateTestUsername();
    const email = `${username}@test.com`;
    const password = 'TestPass123!';

    await request.post('http://localhost:8080/auth/register', {
      data: {
        username,
        email,
        password,
      },
    });

    // Now test login via UI
    await page.goto('/login');

    // Should be on login tab by default
    await page.fill('input[name="username"]', username);
    await page.fill('input[name="password"]', password);

    await page.click('button[type="submit"]');

    // Should redirect to dashboard after successful login
    await expect(page).toHaveURL(/\/dashboard/);
  });

  test('should show error for invalid credentials', async ({ page }) => {
    await page.goto('/login');

    await page.fill('input[name="username"]', 'nonexistentuser');
    await page.fill('input[name="password"]', 'WrongPassword123!');

    await page.click('button[type="submit"]');

    // Should show error message (adjust selector based on your error display)
    await expect(page.locator('text=/invalid|error|failed/i')).toBeVisible({
      timeout: 5000,
    });
  });

  test('should redirect unauthenticated users to login', async ({ page }) => {
    await page.goto('/dashboard');

    // Should redirect to login page
    await expect(page).toHaveURL(/\/login/);
  });

  test('should logout user', async ({ page, request }) => {
    // First, register and login a user via API
    const username = generateTestUsername();
    const email = `${username}@test.com`;
    const password = 'TestPass123!';

    const response = await request.post('http://localhost:8080/auth/register', {
      data: {
        username,
        email,
        password,
      },
    });

    const data = await response.json();
    const token = data.token;

    // Set token in localStorage
    await page.goto('/');
    await page.evaluate(
      ({ token, username }) => {
        localStorage.setItem('token', token);
        localStorage.setItem('username', username);
      },
      { token, username }
    );

    // Navigate to dashboard
    await page.goto('/dashboard');
    await expect(page).toHaveURL(/\/dashboard/);

    // Logout (adjust selector based on your UI - might be in settings or a menu)
    // This is a placeholder - adjust based on actual logout button location
    await page.click('[data-testid="logout-button"], button:has-text("Logout"), text=Logout').catch(async () => {
      // If logout button is in settings, navigate there first
      await page.goto('/dashboard/settings');
      await page.click('button:has-text("Logout"), text=Logout');
    });

    // Should redirect to login page
    await expect(page).toHaveURL(/\/login/, { timeout: 5000 });

    // Token should be cleared
    const tokenAfterLogout = await page.evaluate(() => localStorage.getItem('token'));
    expect(tokenAfterLogout).toBeNull();
  });
});
