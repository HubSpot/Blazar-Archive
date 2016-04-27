package com.hubspot.blazar.listener;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.InterProjectBuildMapping;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.AbstractRepositoryBuildVisitor;
import com.hubspot.blazar.data.service.InterProjectBuildMappingService;
import com.hubspot.blazar.data.service.InterProjectBuildService;
import com.hubspot.blazar.data.service.ModuleBuildService;

public class InterProjectRepositoryBuildVisitor extends AbstractRepositoryBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(InterProjectRepositoryBuildVisitor.class);
  private final ModuleBuildService moduleBuildService;
  private final InterProjectBuildService interProjectBuildService;
  private final InterProjectBuildMappingService interProjectBuildMappingService;

  @Inject
  public InterProjectRepositoryBuildVisitor(ModuleBuildService moduleBuildService,
                                            InterProjectBuildService interProjectBuildService,
                                            InterProjectBuildMappingService interProjectBuildMappingService) {
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
    checkDone(build);
  }

  @Override
  protected void visitFailed(RepositoryBuild build) {
    checkDone(build);
  }

  @Override
  protected void visitCancelled(RepositoryBuild build) throws Exception {
    checkDone(build);
  }

  private void checkDone(RepositoryBuild build) {
    Set<InterProjectBuildMapping> repoBuildMappings = interProjectBuildMappingService.getByRepoBuildId(build.getId().get());
    if (repoBuildMappings.isEmpty()) {
      return;
    }
    InterProjectBuild interProjectBuild = interProjectBuildService.getWithId(repoBuildMappings.iterator().next().getInterProjectBuildId()).get();
    InterProjectBuild.State complete = checkComplete(interProjectBuild);
    if (!complete.isFinished()) {
      return;
    }
    interProjectBuildService.finish(InterProjectBuild.getFinishedBuild(interProjectBuild, complete));
  }

  private InterProjectBuild.State checkComplete(InterProjectBuild build) {
    Set<InterProjectBuildMapping> mappings = interProjectBuildMappingService.getMappingsForInterProjectBuild(build);
    InterProjectBuild.State state = InterProjectBuild.State.SUCCEEDED;
    for (InterProjectBuildMapping mapping: mappings) {
      if (!mapping.getState().isFinished()) {
        return InterProjectBuild.State.RUNNING;
      }

      // if there is a failure mark as failed, regardless of any CANCELLED modules
      if (!mapping.getState().equals(InterProjectBuild.State.SUCCEEDED) && !state.equals(InterProjectBuild.State.FAILED)) {
        state = mapping.getState();
      }
    }
    return state;
  }
}
