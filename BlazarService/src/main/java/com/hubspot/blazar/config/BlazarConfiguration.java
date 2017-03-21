package com.hubspot.blazar.config;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

import io.dropwizard.db.DataSourceFactory;

/**
 * The configuration for Blazar.
 * All options that control how the BlazarService behaves are configured here. These are wrapped by {@Link BlazarWrapperConfiguration}
 * so that there is 1 top level key in the dropwizard yaml which contains all the Blazar properties.
 */
public class BlazarConfiguration {


  // Defines the deployments of GitHub that Blazar can connect to.
  @NotNull
  @JsonProperty("github")
  private Map<String, GitHubConfiguration> gitHubConfiguration;

  // The configuration required for Blazar to connect to Singularity
  @Valid
  @NotNull
  @JsonProperty("singularity")
  private SingularityConfiguration singularityConfiguration;

  // Default options we pass to the executor
  @Valid
  @NotNull
  @JsonProperty("executor")
  private ExecutorConfiguration executorConfiguration = ExecutorConfiguration.defaultConfiguration();

  // Configuration for Blazar to connect to Zookeeper
  // Required for leader election, and for Blazar to enable the buildVisitors (only the master handles build events).
  @JsonProperty("zookeeper")
  private Optional<ZooKeeperConfiguration> zooKeeperConfiguration = Optional.absent();

  /* The configuration for Blazar's mysql database
   * database:
   *   driverClass: com.mysql.jdbc.Driver
   *   user: user
   *   password: "password"
   *   url: jdbc:mysql://host:3306/BlazarV2
   */
  @Valid
  @NotNull
  @JsonProperty("database")
  private DataSourceFactory databaseConfiguration;

  // Configuration for Blazar's Ui so the backend can correctly generate links to the UI.
  @Valid
  @NotNull
  @JsonProperty("ui")
  private UiConfiguration uiConfiguration;

  // Optional Configuration for Slack.
  // Blazar uses this configuration to send messages to channels and individuals about builds
  @Valid
  @JsonProperty("slack_blazar")
  private Optional<BlazarSlackConfiguration> slackConfiguration = Optional.absent();

  // allows you to opt-in whole repositories by name
  private Set<String> whitelist = Collections.emptySet();

  // allows you to opt out whole repositories by name
  private Set<String> blacklist = Collections.emptySet();

  // Controls whether this instance of blazar is configured to only accept webhooks or not.
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

  public Optional<ZooKeeperConfiguration> getZooKeeperConfiguration() {
    return zooKeeperConfiguration;
  }

  public BlazarConfiguration setZooKeeperConfiguration(ZooKeeperConfiguration zooKeeperConfiguration) {
    this.zooKeeperConfiguration = Optional.of(zooKeeperConfiguration);
    return this;
  }

  public DataSourceFactory getDatabaseConfiguration() {
    return databaseConfiguration;
  }

  public BlazarConfiguration setDatabaseConfiguration(DataSourceFactory databaseConfiguration) {
    this.databaseConfiguration = databaseConfiguration;
    return this;
  }

  public UiConfiguration getUiConfiguration() {
    return uiConfiguration;
  }

  public BlazarConfiguration setUiConfiguration(UiConfiguration uiConfiguration) {
    this.uiConfiguration = uiConfiguration;
    return this;
  }

  public Optional<BlazarSlackConfiguration> getSlackConfiguration() {
    return slackConfiguration;
  }

  public void setSlackConfiguration(Optional<BlazarSlackConfiguration> slackConfiguration) {
    this.slackConfiguration = slackConfiguration;
  }

  public Set<String> getWhitelist() {
    return whitelist;
  }

  public BlazarConfiguration setWhitelist(Set<String> whitelist) {
    this.whitelist = whitelist;
    return this;
  }

  public Set<String> getBlacklist() {
    return blacklist;
  }

  public BlazarConfiguration setBlacklist(Set<String> blacklist) {
    this.blacklist = blacklist;
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
