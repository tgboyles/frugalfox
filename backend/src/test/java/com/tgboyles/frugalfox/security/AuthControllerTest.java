package com.tgboyles.frugalfox.security;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/** Integration tests for authentication endpoints. */
@SpringBootTest
@Transactional
public class AuthControllerTest {

  @Autowired private WebApplicationContext context;

  private MockMvc mvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  public void setup() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  @Test
  public void testRegisterSuccess() throws Exception {
    RegisterRequest request = new RegisterRequest();
    request.setUsername("newuser");
    request.setPassword("password123");
    request.setEmail("newuser@example.com");

    mvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token", notNullValue()))
        .andExpect(jsonPath("$.username").value("newuser"))
        .andExpect(jsonPath("$.email").value("newuser@example.com"));
  }

  @Test
  @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
  public void testRegisterDuplicateUsername() throws Exception {
    // Register first user
    RegisterRequest firstRequest = new RegisterRequest();
    firstRequest.setUsername("duplicate");
    firstRequest.setPassword("password123");
    firstRequest.setEmail("first@example.com");

    mvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
        .andExpect(status().isCreated());

    // Try to register with same username
    RegisterRequest secondRequest = new RegisterRequest();
    secondRequest.setUsername("duplicate");
    secondRequest.setPassword("password456");
    secondRequest.setEmail("second@example.com");

    mvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Username already exists: duplicate"));
  }

  @Test
  public void testRegisterValidationErrors() throws Exception {
    // Empty username
    RegisterRequest request = new RegisterRequest();
    request.setUsername("");
    request.setPassword("password123");
    request.setEmail("test@example.com");

    mvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testLoginSuccess() throws Exception {
    // First register a user
    RegisterRequest registerRequest = new RegisterRequest();
    registerRequest.setUsername("loginuser");
    registerRequest.setPassword("password123");
    registerRequest.setEmail("loginuser@example.com");

    mvc.perform(
        post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)));

    // Now login
    AuthRequest loginRequest = new AuthRequest();
    loginRequest.setUsername("loginuser");
    loginRequest.setPassword("password123");

    mvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token", notNullValue()))
        .andExpect(jsonPath("$.username").value("loginuser"))
        .andExpect(jsonPath("$.email").value("loginuser@example.com"));
  }

  @Test
  public void testLoginInvalidCredentials() throws Exception {
    // First register a user
    RegisterRequest registerRequest = new RegisterRequest();
    registerRequest.setUsername("loginuser2");
    registerRequest.setPassword("correctpassword");
    registerRequest.setEmail("loginuser2@example.com");

    mvc.perform(
        post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)));

    // Try to login with wrong password
    AuthRequest loginRequest = new AuthRequest();
    loginRequest.setUsername("loginuser2");
    loginRequest.setPassword("wrongpassword");

    mvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Invalid username or password"));
  }

  @Test
  public void testLoginNonexistentUser() throws Exception {
    AuthRequest loginRequest = new AuthRequest();
    loginRequest.setUsername("nonexistent");
    loginRequest.setPassword("password123");

    mvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Invalid username or password"));
  }
}
