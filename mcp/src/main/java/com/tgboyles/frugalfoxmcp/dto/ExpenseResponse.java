package com.tgboyles.frugalfoxmcp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseResponse(
    Long id,
    LocalDate date,
    String merchant,
    BigDecimal amount,
    String bank,
    String category,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
