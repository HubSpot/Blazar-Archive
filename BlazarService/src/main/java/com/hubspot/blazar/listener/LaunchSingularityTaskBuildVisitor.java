package com.hubspot.blazar.listener;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.visitor.AbstractModuleBuildVisitor;
import com.hubspot.blazar.exception.NonRetryableBuildException;
import com.hubspot.blazar.externalservice.BuildClusterService;

@Singleton
public class LaunchSingularityTaskBuildVisitor extends AbstractModuleBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(LaunchSingularityTaskBuildVisitor.class);

  private final BuildClusterService buildClusterService;

  @Inject
  public LaunchSingularityTaskBuildVisitor(BuildClusterService buildClusterService) {
    this.buildClusterService = buildClusterService;
  }

  /**
   * Eagerly launch a docker container to do the build when the build is queued. The container should poll Blazar to
   * check that the build has entered the LAUNCHING state before commencing the build process.
   */
  @Override
  protected void visitQueued(ModuleBuild moduleBuild) throws Exception {
    LOG.info("About to launch docker container for build {}", moduleBuild);
    try {
      buildClusterService.launchBuildContainer(moduleBuild);
    } catch (Exception e) {
      throw new NonRetryableBuildException(String.format("An error occurred while launching docker container for build {}. Will not retry the build", moduleBuild), e);
    }
  }
}
