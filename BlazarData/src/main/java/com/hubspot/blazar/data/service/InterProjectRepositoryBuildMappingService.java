package com.hubspot.blazar.data.service;

import java.util.Set;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.InterProjectBuildMapping;
import com.hubspot.blazar.data.dao.InterProjectRepositoryBuildMappingDao;

public class InterProjectRepositoryBuildMappingService {

  private InterProjectRepositoryBuildMappingDao dao;

  @Inject
  public InterProjectRepositoryBuildMappingService(InterProjectRepositoryBuildMappingDao dao) {
    this.dao = dao;
  }

  public Set<InterProjectBuildMapping> getMappingsForInterProjectBuild(InterProjectBuild interProjectBuild) {
    return dao.getMappingsForInterProjectBuild(interProjectBuild);
  }

  public Optional<InterProjectBuildMapping> findByBuildId(long buildId) {
    return dao.findByBuildId(buildId);
  }

  public void addMapping(InterProjectBuildMapping mapping) {
    dao.insert(mapping);
  }
}
