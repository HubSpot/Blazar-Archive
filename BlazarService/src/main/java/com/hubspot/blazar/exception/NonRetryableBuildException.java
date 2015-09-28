package com.hubspot.blazar.exception;

public class NonRetryableBuildException extends Exception {

  public NonRetryableBuildException(String message) {
    super(message);
  }

  public NonRetryableBuildException(String message, Exception e) {
    super(message, e);
  }
}
