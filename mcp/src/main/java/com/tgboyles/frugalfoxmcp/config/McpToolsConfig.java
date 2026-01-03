package com.tgboyles.frugalfoxmcp.config;

import com.tgboyles.frugalfoxmcp.dto.AuthResponse;
import com.tgboyles.frugalfoxmcp.dto.ExpenseRequest;
import com.tgboyles.frugalfoxmcp.dto.ExpenseResponse;
import com.tgboyles.frugalfoxmcp.service.CredentialsHolder;
import com.tgboyles.frugalfoxmcp.service.FrugalFoxApiClient;
import com.tgboyles.frugalfoxmcp.service.TokenManager;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
public class McpToolsConfig {

    private final FrugalFoxApiClient apiClient;
    private final TokenManager tokenManager;
    private final CredentialsHolder credentialsHolder;

    public McpToolsConfig(FrugalFoxApiClient apiClient, TokenManager tokenManager, CredentialsHolder credentialsHolder) {
        this.apiClient = apiClient;
        this.tokenManager = tokenManager;
        this.credentialsHolder = credentialsHolder;
    }

    /**
     * Get a valid token, automatically refreshing if expired.
     * Requires credentials to have been provided via SSE query parameters.
     */
    private String getValidToken() {
        if (!credentialsHolder.hasCredentials()) {
            throw new IllegalStateException("No credentials available. Please provide username and password in SSE connection URL.");
        }
        return tokenManager.getValidToken(credentialsHolder.getUsername(), credentialsHolder.getPassword());
    }

