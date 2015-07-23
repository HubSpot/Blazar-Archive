package com.hubspot.blazar;

import com.google.inject.Inject;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;

import java.util.Collections;
import java.util.Set;

public class BuildService {

  @Inject
  public BuildService() {}

  public Set<Module> getModules(GitInfo gitInfo) {
    return Collections.emptySet();
  }

  public void setModules(GitInfo gitInfo, Set<Module> modules) {

  }
}
