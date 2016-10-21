package com.hubspot.blazar.data.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.hubspot.blazar.base.BuildOptions;
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuildInfo;
import com.hubspot.blazar.base.ModuleState;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.branch.BranchStatus;
import com.hubspot.blazar.data.dao.BranchDao;
import com.hubspot.blazar.data.dao.RepositoryBuildDao;
import com.hubspot.blazar.data.dao.StateDao;
import com.hubspot.blazar.data.service.BranchStatusService;


public class BranchStatusServiceTest {
  private BuildTrigger manualTrigger = BuildTrigger.forUser("user");
  private BuildOptions defaultOptions = BuildOptions.defaultOptions();
  private GitInfo branch1 = new GitInfo(Optional.of(1), "git.example.com", "example", "example", 1337, "master", true, System.currentTimeMillis(), System.currentTimeMillis());
  private GitInfo branch2 = new GitInfo(Optional.of(2), "git.example.com", "example", "example", 1337, "notMaster", true, System.currentTimeMillis(), System.currentTimeMillis());
  private Module module1 = new Module(Optional.of(1), "module", "config", "/", "/*", true, System.currentTimeMillis(), System.currentTimeMillis(), Optional.absent());
  private ModuleBuild moduleBuild1 = ModuleBuild.newBuilder(1, 1, 1, ModuleBuild.State.SUCCEEDED).build();
  private ModuleBuild moduleBuild2 = ModuleBuild.newBuilder(2, 1, 2, ModuleBuild.State.SKIPPED).build();
  private ModuleBuild moduleBuild3 = ModuleBuild.newBuilder(3, 1, 3, ModuleBuild.State.FAILED).build();
  private ModuleBuild moduleBuild4pending = ModuleBuild.newBuilder(4, 1, 4, ModuleBuild.State.QUEUED).build();
  private ModuleBuild moduleBuild4launching = ModuleBuild.newBuilder(4, 1, 4, ModuleBuild.State.LAUNCHING).build();
  // #5 does not exist repo build #5 is queued
  private RepositoryBuild branchBuild1 = RepositoryBuild.newBuilder(1, 1, RepositoryBuild.State.SUCCEEDED, manualTrigger, defaultOptions).build();
  private ModuleBuildInfo moduleBuildInfo1 = new ModuleBuildInfo(moduleBuild1, branchBuild1);
  private RepositoryBuild branchBuild2 = RepositoryBuild.newBuilder(1, 2, RepositoryBuild.State.SUCCEEDED, manualTrigger, defaultOptions).build();
  private ModuleBuildInfo moduleBuildInfo2 = new ModuleBuildInfo(moduleBuild2, branchBuild2);
  private RepositoryBuild branchBuild3 = RepositoryBuild.newBuilder(1, 3, RepositoryBuild.State.FAILED, manualTrigger, defaultOptions).build();
  private ModuleBuildInfo moduleBuildInfo3 = new ModuleBuildInfo(moduleBuild3, branchBuild3);
  private RepositoryBuild branchBuild4 = RepositoryBuild.newBuilder(1, 4, RepositoryBuild.State.LAUNCHING, manualTrigger, defaultOptions).build();
  private ModuleBuildInfo moduleBuildInfo4 = new ModuleBuildInfo(moduleBuild4launching, branchBuild4);
  private RepositoryBuild branchBuild5 = RepositoryBuild.newBuilder(1, 5, RepositoryBuild.State.QUEUED, manualTrigger, defaultOptions).build();
  private RepositoryBuild branchBuild6 = RepositoryBuild.newBuilder(1, 6, RepositoryBuild.State.QUEUED, manualTrigger, defaultOptions).build();


  private BranchDao branchDao = mock(BranchDao.class);
  private StateDao stateDao = mock(StateDao.class);
  private RepositoryBuildDao branchBuildDao = mock(RepositoryBuildDao.class);
  private BranchStatusService branchStatusService = new BranchStatusService(branchDao, stateDao, branchBuildDao);

