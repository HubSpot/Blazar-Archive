package com.hubspot.blazar.listener;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.InterProjectBuildMapping;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.AbstractRepositoryBuildVisitor;
import com.hubspot.blazar.data.service.InterProjectBuildMappingService;
import com.hubspot.blazar.data.service.InterProjectBuildService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.RepositoryBuildService;

public class InterProjectRepositoryBuildVisitor extends AbstractRepositoryBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(InterProjectRepositoryBuildVisitor.class);
  private final ModuleBuildService moduleBuildService;
  private final RepositoryBuildService repoBuildService;
  private final InterProjectBuildService interProjectBuildService;
  private final InterProjectBuildMappingService interProjectBuildMappingService;

  @Inject
  public InterProjectRepositoryBuildVisitor(ModuleBuildService moduleBuildService,
                                            RepositoryBuildService repoBuildService,
                                            InterProjectBuildService interProjectBuildService,
                                            InterProjectBuildMappingService interProjectBuildMappingService) {
    this.repoBuildService = repoBuildService;
    this.moduleBuildService = moduleBuildService;
    this.interProjectBuildService = interProjectBuildService;
    this.interProjectBuildMappingService = interProjectBuildMappingService;
  }

  @Override
  protected void visitLaunching(RepositoryBuild build) throws Exception {
    Set<InterProjectBuildMapping> mappings = interProjectBuildMappingService.getByRepoBuildId(build.getId().get());
    if (mappings.isEmpty()) {
      return;
    }
    Set<ModuleBuild> moduleBuildsTriggered = moduleBuildService.getByRepositoryBuild(build.getId().get());
    for (InterProjectBuildMapping mapping : mappings) {
      for (ModuleBuild moduleBuild : moduleBuildsTriggered) {
        if (mapping.getModuleId() == moduleBuild.getModuleId()) {
          interProjectBuildMappingService.updateBuilds(mapping.withModuleBuildId(moduleBuild.getId().get()));
        }
      }
    }
  }

  @Override
  protected void visitSucceeded(RepositoryBuild build) throws Exception {
    Set<InterProjectBuild> builds = new HashSet<>();

    Set<InterProjectBuildMapping> mappings = interProjectBuildMappingService.getByRepoBuildId(build.getId().get());
    for (InterProjectBuildMapping mapping : mappings) {
      Optional<InterProjectBuild> interProjectBuild = interProjectBuildService.getWithId(mapping.getInterProjectBuildId());
      if (!interProjectBuild.isPresent()) {
        throw new IllegalStateException(String.format("Cannot have InterProject build mapping %s that points to non-existent InterProject build", mapping));
      }
      if (!checkComplete(interProjectBuild.get())) {
        return;
      }
      builds.add(interProjectBuild.get());
    }

    if (builds.size() > 1) {
      throw new IllegalStateException(String.format("Cannot have multiple inter project builds owning a given repo build %s", build));
    }
    interProjectBuildService.finish(InterProjectBuild.getFinishedBuild(builds.iterator().next()));
  }


  private boolean checkComplete(InterProjectBuild build) {
    Set<InterProjectBuildMapping> mappings = interProjectBuildMappingService.getMappingsForBuild(build);
    for (InterProjectBuildMapping mapping: mappings) {
      Optional<RepositoryBuild> repoBuild = repoBuildService.get(mapping.getRepoBuildId().get());
      if (repoBuild.isPresent() && !repoBuild.get().getState().isComplete()) {
        return false;
      }
    }
    return true;
  }


}
