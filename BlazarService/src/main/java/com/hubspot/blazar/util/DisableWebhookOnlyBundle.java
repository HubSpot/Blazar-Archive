package com.hubspot.blazar.util;

import com.hubspot.blazar.config.BlazarConfiguration;
import io.dropwizard.Bundle;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * A Bundle decorator that doesn't install the bundle if running in webhookOnly mode
 */
public class DisableWebhookOnlyBundle implements ConfiguredBundle<BlazarConfiguration> {
  private final Bundle delegate;

  public DisableWebhookOnlyBundle(Bundle delegate) {
    this.delegate = delegate;
  }

  @Override
  public void initialize(Bootstrap<?> bootstrap) {}

  @Override
  public void run(BlazarConfiguration configuration, Environment environment) throws Exception {
    if (!configuration.isWebhookOnly()) {
      delegate.run(environment);
    }
  }
}
