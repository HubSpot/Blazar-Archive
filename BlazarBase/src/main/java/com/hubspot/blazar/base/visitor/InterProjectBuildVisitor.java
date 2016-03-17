package com.hubspot.blazar.base.visitor;

import com.hubspot.blazar.base.InterProjectBuild;

public interface InterProjectBuildVisitor {
  void visit(InterProjectBuild interProjectBuild) throws Exception;
}
