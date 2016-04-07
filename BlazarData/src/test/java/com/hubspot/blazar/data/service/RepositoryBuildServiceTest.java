package com.hubspot.blazar.data.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.BuildOptions;
import com.hubspot.blazar.base.BuildOptions.BuildDownstreams;
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.RepositoryBuild.State;
import com.hubspot.blazar.data.BlazarDataTestBase;
import com.hubspot.blazar.data.util.BuildNumbers;
import com.hubspot.blazar.github.GitHubProtos.Commit;

import static org.assertj.core.api.Assertions.assertThat;

public class RepositoryBuildServiceTest extends BlazarDataTestBase {
  private RepositoryBuildService repositoryBuildService;

  private GitInfo branchOne;
  private GitInfo branchTwo;
  private long buildIdOne;
  private long buildIdTwo;
  private BuildTrigger buildTriggerOne;
  private BuildTrigger buildTriggerTwo;
  private BuildOptions buildOptionsOne;
  private BuildOptions buildOptionsTwo;
  private CommitInfo commitInfoOne;
  private CommitInfo commitInfoTwo;
  private DependencyGraph dependencyGraphOne;
  private DependencyGraph dependencyGraphTwo;

  @Before
  public void before() {
    this.repositoryBuildService = getFromGuice(RepositoryBuildService.class);

    BranchService branchService = getFromGuice(BranchService.class);
    branchOne = branchService.upsert(GitInfo.fromString("github.com/HubSpot/Test#one"));
    branchTwo = branchService.upsert(GitInfo.fromString("github.com/HubSpot/Test#two"));

    Optional<Commit> absentCommit = Optional.absent();
    List<Commit> emptyCommits = Collections.emptyList();
    Set<Integer> emptyIntegers = Collections.emptySet();

    buildTriggerOne = BuildTrigger.forCommit("abc");
    buildOptionsOne = new BuildOptions(Collections.singleton(123), BuildDownstreams.NONE, true);
    buildIdOne = repositoryBuildService.enqueue(branchOne, buildTriggerOne, buildOptionsOne);
    commitInfoOne = new CommitInfo(Commit.newBuilder().setId("abc").build(), absentCommit, emptyCommits, false);
    dependencyGraphOne = new DependencyGraph(Collections.singletonMap(123, emptyIntegers), Collections.singletonList(123));

    buildTriggerTwo = BuildTrigger.forCommit("def");
    buildOptionsTwo = new BuildOptions(Collections.singleton(456), BuildDownstreams.NONE, false);
    buildIdTwo = repositoryBuildService.enqueue(branchTwo, buildTriggerTwo, buildOptionsTwo);
    commitInfoTwo = new CommitInfo(Commit.newBuilder().setId("def").build(), absentCommit, emptyCommits, false);
    dependencyGraphTwo = new DependencyGraph(Collections.singletonMap(456, emptyIntegers), Collections.singletonList(456));
  }

  @Test
  public void itEnqueuesNewBuildWhenNonePresent() {
    RepositoryBuild repositoryBuild = repositoryBuildService.get(buildIdOne).get();

    validateBuild(RepositoryBuild.queuedBuild(branchOne, buildTriggerOne, 1, buildOptionsOne), repositoryBuild);

    BuildNumbers buildNumbers = repositoryBuildService.getBuildNumbers(branchOne.getId().get());
    assertThat(buildNumbers.getPendingBuildId().get()).isEqualTo(buildIdOne);
    assertThat(buildNumbers.getPendingBuildNumber().get()).isEqualTo(1);
  }

  @Test
  public void itReturnsPendingBuildForSecondPushEvent() {
    long newBuildId = repositoryBuildService.enqueue(branchOne, buildTriggerTwo, buildOptionsTwo);
    assertThat(newBuildId).isEqualTo(buildIdOne);
  }

  @Test
  public void itEnqueuesNewBuildForManualTrigger() {
    BuildTrigger manualTrigger = BuildTrigger.forUser("test");
    long newBuildId = repositoryBuildService.enqueue(branchOne, manualTrigger, buildOptionsOne);
    assertThat(newBuildId).isNotEqualTo(buildIdOne);

    RepositoryBuild repositoryBuild = repositoryBuildService.get(newBuildId).get();

    validateBuild(RepositoryBuild.queuedBuild(branchOne, manualTrigger, 2, buildOptionsOne), repositoryBuild);

    BuildNumbers buildNumbers = repositoryBuildService.getBuildNumbers(branchOne.getId().get());
    assertThat(buildNumbers.getPendingBuildId().get()).isEqualTo(buildIdOne);
    assertThat(buildNumbers.getPendingBuildNumber().get()).isEqualTo(1);
  }

  @Test
  public void itEnqueuesNewBuildForDifferentBranch() {
    assertThat(buildIdTwo).isNotEqualTo(buildIdOne);

    RepositoryBuild repositoryBuild = repositoryBuildService.get(buildIdTwo).get();

    validateBuild(RepositoryBuild.queuedBuild(branchTwo, buildTriggerTwo, 1, buildOptionsTwo), repositoryBuild);

    BuildNumbers buildNumbers = repositoryBuildService.getBuildNumbers(branchTwo.getId().get());
    assertThat(buildNumbers.getPendingBuildId().get()).isEqualTo(buildIdTwo);
    assertThat(buildNumbers.getPendingBuildNumber().get()).isEqualTo(1);
  }

