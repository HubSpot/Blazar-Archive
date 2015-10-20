package com.hubspot.blazar.discovery.maven;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.google.common.base.Optional;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.BuildpackConfiguration;
import com.hubspot.blazar.discovery.AbstractModuleDiscovery;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;

@Singleton
public class MavenModuleDiscovery extends AbstractModuleDiscovery {
  public static final String NAME = "maven";

  private static final Logger LOG = LoggerFactory.getLogger(MavenModuleDiscovery.class);

  private final XmlFactory xmlFactory;
  private final Optional<BuildpackConfiguration> buildpackConfiguration;

  @Inject
  public MavenModuleDiscovery(XmlFactory xmlFactory, BlazarConfiguration configuration) {
    this.xmlFactory = xmlFactory;
    this.buildpackConfiguration = Optional.fromNullable(configuration.getModuleBuildpackConfiguration().get(NAME));
  }

  @Override
  public boolean shouldRediscover(GitInfo gitInfo, PushEvent pushEvent) {
    for (String path : affectedPaths(pushEvent)) {
      if (isPom(path)) {
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
        pom = mapper.readValue(parser, ProjectObjectModel.class);
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
      modules.add(new DiscoveredModule(pom.getArtifactId(), path, glob, buildpack, pom.getDependencyInfo()));
    }

    return modules;
  }

  private Optional<GitInfo> buildpackFor(String file, GHRepository repository, GitInfo gitInfo) throws IOException {
    if (!buildpackConfiguration.isPresent()) {
      return Optional.absent();
    }

    if (buildpackConfiguration.get().getRepoBuildpack().containsKey(gitInfo)) {
      return Optional.of(buildpackConfiguration.get().getRepoBuildpack().get(gitInfo));
    } else if (!"master".equals(gitInfo.getBranch())) {
      return Optional.fromNullable(buildpackConfiguration.get().getBranchBuildpack().get(gitInfo.getBranch()));
    } else if (isDeployable(file, repository, gitInfo)) {
      return buildpackConfiguration.get().getDeployableBuildpack();
    } else {
      return buildpackConfiguration.get().getDefaultBuildpack();
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

  private static boolean isPom(String path) {
    return "pom.xml".equals(path) || path.endsWith("/pom.xml");
  }
}
