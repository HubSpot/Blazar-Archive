package com.hubspot.blazar.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BlazarConfiguration extends Configuration {

  @JsonProperty("github")
  private Map<String, GitHubConfiguration> gitHubConfiguration = Collections.emptyMap();

  @Valid
  @JsonProperty("zookeeper")
  private ZooKeeperConfiguration zooKeeperConfiguration;

  @JsonProperty("database")
  private DataSourceFactory databaseConfiguration;

  public Map<String, GitHubConfiguration> getGitHubConfiguration() {
    return gitHubConfiguration;
  }

  public BlazarConfiguration setGitHubConfiguration(Map<String, GitHubConfiguration> gitHubConfiguration) {
    this.gitHubConfiguration = gitHubConfiguration;
    return this;
  }

  public ZooKeeperConfiguration getZooKeeperConfiguration() {
    return zooKeeperConfiguration;
  }

  public BlazarConfiguration setZooKeeperConfiguration(ZooKeeperConfiguration zooKeeperConfiguration) {
    this.zooKeeperConfiguration = zooKeeperConfiguration;
    return this;
  }

  public DataSourceFactory getDatabaseConfiguration() {
    return databaseConfiguration;
  }

  public BlazarConfiguration setDatabaseConfiguration(DataSourceFactory databaseConfiguration) {
    this.databaseConfiguration = databaseConfiguration;
    return this;
  }
}
