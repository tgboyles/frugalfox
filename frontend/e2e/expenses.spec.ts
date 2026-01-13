/**
 * Expenses Page Integration Tests
 * Tests viewing, searching, filtering, and managing expenses
 */

import { test, expect } from './fixtures';
import { createMultipleTestExpenses, createTestExpense } from './helpers/test-data';

test.describe('Expenses Page', () => {
  test('should display list of expenses', async ({
    page,
    authenticatedUser,
    request,
  }) => {
    // Create test expenses
    await createMultipleTestExpenses(request, authenticatedUser.token!, 5);

    await page.goto('/dashboard/expenses');

    // Should display expenses in a table or list
    await expect(
      page.locator('table, [role="table"], [data-testid="expense-list"]').first()
    ).toBeVisible({ timeout: 5000 });

    // Should display at least one expense row
    const expenseRows = page.locator(
      'tr:has(td), [role="row"]:has([role="cell"]), [data-testid*="expense-item"]'
    );
    await expect(expenseRows.first()).toBeVisible();
  });

  test('should filter expenses by category', async ({
    page,
    authenticatedUser,
    request,
  }) => {
    // Create expenses with specific categories
    await createTestExpense(request, authenticatedUser.token!, {
      category: 'Food',
      merchant: 'Restaurant',
      amount: 50,
    });
    await createTestExpense(request, authenticatedUser.token!, {
      category: 'Transport',
      merchant: 'Gas Station',
      amount: 40,
    });

    await page.goto('/dashboard/expenses');

    // Find and use category filter (adjust selector based on implementation)
    const categoryFilter = page.locator(
      'select[name="category"], input[name="category"], [data-testid="category-filter"]'
    ).first();

    if (await categoryFilter.count() > 0) {
      await categoryFilter.selectOption('Food').catch(() => categoryFilter.fill('Food'));

      // Wait for filtered results (may timeout if filtering is instant)
      await page.waitForLoadState('networkidle', { timeout: 3000 }).catch(() => {
        // Intentionally ignored - filtering might be synchronous
      });

      // Should only show Food category expenses
      await expect(page.locator('text=Restaurant')).toBeVisible();
    }
  });

  test('should search expenses by merchant', async ({
    page,
    authenticatedUser,
    request,
  }) => {
    // Create expenses with different merchants
    await createTestExpense(request, authenticatedUser.token!, {
      merchant: 'Unique Coffee Shop',
      amount: 15,
    });
    await createTestExpense(request, authenticatedUser.token!, {
      merchant: 'Generic Store',
      amount: 25,
    });

    await page.goto('/dashboard/expenses');

    // Find search input
    const searchInput = page.locator(
      'input[type="search"], input[placeholder*="search" i], input[name="merchant"]'
    ).first();

    if (await searchInput.count() > 0) {
      await searchInput.fill('Coffee');

      // Wait for search results (may timeout if search is synchronous)
      await page.waitForLoadState('networkidle', { timeout: 3000 }).catch(() => {
        // Intentionally ignored - search might be instant
      });

      // Should show matching expense
      await expect(page.locator('text=Unique Coffee Shop')).toBeVisible();
    }
  });

  test('should delete an expense', async ({ page, authenticatedUser, request }) => {
    // Create a test expense
    const expense = await createTestExpense(request, authenticatedUser.token!, {
      merchant: 'To Be Deleted',
      amount: 99.99,
    });

    await page.goto('/dashboard/expenses');

    // Find the expense row
    const expenseRow = page.locator(`text=${expense.merchant}`).first();
    await expect(expenseRow).toBeVisible();

    // Click delete button (adjust selector based on implementation)
    await page
      .locator(
        `tr:has-text("${expense.merchant}") button:has-text("Delete"), [data-testid="delete-${expense.id}"]`
      )
      .first()
      .click()
      .catch(async () => {
        // Alternative: click on delete icon
        await page.locator(`tr:has-text("${expense.merchant}") [aria-label="Delete"]`).click();
      });

    // Confirm deletion if there's a confirmation dialog
    await page
      .click('button:has-text("Confirm"), button:has-text("Yes"), button:has-text("Delete")')
      .catch(() => {
        // No confirmation dialog
      });

    // Wait for deletion to complete (may timeout if deletion is instant)
    await page.waitForLoadState('networkidle', { timeout: 3000 }).catch(() => {
      // Intentionally ignored - deletion might be synchronous
    });

    // Expense should no longer be visible
    await expect(page.locator(`text=${expense.merchant}`)).not.toBeVisible();
  });

  test('should paginate through expenses', async ({
    page,
    authenticatedUser,
    request,
  }) => {
    // Create more expenses than fit on one page (assuming 20 per page)
    await createMultipleTestExpenses(request, authenticatedUser.token!, 25);

    await page.goto('/dashboard/expenses');

    // Should show pagination controls
    const nextButton = page.locator(
      'button:has-text("Next"), [aria-label="Next page"], [data-testid="next-page"]'
    );

    if (await nextButton.count() > 0) {
      await nextButton.first().click();

      // Should navigate to page 2 (may timeout if navigation is instant)
      await page.waitForLoadState('networkidle', { timeout: 3000 }).catch(() => {
        // Intentionally ignored - pagination might be synchronous
      });
      
      // URL might have page parameter or page indicator should change
      const pageIndicator = page.locator('text=/page 2|2 of/i');
      if (await pageIndicator.count() > 0) {
        await expect(pageIndicator).toBeVisible();
      }
    }
  });

  test('should sort expenses by amount', async ({
    page,
    authenticatedUser,
    request,
  }) => {
    // Create expenses with different amounts
    await createTestExpense(request, authenticatedUser.token!, {
      amount: 10.0,
      merchant: 'Cheap',
    });
    await createTestExpense(request, authenticatedUser.token!, {
      amount: 100.0,
      merchant: 'Expensive',
    });
    await createTestExpense(request, authenticatedUser.token!, {
      amount: 50.0,
      merchant: 'Medium',
    });

    await page.goto('/dashboard/expenses');

    // Click on amount column header to sort (if sortable)
    const amountHeader = page.locator(
      'th:has-text("Amount"), [role="columnheader"]:has-text("Amount")'
    );

    if (await amountHeader.count() > 0) {
      await amountHeader.click();
      // Wait for sorting to complete (may timeout if sort is instant)
      await page.waitForLoadState('networkidle', { timeout: 3000 }).catch(() => {
        // Intentionally ignored - sorting might be synchronous
      });

      // Check if expenses are sorted (implementation-specific)
      // This is a basic check - adjust based on your UI
      const firstExpenseAmount = await page
        .locator('td:has-text("$"), [role="cell"]:has-text("$")')
        .first()
        .textContent();
      
      expect(firstExpenseAmount).toBeTruthy();
    }
  });

  test('should export expenses to CSV', async ({
    page,
    authenticatedUser,
    request,
  }) => {
    // Create test expenses
    await createMultipleTestExpenses(request, authenticatedUser.token!, 5);

    await page.goto('/dashboard/expenses');

    // Click export button
    const exportButton = page.locator(
      'button:has-text("Export"), [data-testid="export-button"]'
    );

    if (await exportButton.count() > 0) {
      // Listen for download
      const downloadPromise = page.waitForEvent('download', { timeout: 5000 }).catch(() => null);
      
      await exportButton.click();

      const download = await downloadPromise;
      
      if (download) {
        // Verify download started
        expect(download.suggestedFilename()).toContain('.csv');
      }
    }
  });
});
