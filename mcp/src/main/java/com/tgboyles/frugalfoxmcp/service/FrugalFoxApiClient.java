package com.tgboyles.frugalfoxmcp.service;

import com.tgboyles.frugalfoxmcp.config.FrugalFoxApiConfig;
import com.tgboyles.frugalfoxmcp.dto.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class FrugalFoxApiClient {

    private final WebClient webClient;
    private final FrugalFoxApiConfig config;

    public FrugalFoxApiClient(FrugalFoxApiConfig config) {
        this.config = config;
        this.webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // Authentication methods
    public AuthResponse register(String username, String password, String email) {
        RegisterRequest request = new RegisterRequest(username, password, email);
        return webClient.post()
                .uri("/auth/register")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .timeout(Duration.ofMillis(config.getTimeout()))
                .block();
    }

    public AuthResponse login(String username, String password) {
        AuthRequest request = new AuthRequest(username, password);
        return webClient.post()
                .uri("/auth/login")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .timeout(Duration.ofMillis(config.getTimeout()))
                .block();
    }

    // Expense CRUD methods
    public ExpenseResponse createExpense(String token, ExpenseRequest expenseRequest) {
        return webClient.post()
                .uri("/expenses")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(expenseRequest)
                .retrieve()
                .bodyToMono(ExpenseResponse.class)
                .timeout(Duration.ofMillis(config.getTimeout()))
                .block();
    }

    public ExpenseResponse getExpense(String token, Long id) {
        return webClient.get()
                .uri("/expenses/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(ExpenseResponse.class)
                .timeout(Duration.ofMillis(config.getTimeout()))
                .block();
    }

    public ExpenseResponse updateExpense(String token, Long id, ExpenseRequest expenseRequest) {
        return webClient.put()
                .uri("/expenses/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(expenseRequest)
                .retrieve()
                .bodyToMono(ExpenseResponse.class)
                .timeout(Duration.ofMillis(config.getTimeout()))
                .block();
    }

    public void deleteExpense(String token, Long id) {
        webClient.delete()
                .uri("/expenses/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofMillis(config.getTimeout()))
                .block();
    }

    // Expense search and filter
    public Map<String, Object> searchExpenses(
            String token,
            String category,
            String bank,
            String merchant,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Integer page,
            Integer size,
            String sort
    ) {
        Map<String, String> params = new HashMap<>();
        if (category != null) params.put("category", category);
        if (bank != null) params.put("bank", bank);
        if (merchant != null) params.put("merchant", merchant);
        if (startDate != null) params.put("startDate", startDate.toString());
        if (endDate != null) params.put("endDate", endDate.toString());
        if (minAmount != null) params.put("minAmount", minAmount.toString());
        if (maxAmount != null) params.put("maxAmount", maxAmount.toString());
        if (page != null) params.put("page", page.toString());
        if (size != null) params.put("size", size.toString());
        if (sort != null) params.put("sort", sort);

        StringBuilder uriBuilder = new StringBuilder("/expenses");
        if (!params.isEmpty()) {
            uriBuilder.append("?");
            params.forEach((key, value) ->
                uriBuilder.append(key).append("=").append(value).append("&")
            );
            uriBuilder.deleteCharAt(uriBuilder.length() - 1); // Remove trailing &
        }

        return webClient.get()
                .uri(uriBuilder.toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(Duration.ofMillis(config.getTimeout()))
                .block();
    }

    public String healthCheck() {
        return webClient.get()
                .uri("/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(config.getTimeout()))
                .onErrorResume(e -> Mono.just("Service unavailable: " + e.getMessage()))
                .block();
    }
}
