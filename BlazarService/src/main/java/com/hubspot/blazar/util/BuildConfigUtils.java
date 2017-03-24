package com.hubspot.blazar.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.BuildConfig;
import com.hubspot.blazar.base.BuildStep;
import com.hubspot.blazar.base.Dependency;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.PostBuildSteps;
import com.hubspot.blazar.base.StepActivationCriteria;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.ExecutorConfiguration;
import com.hubspot.blazar.exception.NonRetryableBuildException;
import com.hubspot.blazar.external.models.singularity.BuildCGroupResources;

@Singleton
public class BuildConfigUtils {

  public static final String BUILDPACK_FILE = ".blazar-buildpack.yaml";
  private static final Logger LOG = LoggerFactory.getLogger(BuildConfigUtils.class);
  private final ExecutorConfiguration executorConfiguration;
  private final GitHubHelper gitHubHelper;

  @Inject
  public BuildConfigUtils(BlazarConfiguration blazarConfiguration,
                          GitHubHelper gitHubHelper) {
    this.executorConfiguration = blazarConfiguration.getExecutorConfiguration();
    this.gitHubHelper = gitHubHelper;
  }

  public BuildConfig getConfigForBuildpackOnBranch(GitInfo gitInfo) throws IOException, NonRetryableBuildException {
    return getConfigAtRefOrDefault(gitInfo, BUILDPACK_FILE, gitInfo.getBranch());
  }

  public BuildConfig addMissingBuildConfigSettings(BuildConfig buildConfig) {
    BuildConfig.Builder buildConfigWithDefaults = buildConfig.toBuilder();
    if (!buildConfig.getUser().isPresent()) {
      buildConfigWithDefaults = buildConfigWithDefaults.setUser(Optional.of(executorConfiguration.getDefaultBuildUser()));
    }

    if (!buildConfig.getBuildResources().isPresent()) {
      buildConfigWithDefaults = buildConfigWithDefaults.setBuildResources(executorConfiguration.getDefaultBuildResources());
    }
    return buildConfigWithDefaults.build();
  }

  /**
   * This merges 2 configurations (primary, secondary) into 1 resolved configuration that is used by a Blazar Executor to build
   *
   * Collections of `BuildSteps` are overridden.
   * List<BuildStep> steps
   *  Overridden so that child builds can define a new build.
   *
   * List<BuildStep> before
   *  Overridden, buildpacks should not define this so child builds can insert their own steps before the 'core' section of the build (`steps` section)
   *
   * Optional<PostBuildSteps> after
   *  Overridden, buildpacks should not define this so child builds can insert their own steps after the 'core' section of the build
   *
   * Map<String, String> env
   *  Merged preferring `primary` when there are duplicate values, this lets child builds decide how the build
   *  tools execute without having to replicate the whole build
   *
   * List<String> buildDeps
   *  Merged putting `primary` before `secondary`, We turn `buildDeps` into additional values on the $PATH,
   *  putting primary before secondary effectively overrides secondary values that provide binaries of the same name.
   *
   * List<String> webhooks
   *  Merged putting `primary` before `secondary`, This is not currently used by anything as Blazar does not have Webhooks.
   *
   * List<String> cache
   *  Merged putting `primary` before `secondary`, Primary caches will be stored first.
   *
   * Optional<GitInfo> buildpack
   *  We override because the child build should be able to specify its own buildpack.
   *
   * Optional<String> user
   *  We override because the child build should be able to specify its own user
   *
   * Map<String, StepActivationCriteria> stepActivation
   *  Merged preferring `primary` when there are duplicate values, duplicate values, this lets child builds decide when
   *  the build tools execute without having to replicate the whole build
   *
   * Optional<BuildCGroupResources> buildResources
   *  Overridden allows child builds to specify different resource limits
   *
   * Set<Dependency> depends
   *  Merged allows child builds to specify their own additional dependencies
   *
   * Set<Dependency> provides
   *  Merged allows child builds to specify their own names to depend on
   *
   */
  public BuildConfig mergeConfig(BuildConfig primary, BuildConfig secondary) {
    List<BuildStep> steps = primary.getSteps().isEmpty() ? secondary.getSteps() : primary.getSteps();
    List<BuildStep> before = primary.getBefore().isEmpty() ? secondary.getBefore() : primary.getBefore();
    Optional<PostBuildSteps> after = (!primary.getAfter().isPresent()) ? secondary.getAfter() : primary.getAfter();
    Optional<GitInfo> buildpack = (!primary.getBuildpack().isPresent() ? secondary.getBuildpack() : primary.getBuildpack());
    Map<String, String> env = new LinkedHashMap<>();
    env.putAll(secondary.getEnv());
    env.putAll(primary.getEnv());
    List<String> buildDeps = Lists.newArrayList(Iterables.concat(secondary.getBuildDeps(), primary.getBuildDeps()));
    List<String> webhooks = Lists.newArrayList(Iterables.concat(secondary.getWebhooks(), primary.getWebhooks()));
    List<String> cache = Lists.newArrayList(Iterables.concat(secondary.getCache(), primary.getCache()));
    final Optional<String> user;
    if (primary.getUser().isPresent()) {
      user = primary.getUser();
    } else {
      user = secondary.getUser();
    }

    Map<String, StepActivationCriteria> stepActivation = new LinkedHashMap<>();
    stepActivation.putAll(secondary.getStepActivation());
    stepActivation.putAll(primary.getStepActivation());

    final Optional<BuildCGroupResources> buildResources;
    if (primary.getBuildResources().isPresent()) {
      buildResources = primary.getBuildResources();
    } else {
      buildResources = secondary.getBuildResources();
    }

    Set<Dependency> depends = new HashSet<>();
    depends.addAll(primary.getDepends());
    depends.addAll(secondary.getDepends());

    Set<Dependency> provides = new HashSet<>();
    provides.addAll(primary.getProvides());
    provides.addAll(secondary.getProvides());

    return new BuildConfig(steps, before, after, env, buildDeps, webhooks, cache, buildpack, user, stepActivation, buildResources, depends, provides);
  }

  public BuildConfig getConfigAtRefOrDefault(GitInfo gitInfo, String configPath, String ref) throws IOException, NonRetryableBuildException {

    String repositoryName = gitInfo.getFullRepositoryName();
    LOG.info("Going to fetch config for path {} in repo {}@{}", configPath, repositoryName, ref);

    try {
      return gitHubHelper.configAtSha(configPath, gitInfo, ref).or(BuildConfig.makeDefaultBuildConfig());
    } catch (JsonProcessingException e) {
      String message = String.format("Invalid config found for path %s in repo %s@%s, failing build", configPath, repositoryName, ref);
      throw new NonRetryableBuildException(message, e);
    } catch (FileNotFoundException e) {
      return BuildConfig.makeDefaultBuildConfig();
    }
  }
}
