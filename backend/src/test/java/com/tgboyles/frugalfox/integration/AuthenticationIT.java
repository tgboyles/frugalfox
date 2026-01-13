package com.tgboyles.frugalfox.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.restassured.http.ContentType;

/**
 * Integration tests for authentication endpoints.
 *
 * <p>Tests user registration and login flows.
 */
public class AuthenticationIT extends BaseIntegrationTest {

	@Test
	public void testRegisterNewUser_Success() {
		String username = generateUniqueUsername();

		given()
			.contentType(ContentType.JSON)
			.body(String.format(
				"{\"username\":\"%s\",\"password\":\"password123\",\"email\":\"%s@example.com\"}",
				username, username))
			.when()
			.post("/auth/register")
			.then()
			.statusCode(201)
			.body("token", notNullValue())
			.body("username", equalTo(username))
			.body("email", equalTo(username + "@example.com"));
	}

	@Test
	public void testRegisterDuplicateUsername_Fails() {
		String username = generateUniqueUsername();

		// Register first user
		registerAndGetToken(username, "password123", username + "@example.com");

		// Try to register again with same username
		given()
			.contentType(ContentType.JSON)
			.body(String.format(
				"{\"username\":\"%s\",\"password\":\"password456\",\"email\":\"different@example.com\"}",
				username))
			.when()
			.post("/auth/register")
			.then()
			.statusCode(401); // BadCredentials exception returns 401
	}

	@Test
	public void testRegisterInvalidEmail_Fails() {
		String username = generateUniqueUsername();

		given()
			.contentType(ContentType.JSON)
			.body(String.format(
				"{\"username\":\"%s\",\"password\":\"password123\",\"email\":\"invalid-email\"}",
				username))
			.when()
			.post("/auth/register")
			.then()
			.statusCode(400); // Validation error
	}

	@Test
	public void testRegisterMissingPassword_Fails() {
		String username = generateUniqueUsername();

		given()
			.contentType(ContentType.JSON)
			.body(String.format("{\"username\":\"%s\",\"email\":\"%s@example.com\"}", username, username))
			.when()
			.post("/auth/register")
			.then()
			.statusCode(400); // Validation error
	}

	@Test
	public void testLoginWithValidCredentials_Success() {
		String username = generateUniqueUsername();
		String password = "password123";

		// Register user first
		registerAndGetToken(username, password, username + "@example.com");

		// Login
		given()
			.contentType(ContentType.JSON)
			.body(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password))
			.when()
			.post("/auth/login")
			.then()
			.statusCode(200)
			.body("token", notNullValue())
			.body("username", equalTo(username));
	}

	@Test
	public void testLoginWithInvalidPassword_Fails() {
		String username = generateUniqueUsername();

		// Register user
		registerAndGetToken(username, "password123", username + "@example.com");

		// Try to login with wrong password
		given()
			.contentType(ContentType.JSON)
			.body(String.format("{\"username\":\"%s\",\"password\":\"wrongpassword\"}", username))
			.when()
			.post("/auth/login")
			.then()
			.statusCode(401);
	}

	@Test
	public void testLoginWithNonexistentUser_Fails() {
		given()
			.contentType(ContentType.JSON)
			.body("{\"username\":\"nonexistent\",\"password\":\"password123\"}")
			.when()
			.post("/auth/login")
			.then()
			.statusCode(401);
	}
}
