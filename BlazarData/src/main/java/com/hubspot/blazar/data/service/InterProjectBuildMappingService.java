package com.hubspot.blazar.data.service;

import java.util.Set;

import javax.transaction.Transactional;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.InterProjectBuildMapping;
import com.hubspot.blazar.data.dao.InterProjectBuildMappingDao;

public class InterProjectBuildMappingService {

  private InterProjectBuildMappingDao dao;

  @Inject
  public InterProjectBuildMappingService(InterProjectBuildMappingDao dao) {
    this.dao = dao;
  }

  public Set<InterProjectBuildMapping> getMappingsForInterProjectBuild(InterProjectBuild interProjectBuild) {
    return dao.getMappingsForInterProjectBuild(interProjectBuild);
  }

  public Set<InterProjectBuildMapping> getMappingsForRepo(long interProjectBuildId, int branchId) {
    return dao.getMappingsForRepo(interProjectBuildId, branchId);
  }

  public Set<InterProjectBuildMapping> getMappingsForModule(long interProjectBuildId, int moduleId) {
    return dao.getMappingsForModule(interProjectBuildId, moduleId);
  }

  public Set<InterProjectBuildMapping> getByRepoBuildId(long repoBuildId) {
    return dao.getByRepoBuildId(repoBuildId);
  }

  public Optional<InterProjectBuildMapping> getByModuleBuildId(long moduleBuildId) {
    return dao.getByModuleBuildId(moduleBuildId);
  }

  public Optional<InterProjectBuildMapping> getByMappingId(long id) {
    return dao.getByMappingId(id);
  }

  @Transactional
  public long insert(InterProjectBuildMapping interProjectBuildMapping) {
    return dao.insert(interProjectBuildMapping);
  }

  @Transactional
  public int updateBuilds(InterProjectBuildMapping mapping) {
    return dao.updateBuilds(mapping);
  }


}
