package com.hubspot.blazar.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.net.UrlEscapers;
import com.google.inject.Inject;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.github.GitHubProtos.Commit;
import com.hubspot.blazar.github.GitHubProtos.CreateEvent;
import com.hubspot.blazar.github.GitHubProtos.DeleteEvent;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;
import com.hubspot.blazar.github.GitHubProtos.Repository;
import com.hubspot.blazar.util.ModuleDiscovery;
import io.dropwizard.setup.Environment;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@Path("/github/webhooks")
@Consumes(MediaType.APPLICATION_JSON)
public class GitHubWebHookResource {
  private final BranchService branchService;
  private final ModuleService moduleService;
  private final ModuleDiscovery moduleDiscovery;
  private final ObjectMapper mapper;

  @Inject
  public GitHubWebHookResource(BranchService branchService,
                               ModuleService moduleService,
                               ModuleDiscovery moduleDiscovery,
                               Environment environment) {
    this.branchService = branchService;
    this.moduleService = moduleService;
    this.moduleDiscovery = moduleDiscovery;
    this.mapper = environment.getObjectMapper();
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
      branchService.delete(gitInfo(deleteEvent));
    }
  }

  @POST
  @Path("/push")
  public void processPushEvent(PushEvent pushEvent) throws IOException {
    System.out.println(mapper.writeValueAsString(pushEvent));
    if (!pushEvent.getRef().startsWith("refs/tags/")) {
      GitInfo gitInfo = branchService.upsert(gitInfo(pushEvent));

      Set<Module> modules = updateModules(gitInfo, pushEvent);
      triggerBuilds(pushEvent, modules);
    }
  }

  @PUT
  @Path("/process")
  @Produces(MediaType.APPLICATION_JSON)
  public Set<Module> processBranch(GitInfo gitInfo) throws IOException {
    gitInfo = branchService.upsert(gitInfo);

    Set<Module> modules = moduleDiscovery.discover(gitInfo);
    return moduleService.setModules(gitInfo, modules);
  }

  private Set<Module> updateModules(GitInfo gitInfo, PushEvent pushEvent) throws IOException {
    for (String path : affectedPaths(pushEvent)) {
      if (isPom(path)) {
        return moduleService.setModules(gitInfo, moduleDiscovery.discover(gitInfo));
      }
    }

    return moduleService.getModules(gitInfo);
  }

  private void triggerBuilds(PushEvent pushEvent, Set<Module> modules) {
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
    return gitInfo(createEvent.getRepository(), createEvent.getRef(), true);
  }

  private GitInfo gitInfo(DeleteEvent deleteEvent) {
    return gitInfo(deleteEvent.getRepository(), deleteEvent.getRef(), false);
  }

  private GitInfo gitInfo(PushEvent pushEvent) {
    return gitInfo(pushEvent.getRepository(), pushEvent.getRef(), true);
  }

  private GitInfo gitInfo(Repository repository, String ref, boolean active) {
    String host = URI.create(repository.getUrl()).getHost();
    String fullName = repository.getFullName();
    String organization = fullName.substring(0, fullName.indexOf('/'));
    String repositoryName = fullName.substring(fullName.indexOf('/') + 1);
    int repositoryId = repository.getId();
    String branch = ref.startsWith("refs/heads/") ? ref.substring("refs/heads/".length()) : ref;
    String escapedBranch = UrlEscapers.urlPathSegmentEscaper().escape(branch);

    return new GitInfo(Optional.<Long>absent(), host, organization, repositoryName, repositoryId, escapedBranch, active);
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
