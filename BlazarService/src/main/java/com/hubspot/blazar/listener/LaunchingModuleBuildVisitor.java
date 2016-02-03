package com.hubspot.blazar.listener;

import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.visitor.AbstractModuleBuildVisitor;
import com.hubspot.blazar.util.SingularityBuildLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LaunchingModuleBuildVisitor extends AbstractModuleBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(LaunchingModuleBuildVisitor.class);

  private final SingularityBuildLauncher singularityBuildLauncher;

  @Inject
  public LaunchingModuleBuildVisitor(SingularityBuildLauncher singularityBuildLauncher) {
    this.singularityBuildLauncher = singularityBuildLauncher;
  }

  @Override
  protected void visitLaunching(ModuleBuild build) throws Exception {
    LOG.info("About to launch build {}", build.getId().get());
    singularityBuildLauncher.launchBuild(build);
  }
}
