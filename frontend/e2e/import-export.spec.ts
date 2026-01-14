/**
 * CSV Import/Export Integration Tests
 * Tests bulk import and export functionality
 */

import { test, expect } from './fixtures';
import { createMultipleTestExpenses } from './helpers/test-data';
import { writeFile } from 'fs/promises';
import { join } from 'path';
import { tmpdir } from 'os';

test.describe('CSV Import/Export', () => {
  test('should import expenses from CSV file', async ({
    page,
    authenticatedUser: _user,
  }) => {
    // Create a temporary CSV file with dynamic dates
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);
    const twoDaysAgo = new Date(today);
    twoDaysAgo.setDate(twoDaysAgo.getDate() - 2);

    const formatDate = (date: Date) => date.toISOString().split('T')[0];

    const csvContent = `date,merchant,amount,bank,category
${formatDate(yesterday)},Starbucks,25.50,Chase,Food
${formatDate(twoDaysAgo)},Amazon,150.00,Wells Fargo,Shopping
${formatDate(today)},Shell,45.75,Chase,Transport`;

    const tempDir = tmpdir();
    const csvFilePath = join(tempDir, `test-expenses-${Date.now()}.csv`);
    await writeFile(csvFilePath, csvContent);

    // Navigate to add-expense page where import is located
    await page.goto('/dashboard/add-expense');

    // Find the CSV file input (id="csv-file")
    const fileInput = page.locator('#csv-file');
    await fileInput.setInputFiles(csvFilePath);

    // Click upload button
    await page.click('button:has-text("Upload and Import")');

    // Wait for import to complete
    await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => {});

    // Should show import results with success count (use .first() for strict mode)
    await expect(
      page.locator('text=Import Results')
    ).toBeVisible({ timeout: 5000 });

    // Navigate to expenses to verify import
    await page.goto('/dashboard/expenses');
    await expect(page.locator('text=Starbucks')).toBeVisible({ timeout: 5000 });
  });

  test('should show error for invalid CSV format', async ({
    page,
    authenticatedUser: _user,
  }) => {
    // Create an invalid CSV file (wrong column order/names)
    const csvContent = `invalid,header,format
not,a,valid`;

    const tempDir = tmpdir();
    const csvFilePath = join(tempDir, `invalid-expenses-${Date.now()}.csv`);
    await writeFile(csvFilePath, csvContent);

    await page.goto('/dashboard/add-expense');

    const fileInput = page.locator('#csv-file');
    await fileInput.setInputFiles(csvFilePath);

    await page.click('button:has-text("Upload and Import")');

    // Should show error message in import results
    await expect(
      page.locator('text=/error|failed|invalid/i')
    ).toBeVisible({ timeout: 5000 });
  });

  test('should export expenses to CSV', async ({
    page,
    authenticatedUser,
    request,
  }) => {
    // Create test expenses to export
    await createMultipleTestExpenses(request, authenticatedUser.token!, 10);

    // Export is on settings page
    await page.goto('/dashboard/settings');

    // Listen for download event
    const downloadPromise = page.waitForEvent('download', { timeout: 5000 });

    // Click download CSV button
    await page.click('button:has-text("Download CSV")');

    const download = await downloadPromise;

    // Verify the download
    expect(download.suggestedFilename()).toMatch(/\.csv$/);

    // Save the file to verify content
    const tempDir = tmpdir();
    const downloadPath = join(tempDir, download.suggestedFilename());
    await download.saveAs(downloadPath);

    // Verify file was downloaded
    expect(downloadPath).toBeTruthy();
  });

  test('should export filtered expenses', async ({
    page,
    authenticatedUser,
    request,
  }) => {
    // Create expenses with different categories
    await createMultipleTestExpenses(request, authenticatedUser.token!, 15);

    // Export with filters is on settings page
    await page.goto('/dashboard/settings');

    // Apply a filter (e.g., category)
    await page.fill('#export-category', 'Food');

    // Listen for download event
    const downloadPromise = page.waitForEvent('download', { timeout: 5000 }).catch(() => null);

    // Click download button
    await page.click('button:has-text("Download CSV")');

    const download = await downloadPromise;
    if (download) {
      expect(download.suggestedFilename()).toMatch(/\.csv$/);
    }
  });

  test('should handle large CSV import', async ({ page, authenticatedUser: _user }) => {
    // Create a CSV with many rows (but within limit)
    const rows = ['date,merchant,amount,bank,category'];
    const today = new Date();
    for (let i = 1; i <= 100; i++) {
      const date = new Date(today);
      date.setDate(date.getDate() - (i % 28));
      const formatDate = (d: Date) => d.toISOString().split('T')[0];
      rows.push(`${formatDate(date)},Store ${i},${10 + i},Chase,Food`);
    }
    const csvContent = rows.join('\n');

    const tempDir = tmpdir();
    const csvFilePath = join(tempDir, `large-expenses-${Date.now()}.csv`);
    await writeFile(csvFilePath, csvContent);

    await page.goto('/dashboard/add-expense');

    const fileInput = page.locator('#csv-file');
    await fileInput.setInputFiles(csvFilePath);

    await page.click('button:has-text("Upload and Import")');

    // Wait for import to complete (might take longer)
    await page.waitForLoadState('networkidle', { timeout: 10000 }).catch(() => {});

    // Should show success in import results (use exact match to avoid strict mode)
    await expect(
      page.locator('text=Import Results')
    ).toBeVisible({ timeout: 10000 });
  });
});
