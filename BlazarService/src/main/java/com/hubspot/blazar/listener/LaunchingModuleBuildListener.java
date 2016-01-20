package com.hubspot.blazar.listener;

import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.listener.ModuleBuildListener;
import com.hubspot.blazar.util.SingularityBuildLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LaunchingModuleBuildListener implements ModuleBuildListener {
  private static final Logger LOG = LoggerFactory.getLogger(LaunchingModuleBuildListener.class);

  private final SingularityBuildLauncher singularityBuildLauncher;

  @Inject
  public LaunchingModuleBuildListener(SingularityBuildLauncher singularityBuildLauncher) {
    this.singularityBuildLauncher = singularityBuildLauncher;
  }

  @Override
  public void buildChanged(ModuleBuild build) throws Exception {
    LOG.info("About to launch build {}", build.getId().get());
    singularityBuildLauncher.launchBuild(build);
  }
}
