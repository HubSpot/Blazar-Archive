package com.hubspot.blazar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.hubspot.blazar.github.GitHubProtos.Commit;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;
import com.hubspot.blazar.github.GitHubProtos.Repository;
import io.dropwizard.setup.Environment;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;
import org.kohsuke.github.GitHub;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Path("/github/webhooks")
@Consumes(MediaType.APPLICATION_JSON)
public class GitHubWebHookResource {
  private final BuildService buildService;
  private final GitHub gitHub;
  private final ObjectMapper mapper;

  @Inject
  public GitHubWebHookResource(BuildService buildService, GitHub gitHub, Environment environment) {
    this.buildService = buildService;
    this.gitHub = gitHub;
    this.mapper = environment.getObjectMapper();
  }

  @POST
  @Path("/push")
  public void processWebhook(PushEvent pushEvent) throws IOException {
    System.out.println(mapper.writeValueAsString(pushEvent));
    updateBuilds(pushEvent);
    triggerBuilds(pushEvent);
  }

  @PUT
  @Path("/process")
  @Produces(MediaType.TEXT_PLAIN)
  public String processBranch(GitInfo gitInfo) throws IOException {
    GHRepository repository = gitHub.getRepository(gitInfo.getFullRepositoryName());
    GHTree tree = repository.getTreeRecursive(gitInfo.getBranch(), 1);

    Set<String> poms = new HashSet<>();
    for (GHTreeEntry entry : tree.getTree()) {
      if ("pom.xml".equals(entry.getPath()) || entry.getPath().endsWith("/pom.xml")) {
        poms.add(entry.getPath());
      }
    }

    if (poms.isEmpty()) {
      return "No POMs found";
    }

    Map<String, Integer> pomDepth = new HashMap<>();
    for (String pom : poms) {
      pomDepth.put(pom, pom.length() - pom.replace("/", "").length());
    }

    int minDepth = Collections.min(pomDepth.values());

    Set<String> rootPoms = new HashSet<>();
    for (Entry<String, Integer> entry : pomDepth.entrySet()) {
      if (entry.getValue() == minDepth) {
        rootPoms.add(entry.getKey());
      }
    }

    if (rootPoms.size() > 1) {
      Response response = Response.status(400)
          .type(MediaType.TEXT_PLAIN_TYPE)
          .entity("Unable to determine root POM for POMs: " + poms)
          .build();
      throw new WebApplicationException(response);
    }

    return rootPoms.iterator().next();
  }

  private void updateBuilds(PushEvent pushEvent) {
    for (String path : affectedPaths(pushEvent)) {
      if (path.endsWith("pom.xml")) {
        System.out.println("POM changed: " + path);
      }
    }
  }

  private void triggerBuilds(PushEvent pushEvent) {
    GitInfo gitInfo = gitInfo(pushEvent);
    Set<Module> modules = buildService.getModules(gitInfo);

    Set<Module> toBuild = new HashSet<>();
    for (String path : affectedPaths(pushEvent)) {
      for (Module module : modules) {
        if (path.startsWith(module.getPath())) {
          toBuild.add(module);
        }
      }
    }

    for (Module module : toBuild) {
      System.out.println("Going to build module: " + module.getName());
    }
  }

  private GitInfo gitInfo(PushEvent pushEvent) {
    Repository repository = pushEvent.getRepository();

    String host = URI.create(repository.getUrl()).getHost();
    String organization = repository.getOrganization();
    String repositoryName = repository.getName();
    String branch = pushEvent.getRef().substring(pushEvent.getRef().lastIndexOf('/') + 1);

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
