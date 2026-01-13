package com.tgboyles.frugalfox.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import io.restassured.http.ContentType;

/**
 * Integration tests for CSV import and export functionality.
 *
 * <p>Tests importing expenses from CSV files and exporting to CSV format.
 */
public class CsvImportExportIT extends BaseIntegrationTest {

	@Test
	public void testImportValidCsvFile_Success() throws IOException {
		String username = generateUniqueUsername();
		String token = registerAndGetToken(username, "password123", username + "@example.com");

		File csvFile = createCsvFile("valid_import.csv",
			"date,merchant,amount,bank,category\n" +
			"2025-12-26,Whole Foods,125.50,Chase,Groceries\n" +
			"2025-12-27,Target,200.00,Amex,Shopping\n" +
			"2025-12-28,Shell,45.00,Chase,Gas\n");

		given()
			.header("Authorization", "Bearer " + token)
			.multiPart("file", csvFile, "text/csv")
			.when()
			.post("/expenses/import")
			.then()
			.statusCode(200)
			.body("totalRows", equalTo(3))
			.body("successfulRows", equalTo(3))
			.body("failedRows", equalTo(0));

		// Verify expenses were created
		given()
			.header("Authorization", "Bearer " + token)
			.when()
			.get("/expenses")
			.then()
			.statusCode(200)
			.body("totalElements", equalTo(3));

		csvFile.delete();
	}

	@Test
	public void testImportCsvWithValidationErrors_PartialSuccess() throws IOException {
		String username = generateUniqueUsername();
		String token = registerAndGetToken(username, "password123", username + "@example.com");

		File csvFile = createCsvFile("partial_import.csv",
			"date,merchant,amount,bank,category\n" +
			"2025-12-26,Whole Foods,125.50,Chase,Groceries\n" +
			"invalid-date,Target,200.00,Amex,Shopping\n" +  // Invalid date
			"2025-12-28,Shell,45.00,Chase,Gas\n");

		given()
			.header("Authorization", "Bearer " + token)
			.multiPart("file", csvFile, "text/csv")
			.when()
			.post("/expenses/import")
			.then()
			.statusCode(200)
			.body("totalRows", equalTo(3))
			.body("successfulRows", equalTo(2))
			.body("failedRows", equalTo(1));

		csvFile.delete();
	}

	@Test
	public void testImportEmptyFile_Fails() throws IOException {
		String username = generateUniqueUsername();
		String token = registerAndGetToken(username, "password123", username + "@example.com");

		File csvFile = createCsvFile("empty.csv", "");

		given()
			.header("Authorization", "Bearer " + token)
			.multiPart("file", csvFile, "text/csv")
			.when()
			.post("/expenses/import")
			.then()
			.statusCode(400); // Bad request

		csvFile.delete();
	}

	@Test
	public void testImportWithoutAuth_Fails() throws IOException {
		File csvFile = createCsvFile("no_auth.csv",
			"date,merchant,amount,bank,category\n" +
			"2025-12-26,Whole Foods,125.50,Chase,Groceries\n");

		given()
			.multiPart("file", csvFile, "text/csv")
			.when()
			.post("/expenses/import")
			.then()
			.statusCode(401); // Unauthorized

		csvFile.delete();
	}

	@Test
	public void testImportInvalidContentType_Fails() throws IOException {
		String username = generateUniqueUsername();
		String token = registerAndGetToken(username, "password123", username + "@example.com");

		File csvFile = createCsvFile("wrong_type.txt",
			"date,merchant,amount,bank,category\n" +
			"2025-12-26,Whole Foods,125.50,Chase,Groceries\n");

		given()
			.header("Authorization", "Bearer " + token)
			.multiPart("file", csvFile, "text/plain")
			.when()
			.post("/expenses/import")
			.then()
			.statusCode(400); // Invalid file type

		csvFile.delete();
	}