  @Test
  public void itBeginsPendingBuild() {
    RepositoryBuild pending = repositoryBuildService.get(buildIdOne).get();

    RepositoryBuild launching = pending.withState(State.LAUNCHING)
        .withStartTimestamp(123L)
        .withCommitInfo(commitInfoOne)
        .withDependencyGraph(dependencyGraphOne);

    repositoryBuildService.begin(launching);

    RepositoryBuild fetched = repositoryBuildService.get(buildIdOne).get();

    validateBuild(launching, fetched);

    BuildNumbers buildNumbers = repositoryBuildService.getBuildNumbers(branchOne.getId().get());
    assertThat(buildNumbers.getPendingBuildId().isPresent()).isFalse();
    assertThat(buildNumbers.getPendingBuildNumber().isPresent()).isFalse();
    assertThat(buildNumbers.getInProgressBuildId().get()).isEqualTo(buildIdOne);
    assertThat(buildNumbers.getInProgressBuildNumber().get()).isEqualTo(1);
  }

  @Test
  public void itMovesNextQueuedBuildIntoPendingSlotWhenBuildBegins() {
    BuildTrigger manualTrigger = BuildTrigger.forUser("test");
    long newBuildId = repositoryBuildService.enqueue(branchOne, manualTrigger, buildOptionsOne);
    assertThat(newBuildId).isNotEqualTo(buildIdOne);

    RepositoryBuild pending = repositoryBuildService.get(buildIdOne).get();

    RepositoryBuild launching = pending.withState(State.LAUNCHING)
        .withStartTimestamp(123L)
        .withCommitInfo(commitInfoOne)
        .withDependencyGraph(dependencyGraphOne);

    repositoryBuildService.begin(launching);

    RepositoryBuild fetched = repositoryBuildService.get(buildIdOne).get();

    validateBuild(launching, fetched);

    BuildNumbers buildNumbers = repositoryBuildService.getBuildNumbers(branchOne.getId().get());
    assertThat(buildNumbers.getPendingBuildId().get()).isEqualTo(newBuildId);
    assertThat(buildNumbers.getPendingBuildNumber().get()).isEqualTo(2);
    assertThat(buildNumbers.getInProgressBuildId().get()).isEqualTo(buildIdOne);
    assertThat(buildNumbers.getInProgressBuildNumber().get()).isEqualTo(1);
  }

  @Test
  public void itCancelsPendingBuild() {
    RepositoryBuild repositoryBuild = repositoryBuildService.get(buildIdOne).get();

    repositoryBuildService.cancel(repositoryBuild);

    assertThat(repositoryBuildService.get(buildIdOne).isPresent()).isFalse();

    BuildNumbers buildNumbers = repositoryBuildService.getBuildNumbers(branchOne.getId().get());
    assertThat(buildNumbers.getPendingBuildId().isPresent()).isFalse();
    assertThat(buildNumbers.getPendingBuildNumber().isPresent()).isFalse();
  }

  @Test
  public void itCancelsExtraQueuedBuildProperly() {
    BuildTrigger manualTrigger = BuildTrigger.forUser("test");
    long newBuildId = repositoryBuildService.enqueue(branchOne, manualTrigger, buildOptionsOne);
    assertThat(newBuildId).isNotEqualTo(buildIdOne);

    RepositoryBuild repositoryBuild = repositoryBuildService.get(newBuildId).get();

    repositoryBuildService.cancel(repositoryBuild);

    assertThat(repositoryBuildService.get(newBuildId).isPresent()).isFalse();

    BuildNumbers buildNumbers = repositoryBuildService.getBuildNumbers(branchOne.getId().get());
    assertThat(buildNumbers.getPendingBuildId().get()).isEqualTo(buildIdOne);
    assertThat(buildNumbers.getPendingBuildNumber().get()).isEqualTo(1);
  }

  @Test
  public void itMovesNextQueuedBuildIntoPendingSlotProperly() {
    BuildTrigger manualTrigger = BuildTrigger.forUser("test");
    long newBuildId = repositoryBuildService.enqueue(branchOne, manualTrigger, buildOptionsOne);
    assertThat(newBuildId).isNotEqualTo(buildIdOne);

    RepositoryBuild repositoryBuild = repositoryBuildService.get(buildIdOne).get();

    repositoryBuildService.cancel(repositoryBuild);

    assertThat(repositoryBuildService.get(buildIdOne).isPresent()).isFalse();

    BuildNumbers buildNumbers = repositoryBuildService.getBuildNumbers(branchOne.getId().get());
    assertThat(buildNumbers.getPendingBuildId().get()).isEqualTo(newBuildId);
    assertThat(buildNumbers.getPendingBuildNumber().get()).isEqualTo(2);
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
