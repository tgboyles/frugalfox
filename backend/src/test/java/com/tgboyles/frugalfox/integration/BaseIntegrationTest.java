package com.tgboyles.frugalfox.integration;

import static io.restassured.RestAssured.given;

import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.tgboyles.frugalfox.expense.ExpenseRepository;
import com.tgboyles.frugalfox.user.UserRepository;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * Base class for REST Assured integration tests.
 *
 * <p>Provides common setup including:
 * - Random port configuration for Spring Boot
 * - REST Assured base configuration
 * - Data cleanup between tests
 * - Utility methods for authentication
 *
 * <p>Tests extending this class assume the application is running
 * (can be started via Docker Compose or mvn spring-boot:run).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {

	@LocalServerPort
	protected int port;

	@Autowired
	protected UserRepository userRepository;

	@Autowired
	protected ExpenseRepository expenseRepository;

	protected String baseUrl;

	@BeforeAll
	public static void setupRestAssured() {
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
	}

	@BeforeEach
	public void setUp() {
		baseUrl = "http://localhost:" + port;
		RestAssured.baseURI = baseUrl;
		RestAssured.basePath = "";

		// Clean up test data before each test
		expenseRepository.deleteAll();
		userRepository.deleteAll();
	}

	/**
	 * Generates a unique username for testing to avoid conflicts.
	 *
	 * @return a unique username
	 */
	protected String generateUniqueUsername() {
		return "testuser_" + UUID.randomUUID().toString().substring(0, 8);
	}

	/**
	 * Registers a new user and returns the JWT token.
	 *
	 * @param username the username
	 * @param password the password
	 * @param email the email
	 * @return JWT token for the registered user
	 */
	protected String registerAndGetToken(String username, String password, String email) {
		Response response = given()
			.contentType(ContentType.JSON)
			.body(String.format(
				"{\"username\":\"%s\",\"password\":\"%s\",\"email\":\"%s\"}",
				username, password, email))
			.when()
			.post("/auth/register")
			.then()
			.statusCode(201)
			.extract()
			.response();

		return response.jsonPath().getString("token");
	}

	/**
	 * Logs in a user and returns the JWT token.
	 *
	 * @param username the username
	 * @param password the password
	 * @return JWT token for the authenticated user
	 */
	protected String loginAndGetToken(String username, String password) {
		Response response = given()
			.contentType(ContentType.JSON)
			.body(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password))
			.when()
			.post("/auth/login")
			.then()
			.statusCode(200)
			.extract()
			.response();

		return response.jsonPath().getString("token");
	}

	/**
	 * Creates an expense JSON payload for testing.
	 *
	 * @param date the expense date (YYYY-MM-DD format)
	 * @param merchant the merchant name
	 * @param amount the amount
	 * @param bank the bank name
	 * @param category the category
	 * @return JSON string representing the expense
	 */
	protected String createExpenseJson(
		String date, String merchant, String amount, String bank, String category) {
		return String.format(
			"{\"date\":\"%s\",\"merchant\":\"%s\",\"amount\":%s,\"bank\":\"%s\",\"category\":\"%s\"}",
			date, merchant, amount, bank, category);
	}
}
