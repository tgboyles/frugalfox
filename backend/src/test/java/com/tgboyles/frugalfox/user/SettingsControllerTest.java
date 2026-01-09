package com.tgboyles.frugalfox.user;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tgboyles.frugalfox.security.RegisterRequest;

/** Integration tests for settings endpoints. */
@SpringBootTest
@Transactional
public class SettingsControllerTest {

	@Autowired private WebApplicationContext context;

	private MockMvc mvc;
	private ObjectMapper objectMapper;

	@BeforeEach
	public void setup() {
		mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
	}

	private String registerAndGetToken(String username, String password, String email)
		throws Exception {
		RegisterRequest registerRequest = new RegisterRequest();
		registerRequest.setUsername(username);
		registerRequest.setPassword(password);
		registerRequest.setEmail(email);

		MvcResult result =
			mvc.perform(
					post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registerRequest)))
				.andExpect(status().isCreated())
				.andReturn();

		String responseBody = result.getResponse().getContentAsString();
		JsonNode jsonNode = objectMapper.readTree(responseBody);
		return jsonNode.get("token").asText();
	}

	@Test
	public void testUpdateEmailSuccess() throws Exception {
		String token = registerAndGetToken("testuser", "password123", "old@example.com");

		UpdateEmailRequest request = new UpdateEmailRequest();
		request.setEmail("new@example.com");

		mvc.perform(
				put("/settings/email")
					.header("Authorization", "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("new@example.com"))
			.andExpect(jsonPath("$.username").value("testuser"))
			.andExpect(jsonPath("$.id", notNullValue()))
			.andExpect(jsonPath("$.updatedAt", notNullValue()));
	}

	@Test
	public void testUpdateEmailDuplicateEmail() throws Exception {
		// Register two users
		registerAndGetToken("user1", "password123", "user1@example.com");
		String token2 = registerAndGetToken("user2", "password123", "user2@example.com");

		// Try to update user2's email to user1's email
		UpdateEmailRequest request = new UpdateEmailRequest();
		request.setEmail("user1@example.com");

		mvc.perform(
				put("/settings/email")
					.header("Authorization", "Bearer " + token2)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Email already in use: user1@example.com"));
	}

	@Test
	public void testUpdateEmailInvalidEmail() throws Exception {
		String token = registerAndGetToken("testuser2", "password123", "test2@example.com");

		UpdateEmailRequest request = new UpdateEmailRequest();
		request.setEmail("invalid-email");

		mvc.perform(
				put("/settings/email")
					.header("Authorization", "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Validation failed"));
	}

	@Test
	public void testUpdateEmailUnauthorized() throws Exception {
		UpdateEmailRequest request = new UpdateEmailRequest();
		request.setEmail("new@example.com");

		mvc.perform(
				put("/settings/email")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden());
	}

	@Test
	public void testUpdatePasswordSuccess() throws Exception {
		String token = registerAndGetToken("testuser3", "password123", "test3@example.com");

		UpdatePasswordRequest request = new UpdatePasswordRequest();
		request.setCurrentPassword("password123");
		request.setNewPassword("newpassword123");

		mvc.perform(
				put("/settings/password")
					.header("Authorization", "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Password updated successfully"));
	}

	@Test
	public void testUpdatePasswordIncorrectCurrentPassword() throws Exception {
		String token = registerAndGetToken("testuser4", "password123", "test4@example.com");

		UpdatePasswordRequest request = new UpdatePasswordRequest();
		request.setCurrentPassword("wrongpassword");
		request.setNewPassword("newpassword123");

		mvc.perform(
				put("/settings/password")
					.header("Authorization", "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Current password is incorrect"));
	}

	@Test
	public void testUpdatePasswordTooShort() throws Exception {
		String token = registerAndGetToken("testuser5", "password123", "test5@example.com");

		UpdatePasswordRequest request = new UpdatePasswordRequest();
		request.setCurrentPassword("password123");
		request.setNewPassword("short");

		mvc.perform(
				put("/settings/password")
					.header("Authorization", "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Validation failed"));
	}

	@Test
	public void testUpdatePasswordUnauthorized() throws Exception {
		UpdatePasswordRequest request = new UpdatePasswordRequest();
		request.setCurrentPassword("password123");
		request.setNewPassword("newpassword123");

		mvc.perform(
				put("/settings/password")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden());
	}
}
