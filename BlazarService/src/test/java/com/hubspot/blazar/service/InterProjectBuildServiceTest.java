package com.hubspot.blazar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.github.BlazarGHChange;
import org.kohsuke.github.BlazarGHCommit;
import org.kohsuke.github.BlazarGHCommitFile;
import org.kohsuke.github.BlazarGHCommitShortInfo;
import org.kohsuke.github.BlazarGHContent;
import org.kohsuke.github.BlazarGHRepository;
import org.kohsuke.github.BlazarGHTreeEntry;
import org.kohsuke.github.BlazarGitUser;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.hubspot.blazar.BlazarServiceTestBase;
import com.hubspot.blazar.BlazarServiceTestModule;
import com.hubspot.blazar.base.BuildOptions;
import com.hubspot.blazar.base.BuildMetadata;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.InterProjectBuildMapping;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.DependenciesService;
import com.hubspot.blazar.data.service.InterProjectBuildMappingService;
import com.hubspot.blazar.data.service.InterProjectBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.util.SingularityBuildLauncher;
import com.hubspot.blazar.util.TestSingularityBuildLauncher;

import io.dropwizard.db.ManagedDataSource;

@RunWith(JukitoRunner.class)
@UseModules({BlazarServiceTestModule.class})
public class InterProjectBuildServiceTest extends BlazarServiceTestBase {

  private static final Logger LOG = LoggerFactory.getLogger(InterProjectBuildServiceTest.class);

  @Inject
  private BranchService branchService;
  @Inject
  private DependenciesService dependenciesService;
  @Inject
  private ModuleService moduleService;
  @Inject
  private InterProjectBuildService interProjectBuildService;
  @Inject
  private InterProjectBuildMappingService interProjectBuildMappingService;
  @Inject
  private SingularityBuildLauncher singularityBuildLauncher;
  @Inject
  private TestUtils testUtils;
  @Inject
  private Map<String, GitHub> gitHubMap;

  @Before
  public void before(ManagedDataSource dataSource) throws Exception {
    // set up the data for these inter-project build tests
    runSql(dataSource, "InterProjectData.sql");
  }

  @Test
  public void itProducesTheRightTopologicalSort() {
    Optional<Module> module1 = moduleService.get(1);
    DependencyGraph graph = dependenciesService.buildInterProjectDependencyGraph(Sets.newHashSet(module1.get()));
    assertThat(Arrays.asList(1, 4, 7, 8, 10, 11, 9, 13)).isEqualTo(graph.getTopologicalSort());
  }

  @Test
  public void itCreatesTheCorrectInterProjectBuildMapping() {
    long mappingId = interProjectBuildMappingService.insert(InterProjectBuildMapping.makeNewMapping(1, 2, Optional.of(3L), 4));
    InterProjectBuildMapping createdMapping = interProjectBuildMappingService.getByMappingId(mappingId).get();
    assertThat(new InterProjectBuildMapping(Optional.of(mappingId), 123, 123, Optional.of(3L), 123, Optional.<Long>absent(), InterProjectBuild.State.QUEUED)).isEqualTo(createdMapping);
  }

  @Test
  public void itRunsASuccessfulInterProjectBuild() throws Exception {
    ((TestSingularityBuildLauncher) singularityBuildLauncher).clearModulesToFail();
    // Trigger interProjectBuild
    InterProjectBuild testableBuild = testUtils.runInterProjectBuild(1, Optional.<BuildMetadata>absent());
    long buildId = testableBuild.getId().get();
    assertThat(Sets.newHashSet(1)).isEqualTo(testableBuild.getModuleIds());
    assertThat(InterProjectBuild.State.SUCCEEDED).isEqualTo(testableBuild.getState());
    assertThat(Arrays.asList(1, 4, 7, 8, 10, 11, 9, 13)).isEqualTo(interProjectBuildService.getWithId(buildId).get().getDependencyGraph().get().getTopologicalSort());
    Set<InterProjectBuildMapping> mappings = interProjectBuildMappingService.getMappingsForInterProjectBuild(interProjectBuildService.getWithId(buildId).get());
    for (InterProjectBuildMapping mapping : mappings) {
      assertThat(InterProjectBuild.State.SUCCEEDED).isEqualTo(mapping.getState());
    }
    // ensure there is 1 repo build for these modules
    Set<Long> repoBuildIds = new HashSet<>();
    Set<Integer> modulesInOneBuild = ImmutableSet.of(7,8,9);
    for (InterProjectBuildMapping mapping : mappings) {
      if (modulesInOneBuild.contains(mapping.getModuleId())) {
        repoBuildIds.add(mapping.getRepoBuildId().get());
      }
    }
    assertThat(repoBuildIds.size()).isEqualTo(1);
  }

