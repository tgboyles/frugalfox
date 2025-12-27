package com.tgboyles.frugalfox.expense;

import static org.assertj.core.api.Assertions.assertThat;

import com.tgboyles.frugalfox.user.User;
import com.tgboyles.frugalfox.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

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
  public void testSaveAndFindExpense() {
    Expense expense = createExpense("Whole Foods", new BigDecimal("125.50"), "Groceries");
    Expense saved = expenseRepository.save(expense);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getMerchant()).isEqualTo("Whole Foods");
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getUpdatedAt()).isNotNull();
  }

  @Test
  public void testFindByIdAndUser() {
    Expense expense = createAndSaveExpense("Target", new BigDecimal("75.00"), "Shopping");

    Optional<Expense> found = expenseRepository.findByIdAndUser(expense.getId(), testUser);

    assertThat(found).isPresent();
    assertThat(found.get().getMerchant()).isEqualTo("Target");
  }

  @Test
  public void testFindByIdAndUserNotFound() {
    Expense expense = createAndSaveExpense("Store", new BigDecimal("50.00"), "Shopping");

    Optional<Expense> found = expenseRepository.findByIdAndUser(expense.getId(), otherUser);

    assertThat(found).isEmpty();
  }

  @Test
  public void testFindByUserAndCategory() {
    createAndSaveExpense("Store1", new BigDecimal("50.00"), "Groceries");
    createAndSaveExpense("Store2", new BigDecimal("60.00"), "Shopping");
    createAndSaveExpense("Store3", new BigDecimal("70.00"), "Groceries");

    List<Expense> groceries = expenseRepository.findByUserAndCategory(testUser, "Groceries");

    assertThat(groceries).hasSize(2);
    assertThat(groceries).allMatch(e -> e.getCategory().equals("Groceries"));
    assertThat(groceries).allMatch(e -> e.getUser().equals(testUser));
  }

  @Test
  public void testFindByUserAndBank() {
    Expense expense1 = createExpense("Store1", new BigDecimal("50.00"), "Shopping");
    expense1.setBank("Chase");
    expenseRepository.save(expense1);

    Expense expense2 = createExpense("Store2", new BigDecimal("60.00"), "Shopping");
    expense2.setBank("Bank of America");
    expenseRepository.save(expense2);

    Expense expense3 = createExpense("Store3", new BigDecimal("70.00"), "Shopping");
    expense3.setBank("Chase");
    expenseRepository.save(expense3);

    List<Expense> chaseExpenses = expenseRepository.findByUserAndBank(testUser, "Chase");

    assertThat(chaseExpenses).hasSize(2);
    assertThat(chaseExpenses).allMatch(e -> e.getBank().equals("Chase"));
  }

  @Test
  public void testFindByUserAndDateBetween() {
    Expense expense1 = createExpense("Store1", new BigDecimal("50.00"), "Shopping");
    expense1.setDate(LocalDate.of(2025, 12, 24));
    expenseRepository.save(expense1);

    Expense expense2 = createExpense("Store2", new BigDecimal("60.00"), "Shopping");
    expense2.setDate(LocalDate.of(2025, 12, 25));
    expenseRepository.save(expense2);

    Expense expense3 = createExpense("Store3", new BigDecimal("70.00"), "Shopping");
    expense3.setDate(LocalDate.of(2025, 12, 27));
    expenseRepository.save(expense3);

    List<Expense> expenses =
        expenseRepository.findByUserAndDateBetween(
            testUser, LocalDate.of(2025, 12, 24), LocalDate.of(2025, 12, 26));

    assertThat(expenses).hasSize(2);
  }

  @Test
  public void testFindByUserAndMerchantContainingIgnoreCase() {
    createAndSaveExpense("Whole Foods", new BigDecimal("50.00"), "Shopping");
    createAndSaveExpense("Whole Foods Market", new BigDecimal("60.00"), "Shopping");
    createAndSaveExpense("Target", new BigDecimal("70.00"), "Shopping");

    List<Expense> wholeFoodsExpenses =
        expenseRepository.findByUserAndMerchantContainingIgnoreCase(testUser, "whole");

    assertThat(wholeFoodsExpenses).hasSize(2);
    assertThat(wholeFoodsExpenses)
        .allMatch(e -> e.getMerchant().toLowerCase().contains("whole"));
  }

  @Test
  public void testDeleteExpense() {
    Expense expense = createAndSaveExpense("ToDelete", new BigDecimal("25.00"), "Test");

    expenseRepository.delete(expense);

    Optional<Expense> found = expenseRepository.findByIdAndUser(expense.getId(), testUser);
    assertThat(found).isEmpty();
  }

  @Test
  public void testUpdateExpense() {
    Expense expense = createAndSaveExpense("Original", new BigDecimal("100.00"), "Shopping");
    LocalDate createdAt = expense.getCreatedAt().toLocalDate();

    expense.setMerchant("Updated");
    expense.setAmount(new BigDecimal("150.00"));
    Expense updated = expenseRepository.save(expense);

    Optional<Expense> found = expenseRepository.findByIdAndUser(expense.getId(), testUser);
    assertThat(found).isPresent();
    assertThat(found.get().getMerchant()).isEqualTo("Updated");
    assertThat(found.get().getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
  }

  @Test
  public void testCascadeDeleteWithUser() {
    createAndSaveExpense("Store1", new BigDecimal("50.00"), "Shopping");
    createAndSaveExpense("Store2", new BigDecimal("60.00"), "Food");

    List<Expense> beforeDelete = expenseRepository.findByUserAndCategory(testUser, "Shopping");
    assertThat(beforeDelete).hasSize(1);

    // Clear the persistence context to detach all entities
    entityManager.flush();
    entityManager.clear();

    Long userId = testUser.getId();
    userRepository.deleteById(userId);
    userRepository.flush();

    // After user is deleted, expenses should be cascade deleted
    assertThat(expenseRepository.count()).isEqualTo(0);
  }

  @Test
  public void testUserIsolation() {
    createAndSaveExpense("User1 Store", new BigDecimal("50.00"), "Shopping");

    Expense otherExpense = createExpense("User2 Store", new BigDecimal("100.00"), "Shopping");
    otherExpense.setUser(otherUser);
    expenseRepository.save(otherExpense);

    List<Expense> testUserExpenses =
        expenseRepository.findByUserAndCategory(testUser, "Shopping");
    assertThat(testUserExpenses).hasSize(1);
    assertThat(testUserExpenses.get(0).getMerchant()).isEqualTo("User1 Store");

    List<Expense> otherUserExpenses =
        expenseRepository.findByUserAndCategory(otherUser, "Shopping");
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
