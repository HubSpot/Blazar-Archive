package com.hubspot.blazar.externalservice.sentry;

import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 *  This classes job is to provide a no-op notifier for situations where sentry or another notifier
 *  is not configured.
 */

@Singleton
public class DevNullExceptionNotifier implements ExceptionNotifier {

  @Inject
  public DevNullExceptionNotifier () {
    // appease guice
  }

  @Override
  public void notify(Throwable t, Map<String, String> extraData) {
    // Quietly does nothing
  }

  @Override
  public void start() throws Exception {

  }

  @Override
  public void stop() throws Exception {

  }
}
