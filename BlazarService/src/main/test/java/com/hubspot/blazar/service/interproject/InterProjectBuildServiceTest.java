package com.hubspot.blazar.service.interproject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.InterProjectBuildMapping;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.UiConfiguration;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.DependenciesService;
import com.hubspot.blazar.data.service.InterProjectBuildService;
import com.hubspot.blazar.data.service.InterProjectModuleBuildMappingService;
import com.hubspot.blazar.data.service.InterProjectRepositoryBuildMappingService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.github.GitHubProtos.Commit;
import com.hubspot.blazar.listener.BuildEventDispatcher;
import com.hubspot.blazar.service.BlazarServiceTestBase;
import com.hubspot.blazar.service.BlazarServiceTestModule;
import com.hubspot.blazar.util.GitHubHelper;
import com.hubspot.blazar.util.SingularityBuildLauncher;
import io.dropwizard.db.ManagedDataSource;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.doAnswer;

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

  @Before
  public void before() throws IOException, SQLException, LiquibaseException {
    // set up the data for these inter-project build tests
    Connection connection = getFromGuice(ManagedDataSource.class).getConnection();
    JdbcConnection jdbcConnection = new JdbcConnection(connection);
    ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
    Liquibase liquibase = new Liquibase("interProjectData.sql", resourceAccessor, jdbcConnection);
    liquibase.update(new Contexts());
  }

  @Test
  public void checkTableSetup() {
    BranchService branchService = getFromGuice(BranchService.class);
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
                                           BlazarConfiguration blazarConfiguration,
                                           GitHubHelper gitHubHelper,
                                           final GHRepository gHRepository,
                                           final GHCommit gHCommit) throws Exception {
    // do mocks
    Answer<Void> answer = new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        ModuleBuild m = (ModuleBuild) invocation.getArguments()[1];
        LOG.info("PRETENDING TO LAUNCH {}", m);
        return null;
      }
    };
    doAnswer(answer).when(singularityBuildLauncher).launchBuild(Matchers.<ModuleBuild>any());

    final Answer<GHRepository> ghRepositoryAnswer = new Answer<GHRepository>() {
      @Override
      public GHRepository answer(InvocationOnMock invocation) {
        return gHRepository;
      }
    };
    doAnswer(ghRepositoryAnswer).when(gitHubHelper).repositoryFor(Matchers.<GitInfo>any());

    Answer<Optional<String>> shaAnswer = new Answer<Optional<String>>() {
      @Override
      public Optional<String> answer(InvocationOnMock invocation) {
        LOG.debug("returned fake sha");
        return Optional.of("0000000000000000000000000000000000000000"); // fake sha
      }
    };
    doAnswer(shaAnswer).when(gitHubHelper).shaFor(Matchers.<GHRepository>any(), Matchers.<GitInfo>any());

    final Answer<GHCommit> ghCommitAnswer = new Answer<GHCommit> () {
      @Override
      public GHCommit answer(InvocationOnMock invocation) {
        return gHCommit;
      }
    };
    doAnswer(ghCommitAnswer).when(gHRepository).getCommit(Matchers.<String>any());

    Answer<Commit> toCommitAnswer = new Answer<Commit> () {
      @Override
      public Commit answer(InvocationOnMock invocation) {
        Commit.Builder builder = Commit.newBuilder()
            .setId("0000000000000000000000000000000000000000")
            .setMessage("fake message")
            .setTimestamp("0");
        return builder.build();
      }
    };
    doAnswer(toCommitAnswer).when(gitHubHelper).toCommit(Matchers.<GHCommit>any());

    final Answer<CommitInfo> commitInfoAnswer = new Answer<CommitInfo>() {
      @Override
      public CommitInfo answer(InvocationOnMock invocation) {
        Commit.Builder builder = Commit.newBuilder()
            .setId("0000000000000000000000000000000000000000")
            .setMessage("fake message")
            .setTimestamp("0");
        return new CommitInfo(builder.build(), Optional.<Commit>absent(), Collections.<Commit>emptyList(), false);
      }
    };
    doAnswer(commitInfoAnswer).when(gitHubHelper).commitInfoFor(Matchers.<GHRepository>any(), Matchers.<Commit>any(), Matchers.<Optional<Commit>>any());

    final Answer<UiConfiguration> uiConfigurationAnswer = new Answer<UiConfiguration> () {
      @Override
      public UiConfiguration answer(InvocationOnMock invocation) {
        return new UiConfiguration("http://localhost/test/base/url");
      }
    };
    doAnswer(uiConfigurationAnswer).when(blazarConfiguration).getUiConfiguration();

    // do tests

    InterProjectBuild build = InterProjectBuild.getQueuedBuild(Sets.newHashSet(1), BuildTrigger.forInterProjectBuild(branchService.get(1).get()));
    long id = interProjectBuildService.enqueue(build);
    Optional<InterProjectBuild> maybeQueued = interProjectBuildService.getWithId(id);
    int count = 0;
    // wait for build to finish
    while (maybeQueued.isPresent() && maybeQueued.get().getState() != InterProjectBuild.State.FINISHED) {
      if (count > 10) {
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

  @Test
  public void testSingularityMock() {
  }

  private void postSingularityEvents(ModuleBuild build) {
    LOG.info("Build for moduleId {} triggered", build.getModuleId());
  }

}
