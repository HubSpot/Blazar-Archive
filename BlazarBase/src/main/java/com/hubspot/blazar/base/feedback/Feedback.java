package com.hubspot.blazar.base.feedback;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class Feedback {

  private final String username;
  private final String message;
  private final String page;
  private final long timestamp;
  private final Optional<String> other;

  @JsonCreator
  public Feedback(@JsonProperty("username") String username,
                  @JsonProperty("message") String message,
                  @JsonProperty("page") String page,
                  @JsonProperty("timestamp") long timestamp,
                  @JsonProperty("other") Optional<String> other) {
    this.username = username;
    this.message = message;
    this.page = page;
    this.timestamp = timestamp;
    this.other = other;
  }

  public String getUsername() {
    return username;
  }

  public String getMessage() {
    return message;
  }

  public String getPage() {
    return page;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public Optional<String> getOther() {
    return other;
  }

}
