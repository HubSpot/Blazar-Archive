package com.hubspot.blazar.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.hubspot.blazar.base.BuildOptions;
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.DependenciesService;
import com.hubspot.blazar.data.service.ModuleDiscoveryService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.discovery.ModuleDiscovery;
import com.hubspot.blazar.github.GitHubProtos.Commit;

@RunWith(JukitoRunner.class)
public class RepositoryBuildLauncherTest {

  private final RepositoryBuildService repositoryBuildService = mock(RepositoryBuildService.class);
  private final BranchService branchService = mock(BranchService.class);
  private final ModuleService moduleService = mock(ModuleService.class);
  private final DependenciesService dependenciesService = mock(DependenciesService.class);
  private final ModuleDiscoveryService moduleDiscoveryService = mock(ModuleDiscoveryService.class);
  private final ModuleDiscovery moduleDiscovery = mock(ModuleDiscovery.class);
  private final GitHubHelper gitHubHelper = mock(GitHubHelper.class);

  @Test
  public void itUsesPreviousCommitWhenBuildIsPartOfInterProjectBuild() throws Exception {
    GitInfo branch = new GitInfo(Optional.of(1), "git.example.com", "example", "repository", 1, "master", true, 0L, 1L);

    String commitSha = "0000000000000000000000000000000000000000";
    Commit commit = Commit.newBuilder().setId(commitSha).build();
    CommitInfo commitInfo = new CommitInfo(commit, Optional.absent(), Collections.emptyList(), false);

    RepositoryBuild previousBuild = RepositoryBuild.newBuilder(1, 1, RepositoryBuild.State.SUCCEEDED, BuildTrigger.forCommit(commitSha), BuildOptions.defaultOptions())
        .setCommitInfo(Optional.of(commitInfo))
        .build();
    Optional<RepositoryBuild> previousBuildOptional = Optional.of(previousBuild);

    BuildOptions ipbBuildOptions = new BuildOptions(ImmutableSet.of(1), BuildOptions.BuildDownstreams.NONE, false);
    RepositoryBuild currentBuild = RepositoryBuild.queuedBuild(branch, BuildTrigger.forInterProjectBuild(1), 2, ipbBuildOptions);

    RepositoryBuildLauncher launcher = new RepositoryBuildLauncher(repositoryBuildService, branchService, moduleService, dependenciesService, moduleDiscoveryService, moduleDiscovery, gitHubHelper);

    doAnswer(invocation -> {
      Commit currentCommit = (Commit) invocation.getArguments()[1];
      Optional<Commit> previousCommit = (Optional<Commit>) invocation.getArguments()[2];
      assertThat(currentCommit.getId()).isEqualTo(commitSha);
      assertThat(previousCommit.isPresent()).isTrue();
      assertThat(previousCommit.get().getId()).isEqualTo(commitSha);
      return null;
    }).when(gitHubHelper).commitInfoFor(any(), any(), any());

    when(gitHubHelper.shaFor(any(), any())).thenThrow(new IllegalStateException("Previous build is present this should not be called"));

    launcher.calculateCommitInfoForBuild(branch, currentBuild, previousBuildOptional);
  }

  @Test
  public void itUsesNewCommitWhenBranchBuildIsNotPartOfAInterProjectBuild() throws Exception {
    GitInfo branch = new GitInfo(Optional.of(1), "git.example.com", "example", "repository", 1, "master", true, 0L, 1L);

    String initialCommitSha = "0000000000000000000000000000000000000000";
    String secondCommitSha = "1111111111111111111111111111111111111111";

    Commit initialCommit = Commit.newBuilder().setId(initialCommitSha).build();

    CommitInfo previousCommitInfo = new CommitInfo(initialCommit, Optional.absent(), Collections.emptyList(), false);
    RepositoryBuild previousBuild = RepositoryBuild.newBuilder(1, 1, RepositoryBuild.State.SUCCEEDED, BuildTrigger.forCommit(initialCommitSha), BuildOptions.defaultOptions())
        .setCommitInfo(Optional.of(previousCommitInfo))
        .build();
    Optional<RepositoryBuild> previousBuildOptional = Optional.of(previousBuild);


    RepositoryBuild currentBuild = RepositoryBuild.queuedBuild(branch, BuildTrigger.forCommit(secondCommitSha), 2, BuildOptions.defaultOptions());
    RepositoryBuildLauncher launcher = new RepositoryBuildLauncher(repositoryBuildService, branchService, moduleService, dependenciesService, moduleDiscoveryService, moduleDiscovery, gitHubHelper);

    GHRepository repository = mock(GHRepository.class);

    when(gitHubHelper.shaFor(any(), any())).thenReturn(Optional.of(secondCommitSha));
    when(gitHubHelper.repositoryFor(any())).thenReturn(repository);

    doAnswer(invocation -> {
      String sha = (String) invocation.getArguments()[0];
      return new GHCommit() {
        @Override
        public String getSHA1() {
          return sha;
        }
      };
    }).when(repository).getCommit(anyString());

    doAnswer(invocation -> {
      GHCommit commit = (GHCommit) invocation.getArguments()[0];
      return Commit.newBuilder().setId(commit.getSHA1()).build();
    }).when(gitHubHelper).toCommit(any());


    // Test that the method was called with the expected arguments.
    doAnswer(invocation -> {
      Commit currentCommit = (Commit) invocation.getArguments()[1];
      Optional<Commit> previousCommit = (Optional<Commit>) invocation.getArguments()[2];
      assertThat(currentCommit.getId()).isEqualTo(secondCommitSha);
      assertThat(previousCommit.isPresent()).isTrue();
      assertThat(previousCommit.get().getId()).isEqualTo(initialCommitSha);
      return null;
    }).when(gitHubHelper).commitInfoFor(any(), any(), any());


    launcher.calculateCommitInfoForBuild(branch, currentBuild, previousBuildOptional);
  }
}
