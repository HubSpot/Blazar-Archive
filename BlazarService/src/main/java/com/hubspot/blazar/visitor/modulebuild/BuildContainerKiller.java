package com.hubspot.blazar.visitor.modulebuild;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.visitor.AbstractModuleBuildVisitor;
import com.hubspot.blazar.exception.NonRetryableBuildException;
import com.hubspot.blazar.externalservice.BuildClusterService;

/**
 * This class handles the cancellation of builds by killing the build container.
 */
@Singleton
public class BuildContainerKiller extends AbstractModuleBuildVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(BuildContainerKiller.class);

  private final BuildClusterService buildClusterService;

  @Inject
  public BuildContainerKiller(BuildClusterService buildClusterService) {
    this.buildClusterService = buildClusterService;
  }

  @Override
  protected void visitCancelled(ModuleBuild moduleBuild) throws NonRetryableBuildException {
    try {
      if (!moduleBuild.getTaskId().isPresent()) {
        LOG.debug("Cancelled module build {} doesn't contain a container id (taskId) so we will not try to kill its container.", moduleBuild.getId().get());
        return;
      }
      buildClusterService.killBuildContainer(moduleBuild);
    } catch (Exception e) {
      throw new NonRetryableBuildException(String.format("A problem encountered while trying to kill the container of cancelled module build %d", moduleBuild.getId().get()), e);
    }
  }
}
