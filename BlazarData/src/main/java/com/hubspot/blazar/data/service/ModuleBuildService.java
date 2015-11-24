package com.hubspot.blazar.data.service;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.data.dao.ModuleBuildDao;
import com.hubspot.blazar.data.dao.ModuleDao;
import com.hubspot.blazar.data.util.BuildNumbers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;

@Singleton
public class ModuleBuildService {
  private static final Logger LOG = LoggerFactory.getLogger(ModuleBuildService.class);

  private final ModuleBuildDao moduleBuildDao;
  private final ModuleDao moduleDao;
  private final EventBus eventBus;

  @Inject
  public ModuleBuildService(ModuleBuildDao moduleBuildDao, ModuleDao moduleDao, EventBus eventBus) {
    this.moduleBuildDao = moduleBuildDao;
    this.moduleDao = moduleDao;
    this.eventBus = eventBus;
  }

  public BuildNumbers getBuildNumbers(int moduleId) {
    return moduleBuildDao.getBuildNumbers(moduleId);
  }

  public void enqueue(RepositoryBuild repositoryBuild, Module module) {
    BuildNumbers buildNumbers = getBuildNumbers(module.getId().get());

    if (buildNumbers.getPendingBuildId().isPresent()) {
      int pendingBuildId = buildNumbers.getPendingBuildId().get();
      LOG.info("Not enqueuing build for module {}, pending build {} already exists", module.getId().get(), pendingBuildId);
    } else {
      int nextBuildNumber = buildNumbers.getNextBuildNumber();
      LOG.info("Enqueuing build for module {} with build number {}", module.getId().get(), nextBuildNumber);
      ModuleBuild build = ModuleBuild.queuedBuild(repositoryBuild, module, nextBuildNumber);
      build = enqueue(build);
      LOG.info("Enqueued build for module {} with id {}", module.getId().get(), build.getId().get());
    }
  }

  @Transactional
  protected ModuleBuild enqueue(ModuleBuild build) {
    long id = moduleBuildDao.enqueue(build);
    build = build.withId(id);

    checkAffectedRowCount(moduleDao.updatePendingBuild(build));

    eventBus.post(build);

    return build;
  }

  @Transactional
  public void begin(ModuleBuild build) {
    Preconditions.checkArgument(build.getStartTimestamp().isPresent());

    checkAffectedRowCount(moduleBuildDao.begin(build));
    checkAffectedRowCount(moduleDao.updateInProgressBuild(build));

    eventBus.post(build);
  }

  private static void checkAffectedRowCount(int affectedRows) {
    Preconditions.checkState(affectedRows == 1, "Expected to update 1 row but updated %s", affectedRows);
  }
}
