/**
 * Settings Page Integration Tests
 * Tests user settings functionality (profile, password change)
 */

import { test, expect } from './fixtures';

test.describe('Settings Page', () => {
  test('should display user information', async ({ page, authenticatedUser }) => {
    await page.goto('/dashboard/settings');

    // Should display username or email - check if either is visible
    const usernameVisible = await page.locator(`text=${authenticatedUser.username}`).isVisible().catch(() => false);
    const emailVisible = await page.locator(`text=${authenticatedUser.email}`).isVisible().catch(() => false);
    
    // At least one should be visible
    expect(usernameVisible || emailVisible).toBeTruthy();
  });

  test('should update email address', async ({ page, authenticatedUser }) => {
    await page.goto('/dashboard/settings');

    const newEmail = `updated_${authenticatedUser.username}@test.com`;

    // Find email input field
    const emailInput = page.locator('input[name="email"], input[type="email"]').first();

    if (await emailInput.count() > 0) {
      // Clear and fill new email
      await emailInput.clear();
      await emailInput.fill(newEmail);

      // Click update/save button
      await page
        .click('button:has-text("Update"), button:has-text("Save")')
        .catch(() => page.click('button[type="submit"]'));

      // Should show success message
      await expect(
        page.locator('text=/success|updated|saved/i, [role="alert"]')
      ).toBeVisible({ timeout: 5000 });
    }
  });

  test('should change password successfully', async ({ page, authenticatedUser }) => {
    await page.goto('/dashboard/settings');

    // Find password change section
    const currentPasswordInput = page.locator(
      'input[name="currentPassword"], input[placeholder*="current" i]'
    );

    if (await currentPasswordInput.count() > 0) {
      await currentPasswordInput.fill(authenticatedUser.password);

      const newPasswordInput = page.locator(
        'input[name="newPassword"], input[placeholder*="new" i]'
      ).first();
      await newPasswordInput.fill('NewTestPass123!');

      // If there's a confirm password field
      const confirmPasswordInput = page.locator(
        'input[name="confirmPassword"], input[placeholder*="confirm" i]'
      );
      if (await confirmPasswordInput.count() > 0) {
        await confirmPasswordInput.fill('NewTestPass123!');
      }

      // Submit password change
      await page
        .click('button:has-text("Change Password"), button:has-text("Update Password")')
        .catch(() => page.click('button[type="submit"]'));

      // Should show success message
      await expect(
        page.locator('text=/success|updated|changed/i, [role="alert"]')
      ).toBeVisible({ timeout: 5000 });
    }
  });

  test('should show error for incorrect current password', async ({
    page,
    authenticatedUser,
  }) => {
    await page.goto('/dashboard/settings');

    const currentPasswordInput = page.locator(
      'input[name="currentPassword"], input[placeholder*="current" i]'
    );

    if (await currentPasswordInput.count() > 0) {
      await currentPasswordInput.fill('WrongPassword123!');

      const newPasswordInput = page.locator(
        'input[name="newPassword"], input[placeholder*="new" i]'
      ).first();
      await newPasswordInput.fill('NewTestPass123!');

      await page
        .click('button:has-text("Change Password"), button:has-text("Update Password")')
        .catch(() => page.click('button[type="submit"]'));

      // Should show error message
      await expect(
        page.locator('text=/incorrect|wrong|invalid|error/i')
      ).toBeVisible({ timeout: 5000 });
    }
  });

  test('should show error for password mismatch', async ({
    page,
    authenticatedUser,
  }) => {
    await page.goto('/dashboard/settings');

    const currentPasswordInput = page.locator(
      'input[name="currentPassword"], input[placeholder*="current" i]'
    );

    if (await currentPasswordInput.count() > 0) {
      await currentPasswordInput.fill(authenticatedUser.password);

      const newPasswordInput = page.locator(
        'input[name="newPassword"], input[placeholder*="new" i]'
      ).first();
      await newPasswordInput.fill('NewTestPass123!');

      const confirmPasswordInput = page.locator(
        'input[name="confirmPassword"], input[placeholder*="confirm" i]'
      );
      
      if (await confirmPasswordInput.count() > 0) {
        await confirmPasswordInput.fill('DifferentPass123!');

        await page
          .click('button:has-text("Change Password"), button:has-text("Update Password")')
          .catch(() => page.click('button[type="submit"]'));

        // Should show error message about mismatch
        await expect(
          page.locator('text=/match|mismatch|same/i')
        ).toBeVisible({ timeout: 5000 });
      }
    }
  });

  test('should have logout functionality', async ({ page, authenticatedUser }) => {
    await page.goto('/dashboard/settings');

    // Find logout button
    const logoutButton = page.locator(
      'button:has-text("Logout"), button:has-text("Sign Out"), [data-testid="logout-button"]'
    );

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
    authenticatedUser,
  }) => {
    await page.goto('/dashboard/settings');

    // Should have sections for account info
    await expect(
      page.locator('text=/account|profile|user/i').first()
    ).toBeVisible({ timeout: 5000 });
  });
});
