package com.tgboyles.frugalfoxmcp.dto;

import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseResponse(
    @NonNull Long id,
    @NonNull LocalDate date,
    @NonNull String merchant,
    @NonNull BigDecimal amount,
    @NonNull String bank,
    @NonNull String category,
    @NonNull LocalDateTime createdAt,
    @NonNull LocalDateTime updatedAt
) {
}
