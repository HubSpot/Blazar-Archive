package com.hubspot.blazar.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.hubspot.blazar.BlazarServiceTestBase;
import com.hubspot.blazar.BlazarServiceTestModule;
import com.hubspot.blazar.base.BuildOptions;
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleActivityPage;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.listener.BuildEventDispatcher;

@RunWith(JukitoRunner.class)
@UseModules({BlazarServiceTestModule.class})
public class ModuleActivityTest extends BlazarServiceTestBase {

  @Inject
  private BuildEventDispatcher buildEventDispatcher; // required to have event bus work
  @Inject
  private BranchService branchService;
  @Inject
  private ModuleService moduleService;
  @Inject
  private RepositoryBuildService repositoryBuildService;
  @Inject
  private ModuleBuildService moduleBuildService;
  @Inject
  private TestUtils testUtils;
  private GitInfo branch;
  private Module module;

  @Before
  public void before() throws Exception {
    runSql("InterProjectData.sql");
    module = moduleService.get(1).get();
    branch = branchService.get(moduleService.getBranchIdFromModuleId(module.getId().get())).get();

    // Run 10 build of our module
    BuildTrigger trigger = BuildTrigger.forUser("bob");
    BuildOptions options = new BuildOptions(ImmutableSet.of(1), BuildOptions.BuildDownstreams.NONE, false);
    for (int i=0; i < 10; i++) {
      testUtils.runAndWaitForRepositoryBuild(branch, trigger, options);
    }
  }

  @Test
  public void testGetsHistory() {
    ModuleActivityPage page = moduleBuildService.getModuleBuildHistoryPage(module.getId().get(), 10, Optional.of(100));
    assertThat(page.getModuleBuildInfos().size()).isEqualTo(4);
  }

  @Test
  public void testGetsPageSize() {

  }

  public static GitInfo newGitInfo(int repositoryId, String repositoryName, String branch) {
    return new GitInfo(Optional.<Integer>absent(), "github", "HubSpot", repositoryName, repositoryId, branch, true, System.currentTimeMillis(), System.currentTimeMillis());
  }
}
