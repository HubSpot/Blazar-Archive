package com.hubspot.blazar.listener;

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
  private ModuleBuildService moduleBuildService;
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
    this.moduleBuildService = moduleBuildService;
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
    markBuildMappingAs(mapping.get(), InterProjectBuild.State.SUCCEEDED);
    LOG.info("Found mapping {} corresponding to moduleBuild {}", mapping, build.getId());
    InterProjectBuild interProjectBuild = interProjectBuildService.getWithId(mapping.get().getInterProjectBuildId()).get();

    DependencyGraph graph = interProjectBuild.getDependencyGraph().get();
    Set<InterProjectBuildMapping> moduleBuildMappings = interProjectBuildMappingService.getMappingsForInterProjectBuild(interProjectBuild);

    SetMultimap<Integer, Integer> launchableBranchToModuleMap = HashMultimap.create();
    for (int moduleId : graph.outgoingVertices(build.getModuleId())) {
      if (upstreamsComplete(graph.incomingVertices(moduleId), moduleBuildMappings) && noCurrentBuildFor(moduleId, interProjectBuild.getId().get())) {
        launchableBranchToModuleMap.put(moduleService.getBranchIdFromModuleId(moduleId), moduleId);
      } else {
        LOG.debug("Upstreams not complete for module {} not launching InterRepoBuild yet.", moduleId);
      }
    }

    for (Map.Entry<Integer, Set<Integer>> entry : Multimaps.asMap(launchableBranchToModuleMap).entrySet()) {
      Set<Integer> launchableModules = entry.getValue();
      GitInfo gitInfo = branchService.get(entry.getKey()).get();
      BuildTrigger buildTrigger = BuildTrigger.forInterProjectBuild(gitInfo);
      BuildOptions buildOptions = new BuildOptions(launchableModules, BuildOptions.BuildDownstreams.NONE, false);
      long buildId = repositoryBuildService.enqueue(gitInfo, buildTrigger, buildOptions);
      for (Integer moduleId : launchableModules) {
        interProjectBuildMappingService.insert(InterProjectBuildMapping.makeNewMapping(interProjectBuild.getId().get(), gitInfo.getId().get(), Optional.of(buildId), moduleId));
      }
      LOG.info("Queued repo build {} as part of InterProjectBuild {}", buildId, interProjectBuild.getId().get());
    }
  }

  @Override
  protected void visitCancelled(ModuleBuild build) throws Exception {
    Optional<InterProjectBuildMapping> mapping = interProjectBuildMappingService.getByModuleBuildId(build.getId().get());
    if (!mapping.isPresent()) {
      return;
    }
    markBuildMappingAs(mapping.get(), InterProjectBuild.State.CANCELLED);
    cancelTree(build);
  }

  @Override
  protected void visitFailed(ModuleBuild build) throws Exception {
    Optional<InterProjectBuildMapping> mapping = interProjectBuildMappingService.getByModuleBuildId(build.getId().get());
    if (!mapping.isPresent()) {
      return;
    }
    markBuildMappingAs(mapping.get(), InterProjectBuild.State.FAILED);
    cancelTree(build);
  }

  private void markBuildMappingAs(InterProjectBuildMapping mapping, InterProjectBuild.State state) {
    interProjectBuildMappingService.updateBuilds(mapping.withModuleBuildId(state));
  }

  private boolean noCurrentBuildFor(int moduleId, Long interProjectBuildId) {
    Set<InterProjectBuildMapping> mappings = interProjectBuildMappingService.getMappingsForModule(interProjectBuildId, moduleId);
    return mappings.isEmpty();
  }

  private boolean upstreamsComplete(Set<Integer> upstreamModuleIds, Set<InterProjectBuildMapping> mappings) {
    for (Integer upstreamModuleId : upstreamModuleIds) {
      for (InterProjectBuildMapping mapping : mappings) {
        if (!(mapping.getModuleId() == upstreamModuleId)) {
          continue;
        }
        if (!mapping.getModuleBuildId().isPresent()) {
          return false;
        }
        ModuleBuild moduleBuild = moduleBuildService.get(mapping.getModuleBuildId().get()).get();
        ModuleBuild.State state = moduleBuild.getState();
        if (!state.equals(ModuleBuild.State.SUCCEEDED)) {
          return false;
        }
      }
    }
    return true;
  }

  private void cancelTree(ModuleBuild build) throws NonRetryableBuildException {
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
}
