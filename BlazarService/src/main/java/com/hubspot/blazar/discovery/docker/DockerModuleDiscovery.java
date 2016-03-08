package com.hubspot.blazar.discovery.docker;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hubspot.blazar.base.DiscoveryResult;
import com.hubspot.blazar.base.MalformedFile;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.DependencyInfo;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.discovery.ModuleDiscovery;
import com.hubspot.blazar.util.GitHubHelper;

@Singleton
public class DockerModuleDiscovery implements ModuleDiscovery {
  private static final Optional<GitInfo> MASTER_BUILDPACK =
      Optional.of(GitInfo.fromString("git.hubteam.com/HubSpot/Blazar-Buildpack-Docker#master"));

  private final GitHubHelper gitHubHelper;

  @Inject
  public DockerModuleDiscovery(GitHubHelper gitHubHelper) {
    this.gitHubHelper = gitHubHelper;
  }

  @Override
  public boolean shouldRediscover(GitInfo gitInfo, CommitInfo commitInfo) throws IOException {
    for (String path : gitHubHelper.affectedPaths(commitInfo)) {
      if (isDockerConfig(path)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public DiscoveryResult discover(GitInfo gitInfo) throws IOException {
    GHRepository repository = gitHubHelper.repositoryFor(gitInfo);
    GHTree tree = gitHubHelper.treeFor(repository, gitInfo);

    Set<String> dockerFiles = new HashSet<>();
    for (GHTreeEntry entry : tree.getTree()) {
      if (isDockerConfig(entry.getPath())) {
        dockerFiles.add(entry.getPath());
      }
    }

    Set<DiscoveredModule> modules = new HashSet<>();
    for (String dockerFile: dockerFiles) {
      String moduleName = moduleName(gitInfo, dockerFile);
      String glob = (dockerFile.contains("/") ? dockerFile.substring(0, dockerFile.lastIndexOf('/') + 1) : "") + "**";
      modules.add(new DiscoveredModule(moduleName, "docker", dockerFile, glob,  MASTER_BUILDPACK, getDockerfileDeps()));
    }
    return new DiscoveryResult(modules, Collections.<MalformedFile>emptySet());
  }

  private DependencyInfo getDockerfileDeps() throws IOException {
    // todo Currently not supporting deps because most builds are not in the same repository and deps builds are limited to repo committed to
    Set<String> emtpySet = Collections.emptySet();
    return new DependencyInfo(emtpySet, emtpySet);
  }

  private static String moduleName(GitInfo gitInfo, String path) {
    return path.contains("/") ? folderName(path) : gitInfo.getRepository();
  }

  private static String folderName(String path) {
    String folderPath = path.substring(0, path.lastIndexOf('/'));
    return folderPath.contains("/") ? folderPath.substring(folderPath.lastIndexOf('/') + 1) : folderPath;
  }

  private static boolean isDockerConfig(String path) {
    return "Dockerfile".equals(path) || path.endsWith("/Dockerfile");
  }
}
