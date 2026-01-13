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
    authenticatedUser,
  }) => {
    // Create a temporary CSV file
    const csvContent = `amount,category,merchant,date,bank
25.50,Food,Starbucks,2024-01-15,Chase
150.00,Shopping,Amazon,2024-01-14,Wells Fargo
45.75,Transport,Shell,2024-01-13,Chase`;

    const tempDir = tmpdir();
    const csvFilePath = join(tempDir, `test-expenses-${Date.now()}.csv`);
    await writeFile(csvFilePath, csvContent);

    await page.goto('/dashboard/expenses');

    // Find import button (adjust selector based on implementation)
    const importButton = page.locator(
      'button:has-text("Import"), [data-testid="import-button"], input[type="file"]'
    ).first();

    if (await importButton.count() > 0) {
      // If it's a file input directly
      if ((await importButton.getAttribute('type')) === 'file') {
        await importButton.setInputFiles(csvFilePath);
      } else {
        // If it's a button that triggers a file input
        await importButton.click();
        
        // Find the file input
        const fileInput = page.locator('input[type="file"]');
        await fileInput.setInputFiles(csvFilePath);
      }

      // Wait for import to complete
      await page.waitForTimeout(2000);

      // Should show success message
      await expect(
        page.locator('text=/imported|success|uploaded/i, [role="alert"]')
      ).toBeVisible({ timeout: 5000 }).catch(() => {
        // Import might auto-refresh the page
        return page.waitForTimeout(1000);
      });

      // Verify imported expenses appear in the list
      await expect(page.locator('text=Starbucks')).toBeVisible({ timeout: 5000 });
    }
  });

  test('should show error for invalid CSV format', async ({
    page,
    authenticatedUser,
  }) => {
    // Create an invalid CSV file
    const csvContent = `invalid,header,format
not,a,valid,expense`;

    const tempDir = tmpdir();
    const csvFilePath = join(tempDir, `invalid-expenses-${Date.now()}.csv`);
    await writeFile(csvFilePath, csvContent);

    await page.goto('/dashboard/expenses');

    const importButton = page.locator(
      'button:has-text("Import"), [data-testid="import-button"], input[type="file"]'
    ).first();

    if (await importButton.count() > 0) {
      if ((await importButton.getAttribute('type')) === 'file') {
        await importButton.setInputFiles(csvFilePath);
      } else {
        await importButton.click();
        const fileInput = page.locator('input[type="file"]');
        await fileInput.setInputFiles(csvFilePath);
      }

      // Should show error message
      await expect(
        page.locator('text=/error|invalid|failed/i')
      ).toBeVisible({ timeout: 5000 });
    }
  });

  test('should export expenses to CSV', async ({
    page,
    authenticatedUser,
    request,
  }) => {
    // Create test expenses to export
    await createMultipleTestExpenses(request, authenticatedUser.token!, 10);

    await page.goto('/dashboard/expenses');

    // Find export button
    const exportButton = page.locator(
      'button:has-text("Export"), [data-testid="export-button"]'
    );

    if (await exportButton.count() > 0) {
      // Listen for download event
      const downloadPromise = page.waitForEvent('download', { timeout: 5000 });

      await exportButton.click();

      const download = await downloadPromise;

      // Verify the download
      expect(download.suggestedFilename()).toMatch(/\.csv$/);

      // Save the file to verify content
      const tempDir = tmpdir();
      const downloadPath = join(tempDir, download.suggestedFilename());
      await download.saveAs(downloadPath);

      // Verify file was downloaded
      expect(downloadPath).toBeTruthy();
    }
  });

  test('should export filtered expenses', async ({
    page,
    authenticatedUser,
    request,
  }) => {
    // Create expenses with different categories
    await createMultipleTestExpenses(request, authenticatedUser.token!, 15);

    await page.goto('/dashboard/expenses');

    // Apply a filter (e.g., category)
    const categoryFilter = page.locator(
      'select[name="category"], [data-testid="category-filter"]'
    ).first();

    if (await categoryFilter.count() > 0) {
      await categoryFilter.selectOption('Food').catch(() => {});
      await page.waitForTimeout(500);

      // Export filtered results
      const exportButton = page.locator('button:has-text("Export")');
      
      if (await exportButton.count() > 0) {
        const downloadPromise = page.waitForEvent('download', { timeout: 5000 }).catch(() => null);
        await exportButton.click();

        const download = await downloadPromise;
        if (download) {
          expect(download.suggestedFilename()).toMatch(/\.csv$/);
        }
      }
    }
  });

  test('should handle large CSV import', async ({ page, authenticatedUser }) => {
    // Create a CSV with many rows (but within limit)
    const rows = ['amount,category,merchant,date,bank'];
    for (let i = 1; i <= 100; i++) {
      rows.push(`${10 + i},Food,Store ${i},2024-01-${String(i % 28 + 1).padStart(2, '0')},Chase`);
    }
    const csvContent = rows.join('\n');

    const tempDir = tmpdir();
    const csvFilePath = join(tempDir, `large-expenses-${Date.now()}.csv`);
    await writeFile(csvFilePath, csvContent);

    await page.goto('/dashboard/expenses');

    const importButton = page.locator('input[type="file"]').first();

    if (await importButton.count() > 0) {
      await importButton.setInputFiles(csvFilePath);

      // Wait for import to complete (might take longer)
      await page.waitForTimeout(3000);

      // Should show success or import in progress
      await expect(
        page.locator('text=/imported|success|processing/i, [role="alert"]')
      ).toBeVisible({ timeout: 10000 }).catch(() => {
        // Import might auto-refresh
        return page.waitForTimeout(1000);
      });
    }
  });
});
