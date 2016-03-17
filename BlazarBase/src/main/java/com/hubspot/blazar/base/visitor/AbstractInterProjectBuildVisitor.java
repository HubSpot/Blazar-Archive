package com.hubspot.blazar.base.visitor;

import com.hubspot.blazar.base.InterProjectBuild;

public class AbstractInterProjectBuildVisitor implements InterProjectBuildVisitor {
  @Override
  public final void visit(InterProjectBuild build) throws Exception {
    switch (build.getState()) {
      case CALCULATING:
        visitQueued(build);
        break;
      case RUNNING:
        visitRunning(build);
        break;
      case FINISHED:
        visitFinished(build);
        break;
      default:
        throw new IllegalStateException("Unexpected build state: " + build.getState());
    }

  }

  protected void visitRunning(InterProjectBuild build) throws Exception {}
  protected void visitFinished(InterProjectBuild build) throws Exception {}
  protected void visitQueued(InterProjectBuild build) throws Exception {}

}
