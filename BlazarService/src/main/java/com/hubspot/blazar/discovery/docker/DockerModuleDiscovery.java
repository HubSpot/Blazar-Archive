package com.hubspot.blazar.discovery.docker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.DependencyInfo;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.discovery.AbstractModuleDiscovery;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;

@Singleton
public class DockerModuleDiscovery extends AbstractModuleDiscovery {

  private static final Logger LOG = LoggerFactory.getLogger(DockerModuleDiscovery.class);
  private static final Optional<GitInfo> BRANCH_BUILDPACK =
      Optional.of(GitInfo.fromString("git.hubteam.com/paas/Blazar-Buildpack-Docker#stable"));
  private static final Optional<GitInfo> MASTER_BUILDPACK =
      Optional.of(GitInfo.fromString("git.hubteam.com/paas/Blazar-Buildpack-Docker#publish"));
  private static final Optional<GitInfo> DEPLOYABLE_BUILDPACK =
      Optional.of(GitInfo.fromString("git.hubteam.com/paas/Blazar-Buildpack-Docker#deployable"));

  @Inject
  public DockerModuleDiscovery() {}

  @Override
  public boolean allowDuplicates() {
    return false;
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
      DependencyInfo dependencyInfo = getDockerfileDeps(dockerFile, repository, gitInfo, moduleName);
      modules.add(new DiscoveredModule(moduleName, dockerFile, glob, buildpackFor(dockerFile, repository, gitInfo), dependencyInfo));
    }
    return modules;
  }

  private DependencyInfo getDockerfileDeps(String path, GHRepository repository, GitInfo gitInfo, String moduleName) throws IOException {
    GHContent fileContents = repository.getFileContent(path, gitInfo.getBranch());
    String content = fileContents.getContent();
    HashSet<String> emtpySet = new HashSet<>();
    if (!content.startsWith("FROM: ")) {
      return new DependencyInfo(emtpySet, emtpySet);
    }
    String firstLine = content.split("\\n")[0];
    String dockerImageString = firstLine.substring(firstLine.indexOf("FROM: "));
    DockerImage image = DockerImage.parseFromImageName(dockerImageString);

    HashSet<String> depends = new HashSet<>();
    depends.add(String.format("docker-%s", image.getImage()));

    HashSet<String> provides = new HashSet<>();
    provides.add(String.format("docker-%s", moduleName));

    return new DependencyInfo(depends, provides);
  }

  private Optional<GitInfo> buildpackFor(String file, GHRepository repository, GitInfo gitInfo) throws IOException {
    if (!"master".equals(gitInfo.getBranch())) {
      LOG.info("Picked branch buildpack {} for {}", BRANCH_BUILDPACK, String.format("%s-%s", gitInfo.getFullRepositoryName(), file));
      return BRANCH_BUILDPACK;
    } else if (isDeployable(file, repository, gitInfo)) {
      LOG.info("Picked deployable buildpack {} for {}", DEPLOYABLE_BUILDPACK, String.format("%s-%s", gitInfo.getFullRepositoryName(), file));
      return DEPLOYABLE_BUILDPACK;
    } else {
      LOG.info("Picked master buildpack {} for {}", MASTER_BUILDPACK, String.format("%s-%s", gitInfo.getFullRepositoryName(), file));
      return MASTER_BUILDPACK;
    }
  }

  private boolean isDeployable(String file, GHRepository repository, GitInfo gitInfo) throws IOException {
    String folder = file.contains("/") ? file.substring(0, file.lastIndexOf('/') + 1) : "";
    try {
      contentsFor(folder + ".build-executable", repository, gitInfo);
      return true;
    } catch (FileNotFoundException e) {
      return false;
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
