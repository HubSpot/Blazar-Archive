package com.hubspot.blazar.discovery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.net.UrlEscapers;
import com.hubspot.blazar.base.BuildConfig;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.github.GitHubProtos.Commit;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GitHub;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractModuleDiscovery implements ModuleDiscovery {

  @Inject
  Map<String, GitHub> gitHubByHost;

  @Inject
  ObjectMapper mapper;

  @Inject
  YAMLFactory yamlFactory;

  protected Optional<BuildConfig> configFor(String path, GHRepository repository, GitInfo gitInfo) throws IOException {
    String configPath = (path.contains("/") ? path.substring(0, path.lastIndexOf('/') + 1) : "") + ".blazar.yaml";
    final String config;
    try {
      config = contentsFor(configPath, repository, gitInfo);
    } catch (FileNotFoundException e) {
      return Optional.absent();
    }

    try {
      return Optional.of(mapper.readValue(yamlFactory.createParser(config), BuildConfig.class));
    } catch (IOException e) {
      return Optional.absent();
    }
  }

  protected String contentsFor(String file, GHRepository repository, GitInfo gitInfo) throws IOException {
    GHContent content = repository.getFileContent(file, gitInfo.getBranch());
    return content.getContent();
  }

  protected GHTree treeFor(GHRepository repository, GitInfo gitInfo) throws IOException {
    String escapedBranch = UrlEscapers.urlPathSegmentEscaper().escape(gitInfo.getBranch());
    return repository.getTreeRecursive(escapedBranch, 1);
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
