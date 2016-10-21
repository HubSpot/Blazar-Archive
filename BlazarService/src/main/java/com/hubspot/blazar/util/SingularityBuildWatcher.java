package com.hubspot.blazar.util;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.google.inject.name.Named;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuild.State;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.ExecutorConfiguration;
import com.hubspot.blazar.config.SingularityConfiguration;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.listener.SingularityTaskKiller;
import com.hubspot.singularity.ExtendedTaskState;
import com.hubspot.singularity.SingularityTaskHistory;
import com.hubspot.singularity.SingularityTaskHistoryUpdate;
import com.hubspot.singularity.SingularityTaskIdHistory;
import com.hubspot.singularity.client.SingularityClient;
import io.dropwizard.lifecycle.Managed;

@Singleton
public class SingularityBuildWatcher implements LeaderLatchListener, Managed {
  private static final Logger LOG = LoggerFactory.getLogger(SingularityBuildWatcher.class);

  private final ScheduledExecutorService executorService;
  private final ModuleBuildService moduleBuildService;
  private final SingularityClient singularityClient;
  private final SingularityTaskKiller singularityTaskKiller;
  private final MetricRegistry metricRegistry;
  private final SingularityConfiguration singularityConfiguration;
  private final ExecutorConfiguration executorConfiguration;
  private final AtomicBoolean running;
  private final AtomicBoolean leader;

  @Inject
  public SingularityBuildWatcher(@Named("QueueProcessor") ScheduledExecutorService executorService,
                                 ModuleBuildService moduleBuildService,
                                 SingularityClient singularityClient,
                                 SingularityTaskKiller singularityTaskKiller,
                                 MetricRegistry metricRegistry,
                                 BlazarConfiguration blazarConfiguration) {
    this.executorService = executorService;
    this.moduleBuildService = moduleBuildService;
    this.singularityClient = singularityClient;
    this.singularityTaskKiller = singularityTaskKiller;
    this.metricRegistry = metricRegistry;
    this.singularityConfiguration = blazarConfiguration.getSingularityConfiguration();
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
          for (ModuleBuild build : moduleBuildService.getByState(State.LAUNCHING)) {
            long age = System.currentTimeMillis() - build.getStartTimestamp().get();
            if (age < TimeUnit.MINUTES.toMillis(1)) {
              continue;
            }

            String requestId = singularityConfiguration.getRequest();
            String runId =  String.valueOf(build.getId().get());
            Optional<SingularityTaskIdHistory> task = singularityClient.getHistoryForTask(requestId, runId);
            if (task.isPresent()) {
              Optional<ExtendedTaskState> taskState = task.get().getLastTaskState();
              if (taskState.isPresent() && taskState.get().isDone()) {
                String taskId = task.get().getTaskId().getId();
                LOG.info("Updating build {} to FAILED because taskId {} is done", build.getId().get(), taskId);
                moduleBuildService.update(build.toBuilder().setState(State.FAILED).setTaskId(Optional.of(taskId)).setEndTimestamp(Optional.of(task.get().getUpdatedAt())).build());
                if (taskState.get().isSuccess()) {
                  metricRegistry.meter(getClass().getName() + ".succeeded").mark();
                } else {
                  metricRegistry.meter(getClass().getName() + ".failed").mark();
                }
              }
            }
          }

          for (ModuleBuild build : moduleBuildService.getByState(State.IN_PROGRESS)) {
            long age = System.currentTimeMillis() - build.getStartTimestamp().get();
            long maxAge = executorConfiguration.getBuildTimeoutMillis();
            if (age < TimeUnit.MINUTES.toMillis(1)) {
              continue;
            }

            String taskId = build.getTaskId().get();
            Optional<SingularityTaskHistory> taskHistory = singularityClient.getHistoryForTask(taskId);
            Optional<Long> completedTimestamp = completedTimestamp(taskHistory);
            if (!taskHistory.isPresent()) {
              LOG.warn("No task history found for taskId {}", taskId);
            } else if (completedTimestamp.isPresent()) {
              LOG.info("Failing build {} because taskId {} is done", build.getId().get(), taskId);
              moduleBuildService.update(build.toBuilder().setState(State.FAILED).setEndTimestamp(Optional.of(completedTimestamp.get())).build());
            } else if (age > maxAge) {
              LOG.info("Failing build {} because its age {} exceeded max of {}", build.getId().get(), age, maxAge);
              moduleBuildService.update(build.toBuilder().setState(State.FAILED).setEndTimestamp(Optional.of(System.currentTimeMillis())).build());
              singularityTaskKiller.killTask(build);
            }
          }
        }
      } catch (Throwable t) {
        LOG.error("Error checking for failed or lost tasks", t);
      }
    }

    private Optional<Long> completedTimestamp(Optional<SingularityTaskHistory> taskHistory) {
      if (!taskHistory.isPresent()) {
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
