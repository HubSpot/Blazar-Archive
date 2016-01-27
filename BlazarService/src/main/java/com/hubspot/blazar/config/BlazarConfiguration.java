package com.hubspot.blazar.config;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BlazarConfiguration extends Configuration {

  @NotNull
  @JsonProperty("github")
  private Map<String, GitHubConfiguration> gitHubConfiguration;

  @Valid
  @NotNull
  @JsonProperty("singularity")
  private SingularityConfiguration singularityConfiguration;

  @Valid
  @NotNull
  @JsonProperty("executor")
  private ExecutorConfiguration executorConfiguration = new ExecutorConfiguration("root");

  @Valid
  @NotNull
  @JsonProperty("zookeeper")
  private ZooKeeperConfiguration zooKeeperConfiguration;

  @Valid
  @NotNull
  @JsonProperty("database")
  private DataSourceFactory databaseConfiguration;

  @Valid
  @JsonProperty("slack_blazar")
  private Optional<SlackConfiguration> slackConfiguration = Optional.absent();

  private Set<String> whitelist = Collections.emptySet();

  private boolean webhookOnly = false;

  public Map<String, GitHubConfiguration> getGitHubConfiguration() {
    return gitHubConfiguration;
  }

  public BlazarConfiguration setGitHubConfiguration(Map<String, GitHubConfiguration> gitHubConfiguration) {
    this.gitHubConfiguration = gitHubConfiguration;
    return this;
  }

  public SingularityConfiguration getSingularityConfiguration() {
    return singularityConfiguration;
  }

  public BlazarConfiguration setSingularityConfiguration(SingularityConfiguration singularityConfiguration) {
    this.singularityConfiguration = singularityConfiguration;
    return this;
  }

  public ExecutorConfiguration getExecutorConfiguration() {
    return executorConfiguration;
  }

  public BlazarConfiguration setExecutorConfiguration(ExecutorConfiguration executorConfiguration) {
    this.executorConfiguration = executorConfiguration;
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

  public Optional<SlackConfiguration> getSlackConfiguration() {
    return slackConfiguration;
  }

  public void setSlackConfiguration(Optional<SlackConfiguration> slackConfiguration) {
    this.slackConfiguration = slackConfiguration;
  }

  public Set<String> getWhitelist() {
    return whitelist;
  }

  public BlazarConfiguration setWhitelist(Set<String> whitelist) {
    this.whitelist = whitelist;
    return this;
  }

  public boolean isWebhookOnly() {
    return webhookOnly;
  }

  public BlazarConfiguration setWebhookOnly(boolean webhookOnly) {
    this.webhookOnly = webhookOnly;
    return this;
  }
}
