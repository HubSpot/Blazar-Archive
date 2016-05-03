package com.hubspot.blazar.base.visitor;

import java.util.Iterator;

import com.hubspot.blazar.base.InterProjectBuild;

public class AbstractInterProjectBuildVisitor implements InterProjectBuildVisitor {
  @Override
  public final void visit(InterProjectBuild build) throws Exception {
    switch (build.getState()) {
      case QUEUED:
        visitQueued(build);
        break;
      case IN_PROGRESS:
        visitRunning(build);
        break;
      case FAILED:
        visitFailed(build);
        break;
      case CANCELLED:
        visitCancelled(build);
        break;
      case SUCCEEDED:
        visitSucceeded(build);
        break;
      default:
        throw new IllegalStateException("Unexpected build state: " + build.getState());
    }

  }

  protected void visitQueued(InterProjectBuild build) throws Exception {}
  protected void visitRunning(InterProjectBuild build) throws Exception {}
  protected void visitCancelled(InterProjectBuild build) throws Exception {}
  protected void visitFailed(InterProjectBuild build) throws Exception {}
  protected void visitSucceeded(InterProjectBuild build) throws Exception {}

}
