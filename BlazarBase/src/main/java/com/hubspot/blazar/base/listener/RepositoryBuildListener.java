package com.hubspot.blazar.base.listener;

import com.hubspot.blazar.base.RepositoryBuild;

public interface RepositoryBuildListener {
  void buildChanged(RepositoryBuild build) throws Exception;
}
