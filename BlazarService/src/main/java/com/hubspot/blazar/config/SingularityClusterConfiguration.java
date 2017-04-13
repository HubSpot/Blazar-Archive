package com.hubspot.blazar.config;

import static com.hubspot.blazar.config.SingularityClusterConfiguration.BuildStrategy.ALWAYS;

import java.util.Collections;
import java.util.Set;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.CharMatcher;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.hubspot.singularity.SingularityClientCredentials;

public class SingularityClusterConfiguration {

  public enum BuildStrategy {
    BLACKLIST, // Build in this cluster only if the built module is NOT in a repository that is included in the provided repositories.
    WHITELIST, // Build in this cluster only if the built module is in a repository that is included in the provided whitelist
    EXCLUSIVE_WHITELIST, // Build the repositories provided in the whitelist exclusively in this cluster and nowhere else
    ALWAYS, // This cluster is available for any build
    EMERGENCY, // Only build in this cluster if there is an emergency, i.e. other clusters are unavailable
    EMERGENCY_AND_WHITELIST,
    EMERGENCY_AND_EXCLUSIVE_WHITELIST
  }

  @NotNull
  private final String host;
  @NotNull
  private final String request;
  private final Optional<String> path;
  private final Optional<SingularityClientCredentials> credentials;
  private final int slaveHttpPort;
  /**
   * An instruction on how to use this cluster when building
   */
  private final BuildStrategy buildStrategy;
  /**
   * A set of repositories that are allowed/disallowed (depending on the selected build strategy) to build in this cluster.
   * The name should be in the form of <git-host>-<organization>-<repository name>
   */
  private final Set<String> repositories;

  @JsonCreator
  public SingularityClusterConfiguration(@JsonProperty("host") String host,
                                         @JsonProperty("request") String request,
                                         @JsonProperty("path") Optional<String> path,
                                         @JsonProperty("credentials") Optional<SingularityClientCredentials> credentials,
                                         @JsonProperty("slaveHttpPort") int slaveHttpPort,
                                         @JsonProperty("buildStrategy") BuildStrategy buildStrategy,
                                         @JsonProperty("repositories") Set<String> repositories) {

    Preconditions.checkArgument(!Strings.isNullOrEmpty(host));
    this.host = host;
    this.request = request;
    this.path = trimSlashes(path); // singularity client adds leading and trailing slashes
    this.credentials = credentials;
    this.slaveHttpPort = slaveHttpPort;
    this.buildStrategy = MoreObjects.firstNonNull(buildStrategy, ALWAYS);
    this.repositories = MoreObjects.firstNonNull(repositories, Collections.emptySet());
  }

  public String getHost() {
    return host;
  }

  public String getRequest() {
    return request;
  }

  public Optional<String> getPath() {
    return path;
  }

  public Optional<SingularityClientCredentials> getCredentials() {
    return credentials;
  }

  public int getSlaveHttpPort() {
    return slaveHttpPort;
  }

  public BuildStrategy getBuildStrategy() {
    return buildStrategy;
  }

  public Set<String> getRepositories() {
    return repositories;
  }

  private static Optional<String> trimSlashes(Optional<String> s) {
    if (s.isPresent()) {
      return Optional.of(CharMatcher.is('/').trimFrom(s.get()));
    } else {
      return s;
    }
  }
}
