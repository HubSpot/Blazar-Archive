package com.hubspot.blazar.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Set;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.hubspot.blazar.BlazarServiceTestBase;
import com.hubspot.blazar.BlazarServiceTestModule;
import com.hubspot.blazar.base.Dependency;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.DependenciesService;
import com.hubspot.blazar.data.service.ModuleDiscoveryService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.discovery.ModuleDiscoveryHandler;
import com.hubspot.blazar.visitor.BuildEventDispatcher;

import io.dropwizard.db.ManagedDataSource;

@RunWith(JukitoRunner.class)
@UseModules({BlazarServiceTestModule.class})
public class DependencyVersionTest extends BlazarServiceTestBase {
  private static final Logger LOG = LoggerFactory.getLogger(DependencyVersionTest.class);
  @Inject
  private BuildEventDispatcher buildEventDispatcher;
  @Inject
  private BranchService branchService;
  @Inject
  private ModuleDiscoveryHandler moduleDiscoveryHandler;
  @Inject
  private ModuleDiscoveryService moduleDiscoveryService;
  @Inject
  private ModuleService moduleService;
  @Inject
  private DependenciesService dependenciesService;

  @Before
  public void before(ManagedDataSource dataSource) throws Exception {
    runSql(dataSource, "InterProjectData.sql");
  }

  @Test
  public void itHasModuleVersion() throws InterruptedException, IOException {
    // Run discovery on all branches and check that all modules have a version
    for (GitInfo branch : branchService.getAll()) {
      moduleDiscoveryHandler.updateModules(branch, true);
      for (Module module : moduleService.getByBranch(branch.getId().get())) {
        Set<Dependency> deps = dependenciesService.getDependencies(module.getId().get());
        Set<Dependency> provided = dependenciesService.getProvided(module.getId().get());
        for (Dependency d : deps) {
          assertThat(d.getVersion()).isEqualTo("1.0.0");
        }

        for (Dependency p : provided) {
          LOG.info("Checking version of {}", p);
          assertThat(p.getVersion()).isEqualTo("1.0.0");
        }
      }
    }
  }
}
