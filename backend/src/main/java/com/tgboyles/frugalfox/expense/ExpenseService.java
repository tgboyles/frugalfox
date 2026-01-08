package com.tgboyles.frugalfox.expense;

import com.tgboyles.frugalfox.user.User;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for expense operations.
 *
 * <p>All operations are scoped to the authenticated user to ensure data isolation.
 */
@Service
@Transactional
public class ExpenseService {

  private final ExpenseRepository expenseRepository;
  private final Validator validator;

  public ExpenseService(ExpenseRepository expenseRepository, Validator validator) {
    this.expenseRepository = expenseRepository;
    this.validator = validator;
  }

  /**
   * Creates a new expense for the given user.
   *
   * @param expense the expense to create
   * @param user the user who owns the expense
   * @return the created expense
   */
  public Expense createExpense(Expense expense, User user) {
    expense.setUser(user);
    return expenseRepository.save(expense);
  }

  /**
   * Retrieves an expense by ID for the given user.
   *
   * @param id the expense ID
   * @param user the user
   * @return the expense
   * @throws ExpenseNotFoundException if expense not found or doesn't belong to user
   */
  @Transactional(readOnly = true)
  public Expense getExpenseById(Long id, User user) {
    return expenseRepository
        .findByIdAndUser(id, user)
        .orElseThrow(() -> new ExpenseNotFoundException(id));
  }

  /**
   * Updates an existing expense for the given user.
   *
   * @param id the expense ID
   * @param expenseDetails the updated expense details
   * @param user the user
   * @return the updated expense
   * @throws ExpenseNotFoundException if expense not found or doesn't belong to user
   */
  public Expense updateExpense(Long id, Expense expenseDetails, User user) {
    Expense expense = getExpenseById(id, user);

    expense.setDate(expenseDetails.getDate());
    expense.setMerchant(expenseDetails.getMerchant());
    expense.setAmount(expenseDetails.getAmount());
    expense.setBank(expenseDetails.getBank());
    expense.setCategory(expenseDetails.getCategory());

    return expenseRepository.save(expense);
  }

  /**
   * Deletes an expense for the given user.
   *
   * @param id the expense ID
   * @param user the user
   * @throws ExpenseNotFoundException if expense not found or doesn't belong to user
   */
  public void deleteExpense(Long id, User user) {
    Expense expense = getExpenseById(id, user);
    expenseRepository.delete(expense);
  }

  /**
   * Searches for expenses using dynamic criteria, scoped to the given user.
   *
   * @param criteria the search criteria
   * @param user the user
   * @param pageable the pagination information
   * @return a page of expenses
   */
  @Transactional(readOnly = true)
  public Page<Expense> searchExpenses(
      ExpenseSearchCriteria criteria, User user, Pageable pageable) {
    Specification<Expense> spec = buildSpecification(criteria, user);
    return expenseRepository.findAll(spec, pageable);
  }

