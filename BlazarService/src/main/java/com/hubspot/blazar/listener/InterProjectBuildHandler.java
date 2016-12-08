package com.hubspot.blazar.listener;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.google.inject.Inject;
import com.hubspot.blazar.base.BuildOptions;
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.InterProjectBuildMapping;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.AbstractInterProjectBuildVisitor;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.DependenciesService;
import com.hubspot.blazar.data.service.InterProjectBuildMappingService;
import com.hubspot.blazar.data.service.InterProjectBuildService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.util.GitHubHelper;

public class InterProjectBuildHandler extends AbstractInterProjectBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(InterProjectBuildHandler.class);
  private DependenciesService dependenciesService;
  private ModuleService moduleService;
  private ModuleBuildService moduleBuildService;
  private BranchService branchService;
  private RepositoryBuildService repositoryBuildService;
  private GitHubHelper gitHubHelper;
  private InterProjectBuildMappingService interProjectBuildMappingService;
  private InterProjectBuildService interProjectBuildService;

  @Inject
  public InterProjectBuildHandler(DependenciesService dependenciesService,
                                  ModuleService moduleService,
                                  ModuleBuildService moduleBuildService,
                                  BranchService branchService,
                                  RepositoryBuildService repositoryBuildService,
                                  GitHubHelper gitHubHelper,
                                  InterProjectBuildMappingService interProjectBuildMappingService,
                                  InterProjectBuildService interProjectBuildService) {
    this.dependenciesService = dependenciesService;
    this.moduleService = moduleService;
    this.moduleBuildService = moduleBuildService;
    this.branchService = branchService;
    this.repositoryBuildService = repositoryBuildService;
    this.gitHubHelper = gitHubHelper;
    this.interProjectBuildMappingService = interProjectBuildMappingService;
    this.interProjectBuildService = interProjectBuildService;
  }

  @Override
  protected void visitQueued(InterProjectBuild build) throws Exception {
    long start = System.currentTimeMillis();
    LOG.info("Building graph for InterProjectBuild {}", build.getId().get());
    Set<Module> s = new HashSet<>();
    for (int i : build.getModuleIds()) {
      s.add(moduleService.get(i).get());
    }
    DependencyGraph d = dependenciesService.buildInterProjectDependencyGraph(s);
    LOG.debug("Built graph for InterProjectBuild {} in {}", build.getId().get(), System.currentTimeMillis() - start);
    if (s.isEmpty()) {
      interProjectBuildService.finish(InterProjectBuild.getFinishedBuild(build, InterProjectBuild.State.SUCCEEDED));
    }
    interProjectBuildService.start(build.withDependencyGraph(d));
  }

  @Override
  protected void visitRunning(InterProjectBuild build) throws Exception {
    DependencyGraph d = build.getDependencyGraph().get();
    Set<InterProjectBuildMapping> mappings = interProjectBuildMappingService.getMappingsForInterProjectBuild(build);
    if (mappings.size() != 0 && build.getBuildTrigger().getType() == BuildTrigger.Type.PUSH) {
      LOG.info("InterProjectBuild was triggered by push, no need to launch builds, mappings exist");
      return;
    } else if (mappings.size() == 0 && build.getBuildTrigger().getType() == BuildTrigger.Type.PUSH) {
      LOG.info("InterProjectBuild was triggered by push, with no child builds triggered, marking as success");
      interProjectBuildService.finish(InterProjectBuild.getFinishedBuild(build, InterProjectBuild.State.SUCCEEDED));
      return;
    } else if (mappings.size() != 0) {
      LOG.warn("Running InterProjectBuild was launched and build mappings created outside of intended flow, Ignoring Event");
      return;
    }

    Map<Integer, Collection<Integer>> branchToLaunchableModules = getBuildableModulesPerBranch(d, build.getModuleIds());

    for (Map.Entry<Integer, Collection<Integer>> entry : branchToLaunchableModules.entrySet()) {
      Set<Integer> moduleIds = ImmutableSet.copyOf(entry.getValue());
      BuildTrigger buildTrigger = BuildTrigger.forInterProjectBuild(build);
      BuildOptions buildOptions = new BuildOptions(moduleIds, BuildOptions.BuildDownstreams.NONE, false);
      GitInfo gitInfo = branchService.get(entry.getKey()).get();
      long buildId = repositoryBuildService.enqueue(gitInfo, buildTrigger, buildOptions);
      for (int moduleId : moduleIds) {
        interProjectBuildMappingService.insert(InterProjectBuildMapping.makeNewMapping(build.getId().get(), gitInfo.getId().get(), Optional.of(buildId), moduleId));
      }
      LOG.info("Queued repo build {} as part of InterProjectBuild {}", buildId, build.getId().get());
    }
  }

  @Override
  protected void visitCancelled(InterProjectBuild build) throws Exception {
    // Cancelling in-progress repository Builds will cause the cancellation of modules which will cancel the rest of the tree in InterProjectModuleBuildVisitor
    Set<InterProjectBuildMapping> mappings = interProjectBuildMappingService.getMappingsForInterProjectBuild(build);
    for (InterProjectBuildMapping mapping : mappings) {
      if (!mapping.getState().isFinished()) {
        RepositoryBuild repositoryBuild = repositoryBuildService.get(mapping.getRepoBuildId().get()).get();
        repositoryBuildService.cancel(repositoryBuild);
      }
    }
  }

  /**
   *
   * For each part of an InterProjectBuild we launch a branch builds (repository build)
   * with a specific set of modules that are to be built. When a module finishes we check
   * if any downstream dependencies can be started, and start those at that time. This
   * method helps us launch as many modules as we can (per branch) before the feedback loop
   * of builds causing other builds to launch has been started.
   *
   * Because of certain dependency relationships between projects you cannot guarantee that
   * all modules in the graph in a given branch can be built together. Sometimes you must build
   * half of the modules in the branch, then build some external dependency, and then build the rest
   * of the modules in the branch.
   *
   * This method figures out which modules we can launch on a per-branch basis at the start of the InterProjectBuild
   */
  private Map<Integer, Collection<Integer>> getBuildableModulesPerBranch(DependencyGraph graph, Set<Integer> originallyTriggeredModuleIds) {
    SetMultimap<Integer, Integer> branchIdToLaunchableModules = HashMultimap.create();
    // Modules with no upstreams in our graph become the 'root' nodes from which the InterProject build will spread
    Set<Integer> rootModules = originallyTriggeredModuleIds.stream().filter(moduleId -> graph.incomingVertices(moduleId).isEmpty()).collect(Collectors.toSet());

    for (int rootModule : rootModules) {
      int branchId = moduleService.getBranchIdFromModuleId(rootModule);
      // add this to the map because we want to build it
      branchIdToLaunchableModules.put(branchId, rootModule);

      /*
       * To be able to start a build of a downstream module of this root module in the next repository build:
       *  1. It must share a branch with this one
       *     If it does not then it will be started either by a different root module, or at a later time
       *
       *  2. All of the downstream module's dependencies must share a branch with this one
       *     If they do not then we need to wait for them to be build to build the next module -- so we cannot build it now
       */
      Set<Integer> moduleIdsForBranch = moduleService.getByBranch(branchId).stream().map(module -> module.getId().get()).collect(Collectors.toSet());
      Set<Integer> downstreamModules = graph.reachableVertices(rootModule);  // all downstream nodes in the graph (recursive)

      for (int downstreamModuleId : downstreamModules) {
        if (moduleIdsForBranch.contains(downstreamModuleId) && // condition 1
            moduleIdsForBranch.containsAll(graph.getAllUpstreamNodes(downstreamModuleId))) {  // condition 2
          branchIdToLaunchableModules.put(branchId, downstreamModuleId);
        }
      }
    }
    return branchIdToLaunchableModules.asMap();
  }
}
