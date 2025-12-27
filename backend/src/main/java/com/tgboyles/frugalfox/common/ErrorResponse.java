package com.tgboyles.frugalfox.common;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Standardized error response structure for API errors.
 */
public class ErrorResponse {

  private LocalDateTime timestamp;
  private int status;
  private String error;
  private String message;
  private List<FieldError> errors;

  public ErrorResponse() {
    this.timestamp = LocalDateTime.now();
    this.errors = new ArrayList<>();
  }

  public ErrorResponse(int status, String error, String message) {
    this();
    this.status = status;
    this.error = error;
    this.message = message;
  }

  // Getters and setters

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public List<FieldError> getErrors() {
    return errors;
  }

  public void setErrors(List<FieldError> errors) {
    this.errors = errors;
  }

  /** Nested class for field-specific validation errors. */
  public static class FieldError {
    private String field;
    private String message;

    public FieldError() {}

    public FieldError(String field, String message) {
      this.field = field;
      this.message = message;
    }

    public String getField() {
      return field;
    }

    public void setField(String field) {
      this.field = field;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }
  }
}
