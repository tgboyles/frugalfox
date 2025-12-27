package com.tgboyles.frugalfox.expense;

import com.tgboyles.frugalfox.user.User;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
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

  public ExpenseService(ExpenseRepository expenseRepository) {
    this.expenseRepository = expenseRepository;
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
