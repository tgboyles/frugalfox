package com.tgboyles.frugalfox.expense;

import com.tgboyles.frugalfox.user.User;
import jakarta.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for expense operations.
 *
 * <p>All endpoints require authentication and are scoped to the authenticated user.
 */
@RestController
@RequestMapping("/expenses")
public class ExpenseController {

  private final ExpenseService expenseService;

  public ExpenseController(ExpenseService expenseService) {
    this.expenseService = expenseService;
  }

  /**
   * Creates a new expense for the authenticated user.
   *
   * @param expense the expense to create
   * @param user the authenticated user
   * @return the created expense with 201 status
   */
  @PostMapping
  public ResponseEntity<Expense> createExpense(
      @Valid @RequestBody Expense expense, @AuthenticationPrincipal User user) {
    Expense created = expenseService.createExpense(expense, user);
    return ResponseEntity.status(HttpStatus.CREATED)
        .header("Location", "/expenses/" + created.getId())
        .body(created);
  }

  /**
   * Imports expenses from a CSV file for the authenticated user.
   *
   * <p>Expected CSV format: date,merchant,amount,bank,category
   *
   * <p>The file must not exceed 1000 rows. Returns statistics about the import operation including
   * any validation errors encountered.
   *
   * @param file the CSV file to import
   * @param user the authenticated user
   * @return import result with statistics and any errors (200 status)
   * @throws CsvImportException if the file is malformed or exceeds row limit (400 status)
   */
  @PostMapping("/import")
  public ResponseEntity<ImportResult> importExpenses(
      @RequestParam("file") MultipartFile file, @AuthenticationPrincipal User user)
      throws IOException {

    // Validate file is present
    if (file.isEmpty()) {
      throw new CsvImportException("File is required and cannot be empty");
    }

    // Validate content type
    String contentType = file.getContentType();
    if (contentType == null
        || (!contentType.equals("text/csv") && !contentType.equals("application/csv"))) {
      throw new CsvImportException(
          "Invalid file type. Expected CSV file (text/csv), but got: " + contentType);
    }

    ImportResult result = expenseService.importExpenses(file.getInputStream(), user);
    return ResponseEntity.ok(result);
  }

  /**
   * Retrieves an expense by ID for the authenticated user.
   *
   * @param id the expense ID
   * @param user the authenticated user
   * @return the expense with 200 status
   */
  @GetMapping("/{id}")
  public ResponseEntity<Expense> getExpense(
      @PathVariable Long id, @AuthenticationPrincipal User user) {
    Expense expense = expenseService.getExpenseById(id, user);
    return ResponseEntity.ok(expense);
  }

  /**
   * Updates an existing expense for the authenticated user.
   *
   * @param id the expense ID
   * @param expense the updated expense details
   * @param user the authenticated user
   * @return the updated expense with 200 status
   */
  @PutMapping("/{id}")
  public ResponseEntity<Expense> updateExpense(
      @PathVariable Long id,
      @Valid @RequestBody Expense expense,
      @AuthenticationPrincipal User user) {
    Expense updated = expenseService.updateExpense(id, expense, user);
    return ResponseEntity.ok(updated);
  }

  /**
   * Deletes an expense for the authenticated user.
   *
   * @param id the expense ID
   * @param user the authenticated user
   * @return 204 No Content status
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteExpense(
      @PathVariable Long id, @AuthenticationPrincipal User user) {
    expenseService.deleteExpense(id, user);
    return ResponseEntity.noContent().build();
  }

  /**
   * Searches for expenses with optional filters, scoped to the authenticated user.
   *
   * @param category optional category filter (exact match)
   * @param bank optional bank filter (exact match)
   * @param merchant optional merchant filter (partial match, case-insensitive)
   * @param startDate optional start date filter (inclusive)
   * @param endDate optional end date filter (inclusive)
   * @param minAmount optional minimum amount filter (inclusive)
   * @param maxAmount optional maximum amount filter (inclusive)
   * @param pageable pagination and sorting parameters
   * @param user the authenticated user
   * @return a page of expenses with 200 status
   */
  @GetMapping
  public ResponseEntity<Page<Expense>> searchExpenses(
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String bank,
      @RequestParam(required = false) String merchant,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate,
      @RequestParam(required = false) BigDecimal minAmount,
      @RequestParam(required = false) BigDecimal maxAmount,
      @PageableDefault(size = 20, sort = "date") Pageable pageable,
      @AuthenticationPrincipal User user) {

    ExpenseSearchCriteria criteria = new ExpenseSearchCriteria();
    criteria.setCategory(category);
    criteria.setBank(bank);
    criteria.setMerchant(merchant);
    criteria.setStartDate(startDate);
    criteria.setEndDate(endDate);
    criteria.setMinAmount(minAmount);
    criteria.setMaxAmount(maxAmount);

    Page<Expense> expenses = expenseService.searchExpenses(criteria, user, pageable);
    return ResponseEntity.ok(expenses);
  }
}
