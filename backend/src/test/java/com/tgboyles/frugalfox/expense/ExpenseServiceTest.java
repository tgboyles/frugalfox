package com.tgboyles.frugalfox.expense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
public void testCreateExpense() {
	Expense newExpense = new Expense();
	newExpense.setDate(LocalDate.of(2025, 12, 26));
	newExpense.setMerchant("Target");
	newExpense.setAmount(new BigDecimal("75.00"));
	newExpense.setBank("Chase");
	newExpense.setCategory("Shopping");

	when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

	Expense result = expenseService.createExpense(newExpense, testUser);

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
	verify(expenseRepository).saveAll(any());
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
}
