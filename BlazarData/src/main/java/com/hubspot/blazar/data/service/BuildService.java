package com.hubspot.blazar.data.service;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.hubspot.blazar.base.Build;
import com.hubspot.blazar.base.BuildState;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.data.dao.BuildDao;
import com.hubspot.blazar.data.dao.ModuleDao;
import com.hubspot.guice.transactional.Transactional;

public class BuildService {
  private final BuildStateService buildStateService;
  private final BuildDao buildDao;
  private final ModuleDao moduleDao;

  @Inject
  public BuildService(BuildStateService buildStateService, BuildDao buildDao, ModuleDao moduleDao) {
    this.buildStateService = buildStateService;
    this.buildDao = buildDao;
    this.moduleDao = moduleDao;
  }

  public Optional<ModuleBuild> get(long id) {
    return buildDao.get(id);
  }

  @Transactional
  public BuildState enqueue(Module module) {
    BuildState buildState = buildStateService.getByModule(module);

    if (buildState.getPendingBuild().isPresent()) {
      return buildState;
    } else {
      final int nextBuildNumber;
      if (buildState.getInProgressBuild().isPresent()) {
        nextBuildNumber = buildState.getInProgressBuild().get().getBuildNumber() + 1;
      } else if (buildState.getLastBuild().isPresent()) {
        nextBuildNumber = buildState.getLastBuild().get().getBuildNumber() + 1;
      } else {
        nextBuildNumber = 1;
      }

      Build build = Build.queuedBuild(module, nextBuildNumber);
      long id = buildDao.enqueue(build);
      build = build.withId(id);
      checkAffectedRowCount(moduleDao.updatePendingBuild(build));

      return buildState.withPendingBuild(build);
    }
  }

  @Transactional
  public void begin(Build build) {
    checkAffectedRowCount(buildDao.begin(build));
    checkAffectedRowCount(moduleDao.updateInProgressBuild(build));
  }

  @Transactional
  public void update(Build build) {
    if (build.getState().isComplete()) {
      Preconditions.checkArgument(build.getEndTimestamp().isPresent());
      Preconditions.checkArgument(build.getLog().isPresent());

      checkAffectedRowCount(buildDao.complete(build));
      checkAffectedRowCount(moduleDao.updateLastBuild(build));
      // TODO build queued module if present
    } else {
      checkAffectedRowCount(buildDao.update(build));
    }
  }

  private static void checkAffectedRowCount(int affectedRows) {
    Preconditions.checkState(affectedRows == 1, "Expected to update 1 row but updated %s", affectedRows);
  }
}
