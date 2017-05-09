package com.hubspot.blazar.visitor.repositorybuild;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.hubspot.blazar.base.InterProjectBuildMapping;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.AbstractRepositoryBuildVisitor;
import com.hubspot.blazar.data.service.InterProjectBuildMappingService;
import com.hubspot.blazar.data.service.ModuleBuildService;

public class InterProjectRepositoryBuildVisitor extends AbstractRepositoryBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(InterProjectRepositoryBuildVisitor.class);
  private final ModuleBuildService moduleBuildService;
  private final InterProjectBuildMappingService interProjectBuildMappingService;

  @Inject
  public InterProjectRepositoryBuildVisitor(ModuleBuildService moduleBuildService,
                                            InterProjectBuildMappingService interProjectBuildMappingService) {
    this.moduleBuildService = moduleBuildService;
    this.interProjectBuildMappingService = interProjectBuildMappingService;
  }

  @Override
  protected void visitLaunching(RepositoryBuild build) throws Exception {
    Set<InterProjectBuildMapping> mappings = interProjectBuildMappingService.getByRepoBuildId(build.getId().get());
    if (mappings.isEmpty()) {
      return;
    }
    long ipbId = mappings.stream().findAny().get().getInterProjectBuildId();
    LOG.info("Repo Build {} is part of InterProjectBuild {} creating mappings for triggered modules", build, ipbId);
    Set<ModuleBuild> moduleBuildsTriggered = moduleBuildService.getByRepositoryBuild(build.getId().get());
    for (InterProjectBuildMapping mapping : mappings) {
      for (ModuleBuild moduleBuild : moduleBuildsTriggered) {
        if (mapping.getModuleId() == moduleBuild.getModuleId()) {
          LOG.debug("RepoBuild {} -> IPB {} -> ModuleBuild -> {}", build, ipbId, moduleBuild.getId().get());
          interProjectBuildMappingService.updateBuilds(mapping.withModuleBuildId(moduleBuild.getId().get()));
        }
      }
    }
  }
}
