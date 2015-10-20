package com.hubspot.blazar.config;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
  @JsonProperty("zookeeper")
  private ZooKeeperConfiguration zooKeeperConfiguration;

  @Valid
  @NotNull
  @JsonProperty("database")
  private DataSourceFactory databaseConfiguration;

  @Valid
  @JsonProperty("ui")
  private UiConfiguration uiConfiguration = new UiConfiguration();

  @Valid
  @NotNull
  @JsonProperty("moduleBuildpacks")
  private Map<String, BuildpackConfiguration> moduleBuildpackConfiguration = new HashMap<>();

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

  public UiConfiguration getUiConfiguration() {
    return uiConfiguration;
  }

  public BlazarConfiguration setUiConfiguration(UiConfiguration uiConfiguration) {
    this.uiConfiguration = uiConfiguration;
    return this;
  }

  public Map<String, BuildpackConfiguration> getModuleBuildpackConfiguration() {
    return moduleBuildpackConfiguration;
  }

  public BlazarConfiguration setModuleBuildpackConfiguration(Map<String, BuildpackConfiguration> moduleBuildpackConfiguration) {
    this.moduleBuildpackConfiguration = moduleBuildpackConfiguration;
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
