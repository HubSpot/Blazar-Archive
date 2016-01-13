package com.hubspot.blazar.listener;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.listener.ModuleBuildListener;
import com.hubspot.singularity.SingularityTaskCleanupResult;
import com.hubspot.singularity.api.SingularityKillTaskRequest;
import com.hubspot.singularity.client.SingularityClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SingularityTaskKiller implements ModuleBuildListener {
  private static final Logger LOG = LoggerFactory.getLogger(SingularityTaskKiller.class);

  private final SingularityClient singularityClient;
  private final SingularityKillTaskRequest killTaskRequest;

  @Inject
  public SingularityTaskKiller(SingularityClient singularityClient) {
    this.singularityClient = singularityClient;
    this.killTaskRequest = new SingularityKillTaskRequest(
        Optional.of(true),
        Optional.of("The associated Blazar build has been cancelled"),
        Optional.<String>absent(),
        Optional.<Boolean>absent()
    );
  }

  @Override
  public void buildChanged(ModuleBuild build) throws Exception {
    if (build.getTaskId().isPresent()) {
      String taskId = build.getTaskId().get();
      LOG.info("Killing singularity task {} for cancelled build {}", taskId, build.getId().get());
      Optional<SingularityTaskCleanupResult> result = singularityClient.killTask(taskId, Optional.of(killTaskRequest));
      if (result.isPresent()) {
        LOG.info("Cleanup result for task {}: {}", result.get());
      } else {
        LOG.info("No cleanup result for task {}", taskId);
      }
    }
  }
}
