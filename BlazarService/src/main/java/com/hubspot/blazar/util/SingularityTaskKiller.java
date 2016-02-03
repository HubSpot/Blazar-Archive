package com.hubspot.blazar.util;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hubspot.blazar.base.Build;
import com.hubspot.singularity.SingularityTaskCleanupResult;
import com.hubspot.singularity.client.SingularityClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SingularityTaskKiller {
  private static final Logger LOG = LoggerFactory.getLogger(SingularityTaskKiller.class);

  private final SingularityClient singularityClient;

  @Inject
  public SingularityTaskKiller(SingularityClient singularityClient, EventBus eventBus) {
    this.singularityClient = singularityClient;

    eventBus.register(this);
  }

  @Subscribe
  public void killSingularityTask(Build build) throws Exception {
    if (build.getState() == Build.State.CANCELLED && build.getTaskId().isPresent()) {
      String taskId = build.getTaskId().get();
      LOG.info("Killing singularity task {} for cancelled build {}", taskId, build.getId().get());
      Optional<SingularityTaskCleanupResult> result = singularityClient.killTask(taskId, Optional.<String>absent());
      if (result.isPresent()) {
        LOG.info("Cleanup result for task {}: {}", result.get());
      } else {
        LOG.info("No cleanup result for task {}", taskId);
      }
    }
  }
}
