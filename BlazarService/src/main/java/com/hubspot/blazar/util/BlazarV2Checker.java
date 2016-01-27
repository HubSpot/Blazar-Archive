package com.hubspot.blazar.util;

import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.discovery.AbstractModuleDiscovery;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;
import org.kohsuke.github.GHRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

@Singleton
public class BlazarV2Checker extends AbstractModuleDiscovery {

  @Inject
  public BlazarV2Checker() {}

  public boolean isBlazarV2(GitInfo gitInfo) throws IOException {
    GHRepository repository = repositoryFor(gitInfo);
    try {
      return contentsFor(".blazar.yaml", repository, gitInfo).contains("enabled: true");
    } catch (FileNotFoundException e) {
      return false;
    }
  }

  @Override
  public boolean shouldRediscover(GitInfo gitInfo, PushEvent pushEvent) throws IOException {
    return false;
  }

  @Override
  public Set<DiscoveredModule> discover(GitInfo gitInfo) throws IOException {
    return Collections.emptySet();
  }
}
