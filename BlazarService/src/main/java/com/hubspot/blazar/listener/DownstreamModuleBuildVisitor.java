package com.hubspot.blazar.listener;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.AbstractModuleBuildVisitor;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.RepositoryBuildService;

@Singleton
public class DownstreamModuleBuildVisitor extends AbstractModuleBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(DownstreamModuleBuildVisitor.class);

  private final RepositoryBuildService repositoryBuildService;
  private final ModuleBuildService moduleBuildService;
  private final EventBus eventBus;

  @Inject
  public DownstreamModuleBuildVisitor(RepositoryBuildService repositoryBuildService,
                                      ModuleBuildService moduleBuildService,
                                      EventBus eventBus) {
    this.repositoryBuildService = repositoryBuildService;
    this.moduleBuildService = moduleBuildService;
    this.eventBus = eventBus;
  }

  @Override
  protected void visitSucceeded(ModuleBuild build) throws Exception {
    LOG.info("Checking for builds downstream of {}", build.getId().get());

    RepositoryBuild repositoryBuild = repositoryBuildService.get(build.getRepoBuildId()).get();
    DependencyGraph dependencyGraph = repositoryBuild.getDependencyGraph().get();

    Set<Integer> downstreamModules = dependencyGraph.outgoingVertices(build.getModuleId());
    if (!downstreamModules.isEmpty()) {
      Set<ModuleBuild> builds = moduleBuildService.getByRepositoryBuild(build.getRepoBuildId());
      for (ModuleBuild maybeDownstream : builds) {
        if (downstreamModules.contains(maybeDownstream.getModuleId())) {
          ModuleBuild downstreamBuild = maybeDownstream;
          if (downstreamBuild.getState().isWaiting()) {
            LOG.info("Posting event for downstream build {}", downstreamBuild.getId().get());
            eventBus.post(downstreamBuild);
          } else {
            LOG.info("Not launching downstream build {} because it is in state {} (Needs to be QUEUED)", downstreamBuild.getId().get(), downstreamBuild.getState());
          }
        }
      }
    }
  }
}
