package com.hubspot.blazar.listener;

import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.listener.RepositoryBuildListener;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.util.GitHubHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.FileSystems;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class LaunchingRepositoryBuildListener implements RepositoryBuildListener {
  private static final Logger LOG = LoggerFactory.getLogger(LaunchingRepositoryBuildListener.class);

  private final ModuleBuildService moduleBuildService;
  private final ModuleService moduleService;
  private final GitHubHelper gitHubHelper;

  @Inject
  public LaunchingRepositoryBuildListener(ModuleBuildService moduleBuildService,
                                          ModuleService moduleService,
                                          GitHubHelper gitHubHelper) {
    this.moduleBuildService = moduleBuildService;
    this.moduleService = moduleService;
    this.gitHubHelper = gitHubHelper;
  }

  @Override
  public void buildChanged(RepositoryBuild build) {
    LOG.info("Going to enqueue module builds for repository build {}", build.getId().get());

    Set<Module> modules = moduleService.getByBranch(build.getBranchId());
    Set<Module> toBuild = findModulesToBuild(build.getCommitInfo().get(), modules);

    for (Module module : toBuild) {
      moduleBuildService.enqueue(build, module);
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
