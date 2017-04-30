package com.hubspot.blazar.externalservice;

import static com.hubspot.blazar.externalservice.BuildClusterService.BuildContainerInfo.BuildContainerState.FINISHED;
import static com.hubspot.blazar.externalservice.BuildClusterService.BuildContainerInfo.BuildContainerState.NOT_STARTED;
import static com.hubspot.blazar.externalservice.BuildClusterService.BuildContainerInfo.BuildContainerState.RUNNING;
import static com.hubspot.blazar.externalservice.BuildClusterService.BuildContainerInfo.BuildContainerState.UNKNOWN;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.inject.name.Named;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuild.State;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.ExecutorConfiguration;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.externalservice.BuildClusterService.BuildContainerInfo;
import com.hubspot.blazar.externalservice.BuildClusterService.BuildContainerInfo.BuildContainerState;

import io.dropwizard.lifecycle.Managed;

/**
 * Watches for builds that have completed without BlazarService's knowledge.
 * This can happen when a build container is killed by mesos because of an OOM, or is lost
 * due to slave disconnections etc. When we find out that a build container has finished
 * running without reporting SUCCESS back to Blazar we consider that build failed.
 *
 * We also watch for builds that have been running for longer than the maximum time configured.
 * If we find a build container that has been running for longer than the configured time we kill
 * the container and fail the build.
 */
@Singleton
public class LostBuildCleaner implements LeaderLatchListener, Managed {
  private static final Logger LOG = LoggerFactory.getLogger(LostBuildCleaner.class);

  private final ScheduledExecutorService executorService;
  private final ModuleBuildService moduleBuildService;
  private final BuildClusterService buildClusterService;
  private final ExecutorConfiguration executorConfiguration;
  private final AtomicBoolean running;
  private final AtomicBoolean leader;

  @Inject
  public LostBuildCleaner(@Named("QueueProcessor") ScheduledExecutorService executorService,
                          ModuleBuildService moduleBuildService,
                          BuildClusterService buildClusterService,
                          BlazarConfiguration blazarConfiguration) {
    this.executorService = executorService;
    this.moduleBuildService = moduleBuildService;
    this.buildClusterService = buildClusterService;
    this.executorConfiguration = blazarConfiguration.getExecutorConfiguration();

    this.running = new AtomicBoolean();
    this.leader = new AtomicBoolean();
  }

  @Override
  public void start() throws Exception {
    running.set(true);
    executorService.scheduleAtFixedRate(new BuildChecker(), 0, 10, TimeUnit.SECONDS);
  }

  @Override
  public void stop() throws Exception {
    running.set(false);
  }

  @Override
  public void isLeader() {
    leader.set(true);
  }

  @Override
  public void notLeader() {
    leader.set(false);
  }

  private class BuildChecker implements Runnable {

    @Override
    public void run() {
      try {
        if (running.get() && leader.get()) {
          handleLostLaunchingBuilds();
          handleLongRunningBuilds();
        }
      } catch (Throwable t) {
        LOG.error("Error checking for failed or lost tasks", t);
      }
    }

    // Handle builds that are in launching state and their container didn't report back after a minute
    private void handleLostLaunchingBuilds() {
      for (ModuleBuild moduleBuild : moduleBuildService.getByState(State.LAUNCHING)) {
        handleLostLaunchingBuild(moduleBuild);
      }
    }

    private void handleLostLaunchingBuild(ModuleBuild moduleBuild) {
      try {
        long age = System.currentTimeMillis() - moduleBuild.getStartTimestamp().get();
        if (age < TimeUnit.MINUTES.toMillis(1)) {
          return;
        }

        BuildContainerInfo buildContainerInfo = buildClusterService.getBuildContainerInfo(moduleBuild);
        BuildContainerState buildContainerState = buildContainerInfo.getState();
        //If the container has not yet started, it is running, or we don't know its state we will wait to check again in the next cycle
        if (buildContainerState == NOT_STARTED || buildContainerState == RUNNING || buildContainerState == UNKNOWN) {
          return;
        }

        //It has finished and didn't report back. We consider the build failed
        if (buildContainerState == FINISHED) {
          LOG.info("Updating module build {} to FAILED because the container with id %s has finished (failed/succeded/killed/lost) and it didn't report back",
              moduleBuild.getId().get(), buildContainerInfo.getContainerId().get());
          moduleBuildService.update(moduleBuild.toBuilder()
              .setState(State.FAILED)
              .setTaskId(buildContainerInfo.getContainerId())
              .setEndTimestamp(Optional.of(buildContainerInfo.getUpdatedAtMillis()))
              .build());
        }
      } catch (Throwable t) {
        LOG.error("An error occurred while checking module build {}. The error is: {}. Will try again to check the module in the next cycle", moduleBuild.getId().get(), t.getMessage(), t);
      }
    }

