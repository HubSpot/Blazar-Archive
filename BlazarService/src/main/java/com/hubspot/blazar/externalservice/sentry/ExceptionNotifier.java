package com.hubspot.blazar.externalservice.sentry;

import java.util.Map;

import io.dropwizard.lifecycle.Managed;

public interface ExceptionNotifier extends Managed {

  void notify(Throwable t, Map<String, String> extraData);

}
