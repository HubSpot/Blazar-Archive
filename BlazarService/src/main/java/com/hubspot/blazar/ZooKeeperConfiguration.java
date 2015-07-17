package com.hubspot.blazar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ZooKeeperConfiguration {

  @NotNull
  private final String quorum;

  @NotNull
  private final String namespace;

  @Min(0)
  private final int sessionTimeoutMillis;

  @Min(0)
  private final int connectTimeoutMillis;

  @Min(0)
  private final int initialRetryBackoffMillis;

  @Min(0)
  private final int maxRetries;

  @JsonCreator
  public ZooKeeperConfiguration(@JsonProperty("quorum") String quorum,
                                @JsonProperty("namespace") String namespace,
                                @JsonProperty("sessionTimeoutMillis") Optional<Integer> sessionTimeoutMillis,
                                @JsonProperty("connectTimeoutMillis") Optional<Integer> connectTimeoutMillis,
                                @JsonProperty("initialRetryBackoffMillis") Optional<Integer> initialRetryBackoffMillis,
                                @JsonProperty("maxRetries") Optional<Integer> maxRetries) {
    this.quorum = quorum;
    this.namespace = namespace;
    this.sessionTimeoutMillis = sessionTimeoutMillis.or(600_000);
    this.connectTimeoutMillis = connectTimeoutMillis.or(60_000);
    this.initialRetryBackoffMillis = initialRetryBackoffMillis.or(1_000);
    this.maxRetries = maxRetries.or(3);
  }

  public String getQuorum() {
    return quorum;
  }

  public String getNamespace() {
    return namespace;
  }

  public int getSessionTimeoutMillis() {
    return sessionTimeoutMillis;
  }

  public int getConnectTimeoutMillis() {
    return connectTimeoutMillis;
  }

  public int getInitialRetryBackoffMillis() {
    return initialRetryBackoffMillis;
  }

  public int getMaxRetries() {
    return maxRetries;
  }
}
