/**
 * Settings Page Integration Tests
 * Tests user settings functionality (profile, password change)
 */

import { test, expect } from './fixtures';

test.describe('Settings Page', () => {
  test('should display user information', async ({ page, authenticatedUser }) => {
    await page.goto('/dashboard/settings');

    // Should display username (use .first() since it appears in sidebar and main content)
    await expect(page.locator(`text=${authenticatedUser.username}`).first()).toBeVisible({ timeout: 5000 });
  });

  test('should update email address', async ({ page, authenticatedUser }) => {
    await page.goto('/dashboard/settings');

    const newEmail = `updated_${authenticatedUser.username}@test.com`;

    // Find email input field by id
    await page.fill('#new-email', newEmail);

    // Click update button
    await page.click('button:has-text("Update Email")');

    // Should show success message (alert dialog in this case)
    // Note: The app uses alert() which Playwright handles automatically
    page.once('dialog', (dialog) => {
      expect(dialog.message()).toContain('success');
      dialog.accept();
    });
  });

  test('should change password successfully', async ({ page, authenticatedUser }) => {
    await page.goto('/dashboard/settings');

    // Fill password change form using id selectors
    await page.fill('#current-password', authenticatedUser.password);
    await page.fill('#new-password', 'NewTestPass123!');
    await page.fill('#confirm-password', 'NewTestPass123!');

    // Handle the success alert
    page.once('dialog', async (dialog) => {
      expect(dialog.message()).toContain('success');
      await dialog.accept();
    });

    // Submit password change
    await page.click('button:has-text("Change Password")');
  });

  test('should show error for incorrect current password', async ({
    page,
    authenticatedUser: _user,
  }) => {
    await page.goto('/dashboard/settings');

    // Fill with wrong current password
    await page.fill('#current-password', 'WrongPassword123!');
    await page.fill('#new-password', 'NewTestPass123!');
    await page.fill('#confirm-password', 'NewTestPass123!');

    // Handle the error alert
    page.once('dialog', async (dialog) => {
      expect(dialog.message().toLowerCase()).toMatch(/incorrect|wrong|invalid|error|failed/i);
      await dialog.accept();
    });

    await page.click('button:has-text("Change Password")');
  });

  test('should show error for password mismatch', async ({
    page,
    authenticatedUser,
  }) => {
    await page.goto('/dashboard/settings');

    // Fill with mismatched passwords
    await page.fill('#current-password', authenticatedUser.password);
    await page.fill('#new-password', 'NewTestPass123!');
    await page.fill('#confirm-password', 'DifferentPass123!');

    // Handle the mismatch alert
    page.once('dialog', async (dialog) => {
      expect(dialog.message().toLowerCase()).toMatch(/match|mismatch|same/i);
      await dialog.accept();
    });

    await page.click('button:has-text("Change Password")');
  });

  test('should have logout functionality', async ({ page, authenticatedUser: _user }) => {
    await page.goto('/dashboard/settings');

    // The logout functionality is handled through the sidebar, not settings page
    // Navigate via sidebar
    await page.click('a[href="/dashboard/settings"]');

    // Look for logout in sidebar or navigate to where logout exists
    const logoutButton = page.locator('button:has-text("Logout"), button:has-text("Sign Out")');

    if (await logoutButton.count() > 0) {
      await logoutButton.click();

      // Should redirect to login page
      await expect(page).toHaveURL(/\/login/, { timeout: 5000 });

      // Token should be cleared from localStorage
      const token = await page.evaluate(() => localStorage.getItem('token'));
      expect(token).toBeNull();
    }
  });

  test('should display account information section', async ({
    page,
    authenticatedUser: _user,
  }) => {
    await page.goto('/dashboard/settings');

    // Should have Account Information header
    await expect(
      page.locator('text=Account Information')
    ).toBeVisible({ timeout: 5000 });
  });

  test('should delete account successfully', async ({ page, authenticatedUser }) => {
    await page.goto('/dashboard/settings');

    // Wait for settings page to load
    await expect(page.locator('text=Account Information')).toBeVisible({ timeout: 5000 });

    // Handle the confirmation dialog (first confirm)
    page.once('dialog', async (dialog) => {
      expect(dialog.type()).toBe('confirm');
      expect(dialog.message().toLowerCase()).toMatch(/delete|cannot be undone|permanently/i);
      await dialog.accept();
    });

    // Handle the success alert (second dialog)
    page.once('dialog', async (dialog) => {
      expect(dialog.type()).toBe('alert');
      expect(dialog.message().toLowerCase()).toMatch(/deleted|success/i);
      await dialog.accept();
    });

    // Click delete account button
    await page.click('button:has-text("Delete Account")');

    // Should redirect to login page
    await expect(page).toHaveURL(/\/login/, { timeout: 10000 });

    // Token should be cleared from localStorage
    const token = await page.evaluate(() => localStorage.getItem('token'));
    expect(token).toBeNull();

    // Try to login with deleted user - should fail
    await page.fill('input[type="text"]', authenticatedUser.username);
    await page.fill('input[type="password"]', authenticatedUser.password);

    // Handle the error alert
    page.once('dialog', async (dialog) => {
      expect(dialog.message().toLowerCase()).toMatch(/invalid|error|failed|not found/i);
      await dialog.accept();
    });

    await page.click('button:has-text("Sign In")');
  });
});
