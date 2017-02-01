package com.hubspot.blazar.exception;

public class NonRetryableBuildException extends RuntimeException {

  public NonRetryableBuildException(String message) {
    super(message);
  }

  public NonRetryableBuildException(String message, Throwable t) {
    super(message, t);
  }
}
