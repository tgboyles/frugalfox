package com.tgboyles.frugalfoxmcp.service;

import com.tgboyles.frugalfoxmcp.config.FrugalFoxApiConfig;
import com.tgboyles.frugalfoxmcp.dto.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    // Authentication method (used internally by TokenManager for automatic token refresh)
    @NonNull
    public AuthResponse login(@NonNull String username, @NonNull String password) {
        AuthRequest request = new AuthRequest(username, password);
        return Objects.requireNonNull(webClient.post()
                .uri("/auth/login")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .timeout(Duration.ofMillis(config.getTimeout()))
                .block());
    }

    // Expense CRUD methods
    @NonNull
    public ExpenseResponse createExpense(@NonNull String token, @NonNull ExpenseRequest expenseRequest) {
        String authHeader = "Bearer " + token;
        return Objects.requireNonNull(webClient.post()
                .uri("/expenses")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .bodyValue(expenseRequest)
                .retrieve()
                .bodyToMono(ExpenseResponse.class)
                .timeout(Duration.ofMillis(config.getTimeout()))
                .block());
    }

    @NonNull
    public ExpenseResponse getExpense(@NonNull String token, @NonNull Long id) {
        String authHeader = "Bearer " + token;
        return Objects.requireNonNull(webClient.get()
                .uri("/expenses/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(ExpenseResponse.class)
                .timeout(Duration.ofMillis(config.getTimeout()))
                .block());
    }

    @NonNull
    public ExpenseResponse updateExpense(@NonNull String token, @NonNull Long id, @NonNull ExpenseRequest expenseRequest) {
        String authHeader = "Bearer " + token;
        return Objects.requireNonNull(webClient.put()
                .uri("/expenses/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .bodyValue(expenseRequest)
                .retrieve()
                .bodyToMono(ExpenseResponse.class)
                .timeout(Duration.ofMillis(config.getTimeout()))
                .block());
    }

    public void deleteExpense(@NonNull String token, @NonNull Long id) {
        String authHeader = "Bearer " + token;
        webClient.delete()
                .uri("/expenses/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofMillis(config.getTimeout()))
                .block();
    }

    // Expense search and filter
    @NonNull
    public Map<String, Object> searchExpenses(
            @NonNull String token,
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

        String authHeader = "Bearer " + token;
        return Objects.requireNonNull(webClient.get()
                .uri(Objects.requireNonNull(uriBuilder.toString()))
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(Duration.ofMillis(config.getTimeout()))
                .block());
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
