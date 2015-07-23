package com.hubspot.blazar.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.inject.Inject;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.data.service.BuildDefinitionService;
import com.hubspot.blazar.github.GitHubProtos.Commit;
import com.hubspot.blazar.github.GitHubProtos.CreateEvent;
import com.hubspot.blazar.github.GitHubProtos.DeleteEvent;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;
import com.hubspot.blazar.github.GitHubProtos.Repository;
import io.dropwizard.setup.Environment;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;
import org.kohsuke.github.GitHub;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Path("/github/webhooks")
@Consumes(MediaType.APPLICATION_JSON)
public class GitHubWebHookResource {
  private final BuildDefinitionService buildDefinitionService;
  private final GitHub gitHub;
  private final ObjectMapper mapper;
  private final XmlMapper xmlMapper;

  @Inject
  public GitHubWebHookResource(BuildDefinitionService buildDefinitionService,
                               GitHub gitHub,
                               Environment environment,
                               XmlMapper xmlMapper) {
    this.buildDefinitionService = buildDefinitionService;
    this.gitHub = gitHub;
    this.mapper = environment.getObjectMapper();
    this.xmlMapper = xmlMapper;
  }

  @POST
  @Path("/create")
  public void processCreateEvent(CreateEvent createEvent) throws IOException {
    System.out.println(mapper.writeValueAsString(createEvent));
    if ("branch".equalsIgnoreCase(createEvent.getRefType())) {
      processBranch(gitInfo(createEvent));
    }
  }

  @POST
  @Path("/delete")
  public void processDeleteEvent(DeleteEvent deleteEvent) throws IOException {
    System.out.println(mapper.writeValueAsString(deleteEvent));
    if ("branch".equalsIgnoreCase(deleteEvent.getRefType())) {
      buildDefinitionService.setModules(gitInfo(deleteEvent), Collections.<Module>emptySet());
    }
  }

  @POST
  @Path("/push")
  public void processPushEvent(PushEvent pushEvent) throws IOException {
    System.out.println(mapper.writeValueAsString(pushEvent));
    updateBuilds(pushEvent);
    triggerBuilds(pushEvent);
  }

  @PUT
  @Path("/process")
  @Produces(MediaType.APPLICATION_JSON)
  public Set<Module> processBranch(GitInfo gitInfo) throws IOException {
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
      modules.add(new Module(artifactId, pom));
    }

    buildDefinitionService.setModules(gitInfo, modules);
    return modules;
  }

  private void updateBuilds(PushEvent pushEvent) throws IOException {
    boolean processBranch = false;
    for (String path : affectedPaths(pushEvent)) {
      if (isPom(path)) {
        processBranch = true;
      }
    }

    if (processBranch) {
      processBranch(gitInfo(pushEvent));
    }
  }

  private void triggerBuilds(PushEvent pushEvent) {
    GitInfo gitInfo = gitInfo(pushEvent);
    Set<Module> modules = buildDefinitionService.getModules(gitInfo);

    Set<Module> toBuild = new HashSet<>();
    for (String path : affectedPaths(pushEvent)) {
      for (Module module : modules) {
        if (module.contains(path)) {
          toBuild.add(module);
        }
      }
    }

    for (Module module : toBuild) {
      System.out.println("Going to build module: " + module.getName());
    }
  }

  private static boolean isPom(String path) {
    return "pom.xml".equals(path) || path.endsWith("/pom.xml");
  }

  private GitInfo gitInfo(CreateEvent createEvent) {
    return gitInfo(createEvent.getRepository(), createEvent.getRef());
  }

  private GitInfo gitInfo(DeleteEvent deleteEvent) {
    return gitInfo(deleteEvent.getRepository(), deleteEvent.getRef());
  }

  private GitInfo gitInfo(PushEvent pushEvent) {
    return gitInfo(pushEvent.getRepository(), pushEvent.getRef());
  }

  private GitInfo gitInfo(Repository repository, String ref) {
    String host = URI.create(repository.getUrl()).getHost();
    String fullName = repository.getFullName();
    String organization = fullName.substring(0, fullName.indexOf('/'));
    String repositoryName = fullName.substring(fullName.indexOf('/') + 1);
    String branch = ref.substring(ref.lastIndexOf('/') + 1);

    return new GitInfo(host, organization, repositoryName, branch);
  }

  private static Set<String> affectedPaths(PushEvent pushEvent) {
    Set<String> affectedPaths = new HashSet<>();
    for (Commit commit : pushEvent.getCommitsList()) {
      affectedPaths.addAll(commit.getAddedList());
      affectedPaths.addAll(commit.getModifiedList());
      affectedPaths.addAll(commit.getRemovedList());
    }

    return affectedPaths;
  }
}
