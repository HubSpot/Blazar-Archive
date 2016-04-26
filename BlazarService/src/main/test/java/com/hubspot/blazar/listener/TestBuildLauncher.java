package com.hubspot.blazar.listener;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.visitor.AbstractModuleBuildVisitor;
import com.hubspot.blazar.util.SingularityBuildLauncher;

public class TestBuildLauncher extends AbstractModuleBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(TestBuildLauncher.class);

  private final SingularityBuildLauncher singularityBuildLauncher;

  @Inject
  public TestBuildLauncher(SingularityBuildLauncher singularityBuildLauncher) {
    this.singularityBuildLauncher = singularityBuildLauncher;
  }

  /**
   * "Launch" builds  when the build is in a launching state. This is because in PROD we pre-launch things into singularity and they wait for state changes.
   * In tests this doesn't happen so we have to call #launchBuild() ourselves.
   */
  @Override
  protected void visitLaunching(ModuleBuild build) throws Exception {
    LOG.info("About to launch Singularity task for build {}", build);
    singularityBuildLauncher.launchBuild(build);
  }
}
