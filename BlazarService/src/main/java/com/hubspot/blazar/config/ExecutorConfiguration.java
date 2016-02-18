package com.hubspot.blazar.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.concurrent.TimeUnit;

public class ExecutorConfiguration {

  @NotNull
  private final String defaultBuildUser;

  @Size(min = 0)
  private final long buildTimeoutMillis;

  @JsonCreator
  public ExecutorConfiguration(@JsonProperty("defaultBuildUser") Optional<String> defaultBuildUser,
                               @JsonProperty("buildTimeoutMillis") Optional<Long> buildTimeoutMillis) {
    this.defaultBuildUser = Objects.firstNonNull(defaultBuildUser, Optional.<String>absent()).or("root");
    this.buildTimeoutMillis = Objects.firstNonNull(buildTimeoutMillis, Optional.<Long>absent()).or(TimeUnit.MINUTES.toMillis(20));
  }

  public static ExecutorConfiguration defaultConfiguration() {
    return new ExecutorConfiguration(null, null);
  }

  public String getDefaultBuildUser() {
    return defaultBuildUser;
  }

  public long getBuildTimeoutMillis() {
    return buildTimeoutMillis;
  }
}
