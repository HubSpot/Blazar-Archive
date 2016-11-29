package com.hubspot.blazar.data.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.data.BlazarDataTestModule;
import com.hubspot.blazar.test.base.service.DatabaseBackedTest;

import io.dropwizard.db.ManagedDataSource;

@RunWith(JukitoRunner.class)
@UseModules({BlazarDataTestModule.class})
public class RepositoryBuildDaoTest extends DatabaseBackedTest {

  @Inject
  RepositoryBuildDao dao;

  @Before
  @Inject
  public void before(ManagedDataSource dataSource) throws Exception {
    runSql(dataSource, "buildHistory.sql");
  }

  @Test
  public void itGetsPreviousBuild() {
    Optional<RepositoryBuild> buildThree = dao.getByBranchAndNumber(1, 3);
    assertThat(buildThree.isPresent()).isTrue();
    assertThat(buildThree.get().getBuildNumber()).isEqualTo(3);
    Optional<RepositoryBuild> buildTwo = dao.getPreviousBuild(buildThree.get());
    assertThat(buildTwo.isPresent()).isTrue();
    assertThat(buildTwo.get().getBuildNumber()).isEqualTo(2);
  }
}
