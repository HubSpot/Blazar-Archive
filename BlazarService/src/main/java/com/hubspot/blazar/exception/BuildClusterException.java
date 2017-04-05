package com.hubspot.blazar.exception;

public class BuildClusterException extends Exception {
  public BuildClusterException(String message) {
    super(message);
  }

  public BuildClusterException(String message, Throwable t) {
    super(message, t);
  }
}
