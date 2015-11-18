package com.hubspot.blazar.discovery.maven;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.google.common.base.Optional;
import com.hubspot.blazar.base.CommitInfo;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.discovery.ModuleDiscovery;
import com.hubspot.blazar.util.GitHubHelper;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class MavenModuleDiscovery implements ModuleDiscovery {
  private static final Logger LOG = LoggerFactory.getLogger(MavenModuleDiscovery.class);

  private static final Optional<GitInfo> STANDARD_BUILDPACK =
      Optional.of(GitInfo.fromString("git.hubteam.com/paas/Blazar-Buildpack-Java#publish"));
  private static final Optional<GitInfo> DEPLOYABLE_BUILDPACK =
      Optional.of(GitInfo.fromString("git.hubteam.com/paas/Blazar-Buildpack-Java#deployable"));
  private static final String EXECUTABLE_MARKER = ".build-executable";

  private final GitHubHelper gitHubHelper;
  private final ObjectMapper objectMapper;
  private final XmlFactory xmlFactory;

  @Inject
  public MavenModuleDiscovery(GitHubHelper gitHubHelper, ObjectMapper objectMapper, XmlFactory xmlFactory) {
    this.gitHubHelper = gitHubHelper;
    this.objectMapper = objectMapper;
    this.xmlFactory = xmlFactory;
  }

  @Override
  public boolean shouldRediscover(GitInfo gitInfo, CommitInfo commitInfo) {
    for (String path : gitHubHelper.affectedPaths(commitInfo)) {
      if (isPom(path) || isExecutableMarker(path)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Set<DiscoveredModule> discover(GitInfo gitInfo) throws IOException {
    GHRepository repository = gitHubHelper.repositoryFor(gitInfo);
    GHTree tree = gitHubHelper.treeFor(repository, gitInfo);

    Set<String> allPaths = new HashSet<>();
    Set<String> poms = new HashSet<>();
    for (GHTreeEntry entry : tree.getTree()) {
      allPaths.add(entry.getPath());

      if (isPom(entry.getPath())) {
        poms.add(entry.getPath());
      }
    }

    Set<DiscoveredModule> modules = new HashSet<>();
    for (String path : poms) {
      final ProjectObjectModel pom;

      try {
        JsonParser parser = xmlFactory.createParser(gitHubHelper.contentsFor(path, repository, gitInfo));
        pom = objectMapper.readValue(parser, ProjectObjectModel.class);
      } catch (IOException e) {
        LOG.error("Error parsing POM at path {} for repo {}@{}", path, gitInfo.getFullRepositoryName(), gitInfo.getBranch());
        continue;
      }

      final String glob;
      if ("pom".equals(pom.getPackaging())) {
        glob = path;
      } else {
        glob = (path.contains("/") ? path.substring(0, path.lastIndexOf('/') + 1) : "") + "**";
      }

      Optional<GitInfo> buildpack = buildpackFor(path, allPaths);
      modules.add(new DiscoveredModule(pom.getArtifactId(), "maven", path, glob, buildpack, pom.getDependencyInfo()));
    }

    return modules;
  }

  private Optional<GitInfo> buildpackFor(String file, Set<String> allFiles) throws IOException {
    if (isDeployable(file, allFiles)) {
      return DEPLOYABLE_BUILDPACK;
    } else {
      return STANDARD_BUILDPACK;
    }
  }

  private boolean isDeployable(String file, Set<String> allFiles) throws IOException {
    String folder = file.contains("/") ? file.substring(0, file.lastIndexOf('/') + 1) : "";
    return allFiles.contains(folder + EXECUTABLE_MARKER);
  }

  private static boolean isPom(String path) {
    return "pom.xml".equals(path) || path.endsWith("/pom.xml");
  }

  private static boolean isExecutableMarker(String path) {
    return EXECUTABLE_MARKER.equals(path) || path.endsWith("/" + EXECUTABLE_MARKER);
  }
}
