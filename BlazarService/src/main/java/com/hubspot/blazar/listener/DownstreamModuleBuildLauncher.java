package com.hubspot.blazar.listener;

import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuild.State;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.listener.ModuleBuildListener;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.util.ModuleBuildLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Singleton
public class DownstreamModuleBuildLauncher implements ModuleBuildListener {
  private static final Logger LOG = LoggerFactory.getLogger(DownstreamModuleBuildLauncher.class);

  private final RepositoryBuildService repositoryBuildService;
  private final ModuleBuildService moduleBuildService;
  private final ModuleBuildLauncher moduleBuildLauncher;

  @Inject
  public DownstreamModuleBuildLauncher(RepositoryBuildService repositoryBuildService,
                                       ModuleBuildService moduleBuildService,
                                       ModuleBuildLauncher moduleBuildLauncher) {
    this.repositoryBuildService = repositoryBuildService;
    this.moduleBuildService = moduleBuildService;
    this.moduleBuildLauncher = moduleBuildLauncher;
  }

  @Override
  public void buildChanged(ModuleBuild build) throws Exception {
    LOG.info("Checking for builds downstream of {}", build.getId().get());

    RepositoryBuild repositoryBuild = repositoryBuildService.get(build.getRepoBuildId()).get();
    DependencyGraph dependencyGraph = repositoryBuild.getDependencyGraph().get();

    Set<Integer> downstreamModules = dependencyGraph.outgoingVertices(build.getModuleId());
    if (!downstreamModules.isEmpty()) {
      Set<ModuleBuild> builds = moduleBuildService.getByRepositoryBuild(build.getRepoBuildId());
      Map<Integer, ModuleBuild> buildMap = mapByModuleId(builds);
      for (int downstreamModule : downstreamModules) {
        ModuleBuild downstreamBuild = buildMap.get(downstreamModule);
        if (downstreamBuild.getState() != State.QUEUED) {
          LOG.info("Not launching downstream build {} because it is in state {} (Needs to be QUEUED)", downstreamBuild.getId().get(), downstreamBuild.getState());
          continue;
        }

        Set<Integer> upstreamModules = dependencyGraph.incomingVertices(downstreamModule);

        boolean ready = true;
        for (int upstreamModule : upstreamModules) {
          ModuleBuild upstreamBuild = buildMap.get(upstreamModule);
          Set<State> allowedUpstreamStates = EnumSet.of(State.SUCCEEDED, State.SKIPPED);
          if (upstreamBuild != null && !allowedUpstreamStates.contains(upstreamBuild.getState())) {
            LOG.info("Not launching downstream build {} because upstream build {} is in state {} (Needs to be one of {})", downstreamBuild.getId().get(), upstreamBuild.getId().get(), upstreamBuild.getState(), allowedUpstreamStates);
            ready = false;
            break;
          }
        }

        if (ready) {
          LOG.info("Going to launch build {} which is downstream of {}", downstreamBuild.getId().get(), build.getId().get());
          moduleBuildLauncher.launch(repositoryBuild, downstreamBuild);
        }
      }
    }
  }

  private static Map<Integer, ModuleBuild> mapByModuleId(Set<ModuleBuild> builds) {
    Map<Integer, ModuleBuild> buildMap = new HashMap<>();
    for (ModuleBuild build : builds) {
      buildMap.put(build.getModuleId(), build);
    }

    return buildMap;
  }
}
