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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.hubspot.blazar.base.BuildOptions.BuildDownstreams;
import com.hubspot.blazar.base.BuildTrigger.Type;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.InterProjectBuildMapping;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.RepositoryBuild.State;
import com.hubspot.blazar.base.visitor.AbstractRepositoryBuildVisitor;
import com.hubspot.blazar.data.service.DependenciesService;
import com.hubspot.blazar.data.service.InterProjectBuildMappingService;
import com.hubspot.blazar.data.service.InterProjectBuildService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.util.GitHubHelper;

@Singleton
public class LaunchingRepositoryBuildVisitor extends AbstractRepositoryBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(LaunchingRepositoryBuildVisitor.class);

  private final RepositoryBuildService repositoryBuildService;
  private final ModuleBuildService moduleBuildService;
  private InterProjectBuildService interProjectBuildService;
  private InterProjectBuildMappingService interProjectBuildMappingService;
  private final ModuleService moduleService;
  private DependenciesService dependenciesService;
  private final GitHubHelper gitHubHelper;

  @Inject
  public LaunchingRepositoryBuildVisitor(RepositoryBuildService repositoryBuildService,
                                         ModuleBuildService moduleBuildService,
                                         InterProjectBuildService interProjectBuildService,
                                         InterProjectBuildMappingService interProjectBuildMappingService,
                                         ModuleService moduleService,
                                         DependenciesService dependenciesService,
                                         GitHubHelper gitHubHelper) {
    this.repositoryBuildService = repositoryBuildService;
    this.moduleBuildService = moduleBuildService;
    this.interProjectBuildService = interProjectBuildService;
    this.interProjectBuildMappingService = interProjectBuildMappingService;
    this.moduleService = moduleService;
    this.dependenciesService = dependenciesService;
    this.gitHubHelper = gitHubHelper;
  }

  @Override
  protected void visitLaunching(RepositoryBuild build) throws Exception {
    LOG.info("Going to enqueue module builds for repository build {}", build.getId().get());

    Set<Module> modules = filterActive(moduleService.getByBranch(build.getBranchId()));
    Set<Module> toBuild = findModulesToBuild(build, modules);

    Optional<Long> interProjectBuildId = Optional.absent();
    if (build.getBuildOptions().getBuildDownstreams() == BuildDownstreams.INTER_PROJECT) {
      Set<Integer> allModuleIds = getIds(modules);
      Set<Module> realToBuildModules = new HashSet<>();
      DependencyGraph interProjectGraph = dependenciesService.buildInterProjectDependencyGraph(toBuild);
      for (Module rootModule : toBuild)  {
        Set<Integer> upstreams = interProjectGraph.getAllUpstreamNodes(rootModule.getId().get());
        // this module has no incoming modules outside this repo, so we're building it in this repoBuild
        if (allModuleIds.containsAll(upstreams)) {
          realToBuildModules.add(rootModule);
        }
        // find all downstream this root module would trigger
        // if they (and their upstreams) are in this repo we can also build them now
        for (int downstream : interProjectGraph.reachableVertices(rootModule.getId().get())) {
          boolean sameBranch = moduleService.getBranchIdFromModuleId(downstream) == build.getBranchId();
          boolean noExternalUpstreams = allModuleIds.containsAll(interProjectGraph.getAllUpstreamNodes(downstream));
          if (sameBranch && noExternalUpstreams) {
            realToBuildModules.add(moduleService.get(downstream).get());
          }
        }
      }
      toBuild = realToBuildModules;
      InterProjectBuild ipb = InterProjectBuild.getQueuedBuild(ImmutableSet.copyOf(getIds(realToBuildModules)), build.getBuildTrigger());
      interProjectBuildId = Optional.of(interProjectBuildService.enqueue(ipb));
    }
    // Only calculate skipped modules after we know what modules will build
    Set<Module> skipped = Sets.difference(modules, toBuild);

    if (modules.isEmpty()) {
      LOG.info("No module builds for repository build {}, setting status to failed", build.getId().get());
      repositoryBuildService.update(build.withState(State.FAILED).withEndTimestamp(System.currentTimeMillis()));
    } else {
      for (Module module : toBuild) {
        moduleBuildService.enqueue(build, module);
        if (build.getBuildOptions().getBuildDownstreams() == BuildDownstreams.INTER_PROJECT) {
          interProjectBuildMappingService.insert(InterProjectBuildMapping.makeNewMapping(interProjectBuildId.get(), build.getBranchId(), build.getId(), module.getId().get()));
        }
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
    if (build.getBuildTrigger().getType() == Type.PUSH) {
      if (commitInfo.isTruncated()) {
        toBuild.addAll(allModules);
      } else {
        for (String path : gitHubHelper.affectedPaths(commitInfo)) {
          for (Module module : allModules) {
            if (module.contains(FileSystems.getDefault().getPath(path))) {
              toBuild.add(module);
            } else if (!lastBuildSucceeded(module)) {
              toBuild.add(module);
            }
          }
        }
      }
    } else if (build.getBuildOptions().getModuleIds().isEmpty()) {
      toBuild.addAll(allModules);
    } else {
      final Set<Integer> requestedModuleIds = build.getBuildOptions().getModuleIds();
      for (Module module : allModules) {
        if (requestedModuleIds.contains(module.getId().get())) {
          toBuild.add(module);
        }
      }
    }

    if (build.getBuildOptions().getBuildDownstreams() == BuildDownstreams.WITHIN_REPOSITORY) {
      addDownstreamModules(build, allModules, toBuild);
    }

    return toBuild;
  }

  private void addDownstreamModules(RepositoryBuild build, Set<Module> allModules, Set<Module> toBuild) {
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

  private boolean lastBuildSucceeded(Module module) {
    Optional<ModuleBuild> previous = moduleBuildService.getPreviousBuild(module);
    return previous.isPresent() && previous.get().getState() == ModuleBuild.State.SUCCEEDED;
  }

  private static Set<Integer> getIds(Set<Module> modules) {
    Set<Integer> ints = new HashSet<>();
    for (Module m : modules) {
      ints.add(m.getId().get());
    }
    return ints;
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
