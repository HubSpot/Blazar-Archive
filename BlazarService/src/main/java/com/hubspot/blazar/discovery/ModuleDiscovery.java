package com.hubspot.blazar.discovery;

import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.GitInfo;

import java.io.IOException;
import java.util.Set;

public interface ModuleDiscovery {
  boolean shouldRediscover(GitInfo gitInfo, CommitInfo commitInfo) throws IOException;
  Set<DiscoveredModule> discover(GitInfo gitInfo) throws IOException;
}
