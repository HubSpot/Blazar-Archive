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
import com.hubspot.blazar.external.models.singularity.Resources;

@Singleton
public class BuildConfigUtils {

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
    return getConfigAtRefOrDefault(gitInfo, ".blazar-buildpack.yaml", gitInfo.getBranch());
  }

  public BuildConfig ensureDefaultConfigurationForBuild(BuildConfig buildConfig) {
    BuildConfig.Builder buildConfigWithDefaults = buildConfig.toBuilder();
    if (!buildConfigWithDefaults.getUser().isPresent()) {
      buildConfigWithDefaults = buildConfigWithDefaults.setUser(Optional.of(executorConfiguration.getDefaultBuildUser()));
    }

    if (!buildConfigWithDefaults.getBuildResources().isPresent()) {
      buildConfigWithDefaults = buildConfigWithDefaults.setBuildResources(executorConfiguration.getDefaultBuildResources());
    }
    return buildConfigWithDefaults.build();
  }


  public BuildConfig mergeConfig(BuildConfig primary, BuildConfig secondary) {
    List<BuildStep> steps = primary.getSteps().isEmpty() ? secondary.getSteps() : primary.getSteps();
    List<BuildStep> before = primary.getBefore().isEmpty() ? secondary.getBefore() : primary.getBefore();
    Optional<PostBuildSteps> after = (!primary.getAfter().isPresent()) ? secondary.getAfter() : primary.getAfter();
    Map<String, String> env = new LinkedHashMap<>();
    env.putAll(secondary.getEnv());
    env.putAll(primary.getEnv());
    List<String> buildDeps = Lists.newArrayList(Iterables.concat(secondary.getBuildDeps(), primary.getBuildDeps()));
    List<String> webhooks = Lists.newArrayList(Iterables.concat(secondary.getWebhooks(), primary.getWebhooks()));
    List<String> cache = Lists.newArrayList(Iterables.concat(secondary.getCache(), primary.getCache()));
    final String user;
    if (primary.getUser().isPresent()) {
      user = primary.getUser().get();
    } else {
      user = secondary.getUser().get();
    }

    Map<String, StepActivationCriteria> stepActivation = new LinkedHashMap<>();
    stepActivation.putAll(secondary.getStepActivation());
    stepActivation.putAll(primary.getStepActivation());

    final Optional<Resources> buildResources;
    if (primary.getBuildResources().isPresent()) {
      buildResources = primary.getBuildResources();
    } else {
      buildResources = secondary.getBuildResources();
    }

    Set<Dependency> depends = new HashSet<>();
    depends.addAll(primary.getDepends());
    depends.addAll(secondary.getDepends());

    Set<Dependency> provides = new HashSet<>();
    provides.addAll(primary.getDepends());
    provides.addAll(secondary.getDepends());

    return new BuildConfig(steps, before, after, env, buildDeps, webhooks, cache, Optional.<GitInfo>absent(), Optional.of(user), stepActivation, buildResources, depends, provides);
  }

  public BuildConfig getConfigAtRefOrDefault(GitInfo gitInfo, String configPath, String ref) throws IOException, NonRetryableBuildException {

    String repositoryName = gitInfo.getFullRepositoryName();
    LOG.info("Going to fetch config for path {} in repo {}@{}", configPath, repositoryName, gitInfo.getBranch());

    try {
      return gitHubHelper.configAtSha(configPath, gitInfo, ref).or(BuildConfig.makeDefaultBuildConfig());
    } catch (JsonProcessingException e) {
      String message = String.format("Invalid config found for path %s in repo %s@%s, failing build", configPath, repositoryName, gitInfo.getBranch());
      throw new NonRetryableBuildException(message, e);
    } catch (FileNotFoundException e) {
      String message = String.format("No repository found for %s", gitInfo.getFullRepositoryName());
      throw new NonRetryableBuildException(message, e);
    }
  }
}
