package com.hubspot.blazar.data.service;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.BuildDefinition;
import com.hubspot.blazar.data.dao.BuildDefinitionDao;

import java.util.Set;

public class BuildDefinitionService {
  private final BuildDefinitionDao buildDefinitionDao;

  @Inject
  public BuildDefinitionService(BuildDefinitionDao buildDefinitionDao) {
    this.buildDefinitionDao = buildDefinitionDao;
  }

  public Set<BuildDefinition> getAllBuildDefinitions(long since) {
    return buildDefinitionDao.getAllBuildDefinitions(since);
  }

  public Optional<BuildDefinition> getByModuleId(int moduleId) {
    return buildDefinitionDao.getByModuleId(moduleId);
  }
}
