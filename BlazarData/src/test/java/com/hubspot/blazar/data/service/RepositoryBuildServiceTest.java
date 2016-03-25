package com.hubspot.blazar.data.service;

import com.hubspot.blazar.base.BuildOptions;
import com.hubspot.blazar.base.BuildOptions.BuildDownstreams;
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.data.BlazarDataTestBase;
import com.hubspot.blazar.data.util.BuildNumbers;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class RepositoryBuildServiceTest extends BlazarDataTestBase {
  private RepositoryBuildService repositoryBuildService;

  private GitInfo branchOne;
  private GitInfo branchTwo;

  @Before
  public void before() {
    this.repositoryBuildService = getFromGuice(RepositoryBuildService.class);

    BranchService branchService = getFromGuice(BranchService.class);
    branchOne = branchService.upsert(GitInfo.fromString("github.com/HubSpot/Test#one"));
    branchTwo = branchService.upsert(GitInfo.fromString("github.com/HubSpot/Test#two"));
  }

  @Test
  public void itEnqueuesNewBuildWhenNonePresent() {
    BuildTrigger buildTrigger = BuildTrigger.forCommit("abc");
    BuildOptions buildOptions = new BuildOptions(Collections.singleton(123), BuildDownstreams.NONE);
    long id = repositoryBuildService.enqueue(branchOne, buildTrigger, buildOptions);

    RepositoryBuild repositoryBuild = repositoryBuildService.get(id).get();

    validateBuild(RepositoryBuild.queuedBuild(branchOne, buildTrigger, 1, buildOptions), repositoryBuild);

    BuildNumbers buildNumbers = repositoryBuildService.getBuildNumbers(branchOne.getId().get());
    assertThat(buildNumbers.getPendingBuildId().get()).isEqualTo(id);
    assertThat(buildNumbers.getPendingBuildNumber().get()).isEqualTo(1);
  }

  @Test
  public void itReturnsPendingBuildWhenPresent() {
    BuildTrigger buildTrigger = BuildTrigger.forCommit("abc");
    BuildOptions buildOptions = new BuildOptions(Collections.singleton(123), BuildDownstreams.NONE);
    long id = repositoryBuildService.enqueue(branchOne, buildTrigger, buildOptions);

    long otherId = repositoryBuildService.enqueue(branchOne, buildTrigger, buildOptions);
    assertThat(otherId).isEqualTo(id);
  }

  @Test
  public void itEnqueuesNewBuildForDifferentBranch() {
    BuildTrigger buildTriggerOne = BuildTrigger.forCommit("abc");
    BuildOptions buildOptionsOne = new BuildOptions(Collections.singleton(123), BuildDownstreams.NONE);
    long idOne = repositoryBuildService.enqueue(branchOne, buildTriggerOne, buildOptionsOne);

    BuildTrigger buildTriggerTwo = BuildTrigger.forCommit("def");
    BuildOptions buildOptionsTwo = new BuildOptions(Collections.singleton(456), BuildDownstreams.NONE);
    long idTwo = repositoryBuildService.enqueue(branchTwo, buildTriggerTwo, buildOptionsTwo);

    assertThat(idTwo).isNotEqualTo(idOne);

    RepositoryBuild repositoryBuild = repositoryBuildService.get(idTwo).get();

    validateBuild(RepositoryBuild.queuedBuild(branchTwo, buildTriggerTwo, 1, buildOptionsTwo), repositoryBuild);

    BuildNumbers buildNumbers = repositoryBuildService.getBuildNumbers(branchTwo.getId().get());
    assertThat(buildNumbers.getPendingBuildId().get()).isEqualTo(idTwo);
    assertThat(buildNumbers.getPendingBuildNumber().get()).isEqualTo(1);
  }



  private static void validateBuild(RepositoryBuild expected, RepositoryBuild actual) {
    assertThat(actual.getBranchId()).isEqualTo(expected.getBranchId());
    assertThat(actual.getBuildNumber()).isEqualTo(expected.getBuildNumber());
    assertThat(actual.getState()).isEqualTo(expected.getState());
    assertThat(actual.getBuildTrigger()).isEqualTo(expected.getBuildTrigger());
    assertThat(actual.getBuildOptions()).isEqualTo(expected.getBuildOptions());
    assertThat(actual.getStartTimestamp()).isEqualTo(expected.getStartTimestamp());
    assertThat(actual.getEndTimestamp()).isEqualTo(expected.getEndTimestamp());
    assertThat(actual.getSha()).isEqualTo(expected.getSha());
    assertThat(actual.getCommitInfo()).isEqualTo(expected.getCommitInfo());
    assertThat(actual.getDependencyGraph()).isEqualTo(expected.getDependencyGraph());
  }
}
