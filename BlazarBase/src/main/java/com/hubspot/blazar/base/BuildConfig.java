package com.hubspot.blazar.base;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

public class BuildConfig {
  private final List<BuildStep> steps;
  private final Map<String, String> env;
  private final List<String> buildDeps;
  private final List<String> webhooks;
  private final List<String> cache;
  private final Optional<GitInfo> buildpack;

  @JsonCreator
  public BuildConfig(@JsonProperty("steps") List<BuildStep> steps,
                     @JsonProperty("env") Map<String, String> env,
                     @JsonProperty("buildDeps") List<String> buildDeps,
                     @JsonProperty("webhooks") List<String> webhooks,
                     @JsonProperty("cache") List<String> cache,
                     @JsonProperty("buildpack") Optional<GitInfo> buildpack) {
    this.steps = Objects.firstNonNull(steps, Collections.<BuildStep>emptyList());
    this.env = Objects.firstNonNull(env, Collections.<String,String>emptyMap());
    this.buildDeps = Objects.firstNonNull(buildDeps, Collections.<String>emptyList());
    this.webhooks = Objects.firstNonNull(webhooks, Collections.<String>emptyList());
    this.buildpack = Objects.firstNonNull(buildpack, Optional.<GitInfo>absent());
    this.cache = Objects.firstNonNull(cache, Collections.<String>emptyList());
  }

  public static BuildConfig makeDefaultBuildConfig(){
    return new BuildConfig(null, null, null, null, null, null);
  }

  public List<BuildStep> getSteps() {
    return steps;
  }

  public Map<String, String> getEnv() {
    return env;
  }

  public List<String> getBuildDeps() {
    return buildDeps;
  }

  public List<String> getWebhooks() {
    return webhooks;
  }

  public List<String> getCache() {
    return cache;
  }

  public Optional<GitInfo> getBuildpack() {
    return buildpack;
  }
}
