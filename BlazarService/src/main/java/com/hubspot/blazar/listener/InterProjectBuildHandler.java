package com.hubspot.blazar.listener;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.inject.Inject;
import com.hubspot.blazar.base.BuildOptions;
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.CommitInfo;
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
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.exception.NonRetryableBuildException;
import com.hubspot.blazar.github.GitHubProtos;
import com.hubspot.blazar.util.GitHubHelper;

public class InterProjectBuildHandler extends AbstractInterProjectBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(InterProjectBuildHandler.class);
  private DependenciesService dependenciesService;
  private ModuleService moduleService;
  private BranchService branchService;
  private RepositoryBuildService repositoryBuildService;
  private GitHubHelper gitHubHelper;
  private InterProjectBuildMappingService interProjectBuildMappingService;
  private InterProjectBuildService interProjectBuildService;

  @Inject
  public InterProjectBuildHandler(DependenciesService dependenciesService,
                                  ModuleService moduleService,
                                  BranchService branchService,
                                  RepositoryBuildService repositoryBuildService,
                                  GitHubHelper gitHubHelper,
                                  InterProjectBuildMappingService interProjectBuildMappingService,
                                  InterProjectBuildService interProjectBuildService){
    this.dependenciesService = dependenciesService;
    this.moduleService = moduleService;
    this.branchService = branchService;
    this.repositoryBuildService = repositoryBuildService;
    this.gitHubHelper = gitHubHelper;
    this.interProjectBuildMappingService = interProjectBuildMappingService;
    this.interProjectBuildService = interProjectBuildService;
  }

  @Override
  protected void visitQueued(InterProjectBuild build) throws Exception {
    if (build.getBuildTrigger().getType() == BuildTrigger.Type.PUSH) {
      handlePushed(build);
    } else {
      handleOther(build);
    }
  }

  private void handlePushed(InterProjectBuild build) throws Exception {
    String branchAndSha = build.getBuildTrigger().getId();
    String[] split = branchAndSha.split("_");
    if (split.length != 2) {
      throw new NonRetryableBuildException("Data for InterProject-BuildTrigger is not branchId_sha");
    }
    int branchId = Integer.valueOf(split[0]);
    String beforeSha = branchAndSha.split("_")[1];
    GitInfo gitInfo = branchService.get(branchId).get();
    GHRepository repository = gitHubHelper.repositoryFor(gitInfo);
    Optional<String> currentSha = gitHubHelper.shaFor(repository, gitInfo);
    LOG.info("Getting changed files for {}#{} {}-{}", gitInfo.getFullRepositoryName(), gitInfo.getBranch(), beforeSha, currentSha);
    Set<Module> allModules = filterActiveGetId(moduleService.getByBranch(gitInfo.getId().get()));
    Set<Module> toBuild = getAffectedModules(gitInfo, beforeSha, currentSha, allModules);
    Set<Integer> toBuildIds = new HashSet<>();
    for (Module m : toBuild) {
      toBuildIds.add(m.getId().get());
    }
    long start = System.currentTimeMillis();
    DependencyGraph graph = dependenciesService.buildInterProjectDependencyGraph(toBuild);
    LOG.debug("Built graph for InterProjectBuild {} in {}", build.getId().get(), System.currentTimeMillis()-start);
    InterProjectBuild withIdsAndGraph = build.withModuleIds(toBuildIds).withDependencyGraph(graph);

    interProjectBuildService.start(withIdsAndGraph);
  }

  private void handleOther(InterProjectBuild build) {
    long start = System.currentTimeMillis();
    LOG.info("Building graph for InterProjectBuild {}", build.getId().get());
    Set<Module> s = new HashSet<>();
    for (int i : build.getModuleIds()) {
      s.add(moduleService.get(i).get());
    }
    DependencyGraph d = dependenciesService.buildInterProjectDependencyGraph(s);
    LOG.debug("Built graph for InterProjectBuild {} in {}", build.getId().get(), System.currentTimeMillis()-start);
    interProjectBuildService.start(build.withDependencyGraph(d));
  }

  @Override
  protected void visitRunning(InterProjectBuild build) throws Exception {
    DependencyGraph d = build.getDependencyGraph().get();
    Set<InterProjectBuildMapping> mappings = interProjectBuildMappingService.getMappingsForInterProjectBuild(build);
    if (!(mappings.size() == 0)) {
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


  private Set<Module> getAffectedModules(GitInfo gitInfo, String beforeSha, Optional<String> currentSha, Set<Module> allModules) throws IOException {
    GHRepository repo = gitHubHelper.repositoryFor(gitInfo);
    GitHubProtos.Commit beforeCommit = gitHubHelper.toCommit(repo.getCommit(beforeSha));
    CommitInfo commitInfo;
    if (currentSha.isPresent()) {
      GitHubProtos.Commit currentCommit = gitHubHelper.toCommit(repo.getCommit(currentSha.get()));
      commitInfo = gitHubHelper.commitInfoFor(repo, currentCommit, Optional.of(beforeCommit));
    } else {
      commitInfo = gitHubHelper.commitInfoFor(repo, beforeCommit, Optional.<GitHubProtos.Commit>absent());
    }
    Set<Module> toBuild = new HashSet<>();
    if (commitInfo.isTruncated()) {
      return allModules;
    } else {
      for (String path : gitHubHelper.affectedPaths(commitInfo)) {
        for (Module module : allModules) {
          if (module.contains(FileSystems.getDefault().getPath(path))) {
            toBuild.add(module);
          }
        }
      }
    }
    return toBuild;
  }

  private static Set<Module> filterActiveGetId(Set<Module> modules) {
    Set<Module> filtered = new HashSet<>();
    for (Module module : modules) {
      if (module.isActive()) {
        filtered.add(module);
      }
    }

    return filtered;
  }
}
