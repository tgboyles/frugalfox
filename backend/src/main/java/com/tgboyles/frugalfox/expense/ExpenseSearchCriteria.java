package com.tgboyles.frugalfox.expense;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Search criteria for filtering expenses.
 *
 * <p>All fields are optional and can be combined for complex queries.
 */
public class ExpenseSearchCriteria {

  private String category;
  private String bank;
  private String merchant;
  private LocalDate startDate;
  private LocalDate endDate;
  private BigDecimal minAmount;
  private BigDecimal maxAmount;

  public ExpenseSearchCriteria() {}

  // Getters and setters

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getBank() {
    return bank;
  }

  public void setBank(String bank) {
    this.bank = bank;
  }

  public String getMerchant() {
    return merchant;
  }

  public void setMerchant(String merchant) {
    this.merchant = merchant;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public BigDecimal getMinAmount() {
    return minAmount;
  }

  public void setMinAmount(BigDecimal minAmount) {
    this.minAmount = minAmount;
  }

  public BigDecimal getMaxAmount() {
    return maxAmount;
  }

  public void setMaxAmount(BigDecimal maxAmount) {
    this.maxAmount = maxAmount;
  }
}
