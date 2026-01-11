package com.tgboyles.frugalfox.expense;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.tgboyles.frugalfox.user.User;
import com.tgboyles.frugalfox.user.UserRepository;

/** Data access tests for ExpenseRepository. */
@SpringBootTest
@Transactional
public class ExpenseRepositoryTest {

@Autowired private ExpenseRepository expenseRepository;

@Autowired private UserRepository userRepository;

@Autowired private jakarta.persistence.EntityManager entityManager;

private User testUser;
private User otherUser;

@BeforeEach
public void setup() {
	testUser = new User();
	testUser.setUsername("testuser");
	testUser.setPassword("password123");
	testUser.setEmail("test@example.com");
	testUser.setEnabled(true);
	testUser = userRepository.save(testUser);

	otherUser = new User();
	otherUser.setUsername("otheruser");
	otherUser.setPassword("password456");
	otherUser.setEmail("other@example.com");
	otherUser.setEnabled(true);
	otherUser = userRepository.save(otherUser);
}

@Test
public void save_NewExpense_PersistsExpenseWithGeneratedId() {
	// Arrange
	Expense expense = createExpense("Whole Foods", new BigDecimal("125.50"), "Groceries");

	// Act
	Expense saved = expenseRepository.save(expense);

	// Assert
	assertThat(saved.getId()).isNotNull();
	assertThat(saved.getMerchant()).isEqualTo("Whole Foods");
	assertThat(saved.getCreatedAt()).isNotNull();
	assertThat(saved.getUpdatedAt()).isNotNull();
}

@Test
public void findByIdAndUser_MatchingUserAndId_ReturnsExpense() {
	// Arrange
	Expense expense = createAndSaveExpense("Target", new BigDecimal("75.00"), "Shopping");

	// Act
	Optional<Expense> found = expenseRepository.findByIdAndUser(expense.getId(), testUser);

	// Assert
	assertThat(found).isPresent();
	assertThat(found.get().getMerchant()).isEqualTo("Target");
}

@Test
public void findByIdAndUser_DifferentUser_ReturnsEmpty() {
	// Arrange
	Expense expense = createAndSaveExpense("Store", new BigDecimal("50.00"), "Shopping");

	// Act
	Optional<Expense> found = expenseRepository.findByIdAndUser(expense.getId(), otherUser);

	// Assert
	assertThat(found).isEmpty();
}

@Test
public void findByUserAndCategory_MatchingCategory_ReturnsMatchingExpenses() {
	// Arrange
	createAndSaveExpense("Store1", new BigDecimal("50.00"), "Groceries");
	createAndSaveExpense("Store2", new BigDecimal("60.00"), "Shopping");
	createAndSaveExpense("Store3", new BigDecimal("70.00"), "Groceries");

	// Act
	List<Expense> groceries = expenseRepository.findByUserAndCategory(testUser, "Groceries");

	// Assert
	assertThat(groceries).hasSize(2);
	assertThat(groceries).allMatch(e -> e.getCategory().equals("Groceries"));
	assertThat(groceries).allMatch(e -> e.getUser().equals(testUser));
}

@Test
public void findByUserAndBank_MatchingBank_ReturnsMatchingExpenses() {
	// Arrange
	Expense expense1 = createExpense("Store1", new BigDecimal("50.00"), "Shopping");
	expense1.setBank("Chase");
	expenseRepository.save(expense1);

	Expense expense2 = createExpense("Store2", new BigDecimal("60.00"), "Shopping");
	expense2.setBank("Bank of America");
	expenseRepository.save(expense2);

	Expense expense3 = createExpense("Store3", new BigDecimal("70.00"), "Shopping");
	expense3.setBank("Chase");
	expenseRepository.save(expense3);

	// Act
	List<Expense> chaseExpenses = expenseRepository.findByUserAndBank(testUser, "Chase");

	// Assert
	assertThat(chaseExpenses).hasSize(2);
	assertThat(chaseExpenses).allMatch(e -> e.getBank().equals("Chase"));
}

@Test
public void findByUserAndDateBetween_DateRange_ReturnsExpensesInRange() {
	// Arrange
	Expense expense1 = createExpense("Store1", new BigDecimal("50.00"), "Shopping");
	expense1.setDate(LocalDate.of(2025, 12, 24));
	expenseRepository.save(expense1);

	Expense expense2 = createExpense("Store2", new BigDecimal("60.00"), "Shopping");
	expense2.setDate(LocalDate.of(2025, 12, 25));
	expenseRepository.save(expense2);

	Expense expense3 = createExpense("Store3", new BigDecimal("70.00"), "Shopping");
	expense3.setDate(LocalDate.of(2025, 12, 27));
	expenseRepository.save(expense3);

	// Act
	List<Expense> expenses =
		expenseRepository.findByUserAndDateBetween(
			testUser, LocalDate.of(2025, 12, 24), LocalDate.of(2025, 12, 26));

	// Assert
	assertThat(expenses).hasSize(2);
}

@Test
public void findByUserAndMerchantContainingIgnoreCase_PartialMatch_ReturnsMatchingExpenses() {
	// Arrange
	createAndSaveExpense("Whole Foods", new BigDecimal("50.00"), "Shopping");
	createAndSaveExpense("Whole Foods Market", new BigDecimal("60.00"), "Shopping");
	createAndSaveExpense("Target", new BigDecimal("70.00"), "Shopping");

	// Act
	List<Expense> wholeFoodsExpenses =
		expenseRepository.findByUserAndMerchantContainingIgnoreCase(testUser, "whole");

	// Assert
	assertThat(wholeFoodsExpenses).hasSize(2);
	assertThat(wholeFoodsExpenses)
		.allMatch(e -> e.getMerchant().toLowerCase().contains("whole"));
}

@Test
public void delete_ExistingExpense_RemovesFromDatabase() {
	// Arrange
	Expense expense = createAndSaveExpense("ToDelete", new BigDecimal("25.00"), "Test");

	// Act
	expenseRepository.delete(expense);

	// Assert
	Optional<Expense> found = expenseRepository.findByIdAndUser(expense.getId(), testUser);
	assertThat(found).isEmpty();
}

@Test
public void save_UpdatedExpense_PersistsChanges() {
	// Arrange
	Expense expense = createAndSaveExpense("Original", new BigDecimal("100.00"), "Shopping");
	LocalDate createdAt = expense.getCreatedAt().toLocalDate();

	// Act
	expense.setMerchant("Updated");
	expense.setAmount(new BigDecimal("150.00"));
	Expense updated = expenseRepository.save(expense);

	// Assert
	Optional<Expense> found = expenseRepository.findByIdAndUser(expense.getId(), testUser);
	assertThat(found).isPresent();
	assertThat(found.get().getMerchant()).isEqualTo("Updated");
	assertThat(found.get().getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
}

@Test
public void delete_CascadeFromUser_DeletesAllUserExpenses() {
	// Arrange
	createAndSaveExpense("Store1", new BigDecimal("50.00"), "Shopping");
	createAndSaveExpense("Store2", new BigDecimal("60.00"), "Food");

	List<Expense> beforeDelete = expenseRepository.findByUserAndCategory(testUser, "Shopping");
	assertThat(beforeDelete).hasSize(1);

	// Clear the persistence context to detach all entities
	entityManager.flush();
	entityManager.clear();

	Long userId = testUser.getId();

	// Act
	userRepository.deleteById(userId);
	userRepository.flush();

	// Assert - After user is deleted, expenses should be cascade deleted
	assertThat(expenseRepository.count()).isEqualTo(0);
}

@Test
public void findByUserAndCategory_DifferentUsers_IsolatesExpensesByUser() {
	// Arrange
	createAndSaveExpense("User1 Store", new BigDecimal("50.00"), "Shopping");

	Expense otherExpense = createExpense("User2 Store", new BigDecimal("100.00"), "Shopping");
	otherExpense.setUser(otherUser);
	expenseRepository.save(otherExpense);

	// Act
	List<Expense> testUserExpenses =
		expenseRepository.findByUserAndCategory(testUser, "Shopping");
	List<Expense> otherUserExpenses =
		expenseRepository.findByUserAndCategory(otherUser, "Shopping");

	// Assert
	assertThat(testUserExpenses).hasSize(1);
	assertThat(testUserExpenses.get(0).getMerchant()).isEqualTo("User1 Store");

	assertThat(otherUserExpenses).hasSize(1);
	assertThat(otherUserExpenses.get(0).getMerchant()).isEqualTo("User2 Store");
}

// Helper methods

private Expense createExpense(String merchant, BigDecimal amount, String category) {
	Expense expense = new Expense();
	expense.setDate(LocalDate.of(2025, 12, 26));
	expense.setMerchant(merchant);
	expense.setAmount(amount);
	expense.setBank("Chase");
	expense.setCategory(category);
	expense.setUser(testUser);
	return expense;
}

private Expense createAndSaveExpense(String merchant, BigDecimal amount, String category) {
	Expense expense = createExpense(merchant, amount, category);
	return expenseRepository.save(expense);
}
}