  @Test
  public void itGroupsModulesWhenBuildingOnManualTrigger() throws InterruptedException {
    ((TestSingularityBuildLauncher) singularityBuildLauncher).clearModulesToFail();
    // Trigger interProjectBuild
    InterProjectBuild testableBuild = testUtils.runInterProjectBuild(7, Optional.<BuildMetadata>absent());
    long buildId = testableBuild.getId().get();
    assertThat(Sets.newHashSet(7)).isEqualTo(testableBuild.getModuleIds());
    assertThat(InterProjectBuild.State.SUCCEEDED).isEqualTo(testableBuild.getState());
    Set<InterProjectBuildMapping> mappings = interProjectBuildMappingService.getMappingsForInterProjectBuild(interProjectBuildService.getWithId(buildId).get());
    // ensure there is 1 repo build for these modules
    Set<Long> repoBuildIds = new HashSet<>();
    Set<Integer> modulesInOneBuild = ImmutableSet.of(7,8,9);
    for (InterProjectBuildMapping mapping : mappings) {
      if (modulesInOneBuild.contains(mapping.getModuleId())) {
        repoBuildIds.add(mapping.getRepoBuildId().get());
      }
    }
    assertThat(repoBuildIds.size()).isEqualTo(1);
  }

  @Test
  public void itGroupsModulesWhenBuildingOnPush() throws Exception {
    int repoId = 3;
    String sha = "2222222222222222222222222222222222222222";
    // ensure we have a previous build
    GitInfo gitInfo = branchService.get(repoId).get();
    testUtils.runDefaultRepositoryBuild(gitInfo);

    // "commit" a change
    GitHub testhost = gitHubMap.get("git.example.com");
    BlazarGHRepository repo = (BlazarGHRepository) testhost.getRepository("test/Repo3");
    BlazarGHCommitFile file = new BlazarGHCommitFile("/Module1/.blazar.yaml", BlazarGHCommitFile.Status.modified);
    BlazarGitUser user = new BlazarGitUser("testy", "testy@example.com", 1465954246782L);
    BlazarGHCommitShortInfo info = new BlazarGHCommitShortInfo("change file", user, user);
    BlazarGHTreeEntry entry = new BlazarGHTreeEntry(sha, BlazarGHContent.fromString("git/example/com/Repo3/Module1/.blazar2.yaml"), "/Module1/.blazar.yaml");
    BlazarGHCommit commit = new BlazarGHCommit(sha, ImmutableList.of(file), info);
    BlazarGHChange change = new BlazarGHChange(commit, ImmutableList.of(entry), "master");
    repo.applyChange(change);

    BuildMetadata buildMetadata = BuildMetadata.push("testUser");
    BuildOptions buildOptions = new BuildOptions(ImmutableSet.<Integer>of(), BuildOptions.BuildDownstreams.INTER_PROJECT, false);
    RepositoryBuild repositoryBuild = testUtils.runAndWaitForRepositoryBuild(gitInfo, buildMetadata, buildOptions);
    Set<InterProjectBuildMapping> mappings = interProjectBuildMappingService.getByRepoBuildId(repositoryBuild.getId().get());
    // Here the mappings we have are only for the 1 repo we triggered (and queried on), have to get them all before we can test.
    InterProjectBuild interProjectBuild = interProjectBuildService.getWithId(mappings.iterator().next().getInterProjectBuildId()).get();
    mappings = interProjectBuildMappingService.getMappingsForInterProjectBuild(interProjectBuild);
    // ensure there is 1 repo build for these modules
    Set<Long> repoBuildIds = new HashSet<>();
    Set<Integer> modulesInOneBuild = ImmutableSet.of(7,8,9);
    for (InterProjectBuildMapping mapping : mappings) {
      if (modulesInOneBuild.contains(mapping.getModuleId())) {
        repoBuildIds.add(mapping.getRepoBuildId().get());
      }
    }
    assertThat(repoBuildIds.size()).isEqualTo(1);
    repo.revertLastChange();
  }