    @McpTool(name = "registerUser", description = "Register a new user account. Returns a JWT token that must be used for all subsequent expense operations.")
    public Map<String, Object> registerUser(
            @McpToolParam(description = "Username for the new account", required = true) String username,
            @McpToolParam(description = "Password for the new account", required = true) String password,
            @McpToolParam(description = "Email address for the new account", required = true) String email) {
        try {
            AuthResponse response = apiClient.register(username, password, email);
            return Map.of("success", true, "token", response.token(), "username", response.username(), "email", response.email());
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @McpTool(name = "loginUser", description = "Login with existing credentials. Returns a JWT token that must be used for all subsequent expense operations.")
    public Map<String, Object> loginUser(
            @McpToolParam(description = "Username", required = true) String username,
            @McpToolParam(description = "Password", required = true) String password) {
        try {
            AuthResponse response = apiClient.login(username, password);
            return Map.of("success", true, "token", response.token(), "username", response.username(), "email", response.email());
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @McpTool(name = "healthCheck", description = "Check if the Frugal Fox API is running and healthy.")
    public Map<String, Object> healthCheck() {
        try {
            String health = apiClient.healthCheck();
            return Map.of("success", true, "status", health);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @McpTool(name = "createExpense", description = "Create a new expense. Automatically uses JWT token from credentials. Date must be in ISO 8601 format (YYYY-MM-DD) and cannot be in the future. Amount must be positive.")
    public Map<String, Object> createExpense(
            @McpToolParam(description = "Expense date in YYYY-MM-DD format", required = true) String date,
            @McpToolParam(description = "Merchant name", required = true) String merchant,
            @McpToolParam(description = "Expense amount (positive number)", required = true) String amount,
            @McpToolParam(description = "Bank name", required = true) String bank,
            @McpToolParam(description = "Expense category", required = true) String category) {
        try {
            String token = getValidToken();
            ExpenseRequest expReq = new ExpenseRequest(
                LocalDate.parse(date),
                merchant,
                new BigDecimal(amount),
                bank,
                category
            );
            ExpenseResponse response = apiClient.createExpense(token, expReq);
            return Map.of("success", true, "expense", buildExpenseMap(response));
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @McpTool(name = "getExpense", description = "Get a specific expense by ID. Automatically uses JWT token from credentials. Only returns expenses owned by the authenticated user.")
    public Map<String, Object> getExpense(
            @McpToolParam(description = "Expense ID", required = true) Long id) {
        try {
            String token = getValidToken();
            ExpenseResponse response = apiClient.getExpense(token, id);
            return Map.of("success", true, "expense", buildExpenseMap(response));
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @McpTool(name = "updateExpense", description = "Update an existing expense. Automatically uses JWT token from credentials. All fields must be provided. Only updates expenses owned by the authenticated user.")
    public Map<String, Object> updateExpense(
            @McpToolParam(description = "Expense ID to update", required = true) Long id,
            @McpToolParam(description = "Expense date in YYYY-MM-DD format", required = true) String date,
            @McpToolParam(description = "Merchant name", required = true) String merchant,
            @McpToolParam(description = "Expense amount (positive number)", required = true) String amount,
            @McpToolParam(description = "Bank name", required = true) String bank,
            @McpToolParam(description = "Expense category", required = true) String category) {
        try {
            String token = getValidToken();
            ExpenseRequest expReq = new ExpenseRequest(
                LocalDate.parse(date),
                merchant,
                new BigDecimal(amount),
                bank,
                category
            );
            ExpenseResponse response = apiClient.updateExpense(token, id, expReq);
            return Map.of("success", true, "expense", buildExpenseMap(response));
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @McpTool(name = "deleteExpense", description = "Delete an expense by ID. Automatically uses JWT token from credentials. Only deletes expenses owned by the authenticated user.")
    public Map<String, Object> deleteExpense(
            @McpToolParam(description = "Expense ID to delete", required = true) Long id) {
        try {
            String token = getValidToken();
            apiClient.deleteExpense(token, id);
            return Map.of("success", true, "message", "Expense deleted successfully");
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @McpTool(name = "searchExpenses", description = "Search and filter expenses with pagination. Automatically uses JWT token from credentials. All filter parameters are optional. Returns paginated results (default 20 per page). Category and bank use exact match. Merchant uses partial, case-insensitive search.")
    public Map<String, Object> searchExpenses(
            @McpToolParam(description = "Category to filter by (exact match)", required = false) String category,
            @McpToolParam(description = "Bank to filter by (exact match)", required = false) String bank,
            @McpToolParam(description = "Merchant to filter by (partial, case-insensitive)", required = false) String merchant,
            @McpToolParam(description = "Start date for date range (YYYY-MM-DD)", required = false) String startDate,
            @McpToolParam(description = "End date for date range (YYYY-MM-DD)", required = false) String endDate,
            @McpToolParam(description = "Minimum amount", required = false) String minAmount,
            @McpToolParam(description = "Maximum amount", required = false) String maxAmount,
            @McpToolParam(description = "Page number (0-indexed)", required = false) Integer page,
            @McpToolParam(description = "Page size (default 20)", required = false) Integer size,
            @McpToolParam(description = "Sort field and direction (e.g., 'date,desc')", required = false) String sort) {
        try {
            String token = getValidToken();
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
            BigDecimal min = minAmount != null ? new BigDecimal(minAmount) : null;
            BigDecimal max = maxAmount != null ? new BigDecimal(maxAmount) : null;

            Map<String, Object> response = apiClient.searchExpenses(
                token, category, bank, merchant, start, end, min, max, page, size, sort
            );
            return Map.of("success", true, "data", response);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @McpTool(name = "listAllExpenses", description = "List all expenses for the authenticated user with pagination. Automatically uses JWT token from credentials. Returns 20 expenses per page by default, sorted by date descending.")
    public Map<String, Object> listAllExpenses(
            @McpToolParam(description = "Page number (0-indexed)", required = false) Integer page,
            @McpToolParam(description = "Page size (default 20)", required = false) Integer size) {
        try {
            String token = getValidToken();
            Map<String, Object> response = apiClient.searchExpenses(
                token, null, null, null, null, null, null, null, page, size, null
            );
            return Map.of("success", true, "data", response);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    private static Map<String, Object> buildExpenseMap(ExpenseResponse response) {
        Map<String, Object> expense = new HashMap<>();
        expense.put("id", response.id());
        expense.put("date", response.date().toString());
        expense.put("merchant", response.merchant());
        expense.put("amount", response.amount().toString());
        expense.put("bank", response.bank());
        expense.put("category", response.category());
        expense.put("createdAt", response.createdAt().toString());
        expense.put("updatedAt", response.updatedAt().toString());
        return expense;
    }
}
