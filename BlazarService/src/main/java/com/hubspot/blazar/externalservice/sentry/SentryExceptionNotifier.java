package com.hubspot.blazar.externalservice.sentry;

import java.util.Map;

import net.kencochrane.raven.Raven;
import net.kencochrane.raven.RavenFactory;
import net.kencochrane.raven.event.Event;
import net.kencochrane.raven.event.EventBuilder;
import net.kencochrane.raven.event.interfaces.ExceptionInterface;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.BlazarSentryConfiguration;

/**
 * This class is used to send exception events to sentry (https://sentry.io/)
 * Providing a configured {@Link BlazarSentryConfiguration} in the blazar server yaml
 * will be enough to enable Sending notifications using this notifier.
 *
 * If no sentry configuration is provided {@Link DevNullExceptionNotifier} will be used instead.
 */
@Singleton
public class SentryExceptionNotifier implements ExceptionNotifier {
  private Optional<Raven> raven;
  private final Optional<BlazarSentryConfiguration> sentryConfiguration;

  @Inject
  public SentryExceptionNotifier(BlazarConfiguration configuration) {
    this.sentryConfiguration = configuration.getSentryConfiguration();
  }

  @Override
  public void start() throws Exception {
    if (sentryConfiguration.isPresent()) {
      this.raven = Optional.of(RavenFactory.ravenInstance(sentryConfiguration.get().getSentryDsn()));
    } else {
      this.raven = Optional.absent();
    }
  }

  @Override
  public void stop() throws Exception {
    if (!this.raven.isPresent()) {
      return;
    }
    this.raven.get().closeConnection();
  }

  private String getPrefix() {
    if (Strings.isNullOrEmpty(sentryConfiguration.get().getSentryPrefix())) {
      return "";
    }

    return sentryConfiguration.get().getSentryPrefix() + " ";
  }

  private String getCallingClassName(StackTraceElement[] stackTrace) {
    if (stackTrace != null && stackTrace.length > 2) {
      return stackTrace[2].getClassName();
    } else {
      return "(unknown)";
    }
  }

  private void sendEvent(Raven raven, final EventBuilder eventBuilder) {
    raven.runBuilderHelpers(eventBuilder);

    raven.sendEvent(eventBuilder.build());
  }

  public void notify(Throwable t, Map<String, String> extraData) {
    if (!raven.isPresent()) {
      return;
    }

    final StackTraceElement[] currentThreadStackTrace = Thread.currentThread().getStackTrace();

    final EventBuilder eventBuilder = new EventBuilder()
        .withCulprit(getPrefix() + t.getMessage())
        .withMessage(Strings.nullToEmpty(t.getMessage()))
        .withLevel(Event.Level.ERROR)
        .withLogger(getCallingClassName(currentThreadStackTrace))
        .withSentryInterface(new ExceptionInterface(t));

    if (extraData != null && !extraData.isEmpty()) {
      for (Map.Entry<String, String> entry : extraData.entrySet()) {
        eventBuilder.withExtra(entry.getKey(), entry.getValue());
      }
    }

    sendEvent(raven.get(), eventBuilder);
  }
}
