package com.hubspot.blazar.discovery;

import java.io.IOException;

import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.DiscoveryResult;
import com.hubspot.blazar.base.GitInfo;

public interface ModuleDiscovery {
  boolean shouldRediscover(GitInfo gitInfo, CommitInfo commitInfo) throws IOException;
  DiscoveryResult discover(GitInfo gitInfo) throws IOException;
  boolean isEnabled(GitInfo gitInfo);
}
