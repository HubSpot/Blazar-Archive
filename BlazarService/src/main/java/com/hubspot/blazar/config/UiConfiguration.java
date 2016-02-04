package com.hubspot.blazar.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class UiConfiguration {

  @NotNull
  private final String baseUrl;

  @JsonCreator
  public UiConfiguration(@JsonProperty("baseUrl") String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getBaseUrl() {
    return baseUrl;
  }
}
