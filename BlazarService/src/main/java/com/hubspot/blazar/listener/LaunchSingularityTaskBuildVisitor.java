package com.hubspot.blazar.listener;

import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.visitor.AbstractModuleBuildVisitor;
import com.hubspot.blazar.util.SingularityBuildLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LaunchSingularityTaskBuildVisitor extends AbstractModuleBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(LaunchSingularityTaskBuildVisitor.class);

  private final SingularityBuildLauncher singularityBuildLauncher;

  @Inject
  public LaunchSingularityTaskBuildVisitor(SingularityBuildLauncher singularityBuildLauncher) {
    this.singularityBuildLauncher = singularityBuildLauncher;
  }

  /**
   * Eagerly launch the singularity task when the build is queued. The task will wait for the
   * build to enter the launching state before continuing
   */
  @Override
  protected void visitQueued(ModuleBuild build) throws Exception {
    LOG.info("About to launch Singularity task for build {}", build);
    singularityBuildLauncher.launchBuild(build);
  }
}
