package com.hubspot.blazar.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.getsentry.raven.logback.SentryAppender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.dropwizard.logging.AbstractAppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;

/**
 * This class configures sentry:
 *
 * Sentry DSN : string (Data Source Name) is the secret Sentry gives you to report exceptions with.
 * Prefix : prefix string for event culprit naming and sentry message.
 *
 */
@JsonTypeName("sentry")
public class BlazarSentryConfiguration extends AbstractAppenderFactory<ILoggingEvent> {
  private static final String APPENDER_NAME = "sentry-appender";
  private final String sentryDsn;
  private final String sentryRelease;

  @JsonCreator
  public BlazarSentryConfiguration(
      @NotNull
      @JsonProperty("sentryDsn") String sentryDsn,
      @JsonProperty("sentryRelease") String sentryRelease ) {

    this.sentryDsn = sentryDsn;
    this.sentryRelease = sentryRelease;
  }

  public String getSentryDsn() {
    return sentryDsn;
  }

  public String getSentryRelease() {
    return sentryRelease;
  }

  @Override
  public Appender<ILoggingEvent> build(LoggerContext context,
                                       String applicationName,
                                       LayoutFactory<ILoggingEvent> layoutFactory,
                                       LevelFilterFactory<ILoggingEvent> levelFilterFactory,
                                       AsyncAppenderFactory<ILoggingEvent> asyncAppenderFactory) {

    final SentryAppender appender = new SentryAppender();
    appender.setName(APPENDER_NAME);
    appender.setDsn(sentryDsn);
    appender.setRelease(sentryRelease);
    appender.setServerName(getHostNameOrTaskId());
    appender.setContext(context);
    appender.addFilter(levelFilterFactory.build(Level.ERROR));
    getFilterFactories().stream().forEach(f -> appender.addFilter(f.build()));
    appender.start();
    return wrapAsync(appender, asyncAppenderFactory, context);
  }

  private static String getHostNameOrTaskId() {
    // Try to use "TASK_HOST" provided by singularity deploy environment
    if (System.getenv().containsKey("TASK_HOST") ) {
      return System.getenv().get("TASK_HOST");
    }

    // If not deployed in singularity try to fetch the hostname out of dns
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      return "unknown-host";
    }
  }

}
