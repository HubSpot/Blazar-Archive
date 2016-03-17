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
import com.hubspot.blazar.data.service.InterProjectBuildService;
import com.hubspot.blazar.data.service.InterProjectModuleBuildMappingService;
import com.hubspot.blazar.data.service.InterProjectRepositoryBuildMappingService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.exception.NonRetryableBuildException;

public class InterProjectModuleBuildVisitor extends AbstractModuleBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(InterProjectModuleBuildVisitor.class);
  private final ModuleService moduleService;
  private BranchService branchService;
  private RepositoryBuildService repositoryBuildService;
  private final InterProjectBuildService interProjectBuildService;
  private InterProjectRepositoryBuildMappingService interProjectRepositoryBuildMappingService;
  private final InterProjectModuleBuildMappingService interProjectModuleBuildMappingService;

  @Inject
  public InterProjectModuleBuildVisitor(ModuleService moduleService,
                                        BranchService branchService,
                                        RepositoryBuildService repositoryBuildService,
                                        InterProjectBuildService interProjectBuildService,
                                        InterProjectRepositoryBuildMappingService interProjectRepositoryBuildMappingService,
                                        InterProjectModuleBuildMappingService interProjectModuleBuildMappingService) {
    this.moduleService = moduleService;
    this.branchService = branchService;
    this.repositoryBuildService = repositoryBuildService;
    this.interProjectBuildService = interProjectBuildService;
    this.interProjectRepositoryBuildMappingService = interProjectRepositoryBuildMappingService;
    this.interProjectModuleBuildMappingService = interProjectModuleBuildMappingService;
  }

  @Override
  protected void visitSucceeded(ModuleBuild build) throws Exception {
    Optional<InterProjectBuildMapping> mapping = interProjectModuleBuildMappingService.findByBuildId(build.getId().get());
    if (!mapping.isPresent()) {
      return;
    }
    LOG.info("Found mapping {} corresponding to moduleBuild {}", mapping.get(), build.getId());
    Optional<InterProjectBuild> maybeInterProjectBuild = interProjectBuildService.getWithId(mapping.get().getInterProjectBuildId());
    if(!maybeInterProjectBuild.isPresent()) {
      throw new NonRetryableBuildException(String.format("Found a InterProjectBuildMapping %s with no related InterProjectBuild", mapping.get().toString()));
    }
    InterProjectBuild interProjectBuild = maybeInterProjectBuild.get();
    DependencyGraph graph = interProjectBuild.getDependencyGraph().get();
    SetMultimap<Integer, Integer> launchable = HashMultimap.create();
    for (int moduleId : graph.outgoingVertices(build.getModuleId())) {
      launchable.put(moduleService.getBranchIdFromModuleId(moduleId), moduleId);
    }
    for (Map.Entry<Integer, Set<Integer>> entry : Multimaps.asMap(launchable).entrySet()) {
      GitInfo gitInfo = branchService.get(entry.getKey()).get();
      BuildTrigger buildTrigger = BuildTrigger.forInterProjectBuild(gitInfo);
      BuildOptions buildOptions = new BuildOptions(entry.getValue(), BuildOptions.BuildDownstreams.NONE);
      long buildId = repositoryBuildService.enqueue(gitInfo, buildTrigger, buildOptions);
      interProjectRepositoryBuildMappingService.addMapping(new InterProjectBuildMapping(interProjectBuild.getId().get(), entry.getKey(), Optional.of(buildId)));
      LOG.info("Queued repo build {} as part of InterProjectBuild {}", buildId, interProjectBuild.getId().get());
    }
  }

  @Override
  protected void visitCancelled(ModuleBuild build) throws Exception {
    cancelTree(build);
  }

  @Override
  protected void visitFailed(ModuleBuild build) throws Exception {
    cancelTree(build);
  }

  private void cancelTree(ModuleBuild build) throws NonRetryableBuildException {
    Optional<InterProjectBuildMapping> mapping = interProjectModuleBuildMappingService.findByBuildId(build.getId().get());
    if (!mapping.isPresent()) {
      return;
    }
    Optional<InterProjectBuild> maybeInterProjectBuild = interProjectBuildService.getWithId(mapping.get().getInterProjectBuildId());
    if(!maybeInterProjectBuild.isPresent()) {
      throw new NonRetryableBuildException(String.format("Found a InterProjectBuildMapping %s with no related InterProjectBuild", mapping.get().toString()));
    }
    InterProjectBuild interProjectBuild = maybeInterProjectBuild.get();
    DependencyGraph graph = interProjectBuild.getDependencyGraph().get();
    Stack<Integer> s = new Stack<>();
    s.addAll(graph.outgoingVertices(build.getModuleId()));
    while (!s.empty()) {
      int moduleId = s.pop();
      s.addAll(getChildren(graph, moduleId));
      interProjectModuleBuildMappingService.addMapping(new InterProjectBuildMapping(interProjectBuild.getId().get(), moduleId, Optional.<Long>absent()));
    }
  }

  private static Set<Integer> getChildren(DependencyGraph graph, Integer moduleId) {
    return graph.outgoingVertices(moduleId);
  }
}
