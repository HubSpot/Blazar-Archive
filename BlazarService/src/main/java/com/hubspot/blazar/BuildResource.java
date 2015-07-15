package com.hubspot.blazar;

import com.google.common.base.Optional;
import com.google.inject.Inject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static com.hubspot.blazar.BuildState.Result.IN_PROGRESS;

@Path("/builds")
@Produces(MediaType.APPLICATION_JSON)
public class BuildResource {

  @Inject
  public BuildResource() {}

  @GET
  public Set<ModuleBuildWithState> test() {
    Set<ModuleBuildWithState> builds = new HashSet<>();

    builds.add(build("Contacts", "ContactsHadoop"));
    builds.add(build("HubSpotConnect"));
    builds.add(build("guice-jdbi"));
    builds.add(build("dropwizard-hubspot"));
    builds.add(build("Wormhole"));
    builds.add(build("Email", "EmailApiClient"));
    builds.add(build("TaskService", "TaskServiceWorker"));
    builds.add(build("InternalEmail", "InternalEmailJobs"));
    builds.add(build("DeployService", "DeployServiceData"));
    builds.add(build("HubSpotConfig"));

    return builds;
  }

  private static ModuleBuildWithState build(String repoName) {
    return new ModuleBuildWithState(gitInfo(repoName), new Module(repoName, "."), buildState());
  }

  private static ModuleBuildWithState build(String repoName, String moduleName) {
    return new ModuleBuildWithState(gitInfo(repoName), new Module(moduleName, moduleName), buildState());
  }

  private static GitInfo gitInfo(String repository) {
    return new GitInfo("git.hubteam.com", "HubSpot", repository, "master");
  }

  private static BuildState buildState() {
    Random r = ThreadLocalRandom.current();
    int buildNumber = r.nextInt(1000) + 1;
    long startTime = System.currentTimeMillis() - 30000 - r.nextInt(20000);
    BuildState.Result result = BuildState.Result.values()[r.nextInt(BuildState.Result.values().length)];
    Optional<Long> endTime = result == IN_PROGRESS ? Optional.<Long>absent() : Optional.of(startTime + r.nextInt(30000));

    return new BuildState(buildNumber, "abc", result, startTime, endTime);
  }
}
