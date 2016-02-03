package com.hubspot.blazar.data.service;

import com.google.inject.Inject;
import com.hubspot.blazar.base.BuildState;
import com.hubspot.blazar.data.CachingService;
import com.hubspot.blazar.data.dao.BuildStateDao;

import javax.inject.Singleton;
import java.util.Set;

@Singleton
public class BuildStateService extends CachingService<BuildState> {
  private final BuildStateDao buildStateDao;

  @Inject
  public BuildStateService(BuildStateDao buildStateDao) {
    this.buildStateDao = buildStateDao;
  }

  @Override
  protected Set<BuildState> fetch(long since) {
    return buildStateDao.getAllBuildStates(since);
  }
}
