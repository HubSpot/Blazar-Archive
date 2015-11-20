package com.hubspot.blazar.util;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.FileSystems;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class ModuleBuildLauncher {
  private static final Logger LOG = LoggerFactory.getLogger(BuildLauncher.class);

  private final RepositoryBuildService repositoryBuildService;
  private final ModuleBuildService moduleBuildService;
  private final ModuleService moduleService;
  private final GitHubHelper gitHubHelper;

  @Inject
  public ModuleBuildLauncher(RepositoryBuildService repositoryBuildService,
                             ModuleBuildService moduleBuildService,
                             ModuleService moduleService,
                             GitHubHelper gitHubHelper,
                             EventBus eventBus) {
    this.repositoryBuildService = repositoryBuildService;
    this.moduleBuildService = moduleBuildService;
    this.moduleService = moduleService;
    this.gitHubHelper = gitHubHelper;

    eventBus.register(this);
  }

  @Subscribe
  public void handleRepositoryBuild(RepositoryBuild build) throws Exception {
    LOG.info("Received event for build {} with state {}", build.getId().get(), build.getState());

    if (build.getState() == RepositoryBuild.State.LAUNCHING) {
      Set<Module> modules = moduleService.getByBranch(build.getBranchId());
      Set<Module> toBuild = findModulesToBuild(build.getCommitInfo().get(), modules);

      for (Module module : toBuild) {
        moduleBuildService.enqueue(build, module);
      }
    }
  }

  @Subscribe
  public void handleModuleBuild(ModuleBuild build) throws Exception {
    switch (build.getState()) {
      case QUEUED:
        RepositoryBuild repositoryBuild = repositoryBuildService.get(build.getRepoBuildId()).get();
        if (dependencyGraph(build).upstreamVertices(build.getModuleId()).isEmpty()) {
          // launch build
        }
        break;
      case SUCCEEDED:
        Set<Integer> downstreamModules = dependencyGraph(build).reachableVertices(build.getModuleId());
        if (downstreamModules.isEmpty()) {
          // check if we're the last module, complete repository build if so (maybe move to RepositoryBuildLauncher?)
        } else {
          for (int downstreamModule : dependencyGraph(build).reachableVertices(build.getModuleId())) {
            // launch build if all upstreams are done and still queued
          }
        }
        break;
      case FAILED:
        // cancel all downstream modules
        for (int downstreamModule : dependencyGraph(build).reachableVertices(build.getModuleId())) {
          // cancel build if queued
        }
        break;
    }
  }

  private DependencyGraph dependencyGraph(ModuleBuild build) {
    RepositoryBuild repositoryBuild = repositoryBuildService.get(build.getRepoBuildId()).get();
    return repositoryBuild.getDependencyGraph().get();
  }

  private Set<Module> findModulesToBuild(CommitInfo commitInfo, Set<Module> modules) {
    final Set<Module> toBuild = new HashSet<>();
    if (commitInfo.isTruncated()) {
      toBuild.addAll(modules);
    } else {
      for (String path : gitHubHelper.affectedPaths(commitInfo)) {
        for (Module module : modules) {
          if (module.contains(FileSystems.getDefault().getPath(path))) {
            toBuild.add(module);
          }
        }
      }
    }

    return toBuild;
  }
}