	@Test
	public void testExportExpensesToCsv_Success() throws IOException {
		String username = generateUniqueUsername();
		String token = registerAndGetToken(username, "password123", username + "@example.com");

		// Create some expenses
		createExpense(token, "2025-12-26", "Whole Foods", "125.50", "Chase", "Groceries");
		createExpense(token, "2025-12-27", "Target", "200.00", "Amex", "Shopping");
		createExpense(token, "2025-12-28", "Shell", "45.00", "Chase", "Gas");

		String csvContent = given()
			.header("Authorization", "Bearer " + token)
			.when()
			.get("/expenses/export")
			.then()
			.statusCode(200)
			.header("Content-Type", containsString("text/csv"))
			.header("Content-Disposition", containsString("attachment; filename=\"expenses.csv\""))
			.extract()
			.asString();

		// Verify CSV content contains headers and data
		assert csvContent.contains("date,merchant,amount,bank,category");
		assert csvContent.contains("Whole Foods");
		assert csvContent.contains("Target");
		assert csvContent.contains("Shell");
	}

	@Test
	public void testExportWithFilters_Success() throws IOException {
		String username = generateUniqueUsername();
		String token = registerAndGetToken(username, "password123", username + "@example.com");

		// Create expenses with different categories
		createExpense(token, "2025-12-26", "Whole Foods", "125.50", "Chase", "Groceries");
		createExpense(token, "2025-12-27", "Target", "200.00", "Amex", "Shopping");
		createExpense(token, "2025-12-28", "Shell", "45.00", "Chase", "Gas");

		String csvContent = given()
			.header("Authorization", "Bearer " + token)
			.queryParam("category", "Groceries")
			.when()
			.get("/expenses/export")
			.then()
			.statusCode(200)
			.extract()
			.asString();

		// Verify only groceries are exported
		assert csvContent.contains("Whole Foods");
		assert !csvContent.contains("Target");
		assert !csvContent.contains("Shell");
	}

	@Test
	public void testExportEmptyResult_Success() {
		String username = generateUniqueUsername();
		String token = registerAndGetToken(username, "password123", username + "@example.com");

		String csvContent = given()
			.header("Authorization", "Bearer " + token)
			.when()
			.get("/expenses/export")
			.then()
			.statusCode(200)
			.extract()
			.asString();

		// Should only contain headers
		assert csvContent.equals("date,merchant,amount,bank,category\n");
	}

	@Test
	public void testExportWithoutAuth_Fails() {
		given()
			.when()
			.get("/expenses/export")
			.then()
			.statusCode(401); // Unauthorized
	}

	@Test
	public void testExport_UserIsolation() throws IOException {
		// Create expenses for user 1
		String username1 = generateUniqueUsername();
		String token1 = registerAndGetToken(username1, "password123", username1 + "@example.com");
		createExpense(token1, "2025-12-26", "User1 Store", "100.00", "Chase", "Shopping");

		// Create expenses for user 2
		String username2 = generateUniqueUsername();
		String token2 = registerAndGetToken(username2, "password123", username2 + "@example.com");
		createExpense(token2, "2025-12-26", "User2 Store", "150.00", "Amex", "Shopping");

		// User 1 export should only contain their data
		String csvContent1 = given()
			.header("Authorization", "Bearer " + token1)
			.when()
			.get("/expenses/export")
			.then()
			.statusCode(200)
			.extract()
			.asString();

		assert csvContent1.contains("User1 Store");
		assert !csvContent1.contains("User2 Store");

		// User 2 export should only contain their data
		String csvContent2 = given()
			.header("Authorization", "Bearer " + token2)
			.when()
			.get("/expenses/export")
			.then()
			.statusCode(200)
			.extract()
			.asString();

		assert csvContent2.contains("User2 Store");
		assert !csvContent2.contains("User1 Store");
	}

	/**
	 * Helper method to create a temporary CSV file for testing.
	 */
	private File createCsvFile(String filename, String content) throws IOException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
		File csvFile = new File(tmpDir, filename);
		try (FileWriter writer = new FileWriter(csvFile)) {
			writer.write(content);
		}
		return csvFile;
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