    // Handle builds that are in progress for too long, i.e. they have reported back that the build has started but
    // they haven't reported back to signal that the build finished after waiting for executorConfiguration.getBuildTimeoutMillis()
    private void handleLongRunningBuilds() {
      for (ModuleBuild moduleBuild : moduleBuildService.getByState(State.IN_PROGRESS)) {
        handleLongRunningBuild(moduleBuild);
      }
    }

    private void handleLongRunningBuild(ModuleBuild moduleBuild) {
      LOG.debug("Checking state of build container for module build: {}", moduleBuild.getId().get());
      try {
        long age = System.currentTimeMillis() - moduleBuild.getStartTimestamp().get();
        long maxAge = executorConfiguration.getBuildTimeoutMillis();
        if (age < TimeUnit.MINUTES.toMillis(1)) {
          return;
        }

        BuildContainerInfo buildContainerInfo = buildClusterService.getBuildContainerInfo(moduleBuild);
        BuildContainerState buildContainerState = buildContainerInfo.getState();
        //The fact that the module build has state IN_PROGRESS means that its container started and it reported back
        // to blazar once to tell us that it has started but it has not yet reported back to tell us that it has finished.
        // So if it is reported as NOT_STARTED (i.e. no container could be found with the provided container id)
        // or the container state is UNKNOWN or the container has finished (and didn't reported back) or the container
        // is running for longer than maxAge we will mark the module build as failed. In the latter case we will also
        // ask the build cluster service to kill the container.
        boolean buildExceededMaxAllowedRunningTime = buildContainerState == RUNNING && age > maxAge;
        if (buildContainerState == NOT_STARTED) {
          LOG.warn("Module build {} is in progress for {}ms but could not find its container in build cluster. Will try again in the next cycle.", moduleBuild.getId().get(), age);
          // Todo actually fail things that are not started after 1 minute
          // LOG.info("Failing module build {} because the build is registered in Blazar db as 'in progress' but the build cluster reports it as NOT STARTED", moduleBuild.getId().get());
          // moduleBuildService.update(moduleBuild.toBuilder().setState(State.FAILED).setEndTimestamp(Optional.of(buildContainerInfo.getUpdatedAtMillis())).build());
        } else if (buildContainerState == UNKNOWN) {
          LOG.info("Failing module build {} because the build is registered in Blazar db as 'in progress' but the build cluster reports its state as UNKNOWN", moduleBuild.getId().get());
          moduleBuildService.update(moduleBuild.toBuilder().setState(State.FAILED).setEndTimestamp(Optional.of(buildContainerInfo.getUpdatedAtMillis())).build());
        } else if (buildContainerState == FINISHED) {
          LOG.info("Failing module build {} because the build is registered in Blazar db as 'in progress' but the build cluster reports that the container has FINISHED (failed/succeded/killed/lost)", moduleBuild.getId().get());
          moduleBuildService.update(moduleBuild.toBuilder().setState(State.FAILED).setEndTimestamp(Optional.of(buildContainerInfo.getUpdatedAtMillis())).build());
        } else if (buildExceededMaxAllowedRunningTime) {
          LOG.info("Terminating container of module build {} because it is running for {} milliseconds which exceeds the max allowed running time of {} milliseconds. The build will be marked as failed", moduleBuild.getId().get(), age, maxAge);
          moduleBuildService.update(moduleBuild.toBuilder().setState(State.FAILED).setEndTimestamp(Optional.of(buildContainerInfo.getUpdatedAtMillis())).build());
          try {
            buildClusterService.killBuildContainer(moduleBuild);
          } catch (Exception e) {
            LOG.error("Failed to kill running container of module build {}. The problem was: {}",
                moduleBuild.getId().get(), e.getMessage(), e);
          }
        } else {
          LOG.info("Build container for module build {} has state {}. The container is running for {}ms which doesn't exceed the max allowed running time of {}ms. No need to initiate cleanup",
              moduleBuild.getId().get(), buildContainerState, age, maxAge);
        }
      } catch (Throwable t) {
        LOG.error("An error occurred while checking the build container state for module build {}. The error is: {}. Will try again to check the module in the next cycle", moduleBuild.getId().get(), t.getMessage(), t);
      }
    }
  }
}