  @Before
  public void before() {
    when(branchDao.get(Matchers.eq(1))).thenReturn(Optional.of(branch1));
    when(branchDao.getByRepository(Matchers.eq(1337))).thenReturn(Sets.newHashSet(branch1, branch2));
    when(stateDao.getLastSuccessfulAndNonSkippedBuildInfos(Matchers.eq(1)))
        .thenReturn(ImmutableSet.of(moduleBuildInfo1, moduleBuildInfo3));
    when(branchBuildDao.getRepositoryBuildsByState(Matchers.eq(1), any()))
        .thenReturn(ImmutableSet.of(branchBuild5, branchBuild6));
  }

  @Test
  public void itReturnsTheRightDataWithPendingModule() {
    when(stateDao.getPartialModuleStatesForBranch(Matchers.eq(1)))
        .thenReturn(ImmutableSet.of(ModuleState.newBuilder(module1)
        .setLastBranchBuild(Optional.of(branchBuild3))
        .setLastModuleBuild(Optional.of(moduleBuild3))
        .setPendingBranchBuild(Optional.of(branchBuild4))
        .setPendingModuleBuild(Optional.of(moduleBuild4pending))
        .build()));
    ModuleState expectedState = ModuleState.newBuilder(module1)
        .setLastBranchBuild(Optional.of(branchBuild3))
        .setLastModuleBuild(Optional.of(moduleBuild3))
        .setLastSuccessfulModuleBuild(Optional.of(moduleBuild1))
        .setLastSuccessfulBranchBuild(Optional.of(branchBuild1))
        .setLastNonSkippedModuleBuild(Optional.of(moduleBuild3))
        .setLastNonSkippedBranchBuild(Optional.of(branchBuild3))
        .setPendingBranchBuild(Optional.of(branchBuild4))
        .setPendingModuleBuild(Optional.of(moduleBuild4pending))
        .build();

    Optional<BranchStatus> status = branchStatusService.getBranchStatusById(1);
    assertThat(status.isPresent()).isTrue();
    assertThat(status.get().getQueuedBuilds()).isEqualTo(ImmutableList.of(branchBuild5, branchBuild6));
    assertThat(status.get().getOtherBranches()).doesNotContain(branch1).contains(branch2);
    assertThat(status.get().getModuleStates()).contains(expectedState);
  }


  @Test
  public void itReturnsTheRightDataWithLaunchingModule() {

    when(stateDao.getPartialModuleStatesForBranch(Matchers.eq(1)))
        .thenReturn(ImmutableSet.of(ModuleState.newBuilder(module1)
        .setLastBranchBuild(Optional.of(branchBuild3))
        .setLastModuleBuild(Optional.of(moduleBuild3))
        .setInProgressBranchBuild(Optional.of(branchBuild4))
        .setInProgressModuleBuild(Optional.of(moduleBuild4launching))
        .build()));
    ModuleState expectedState = ModuleState.newBuilder(module1)
        .setLastBranchBuild(Optional.of(branchBuild3))
        .setLastModuleBuild(Optional.of(moduleBuild3))
        .setLastSuccessfulModuleBuild(Optional.of(moduleBuild1))
        .setLastSuccessfulBranchBuild(Optional.of(branchBuild1))
        .setLastNonSkippedModuleBuild(Optional.of(moduleBuild3))
        .setLastNonSkippedBranchBuild(Optional.of(branchBuild3))
        .setInProgressBranchBuild(Optional.of(branchBuild4))
        .setInProgressModuleBuild(Optional.of(moduleBuild4launching))
        .build();

    Optional<BranchStatus> status = branchStatusService.getBranchStatusById(1);
    assertThat(status.isPresent()).isTrue();
    assertThat(status.get().getQueuedBuilds()).isEqualTo(ImmutableList.of(branchBuild5, branchBuild6));
    assertThat(status.get().getOtherBranches()).doesNotContain(branch1).contains(branch2);
    assertThat(status.get().getModuleStates()).contains(expectedState);
  }

  @Test
  public void itReturnsAbsentForMissingBranch() {
    // there is no branch 2
    when(branchDao.get(Matchers.eq(2))).thenReturn(Optional.absent());
    Optional<BranchStatus> status = branchStatusService.getBranchStatusById(2);
    assertThat(status.isPresent()).isFalse();
  }
}