  /**
   * Imports expenses from a CSV file for the given user.
   *
   * <p>Expected CSV format: date,merchant,amount,bank,category
   *
   * <p>Validates the file has no more than 1000 rows and that all rows are well-formed.
   *
   * @param inputStream the CSV file input stream
   * @param user the user who owns the expenses
   * @return import result with statistics and any errors
   * @throws CsvImportException if the file is malformed or exceeds row limit
   */
  public ImportResult importExpenses(InputStream inputStream, User user) {
    ImportResult result = new ImportResult();
    List<Expense> expensesToSave = new ArrayList<>();
    int rowNumber = 0;

    try (Reader reader = new InputStreamReader(inputStream);
        CSVParser csvParser =
            new CSVParser(
                reader,
                CSVFormat.DEFAULT
                    .builder()
                    .setHeader("date", "merchant", "amount", "bank", "category")
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build())) {

      List<CSVRecord> records = csvParser.getRecords();

      // Check row limit
      if (records.size() > 1000) {
        throw new CsvImportException(
            "File exceeds maximum row limit of 1000. Found " + records.size() + " rows.");
      }

      result.setTotalRows(records.size());

      for (CSVRecord record : records) {
        rowNumber = (int) record.getRecordNumber();

        try {
          // Validate required fields are present
          if (record.size() < 5) {
            throw new CsvImportException(
                String.format(
                    "Row %d: Expected 5 columns (date,merchant,amount,bank,category) but found %d",
                    rowNumber, record.size()));
          }

          // Parse fields
          String dateStr = record.get("date");
          String merchant = record.get("merchant");
          String amountStr = record.get("amount");
          String bank = record.get("bank");
          String category = record.get("category");

          // Validate non-empty
          if (dateStr.isBlank()
              || merchant.isBlank()
              || amountStr.isBlank()
              || bank.isBlank()
              || category.isBlank()) {
            throw new CsvImportException(
                String.format("Row %d: All fields are required and cannot be blank", rowNumber));
          }

          // Parse date
          LocalDate date;
          try {
            date = LocalDate.parse(dateStr);
          } catch (DateTimeParseException e) {
            throw new CsvImportException(
                String.format(
                    "Row %d: Invalid date format '%s'. Expected ISO format (YYYY-MM-DD)",
                    rowNumber, dateStr),
                e);
          }

          // Parse amount
          BigDecimal amount;
          try {
            amount = new BigDecimal(amountStr);
          } catch (NumberFormatException e) {
            throw new CsvImportException(
                String.format("Row %d: Invalid amount '%s'. Expected numeric value", rowNumber, amountStr),
                e);
          }

          // Create expense object
          Expense expense = new Expense(user, date, merchant, amount, bank, category);

          // Validate using Bean Validation
          Set<ConstraintViolation<Expense>> violations = validator.validate(expense);
          if (!violations.isEmpty()) {
            List<String> violationMessages = new ArrayList<>();
            for (ConstraintViolation<Expense> violation : violations) {
              violationMessages.add(violation.getMessage());
            }
            throw new CsvImportException(
                String.format(
                    "Row %d: Validation failed: %s", rowNumber, String.join(", ", violationMessages)));
          }

          expensesToSave.add(expense);

        } catch (CsvImportException e) {
          result.addError(e.getMessage());
          result.setFailedImports(result.getFailedImports() + 1);
        } catch (Exception e) {
          result.addError(String.format("Row %d: Unexpected error: %s", rowNumber, e.getMessage()));
          result.setFailedImports(result.getFailedImports() + 1);
        }
      }

      // Save all valid expenses
      if (!expensesToSave.isEmpty()) {
        expenseRepository.saveAll(expensesToSave);
        result.setSuccessfulImports(expensesToSave.size());
      }

    } catch (CsvImportException e) {
      // Re-throw validation exceptions
      throw e;
    } catch (IOException e) {
      throw new CsvImportException("Failed to read CSV file: " + e.getMessage(), e);
    } catch (Exception e) {
      throw new CsvImportException(
          "Failed to parse CSV file. Please ensure it is a valid CSV with columns: date,merchant,amount,bank,category",
          e);
    }

    return result;
  }

  /**
   * Builds a JPA Specification from search criteria.
   *
   * @param criteria the search criteria
   * @param user the user to scope results to
   * @return the specification
   */
  private Specification<Expense> buildSpecification(ExpenseSearchCriteria criteria, User user) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      // Always filter by user
      predicates.add(criteriaBuilder.equal(root.get("user"), user));

      // Category filter (exact match)
      if (criteria.getCategory() != null && !criteria.getCategory().isEmpty()) {
        predicates.add(criteriaBuilder.equal(root.get("category"), criteria.getCategory()));
      }

      // Bank filter (exact match)
      if (criteria.getBank() != null && !criteria.getBank().isEmpty()) {
        predicates.add(criteriaBuilder.equal(root.get("bank"), criteria.getBank()));
      }

      // Merchant filter (partial match, case-insensitive)
      if (criteria.getMerchant() != null && !criteria.getMerchant().isEmpty()) {
        predicates.add(
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("merchant")),
                "%" + criteria.getMerchant().toLowerCase() + "%"));
      }

      // Date range filter
      if (criteria.getStartDate() != null) {
        predicates.add(
            criteriaBuilder.greaterThanOrEqualTo(root.get("date"), criteria.getStartDate()));
      }
      if (criteria.getEndDate() != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), criteria.getEndDate()));
      }

      // Amount range filter
      if (criteria.getMinAmount() != null) {
        predicates.add(
            criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), criteria.getMinAmount()));
      }
      if (criteria.getMaxAmount() != null) {
        predicates.add(
            criteriaBuilder.lessThanOrEqualTo(root.get("amount"), criteria.getMaxAmount()));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
