package com.hubspot.blazar.listener;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.hubspot.blazar.base.BuildOptions;
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.MalformedFile;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.data.service.DependenciesService;
import com.hubspot.blazar.data.service.InterProjectBuildMappingService;
import com.hubspot.blazar.data.service.InterProjectBuildService;
import com.hubspot.blazar.data.service.MalformedFileService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.util.GitHubHelper;

public class LaunchingRepositoryBuildVisitorTest {

  private static final RepositoryBuildService repositoryBuildService = mock(RepositoryBuildService.class);
  private static final ModuleBuildService moduleBuildService = mock(ModuleBuildService.class);
  private static final MalformedFileService malformedFileService = mock(MalformedFileService.class);
  private static final InterProjectBuildService interProjectBuildService = mock(InterProjectBuildService.class);
  private static final InterProjectBuildMappingService interProjectBuildMappingService = mock(InterProjectBuildMappingService.class);
  private static final ModuleService moduleService = mock(ModuleService.class);
  private static final DependenciesService dependenciesService = mock(DependenciesService.class);
  private static final GitHubHelper gitHubHelper = mock(GitHubHelper.class);

  private static final Module activeModule = new Module(Optional.of(1), "activeModule", "config", "/activeModule", "/activeModule/*", true, System.currentTimeMillis(), System.currentTimeMillis(), Optional.absent());
  private static final Module inActiveModule = new Module(Optional.of(2), "inActiveModule", "config", "/inActiveModule", "/inActiveModule/*", false, System.currentTimeMillis(), System.currentTimeMillis(), Optional.absent());
  private static final Map<Integer, Set<Integer>> dependencyMap = ImmutableMap.of(
      1, ImmutableSet.of(),
      2, ImmutableSet.of());
  private static final RepositoryBuild launchingBuild =
        RepositoryBuild.newBuilder(1, 1, RepositoryBuild.State.LAUNCHING, BuildTrigger.forUser("example"), new BuildOptions(ImmutableSet.of(1), BuildOptions.BuildDownstreams.WITHIN_REPOSITORY, false))
            .setId(Optional.of(1L))
            .setDependencyGraph(Optional.of(new DependencyGraph(dependencyMap, ImmutableList.of(1, 2))))
        .build();

  private static final LaunchingRepositoryBuildVisitor buildVisitor = new LaunchingRepositoryBuildVisitor(
      repositoryBuildService,
      moduleBuildService,
      malformedFileService,
      interProjectBuildService,
      interProjectBuildMappingService,
      moduleService,
      dependenciesService,
      gitHubHelper);

  @Test
  public void itFailsBuildOfBranchWhenItHasMalformedFiles() throws Exception {
    when(malformedFileService.getMalformedFiles(anyInt()))
        .thenReturn(ImmutableSet.of(new MalformedFile(1, "config", "/.blazar.yaml", "this is a test malformed file")));
    when(moduleService.getByBranch(1)).thenReturn(ImmutableSet.of(activeModule, inActiveModule));

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
    when(moduleService.getByBranch(1)).thenReturn(ImmutableSet.of(inActiveModule));

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
    when(moduleService.getByBranch(1)).thenReturn(ImmutableSet.of(activeModule, inActiveModule));

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
    }).when(moduleBuildService).enqueue(anyObject(), anyObject());

    RepositoryBuild expectedRepositoryBuild = launchingBuild.toBuilder().setState(RepositoryBuild.State.IN_PROGRESS).build();

    buildVisitor.visitLaunching(launchingBuild);

    assertThat(savedRepositoryBuild).isNotNull();
    assertThat(savedRepositoryBuild[0]).isEqualTo(expectedRepositoryBuild);

    assertThat(modulesThatWereEnqueued).doesNotContain(inActiveModule);
    assertThat(modulesThatWereEnqueued).isEqualTo(ImmutableList.of(activeModule));
  }
}
