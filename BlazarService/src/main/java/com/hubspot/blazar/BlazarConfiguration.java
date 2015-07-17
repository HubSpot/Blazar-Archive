package com.hubspot.blazar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import io.dropwizard.Configuration;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BlazarConfiguration extends Configuration {

  @JsonProperty("github")
  private Optional<GitHubConfiguration> gitHubConfiguration = Optional.absent();

  public Optional<GitHubConfiguration> getGitHubConfiguration() {
    return gitHubConfiguration;
  }

  public BlazarConfiguration setGitHubConfiguration(GitHubConfiguration gitHubConfiguration) {
    this.gitHubConfiguration = Optional.of(gitHubConfiguration);
    return this;
  }
}
