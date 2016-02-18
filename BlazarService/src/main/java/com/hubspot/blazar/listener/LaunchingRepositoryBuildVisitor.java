package com.hubspot.blazar.listener;

import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.hubspot.blazar.base.BuildOptions.BuildDownstreams;
import com.hubspot.blazar.base.BuildTrigger.Type;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.RepositoryBuild.State;
import com.hubspot.blazar.base.visitor.AbstractRepositoryBuildVisitor;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.util.GitHubHelper;

@Singleton
public class LaunchingRepositoryBuildVisitor extends AbstractRepositoryBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(LaunchingRepositoryBuildVisitor.class);

  private final RepositoryBuildService repositoryBuildService;
  private final ModuleBuildService moduleBuildService;
  private final ModuleService moduleService;
  private final GitHubHelper gitHubHelper;

  @Inject
  public LaunchingRepositoryBuildVisitor(RepositoryBuildService repositoryBuildService,
                                         ModuleBuildService moduleBuildService,
                                         ModuleService moduleService,
                                         GitHubHelper gitHubHelper) {
    this.repositoryBuildService = repositoryBuildService;
    this.moduleBuildService = moduleBuildService;
    this.moduleService = moduleService;
    this.gitHubHelper = gitHubHelper;
  }

  @Override
  protected void visitLaunching(RepositoryBuild build) throws Exception {
    LOG.info("Going to enqueue module builds for repository build {}", build.getId().get());

    Set<Module> modules = filterActive(moduleService.getByBranch(build.getBranchId()));
    Set<Module> toBuild = findModulesToBuild(build, modules);
    Set<Module> skipped = Sets.difference(modules, toBuild);

    if (modules.isEmpty()) {
      LOG.info("No module builds for repository build {}, setting status to success", build.getId().get());
      repositoryBuildService.update(build.withState(State.SUCCEEDED).withEndTimestamp(System.currentTimeMillis()));
    } else {
      for (Module module : toBuild) {
        moduleBuildService.enqueue(build, module);
      }

      for (Module module : skipped) {
        moduleBuildService.skip(build, module);
      }

      repositoryBuildService.update(build.withState(State.IN_PROGRESS));
    }
  }

  private Set<Module> findModulesToBuild(RepositoryBuild build, Set<Module> allModules) {
    CommitInfo commitInfo = build.getCommitInfo().get();

    final Set<Module> toBuild = new HashSet<>();
    if (build.getBuildTrigger().getType() == Type.MANUAL) {
      if (build.getBuildOptions().getModuleIds().isEmpty()) {
        toBuild.addAll(allModules);
      } else {
        final Set<Integer> requestedModuleIds = build.getBuildOptions().getModuleIds();
        for (Module module : allModules) {
          if (requestedModuleIds.contains(module.getId().get())) {
            toBuild.add(module);
          }
        }

        addDownstreamModules(build, allModules, toBuild);
      }
    } else if (build.getBuildTrigger().getType() == Type.BRANCH_CREATION) {
      toBuild.addAll(allModules);
    } else if (commitInfo.isTruncated()) {
      toBuild.addAll(allModules);
    } else {
      for (String path : gitHubHelper.affectedPaths(commitInfo)) {
        for (Module module : allModules) {
          if (module.contains(FileSystems.getDefault().getPath(path))) {
            toBuild.add(module);
          }
        }
      }

      addDownstreamModules(build, allModules, toBuild);
    }

    return toBuild;
  }

  private void addDownstreamModules(RepositoryBuild build, Set<Module> allModules, Set<Module> toBuild) {
    if (build.getBuildOptions().getBuildDownstreams().equals(BuildDownstreams.NONE)) {
      return;
    }

    Map<Integer, Module> moduleMap = mapByModuleId(allModules);
    DependencyGraph dependencyGraph = build.getDependencyGraph().get();
    LOG.info("All active modules: {}", moduleMap.keySet());
    LOG.info("Modules directly selected for build (changed or selected by user): {}", mapByModuleId(toBuild).keySet());
    LOG.info("Transitive reduction: {}", dependencyGraph.getTransitiveReduction());
    for (Module module : ImmutableSet.copyOf(toBuild)) {
      for (int downstreamModule : dependencyGraph.reachableVertices(module.getId().get())) {
        toBuild.add(moduleMap.get(downstreamModule));
      }
    }
    LOG.info("All modules to build (including downstream dependencies): {}", mapByModuleId(toBuild).keySet());
  }

  private static Set<Module> filterActive(Set<Module> modules) {
    Set<Module> filtered = new HashSet<>();
    for (Module module : modules) {
      if (module.isActive()) {
        filtered.add(module);
      }
    }

    return filtered;
  }

  private static Map<Integer, Module> mapByModuleId(Set<Module> modules) {
    Map<Integer, Module> moduleMap = new HashMap<>();
    for (Module module : modules) {
      moduleMap.put(module.getId().get(), module);
    }

    return moduleMap;
  }
}
