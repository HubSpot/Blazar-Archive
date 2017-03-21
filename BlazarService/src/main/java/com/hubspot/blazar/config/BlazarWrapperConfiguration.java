package com.hubspot.blazar.config;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

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
