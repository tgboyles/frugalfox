package com.tgboyles.frugalfox.expense;

/**
 * Exception thrown when an expense is not found.
 */
public class ExpenseNotFoundException extends RuntimeException {

public ExpenseNotFoundException(Long id) {
	super("Expense not found with id: " + id);
}
}
