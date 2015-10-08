package com.hubspot.blazar.discovery.docker;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.DependencyInfo;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.discovery.AbstractModuleDiscovery;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class DockerModuleDiscovery extends AbstractModuleDiscovery {

  private static final Logger LOG = LoggerFactory.getLogger(DockerModuleDiscovery.class);
  private static final Optional<GitInfo> BRANCH_BUILDPACK =
      Optional.of(GitInfo.fromString("git.hubteam.com/paas/Blazar-Buildpack-Docker#stable"));
  private static final Optional<GitInfo> MASTER_BUILDPACK =
      Optional.of(GitInfo.fromString("git.hubteam.com/paas/Blazar-Buildpack-Docker#publish"));

  @Inject
  public DockerModuleDiscovery() {}

  @Override
  public boolean shouldRediscover(GitInfo gitInfo, PushEvent pushEvent) throws IOException {
    for (String path : affectedPaths(pushEvent)) {
      if (isDockerConfig(path)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Set<DiscoveredModule> discover(GitInfo gitInfo) throws IOException {
    GHRepository repository = repositoryFor(gitInfo);
    GHTree tree = treeFor(repository, gitInfo);

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
      modules.add(new DiscoveredModule(moduleName, dockerFile, glob, buildpackFor(dockerFile, gitInfo), getDockerfileDeps()));
    }
    return modules;
  }

  private DependencyInfo getDockerfileDeps() throws IOException {
    // todo Currently not supporting deps because most builds are not in the same repository and deps builds are limited to repo committed to
    Set<String> emtpySet = Collections.emptySet();
    return new DependencyInfo(emtpySet, emtpySet);
  }

  private Optional<GitInfo> buildpackFor(String file, GitInfo gitInfo) throws IOException {
    if ("master".equals(gitInfo.getBranch())) {
      LOG.info("Picked master buildpack {} for {}", MASTER_BUILDPACK, String.format("%s-%s", gitInfo.getFullRepositoryName(), file));
      return MASTER_BUILDPACK;
    } else {
      LOG.info("Picked branch buildpack {} for {}", BRANCH_BUILDPACK, String.format("%s-%s", gitInfo.getFullRepositoryName(), file));
      return BRANCH_BUILDPACK;
    }
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
