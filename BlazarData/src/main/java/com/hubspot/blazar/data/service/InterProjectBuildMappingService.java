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

  public Set<InterProjectBuildMapping> getMappingsForBuild(InterProjectBuild interProjectBuild) {
    return dao.getMappingsForBuild(interProjectBuild);
  }

  public Set<InterProjectBuildMapping> getMappingsForRepo(long interProjectBuildId, int repoId) {
    return dao.getMappingsForRepo(interProjectBuildId, repoId);
  }

  public Set<InterProjectBuildMapping> getMappingsForModule(long interProjectBuildId, int moduleId) {
    return dao.getMappingsForModule(interProjectBuildId, moduleId);
  }

  public Set<InterProjectBuildMapping> getByRepoBuildId(long repoBuildId) {
    return dao.getByRepoBuildId(repoBuildId);
  }

  public Set<InterProjectBuildMapping> getByModuleBuildId(long moduleBuildId) {
    return dao.getByModuleBuildId(moduleBuildId);
  }

  public Optional<InterProjectBuildMapping> getById(long id) {
    return dao.getById(id);
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
