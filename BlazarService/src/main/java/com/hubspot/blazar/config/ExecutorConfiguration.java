package com.hubspot.blazar.config;

import java.util.concurrent.TimeUnit;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.hubspot.blazar.external.models.singularity.Resources;

public class ExecutorConfiguration {

  @NotNull
  private final String defaultBuildUser;

  @Min(0)
  private final long buildTimeoutMillis;
  // This is left as optional because the request that you configure in
  // is the last set of default options
  private final Optional<Resources> defaultBuildResources;

  @JsonCreator
  public ExecutorConfiguration(@JsonProperty("defaultBuildUser") Optional<String> defaultBuildUser,
                               @JsonProperty("defaultBuildResources") Optional<Resources> defaultBuildResources,
                               @JsonProperty("buildTimeoutMillis") Optional<Long> buildTimeoutMillis) {

    this.defaultBuildResources = MoreObjects.firstNonNull(defaultBuildResources, Optional.<Resources>absent());
    this.defaultBuildUser = MoreObjects.firstNonNull(defaultBuildUser, Optional.<String>absent()).or("root");
    this.buildTimeoutMillis = MoreObjects.firstNonNull(buildTimeoutMillis, Optional.<Long>absent()).or(TimeUnit.MINUTES.toMillis(20));
  }

  public static ExecutorConfiguration defaultConfiguration() {
    return new ExecutorConfiguration(null, null, null);
  }

  public String getDefaultBuildUser() {
    return defaultBuildUser;
  }

  public long getBuildTimeoutMillis() {
    return buildTimeoutMillis;
  }

  public Optional<Resources> getDefaultBuildResources() {
    return defaultBuildResources;
  }
}
