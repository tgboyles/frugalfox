package com.tgboyles.frugalfox.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.Test;

import io.restassured.http.ContentType;

/**
 * Integration tests for expense search and filtering.
 *
 * <p>Tests various search parameters and pagination.
 */
public class ExpenseSearchIT extends BaseIntegrationTest {

	@Test
	public void testSearchAllExpenses_Success() {
		String username = generateUniqueUsername();
		String token = registerAndGetToken(username, "password123", username + "@example.com");

		// Create multiple expenses
		createExpense(token, "2025-12-26", "Whole Foods", "125.50", "Chase", "Groceries");
		createExpense(token, "2025-12-27", "Target", "200.00", "Chase", "Shopping");
		createExpense(token, "2025-12-28", "Shell", "45.00", "Amex", "Gas");

		given()
			.header("Authorization", "Bearer " + token)
			.when()
			.get("/expenses")
			.then()
			.statusCode(200)
			.body("content", hasSize(3))
			.body("totalElements", equalTo(3));
	}

	@Test
	public void testSearchExpensesByCategory_Success() {
		String username = generateUniqueUsername();
		String token = registerAndGetToken(username, "password123", username + "@example.com");

		// Create expenses with different categories
		createExpense(token, "2025-12-26", "Whole Foods", "125.50", "Chase", "Groceries");
		createExpense(token, "2025-12-27", "Target", "200.00", "Chase", "Shopping");
		createExpense(token, "2025-12-28", "Safeway", "85.00", "Chase", "Groceries");

		given()
			.header("Authorization", "Bearer " + token)
			.queryParam("category", "Groceries")
			.when()
			.get("/expenses")
			.then()
			.statusCode(200)
			.body("content", hasSize(2))
			.body("content[0].category", equalTo("Groceries"))
			.body("content[1].category", equalTo("Groceries"));
	}

	@Test
	public void testSearchExpensesByBank_Success() {
		String username = generateUniqueUsername();
		String token = registerAndGetToken(username, "password123", username + "@example.com");

		// Create expenses with different banks
		createExpense(token, "2025-12-26", "Whole Foods", "125.50", "Chase", "Groceries");
		createExpense(token, "2025-12-27", "Target", "200.00", "Amex", "Shopping");
		createExpense(token, "2025-12-28", "Shell", "45.00", "Chase", "Gas");

		given()
			.header("Authorization", "Bearer " + token)
			.queryParam("bank", "Chase")
			.when()
			.get("/expenses")
			.then()
			.statusCode(200)
			.body("content", hasSize(2))
			.body("content[0].bank", equalTo("Chase"))
			.body("content[1].bank", equalTo("Chase"));
	}

	@Test
	public void testSearchExpensesByMerchant_PartialMatch() {
		String username = generateUniqueUsername();
		String token = registerAndGetToken(username, "password123", username + "@example.com");

		// Create expenses with similar merchant names
		createExpense(token, "2025-12-26", "Whole Foods Market", "125.50", "Chase", "Groceries");
		createExpense(token, "2025-12-27", "Target", "200.00", "Chase", "Shopping");
		createExpense(token, "2025-12-28", "Whole Foods", "85.00", "Chase", "Groceries");

		given()
			.header("Authorization", "Bearer " + token)
			.queryParam("merchant", "Whole")
			.when()
			.get("/expenses")
			.then()
			.statusCode(200)
			.body("content", hasSize(2));
	}

	@Test
	public void testSearchExpensesByDateRange_Success() {
		String username = generateUniqueUsername();
		String token = registerAndGetToken(username, "password123", username + "@example.com");

		// Create expenses with different dates
		createExpense(token, "2025-12-20", "Old Store", "50.00", "Chase", "Shopping");
		createExpense(token, "2025-12-26", "Whole Foods", "125.50", "Chase", "Groceries");
		createExpense(token, "2025-12-27", "Target", "200.00", "Chase", "Shopping");
		createExpense(token, "2026-01-05", "Future Store", "100.00", "Chase", "Shopping");

		given()
			.header("Authorization", "Bearer " + token)
			.queryParam("startDate", "2025-12-26")
			.queryParam("endDate", "2025-12-31")
			.when()
			.get("/expenses")
			.then()
			.statusCode(200)
			.body("content", hasSize(2));
	}

