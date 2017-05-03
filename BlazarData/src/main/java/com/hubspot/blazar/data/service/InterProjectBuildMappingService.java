package com.hubspot.blazar.data.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.hubspot.blazar.base.InterProjectBuildMapping;
import com.hubspot.blazar.data.dao.InterProjectBuildMappingDao;

public class InterProjectBuildMappingService {

  private InterProjectBuildMappingDao dao;

  @Inject
  public InterProjectBuildMappingService(InterProjectBuildMappingDao dao) {
    this.dao = dao;
  }

  /**
   * Many times when requesting mappings what is really wanted is a map of ModuleId -> Mapping for easier access.
   */
  public Map<Integer, InterProjectBuildMapping> getMappingsForInterProjectBuildByModuleId(long interProjectBuildId) {
    Map<Integer, InterProjectBuildMapping> mappingsForInterProjectBuildByModuleId = new HashMap<>();
    Set<InterProjectBuildMapping> mappingsForInterProjectBuild = getMappingsForInterProjectBuild(interProjectBuildId);
    mappingsForInterProjectBuild.forEach(m -> mappingsForInterProjectBuildByModuleId.put(m.getModuleId(), m));
    return ImmutableMap.copyOf(mappingsForInterProjectBuildByModuleId);
  }

  public Set<InterProjectBuildMapping> getMappingsForInterProjectBuild(long interProjectBuildId) {
    return dao.getMappingsForInterProjectBuild(interProjectBuildId);
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
