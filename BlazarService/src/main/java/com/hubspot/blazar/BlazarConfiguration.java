package com.hubspot.blazar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BlazarConfiguration extends Configuration {

  @JsonProperty("github")
  private Optional<GitHubConfiguration> gitHubConfiguration = Optional.absent();

  @Valid
  @JsonProperty("zookeeper")
  private ZooKeeperConfiguration zooKeeperConfiguration;

  @JsonProperty("database")
  private Optional<DataSourceFactory> databaseConfiguration = Optional.absent();

  public Optional<GitHubConfiguration> getGitHubConfiguration() {
    return gitHubConfiguration;
  }

  public BlazarConfiguration setGitHubConfiguration(GitHubConfiguration gitHubConfiguration) {
    this.gitHubConfiguration = Optional.of(gitHubConfiguration);
    return this;
  }

  public ZooKeeperConfiguration getZooKeeperConfiguration() {
    return zooKeeperConfiguration;
  }

  public BlazarConfiguration setZooKeeperConfiguration(ZooKeeperConfiguration zooKeeperConfiguration) {
    this.zooKeeperConfiguration = zooKeeperConfiguration;
    return this;
  }

  public Optional<DataSourceFactory> getDatabaseConfiguration() {
    return databaseConfiguration;
  }

  public BlazarConfiguration setDatabaseConfiguration(DataSourceFactory databaseConfiguration) {
    this.databaseConfiguration = Optional.of(databaseConfiguration);
    return this;
  }
}
