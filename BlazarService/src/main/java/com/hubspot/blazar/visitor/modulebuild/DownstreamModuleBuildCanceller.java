package com.hubspot.blazar.visitor.modulebuild;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.AbstractModuleBuildVisitor;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.RepositoryBuildService;

@Singleton
public class DownstreamModuleBuildCanceller extends AbstractModuleBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(DownstreamModuleBuildCanceller.class);

  private final RepositoryBuildService repositoryBuildService;
  private final ModuleBuildService moduleBuildService;

  @Inject
  public DownstreamModuleBuildCanceller(RepositoryBuildService repositoryBuildService,
                                        ModuleBuildService moduleBuildService) {
    this.repositoryBuildService = repositoryBuildService;
    this.moduleBuildService = moduleBuildService;
  }

  @Override
  protected void visitCancelled(ModuleBuild build) throws Exception {
    LOG.debug("Module build {} has been cancelled looking for downstream module builds that need to be cancelled",
        build.getId().get());
    cancelDownstreamModuleBuilds(build);
  }

  @Override
  protected void visitFailed(ModuleBuild build) throws Exception {
    cancelDownstreamModuleBuilds(build);
  }

  private void cancelDownstreamModuleBuilds(ModuleBuild cancelledModuleBuild) {
    RepositoryBuild repositoryBuild = repositoryBuildService.get(cancelledModuleBuild.getRepoBuildId()).get();
    DependencyGraph dependencyGraph = repositoryBuild.getDependencyGraph().get();

    Set<Integer> downstreamModules = dependencyGraph.outgoingVertices(cancelledModuleBuild.getModuleId());
    for (ModuleBuild moduleBuildInRepoBuild : moduleBuildService.getByRepositoryBuild(cancelledModuleBuild.getRepoBuildId())) {
      LOG.debug("Checking if module build {} is downstream to cancelled module build {} (and has not completed) in order to cancel it.",
          moduleBuildInRepoBuild, cancelledModuleBuild.getId().get());
      if (downstreamModules.contains(moduleBuildInRepoBuild.getModuleId()) && !moduleBuildInRepoBuild.getState().isComplete()) {
        moduleBuildService.cancel(moduleBuildInRepoBuild);
        LOG.debug("Downstream module build {}->{} was cancelled.", cancelledModuleBuild.getId().get(),
            moduleBuildInRepoBuild.getId().get());
      }
    }
  }
}
