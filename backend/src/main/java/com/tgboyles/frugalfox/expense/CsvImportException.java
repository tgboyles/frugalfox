package com.tgboyles.frugalfox.expense;

/**
 * Exception thrown when CSV import validation fails.
 *
 * <p>This exception includes details about where in the CSV file the error occurred.
 */
public class CsvImportException extends RuntimeException {

  private final String details;

  public CsvImportException(String message) {
    super(message);
    this.details = message;
  }

  public CsvImportException(String message, Throwable cause) {
    super(message, cause);
    this.details = message;
  }

  public String getDetails() {
    return details;
  }
}
