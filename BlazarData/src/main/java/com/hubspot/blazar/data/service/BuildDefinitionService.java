package com.hubspot.blazar.data.service;

import com.google.inject.Inject;
import com.hubspot.blazar.base.BuildDefinition;
import com.hubspot.blazar.data.CachingService;
import com.hubspot.blazar.data.dao.BuildDefinitionDao;

import javax.inject.Singleton;
import java.util.Set;

@Singleton
public class BuildDefinitionService extends CachingService<BuildDefinition> {
  private final BuildDefinitionDao buildDefinitionDao;

  @Inject
  public BuildDefinitionService(BuildDefinitionDao buildDefinitionDao) {
    this.buildDefinitionDao = buildDefinitionDao;
  }

  @Override
  protected Set<BuildDefinition> fetch(long since) {
    return buildDefinitionDao.getAllBuildDefinitions(since);
  }
}
