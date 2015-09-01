package com.hubspot.blazar.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Optional;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class MavenModuleDiscovery extends AbstractModuleDiscovery {
  private final XmlMapper xmlMapper;

  @Inject
  public MavenModuleDiscovery(XmlMapper xmlMapper) {
    this.xmlMapper = xmlMapper;
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
  public Set<Module> discover(GitInfo gitInfo) throws IOException {
    GHRepository repository = repositoryFor(gitInfo);
    GHTree tree = treeFor(repository, gitInfo);

    Set<String> poms = new HashSet<>();
    for (GHTreeEntry entry : tree.getTree()) {
      if (isPom(entry.getPath())) {
        poms.add(entry.getPath());
      }
    }

    Set<Module> modules = new HashSet<>();
    for (String pom : poms) {
      JsonNode node = xmlMapper.readTree(contentsFor(pom, repository, gitInfo));
      String artifactId = node.get("artifactId").textValue();
      final String glob;
      if ("pom".equals(node.path("packaging").textValue())) {
        glob = pom;
      } else {
        glob = (pom.contains("/") ? pom.substring(0, pom.lastIndexOf('/') + 1) : "") + "**";
      }
      modules.add(new Module(artifactId, pom, glob));
    }

    return modules;
  }

  private static boolean isPom(String path) {
    return "pom.xml".equals(path) || path.endsWith("/pom.xml");
  }
}
