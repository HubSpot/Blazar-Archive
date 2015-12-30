package com.hubspot.blazar.util;

import com.google.common.base.Optional;
import com.google.inject.name.Named;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuild.State;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.singularity.ExtendedTaskState;
import com.hubspot.singularity.SingularityTaskHistory;
import com.hubspot.singularity.SingularityTaskHistoryUpdate;
import com.hubspot.singularity.SingularityTaskIdHistory;
import com.hubspot.singularity.client.SingularityClient;
import io.dropwizard.lifecycle.Managed;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class SingularityBuildWatcher implements LeaderLatchListener, Managed {
  private static final Logger LOG = LoggerFactory.getLogger(SingularityBuildWatcher.class);

  private final ScheduledExecutorService executorService;
  private final ModuleBuildService moduleBuildService;
  private final SingularityClient singularityClient;
  private final AtomicBoolean running;
  private final AtomicBoolean leader;

  @Inject
  public SingularityBuildWatcher(@Named("QueueProcessor") ScheduledExecutorService executorService,
                                 ModuleBuildService moduleBuildService,
                                 SingularityClient singularityClient) {
    this.executorService = executorService;
    this.moduleBuildService = moduleBuildService;
    this.singularityClient = singularityClient;

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
          for (ModuleBuild build : moduleBuildService.getByState(State.LAUNCHING)) {
            long age = System.currentTimeMillis() - build.getStartTimestamp().get();
            if (age < TimeUnit.MINUTES.toMillis(1)) {
              continue;
            }

            String runId =  String.valueOf(build.getId().get());
            Optional<SingularityTaskIdHistory> task = singularityClient.getHistoryForTask("blazar-executor", runId);
            if (task.isPresent()) {
              Optional<ExtendedTaskState> taskState = task.get().getLastTaskState();
              if (taskState.isPresent() && taskState.get().isDone()) {
                LOG.info("Updating build {} to FAILED because runId {} is done", build.getId().get(), runId);
                moduleBuildService.update(build.withState(State.FAILED).withEndTimestamp(task.get().getUpdatedAt()));
              }
            }
          }

          for (ModuleBuild build : moduleBuildService.getByState(State.IN_PROGRESS)) {
            long age = System.currentTimeMillis() - build.getStartTimestamp().get();
            if (age < TimeUnit.MINUTES.toMillis(2)) {
              continue;
            }

            String taskId = build.getTaskId().get();
            Optional<Long> completedTimestamp = completedTimestamp(taskId);

            if (completedTimestamp.isPresent()) {
              LOG.info("Updating build {} to FAILED because taskId {} is done", build.getId().get(), taskId);
              moduleBuildService.update(build.withState(State.FAILED).withEndTimestamp(completedTimestamp.get()));
            }
          }
        }
      } catch (Throwable t) {
        LOG.error("Error checking for failed or lost tasks", t);
      }
    }

    private Optional<Long> completedTimestamp(String taskId) {
      Optional<SingularityTaskHistory> taskHistory = singularityClient.getHistoryForTask(taskId);
      if (!taskHistory.isPresent()) {
        LOG.warn("No task history found for taskId {}", taskId);
        return Optional.absent();
      }

      for (SingularityTaskHistoryUpdate taskUpdate : taskHistory.get().getTaskUpdates()) {
        if (taskUpdate.getTaskState().isDone()) {
          return Optional.of(taskUpdate.getTimestamp());
        }
      }

      return Optional.absent();
    }
  }
}
