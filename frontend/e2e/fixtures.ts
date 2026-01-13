/**
 * Playwright fixtures for Frugal Fox integration tests
 * Provides authenticated user context for tests
 */

import { test as base, expect } from '@playwright/test';
import {
  registerTestUser,
  deleteAllExpenses,
  type TestUser,
} from './helpers/test-data';

// Extend basic test by providing "authenticatedUser" fixture
type TestFixtures = {
  authenticatedUser: TestUser;
};

export const test = base.extend<TestFixtures>({
  authenticatedUser: async ({ page, request }, use) => {
    // Setup: Create a new test user and log them in
    const user = await registerTestUser(request);

    // Store token in localStorage
    await page.goto('/');
    await page.evaluate((token) => {
      localStorage.setItem('token', token);
      localStorage.setItem('username', '');
    }, user.token);

    // Use the fixture in the test
    await use(user);

    // Teardown: Clean up user's expenses
    if (user.token) {
      try {
        await deleteAllExpenses(request, user.token);
      } catch (error) {
        console.warn('Failed to clean up expenses:', error);
      }
    }

    // Clear localStorage
    await page.evaluate(() => {
      localStorage.clear();
    });
  },
});

export { expect };
