package com.hubspot.blazar.util;

import com.google.common.base.Preconditions;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.github.GitHubProtos.Commit;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GitHub;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractModuleDiscovery implements ModuleDiscovery {

  @Inject
  Map<String, GitHub> gitHubByHost;

  @Override
  public boolean allowDuplicates() {
    return true;
  }

  protected String contentsFor(String file, GHRepository repository, GitInfo gitInfo) throws IOException {
    GHContent content = repository.getFileContent(file, gitInfo.getBranch());
    return content.getContent();
  }

  protected GHTree treeFor(GHRepository repository, GitInfo gitInfo) throws IOException {
    return repository.getTreeRecursive(gitInfo.getBranch(), 1);
  }

  protected GHRepository repositoryFor(GitInfo gitInfo) throws IOException {
    return gitHubFor(gitInfo).getRepository(gitInfo.getFullRepositoryName());
  }

  protected GitHub gitHubFor(GitInfo gitInfo) {
    String host = gitInfo.getHost();

    return Preconditions.checkNotNull(gitHubByHost.get(host), "No GitHub found for host " + host);
  }

  protected Set<String> affectedPaths(PushEvent pushEvent) {
    Set<String> affectedPaths = new HashSet<>();
    for (Commit commit : pushEvent.getCommitsList()) {
      affectedPaths.addAll(commit.getAddedList());
      affectedPaths.addAll(commit.getModifiedList());
      affectedPaths.addAll(commit.getRemovedList());
    }

    return affectedPaths;
  }
}
