package com.hubspot.blazar.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
import com.hubspot.blazar.base.BuildMetadata;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleActivityPage;
import com.hubspot.blazar.base.ModuleBuildInfo;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;

import io.dropwizard.db.ManagedDataSource;

@RunWith(JukitoRunner.class)
@UseModules({BlazarServiceTestModule.class})
public class ModuleActivityTest extends BlazarServiceTestBase {

  @Inject
  private BranchService branchService;
  @Inject
  private ModuleService moduleService;
  @Inject
  private ModuleBuildService moduleBuildService;
  @Inject
  private TestUtils testUtils;
  private GitInfo branch;
  private Module module;
  private List<RepositoryBuild> launchedBuilds = new ArrayList<>();

  @Before
  @Inject
  public void before(ManagedDataSource dataSource) throws Exception {
    runSql(dataSource, "InterProjectData.sql");
    module = moduleService.get(1).get();
    branch = branchService.get(moduleService.getBranchIdFromModuleId(module.getId().get())).get();
  }

  private void seedBuilds() {
    // Run 10 build of our module
    BuildMetadata trigger = BuildMetadata.manual(Optional.of("testUser"));
    BuildOptions options = new BuildOptions(ImmutableSet.of(1), BuildOptions.BuildDownstreams.NONE, false);
    for (int i=0; i < 10; i++) {
      launchedBuilds.add(testUtils.runAndWaitForRepositoryBuild(branch, trigger, options));
    }
  }

  @Test
  public void testGetsRightActivity() {
    seedBuilds();
    ModuleActivityPage page = moduleBuildService.getModuleActivityPage(module.getId().get(), Optional.of(10), Optional.of(100));
    // check that we find the number of builds we started
    assertThat(page.getModuleBuildInfos().size()).isEqualTo(launchedBuilds.size());
    // builds are launched in ASC order, but history endpoint returns in DESC order
    launchedBuilds.sort(Comparator.comparing(RepositoryBuild::getBuildNumber, Comparator.reverseOrder()));

    // check that they are correctly paired (module build is associated with repo build found)
    for (int i = 0; i < page.getModuleBuildInfos().size(); i++) {
      RepositoryBuild launchedBuild = launchedBuilds.get(i);
      ModuleBuildInfo info = page.getModuleBuildInfos().get(i);
      assertThat(launchedBuild).isEqualTo(info.getBranchBuild());
      assertThat(info.getModuleBuild().getRepoBuildId()).isEqualTo(launchedBuild.getId().get());
    }
  }

  @Test
  public void testGetsCorrectPageSize() {
    seedBuilds();
    ModuleActivityPage page1 = moduleBuildService.getModuleActivityPage(module.getId().get(), Optional.of(10), Optional.of(100));
    // check that we find 10 (the number of builds we started)
    assertThat(page1.getModuleBuildInfos().size()).isEqualTo(10);

    ModuleActivityPage page2 = moduleBuildService.getModuleActivityPage(module.getId().get(), Optional.of(5), Optional.of(100));
    // check that we find 5
    assertThat(page2.getModuleBuildInfos().size()).isEqualTo(5);

    ModuleActivityPage page3 = moduleBuildService.getModuleActivityPage(module.getId().get(), Optional.of(5), Optional.of(4));
    // check that we find 5
    assertThat(page3.getModuleBuildInfos().size()).isEqualTo(4);
  }

  public void testGetsActivity() {
    ModuleActivityPage page = moduleBuildService.getModuleActivityPage(module.getId().get(), Optional.of(10), Optional.of(100));
    assertThat(page.getModuleBuildInfos().size()).isEqualTo(4);
  }

  public static GitInfo newGitInfo(int repositoryId, String repositoryName, String branch) {
    return new GitInfo(Optional.<Integer>absent(), "github", "HubSpot", repositoryName, repositoryId, branch, true, System.currentTimeMillis(), System.currentTimeMillis());
  }
}
