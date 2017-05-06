package com.hubspot.blazar.base;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.hubspot.blazar.external.models.singularity.BuildCGroupResources;

public class BuildConfig {
  private final List<BuildStep> steps;
  private final List<BuildStep> before;
  private final Optional<PostBuildSteps> after;
  private final Map<String, String> env;
  private final List<String> buildDeps;
  private final List<String> webhooks;
  private final List<String> cache;
  private final Optional<GitInfo> buildpack;
  private final Optional<String> user;
  private final Map<String, StepActivationCriteria> stepActivation;
  private final Optional<BuildCGroupResources> buildResources;
  private final Set<Dependency> depends;
  private final Set<Dependency> provides;
  /**
   * When a build config that resides in a folder is disabled
   * all modules that have been auto-discovered are disabled, i.e. nothing under this folder will be built
   */
  private final boolean disabled;
  /**
   * The default behavior for each folder is to get the Union of the plugin-discovered module dependencies
   * with the dependencies specified in the build configuration file that resides inside the folder.
   * "ignorePluginDiscoveredDependencies" can be set to true to keep only the dependencies specified in the
   * build configuration file.
   *
   * If there is no build configuration file in the folder this setting will be ignored, i.e. if only plugin-discovered
   * dependencies exist those will always be used.
   */
  private final boolean ignorePluginDiscoveredDependencies;

  @JsonCreator
  public BuildConfig(@JsonProperty("steps") List<BuildStep> steps,
                     @JsonProperty("before") List<BuildStep> before,
                     @JsonProperty("after") Optional<PostBuildSteps> after,
                     @JsonProperty("env") Map<String, String> env,
                     @JsonProperty("buildDeps") List<String> buildDeps,
                     @JsonProperty("webhooks") List<String> webhooks,
                     @JsonProperty("cache") List<String> cache,
                     @JsonProperty("buildpack") Optional<GitInfo> buildpack,
                     @JsonProperty("user") Optional<String> user,
                     @JsonProperty("stepActivation") Map<String, StepActivationCriteria> stepActivation,
                     @JsonProperty("buildResources") Optional<BuildCGroupResources> buildResources,
                     @JsonProperty("depends") Set<Dependency> depends,
                     @JsonProperty("provides") Set<Dependency> provides,
                     @JsonProperty("disabled") boolean disabled,
                     @JsonProperty("ignorePluginDiscoveredDependencies") boolean ignorePluginDiscoveredDependencies) {
    this.steps = MoreObjects.firstNonNull(steps, Collections.<BuildStep>emptyList());
    this.before = MoreObjects.firstNonNull(before, Collections.<BuildStep>emptyList());
    this.after = MoreObjects.firstNonNull(after, Optional.<PostBuildSteps>absent());
    this.env = MoreObjects.firstNonNull(env, Collections.<String,String>emptyMap());
    this.buildDeps = MoreObjects.firstNonNull(buildDeps, Collections.<String>emptyList());
    this.webhooks = MoreObjects.firstNonNull(webhooks, Collections.<String>emptyList());
    this.buildpack = MoreObjects.firstNonNull(buildpack, Optional.<GitInfo>absent());
    this.cache = MoreObjects.firstNonNull(cache, Collections.<String>emptyList());
    this.user = MoreObjects.firstNonNull(user, Optional.<String>absent());
    this.stepActivation = MoreObjects.firstNonNull(stepActivation, Collections.<String, StepActivationCriteria>emptyMap());
    this.buildResources = MoreObjects.firstNonNull(buildResources, Optional.<BuildCGroupResources>absent());
    this.depends = MoreObjects.firstNonNull(depends, Collections.<Dependency>emptySet());
    this.provides = MoreObjects.firstNonNull(provides, Collections.<Dependency>emptySet());
    this.disabled = disabled;
    this.ignorePluginDiscoveredDependencies = ignorePluginDiscoveredDependencies;
  }

  public static BuildConfig makeDefaultBuildConfig(){
    return new BuildConfig(null, null, null, null, null, null,
        null, null, null, null, null, null,
        null, false, false);
  }

  public List<BuildStep> getSteps() {
    return steps;
  }

  public List<BuildStep> getBefore() {
    return before;
  }

