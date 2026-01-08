package com.tgboyles.frugalfox.expense;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing the result of a bulk expense import operation.
 *
 * <p>Contains statistics about the import (total, successful, failed) and any error messages.
 */
public class ImportResult {

  private int totalRows;
  private int successfulImports;
  private int failedImports;
  private List<String> errors;

  public ImportResult() {
    this.errors = new ArrayList<>();
  }

  public ImportResult(int totalRows, int successfulImports, int failedImports, List<String> errors) {
    this.totalRows = totalRows;
    this.successfulImports = successfulImports;
    this.failedImports = failedImports;
    this.errors = errors != null ? errors : new ArrayList<>();
  }

  public int getTotalRows() {
    return totalRows;
  }

  public void setTotalRows(int totalRows) {
    this.totalRows = totalRows;
  }

  public int getSuccessfulImports() {
    return successfulImports;
  }

  public void setSuccessfulImports(int successfulImports) {
    this.successfulImports = successfulImports;
  }

  public int getFailedImports() {
    return failedImports;
  }

  public void setFailedImports(int failedImports) {
    this.failedImports = failedImports;
  }

  public List<String> getErrors() {
    return errors;
  }

  public void setErrors(List<String> errors) {
    this.errors = errors;
  }

  public void addError(String error) {
    this.errors.add(error);
  }
}
