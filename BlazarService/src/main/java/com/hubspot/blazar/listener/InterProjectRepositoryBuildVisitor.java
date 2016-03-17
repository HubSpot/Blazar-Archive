package com.hubspot.blazar.listener;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.InterProjectBuildMapping;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.AbstractRepositoryBuildVisitor;
import com.hubspot.blazar.data.service.InterProjectModuleBuildMappingService;
import com.hubspot.blazar.data.service.InterProjectRepositoryBuildMappingService;
import com.hubspot.blazar.data.service.ModuleBuildService;

public class InterProjectRepositoryBuildVisitor extends AbstractRepositoryBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(InterProjectRepositoryBuildVisitor.class);
  private final ModuleBuildService moduleBuildService;
  private final InterProjectRepositoryBuildMappingService interProjectRepositoryBuildMappingService;
  private final InterProjectModuleBuildMappingService interProjectModuleBuildMappingService;

  @Inject
  public InterProjectRepositoryBuildVisitor(ModuleBuildService moduleBuildService,
                                            InterProjectRepositoryBuildMappingService interProjectRepositoryBuildMappingService,
                                            InterProjectModuleBuildMappingService interProjectModuleBuildMappingService) {
    this.moduleBuildService = moduleBuildService;
    this.interProjectRepositoryBuildMappingService = interProjectRepositoryBuildMappingService;
    this.interProjectModuleBuildMappingService = interProjectModuleBuildMappingService;
  }

  @Override
  protected void visitLaunching(RepositoryBuild build) throws Exception {
    Optional<InterProjectBuildMapping> mapping = interProjectRepositoryBuildMappingService.findByBuildId(build.getId().get());
    if (!mapping.isPresent()) {
      return;
    }
    Set<ModuleBuild> moduleBuildsTriggered = moduleBuildService.getByRepositoryBuild(build.getId().get());
    for (ModuleBuild m : moduleBuildsTriggered) {
      LOG.debug("Making mapping to Inter Project Build {} from moduleBuild {}", mapping.get().getInterProjectBuildId(), m.getId().get());
      interProjectModuleBuildMappingService.addMapping(new InterProjectBuildMapping(mapping.get().getInterProjectBuildId(), m.getModuleId(), Optional.of(m.getId().get())));
    }
  }
}
