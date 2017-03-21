package com.hubspot.blazar.config;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

/**
 * This wraps {@Link BlazarConfiguration} so that all the Blazar configuration
 * options are available under a single key in the dropwizard yaml.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BlazarWrapperConfiguration extends Configuration {

  private BlazarConfiguration blazarConfiguration;

  @JsonCreator
  public BlazarWrapperConfiguration(@JsonProperty("blazarConfiguration") BlazarConfiguration blazarConfiguration) {

    this.blazarConfiguration = blazarConfiguration;
  }

  public BlazarConfiguration getBlazarConfiguration() {
    return blazarConfiguration;
  }
}
