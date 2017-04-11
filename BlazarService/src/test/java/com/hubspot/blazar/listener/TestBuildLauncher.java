package com.hubspot.blazar.listener;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.visitor.AbstractModuleBuildVisitor;
import com.hubspot.blazar.exception.NonRetryableBuildException;
import com.hubspot.blazar.externalservice.BuildClusterService;

public class TestBuildLauncher extends AbstractModuleBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(TestBuildLauncher.class);

  private final BuildClusterService buildClusterService;

  @Inject
  public TestBuildLauncher(BuildClusterService buildClusterService) {
    this.buildClusterService = buildClusterService;
  }

  /**
   * "Launch" the build container when the build is in the LAUNCHING state.
   * When Blazar runs in production we pre-launch containers when the build is still QUEUED in order to speed up the build.
   * During tests we launch the container when the build is in the LAUNCHING state.
   */
  @Override
  protected void visitLaunching(ModuleBuild moduleBuild) throws Exception {
    LOG.info("About to launch docker container for build {}", moduleBuild);
    try {
      buildClusterService.launchBuildContainer(moduleBuild);
    } catch (Exception e) {
      throw new NonRetryableBuildException(String.format("An error occurred while launching docker container for build {}. Will not retry the build", moduleBuild), e);
    }
  }
}
