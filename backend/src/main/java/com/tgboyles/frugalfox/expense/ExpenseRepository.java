package com.tgboyles.frugalfox.expense;

import com.tgboyles.frugalfox.user.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Expense entity.
 *
 * <p>Provides database operations with user-scoped queries.
 */
@Repository
public interface ExpenseRepository
    extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

  /**
   * Finds an expense by ID and user.
   *
   * @param id the expense ID
   * @param user the user
   * @return an Optional containing the expense if found
   */
  Optional<Expense> findByIdAndUser(Long id, User user);

  /**
   * Finds all expenses for a user by category.
   *
   * @param category the category
   * @param user the user
   * @return list of expenses
   */
  List<Expense> findByUserAndCategory(User user, String category);

  /**
   * Finds all expenses for a user by bank.
   *
   * @param user the user
   * @param bank the bank
   * @return list of expenses
   */
  List<Expense> findByUserAndBank(User user, String bank);

  /**
   * Finds all expenses for a user within a date range.
   *
   * @param user the user
   * @param startDate the start date
   * @param endDate the end date
   * @return list of expenses
   */
  List<Expense> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

  /**
   * Finds all expenses for a user with merchant containing the search term (case-insensitive).
   *
   * @param user the user
   * @param merchant the merchant search term
   * @return list of expenses
   */
  List<Expense> findByUserAndMerchantContainingIgnoreCase(User user, String merchant);
}