	@Test
	public void testSearchExpensesByAmountRange_Success() {
		String username = generateUniqueUsername();
		String token = registerAndGetToken(username, "password123", username + "@example.com");

		// Create expenses with different amounts
		createExpense(token, "2025-12-26", "Small Purchase", "25.00", "Chase", "Shopping");
		createExpense(token, "2025-12-27", "Medium Purchase", "125.50", "Chase", "Shopping");
		createExpense(token, "2025-12-28", "Large Purchase", "500.00", "Chase", "Shopping");

		given()
			.header("Authorization", "Bearer " + token)
			.queryParam("minAmount", "100")
			.queryParam("maxAmount", "300")
			.when()
			.get("/expenses")
			.then()
			.statusCode(200)
			.body("content", hasSize(1))
			.body("content[0].amount", equalTo(125.5f));
	}

	@Test
	public void testSearchExpensesWithMultipleFilters_Success() {
		String username = generateUniqueUsername();
		String token = registerAndGetToken(username, "password123", username + "@example.com");

		// Create various expenses
		createExpense(token, "2025-12-26", "Whole Foods", "125.50", "Chase", "Groceries");
		createExpense(token, "2025-12-27", "Target", "200.00", "Chase", "Shopping");
		createExpense(token, "2025-12-28", "Shell", "45.00", "Amex", "Gas");
		createExpense(token, "2025-12-29", "Safeway", "85.00", "Chase", "Groceries");

		// Search with multiple filters
		given()
			.header("Authorization", "Bearer " + token)
			.queryParam("category", "Groceries")
			.queryParam("bank", "Chase")
			.queryParam("minAmount", "100")
			.when()
			.get("/expenses")
			.then()
			.statusCode(200)
			.body("content", hasSize(1))
			.body("content[0].merchant", equalTo("Whole Foods"));
	}

	@Test
	public void testSearchExpensesWithPagination_Success() {
		String username = generateUniqueUsername();
		String token = registerAndGetToken(username, "password123", username + "@example.com");

		// Create many expenses
		for (int i = 1; i <= 25; i++) {
			createExpense(token, "2025-12-26", "Store " + i, String.valueOf(100.00 + i), "Chase", "Shopping");
		}

		// Get first page (default size is 20)
		given()
			.header("Authorization", "Bearer " + token)
			.queryParam("page", "0")
			.queryParam("size", "10")
			.when()
			.get("/expenses")
			.then()
			.statusCode(200)
			.body("content", hasSize(10))
			.body("totalElements", equalTo(25))
			.body("totalPages", equalTo(3))
			.body("number", equalTo(0));

		// Get second page
		given()
			.header("Authorization", "Bearer " + token)
			.queryParam("page", "1")
			.queryParam("size", "10")
			.when()
			.get("/expenses")
			.then()
			.statusCode(200)
			.body("content", hasSize(10))
			.body("number", equalTo(1));
	}

	@Test
	public void testSearchExpenses_UserIsolation() {
		// Create expenses for user 1
		String username1 = generateUniqueUsername();
		String token1 = registerAndGetToken(username1, "password123", username1 + "@example.com");
		createExpense(token1, "2025-12-26", "User1 Store", "100.00", "Chase", "Shopping");
		createExpense(token1, "2025-12-27", "User1 Store 2", "200.00", "Chase", "Shopping");

		// Create expenses for user 2
		String username2 = generateUniqueUsername();
		String token2 = registerAndGetToken(username2, "password123", username2 + "@example.com");
		createExpense(token2, "2025-12-26", "User2 Store", "150.00", "Chase", "Shopping");

		// User 1 should only see their expenses
		given()
			.header("Authorization", "Bearer " + token1)
			.when()
			.get("/expenses")
			.then()
			.statusCode(200)
			.body("content", hasSize(2));

		// User 2 should only see their expenses
		given()
			.header("Authorization", "Bearer " + token2)
			.when()
			.get("/expenses")
			.then()
			.statusCode(200)
			.body("content", hasSize(1));
	}

	/**
	 * Helper method to create an expense.
	 */
	private void createExpense(String token, String date, String merchant, String amount, String bank, String category) {
		String expenseJson = createExpenseJson(date, merchant, amount, bank, category);
		given()
			.header("Authorization", "Bearer " + token)
			.contentType(ContentType.JSON)
			.body(expenseJson)
			.when()
			.post("/expenses")
			.then()
			.statusCode(201);
	}
}
