package com.hubspot.blazar.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.hubspot.blazar.base.BuildConfig;
import com.hubspot.blazar.base.BuildStep;
import com.hubspot.blazar.base.Dependency;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.StepActivationCriteria;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.ExecutorConfiguration;
import com.hubspot.blazar.exception.NonRetryableBuildException;
import com.hubspot.blazar.external.models.singularity.BuildCGroupResources;

public class BlazarConfigUtilsTest {

  private static final String DEFAULT_BUILD_USER = "DefaultBuildUser";
  private static final BuildCGroupResources DEFAULT_BUILD_BUILD_CGROUP_RESOURCES = new BuildCGroupResources(2, 2560);
  private static final long DEFAULT_BUILD_TIMEOUT = 1000L;
  private static final GitInfo PRIMARY_BRANCH = GitInfo.fromString("git.example.com/TestOrg/repo.git#primary");
  private static final GitInfo SECONDARY_BRANCH = GitInfo.fromString("git.example.com/TestOrg/repo.git#secondary");

  private static final GitHubHelper gitHubHelper = mock(GitHubHelper.class);
  private static final BlazarConfiguration blazarConfiguration = mock(BlazarConfiguration.class);
  private static final ExecutorConfiguration exexutorConfiguration = new ExecutorConfiguration(Optional.of(DEFAULT_BUILD_USER), Optional.of(DEFAULT_BUILD_BUILD_CGROUP_RESOURCES), Optional.of(DEFAULT_BUILD_TIMEOUT));
  private static final BuildConfigUtils configUtils = new BuildConfigUtils(blazarConfiguration, gitHubHelper);

  private static final BuildConfig primaryConfig = BuildConfig.newBuilder()
      .setSteps(ImmutableList.of(BuildStep.fromString("echo hi")))
      .setBefore(ImmutableList.of(BuildStep.fromString("echo before - primary")))
      .setAfter(Optional.absent())
      .setEnv(ImmutableMap.of("VAR", "primary"))
      .setBuildDeps(ImmutableList.of("dep-1"))
      .setCache(ImmutableList.of("primary"))
      .setBuildpack(Optional.of(PRIMARY_BRANCH))
      .setUser(Optional.of("primary-build-user"))
      .setStepActivation(ImmutableMap.of("step-one", new StepActivationCriteria(ImmutableSet.of("master"))))
      .setBuildResources(Optional.of(new BuildCGroupResources(100, 100L)))
      .setDepends(ImmutableSet.of(Dependency.fromString("primary-dep")))
      .setProvides(ImmutableSet.of(Dependency.fromString("primary")))
      .build();

  private static final BuildConfig secondaryConfig = BuildConfig.newBuilder()
      .setSteps(ImmutableList.of(BuildStep.fromString("echo hi")))
      .setBefore(ImmutableList.of(BuildStep.fromString("echo before - secondary")))
      .setAfter(Optional.absent())
      .setEnv(ImmutableMap.of("VAR", "secondary", "VAR2", "secondary"))
      .setBuildDeps(ImmutableList.of("dep-2"))
      .setCache(ImmutableList.of("secondary"))
      .setBuildpack(Optional.of(SECONDARY_BRANCH))
      .setUser(Optional.of("secondary-build-user"))
      .setStepActivation(ImmutableMap.of("step-one", new StepActivationCriteria(ImmutableSet.of("not-master")), "step-two", new StepActivationCriteria(ImmutableSet.of("master"))))
      .setBuildResources(Optional.of(new BuildCGroupResources(200, 200L)))
      .setDepends(ImmutableSet.of(Dependency.fromString("secondary-dep")))
      .setProvides(ImmutableSet.of(Dependency.fromString("secondary")))
      .build();

  private static final BuildConfig primaryConfigWithoutOptions = BuildConfig.makeDefaultBuildConfig();

  @Before
  public void before() {
    when(blazarConfiguration.getExecutorConfiguration()).thenReturn(exexutorConfiguration);
  }

  @Test
  public void itPrefersPrimaryConfigForNonMergableFieldsWhenMergingConfigs() {
    BuildConfig mergedConfig = configUtils.mergeConfig(primaryConfig, secondaryConfig);
    assertThat(mergedConfig.getSteps()).isEqualTo(primaryConfig.getSteps());
    assertThat(mergedConfig.getBefore()).isEqualTo(primaryConfig.getBefore());
    assertThat(mergedConfig.getAfter()).isEqualTo(primaryConfig.getAfter());
    assertThat(mergedConfig.getBuildpack()).isEqualTo(primaryConfig.getBuildpack());
    assertThat(mergedConfig.getUser()).isEqualTo(primaryConfig.getUser());
    assertThat(mergedConfig.getBuildResources()).isEqualTo(primaryConfig.getBuildResources());
  }

  @Test
  public void itFallsBackToSecondaryConfigWhenOptionNotPresentInPrimary() {
    BuildConfig mergedConfig = configUtils.mergeConfig(primaryConfigWithoutOptions, secondaryConfig);
    assertThat(mergedConfig).isEqualTo(secondaryConfig);
  }

  @Test
  public void itMergesCollectionFieldsPresentInBothConfigs() {
    // Caveat we don't merge `steps` or `before` and they are collections.
    BuildConfig mergedConfig = configUtils.mergeConfig(primaryConfig, secondaryConfig);

    // Union different keys, prefer primary for same keys
    assertThat(mergedConfig.getEnv().get("VAR")).isEqualTo("primary"); // in both configs primary has precedence
    assertThat(mergedConfig.getEnv().get("VAR2")).isEqualTo("secondary"); // only in 2nd config is present

    // Union
    assertThat(mergedConfig.getBuildDeps().containsAll(primaryConfig.getBuildDeps())).isTrue();
    assertThat(mergedConfig.getBuildDeps().containsAll(secondaryConfig.getBuildDeps())).isTrue();

    // Union
    assertThat(mergedConfig.getCache().containsAll(primaryConfig.getCache())).isTrue();
    assertThat(mergedConfig.getCache().containsAll(secondaryConfig.getCache())).isTrue();

    // Union different keys, prefer primary for same keys
    assertThat(mergedConfig.getStepActivation().get("step-one")).isEqualTo(new StepActivationCriteria(ImmutableSet.of("master")));
    assertThat(mergedConfig.getStepActivation().get("step-two")).isEqualTo(new StepActivationCriteria(ImmutableSet.of("master")));

    // Union
    assertThat(mergedConfig.getDepends().containsAll(primaryConfig.getDepends())).isTrue();
    assertThat(mergedConfig.getDepends().containsAll(secondaryConfig.getDepends())).isTrue();

    // Union
    assertThat(mergedConfig.getProvides().containsAll(primaryConfig.getProvides())).isTrue();
    assertThat(mergedConfig.getProvides().containsAll(secondaryConfig.getProvides())).isTrue();

  }

  @Test
  public void itReturnsDefaultConfigIfConfigNotFound() throws IOException, NonRetryableBuildException {
    when(gitHubHelper.configAtSha(anyString(), any(), anyString())).thenReturn(Optional.absent());
    BuildConfig config = configUtils.getConfigAtRefOrDefault(PRIMARY_BRANCH, BuildConfigUtils.BUILDPACK_FILE, "master");
    assertThat(config).isEqualTo(BuildConfig.makeDefaultBuildConfig());
  }
}
