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
import com.hubspot.blazar.base.visitor.AbstractInterProjectBuildVisitor;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.DependenciesService;
import com.hubspot.blazar.data.service.InterProjectBuildService;
import com.hubspot.blazar.data.service.InterProjectRepositoryBuildMappingService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;

public class InterProjectBuildLauncher extends AbstractInterProjectBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(InterProjectBuildLauncher.class);
  private DependenciesService dependenciesService;
  private ModuleService moduleService;
  private BranchService branchService;
  private RepositoryBuildService repositoryBuildService;
  private InterProjectRepositoryBuildMappingService interProjectRepositoryBuildMappingService;
  private InterProjectBuildService interProjectBuildService;

  @Inject
  public InterProjectBuildLauncher(DependenciesService dependenciesService,
                                   ModuleService moduleService,
                                   BranchService branchService,
                                   RepositoryBuildService repositoryBuildService,
                                   InterProjectRepositoryBuildMappingService interProjectRepositoryBuildMappingService,
                                   InterProjectBuildService interProjectBuildService){
    this.dependenciesService = dependenciesService;
    this.moduleService = moduleService;
    this.branchService = branchService;
    this.repositoryBuildService = repositoryBuildService;
    this.interProjectRepositoryBuildMappingService = interProjectRepositoryBuildMappingService;
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
    interProjectBuildService.start(InterProjectBuild.withDependencyGraph(build, d));
  }


  @Override
  protected void visitRunning(InterProjectBuild build) throws Exception {
    DependencyGraph d = build.getDependencyGraph().get();
    Set<InterProjectBuildMapping> repoBuildsTriggered = interProjectRepositoryBuildMappingService.getMappingsForInterProjectBuild(build);
    if (!(repoBuildsTriggered.size() == 0)) {
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
      GitInfo gitInfo = branchService.get(entry.getKey()).get();
      BuildTrigger buildTrigger = BuildTrigger.forInterProjectBuild(gitInfo);
      BuildOptions buildOptions = new BuildOptions(entry.getValue(), BuildOptions.BuildDownstreams.NONE);
      long buildId = repositoryBuildService.enqueue(gitInfo, buildTrigger, buildOptions);
      interProjectRepositoryBuildMappingService.addMapping(new InterProjectBuildMapping(build.getId().get(), entry.getKey(), Optional.of(buildId)));
      LOG.info("Queued repo build {} as part of InterProjectBuild {}", buildId, build.getId().get());
    }
  }
}
