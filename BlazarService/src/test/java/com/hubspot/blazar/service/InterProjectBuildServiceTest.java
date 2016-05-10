package com.hubspot.blazar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import java.util.Arrays;
import java.util.Set;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.hubspot.blazar.BlazarServiceTestBase;
import com.hubspot.blazar.BlazarServiceTestModule;
import com.hubspot.blazar.base.BuildOptions;
import com.hubspot.blazar.base.BuildTrigger;
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
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.listener.BuildEventDispatcher;
import com.hubspot.blazar.util.SingularityBuildLauncher;
import com.hubspot.blazar.util.TestSingularityBuildLauncher;

@RunWith(JukitoRunner.class)
@UseModules({BlazarServiceTestModule.class})
public class InterProjectBuildServiceTest extends BlazarServiceTestBase {

  private static final Logger LOG = LoggerFactory.getLogger(InterProjectBuildServiceTest.class);

  @Inject
  private BuildEventDispatcher buildEventDispatcher;
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
  private RepositoryBuildService repositoryBuildService;
  @Inject
  private ModuleBuildService moduleBuildService;
  @Inject
  private SingularityBuildLauncher singularityBuildLauncher;
  @Inject
  private TestUtils testUtils;

  @Before
  public void before() throws Exception {
    // set up the data for these inter-project build tests
    runSql("InterProjectData.sql");
  }

  @Test
  public void testInterBuildDependencyTree() {
    Optional<Module> module1 = moduleService.get(1);
    DependencyGraph graph = dependenciesService.buildInterProjectDependencyGraph(Sets.newHashSet(module1.get()));
    assertThat(Arrays.asList(1, 4, 7, 8, 10, 11, 9, 13)).isEqualTo(graph.getTopologicalSort());
  }

  @Test
  public void testInterProjectMappings() {
    long mappingId = interProjectBuildMappingService.insert(InterProjectBuildMapping.makeNewMapping(1, 2, Optional.of(3L), 4));
    InterProjectBuildMapping createdMapping = interProjectBuildMappingService.getByMappingId(mappingId).get();
    assertThat(new InterProjectBuildMapping(Optional.of(mappingId), 123, 123, Optional.of(3L), 123, Optional.<Long>absent(), InterProjectBuild.State.QUEUED)).isEqualTo(createdMapping);
  }

  @Test
  public void testSuccessfulInterProjectBuild() throws Exception {
    ((TestSingularityBuildLauncher) singularityBuildLauncher).clearModulesToFail();
    // Trigger interProjectBuild
    InterProjectBuild testableBuild = testUtils.runInterProjectBuild(1, Optional.<BuildTrigger>absent());
    long buildId = testableBuild.getId().get();
    assertThat(Sets.newHashSet(1)).isEqualTo(testableBuild.getModuleIds());
    assertThat(InterProjectBuild.State.SUCCEEDED).isEqualTo(testableBuild.getState());
    assertThat(Arrays.asList(1, 4, 7, 8, 10, 11, 9, 13)).isEqualTo(interProjectBuildService.getWithId(buildId).get().getDependencyGraph().get().getTopologicalSort());
    Set<InterProjectBuildMapping> mappings = interProjectBuildMappingService.getMappingsForInterProjectBuild(interProjectBuildService.getWithId(buildId).get());
    for (InterProjectBuildMapping mapping : mappings) {
      assertThat(InterProjectBuild.State.SUCCEEDED).isEqualTo(mapping.getState());
    }
  }

  @Test
  public void testInterProjectBuildFromPush() throws Exception {
    int repoId = 1;
    String sha = "0000000000000000000000000000000000000000";
    branchService.get(1);
    GitInfo gitInfo = branchService.get(repoId).get();
    BuildTrigger buildTrigger = BuildTrigger.forCommit(sha);
    BuildOptions buildOptions = new BuildOptions(ImmutableSet.<Integer>of(), BuildOptions.BuildDownstreams.INTER_PROJECT, false);
    RepositoryBuild repositoryBuild = testUtils.runAndWaitForRepositoryBuild(gitInfo, buildTrigger, buildOptions);
    Set<InterProjectBuildMapping> interProjectBuildMappings = interProjectBuildMappingService.getByRepoBuildId(repositoryBuild.getId().get());
    if (interProjectBuildMappings.isEmpty()) {
      fail(String.format("Expected to have mappings for repo build %s", repositoryBuild));
    }

    Optional<InterProjectBuild> interProjectBuild = interProjectBuildService.getWithId(interProjectBuildMappings.iterator().next().getInterProjectBuildId());
    if (!interProjectBuild.isPresent()) {
      fail(String.format("InterProjectBuild for mapping %s should be present", interProjectBuildMappings.iterator().next()));
    }

    assertThat(Sets.newHashSet(1, 2, 3)).isEqualTo(interProjectBuild.get().getModuleIds());
    assertThat(Arrays.asList(1, 2, 4, 5, 7, 6, 8, 10, 11, 9, 13)).isEqualTo(interProjectBuild.get().getDependencyGraph().get().getTopologicalSort());
  }

  @Test
  public void testInterProjectBuildWithFailures() throws Exception {
    Set<Integer> expectedFailures = Sets.newHashSet(7);
    Set<Integer> expectedSuccess  = Sets.newHashSet(1, 4);
    Set<Integer> expectedCancel = Sets.newHashSet(8, 9, 10, 11, 13);
    ((TestSingularityBuildLauncher) singularityBuildLauncher).setModulesToFail(expectedFailures);
    // Cause module #10 to fail, causing 13 to be cancelled

    InterProjectBuild testableBuild = testUtils.runInterProjectBuild(1, Optional.<BuildTrigger>absent());
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
  }


}
