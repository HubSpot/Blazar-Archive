package com.hubspot.blazar.listener;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.visitor.AbstractModuleBuildVisitor;
import com.hubspot.singularity.SingularityTaskCleanupResult;
import com.hubspot.singularity.api.SingularityKillTaskRequest;
import com.hubspot.singularity.client.SingularityClient;

/**
 * This class handles the cancellation of builds in Singularity by killing the task.
 */
@Singleton
public class SingularityTaskKiller extends AbstractModuleBuildVisitor {
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
  protected void visitCancelled(ModuleBuild build) {
    if (build.getTaskId().isPresent()) {
      killTask(build);
    }
  }

  public void killTask(ModuleBuild build) {
    String taskId = build.getTaskId().get();
    LOG.info("Killing singularity task for build {}", build.getId().get());
    Optional<SingularityTaskCleanupResult> result = singularityClient.killTask(taskId, Optional.of(killTaskRequest));
    if (result.isPresent()) {
      LOG.info("Cleanup result for task {}: {}", result.get());
    } else {
      LOG.info("No cleanup result for task {}", taskId);
    }
  }
}
