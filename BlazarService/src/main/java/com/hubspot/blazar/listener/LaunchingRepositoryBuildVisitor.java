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
import com.hubspot.blazar.data.service.MalformedFileService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.util.GitHubHelper;

@Singleton
public class LaunchingRepositoryBuildVisitor extends AbstractRepositoryBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(LaunchingRepositoryBuildVisitor.class);

  private final RepositoryBuildService repositoryBuildService;
  private final ModuleBuildService moduleBuildService;
  private MalformedFileService malformedFileService;
  private InterProjectBuildService interProjectBuildService;
  private InterProjectBuildMappingService interProjectBuildMappingService;
  private final ModuleService moduleService;
  private DependenciesService dependenciesService;
  private final GitHubHelper gitHubHelper;

  @Inject
  public LaunchingRepositoryBuildVisitor(RepositoryBuildService repositoryBuildService,
                                         ModuleBuildService moduleBuildService,
                                         MalformedFileService malformedFileService,
                                         InterProjectBuildService interProjectBuildService,
                                         InterProjectBuildMappingService interProjectBuildMappingService,
                                         ModuleService moduleService,
                                         DependenciesService dependenciesService,
                                         GitHubHelper gitHubHelper) {
    this.repositoryBuildService = repositoryBuildService;
    this.moduleBuildService = moduleBuildService;
    this.malformedFileService = malformedFileService;
    this.interProjectBuildService = interProjectBuildService;
    this.interProjectBuildMappingService = interProjectBuildMappingService;
    this.moduleService = moduleService;
    this.dependenciesService = dependenciesService;
    this.gitHubHelper = gitHubHelper;
  }

  /**
   * This method launches a branch build this involves:
   * 1. Checking that there are no malformed files (We fail the build in this case)
   * 2. Checking that there are any modules to build (We fail the build in this case)
   * 3. Calculate which modules to build (Depends on what kind of build it is)
   * 4. Enqueue builds (If this is a InterProject build create the InterProjectBuild mappings)
   * 5. Mark any modules that are not Enqueued to build as `Skipped`
   * 6. Update the state of this branch build
   */
  @Override
  protected void visitLaunching(RepositoryBuild build) throws Exception {
    LOG.info("Going to enqueue module builds for repository build {}", build.getId().get());

    final Set<Module> activeModules = filterActive(moduleService.getByBranch(build.getBranchId()));

    // 1. Check for malformed files
    if (!malformedFileService.getMalformedFiles(build.getBranchId()).isEmpty()) {
      failBranchAndModuleBuilds(build, activeModules);
      return;
    }

    // 2. Check for buildable modules
    if (activeModules.isEmpty()) {
      LOG.info("No active modules to build in branch {} - failing build", build.getId().get());
      repositoryBuildService.fail(build);
    }

    final Optional<Long> interProjectBuildId;
    final Set<Module> toBuild;

    // 3. The modules we choose to build depends on if this is an InterProject build or not
    //    If this is an InterProject build we enqueue one of those as well.
    if (build.getBuildOptions().getBuildDownstreams() == BuildDownstreams.INTER_PROJECT) {
      toBuild = determineModulesToBuildUsingInterProjectBuildGraph(build, activeModules);
      InterProjectBuild ipb = InterProjectBuild.getQueuedBuild(ImmutableSet.copyOf(getIds(toBuild)), build.getBuildTrigger());
      interProjectBuildId = Optional.of(interProjectBuildService.enqueue(ipb));
    } else {
      interProjectBuildId = Optional.absent();
      toBuild = findModulesToBuild(build, activeModules);
    }

    // 4. Launch the modules we want to build
    for (Module module : build.getDependencyGraph().get().orderByTopologicalSort(toBuild)) {
      moduleBuildService.enqueue(build, module);
      if (build.getBuildOptions().getBuildDownstreams() == BuildDownstreams.INTER_PROJECT) {
        interProjectBuildMappingService.insert(InterProjectBuildMapping.makeNewMapping(interProjectBuildId.get(), build.getBranchId(), build.getId(), module.getId().get()));
      }
    }

    // 5. Only calculate skipped modules after we know what modules will build
    Set<Module> skipped = Sets.difference(activeModules, toBuild);
    for (Module module : skipped) {
      moduleBuildService.skip(build, module);
    }

    // 6. Update the state of this branch build.
    repositoryBuildService.update(build.toBuilder().setState(State.IN_PROGRESS).build());
  }

  private void failBranchAndModuleBuilds(RepositoryBuild build, Set<Module> activeModules) {
    LOG.info("Malformed file on branch {} -- failing build {}", build.getBranchId(), build.getId().get());
    for (Module module : activeModules) {
      moduleBuildService.createFailedBuild(build, module);
    }
    repositoryBuildService.fail(build);
  }

  /**
   * Because there can be dependency chains between projects' modules that go back and forth.
   * InterProjectBuilds can require that a single repository is triggered more than one time.
   * This method determines which modules can be built in the first build of an inter-project graph.
   */
  private Set<Module> determineModulesToBuildUsingInterProjectBuildGraph(RepositoryBuild build, Set<Module> activeModules) {
    Set<Integer> allModuleIds = getIds(activeModules);
    Set<Module> toBuild = findModulesToBuild(build, activeModules);

    Set<Module> interProjectModulesToBuild = new HashSet<>();
    DependencyGraph interProjectGraph = dependenciesService.buildInterProjectDependencyGraph(toBuild);
    for (Module rootModule : toBuild) {
      Set<Integer> upstreams = interProjectGraph.getAllUpstreamNodes(rootModule.getId().get());
      // this module has no incoming modules outside this repo, so we're building it in this repoBuild
      if (allModuleIds.containsAll(upstreams)) {
        interProjectModulesToBuild.add(rootModule);
      }
      // find all downstream this root module would trigger
      // if they (and their upstreams) are in this repo we can also build them now
      for (int downstream : interProjectGraph.reachableVertices(rootModule.getId().get())) {
        boolean sameBranch = moduleService.getBranchIdFromModuleId(downstream) == build.getBranchId();
        boolean noExternalUpstreams = allModuleIds.containsAll(interProjectGraph.getAllUpstreamNodes(downstream));
        if (sameBranch && noExternalUpstreams) {
          interProjectModulesToBuild.add(moduleService.get(downstream).get());
        }
      }
    }
    return interProjectModulesToBuild;
  }


  private Set<Module> findModulesToBuild(RepositoryBuild build, Set<Module> buildableModules) {
    CommitInfo commitInfo = build.getCommitInfo().get();

    final Set<Module> toBuild = new HashSet<>();
    if (build.getBuildTrigger().getType() == Type.PUSH) {
      if (commitInfo.isTruncated()) {
        toBuild.addAll(buildableModules);
      } else {
        for (String path : gitHubHelper.affectedPaths(commitInfo)) {
          for (Module module : buildableModules) {
            if (module.contains(FileSystems.getDefault().getPath(path))) {
              toBuild.add(module);
            } else if (!lastBuildSucceeded(module)) {
              toBuild.add(module);
            }
          }
        }
      }
    } else if (build.getBuildOptions().getModuleIds().isEmpty()) {
      toBuild.addAll(buildableModules);
    } else {
      final Set<Integer> requestedModuleIds = build.getBuildOptions().getModuleIds();
      for (Module module : buildableModules) {
        if (requestedModuleIds.contains(module.getId().get())) {
          toBuild.add(module);
        }
      }
    }

    if (build.getBuildOptions().getBuildDownstreams() == BuildDownstreams.WITHIN_REPOSITORY) {
      addDownstreamModules(build, buildableModules, toBuild);
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
