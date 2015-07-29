package com.hubspot.blazar.data.service;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.hubspot.blazar.base.BuildState;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.data.dao.BuildStateDao;

import java.util.Set;

public class BuildStateService {
  private final BuildStateDao buildStateDao;

  @Inject
  public BuildStateService(BuildStateDao buildStateDao) {
    this.buildStateDao = buildStateDao;
  }

  public Set<BuildState> getAllBuildStates() {
    return buildStateDao.getAllBuildStates();
  }

  public Optional<ModuleBuild> get(long id) {
    return buildStateDao.get(id);
  }

  public void update(ModuleBuild moduleBuild) {
    int updated = buildStateDao.update(moduleBuild.getBuild());
    Preconditions.checkState(updated == 1, "Expected to update 1 row but updated %s", updated);
  }
}
