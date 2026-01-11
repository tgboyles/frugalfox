package com.tgboyles.frugalfox.expense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.tgboyles.frugalfox.user.User;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/** Unit tests for ExpenseService. */
@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

@Mock private ExpenseRepository expenseRepository;

@Mock private Validator validator;

@InjectMocks private ExpenseService expenseService;

private User testUser;
private Expense testExpense;

@BeforeEach
public void setup() {
	testUser = new User();
	testUser.setId(1L);
	testUser.setUsername("testuser");
	testUser.setEmail("test@example.com");

	testExpense = new Expense();
	testExpense.setId(1L);
	testExpense.setDate(LocalDate.of(2025, 12, 26));
	testExpense.setMerchant("Whole Foods");
	testExpense.setAmount(new BigDecimal("125.50"));
	testExpense.setBank("Chase");
	testExpense.setCategory("Groceries");
	testExpense.setUser(testUser);
}

@Test
public void createExpense_ValidInput_SetsUserAndSavesExpense() {
	// Arrange
	Expense newExpense = new Expense();
	newExpense.setDate(LocalDate.of(2025, 12, 26));
	newExpense.setMerchant("Target");
	newExpense.setAmount(new BigDecimal("75.00"));
	newExpense.setBank("Chase");
	newExpense.setCategory("Shopping");

	when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

	// Act
	Expense result = expenseService.createExpense(newExpense, testUser);

	// Assert
	assertThat(result).isNotNull();
	assertThat(result.getUser()).isEqualTo(testUser);
	verify(expenseRepository).save(any(Expense.class));
}

@Test
public void testGetExpenseByIdSuccess() {
	when(expenseRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testExpense));

	Expense result = expenseService.getExpenseById(1L, testUser);

	assertThat(result).isEqualTo(testExpense);
	verify(expenseRepository).findByIdAndUser(1L, testUser);
}

@Test
public void testGetExpenseByIdNotFound() {
	when(expenseRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

	assertThatThrownBy(() -> expenseService.getExpenseById(999L, testUser))
		.isInstanceOf(ExpenseNotFoundException.class)
		.hasMessageContaining("Expense not found with id: 999");
}

@Test
public void testUpdateExpenseSuccess() {
	Expense updatedData = new Expense();
	updatedData.setDate(LocalDate.of(2025, 12, 26));
	updatedData.setMerchant("Whole Foods Market");
	updatedData.setAmount(new BigDecimal("130.00"));
	updatedData.setBank("Chase");
	updatedData.setCategory("Groceries");

	when(expenseRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testExpense));
	when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

	Expense result = expenseService.updateExpense(1L, updatedData, testUser);

	assertThat(result).isNotNull();
	verify(expenseRepository).findByIdAndUser(1L, testUser);
	verify(expenseRepository).save(any(Expense.class));
}

@Test
public void testUpdateExpenseNotFound() {
	Expense updatedData = new Expense();
	updatedData.setMerchant("Test");

	when(expenseRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

	assertThatThrownBy(() -> expenseService.updateExpense(999L, updatedData, testUser))
		.isInstanceOf(ExpenseNotFoundException.class);
}

@Test
public void testDeleteExpenseSuccess() {
	when(expenseRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testExpense));

	expenseService.deleteExpense(1L, testUser);

	verify(expenseRepository).findByIdAndUser(1L, testUser);
	verify(expenseRepository).delete(testExpense);
}

