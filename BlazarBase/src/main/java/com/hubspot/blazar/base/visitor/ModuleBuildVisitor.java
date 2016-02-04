package com.hubspot.blazar.base.visitor;

import com.hubspot.blazar.base.ModuleBuild;

public interface ModuleBuildVisitor {
  void visit(ModuleBuild build) throws Exception;
}
