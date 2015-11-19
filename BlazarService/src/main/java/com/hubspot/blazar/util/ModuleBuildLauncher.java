package com.hubspot.blazar.util;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.RepositoryBuild.State;
import com.hubspot.blazar.data.service.ModuleService;
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

  private final ModuleService moduleService;
  private final GitHubHelper gitHubHelper;

  @Inject
  public ModuleBuildLauncher(ModuleService moduleService,
                             GitHubHelper gitHubHelper,
                             EventBus eventBus) {
    this.moduleService = moduleService;
    this.gitHubHelper = gitHubHelper;

    eventBus.register(this);
  }

  @Subscribe
  public void handleBuildChange(RepositoryBuild build) throws Exception {
    LOG.info("Received event for build {} with state {}", build.getId().get(), build.getState());

    if (build.getState() == State.LAUNCHING) {
      Set<Module> modules = moduleService.getByBranch(build.getBranchId());
      Set<Module> toBuild = findModulesToBuild(build.getCommitInfo().get(), modules);
      Set<Module> buildNow = build.getDependencyGraph().get().reduceModules(toBuild);
      for (Module module : buildNow) {
        // trigger builds
      }
    }
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
