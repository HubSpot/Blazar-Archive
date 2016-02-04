package com.hubspot.blazar.base.visitor;

import com.hubspot.blazar.base.RepositoryBuild;

public abstract class AbstractRepositoryBuildVisitor implements RepositoryBuildVisitor {

  @Override
  public final void visit(RepositoryBuild build) throws Exception {
    switch (build.getState()) {
      case QUEUED:
        visitQueued(build);
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
      case UNSTABLE:
        visitUnstable(build);
        break;
      default:
        throw new IllegalStateException("Unexpected build state: " + build.getState());
    }
  }

  protected void visitQueued(RepositoryBuild build) throws Exception {}
  protected void visitLaunching(RepositoryBuild build) throws Exception {}
  protected void visitInProgress(RepositoryBuild build) throws Exception {}
  protected void visitSucceeded(RepositoryBuild build) throws Exception {}
  protected void visitCancelled(RepositoryBuild build) throws Exception {}
  protected void visitFailed(RepositoryBuild build) throws Exception {}
  protected void visitUnstable(RepositoryBuild build) throws Exception {}
}
