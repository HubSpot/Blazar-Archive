package com.hubspot.blazar.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class configures the constants sentry needs to function.
 *
 * Sentry DSN : string (Data Source Name) is the secret Sentry gives you to report exceptions with.
 * Prefix : prefix string for event culprit naming and sentry message.
 *
 */
public class BlazarSentryConfiguration {

  private final String sentryDsn;
  private final String sentryPrefix;

  @JsonCreator
  public BlazarSentryConfiguration(@JsonProperty("sentryDsn") String sentryDsn,
                                   @JsonProperty("sentryPrefix") String sentryPrefix) {

    this.sentryDsn = sentryDsn;
    this.sentryPrefix = sentryPrefix;
  }

  public String getSentryDsn() {
    return sentryDsn;
  }

  public String getSentryPrefix() {
    return sentryPrefix;
  }
}
