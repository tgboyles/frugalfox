package com.tgboyles.frugalfox.expense;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tgboyles.frugalfox.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Expense entity representing a user's expense transaction.
 *
 * <p>Each expense belongs to a specific user and contains information about the transaction
 * including date, merchant, amount, bank, and category.
 */
@Entity
@Table(name = "expenses")
public class Expense {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @NotNull(message = "Date is required")
  @PastOrPresent(message = "Date cannot be in the future")
  @Column(name = "expense_date", nullable = false)
  private LocalDate date;

  @NotBlank(message = "Merchant is required")
  @Size(max = 255, message = "Merchant must not exceed 255 characters")
  @Column(nullable = false)
  private String merchant;

  @NotNull(message = "Amount is required")
  @Positive(message = "Amount must be greater than zero")
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @NotBlank(message = "Bank is required")
  @Size(max = 100, message = "Bank must not exceed 100 characters")
  @Column(nullable = false, length = 100)
  private String bank;

  @NotBlank(message = "Category is required")
  @Size(max = 100, message = "Category must not exceed 100 characters")
  @Column(nullable = false, length = 100)
  private String category;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /** Default constructor for JPA. */
  public Expense() {}

  /**
   * Constructor for creating a new expense.
   *
   * @param user the user who owns this expense
   * @param date the date of the expense
   * @param merchant the merchant or description
   * @param amount the amount
   * @param bank the bank or payment source
   * @param category the expense category
   */
  public Expense(
      User user,
      LocalDate date,
      String merchant,
      BigDecimal amount,
      String bank,
      String category) {
    this.user = user;
    this.date = date;
    this.merchant = merchant;
    this.amount = amount;
    this.bank = bank;
    this.category = category;
  }

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  // Getters and setters

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public String getMerchant() {
    return merchant;
  }

  public void setMerchant(String merchant) {
    this.merchant = merchant;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getBank() {
    return bank;
  }

  public void setBank(String bank) {
    this.bank = bank;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Expense)) {
      return false;
    }
    Expense expense = (Expense) o;
    return id != null && id.equals(expense.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "Expense{"
        + "id="
        + id
        + ", date="
        + date
        + ", merchant='"
        + merchant
        + '\''
        + ", amount="
        + amount
        + ", bank='"
        + bank
        + '\''
        + ", category='"
        + category
        + '\''
        + '}';
  }
}
