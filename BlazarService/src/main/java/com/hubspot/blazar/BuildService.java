package com.hubspot.blazar;

import com.google.inject.Inject;

import java.util.Collections;
import java.util.Set;

public class BuildService {

  @Inject
  public BuildService() {}

  public Set<Module> getModules(GitInfo gitInfo) {
    return Collections.emptySet();
  }
}
