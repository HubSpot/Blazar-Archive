package com.hubspot.blazar.data.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.hubspot.blazar.base.BuildOptions;
import com.hubspot.blazar.base.BuildMetadata;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.MalformedFile;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuildInfo;
import com.hubspot.blazar.base.ModuleState;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.branch.BranchStatus;
import com.hubspot.blazar.data.dao.BranchDao;
import com.hubspot.blazar.data.dao.MalformedFileDao;
import com.hubspot.blazar.data.dao.RepositoryBuildDao;
import com.hubspot.blazar.data.dao.StateDao;
import com.hubspot.blazar.data.service.BranchStatusService;


public class BranchStatusServiceTest {
  private static final BuildMetadata manualTrigger = BuildMetadata.manual(Optional.of("user"));
  private static final BuildOptions defaultOptions = BuildOptions.defaultOptions();
  private static final GitInfo branch1 = new GitInfo(Optional.of(1), "git.example.com", "example", "example", 1337, "master", true, System.currentTimeMillis(), System.currentTimeMillis());
  private static final GitInfo branch2 = new GitInfo(Optional.of(2), "git.example.com", "example", "example", 1337, "notMaster", true, System.currentTimeMillis(), System.currentTimeMillis());
  private static final Module module1 = new Module(Optional.of(1), "module", "config", "/", "/*", true, System.currentTimeMillis(), System.currentTimeMillis(), Optional.absent());
  private static final ModuleBuild module1Build1Succeded = ModuleBuild.newBuilder(1, 1, 1, ModuleBuild.State.SUCCEEDED).build();
  private static final ModuleBuild module1Build2Skipped = ModuleBuild.newBuilder(2, 1, 2, ModuleBuild.State.SKIPPED).build();
  private static final ModuleBuild module1Build3Failed = ModuleBuild.newBuilder(3, 1, 3, ModuleBuild.State.FAILED).build();
  private static final ModuleBuild module1Build4Queued = ModuleBuild.newBuilder(4, 1, 4, ModuleBuild.State.QUEUED).build();
  private static final ModuleBuild module1Build4Launching = ModuleBuild.newBuilder(4, 1, 4, ModuleBuild.State.LAUNCHING).build();
  // #5 does not exist repo build #5 is queued
  private static final RepositoryBuild branch1Build1 = RepositoryBuild.newBuilder(1, 1, RepositoryBuild.State.SUCCEEDED, manualTrigger, defaultOptions).build();
  private static final ModuleBuildInfo module1Build1Info = new ModuleBuildInfo(module1Build1Succeded, branch1Build1);
  private static final RepositoryBuild branch1Build2 = RepositoryBuild.newBuilder(1, 2, RepositoryBuild.State.SUCCEEDED, manualTrigger, defaultOptions).build();
  private static final ModuleBuildInfo module1Build2Info = new ModuleBuildInfo(module1Build2Skipped, branch1Build2);
  private static final RepositoryBuild branch1Build3 = RepositoryBuild.newBuilder(1, 3, RepositoryBuild.State.FAILED, manualTrigger, defaultOptions).build();
  private static final ModuleBuildInfo module1Build3Info = new ModuleBuildInfo(module1Build3Failed, branch1Build3);
  private static final RepositoryBuild branch1Build4 = RepositoryBuild.newBuilder(1, 4, RepositoryBuild.State.LAUNCHING, manualTrigger, defaultOptions).build();
  private static final ModuleBuildInfo module1Build4Info = new ModuleBuildInfo(module1Build4Launching, branch1Build4);
  private static final RepositoryBuild branch1Build5 = RepositoryBuild.newBuilder(1, 5, RepositoryBuild.State.QUEUED, manualTrigger, defaultOptions).build();
  private static final RepositoryBuild branch1Build6 = RepositoryBuild.newBuilder(1, 6, RepositoryBuild.State.QUEUED, manualTrigger, defaultOptions).build();

  private static final BranchDao branchDao = mock(BranchDao.class);
  private static final StateDao stateDao = mock(StateDao.class);
  private static final RepositoryBuildDao branchBuildDao = mock(RepositoryBuildDao.class);
  private static final MalformedFileDao malformedFileDao = mock(MalformedFileDao.class);
  private static final BranchStatusService branchStatusService = new BranchStatusService(branchDao, stateDao, malformedFileDao, branchBuildDao);

  private static final Set<MalformedFile> malformedFiles = ImmutableSet.of(new MalformedFile(1, "config", "/broken/.blazar.yaml", "Testing 123"));

