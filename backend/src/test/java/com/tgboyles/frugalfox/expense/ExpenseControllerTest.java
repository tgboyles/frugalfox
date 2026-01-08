package com.tgboyles.frugalfox.expense;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgboyles.frugalfox.security.AuthRequest;
import com.tgboyles.frugalfox.user.User;
import com.tgboyles.frugalfox.user.UserRepository;

/** Integration tests for expense endpoints. */
@SpringBootTest
@Transactional
public class ExpenseControllerTest {

@Autowired private WebApplicationContext context;

private ObjectMapper objectMapper;

@Autowired private UserRepository userRepository;

@Autowired private PasswordEncoder passwordEncoder;

@Autowired private ExpenseRepository expenseRepository;

private MockMvc mvc;
private String authToken;
private User testUser;

@BeforeEach
public void setup() throws Exception {
	mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
	objectMapper = new ObjectMapper();
	objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

	// Create test user
	testUser = new User();
	testUser.setUsername("testuser");
	testUser.setPassword(passwordEncoder.encode("password123"));
	testUser.setEmail("test@example.com");
	testUser.setEnabled(true);
	testUser = userRepository.save(testUser);

	// Login to get auth token
	AuthRequest loginRequest = new AuthRequest();
	loginRequest.setUsername("testuser");
	loginRequest.setPassword("password123");

	MvcResult result =
		mvc.perform(
				post("/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(loginRequest)))
			.andExpect(status().isOk())
			.andReturn();

	String responseBody = result.getResponse().getContentAsString();
	authToken =
		objectMapper.readTree(responseBody).get("token").asText();
}

@Test
public void testCreateExpenseSuccess() throws Exception {
	Expense expense = new Expense();
	expense.setDate(LocalDate.of(2025, 12, 26));
	expense.setMerchant("Whole Foods");
	expense.setAmount(new BigDecimal("125.50"));
	expense.setBank("Chase");
	expense.setCategory("Groceries");

	mvc.perform(
			post("/expenses")
				.header("Authorization", "Bearer " + authToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(expense)))
		.andExpect(status().isCreated())
		.andExpect(jsonPath("$.id").exists())
		.andExpect(jsonPath("$.merchant").value("Whole Foods"))
		.andExpect(jsonPath("$.amount").value(125.50))
		.andExpect(jsonPath("$.bank").value("Chase"))
		.andExpect(jsonPath("$.category").value("Groceries"))
		.andExpect(jsonPath("$.createdAt").exists())
		.andExpect(jsonPath("$.updatedAt").exists());
}

@Test
public void testCreateExpenseValidationErrors() throws Exception {
	// Negative amount
	Expense expense = new Expense();
	expense.setDate(LocalDate.of(2025, 12, 26));
	expense.setMerchant("Test Store");
	expense.setAmount(new BigDecimal("-50.00"));
	expense.setBank("Chase");
	expense.setCategory("Test");

	mvc.perform(
			post("/expenses")
				.header("Authorization", "Bearer " + authToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(expense)))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.message").value("Validation failed"))
		.andExpect(jsonPath("$.errors[0].field").value("amount"));
}

@Test
public void testCreateExpenseFutureDate() throws Exception {
	Expense expense = new Expense();
	expense.setDate(LocalDate.of(2026, 12, 26));
	expense.setMerchant("Test Store");
	expense.setAmount(new BigDecimal("50.00"));
	expense.setBank("Chase");
	expense.setCategory("Test");

	mvc.perform(
			post("/expenses")
				.header("Authorization", "Bearer " + authToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(expense)))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.errors[0].field").value("date"));
}

@Test
public void testCreateExpenseUnauthorized() throws Exception {
	Expense expense = new Expense();
	expense.setDate(LocalDate.of(2025, 12, 26));
	expense.setMerchant("Whole Foods");
	expense.setAmount(new BigDecimal("125.50"));
	expense.setBank("Chase");
	expense.setCategory("Groceries");

	mvc.perform(
			post("/expenses")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(expense)))
		.andExpect(status().isForbidden());
}

@Test
public void testGetExpenseById() throws Exception {
	// Create expense
	Expense expense = createTestExpense("Target", new BigDecimal("75.00"), "Shopping");

	mvc.perform(
			get("/expenses/" + expense.getId())
				.header("Authorization", "Bearer " + authToken))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").value(expense.getId()))
		.andExpect(jsonPath("$.merchant").value("Target"))
		.andExpect(jsonPath("$.amount").value(75.00))
		.andExpect(jsonPath("$.category").value("Shopping"));
}

@Test
public void testGetExpenseByIdNotFound() throws Exception {
	mvc.perform(
			get("/expenses/99999")
				.header("Authorization", "Bearer " + authToken))
		.andExpect(status().isNotFound())
		.andExpect(jsonPath("$.message").value("Expense not found with id: 99999"));
}

@Test
public void testUpdateExpense() throws Exception {
	// Create expense
	Expense expense = createTestExpense("Whole Foods", new BigDecimal("125.50"), "Groceries");

	// Update it
	expense.setMerchant("Whole Foods Market");
	expense.setAmount(new BigDecimal("130.00"));

	mvc.perform(
			put("/expenses/" + expense.getId())
				.header("Authorization", "Bearer " + authToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(expense)))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.merchant").value("Whole Foods Market"))
		.andExpect(jsonPath("$.amount").value(130.00));
}

@Test
public void testDeleteExpense() throws Exception {
	// Create expense
	Expense expense = createTestExpense("Starbucks", new BigDecimal("12.50"), "Food");

	mvc.perform(
			delete("/expenses/" + expense.getId())
				.header("Authorization", "Bearer " + authToken))
		.andExpect(status().isNoContent());

	// Verify it's deleted
	mvc.perform(
			get("/expenses/" + expense.getId())
				.header("Authorization", "Bearer " + authToken))
		.andExpect(status().isNotFound());
}

@Test
public void testListAllExpenses() throws Exception {
	// Create multiple expenses
	createTestExpense("Whole Foods", new BigDecimal("125.50"), "Groceries");
	createTestExpense("Target", new BigDecimal("75.00"), "Shopping");
	createTestExpense("Starbucks", new BigDecimal("12.50"), "Food");

	mvc.perform(
			get("/expenses")
				.header("Authorization", "Bearer " + authToken))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.content", hasSize(3)))
		.andExpect(jsonPath("$.totalElements").value(3))
		.andExpect(jsonPath("$.totalPages").value(1));
}

@Test
public void testFilterByCategory() throws Exception {
	createTestExpense("Whole Foods", new BigDecimal("125.50"), "Groceries");
	createTestExpense("Target", new BigDecimal("75.00"), "Shopping");
	createTestExpense("Trader Joes", new BigDecimal("85.00"), "Groceries");

	mvc.perform(
			get("/expenses")
				.param("category", "Groceries")
				.header("Authorization", "Bearer " + authToken))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.content", hasSize(2)))
		.andExpect(jsonPath("$.content[0].category").value("Groceries"))
		.andExpect(jsonPath("$.content[1].category").value("Groceries"));
}

@Test
public void testFilterByMerchantPartial() throws Exception {
	createTestExpense("Whole Foods", new BigDecimal("125.50"), "Groceries");
	createTestExpense("Whole Foods Market", new BigDecimal("85.00"), "Groceries");
	createTestExpense("Target", new BigDecimal("75.00"), "Shopping");

	mvc.perform(
			get("/expenses")
				.param("merchant", "whole")
				.header("Authorization", "Bearer " + authToken))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.content", hasSize(2)));
}

@Test
public void testFilterByBank() throws Exception {
	createTestExpenseWithBank("Store1", new BigDecimal("50.00"), "Shopping", "Chase");
	createTestExpenseWithBank("Store2", new BigDecimal("60.00"), "Shopping", "Bank of America");
	createTestExpenseWithBank("Store3", new BigDecimal("70.00"), "Shopping", "Chase");

	mvc.perform(
			get("/expenses")
				.param("bank", "Chase")
				.header("Authorization", "Bearer " + authToken))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.content", hasSize(2)))
		.andExpect(jsonPath("$.content[0].bank").value("Chase"))
		.andExpect(jsonPath("$.content[1].bank").value("Chase"));
}

@Test
public void testFilterByDateRange() throws Exception {
	createTestExpenseWithDate("Store1", new BigDecimal("50.00"), "Shopping", LocalDate.of(2025, 12, 24));
	createTestExpenseWithDate("Store2", new BigDecimal("60.00"), "Shopping", LocalDate.of(2025, 12, 25));
	createTestExpenseWithDate("Store3", new BigDecimal("70.00"), "Shopping", LocalDate.of(2025, 12, 26));

	mvc.perform(
			get("/expenses")
				.param("startDate", "2025-12-25")
				.param("endDate", "2025-12-26")
				.header("Authorization", "Bearer " + authToken))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.content", hasSize(2)));
}

@Test
public void testFilterByAmountRange() throws Exception {
	createTestExpense("Store1", new BigDecimal("30.00"), "Shopping");
	createTestExpense("Store2", new BigDecimal("75.00"), "Shopping");
	createTestExpense("Store3", new BigDecimal("150.00"), "Shopping");

	mvc.perform(
			get("/expenses")
				.param("minAmount", "50")
				.param("maxAmount", "100")
				.header("Authorization", "Bearer " + authToken))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.content", hasSize(1)))
		.andExpect(jsonPath("$.content[0].amount").value(75.00));
}

@Test
public void testCombinedFilters() throws Exception {
	createTestExpenseWithBankAndDate("Whole Foods", new BigDecimal("125.50"), "Groceries", "Chase", LocalDate.of(2025, 12, 26));
	createTestExpenseWithBankAndDate("Target", new BigDecimal("75.00"), "Shopping", "Chase", LocalDate.of(2025, 12, 26));
	createTestExpenseWithBankAndDate("Trader Joes", new BigDecimal("85.00"), "Groceries", "Bank of America", LocalDate.of(2025, 12, 26));

	mvc.perform(
			get("/expenses")
				.param("category", "Groceries")
				.param("bank", "Chase")
				.header("Authorization", "Bearer " + authToken))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.content", hasSize(1)))
		.andExpect(jsonPath("$.content[0].merchant").value("Whole Foods"));
}

@Test
public void testPaginationAndSorting() throws Exception {
	createTestExpenseWithDate("Store1", new BigDecimal("30.00"), "Shopping", LocalDate.of(2025, 12, 24));
	createTestExpenseWithDate("Store2", new BigDecimal("60.00"), "Shopping", LocalDate.of(2025, 12, 25));
	createTestExpenseWithDate("Store3", new BigDecimal("90.00"), "Shopping", LocalDate.of(2025, 12, 26));

	mvc.perform(
			get("/expenses")
				.param("page", "0")
				.param("size", "2")
				.param("sort", "amount,desc")
				.header("Authorization", "Bearer " + authToken))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.content", hasSize(2)))
		.andExpect(jsonPath("$.content[0].amount").value(90.00))
		.andExpect(jsonPath("$.content[1].amount").value(60.00))
		.andExpect(jsonPath("$.totalElements").value(3))
		.andExpect(jsonPath("$.totalPages").value(2));
}

@Test
public void testUserIsolation() throws Exception {
	// Create expense for testUser
	createTestExpense("User1 Store", new BigDecimal("100.00"), "Shopping");

	// Create second user
	User user2 = new User();
	user2.setUsername("testuser2");
	user2.setPassword(passwordEncoder.encode("password123"));
	user2.setEmail("test2@example.com");
	user2.setEnabled(true);
	user2 = userRepository.save(user2);

	// Create expense for user2
	Expense expense2 = new Expense();
	expense2.setDate(LocalDate.of(2025, 12, 26));
	expense2.setMerchant("User2 Store");
	expense2.setAmount(new BigDecimal("200.00"));
	expense2.setBank("Chase");
	expense2.setCategory("Shopping");
	expense2.setUser(user2);
	expenseRepository.save(expense2);

	// testUser should only see their own expense
	mvc.perform(
			get("/expenses")
				.header("Authorization", "Bearer " + authToken))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.content", hasSize(1)))
		.andExpect(jsonPath("$.content[0].merchant").value("User1 Store"));
}

@Test
public void testCannotAccessOtherUsersExpense() throws Exception {
	// Create second user
	User user2 = new User();
	user2.setUsername("testuser2");
	user2.setPassword(passwordEncoder.encode("password123"));
	user2.setEmail("test2@example.com");
	user2.setEnabled(true);
	user2 = userRepository.save(user2);

	// Create expense for user2
	Expense expense2 = new Expense();
	expense2.setDate(LocalDate.of(2025, 12, 26));
	expense2.setMerchant("User2 Store");
	expense2.setAmount(new BigDecimal("200.00"));
	expense2.setBank("Chase");
	expense2.setCategory("Shopping");
	expense2.setUser(user2);
	expense2 = expenseRepository.save(expense2);

	// testUser tries to access user2's expense
	mvc.perform(
			get("/expenses/" + expense2.getId())
				.header("Authorization", "Bearer " + authToken))
		.andExpect(status().isNotFound());
}

// Helper methods

private Expense createTestExpense(String merchant, BigDecimal amount, String category) {
	return createTestExpenseWithBankAndDate(merchant, amount, category, "Chase", LocalDate.of(2025, 12, 26));
}

private Expense createTestExpenseWithBank(String merchant, BigDecimal amount, String category, String bank) {
	return createTestExpenseWithBankAndDate(merchant, amount, category, bank, LocalDate.of(2025, 12, 26));
}

private Expense createTestExpenseWithDate(String merchant, BigDecimal amount, String category, LocalDate date) {
	return createTestExpenseWithBankAndDate(merchant, amount, category, "Chase", date);
}

private Expense createTestExpenseWithBankAndDate(String merchant, BigDecimal amount, String category, String bank, LocalDate date) {
	Expense expense = new Expense();
	expense.setDate(date);
	expense.setMerchant(merchant);
	expense.setAmount(amount);
	expense.setBank(bank);
	expense.setCategory(category);
	expense.setUser(testUser);
	return expenseRepository.save(expense);
}
}
