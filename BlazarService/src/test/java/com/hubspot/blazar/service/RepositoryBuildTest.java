package com.hubspot.blazar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.hubspot.blazar.BlazarServiceTestBase;
import com.hubspot.blazar.BlazarServiceTestModule;
import com.hubspot.blazar.base.BuildOptions;
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.listener.BuildEventDispatcher;

@RunWith(JukitoRunner.class)
@UseModules({BlazarServiceTestModule.class})
public class RepositoryBuildTest extends BlazarServiceTestBase {


  @Inject
  private BuildEventDispatcher buildEventDispatcher;
  @Inject
  private BranchService branchService;
  @Inject
  private RepositoryBuildService repositoryBuildService;

  @Before
  public void before() throws Exception {
    // set up the data for these inter-project build tests
    // todo migrate inter project data to a more common place
    runSql("InterProjectData.sql");
  }

  @Test
  public void testRepositoryBuild() throws InterruptedException {
    GitInfo gitInfo = branchService.get(3).get();
    RepositoryBuild build = runAndWaitForRepositoryBuild(gitInfo, BuildTrigger.forCommit("1111111111111111111111111111111111111111"), BuildOptions.defaultOptions());
    assertThat(build.getBranchId()).isEqualTo(3);
    assertThat(build.getState()).isEqualTo(RepositoryBuild.State.SUCCEEDED);
    assertThat(build.getDependencyGraph().get().getTopologicalSort()).isEqualTo(Lists.newArrayList(7,8,9));
  }


  private RepositoryBuild runAndWaitForRepositoryBuild(GitInfo gitInfo, BuildTrigger buildTrigger, BuildOptions buildOptions) throws InterruptedException {
    long id = repositoryBuildService.enqueue(gitInfo, BuildTrigger.forCommit("1111111111111111111111111111111111111111"), BuildOptions.defaultOptions());
    RepositoryBuild build = repositoryBuildService.get(id).get();
    int count = 0;
    while (!build.getState().isComplete()) {
      if (count > 10) {
        fail(String.format("Build %s took more than 10s to complete", build));
      }
      count++;
      Thread.sleep(1000);
      build = repositoryBuildService.get(id).get();
    }
    return build;
  }

}
