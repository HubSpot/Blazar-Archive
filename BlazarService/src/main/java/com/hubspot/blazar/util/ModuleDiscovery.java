package com.hubspot.blazar.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ModuleDiscovery {
  private final GitHub gitHub;
  private final XmlMapper xmlMapper;

  @Inject
  public ModuleDiscovery(GitHub gitHub, XmlMapper xmlMapper) {
    this.gitHub = gitHub;
    this.xmlMapper = xmlMapper;
  }

  public Set<Module> discover(GitInfo gitInfo) throws IOException {
    GHRepository repository = gitHub.getRepository(gitInfo.getFullRepositoryName());
    GHTree tree = repository.getTreeRecursive(gitInfo.getBranch(), 1);

    Set<String> poms = new HashSet<>();
    for (GHTreeEntry entry : tree.getTree()) {
      if (isPom(entry.getPath())) {
        poms.add(entry.getPath());
      }
    }

    Set<Module> modules = new HashSet<>();
    for (String pom : poms) {
      GHContent content = repository.getFileContent(pom, gitInfo.getBranch());
      JsonNode node = xmlMapper.readTree(content.getContent());
      String artifactId = node.get("artifactId").textValue();
      modules.add(new Module(Optional.<Long>absent(), artifactId, pom, true));
    }

    return modules;
  }

  private static boolean isPom(String path) {
    return "pom.xml".equals(path) || path.endsWith("/pom.xml");
  }
}
