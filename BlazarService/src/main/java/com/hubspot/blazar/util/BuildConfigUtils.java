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
    return getConfigAtRef(gitInfo, BUILDPACK_FILE, gitInfo.getBranch());
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
   * This merges the build configuration (.blazar.yaml) found inside a source folder (the primary configuration)
   * with a reusable configuration (the buildpack) which is either specified inside the primary configuration or
   * is auto-discovered by the module discovery plugins. The final resolved configuration is used by
   * the build container (Blazar Executor) to execute the build. The following are the rules for merging the primary
   * build config with the buildpack config
   *
   * Collections of `BuildSteps` are overridden.
   * List<BuildStep> steps
   * No merging. The steps in the primary config are used. If the primary config doesn't specify 'steps' the 'steps'
   * specified in the buildpack are used. This allows to specify the steps once in a buildpack and reuse them across builds
   * with the extra flexibility to completely replace the buildpack steps if required.
   *
   * List<BuildStep> before
   * No merging. The 'before' steps in the primary config are used. If the primary config doesn't specify 'before'
   * steps those specified in the buildpack are used (if any). This allows to specify once the core 'steps' in a
   * buildpack to reuse them across builds and then use the primary config to insert steps BEFORE the core 'steps'
   * if required.
   *
   * Optional<PostBuildSteps> after
   * No merging. The 'after' steps in the primary config are used. If the primary config doesn't specify 'after'
   * steps those specified in the buildpack are used (if any). This allows to specify once the core 'steps'
   * in a buildpack to reuse them across builds and then use the primary config to insert steps AFTER the core 'steps'
   * if required.
   *
   * Map<String, String> env
   *  The environment variable map in buildpack is merged with the environment variable map in primary config.
   *  The enviroment variables in the primary config override those in the buildpack when there are duplicate
   *  environment variables.
   *  In this way common environment variables can be kept in the buildpack and resused across builds. Users can add
   *  more or override the common environment variables by using the primary build config.
   *
   * List<String> buildDeps
   *  The two lists of build dependencies are concatenated putting the values in the `primary` config before the
   *  values in the buildpack. We turn `buildDeps` into additional values on the $PATH, (???????)
   *  putting primary before secondary effectively overrides secondary values that provide binaries of the same name (????).
   *
   * List<String> webhooks
   * The two lists of webhooks are concatenated.
   *
   * List<String> cache
   * The two lists of caches are concatenated putting the values in the `primary` config before the
   *  values in the buildpack which means that caches in the primary config will be strored first.
   *
   * DOES THIS MAKE ANY SENSE since buildpacks cannot be nested???
   * Optional<GitInfo> buildpack
   *  If the primary build config specifies a 'buildpack' and the buildpack itself specifies another 'buildpack' the
   *  buildpack in the primary config is kept....what is the purpose of doing that since we are not going to resolve the
   *  buildpack again? The buildpack in a primary build config is read outside of this method and is then passed to this method
   *  to be merged with the primary one. So this method which should probably ignore the buildpack field.
   *
   * Optional<String> user
   *  The user in the primary config overrides the user set in the buildpack.
   *
   * Map<String, StepActivationCriteria> stepActivation
   *  The two maps with stepActivationCriteria in the buildpack and in the primary config are merged.
   *  The criteria in the primary config override those in the buildpack when there are duplicate keys.
   *  In this way common criteria can be kept in the buildpack and resused across builds and then by using the primary
   *  build config users can add more or override the common criteria if required.
   *
   * Optional<BuildCGroupResources> buildResources
   * The resources specified in the buildpack are ignored if there are resources specified in the primary config.
   *
   * Set<Dependency> depends
   * The two sets of dependencies are merged.
   *
   * Set<Dependency> provides
   * The two sets of provided dependencies are merged.
   *
   * boolean disabled
   * This determines if building is completely disabled for the folder that the primary config is located is,
   * so the value in the primary config is always used and any value in the buildpack is completely ignored.
   * Actually if the primary config sets this to true (i.e. is disabled) the merging of the build configs is never
   * called since no building needs to be executed.
   *
   * boolean ignoreAutoDiscoveredDependencies
   * This determines whether dependencies specified in the buildpack should be ignored and only the dependencies
   * specified in the primary config should be used. Therefore the value in the primary config is always used
   * and any value in the builpack is ignored.
   *
   */
  public BuildConfig mergeBuildConfigs(BuildConfig primaryConfig, BuildConfig buildpackConfig) {
    List<BuildStep> steps = primaryConfig.getSteps().isEmpty() ? buildpackConfig.getSteps() : primaryConfig.getSteps();
    List<BuildStep> before = primaryConfig.getBefore().isEmpty() ? buildpackConfig.getBefore() : primaryConfig.getBefore();
    Optional<PostBuildSteps> after = (!primaryConfig.getAfter().isPresent()) ? buildpackConfig.getAfter() : primaryConfig.getAfter();
    Optional<GitInfo> buildpack = (!primaryConfig.getBuildpack().isPresent() ? buildpackConfig.getBuildpack() : primaryConfig.getBuildpack());
    Map<String, String> env = new LinkedHashMap<>();
    env.putAll(buildpackConfig.getEnv());
    env.putAll(primaryConfig.getEnv());
    List<String> buildDeps = Lists.newArrayList(Iterables.concat(buildpackConfig.getBuildDeps(), primaryConfig.getBuildDeps()));
    List<String> webhooks = Lists.newArrayList(Iterables.concat(buildpackConfig.getWebhooks(), primaryConfig.getWebhooks()));
    List<String> cache = Lists.newArrayList(Iterables.concat(buildpackConfig.getCache(), primaryConfig.getCache()));
    final Optional<String> user;
    if (primaryConfig.getUser().isPresent()) {
      user = primaryConfig.getUser();
    } else {
      user = buildpackConfig.getUser();
    }

    Map<String, StepActivationCriteria> stepActivation = new LinkedHashMap<>();
    stepActivation.putAll(buildpackConfig.getStepActivation());
    stepActivation.putAll(primaryConfig.getStepActivation());

    final Optional<BuildCGroupResources> buildResources;
    if (primaryConfig.getBuildResources().isPresent()) {
      buildResources = primaryConfig.getBuildResources();
    } else {
      buildResources = buildpackConfig.getBuildResources();
    }

    Set<Dependency> depends = new HashSet<>();
    depends.addAll(primaryConfig.getDepends());
    if (!primaryConfig.isIgnoreAutoDiscoveredDependencies()) {
      depends.addAll(buildpackConfig.getDepends());
    }

    Set<Dependency> provides = new HashSet<>();
    provides.addAll(primaryConfig.getProvides());
    if (!primaryConfig.isIgnoreAutoDiscoveredDependencies()) {
      provides.addAll(buildpackConfig.getProvides());
    }

    return new BuildConfig(steps, before, after, env, buildDeps, webhooks, cache, buildpack, user, stepActivation,
        buildResources, depends, provides, primaryConfig.isDisabled(), primaryConfig.isIgnoreAutoDiscoveredDependencies());
  }

  public BuildConfig getConfigAtRef(GitInfo gitInfo, String configPath, String ref) throws IOException, NonRetryableBuildException {

    String repositoryName = gitInfo.getFullRepositoryName();
    LOG.info("Going to fetch build config (buildpack) for path {} in repo {}@{}", configPath, repositoryName, ref);

    try {
      return gitHubHelper.configAtSha(configPath, gitInfo, ref).or(BuildConfig.makeDefaultBuildConfig());
    } catch (JsonProcessingException e) {
      String message = String.format("Invalid config found for path %s in repo %s@%s, failing build", configPath, repositoryName, ref);
      throw new NonRetryableBuildException(message, e);
    } catch (FileNotFoundException e) {
      // Doesn't seem a good idea to obscure the fact that the build pack is missing and leave users with the impression
      // that their buildpack is taken into account
      //return BuildConfig.makeDefaultBuildConfig();
      String message = String.format("The specified build config (buildpack) %s was not found in repo %s@%s.", configPath, repositoryName, ref);
      throw new NonRetryableBuildException(message, e);
    }
  }
}