@Test
public void testDeleteExpenseNotFound() {
	when(expenseRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

	assertThatThrownBy(() -> expenseService.deleteExpense(999L, testUser))
		.isInstanceOf(ExpenseNotFoundException.class);
}

@Test
public void testSearchExpenses() {
	ExpenseSearchCriteria criteria = new ExpenseSearchCriteria();
	criteria.setCategory("Groceries");

	Pageable pageable = PageRequest.of(0, 20);
	Page<Expense> expectedPage = new PageImpl<>(Arrays.asList(testExpense));

	when(expenseRepository.findAll(any(Specification.class), any(Pageable.class)))
		.thenReturn(expectedPage);

	Page<Expense> result = expenseService.searchExpenses(criteria, testUser, pageable);

	assertThat(result).isNotNull();
	assertThat(result.getContent()).hasSize(1);
	assertThat(result.getContent().get(0)).isEqualTo(testExpense);
	verify(expenseRepository).findAll(any(Specification.class), any(Pageable.class));
}

@Test
public void testSearchExpensesWithMultipleCriteria() {
	ExpenseSearchCriteria criteria = new ExpenseSearchCriteria();
	criteria.setCategory("Groceries");
	criteria.setBank("Chase");
	criteria.setMinAmount(new BigDecimal("100.00"));
	criteria.setMaxAmount(new BigDecimal("200.00"));

	Pageable pageable = PageRequest.of(0, 20);
	Page<Expense> expectedPage = new PageImpl<>(Arrays.asList(testExpense));

	when(expenseRepository.findAll(any(Specification.class), any(Pageable.class)))
		.thenReturn(expectedPage);

	Page<Expense> result = expenseService.searchExpenses(criteria, testUser, pageable);

	assertThat(result).isNotNull();
	verify(expenseRepository).findAll(any(Specification.class), any(Pageable.class));
}

@Test
public void testImportExpensesSuccess() {
	String csvContent =
		"""
		date,merchant,amount,bank,category
		2025-01-01,Whole Foods,50.00,Chase,Groceries
		2025-01-02,Target,75.50,BofA,Shopping
		""";

	InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

	when(validator.validate(any(Expense.class))).thenReturn(Collections.emptySet());
	when(expenseRepository.saveAll(any())).thenReturn(Collections.emptyList());

	ImportResult result = expenseService.importExpenses(inputStream, testUser);

	assertThat(result.getTotalRows()).isEqualTo(2);
	assertThat(result.getSuccessfulImports()).isEqualTo(2);
	assertThat(result.getFailedImports()).isEqualTo(0);
	assertThat(result.getErrors()).isEmpty();

	// Capture and verify the expenses that were saved
	ArgumentCaptor<List<Expense>> expenseListCaptor = ArgumentCaptor.forClass(List.class);
	verify(expenseRepository).saveAll(expenseListCaptor.capture());

	List<Expense> savedExpenses = expenseListCaptor.getValue();
	assertThat(savedExpenses).hasSize(2);

	// Verify first expense from CSV (Whole Foods)
	Expense firstExpense = savedExpenses.get(0);
	assertThat(firstExpense.getDate()).isEqualTo(LocalDate.of(2025, 1, 1));
	assertThat(firstExpense.getMerchant()).isEqualTo("Whole Foods");
	assertThat(firstExpense.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
	assertThat(firstExpense.getBank()).isEqualTo("Chase");
	assertThat(firstExpense.getCategory()).isEqualTo("Groceries");
	assertThat(firstExpense.getUser()).isEqualTo(testUser);

	// Verify second expense from CSV (Target)
	Expense secondExpense = savedExpenses.get(1);
	assertThat(secondExpense.getDate()).isEqualTo(LocalDate.of(2025, 1, 2));
	assertThat(secondExpense.getMerchant()).isEqualTo("Target");
	assertThat(secondExpense.getAmount()).isEqualByComparingTo(new BigDecimal("75.50"));
	assertThat(secondExpense.getBank()).isEqualTo("BofA");
	assertThat(secondExpense.getCategory()).isEqualTo("Shopping");
	assertThat(secondExpense.getUser()).isEqualTo(testUser);
}

@Test
public void testImportExpensesExceedsRowLimit() {
	StringBuilder csvContent = new StringBuilder("date,merchant,amount,bank,category\n");
	for (int i = 0; i < 1001; i++) {
	csvContent.append("2025-01-01,Merchant,10.00,Chase,Category\n");
	}

	InputStream inputStream =
		new ByteArrayInputStream(csvContent.toString().getBytes(StandardCharsets.UTF_8));

	assertThatThrownBy(() -> expenseService.importExpenses(inputStream, testUser))
		.isInstanceOf(CsvImportException.class)
		.hasMessageContaining("exceeds maximum row limit of 1000");
}

@Test
public void testImportExpensesInvalidDateFormat() {
	String csvContent =
		"""
		date,merchant,amount,bank,category
		2025-13-01,Whole Foods,50.00,Chase,Groceries
		""";

	InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

	ImportResult result = expenseService.importExpenses(inputStream, testUser);

	assertThat(result.getTotalRows()).isEqualTo(1);
	assertThat(result.getSuccessfulImports()).isEqualTo(0);
	assertThat(result.getFailedImports()).isEqualTo(1);
	assertThat(result.getErrors()).hasSize(1);
	assertThat(result.getErrors().get(0)).contains("Invalid date format");
}

@Test
public void testImportExpensesInvalidAmountFormat() {
	String csvContent =
		"""
		date,merchant,amount,bank,category
		2025-01-01,Whole Foods,not-a-number,Chase,Groceries
		""";

	InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

	ImportResult result = expenseService.importExpenses(inputStream, testUser);

	assertThat(result.getTotalRows()).isEqualTo(1);
	assertThat(result.getSuccessfulImports()).isEqualTo(0);
	assertThat(result.getFailedImports()).isEqualTo(1);
	assertThat(result.getErrors()).hasSize(1);
	assertThat(result.getErrors().get(0)).contains("Invalid amount");
}

@Test
public void testImportExpensesMissingColumns() {
	String csvContent =
		"""
		date,merchant,amount,bank,category
		2025-01-01,Whole Foods,50.00
		""";

	InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

	ImportResult result = expenseService.importExpenses(inputStream, testUser);

	assertThat(result.getTotalRows()).isEqualTo(1);
	assertThat(result.getSuccessfulImports()).isEqualTo(0);
	assertThat(result.getFailedImports()).isEqualTo(1);
	assertThat(result.getErrors()).hasSize(1);
	assertThat(result.getErrors().get(0)).contains("Expected 5 columns");
}

@Test
public void testImportExpensesBlankFields() {
	String csvContent =
		"""
		date,merchant,amount,bank,category
		2025-01-01,,50.00,Chase,Groceries
		""";

	InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

	ImportResult result = expenseService.importExpenses(inputStream, testUser);

	assertThat(result.getTotalRows()).isEqualTo(1);
	assertThat(result.getSuccessfulImports()).isEqualTo(0);
	assertThat(result.getFailedImports()).isEqualTo(1);
	assertThat(result.getErrors()).hasSize(1);
	assertThat(result.getErrors().get(0)).contains("cannot be null or blank");
}

@Test
@SuppressWarnings("unchecked")
public void testImportExpensesValidationFailure() {
	String csvContent =
		"""
		date,merchant,amount,bank,category
		2025-01-01,Whole Foods,-50.00,Chase,Groceries
		""";

	InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

	ConstraintViolation<Expense> mockViolation = mock(ConstraintViolation.class);
	when(mockViolation.getMessage()).thenReturn("Amount must be greater than zero");

	Set<ConstraintViolation<Expense>> violations = Set.of(mockViolation);

	when(validator.validate(any(Expense.class))).thenReturn(violations);

	ImportResult result = expenseService.importExpenses(inputStream, testUser);

	assertThat(result.getTotalRows()).isEqualTo(1);
	assertThat(result.getSuccessfulImports()).isEqualTo(0);
	assertThat(result.getFailedImports()).isEqualTo(1);
	assertThat(result.getErrors()).hasSize(1);
	assertThat(result.getErrors().get(0)).contains("Validation failed");
}

@Test
public void testImportExpensesPartialSuccess() {
	String csvContent =
		"""
		date,merchant,amount,bank,category
		2025-01-01,Whole Foods,50.00,Chase,Groceries
		2025-13-01,Target,75.50,BofA,Shopping
		2025-01-03,Amazon,25.00,Chase,Online
		""";

	InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

	when(validator.validate(any(Expense.class))).thenReturn(Collections.emptySet());
	when(expenseRepository.saveAll(any())).thenReturn(Collections.emptyList());

	ImportResult result = expenseService.importExpenses(inputStream, testUser);

	assertThat(result.getTotalRows()).isEqualTo(3);
	assertThat(result.getSuccessfulImports()).isEqualTo(2);
	assertThat(result.getFailedImports()).isEqualTo(1);
	assertThat(result.getErrors()).hasSize(1);
	assertThat(result.getErrors().get(0)).contains("Invalid date format");
}

@Test
public void testImportExpensesBatchProcessing() {
	// Create CSV with 250 rows to trigger multiple batches (batch size is 100)
	StringBuilder csvContent = new StringBuilder("date,merchant,amount,bank,category\n");
	for (int i = 0; i < 250; i++) {
		csvContent.append("2025-01-01,Merchant").append(i).append(",10.00,Chase,Category\n");
	}

	InputStream inputStream =
		new ByteArrayInputStream(csvContent.toString().getBytes(StandardCharsets.UTF_8));

	when(validator.validate(any(Expense.class))).thenReturn(Collections.emptySet());
	when(expenseRepository.saveAll(any())).thenReturn(Collections.emptyList());

	ImportResult result = expenseService.importExpenses(inputStream, testUser);

	assertThat(result.getTotalRows()).isEqualTo(250);
	assertThat(result.getSuccessfulImports()).isEqualTo(250);
	assertThat(result.getFailedImports()).isEqualTo(0);
	assertThat(result.getErrors()).isEmpty();

	// Verify saveAll was called 3 times (100 + 100 + 50)
	verify(expenseRepository, times(3)).saveAll(any());
}

@Test
public void testExportExpensesToCsv() throws Exception {
	User testUser = new User();
	testUser.setId(1L);
	testUser.setUsername("testuser");

	List<Expense> expenses = Arrays.asList(
		createExpense(1L, testUser, LocalDate.of(2025, 1, 1), "Whole Foods", new BigDecimal("50.00"), "Chase", "Groceries"),
		createExpense(2L, testUser, LocalDate.of(2025, 1, 2), "Target", new BigDecimal("75.50"), "BofA", "Shopping")
	);

	when(expenseRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class)))
		.thenReturn(expenses);

	ExpenseSearchCriteria criteria = new ExpenseSearchCriteria();
	Pageable pageable = PageRequest.of(0, 20);

	String csv = expenseService.exportExpensesToCsv(criteria, testUser, pageable);

	assertThat(csv).isNotNull();
	assertThat(csv).contains("date,merchant,amount,bank,category");
	assertThat(csv).contains("2025-01-01,Whole Foods,50.00,Chase,Groceries");
	assertThat(csv).contains("2025-01-02,Target,75.50,BofA,Shopping");
}

