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
import com.google.common.eventbus.EventBus;
import com.hubspot.blazar.base.BuildConfig;
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
  public static final int MAX_MODULE_ACTIVITY_PAGE_SIZE = 100;
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

  public ModuleActivityPage getModuleActivityPage(int moduleId, Optional<Integer> maybeFromBuildNumber, Optional<Integer> maybePageSize) {
    int pageSize = MAX_MODULE_ACTIVITY_PAGE_SIZE;
    if (maybePageSize.isPresent() && 0 < maybePageSize.get() && maybePageSize.get() < MAX_MODULE_ACTIVITY_PAGE_SIZE) {
      pageSize = maybePageSize.get();
    }

    List<ModuleBuildInfo> builds;
    long start = System.currentTimeMillis();
    if (maybeFromBuildNumber.isPresent() && 0 < maybeFromBuildNumber.get()) {
      int fromBuildNumber = maybeFromBuildNumber.get();
      builds = moduleBuildDao.getLimitedModuleBuildHistory(moduleId, fromBuildNumber, pageSize);
    } else {
      builds = moduleBuildDao.getLatestLimitedModuleBuildHistory(moduleId, pageSize);
    }
    LOG.debug("Got {} builds of activity for module {} in {}", builds.size(), moduleId, System.currentTimeMillis() - start);

    // calculate remaining
    if (builds.isEmpty()) {
      return new ModuleActivityPage(builds, 0);
    }
    int lastBuildNumber = builds.get(builds.size() - 1).getBranchBuild().getBuildNumber();
    /**
     * We have to actually count this, because if a module gets added to a branch its build history
     * starts at the build# for the build it was added on, and doesn't go back all the way to "1"
     */
    Optional<Integer> remainingCount = moduleBuildDao.getRemainingBuildCountForPagedHistory(moduleId, lastBuildNumber);

    // the count() aggregation shouldn't return nothing, but in case it does:
    if (!remainingCount.isPresent()) {
      return new ModuleActivityPage(builds, 0);
    }
    return new ModuleActivityPage(builds, remainingCount.get());
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


  public ModuleBuild enqueue(RepositoryBuild repositoryBuild, Module module, BuildConfig buildConfig, BuildConfig resolvedBuildConfig) {
    int nextBuildNumber = repositoryBuild.getBuildNumber();
    LOG.info("Enqueuing build for module {} with build number {}", module.getId().get(), nextBuildNumber);
    ModuleBuild build = enqueue(ModuleBuild.queuedBuild(repositoryBuild, module, nextBuildNumber, buildConfig, resolvedBuildConfig));
    LOG.info("Enqueued build for module {} with id {}", module.getId().get(), build.getId().get());
    return build;
  }

  @Transactional
  protected ModuleBuild skip(ModuleBuild build) {
    long id = moduleBuildDao.skip(build);
    build = build.toBuilder().setId(Optional.of(id)).build();

    eventBus.post(build);

    return build;
  }

  @Transactional
  protected ModuleBuild enqueue(ModuleBuild build) {
    long id = moduleBuildDao.enqueue(build);
    build = build.toBuilder().setId(Optional.of(id)).build();

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
      beginNoPublish(build.toBuilder().setState(State.LAUNCHING).setStartTimestamp(Optional.of(System.currentTimeMillis())).build());
    }

    update(build.toBuilder().setState(State.CANCELLED).setEndTimestamp(Optional.of(System.currentTimeMillis())).build());
  }

  @Transactional
  public ModuleBuild fail(ModuleBuild build) {
    if (build.getState().isComplete()) {
      throw new IllegalStateException(String.format("Build %d has already completed", build.getId().get()));
    }

    if (build.getState().isWaiting()) {
      beginNoPublish(build.toBuilder()
          .setState(State.LAUNCHING)
          .setStartTimestamp(Optional.of(System.currentTimeMillis()))
          .build());
    }

    ModuleBuild failed = build.toBuilder()
        .setState(State.FAILED)
        .setEndTimestamp(Optional.of(System.currentTimeMillis()))
        .build();
    update(failed);
    return failed;
  }

  /**
   * This creates a failed module build without posting any build events. This allows us to fail module builds
   * without having visitors do things for build states that this module is essentially skipping:
   * For example we can avoid launching containers into singularity {@Link LaunchSingularityTaskBuildVisitor}.
   * This means that we skip all the visitors for ModuleBuilds except the ones that trigger on `State.FAILED`
   */
  @Transactional
  public ModuleBuild createFailedBuild(RepositoryBuild repositoryBuild, Module module) {
    int nextBuildNumber = repositoryBuild.getBuildNumber();

    LOG.info("Enqueuing build for module {} with build number {}", module.getId().get(), nextBuildNumber);
    ModuleBuild queued =
        ModuleBuild.queuedBuild(repositoryBuild, module, nextBuildNumber, BuildConfig.makeDefaultBuildConfig(), BuildConfig.makeDefaultBuildConfig());
    long id = moduleBuildDao.enqueue(queued);
    ModuleBuild queuedWithId = queued.toBuilder().setId(Optional.of(id)).build();
    checkAffectedRowCount(moduleDao.updatePendingBuild(queuedWithId));

    LOG.info("Enqueued build for module {} with id {}", module.getId().get(), id);
    ModuleBuild failed = fail(queuedWithId);
    LOG.info("Failed build for module {} with id {}", module.getId().get(), id);

    eventBus.post(failed);
    return failed;
  }

  public int updateBuildClusterName(int moduleId, String buildClusterName) {
    return moduleBuildDao.updateBuildClusterName(moduleId, buildClusterName);
  }

  private static void checkAffectedRowCount(int affectedRows) {
    Preconditions.checkState(affectedRows == 1, "Expected to update 1 row but updated %s", affectedRows);
  }
}
