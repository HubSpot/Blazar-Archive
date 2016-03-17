package com.hubspot.blazar.data.service;

import java.util.Set;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.InterProjectBuildMapping;
import com.hubspot.blazar.data.dao.InterProjectModuleBuildMappingDao;

public class InterProjectModuleBuildMappingService {

  private InterProjectModuleBuildMappingDao dao;

  @Inject
  public InterProjectModuleBuildMappingService(InterProjectModuleBuildMappingDao dao) {
    this.dao = dao;
  }

  public Set<InterProjectBuildMapping> getMappingsForBuild(InterProjectBuild interProjectBuild) {
    return dao.getMappingsForBuild(interProjectBuild);
  }

  public Optional<InterProjectBuildMapping> findByBuildId(long buildId) {
    return dao.findByBuildId(buildId);
  }

  public void addMapping(InterProjectBuildMapping interProjectBuildMapping) {
    dao.insert(interProjectBuildMapping);
  }

}
