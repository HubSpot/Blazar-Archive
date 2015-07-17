package com.hubspot.blazar;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.jackson.jaxrs.PropertyFiltering;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static com.hubspot.blazar.BuildState.Result.IN_PROGRESS;

@Path("/builds")
@Produces(MediaType.APPLICATION_JSON)
public class BuildResource {
  private static final Map<String, ModuleBuildWithState> BUILD_MAP = initialBuilds();

  @Inject
  public BuildResource() {}

  @GET
  @PropertyFiltering
  public synchronized Collection<ModuleBuildWithState> test() {
    updateBuildMap();

    return BUILD_MAP.values();
  }

  private static void updateBuildMap() {
    for (Entry<String, ModuleBuildWithState> entry : BUILD_MAP.entrySet()) {
      ModuleBuildWithState build = entry.getValue();
      while (build.getBuildState().getStartTime() + 120000 < System.currentTimeMillis()) {
        build = updateBuild(build);
      }

      entry.setValue(build);
    }
  }

  private static ModuleBuildWithState updateBuild(ModuleBuildWithState previous) {
    if (previous.getBuildState().getResult() == IN_PROGRESS) {
      long buildDuration = 15000 + ThreadLocalRandom.current().nextInt(15000);
      if (System.currentTimeMillis() > previous.getBuildState().getStartTime() + buildDuration) {
        BuildState p = previous.getBuildState();
        Optional<Long> endTime = Optional.of(p.getStartTime() + buildDuration);
        BuildState newBuildState = new BuildState(p.getBuildNumber(), p.getCommitSha(), randomCompletedResult(), p.getStartTime(), endTime);
        return new ModuleBuildWithState(previous.getGitInfo(), previous.getModule(), newBuildState);
      } else {
        return previous;
      }
    } else {
      long sleepDuration = 15000 + ThreadLocalRandom.current().nextInt(15000);
      if (System.currentTimeMillis() > previous.getBuildState().getEndTime().get() + sleepDuration) {
        BuildState p = previous.getBuildState();
        long startTime = p.getEndTime().get() + sleepDuration;
        BuildState newBuildState = new BuildState(p.getBuildNumber() + 1, p.getCommitSha(), IN_PROGRESS, startTime, Optional.<Long>absent());
        return new ModuleBuildWithState(previous.getGitInfo(), previous.getModule(), newBuildState);
      } else {
        return previous;
      }
    }
  }

  private static Map<String, ModuleBuildWithState> initialBuilds() {
    List<ModuleBuildWithState> builds = new ArrayList<>();

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

    Map<String, ModuleBuildWithState> buildMap = new ConcurrentHashMap<>();

    for (ModuleBuildWithState build : builds) {
      buildMap.put(build.getGitInfo().getRepository(), build);
    }

    return buildMap;
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

  private static BuildState.Result randomCompletedResult() {
    BuildState.Result result = BuildState.Result.values()[ThreadLocalRandom.current().nextInt(BuildState.Result.values().length)];
    return result == IN_PROGRESS ? randomCompletedResult() : result;
  }
}
