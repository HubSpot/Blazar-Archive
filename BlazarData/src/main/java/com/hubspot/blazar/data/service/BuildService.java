package com.hubspot.blazar.data.service;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.hubspot.blazar.base.*;
import com.hubspot.blazar.data.dao.BuildDao;
import com.hubspot.blazar.data.dao.ModuleDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Transactional;
import java.util.List;

public class BuildService {
  private static final Logger LOG = LoggerFactory.getLogger(BuildService.class);

  private final BuildStateService buildStateService;
  private final BuildDao buildDao;
  private final ModuleDao moduleDao;
  private final EventBus eventBus;

  @Inject
  public BuildService(BuildStateService buildStateService, BuildDao buildDao, ModuleDao moduleDao, EventBus eventBus) {
    this.buildStateService = buildStateService;
    this.buildDao = buildDao;
    this.moduleDao = moduleDao;
    this.eventBus = eventBus;
  }

  public Optional<ModuleBuild> get(long id) {
    return buildDao.get(id);
  }

  public Optional<ModuleBuild> get(GitInfo info, String moduleName, Integer buildNumber) {
    return buildDao.get(info, moduleName, buildNumber);
  }

  public List<Build> getByModule(Module module) {
    return buildDao.getByModule(module);
  }

  public BuildState enqueue(BuildDefinition definition) {
    int moduleId = definition.getModule().getId().get();
    BuildState buildState = buildStateService.getByModule(moduleId);

    if (buildState.getPendingBuild().isPresent()) {
      long pendingBuildId = buildState.getPendingBuild().get().getId().get();
      LOG.info("Not enqueuing build for module {}, pending build {} already exists", moduleId, pendingBuildId);
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

      LOG.info("Enqueuing build for module {} with build number {}", moduleId, nextBuildNumber);
      Build build = Build.queuedBuild(definition.getModule(), nextBuildNumber);
      build = enqueue(build);
      LOG.info("Enqueued build for module {} with id {}", moduleId, build.getId().get());

      return buildState.withPendingBuild(build);
    }
  }

  @Transactional
  protected Build enqueue(Build build) {
    long id = buildDao.enqueue(build);
    build = build.withId(id);

    checkAffectedRowCount(moduleDao.updatePendingBuild(build));

    eventBus.post(build);

    return build;
  }

  @Transactional
  public void begin(Build build) {
    checkAffectedRowCount(buildDao.begin(build));
    checkAffectedRowCount(moduleDao.updateInProgressBuild(build));

    eventBus.post(build);
  }

  @Transactional
  public void update(Build build) {
    if (build.getState().isComplete()) {
      Preconditions.checkArgument(build.getEndTimestamp().isPresent());
      Preconditions.checkArgument(build.getLog().isPresent());

      checkAffectedRowCount(buildDao.complete(build));
      checkAffectedRowCount(moduleDao.updateLastBuild(build));
    } else {
      checkAffectedRowCount(buildDao.update(build));
    }

    eventBus.post(build);
  }

  private static void checkAffectedRowCount(int affectedRows) {
    Preconditions.checkState(affectedRows == 1, "Expected to update 1 row but updated %s", affectedRows);
  }
}
