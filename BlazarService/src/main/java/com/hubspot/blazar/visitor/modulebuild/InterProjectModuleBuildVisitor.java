package com.hubspot.blazar.visitor.modulebuild;

import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
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
import com.hubspot.blazar.data.service.ModuleBuildService;
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
                                        ModuleBuildService moduleBuildService,
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
    LOG.info("Found mapping {} corresponding to moduleBuild {}", mapping, build.getId());
    interProjectBuildMappingService.updateBuilds(mapping.get().withModuleBuildId(InterProjectBuild.State.SUCCEEDED));
    buildChildren(build, mapping.get());
    checkAndCompleteInterProjectBuild(build, mapping.get().getInterProjectBuildId());
  }

  @Override
  protected void visitCancelled(ModuleBuild build) throws Exception {
    Optional<InterProjectBuildMapping> mapping = interProjectBuildMappingService.getByModuleBuildId(build.getId().get());
    if (!mapping.isPresent()) {
      return;
    }
    interProjectBuildMappingService.updateBuilds(mapping.get().withModuleBuildId(InterProjectBuild.State.CANCELLED));
    cancelSubTree(build);
    checkAndCompleteInterProjectBuild(build, mapping.get().getInterProjectBuildId());
  }

  @Override
  protected void visitFailed(ModuleBuild build) throws Exception {
    Optional<InterProjectBuildMapping> mapping = interProjectBuildMappingService.getByModuleBuildId(build.getId().get());
    if (!mapping.isPresent()) {
      return;
    }
    interProjectBuildMappingService.updateBuilds(mapping.get().withModuleBuildId(InterProjectBuild.State.FAILED));
    cancelSubTree(build);
    checkAndCompleteInterProjectBuild(build, mapping.get().getInterProjectBuildId());
  }

  // Building
  private void buildChildren(ModuleBuild build, InterProjectBuildMapping mapping) {
    InterProjectBuild interProjectBuild = interProjectBuildService.getWithId(mapping.getInterProjectBuildId()).get();
    DependencyGraph graph = interProjectBuild.getDependencyGraph().get();
    Set<InterProjectBuildMapping> moduleBuildMappings = interProjectBuildMappingService.getMappingsForInterProjectBuild(interProjectBuild);

    SetMultimap<Integer, Integer> launchableBranchToModuleMap = HashMultimap.create();

    for (int moduleId : graph.reachableVertices(build.getModuleId())) {
      int branchId = moduleService.getBranchIdFromModuleId(moduleId);
      if (shouldBuild(moduleId, branchId, graph, moduleBuildMappings)) {
        launchableBranchToModuleMap.put(branchId, moduleId);
      } else {
        LOG.debug("Upstreams not complete for module {} not launching InterRepoBuild yet.", moduleId);
      }
    }

    for (Map.Entry<Integer, Set<Integer>> entry : Multimaps.asMap(launchableBranchToModuleMap).entrySet()) {
      Set<Integer> launchableModules = entry.getValue();
      GitInfo gitInfo = branchService.get(entry.getKey()).get();
      BuildTrigger buildTrigger = BuildTrigger.forInterProjectBuild(interProjectBuild.getId().get());
      BuildOptions buildOptions = new BuildOptions(launchableModules, BuildOptions.BuildDownstreams.NONE, false);
      long buildId = repositoryBuildService.enqueue(gitInfo, buildTrigger, buildOptions);
      for (Integer moduleId : launchableModules) {
        interProjectBuildMappingService.insert(InterProjectBuildMapping.makeNewMapping(interProjectBuild.getId().get(), gitInfo.getId().get(), Optional.of(buildId), moduleId));
      }
      LOG.info("Queued repo build {} as part of InterProjectBuild {}", buildId, interProjectBuild.getId().get());
    }
  }

  private boolean shouldBuild(int moduleId, int branchId, DependencyGraph graph, Set<InterProjectBuildMapping> mappings) {
    // don't start builds of modules previously triggered
    if (mappings.stream().filter((InterProjectBuildMapping m) -> m.getModuleId() == moduleId).findFirst().isPresent()) {
      return false;
    }
    for (Integer upstream: graph.getAllUpstreamNodes(moduleId)) {
      if (!upstreamCompleteOrInBranch(branchId, upstream, mappings)) {
        return false;
      }
    }
    return true;
  }

  private boolean upstreamCompleteOrInBranch(int branchId, int moduleId, Set<InterProjectBuildMapping> mappings) {
    // See if we've started a build for this
    Optional<InterProjectBuildMapping> mappingFound = Optional.absent();
    for (InterProjectBuildMapping mapping : mappings) {
      if (mapping.getModuleId() == moduleId) {
        mappingFound = Optional.of(mapping);
      }
    }
    if (branchId == moduleService.getBranchIdFromModuleId(moduleId) && !mappingFound.isPresent()) {
      // This module is in the same branch, we want to bundle it in a repo build.
      return true;
    }
    return mappingFound.isPresent() && mappingFound.get().getState().equals(InterProjectBuild.State.SUCCEEDED);
  }

  // Canceling
  private void cancelSubTree(ModuleBuild build) throws NonRetryableBuildException {
    InterProjectBuildMapping mapping = interProjectBuildMappingService.getByModuleBuildId(build.getId().get()).get();
    LOG.info("Found mapping {} corresponding to moduleBuild {}", mapping, build.getId());
    InterProjectBuild interProjectBuild = interProjectBuildService.getWithId(mapping.getInterProjectBuildId()).get();
    DependencyGraph graph = interProjectBuild.getDependencyGraph().get();
    Stack<Integer> s = new Stack<>();
    s.addAll(graph.outgoingVertices(build.getModuleId()));
    while (!s.empty()) {
      int moduleId = s.pop();
      interProjectBuildMappingService.insert(new InterProjectBuildMapping(Optional.<Long>absent(), interProjectBuild.getId().get(), moduleService.getBranchIdFromModuleId(moduleId), Optional.<Long>absent(), moduleId, Optional.<Long>absent(), InterProjectBuild.State.CANCELLED));
      s.addAll(graph.outgoingVertices(moduleId));
    }
  }

  // Completing IPB
  private void checkAndCompleteInterProjectBuild(ModuleBuild build, long interProjectBuildId) {
    InterProjectBuild ipb = interProjectBuildService.getWithId(interProjectBuildId).get();
    if (!isLastModuleBuildInGraph(ipb)) {
      LOG.debug("Module build {} of module {} finished but the associated inter-project-build {} is not done yet", build.getId().get(), build.getModuleId(), ipb.getId().get());
      return;
    }
    interProjectBuildService.finish(InterProjectBuild.getFinishedBuild(ipb, getStateForBuild(ipb)));
  }

  private boolean isLastModuleBuildInGraph(InterProjectBuild build) {
    Set<InterProjectBuildMapping> mappings = interProjectBuildMappingService.getMappingsForInterProjectBuild(build);
    // ensure that all _other_ modules are done, so remove this one
    for (int i : build.getDependencyGraph().get().getTopologicalSort()) {
      Optional<InterProjectBuild.State> state = Optional.absent();
      for (InterProjectBuildMapping mapping : mappings ){
        if (mapping.getModuleId() == i) {
          state = Optional.of(mapping.getState());
        }
      }
      // state not present implies no build has been started for this yet.
      if ((state.isPresent() && !state.get().isFinished()) || !state.isPresent()) {
        return false;
      }
    }
    return true;
  }

  private InterProjectBuild.State getStateForBuild(InterProjectBuild build) {
    Set<InterProjectBuildMapping> mappings = interProjectBuildMappingService.getMappingsForInterProjectBuild(build);
    InterProjectBuild.State state = InterProjectBuild.State.SUCCEEDED;
    for (InterProjectBuildMapping mapping: mappings) {
      // if there is a failure mark as failed
      if (mapping.getState() == InterProjectBuild.State.FAILED) {
        state = mapping.getState();
      }
      // if the state is not failed,
      if (!state.equals(InterProjectBuild.State.FAILED) && mapping.getState() == InterProjectBuild.State.CANCELLED) {
        state = InterProjectBuild.State.CANCELLED;
      }
    }
    return state;
  }

}
