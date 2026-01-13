package com.tgboyles.frugalfox.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * Integration tests for expense CRUD operations.
 *
 * <p>Tests creating, reading, updating, and deleting expenses.
 */
public class ExpenseCrudIT extends BaseIntegrationTest {

	@Test
	public void testCreateExpense_Success() {
		String username = generateUniqueUsername();
		String token = registerAndGetToken(username, "password123", username + "@example.com");

		String expenseJson = createExpenseJson("2025-12-26", "Whole Foods", "125.50", "Chase", "Groceries");

		given()
			.header("Authorization", "Bearer " + token)
			.contentType(ContentType.JSON)
			.body(expenseJson)
			.when()
			.post("/expenses")
			.then()
			.statusCode(201)
			.body("id", notNullValue())
			.body("date", equalTo("2025-12-26"))
			.body("merchant", equalTo("Whole Foods"))
			.body("amount", equalTo(125.50f))
			.body("bank", equalTo("Chase"))
			.body("category", equalTo("Groceries"));
	}

	@Test
	public void testCreateExpenseWithoutAuth_Fails() {
		String expenseJson = createExpenseJson("2025-12-26", "Whole Foods", "125.50", "Chase", "Groceries");

		given()
			.contentType(ContentType.JSON)
			.body(expenseJson)
			.when()
			.post("/expenses")
			.then()
			.statusCode(403); // Forbidden - Spring Security default for unauthenticated requests
	}

	@Test
	public void testCreateExpenseWithInvalidData_Fails() {
		String username = generateUniqueUsername();
		String token = registerAndGetToken(username, "password123", username + "@example.com");

		// Missing required field (merchant)
		String invalidExpenseJson = "{\"date\":\"2025-12-26\",\"amount\":125.50,\"bank\":\"Chase\",\"category\":\"Groceries\"}";

		given()
			.header("Authorization", "Bearer " + token)
			.contentType(ContentType.JSON)
			.body(invalidExpenseJson)
			.when()
			.post("/expenses")
			.then()
			.statusCode(400); // Validation error
	}

	@Test
	public void testGetExpenseById_Success() {
		String username = generateUniqueUsername();
		String token = registerAndGetToken(username, "password123", username + "@example.com");

		// Create expense
		String expenseJson = createExpenseJson("2025-12-26", "Whole Foods", "125.50", "Chase", "Groceries");
		int expenseId = given()
			.header("Authorization", "Bearer " + token)
			.contentType(ContentType.JSON)
			.body(expenseJson)
			.when()
			.post("/expenses")
			.then()
			.statusCode(201)
			.extract()
			.path("id");

		// Get expense by ID
		given()
			.header("Authorization", "Bearer " + token)
			.when()
			.get("/expenses/" + expenseId)
			.then()
			.statusCode(200)
			.body("id", equalTo(expenseId))
			.body("merchant", equalTo("Whole Foods"));
	}

	@Test
	public void testGetExpenseById_NotFound() {
		String username = generateUniqueUsername();
		String token = registerAndGetToken(username, "password123", username + "@example.com");

		given()
			.header("Authorization", "Bearer " + token)
			.when()
			.get("/expenses/999999")
			.then()
			.statusCode(404); // Not found
	}

	@Test
	public void testGetExpenseById_WrongUser_NotFound() {
		// Create expense with user 1
		String username1 = generateUniqueUsername();
		String token1 = registerAndGetToken(username1, "password123", username1 + "@example.com");

		String expenseJson = createExpenseJson("2025-12-26", "Whole Foods", "125.50", "Chase", "Groceries");
		Response createResponse = given()
			.header("Authorization", "Bearer " + token1)
			.contentType(ContentType.JSON)
			.body(expenseJson)
			.when()
			.post("/expenses")
			.then()
			.statusCode(201)
			.extract()
			.response();

		Long expenseId = createResponse.jsonPath().getLong("id");

		// Try to get with user 2
		String username2 = generateUniqueUsername();
		String token2 = registerAndGetToken(username2, "password123", username2 + "@example.com");

		given()
			.header("Authorization", "Bearer " + token2)
			.when()
			.get("/expenses/" + expenseId)
			.then()
			.statusCode(404); // Not found - user isolation
	}