  public Optional<PostBuildSteps> getAfter() {
    return after;
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

  public Optional<BuildCGroupResources> getBuildResources() {
    return buildResources;
  }

  public Set<Dependency> getProvides() {
    return provides;
  }

  public Set<Dependency> getDepends() {
    return depends;
  }

  public boolean isDisabled() {
    return disabled;
  }

  public boolean isIgnorePluginDiscoveredDependencies() {
    return ignorePluginDiscoveredDependencies;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BuildConfig that = (BuildConfig) o;
    return Objects.equals(steps, that.steps) &&
        Objects.equals(before, that.before) &&
        Objects.equals(after, that.after) &&
        Objects.equals(env, that.env) &&
        Objects.equals(buildDeps, that.buildDeps) &&
        Objects.equals(webhooks, that.webhooks) &&
        Objects.equals(cache, that.cache) &&
        Objects.equals(buildpack, that.buildpack) &&
        Objects.equals(user, that.user) &&
        Objects.equals(stepActivation, that.stepActivation) &&
        Objects.equals(buildResources, that.buildResources) &&
        Objects.equals(depends, that.depends) &&
        Objects.equals(provides, that.provides) &&
        Objects.equals(disabled, that.disabled) &&
        Objects.equals(ignorePluginDiscoveredDependencies, that.ignorePluginDiscoveredDependencies);
  }

  @Override
  public int hashCode() {
    return Objects.hash(steps, before, after, env, buildDeps, webhooks, cache, buildpack, user, stepActivation,
        buildResources, depends, provides, disabled, ignorePluginDiscoveredDependencies);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("steps", steps)
        .add("before", before)
        .add("after", after)
        .add("env", env)
        .add("buildDeps", buildDeps)
        .add("webhooks", webhooks)
        .add("cache", cache)
        .add("buildpack", buildpack)
        .add("user", user)
        .add("stepActivation", stepActivation)
        .add("buildResources", buildResources)
        .add("depends", depends)
        .add("provides", provides)
        .add("disabled", disabled)
        .toString();
  }

  public Builder toBuilder() {
    return newBuilder()
        .setSteps(steps)
        .setBefore(before)
        .setAfter(after)
        .setEnv(env)
        .setBuildDeps(buildDeps)
        .setWebhooks(webhooks)
        .setCache(cache)
        .setBuildpack(buildpack)
        .setUser(user)
        .setStepActivation(stepActivation)
        .setBuildResources(buildResources)
        .setDepends(depends)
        .setProvides(provides)
        .setDisabled(disabled);
  }

  public static Builder newBuilder() {
    return new Builder();
  }


  public static class Builder {
    private List<BuildStep> steps;
    private List<BuildStep> before;
    private Optional<PostBuildSteps> after;
    private Map<String, String> env;
    private List<String> buildDeps;
    private List<String> webhooks;
    private List<String> cache;
    private Optional<GitInfo> buildpack;
    private Optional<String> user;
    private Map<String, StepActivationCriteria> stepActivation;
    private Optional<BuildCGroupResources> buildResources;
    private Set<Dependency> depends;
    private Set<Dependency> provides;
    private boolean disabled;
    private boolean ignoreAutoDiscoveredDependencies;

    public BuildConfig build() {
      return new BuildConfig(steps, before, after, env, buildDeps, webhooks, cache, buildpack, user, stepActivation,
          buildResources, depends, provides, disabled, ignoreAutoDiscoveredDependencies);
    }

    public Builder setSteps(List<BuildStep> steps) {
      this.steps = steps;
      return this;
    }

    public Builder setBefore(List<BuildStep> before) {
      this.before = before;
      return this;
    }

    public Builder setAfter(Optional<PostBuildSteps> after) {
      this.after = after;
      return this;
    }

    public Builder setEnv(Map<String, String> env) {
      this.env = env;
      return this;
    }

    public Builder setBuildDeps(List<String> buildDeps) {
      this.buildDeps = buildDeps;
      return this;
    }

    public Builder setWebhooks(List<String> webhooks) {
      this.webhooks = webhooks;
      return this;
    }

    public Builder setCache(List<String> cache) {
      this.cache = cache;
      return this;
    }

    public Builder setBuildpack(Optional<GitInfo> buildpack) {
      this.buildpack = buildpack;
      return this;
    }

    public Builder setUser(Optional<String> user) {
      this.user = user;
      return this;
    }

    public Builder setStepActivation(Map<String, StepActivationCriteria> stepActivation) {
      this.stepActivation = stepActivation;
      return this;
    }

    public Builder setBuildResources(Optional<BuildCGroupResources> buildResources) {
      this.buildResources = buildResources;
      return this;
    }

    public Builder setDepends(Set<Dependency> depends) {
      this.depends = depends;
      return this;
    }

    public Builder setProvides(Set<Dependency> provides) {
      this.provides = provides;
      return this;
    }

    public Builder setDisabled(boolean disabled) {
      this.disabled = disabled;
      return this;
    }

    public Builder setIgnoreAutoDiscoveredDependencies(boolean ignoreAutoDiscoveredDependencies) {
      this.ignoreAutoDiscoveredDependencies = ignoreAutoDiscoveredDependencies;
      return this;
    }
  }
}
