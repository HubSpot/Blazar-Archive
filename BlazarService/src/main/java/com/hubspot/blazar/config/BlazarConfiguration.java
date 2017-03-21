package com.hubspot.blazar.config;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

import io.dropwizard.db.DataSourceFactory;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BlazarConfiguration {

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
  private ExecutorConfiguration executorConfiguration = ExecutorConfiguration.defaultConfiguration();

  @JsonProperty("zookeeper")
  private Optional<ZooKeeperConfiguration> zooKeeperConfiguration = Optional.absent();

  @Valid
  @NotNull
  @JsonProperty("database")
  private DataSourceFactory databaseConfiguration;

  @Valid
  @NotNull
  @JsonProperty("ui")
  private UiConfiguration uiConfiguration;

  @Valid
  @JsonProperty("slack_blazar")
  private Optional<BlazarSlackConfiguration> slackConfiguration = Optional.absent();

  // allows you to opt-in whole repositories by name
  private Set<String> whitelist = Collections.emptySet();
  // allows you to opt out whole repositories by name
  private Set<String> blacklist = Collections.emptySet();

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