	@Test
	public void testUpdateExpense_Success() {
		String username = generateUniqueUsername();
		String token = registerAndGetToken(username, "password123", username + "@example.com");

		// Create expense
		String expenseJson = createExpenseJson("2025-12-26", "Whole Foods", "125.50", "Chase", "Groceries");
		Response createResponse = given()
			.header("Authorization", "Bearer " + token)
			.contentType(ContentType.JSON)
			.body(expenseJson)
			.when()
			.post("/expenses")
			.then()
			.statusCode(201)
			.extract()
			.response();

		Long expenseId = createResponse.jsonPath().getLong("id");

		// Update expense
		String updatedExpenseJson = createExpenseJson("2025-12-27", "Target", "200.00", "Chase", "Shopping");

		given()
			.header("Authorization", "Bearer " + token)
			.contentType(ContentType.JSON)
			.body(updatedExpenseJson)
			.when()
			.put("/expenses/" + expenseId)
			.then()
			.statusCode(200)
			.body("id", equalTo(expenseId.intValue()))
			.body("date", equalTo("2025-12-27"))
			.body("merchant", equalTo("Target"))
			.body("amount", equalTo(200.0f));
	}

	@Test
	public void testUpdateExpense_WrongUser_NotFound() {
		// Create expense with user 1
		String username1 = generateUniqueUsername();
		String token1 = registerAndGetToken(username1, "password123", username1 + "@example.com");

		String expenseJson = createExpenseJson("2025-12-26", "Whole Foods", "125.50", "Chase", "Groceries");
		Response createResponse = given()
			.header("Authorization", "Bearer " + token1)
			.contentType(ContentType.JSON)
			.body(expenseJson)
			.when()
			.post("/expenses")
			.then()
			.statusCode(201)
			.extract()
			.response();

		Long expenseId = createResponse.jsonPath().getLong("id");

		// Try to update with user 2
		String username2 = generateUniqueUsername();
		String token2 = registerAndGetToken(username2, "password123", username2 + "@example.com");

		String updatedExpenseJson = createExpenseJson("2025-12-27", "Target", "200.00", "Chase", "Shopping");

		given()
			.header("Authorization", "Bearer " + token2)
			.contentType(ContentType.JSON)
			.body(updatedExpenseJson)
			.when()
			.put("/expenses/" + expenseId)
			.then()
			.statusCode(404); // Not found - user isolation
	}

	@Test
	public void testDeleteExpense_Success() {
		String username = generateUniqueUsername();
		String token = registerAndGetToken(username, "password123", username + "@example.com");

		// Create expense
		String expenseJson = createExpenseJson("2025-12-26", "Whole Foods", "125.50", "Chase", "Groceries");
		Response createResponse = given()
			.header("Authorization", "Bearer " + token)
			.contentType(ContentType.JSON)
			.body(expenseJson)
			.when()
			.post("/expenses")
			.then()
			.statusCode(201)
			.extract()
			.response();

		Long expenseId = createResponse.jsonPath().getLong("id");

		// Delete expense
		given()
			.header("Authorization", "Bearer " + token)
			.when()
			.delete("/expenses/" + expenseId)
			.then()
			.statusCode(204); // No content

		// Verify it's deleted
		given()
			.header("Authorization", "Bearer " + token)
			.when()
			.get("/expenses/" + expenseId)
			.then()
			.statusCode(404);
	}

	@Test
	public void testDeleteExpense_WrongUser_NotFound() {
		// Create expense with user 1
		String username1 = generateUniqueUsername();
		String token1 = registerAndGetToken(username1, "password123", username1 + "@example.com");

		String expenseJson = createExpenseJson("2025-12-26", "Whole Foods", "125.50", "Chase", "Groceries");
		Response createResponse = given()
			.header("Authorization", "Bearer " + token1)
			.contentType(ContentType.JSON)
			.body(expenseJson)
			.when()
			.post("/expenses")
			.then()
			.statusCode(201)
			.extract()
			.response();

		Long expenseId = createResponse.jsonPath().getLong("id");

		// Try to delete with user 2
		String username2 = generateUniqueUsername();
		String token2 = registerAndGetToken(username2, "password123", username2 + "@example.com");

		given()
			.header("Authorization", "Bearer " + token2)
			.when()
			.delete("/expenses/" + expenseId)
			.then()
			.statusCode(404); // Not found - user isolation
	}
}
