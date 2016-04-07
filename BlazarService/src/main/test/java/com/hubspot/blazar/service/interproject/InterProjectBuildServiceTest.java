package com.hubspot.blazar.service.interproject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.DependenciesService;
import com.hubspot.blazar.data.service.InterProjectBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.service.BlazarServiceTestBase;
import com.hubspot.blazar.util.SingularityBuildLauncher;
import io.dropwizard.db.ManagedDataSource;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class InterProjectBuildServiceTest extends BlazarServiceTestBase { private static final Logger LOG = LoggerFactory.getLogger(InterProjectBuildServiceTest.class);

  private BranchService branchService;
  private DependenciesService dependenciesService;
  private ModuleService moduleService;
  private InterProjectBuildService interProjectBuildService;

  @Before
  public void before() throws IOException, SQLException, LiquibaseException {
    // set up the data for these inter-project build tests
    Connection connection = getFromGuice(ManagedDataSource.class).getConnection();
    JdbcConnection jdbcConnection = new JdbcConnection(connection);
    ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
    Liquibase liquibase = new Liquibase("interProjectData.sql", resourceAccessor, jdbcConnection);
    liquibase.update(new Contexts());

    branchService = getFromGuice(BranchService.class);
    dependenciesService = getFromGuice(DependenciesService.class);
    moduleService = getFromGuice(ModuleService.class);
    interProjectBuildService = getFromGuice(InterProjectBuildService.class);
  }

  @Test
  public void checkTableSetup() {
    BranchService branchService = getFromGuice(BranchService.class);
    Optional<GitInfo> branch = branchService.get(1);

    assertThat(branch.isPresent()).isTrue();
    assertThat(branch.get().getBranch()).isEqualTo("master");
  }

  @Test
  public void testBuildDependencyTree() {
    Optional<Module> module1 = moduleService.get(1);
    DependencyGraph graph = dependenciesService.buildInterProjectDependencyGraph(Sets.newHashSet(module1.get()));
    assertThat(Arrays.asList(1, 4, 7, 8, 10, 9, 13)).isEqualTo(graph.getTopologicalSort());

  }

  @Test
  public void testInterProjectBuildEnqueue() throws Exception {
    setUpMocks();
    InterProjectBuild build = InterProjectBuild.getQueuedBuild(Sets.newHashSet(1), BuildTrigger.forInterProjectBuild(branchService.get(1).get()));
    interProjectBuildService.enqueue(build);
    // assertThat(Arrays.asList(1, 4, 7, 8, 10, 9, 13)).isEqualTo(interProjectBuildService.getWithId(1).get().getDependencyGraph().get().getTopologicalSort());
  }

  @Test
  public void testSingularityMock() {
  }



  private void setUpMocks() throws Exception {
    Answer<Void> buildLauncherAnswer = new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        postSingularityEvents((ModuleBuild) invocation.getArguments()[1]);
        return null;
      }
    };

    SingularityBuildLauncher mockedSingularityBuildLauncher = mock(SingularityBuildLauncher.class);
    doAnswer(buildLauncherAnswer).when(mockedSingularityBuildLauncher).launchBuild(Matchers.<ModuleBuild>any());
  }



  private void postSingularityEvents(ModuleBuild build) {
    LOG.info("Build for moduleId {} triggered", build.getModuleId());
  }

}
