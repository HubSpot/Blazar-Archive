package com.hubspot.blazar.base;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

public class BuildConfig {
  private final List<BuildStep> steps;
  private final List<BuildStep> before;
  private final Map<String, String> env;
  private final List<String> buildDeps;
  private final List<String> webhooks;
  private final List<String> cache;
  private final Optional<GitInfo> buildpack;
  private final Optional<String> user;
  private final Map<String, StepActivationCriteria> stepActivation;
  private final Set<String> depends;
  private final Set<String> provides;

  @JsonCreator
  public BuildConfig(@JsonProperty("steps") List<BuildStep> steps,
                     @JsonProperty("before") List<BuildStep> before,
                     @JsonProperty("env") Map<String, String> env,
                     @JsonProperty("buildDeps") List<String> buildDeps,
                     @JsonProperty("webhooks") List<String> webhooks,
                     @JsonProperty("cache") List<String> cache,
                     @JsonProperty("buildpack") Optional<GitInfo> buildpack,
                     @JsonProperty("user") Optional<String> user,
                     @JsonProperty("stepActivation") Map<String, StepActivationCriteria> stepActivation,
                     @JsonProperty("depends") Set<String> depends,
                     @JsonProperty("provides") Set<String> provides) {
    this.steps = Objects.firstNonNull(steps, Collections.<BuildStep>emptyList());
    this.before = Objects.firstNonNull(before, Collections.<BuildStep>emptyList());
    this.env = Objects.firstNonNull(env, Collections.<String,String>emptyMap());
    this.buildDeps = Objects.firstNonNull(buildDeps, Collections.<String>emptyList());
    this.webhooks = Objects.firstNonNull(webhooks, Collections.<String>emptyList());
    this.buildpack = Objects.firstNonNull(buildpack, Optional.<GitInfo>absent());
    this.cache = Objects.firstNonNull(cache, Collections.<String>emptyList());
    this.user = Objects.firstNonNull(user, Optional.<String>absent());
    this.stepActivation = Objects.firstNonNull(stepActivation, Collections.<String, StepActivationCriteria>emptyMap());
    this.depends = Objects.firstNonNull(depends, Collections.<String>emptySet());
    this.provides = Objects.firstNonNull(provides, Collections.<String>emptySet());
  }

  public static BuildConfig makeDefaultBuildConfig(){
    return new BuildConfig(null, null, null, null, null, null, null, null, null, null, null);
  }

  public List<BuildStep> getSteps() {
    return steps;
  }

  public List<BuildStep> getBefore() {
    return before;
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

  public Optional<String> getUser() {
    return user;
  }

  public Map<String, StepActivationCriteria> getStepActivation() {
    return stepActivation;
  }

  public Set<String> getProvides() {
    return provides;
  }

  public Set<String> getDepends() {
    return depends;
  }

  public BuildConfig withUser(String user) {
    return new BuildConfig(steps, before, env, buildDeps, webhooks, cache, buildpack, Optional.of(user), stepActivation, depends, provides);
  }

}
