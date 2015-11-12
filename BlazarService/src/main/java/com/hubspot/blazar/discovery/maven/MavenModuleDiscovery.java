package com.hubspot.blazar.discovery.maven;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.google.common.base.Optional;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class MavenModuleDiscovery extends AbstractModuleDiscovery {
  private static final Logger LOG = LoggerFactory.getLogger(MavenModuleDiscovery.class);

  private static final Optional<GitInfo> STANDARD_BUILDPACK =
      Optional.of(GitInfo.fromString("git.hubteam.com/paas/Blazar-Buildpack-Java#publish"));
  private static final Optional<GitInfo> DEPLOYABLE_BUILDPACK =
      Optional.of(GitInfo.fromString("git.hubteam.com/paas/Blazar-Buildpack-Java#deployable"));
  private static final String EXECUTABLE_MARKER = ".build-executable";

  private final ObjectMapper objectMapper;
  private final XmlFactory xmlFactory;

  @Inject
  public MavenModuleDiscovery(ObjectMapper objectMapper, XmlFactory xmlFactory) {
    this.objectMapper = objectMapper;
    this.xmlFactory = xmlFactory;
  }

  @Override
  public boolean shouldRediscover(GitInfo gitInfo, PushEvent pushEvent) {
    for (String path : affectedPaths(pushEvent)) {
      if (isPom(path) || isExecutableMarker(path)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Set<DiscoveredModule> discover(GitInfo gitInfo) throws IOException {
    GHRepository repository = repositoryFor(gitInfo);
    GHTree tree = treeFor(repository, gitInfo);

    Set<String> poms = new HashSet<>();
    for (GHTreeEntry entry : tree.getTree()) {
      if (isPom(entry.getPath())) {
        poms.add(entry.getPath());
      }
    }

    Set<DiscoveredModule> modules = new HashSet<>();
    for (String path : poms) {
      final ProjectObjectModel pom;

      try {
        JsonParser parser = xmlFactory.createParser(contentsFor(path, repository, gitInfo));
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

      Optional<GitInfo> buildpack = buildpackFor(path, repository, gitInfo);
      modules.add(new DiscoveredModule(pom.getArtifactId(), "maven", path, glob, buildpack, pom.getDependencyInfo()));
    }

    return modules;
  }

  private Optional<GitInfo> buildpackFor(String file, GHRepository repository, GitInfo gitInfo) throws IOException {
    if (isDeployable(file, repository, gitInfo)) {
      return DEPLOYABLE_BUILDPACK;
    } else {
      return STANDARD_BUILDPACK;
    }
  }

  private boolean isDeployable(String file, GHRepository repository, GitInfo gitInfo) throws IOException {
    String folder = file.contains("/") ? file.substring(0, file.lastIndexOf('/') + 1) : "";
    try {
      contentsFor(folder + EXECUTABLE_MARKER, repository, gitInfo);
      return true;
    } catch (FileNotFoundException e) {
      return false;
    }
  }

  private static boolean isPom(String path) {
    return "pom.xml".equals(path) || path.endsWith("/pom.xml");
  }

  private static boolean isExecutableMarker(String path) {
    return EXECUTABLE_MARKER.equals(path) || path.endsWith("/" + EXECUTABLE_MARKER);
  }
}
