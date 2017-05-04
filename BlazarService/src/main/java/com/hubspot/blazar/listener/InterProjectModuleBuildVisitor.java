package com.hubspot.blazar.listener;

import static com.hubspot.blazar.base.InterProjectBuild.State.CANCELLED;
import static com.hubspot.blazar.base.InterProjectBuild.State.FAILED;
import static com.hubspot.blazar.base.InterProjectBuild.State.SUCCEEDED;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.hubspot.blazar.base.BuildOptions;
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.InterProjectBuildMapping;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.visitor.AbstractModuleBuildVisitor;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.InterProjectBuildMappingService;
import com.hubspot.blazar.data.service.InterProjectBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.exception.NonRetryableBuildException;

public class InterProjectModuleBuildVisitor extends AbstractModuleBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(InterProjectModuleBuildVisitor.class);
  private final ModuleService moduleService;
  private BranchService branchService;
  private RepositoryBuildService repositoryBuildService;
  private final InterProjectBuildService interProjectBuildService;
  private final InterProjectBuildMappingService interProjectBuildMappingService;

  @Inject
  public InterProjectModuleBuildVisitor(ModuleService moduleService,
                                        BranchService branchService,
                                        RepositoryBuildService repositoryBuildService,
                                        InterProjectBuildService interProjectBuildService,
                                        InterProjectBuildMappingService interProjectBuildMappingService) {
    this.moduleService = moduleService;
    this.branchService = branchService;
    this.repositoryBuildService = repositoryBuildService;
    this.interProjectBuildService = interProjectBuildService;
    this.interProjectBuildMappingService = interProjectBuildMappingService;
  }

  @Override
  protected void visitSucceeded(ModuleBuild build) throws Exception {
    Optional<InterProjectBuildMapping> mapping = interProjectBuildMappingService.getByModuleBuildId(build.getId().get());
    if (!mapping.isPresent()) {
      return;
    }
    InterProjectBuildMapping updatedMapping = mapping.get().withModuleBuildId(InterProjectBuild.State.SUCCEEDED);
    interProjectBuildMappingService.updateBuilds(updatedMapping);
    LOG.info("ModuleBuild {} with IPB mapping {} was successful looking for child builds to start", build.getId(), mapping);
    buildChildren(build, updatedMapping);
    LOG.info("Checking if module build {} was las build in IPB with mapping {}", build.getId().get(), mapping);
    checkAndCompleteInterProjectBuild(build, mapping.get().getInterProjectBuildId());
  }

  @Override
  protected void visitCancelled(ModuleBuild build) throws Exception {
    Optional<InterProjectBuildMapping> mapping = interProjectBuildMappingService.getByModuleBuildId(build.getId().get());
    if (!mapping.isPresent()) {
      return;
    }
    visitCancelledOrFailed(CANCELLED, build, mapping.get());
  }

  @Override
  protected void visitFailed(ModuleBuild build) throws Exception {
    Optional<InterProjectBuildMapping> mapping = interProjectBuildMappingService.getByModuleBuildId(build.getId().get());
    if (!mapping.isPresent()) {
      return;
    }
    visitCancelledOrFailed(FAILED, build, mapping.get());
  }

  private void visitCancelledOrFailed(InterProjectBuild.State state, ModuleBuild build, InterProjectBuildMapping mapping) throws Exception {
    InterProjectBuildMapping updatedMapping = mapping.withModuleBuildId(state);
    interProjectBuildMappingService.updateBuilds(updatedMapping);
    LOG.info("ModuleBuild {} with IPB mapping {} {} looking for child nodes to cancel", build.getId(), updatedMapping.getState(), mapping);
    cancelSubTree(build, updatedMapping);
    LOG.info("Checking if module build {} was last build in IPB with mapping {}", build.getId().get(), mapping);
    checkAndCompleteInterProjectBuild(build, mapping.getInterProjectBuildId());
  }

  /**
   * Here we find the child nodes in the graph that can be built and build them.
   * In order to not start extra repository builds we also find "chains" of dependencies that
   * are in the same repository & branch and build those together in the same repository build.
   */
  private void buildChildren(ModuleBuild build, InterProjectBuildMapping mapping) {
    InterProjectBuild interProjectBuild = interProjectBuildService.getWithId(mapping.getInterProjectBuildId()).get();
    DependencyGraph graph = interProjectBuild.getDependencyGraph().get();
    Map<Integer, InterProjectBuildMapping> mappingsForInterProjectBuildByModuleId =
        interProjectBuildMappingService.getMappingsForInterProjectBuildByModuleId(mapping.getInterProjectBuildId());

    // We group modules to be launched by branch so we can start one repository build per branch with all the modules.
    SetMultimap<Integer, Integer> launchableBranchToModuleMap = HashMultimap.create();

    for (int moduleId : graph.reachableVertices(build.getModuleId())) {
      int branchId = moduleService.getBranchIdFromModuleId(moduleId);
      if (shouldBuildModule(moduleId, branchId, mapping.getInterProjectBuildId(), graph, mappingsForInterProjectBuildByModuleId)) {
        launchableBranchToModuleMap.put(branchId, moduleId);
      }
    }

    LOG.info("Found {} modules downstream of {} that can be started", launchableBranchToModuleMap.size(), mapping);

    for (Map.Entry<Integer, Set<Integer>> entry : Multimaps.asMap(launchableBranchToModuleMap).entrySet()) {
      Set<Integer> launchableModules = entry.getValue();
      GitInfo gitInfo = branchService.get(entry.getKey()).get();
      BuildTrigger buildTrigger = BuildTrigger.forInterProjectBuild(interProjectBuild.getId().get());
      BuildOptions buildOptions = new BuildOptions(launchableModules, BuildOptions.BuildDownstreams.NONE, false);
      long buildId = repositoryBuildService.enqueue(gitInfo, buildTrigger, buildOptions);
      for (Integer moduleId : launchableModules) {
        interProjectBuildMappingService.insert(InterProjectBuildMapping.makeNewMapping(interProjectBuild.getId().get(), gitInfo.getId().get(), Optional.of(buildId), moduleId));
      }
      LOG.debug("Queued repo build {} as part of InterProjectBuild {}", buildId, interProjectBuild.getId().get());
    }
  }

  private boolean shouldBuildModule(int moduleId, int branchId, long interProjectBuildId, DependencyGraph graph, Map<Integer, InterProjectBuildMapping> mappingsForInterProjectBuildByModuleId) {
    // don't start builds of modules that have mappings
    if (mappingsForInterProjectBuildByModuleId.containsKey(moduleId)) {
      LOG.debug("Not starting inter project build for module {} because mapping for it exists {}", moduleId, mappingsForInterProjectBuildByModuleId.get(moduleId));
      return false;
    }
    // Check all upstreams complete & successful
    for (Integer upstream: graph.getAllUpstreamNodes(moduleId)) {
      if (!checkThatAllUpstreamsSucceedOrShareBranchId(upstream, branchId, mappingsForInterProjectBuildByModuleId)) {
        LOG.info("Not starting inter project build for module {} because upstreams not complete", moduleId);
        return false;
      }
    }
    LOG.debug("Found that module {} on branch {} part of IPB {} should be built", moduleId, branchId, interProjectBuildId);
    return true;
  }

  private boolean checkThatAllUpstreamsSucceedOrShareBranchId(int moduleId, int branchId, Map<Integer, InterProjectBuildMapping> mappingsForInterProjectBuildByModuleId) {
    // See if we've started a build for this
    InterProjectBuildMapping mappingFound = mappingsForInterProjectBuildByModuleId.get(moduleId);
    if (mappingFound == null && branchId == moduleService.getBranchIdFromModuleId(moduleId)) {
      return true;
    }
    return mappingFound != null && mappingFound.getState().equals(InterProjectBuild.State.SUCCEEDED);
  }

  /**
   * Create interProjectBuild mappings for all modules that are downstream of this one
   * with state `CANCELLED`. This allows us to see what builds were supposed to run
   * according to the inter project graph but did not get executed.
   */
  private void cancelSubTree(ModuleBuild build, InterProjectBuildMapping mapping) throws NonRetryableBuildException {
    long interProjectBuildId = mapping.getInterProjectBuildId();
    InterProjectBuild interProjectBuild = interProjectBuildService.getWithId(interProjectBuildId).get();
    LOG.info("Canceling builds dependent on {} matching IPB mapping {}", build.getId(), mapping);

    Map<Integer, InterProjectBuildMapping> mappingsForInterProjectBuildByModuleId =
        interProjectBuildMappingService.getMappingsForInterProjectBuildByModuleId(interProjectBuildId);

    DependencyGraph graph = interProjectBuild.getDependencyGraph().get();
    Deque<Integer> deque = new ArrayDeque<>();
    Set<Integer> visited = new HashSet<>();
    deque.addAll(graph.outgoingVertices(build.getModuleId()));
    while (!deque.isEmpty()) {
      int moduleId = deque.pop();
      boolean mappingExists = mappingsForInterProjectBuildByModuleId.containsKey(moduleId);
      if (visited.add(moduleId) && !mappingExists) {
        LOG.info("Module {} was downstream of {} module {} in IPB {} creating cancelled mapping");
        interProjectBuildMappingService.insert(new InterProjectBuildMapping(Optional.<Long>absent(), interProjectBuild.getId().get(), moduleService.getBranchIdFromModuleId(moduleId), Optional.<Long>absent(), moduleId, Optional.<Long>absent(), CANCELLED));
        deque.addAll(Sets.difference(graph.outgoingVertices(moduleId), visited));
      }
    }
  }

  /**
   * Checks if this was the last build in the IPB and marks it as complete if so.
   */
  private void checkAndCompleteInterProjectBuild(ModuleBuild build, long interProjectBuildId) {
    InterProjectBuild ipb = interProjectBuildService.getWithId(interProjectBuildId).get();
    if (!isLastModuleBuildInGraph(ipb)) {
      LOG.debug("Module build {} of module {} finished but the associated inter-project-build {} is not done yet", build.getId().get(), build.getModuleId(), ipb.getId().get());
      return;
    }
    interProjectBuildService.finish(InterProjectBuild.getFinishedBuild(ipb, getFinalStateForInterProjectBuild(interProjectBuildId)));
  }

  private boolean isLastModuleBuildInGraph(InterProjectBuild build) {
    Map<Integer, InterProjectBuildMapping> mappingsForInterProjectBuildByModuleId =
        interProjectBuildMappingService.getMappingsForInterProjectBuildByModuleId(build.getId().get());

    // ensure that all _other_ modules are done, so remove this one
    for (int moduleId : build.getDependencyGraph().get().getTopologicalSort()) {
      // No mapping means no build started -- this is not the last build
      if (!mappingsForInterProjectBuildByModuleId.containsKey(moduleId)) {
        return false;
      }

      // This mapping is not in a finished state -- this is not the last build
      InterProjectBuildMapping mapping = mappingsForInterProjectBuildByModuleId.get(moduleId);
      if (!mapping.getState().isFinished()) {
        return false;
      }
    }

    // We made it -- we're the last build
    return true;
  }

  // Identifies final state for IPB
  private InterProjectBuild.State getFinalStateForInterProjectBuild(long interProjectBuildId) {
    Set<InterProjectBuildMapping> mappings = interProjectBuildMappingService.getMappingsForInterProjectBuild(interProjectBuildId);

    Set<InterProjectBuild.State> states = mappings.stream().map(InterProjectBuildMapping::getState).collect(Collectors.toSet());
    if (states.contains(FAILED)) {
      return FAILED;
    }

    if (states.contains(CANCELLED)) {
      return CANCELLED;
    }

    if (states.equals(ImmutableSet.of(SUCCEEDED))) {
      return SUCCEEDED;
    }
    throw new IllegalStateException(String.format("Found un expected states %s in mappings for IPB %s expected SUCCEEDED", states, interProjectBuildId));
  }
}
