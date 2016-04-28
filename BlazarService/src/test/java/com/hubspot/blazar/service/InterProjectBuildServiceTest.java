package com.hubspot.blazar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
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
import com.hubspot.blazar.base.ModuleBuild;
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
  private EventBus eventBus;
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

  @Before
  public void before() throws Exception {
    // set up the data for these inter-project build tests
    runSql("interProjectData.sql");
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
    assertThat(new InterProjectBuildMapping(Optional.of(mappingId), 123, 123, Optional.of(3L), 123, Optional.<Long>absent(), InterProjectBuild.State.CALCULATING)).isEqualTo(createdMapping);
  }

  @Test
  public void testSuccessfulInterProjectBuild() throws Exception {
    ((TestSingularityBuildLauncher) singularityBuildLauncher).clearModulesToFail();
    runInitialBuilds();
    LOG.info("Initial builds are now in the database\n\n\n");
    // Trigger interProjectBuild
    InterProjectBuild testableBuild = runInterProjectBuild(1);
    assertThat(Sets.newHashSet(1)).isEqualTo(testableBuild.getModuleIds());
    assertThat(InterProjectBuild.State.SUCCEEDED).isEqualTo(testableBuild.getState());
    assertThat(Arrays.asList(1, 4, 7, 8, 10, 11, 9, 13)).isEqualTo(interProjectBuildService.getWithId(1).get().getDependencyGraph().get().getTopologicalSort());
    Set<InterProjectBuildMapping> mappings = interProjectBuildMappingService.getMappingsForInterProjectBuild(interProjectBuildService.getWithId(1).get());
    for (InterProjectBuildMapping mapping : mappings) {
      assertThat(InterProjectBuild.State.SUCCEEDED).isEqualTo(mapping.getState());
    }
  }

  @Test
  public void testInterProjectBuildWithFailures() throws Exception {
    runInitialBuilds();

    Set<Integer> expectedFailures = Sets.newHashSet(7);
    Set<Integer> expectedSuccess  = Sets.newHashSet(1, 4);
    Set<Integer> expectedCancel = Sets.newHashSet(8, 9, 10, 11, 13);

    LOG.info("Initial builds are now in the db\n\n\n");
    ((TestSingularityBuildLauncher) singularityBuildLauncher).setModulesToFail(expectedFailures);
    // Cause module #10 to fail, causing 13 to be cancelled

    InterProjectBuild testableBuild = runInterProjectBuild(1);
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

  private InterProjectBuild runInterProjectBuild(int rootModuleId) throws InterruptedException {
    LOG.info("Starting inter-project-build for id {}", rootModuleId);
    InterProjectBuild build = InterProjectBuild.getQueuedBuild(Sets.newHashSet(rootModuleId), BuildTrigger.forInterProjectBuild(branchService.get(moduleService.getBranchIdFromModuleId(rootModuleId)).get()));
    long id = interProjectBuildService.enqueue(build);
    Optional<InterProjectBuild> maybeQueued = interProjectBuildService.getWithId(id);
    int count = 0;
    // wait 1s for build to finish
    while (!maybeQueued.isPresent() && !maybeQueued.get().getState().isFinished()) {
      if (count > 1) {
        fail("InterProject did not finish in 10s");
      }
      count++;
      Thread.sleep(1000);
      maybeQueued = interProjectBuildService.getWithId(id);
    }
    return maybeQueued.get();
  }

  private void runInitialBuilds() {
    List<Integer> branchIds = Lists.newArrayList(1, 2, 3, 4, 5);
    List<RepositoryBuild> buildSeedList = new ArrayList<>();
    for (int i : branchIds) {
      LOG.debug("Launching Repository Build to seed db with successes for all modules on {branchId=1}");
      buildSeedList.add(makeRepoBuild(i));
    }
    LOG.info("All branch builds launched");
    for (RepositoryBuild r : buildSeedList) {
      Set<ModuleBuild> moduleBuilds = moduleBuildService.getByRepositoryBuild(r.getId().get());
      for (ModuleBuild b : moduleBuilds) {
        assertThat(b.getState().isComplete()).isTrue();
        assertThat(b.getState()).isEqualTo(ModuleBuild.State.SUCCEEDED);
      }
    }
    LOG.info("All seed builds succeeded");
  }

  private RepositoryBuild makeRepoBuild(int branchId) {
    long id = repositoryBuildService.enqueue(branchService.get(branchId).get(), BuildTrigger.forBranchCreation("master"), BuildOptions.defaultOptions());
    return repositoryBuildService.get(id).get();
  }
}