  @Test
  public void itBuildsAnInterProjectBuildOnPush() throws Exception {
    int repoId = 1;
    String sha = "0000000000000000000000000000000000000000";
    GitInfo gitInfo = branchService.get(repoId).get();
    BuildMetadata buildMetadata = BuildMetadata.push("testUser");
    BuildOptions buildOptions = new BuildOptions(ImmutableSet.of(), BuildOptions.BuildDownstreams.INTER_PROJECT, false);
    RepositoryBuild repositoryBuild = testUtils.runAndWaitForRepositoryBuild(gitInfo, buildMetadata, buildOptions);
    Set<InterProjectBuildMapping> interProjectBuildMappings = interProjectBuildMappingService.getByRepoBuildId(repositoryBuild.getId().get());
    if (interProjectBuildMappings.isEmpty()) {
      fail(String.format("Expected to have mappings for repo build %s", repositoryBuild));
    }

    Optional<InterProjectBuild> interProjectBuild = interProjectBuildService.getWithId(interProjectBuildMappings.iterator().next().getInterProjectBuildId());
    if (!interProjectBuild.isPresent()) {
      fail(String.format("InterProjectBuild for mapping %s should be present", interProjectBuildMappings.iterator().next()));
    }
    InterProjectBuild completedBuild = testUtils.waitForInterProjectBuild(interProjectBuild.get());

    assertThat(Sets.newHashSet(1, 2, 3)).isEqualTo(completedBuild.getModuleIds());
    assertThat(Arrays.asList(1, 2, 4, 5, 7, 6, 8, 10, 11, 9, 13)).isEqualTo(completedBuild.getDependencyGraph().get().getTopologicalSort());
    assertThat(completedBuild.getState()).isEqualTo(InterProjectBuild.State.SUCCEEDED);
  }

  @Test
  public void itCancelsDownstreamBuildsForInterProjectBuildWithFailures() throws Exception {
    Set<Integer> expectedFailures = Sets.newHashSet(7);
    Set<Integer> expectedSuccess  = Sets.newHashSet(1, 4);
    Set<Integer> expectedCancel = Sets.newHashSet(8, 9, 10, 11, 13);
    ((TestSingularityBuildLauncher) singularityBuildLauncher).setModulesToFail(expectedFailures);
    // Cause module #10 to fail, causing 13 to be cancelled

    InterProjectBuild testableBuild = testUtils.runInterProjectBuild(1, Optional.absent());
    InterProjectBuild buildRun = interProjectBuildService.getWithId(testableBuild.getId().get()).get();
    assertThat(buildRun.getState()).isEqualTo(InterProjectBuild.State.FAILED);
    Set<InterProjectBuildMapping> mappings = interProjectBuildMappingService.getMappingsForInterProjectBuild(buildRun);
    for (InterProjectBuildMapping mapping : mappings) {
      if (expectedFailures.contains(mapping.getModuleId())) {
        assertThat(mapping.getState().equals(InterProjectBuild.State.FAILED));
      } else if (expectedCancel.contains(mapping.getModuleId())) {
        assertThat(mapping.getState().equals(InterProjectBuild.State.CANCELLED));
      } else if (expectedSuccess.contains(mapping.getModuleId())) {
        assertThat(mapping.getState().equals(InterProjectBuild.State.SUCCEEDED));
      }
    }
    ((TestSingularityBuildLauncher) singularityBuildLauncher).clearModulesToFail();
  }
}
