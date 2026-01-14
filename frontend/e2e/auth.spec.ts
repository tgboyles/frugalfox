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

    // Switch to register mode by clicking "Sign up" link
    await page.click('text=Sign up');

    // Fill in registration form (using id selectors)
    await page.fill('#username', username);
    await page.fill('#email', email);
    await page.fill('#password', password);

    // Submit form
    await page.click('button[type="submit"]');

    // Should redirect to dashboard after successful registration
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });
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

    // Should be on login mode by default
    await page.fill('#username', username);
    await page.fill('#password', password);

    await page.click('button[type="submit"]');

    // Should redirect to dashboard after successful login
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });
  });

  test('should show error for invalid credentials', async ({ page }) => {
    await page.goto('/login');

    await page.fill('#username', 'nonexistentuser');
    await page.fill('#password', 'WrongPassword123!');

    await page.click('button[type="submit"]');

    // Should show error message (the actual message is "Failed to login. Please try again.")
    // Wait for the request to complete and error to show
    await expect(page.locator('text=/Failed to login|Invalid|error|credentials/i')).toBeVisible({
      timeout: 10000,
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

    // Navigate to settings page where logout button is located
    await page.goto('/dashboard/settings');
    await expect(page).toHaveURL(/\/dashboard\/settings/);

    // Click logout button
    await page.click('button:has-text("Logout"), button:has-text("Sign Out")');

    // Should redirect to login page
    await expect(page).toHaveURL(/\/login/, { timeout: 5000 });

    // Token should be cleared
    const tokenAfterLogout = await page.evaluate(() => localStorage.getItem('token'));
    expect(tokenAfterLogout).toBeNull();
  });
});
