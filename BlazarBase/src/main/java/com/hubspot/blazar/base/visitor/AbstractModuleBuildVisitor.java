package com.hubspot.blazar.base.visitor;

import com.hubspot.blazar.base.ModuleBuild;

public abstract class AbstractModuleBuildVisitor implements ModuleBuildVisitor {

  @Override
  public final void visit(ModuleBuild build) throws Exception {
    switch (build.getState()) {
      case QUEUED:
        visitQueued(build);
        break;
      case WAITING_FOR_UPSTREAM_BUILD:
        visitWaitingForUpstreamBuild(build);
        break;
      case LAUNCHING:
        visitLaunching(build);
        break;
      case IN_PROGRESS:
        visitInProgress(build);
        break;
      case SUCCEEDED:
        visitSucceeded(build);
        break;
      case CANCELLED:
        visitCancelled(build);
        break;
      case FAILED:
        visitFailed(build);
        break;
      case SKIPPED:
        visitSkipped(build);
        break;
      default:
        throw new IllegalStateException("Unexpected build state: " + build.getState());
    }
  }

  protected void visitQueued(ModuleBuild build) throws Exception {}
  protected void visitWaitingForUpstreamBuild(ModuleBuild build) throws Exception {}
  protected void visitLaunching(ModuleBuild build) throws Exception {}
  protected void visitInProgress(ModuleBuild build) throws Exception {}
  protected void visitSucceeded(ModuleBuild build) throws Exception {}
  protected void visitCancelled(ModuleBuild build) throws Exception {}
  protected void visitFailed(ModuleBuild build) throws Exception {}
  protected void visitSkipped(ModuleBuild build) throws Exception {}
}
