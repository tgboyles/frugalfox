package com.tgboyles.frugalfoxmcp.dto;

import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
    @NonNull LocalDate date,
    @NonNull String merchant,
    @NonNull BigDecimal amount,
    @NonNull String bank,
    @NonNull String category
) {
}
