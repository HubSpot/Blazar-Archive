package com.hubspot.blazar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.hubspot.blazar.github.GitHubProtos.Commit;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;
import com.hubspot.blazar.github.GitHubProtos.Repository;
import io.dropwizard.setup.Environment;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@Path("/github/webhooks")
@Consumes(MediaType.APPLICATION_JSON)
public class GitHubWebHookResource {
  private final BuildService buildService;
  private final ObjectMapper mapper;

  @Inject
  public GitHubWebHookResource(BuildService buildService, Environment environment) {
    this.buildService = buildService;
    this.mapper = environment.getObjectMapper();
  }

  @POST
  @Path("/push")
  public void processWebhook(PushEvent pushEvent) throws IOException {
    System.out.println(mapper.writeValueAsString(pushEvent));
    updateBuilds(pushEvent);
    triggerBuilds(pushEvent);
  }

  private void updateBuilds(PushEvent pushEvent) {
    for (String path : affectedPaths(pushEvent)) {
      if (path.endsWith("pom.xml")) {
        System.out.println("POM changed: " + path);
      }
    }
  }

  private void triggerBuilds(PushEvent pushEvent) {
    Repository repository = pushEvent.getRepository();

    String host = URI.create(repository.getUrl()).getHost();
    String organization = repository.getOrganization();
    String repositoryName = repository.getName();
    String branch = pushEvent.getRef().substring(pushEvent.getRef().lastIndexOf('/') + 1);

    GitInfo gitInfo = new GitInfo(host, organization, repositoryName, branch);
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
