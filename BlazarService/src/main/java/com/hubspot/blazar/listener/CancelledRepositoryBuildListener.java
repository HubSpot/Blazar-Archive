package com.hubspot.blazar.listener;

import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.listener.RepositoryBuildListener;

import javax.inject.Singleton;

@Singleton
public class CancelledRepositoryBuildListener implements RepositoryBuildListener {

  @Override
  public void buildChanged(RepositoryBuild build) throws Exception {
    // TODO cancel all module builds
  }
}
