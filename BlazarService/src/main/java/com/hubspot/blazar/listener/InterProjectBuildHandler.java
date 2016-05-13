package com.hubspot.blazar.listener;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
                                  InterProjectBuildService interProjectBuildService){
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
    LOG.debug("Built graph for InterProjectBuild {} in {}", build.getId().get(), System.currentTimeMillis()-start);
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

    SetMultimap<Integer, Integer> launchable = HashMultimap.create();

    for (int moduleId : build.getModuleIds()) {
      Set<Integer> parents = d.incomingVertices(moduleId);
      if (parents.size() == 0) {
        launchable.put(moduleService.getBranchIdFromModuleId(moduleId), moduleId);
      }
    }

    for (Map.Entry<Integer, Set<Integer>> entry : Multimaps.asMap(launchable).entrySet()) {
      Set<Integer> moduleIds = entry.getValue();
      BuildTrigger buildTrigger = BuildTrigger.forInterProjectBuild(build);
      BuildOptions buildOptions = new BuildOptions(entry.getValue(), BuildOptions.BuildDownstreams.NONE, false);
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
}
