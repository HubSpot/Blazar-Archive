package com.hubspot.blazar.config;

import java.util.concurrent.TimeUnit;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.hubspot.blazar.external.models.singularity.BuildCGroupResources;

public class ExecutorConfiguration {

  @NotNull
  private final String defaultBuildUser;

  @Min(0)
  private final long buildTimeoutMillis;

  @Min(0)
  private final long containerStartTimeoutMillis;
  // This is left as optional because the request that you configure in
  // is the last set of default options
  private final Optional<BuildCGroupResources> defaultBuildResources;

  /**
   * @param defaultBuildUser The default user for builds to run as
   * @param buildTimeoutMillis The time to wait before considering a running build to be stuck and for it to be killed.
   * @param containerStartTimeoutMillis The time to wait before considering the container launch to have failed. Hitting this limit fails the module build.
   */
  @JsonCreator
  public ExecutorConfiguration(@JsonProperty("defaultBuildUser") Optional<String> defaultBuildUser,
                               @JsonProperty("defaultBuildResources") Optional<BuildCGroupResources> defaultBuildResources,
                               @JsonProperty("buildTimeoutMillis") Optional<Long> buildTimeoutMillis,
                               @JsonProperty("containerStartTimeoutMillis") Optional<Long> containerStartTimeoutMillis) {

    this.defaultBuildResources = MoreObjects.firstNonNull(defaultBuildResources, Optional.<BuildCGroupResources>absent());
    this.defaultBuildUser = MoreObjects.firstNonNull(defaultBuildUser, Optional.<String>absent()).or("root");
    this.buildTimeoutMillis = MoreObjects.firstNonNull(buildTimeoutMillis, Optional.<Long>absent()).or(TimeUnit.MINUTES.toMillis(20));
    this.containerStartTimeoutMillis = MoreObjects.firstNonNull(containerStartTimeoutMillis, Optional.<Long>absent()).or(TimeUnit.MINUTES.toMillis(5));
  }

  public static ExecutorConfiguration defaultConfiguration() {
    return new ExecutorConfiguration(Optional.absent(), Optional.absent(), Optional.absent(), Optional.absent());
  }

  public String getDefaultBuildUser() {
    return defaultBuildUser;
  }

  public long getBuildTimeoutMillis() {
    return buildTimeoutMillis;
  }

  public Optional<BuildCGroupResources> getDefaultBuildResources() {
    return defaultBuildResources;
  }

  public long getContainerStartTimeoutMillis() {
    return containerStartTimeoutMillis;
  }
}
