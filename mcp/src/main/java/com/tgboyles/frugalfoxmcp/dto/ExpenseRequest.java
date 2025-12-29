package com.tgboyles.frugalfoxmcp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
    LocalDate date,
    String merchant,
    BigDecimal amount,
    String bank,
    String category
) {
}
