package com.hubspot.blazar.data.service;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleActivityPage;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuild.State;
import com.hubspot.blazar.base.ModuleBuildInfo;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.data.dao.ModuleBuildDao;
import com.hubspot.blazar.data.dao.ModuleDao;

@Singleton
public class ModuleBuildService {
  public static final int MAX_MODULE_HISTORY_PAGE_SIZE = 10;
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

  public Optional<ModuleBuild> get(long id) {
    return moduleBuildDao.get(id);
  }

  public Set<ModuleBuild> getByRepositoryBuild(long repoBuildId) {
    return moduleBuildDao.getByRepositoryBuild(repoBuildId);
  }

  public Set<ModuleBuild> getByState(State state) {
    return moduleBuildDao.getByState(state);
  }

  public ModuleActivityPage getModuleBuildHistoryPage(int moduleId, Optional<Integer> maybeFromBuildNumber, Optional<Integer> maybePageSize) {
    int pageSize = MAX_MODULE_HISTORY_PAGE_SIZE;
    if (maybePageSize.isPresent() && 0 < maybePageSize.get() && maybePageSize.get() < MAX_MODULE_HISTORY_PAGE_SIZE) {
      pageSize = maybePageSize.get();
    }

    int fromBuildNumber;
    if (maybeFromBuildNumber.isPresent() && 0 < maybeFromBuildNumber.get()) {
      fromBuildNumber = maybeFromBuildNumber.get();
    } else {
      Optional<Integer> maxBuildNumber = moduleBuildDao.getMaxBuildNumber(moduleId);
      if (maxBuildNumber.isPresent()) {
        fromBuildNumber = maxBuildNumber.get();
      } else {
        return new ModuleActivityPage(ImmutableList.of(), 0);
      }
    }

    // get a specific page
    List<ModuleBuildInfo> builds = moduleBuildDao.getLimitedModuleBuildHistory(moduleId, fromBuildNumber, pageSize);
    int remainingCt = Math.max(0, moduleBuildDao.getRemainingBuildCountForPagedHistory(moduleId, fromBuildNumber).get() - builds.size());
    return new ModuleActivityPage(builds, remainingCt);
  }

  public Optional<ModuleBuild> getPreviousBuild(ModuleBuild build) {
    return moduleBuildDao.getPreviousBuild(build);
  }

  public Optional<ModuleBuild> getPreviousBuild(Module module) {
    return moduleBuildDao.getPreviousBuild(module);
  }

  public Optional<ModuleBuild> getByModuleAndNumber(int moduleId, int buildNumber) {
    return moduleBuildDao.getByModuleAndNumber(moduleId, buildNumber);
  }

  public void skip(RepositoryBuild repositoryBuild, Module module) {
    int nextBuildNumber = repositoryBuild.getBuildNumber();
    LOG.info("Skipping build for module {} with build number {}", module.getId().get(), nextBuildNumber);
    ModuleBuild build = ModuleBuild.skippedBuild(repositoryBuild, module, nextBuildNumber);
    build = skip(build);
    LOG.info("Skipped build for module {} with id {}", module.getId().get(), build.getId().get());
  }

  public void enqueue(RepositoryBuild repositoryBuild, Module module) {
    int nextBuildNumber = repositoryBuild.getBuildNumber();
    LOG.info("Enqueuing build for module {} with build number {}", module.getId().get(), nextBuildNumber);
    ModuleBuild build = ModuleBuild.queuedBuild(repositoryBuild, module, nextBuildNumber);
    build = enqueue(build);
    LOG.info("Enqueued build for module {} with id {}", module.getId().get(), build.getId().get());
  }

  @Transactional
  protected ModuleBuild skip(ModuleBuild build) {
    long id = moduleBuildDao.skip(build);
    build = build.withId(id);

    eventBus.post(build);

    return build;
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
    beginNoPublish(build);

    eventBus.post(build);
  }

  @Transactional
  void beginNoPublish(ModuleBuild build) {
    Preconditions.checkArgument(build.getStartTimestamp().isPresent());

    checkAffectedRowCount(moduleBuildDao.begin(build));
    checkAffectedRowCount(moduleDao.updateInProgressBuild(build));
  }

  @Transactional
  public void update(ModuleBuild build) {
    if (build.getState().isComplete()) {
      Preconditions.checkArgument(build.getEndTimestamp().isPresent());

      checkAffectedRowCount(moduleBuildDao.complete(build));
      checkAffectedRowCount(moduleDao.updateLastBuild(build));
    } else {
      checkAffectedRowCount(moduleBuildDao.update(build));
    }

    eventBus.post(build);
  }

  @Transactional
  public void cancel(ModuleBuild build) {
    if (build.getState().isComplete()) {
      throw new IllegalStateException(String.format("Build %d has already completed", build.getId().get()));
    }

    if (build.getState().isWaiting()) {
      beginNoPublish(build.withState(State.LAUNCHING).withStartTimestamp(System.currentTimeMillis()));
    }

    update(build.withState(State.CANCELLED).withEndTimestamp(System.currentTimeMillis()));
  }

  @Transactional
  public void fail(ModuleBuild build) {
    if (build.getState().isComplete()) {
      throw new IllegalStateException(String.format("Build %d has already completed", build.getId().get()));
    }

    if (build.getState().isWaiting()) {
      beginNoPublish(build.withState(State.LAUNCHING).withStartTimestamp(System.currentTimeMillis()));
    }

    update(build.withState(State.FAILED).withEndTimestamp(System.currentTimeMillis()));
  }

  private static void checkAffectedRowCount(int affectedRows) {
    Preconditions.checkState(affectedRows == 1, "Expected to update 1 row but updated %s", affectedRows);
  }
}
