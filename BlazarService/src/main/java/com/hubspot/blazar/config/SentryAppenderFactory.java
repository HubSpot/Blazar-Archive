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
 * This class configures sentry via a Logback appender the resource
 * file META-INF/services/io.dropwizard.logging.AppenderFactory is responsible
 * for telling Dropwizard's logging mechanics to create an appender using this class
 * provided that the logging configuration of type "sentry" is available.
 *
 * <p>
 * sentryDsn: string (Data Source Name) is the secret Sentry gives you to report exceptions with.
 * sentryRelease: string the version of blazar you are deploying. Sentry will tag exceptions using this release value
 *
 * This appender is only used if you configure the logging appender in the dropwizard config for sentry as follows:
 * {@Code
 *  logging:
 *    appenders:
 *      - type: sentry
 *        sentryDsn: http://some_Sentry_provided_DSN
 *        sentryRelease: "PROD"
 * }
 *
 */
@JsonTypeName("sentry")
public class SentryAppenderFactory extends AbstractAppenderFactory<ILoggingEvent> {
  private static final String APPENDER_NAME = "sentry-appender";
  private final String sentryDsn;
  private final String sentryRelease;

  @JsonCreator
  public SentryAppenderFactory(
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
