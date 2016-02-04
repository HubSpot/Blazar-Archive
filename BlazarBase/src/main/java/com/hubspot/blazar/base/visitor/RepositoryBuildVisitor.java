package com.hubspot.blazar.base.visitor;

import com.hubspot.blazar.base.RepositoryBuild;

public interface RepositoryBuildVisitor {
  void visit(RepositoryBuild build) throws Exception;
}
