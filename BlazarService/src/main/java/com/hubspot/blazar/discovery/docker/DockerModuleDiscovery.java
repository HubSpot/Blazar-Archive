package com.hubspot.blazar.discovery.docker;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hubspot.blazar.base.DependencyInfo;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.BuildpackConfiguration;
import com.hubspot.blazar.discovery.AbstractModuleDiscovery;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;
import com.google.common.base.Optional;

@Singleton
public class DockerModuleDiscovery extends AbstractModuleDiscovery {
  public static final String NAME = "docker";

  private static final Logger LOG = LoggerFactory.getLogger(DockerModuleDiscovery.class);

  private final Optional<BuildpackConfiguration> buildpackConfiguration;

  @Inject
  public DockerModuleDiscovery(BlazarConfiguration configuration) {
    buildpackConfiguration = Optional.fromNullable(configuration.getModuleBuildpackConfiguration().get(NAME));
  }

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
    if (!buildpackConfiguration.isPresent()) {
      return Optional.absent();
    }
    if (buildpackConfiguration.get().getRepoBuildpack().containsKey(gitInfo)) {
      LOG.info("Picked repo-specific buildpack {} for {}", buildpackConfiguration.get().getRepoBuildpack().get(gitInfo), String.format("%s-%s", gitInfo.getFullRepositoryName(), file));
      return Optional.of(buildpackConfiguration.get().getRepoBuildpack().get(gitInfo));
    } else if ("master".equals(gitInfo.getBranch())) {
      LOG.info("Picked master buildpack {} for {}", buildpackConfiguration.get().getDefaultBuildpack(), String.format("%s-%s", gitInfo.getFullRepositoryName(), file));
      return buildpackConfiguration.get().getDefaultBuildpack();
    } else {
      final Optional<GitInfo> branchBuildpack = Optional.fromNullable(buildpackConfiguration.get().getBranchBuildpack().get(gitInfo.getBranch()));
      LOG.info("Picked branch buildpack {} for {}", branchBuildpack, String.format("%s-%s", gitInfo.getFullRepositoryName(), file));
      return branchBuildpack;
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