  @Before
  public void before() {
    when(branchDao.get(eq(1))).thenReturn(Optional.of(branch1));
    when(branchDao.getByRepository(eq(1337))).thenReturn(Sets.newHashSet(branch1, branch2));
    when(stateDao.getLastSuccessfulAndNonSkippedModuleBuilds(eq(1)))
        .thenReturn(ImmutableSet.of(module1Build1Info, module1Build3Info));
    when(branchBuildDao.getRepositoryBuildsByState(eq(1), eq(ImmutableList.of(RepositoryBuild.State.QUEUED))))
        .thenReturn(ImmutableSet.of(branch1Build5, branch1Build6));
    when(branchBuildDao.getRepositoryBuildsByState(eq(1), eq(ImmutableList.of(RepositoryBuild.State.LAUNCHING, RepositoryBuild.State.IN_PROGRESS))))
        .thenReturn(ImmutableSet.of(branch1Build4));
    when(malformedFileDao.getMalformedFiles(eq(1)))
        .thenReturn(malformedFiles);
  }

  @Test
  public void itReturnsTheExpectedBranchStatusWhenAModuleBuildIsPendingAndBranchBuildsAreQueued() {
    when(stateDao.getLastAndInProgressAndPendingBuildsForBranchAndIncludedModules(eq(1)))
        .thenReturn(ImmutableSet.of(ModuleState.newBuilder(module1)
            .setLastBranchBuild(Optional.of(branch1Build3))
            .setLastModuleBuild(Optional.of(module1Build3Failed))
            .setPendingBranchBuild(Optional.of(branch1Build4))
            .setPendingModuleBuild(Optional.of(module1Build4Queued))
            .build()));
    ModuleState expectedState = ModuleState.newBuilder(module1)
        .setLastBranchBuild(Optional.of(branch1Build3))
        .setLastModuleBuild(Optional.of(module1Build3Failed))
        .setLastSuccessfulModuleBuild(Optional.of(module1Build1Succeded))
        .setLastSuccessfulBranchBuild(Optional.of(branch1Build1))
        .setLastNonSkippedModuleBuild(Optional.of(module1Build3Failed))
        .setLastNonSkippedBranchBuild(Optional.of(branch1Build3))
        .setPendingBranchBuild(Optional.of(branch1Build4))
        .setPendingModuleBuild(Optional.of(module1Build4Queued))
        .build();


    Optional<BranchStatus> status = branchStatusService.getBranchStatusById(1);
    assertThat(status.isPresent()).isTrue();
    assertThat(status.get().getQueuedBuilds()).isEqualTo(ImmutableList.of(branch1Build5, branch1Build6));
    assertThat(status.get().getOtherBranches()).doesNotContain(branch1).contains(branch2);
    assertThat(status.get().getModuleStates()).isEqualTo(ImmutableSet.of(expectedState));
    assertThat(status.get().getActiveBuild()).isEqualTo(Optional.of(branch1Build4));
    assertThat(status.get().getMalformedFiles()).isEqualTo(malformedFiles);
  }


  @Test
  public void itReturnsTheExpectedBranchStatusWhenAModuleBuildIsLaunchingAndBranchBuildsAreQueued() {

    when(stateDao.getLastAndInProgressAndPendingBuildsForBranchAndIncludedModules(eq(1)))
        .thenReturn(ImmutableSet.of(ModuleState.newBuilder(module1)
            .setLastBranchBuild(Optional.of(branch1Build3))
            .setLastModuleBuild(Optional.of(module1Build3Failed))
            .setInProgressBranchBuild(Optional.of(branch1Build4))
            .setInProgressModuleBuild(Optional.of(module1Build4Launching))
            .build()));
    ModuleState expectedState = ModuleState.newBuilder(module1)
        .setLastBranchBuild(Optional.of(branch1Build3))
        .setLastModuleBuild(Optional.of(module1Build3Failed))
        .setLastSuccessfulModuleBuild(Optional.of(module1Build1Succeded))
        .setLastSuccessfulBranchBuild(Optional.of(branch1Build1))
        .setLastNonSkippedModuleBuild(Optional.of(module1Build3Failed))
        .setLastNonSkippedBranchBuild(Optional.of(branch1Build3))
        .setInProgressBranchBuild(Optional.of(branch1Build4))
        .setInProgressModuleBuild(Optional.of(module1Build4Launching))
        .build();

    Optional<BranchStatus> status = branchStatusService.getBranchStatusById(1);
    assertThat(status.isPresent()).isTrue();
    assertThat(status.get().getQueuedBuilds()).isEqualTo(ImmutableList.of(branch1Build5, branch1Build6));
    assertThat(status.get().getOtherBranches()).doesNotContain(branch1).contains(branch2);
    assertThat(status.get().getModuleStates()).isEqualTo(ImmutableSet.of(expectedState));
    assertThat(status.get().getActiveBuild()).isEqualTo(Optional.of(branch1Build4));
    assertThat(status.get().getMalformedFiles()).isEqualTo(malformedFiles);
  }

  @Test
  public void itReturnsAbsentForMissingBranch() {
    // there is no branch 2
    when(branchDao.get(eq(2))).thenReturn(Optional.absent());
    Optional<BranchStatus> status = branchStatusService.getBranchStatusById(2);
    assertThat(status.isPresent()).isFalse();
  }
}