@Test
public void testExportExpensesToCsvWithFilters() throws Exception {
	User testUser = new User();
	testUser.setId(1L);
	testUser.setUsername("testuser");

	List<Expense> expenses = Arrays.asList(
		createExpense(1L, testUser, LocalDate.of(2025, 1, 1), "Whole Foods", new BigDecimal("50.00"), "Chase", "Groceries")
	);

	when(expenseRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class)))
		.thenReturn(expenses);

	ExpenseSearchCriteria criteria = new ExpenseSearchCriteria();
	criteria.setCategory("Groceries");
	Pageable pageable = PageRequest.of(0, 20);

	String csv = expenseService.exportExpensesToCsv(criteria, testUser, pageable);

	assertThat(csv).isNotNull();
	assertThat(csv).contains("date,merchant,amount,bank,category");
	assertThat(csv).contains("2025-01-01,Whole Foods,50.00,Chase,Groceries");
}

@Test
public void testExportExpensesToCsvEmpty() throws Exception {
	User testUser = new User();
	testUser.setId(1L);
	testUser.setUsername("testuser");

	when(expenseRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class)))
		.thenReturn(Collections.emptyList());

	ExpenseSearchCriteria criteria = new ExpenseSearchCriteria();
	Pageable pageable = PageRequest.of(0, 20);

	String csv = expenseService.exportExpensesToCsv(criteria, testUser, pageable);

	assertThat(csv).isNotNull();
	assertThat(csv).contains("date,merchant,amount,bank,category");
	assertThat(csv.split("\n")).hasSize(1); // Only header row
}

private Expense createExpense(Long id, User user, LocalDate date, String merchant, BigDecimal amount, String bank, String category) {
	Expense expense = new Expense(user, date, merchant, amount, bank, category);
	expense.setId(id);
	return expense;
}
}
