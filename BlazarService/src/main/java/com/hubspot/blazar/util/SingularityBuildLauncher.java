package com.hubspot.blazar.util;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.SingularityConfiguration;
import com.hubspot.blazar.external.models.singularity.Resources;
import com.hubspot.singularity.api.SingularityRunNowRequest;
import com.hubspot.singularity.client.SingularityClient;

@Singleton
public class SingularityBuildLauncher {
  private final SingularityClient singularityClient;
  private final SingularityConfiguration singularityConfiguration;

  @Inject
  public SingularityBuildLauncher(SingularityClient singularityClient, BlazarConfiguration blazarConfiguration) {
    this.singularityClient = singularityClient;
    this.singularityConfiguration = blazarConfiguration.getSingularityConfiguration();
  }

  public synchronized void launchBuild(ModuleBuild build) throws Exception {
    singularityClient.runSingularityRequest(singularityConfiguration.getRequest(), Optional.of(buildRequest(build)));
  }

  private SingularityRunNowRequest buildRequest(ModuleBuild build) {
    String buildId = Long.toString(build.getId().get());
    Optional<com.hubspot.mesos.Resources> buildResources = Optional.absent();
    if (build.getResolvedConfig().isPresent() && build.getResolvedConfig().get().getBuildResources().isPresent()) {
      buildResources = Optional.of(Resources.toMesosResources(build.getResolvedConfig().get().getBuildResources().get()));
    }

    return new SingularityRunNowRequest(
        Optional.of("Running Blazar module build " + buildId),
        Optional.of(false),
        Optional.of(buildId),
        Optional.of(Arrays.asList("--buildId", buildId)),
        buildResources);
  }
}
