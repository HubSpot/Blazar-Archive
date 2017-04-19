package com.hubspot.blazar.listener;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.hubspot.blazar.base.BuildConfig;
import com.hubspot.blazar.base.BuildOptions;
import com.hubspot.blazar.base.BuildMetadata;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.MalformedFile;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.DependenciesService;
import com.hubspot.blazar.data.service.InterProjectBuildMappingService;
import com.hubspot.blazar.data.service.InterProjectBuildService;
import com.hubspot.blazar.data.service.MalformedFileService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.exception.NonRetryableBuildException;
import com.hubspot.blazar.github.GitHubProtos;
import com.hubspot.blazar.util.BuildConfigUtils;
import com.hubspot.blazar.util.GitHubHelper;

public class LaunchingRepositoryBuildVisitorTest {

  private static final BranchService branchService = mock(BranchService.class);
  private static final BuildConfigUtils buildConfigUtils = mock(BuildConfigUtils.class);
  private static final RepositoryBuildService repositoryBuildService = mock(RepositoryBuildService.class);
  private static final ModuleBuildService moduleBuildService = mock(ModuleBuildService.class);
  private static final MalformedFileService malformedFileService = mock(MalformedFileService.class);
  private static final InterProjectBuildService interProjectBuildService = mock(InterProjectBuildService.class);
  private static final InterProjectBuildMappingService interProjectBuildMappingService = mock(InterProjectBuildMappingService.class);
  private static final ModuleService moduleService = mock(ModuleService.class);
  private static final DependenciesService dependenciesService = mock(DependenciesService.class);
  private static final GitHubHelper gitHubHelper = mock(GitHubHelper.class);

  private static final GitInfo branch = new GitInfo(Optional.of(1), "git.example.com", "example", "test", 1337, "master", true, 100L, 100L);
  private static final Module activeModule = new Module(Optional.of(1), "activeModule", "config", "/activeModule", "/activeModule/*", true, 100L, 100L, Optional.absent());
  private static final Module inactiveModule = new Module(Optional.of(2), "inactiveModule", "config", "/inactiveModule", "/inactiveModule/*", false, 100L, 100L, Optional.absent());
  private static final Map<Integer, Set<Integer>> dependencyMap = ImmutableMap.of(
      1, ImmutableSet.of(),
      2, ImmutableSet.of());
  private static final CommitInfo commitInfo = new CommitInfo(GitHubProtos.Commit.newBuilder().setId("0000000000000000000000000000000000000000").build(), Optional.absent(), Collections.emptyList(), false);
  private static final RepositoryBuild launchingBuild =
        RepositoryBuild.newBuilder(1, 1, RepositoryBuild.State.LAUNCHING, BuildMetadata.push("testUser"), new BuildOptions(ImmutableSet.of(1), BuildOptions.BuildDownstreams.WITHIN_REPOSITORY, false))
            .setId(Optional.of(1L))
            .setDependencyGraph(Optional.of(new DependencyGraph(dependencyMap, ImmutableList.of(1, 2))))
            .setCommitInfo(Optional.of(commitInfo))
        .build();

  private static final LaunchingRepositoryBuildVisitor buildVisitor = new LaunchingRepositoryBuildVisitor(
      repositoryBuildService,
      buildConfigUtils,
      branchService,
      moduleBuildService,
      malformedFileService,
      interProjectBuildService,
      interProjectBuildMappingService,
      moduleService,
      dependenciesService,
      gitHubHelper);

  @Before
  public void before() throws IOException, NonRetryableBuildException {
    when(branchService.get(anyInt())).thenReturn(Optional.of(branch));
    when(buildConfigUtils.getConfigAtRefOrDefault(anyObject(), anyString(), anyString())).thenReturn(BuildConfig.makeDefaultBuildConfig());
  }

  @Test
  public void itFailsBuildOfBranchWhenItHasMalformedFiles() throws Exception {
    when(malformedFileService.getMalformedFiles(anyInt()))
        .thenReturn(ImmutableSet.of(new MalformedFile(1, "config", "/.blazar.yaml", "this is a test malformed file")));
    when(moduleService.getByBranch(1)).thenReturn(ImmutableSet.of(activeModule, inactiveModule));

    boolean[] moduleBuildFailed = {false};
    doAnswer(invocation ->  {
      RepositoryBuild build = (RepositoryBuild) invocation.getArguments()[0];
      Module module = (Module) invocation.getArguments()[1];

      assertThat(module.equals(activeModule));
      assertThat(build.equals(launchingBuild));
      moduleBuildFailed[0] = true;
      return null;
    }).when(moduleBuildService).createFailedBuild(any(), any());

    boolean[] repoBuildFailed = {false};
    doAnswer(invocation -> {
      RepositoryBuild build = (RepositoryBuild) invocation.getArguments()[0];
      assertThat(build).isEqualTo(launchingBuild);
      repoBuildFailed[0] = true;

      return null;
    }).when(repositoryBuildService).fail(any());

    buildVisitor.visitLaunching(launchingBuild);
    assertThat(repoBuildFailed[0]).isTrue();
    assertThat(moduleBuildFailed[0]).isTrue();
  }

  @Test
  public void itFailsBuildOfBranchWithNoActiveModules() throws Exception {
    // no malformed files no active modules
    when(malformedFileService.getMalformedFiles(anyInt())).thenReturn(ImmutableSet.of());
    when(moduleService.getByBranch(1)).thenReturn(ImmutableSet.of(inactiveModule));

    boolean[] repoBuildFailed = {false};
    doAnswer(invocation -> {
      RepositoryBuild build = (RepositoryBuild) invocation.getArguments()[0];
      assertThat(build).isEqualTo(launchingBuild);
      repoBuildFailed[0] = true;

      return null;
    }).when(repositoryBuildService).fail(any());

    buildVisitor.visitLaunching(launchingBuild);
    assertThat(repoBuildFailed[0]).isTrue();
  }

  @Test
  public void itEnqueuesModuleBuildsAndUpdatesRepositoryBuildToLaunching() throws Exception {
    when(malformedFileService.getMalformedFiles(anyInt())).thenReturn(ImmutableSet.of());
    when(moduleService.getByBranch(1)).thenReturn(ImmutableSet.of(activeModule, inactiveModule));

    doThrow(new RuntimeException("Build should not have been failed")).when(repositoryBuildService).fail(any());

    RepositoryBuild[] savedRepositoryBuild = {null};
    doAnswer(invocation -> {
      savedRepositoryBuild[0] = (RepositoryBuild) invocation.getArguments()[0];
      return null;
    }).when(repositoryBuildService).update(anyObject());

    List<Module> modulesThatWereEnqueued = new ArrayList<>();
    doAnswer(invocation -> {
      modulesThatWereEnqueued.add((Module) invocation.getArguments()[1]);
      return null;
    }).when(moduleBuildService).enqueue(anyObject(), anyObject(), anyObject(), anyObject());

    RepositoryBuild expectedRepositoryBuild = launchingBuild.toBuilder().setState(RepositoryBuild.State.IN_PROGRESS).build();

    buildVisitor.visitLaunching(launchingBuild);

    assertThat(savedRepositoryBuild).isNotNull();
    assertThat(savedRepositoryBuild[0]).isEqualTo(expectedRepositoryBuild);

    assertThat(modulesThatWereEnqueued).doesNotContain(inactiveModule);
    assertThat(modulesThatWereEnqueued).isEqualTo(ImmutableList.of(activeModule));
  }
}
