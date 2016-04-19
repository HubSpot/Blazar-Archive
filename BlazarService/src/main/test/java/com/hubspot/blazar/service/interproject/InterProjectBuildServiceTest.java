package com.hubspot.blazar.service.interproject;

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
import com.hubspot.blazar.base.BuildOptions;
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.InterProjectBuildMapping;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.DependenciesService;
import com.hubspot.blazar.data.service.InterProjectBuildService;
import com.hubspot.blazar.data.service.InterProjectModuleBuildMappingService;
import com.hubspot.blazar.data.service.InterProjectRepositoryBuildMappingService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.listener.BuildEventDispatcher;
import com.hubspot.blazar.service.BlazarServiceTestBase;
import com.hubspot.blazar.service.BlazarServiceTestModule;
import com.hubspot.blazar.util.SingularityBuildLauncher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

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
  private InterProjectRepositoryBuildMappingService iPRepositoryBuildMappingService;
  @Inject
  private InterProjectModuleBuildMappingService iPModuleBuildMappingService;
  @Inject
  private RepositoryBuildService repositoryBuildService;
  @Inject
  private ModuleBuildService moduleBuildService;

  @Before
  public void before() throws Exception {
    // set up the data for these inter-project build tests
    runSql("interProjectData.sql");
  }

  @Test
  public void checkTableSetup() {
    Optional<GitInfo> branch = branchService.get(1);
    assertThat(branch.isPresent()).isTrue();
    assertThat(branch.get().getBranch()).isEqualTo("master");
  }

  @Test
  public void testInterBuildDependencyTree() {
    Optional<Module> module1 = moduleService.get(1);
    DependencyGraph graph = dependenciesService.buildInterProjectDependencyGraph(Sets.newHashSet(module1.get()));
    assertThat(Arrays.asList(1, 4, 7, 8, 10, 9, 13)).isEqualTo(graph.getTopologicalSort());
  }

  @Test
  public void testInterProjectBuildEnqueue(SingularityBuildLauncher singularityBuildLauncher,
                                           BlazarConfiguration blazarConfiguration) throws Exception {
    List<Integer> branchIds = Lists.newArrayList(1,2,3,4,5);
    List<RepositoryBuild> buildSeedList = new ArrayList<>();
    for (int i : branchIds) {
      LOG.debug("Launching Repository Build to seed db with successes for all modules on {branchId=1}");
      buildSeedList.add(makeRepoBuild(i));
    }
    LOG.info("All branch builds launched");
    for (RepositoryBuild r : buildSeedList){
      Set<ModuleBuild> moduleBuilds = moduleBuildService.getByRepositoryBuild(r.getId().get());
      for (ModuleBuild b : moduleBuilds) {
        assertThat(b.getState().isComplete()).isTrue();
        assertThat(b.getState()).isEqualTo(ModuleBuild.State.SUCCEEDED);
      }
    }
    LOG.info("All seed builds succeeded");

    // Trigger interProjectBuild
    LOG.info("Starting inter-project-build test");
    InterProjectBuild build = InterProjectBuild.getQueuedBuild(Sets.newHashSet(1), BuildTrigger.forInterProjectBuild(branchService.get(1).get()));
    long id = interProjectBuildService.enqueue(build);
    Optional<InterProjectBuild> maybeQueued = interProjectBuildService.getWithId(id);
    int count = 0;
    // wait for build to finish
    while (maybeQueued.isPresent() && maybeQueued.get().getState() != InterProjectBuild.State.FINISHED) {
      if (count > 1) {
        fail("InterProject did not finish in 10s");
      }
      count++;
      Thread.sleep(1000);
      maybeQueued = interProjectBuildService.getWithId(id);
    }
    InterProjectBuild testableBuild = maybeQueued.get();
    assertThat(Sets.newHashSet(1)).isEqualTo(testableBuild.getModuleIds());
    assertThat(InterProjectBuild.State.FINISHED).isEqualTo(testableBuild.getState());
    assertThat(Arrays.asList(1, 4, 7, 8, 10, 9, 13)).isEqualTo(interProjectBuildService.getWithId(1).get().getDependencyGraph().get().getTopologicalSort());
    Set<InterProjectBuildMapping> repoBuildsMappings = iPRepositoryBuildMappingService.getMappingsForInterProjectBuild(testableBuild);
    Set<InterProjectBuildMapping> moduleBuildMappings = iPModuleBuildMappingService.getMappingsForBuild(testableBuild);
  }

  private void postSingularityEvents(ModuleBuild build) {
    LOG.info("Build for moduleId {} triggered", build.getModuleId());
  }

  private RepositoryBuild makeRepoBuild(int branchId) {
    long id = repositoryBuildService.enqueue(branchService.get(branchId).get(), BuildTrigger.forBranchCreation("master"), BuildOptions.defaultOptions());
    return repositoryBuildService.get(id).get();
  }

}
