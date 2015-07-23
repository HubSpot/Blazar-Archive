package com.hubspot.blazar.resources;

import com.google.common.base.Optional;
import com.google.common.primitives.Longs;
import com.google.inject.Inject;
import com.hubspot.blazar.base.BuildState;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuildWithState;
import com.hubspot.jackson.jaxrs.PropertyFiltering;
import com.sun.jersey.api.NotFoundException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static com.hubspot.blazar.base.BuildState.Result.IN_PROGRESS;

@Path("/builds")
@Produces(MediaType.APPLICATION_JSON)
public class BuildResource {
  private static final Map<ModuleBuild, ModuleBuildWithState> BUILD_MAP = initialBuilds();

  @Inject
  public BuildResource() {}

  @GET
  @PropertyFiltering
  public synchronized Collection<ModuleBuildWithState> getAllBuildStates() {
    updateBuildMap();

    return BUILD_MAP.values();
  }

  @GET
  @PropertyFiltering
  @Path("/{host}/{organization}/{repository}/{branch}/{module}")
  public synchronized Collection<ModuleBuildWithState> getBuildHistoryForModule(@PathParam("host") String host,
                                                                                @PathParam("organization") String organization,
                                                                                @PathParam("repository") String repository,
                                                                                @PathParam("branch") String branch,
                                                                                @PathParam("module") String module) {
    updateBuildMap();

    GitInfo gitInfo = new GitInfo(host, organization, repository, branch);
    for (ModuleBuildWithState build : BUILD_MAP.values()) {
      if (build.getGitInfo().equals(gitInfo)) {
        if (build.getModule().getName().equals(module)) {
          return buildHistory(build);
        }
      }
    }

    throw new NotFoundException();
  }

  @GET
  @PropertyFiltering
  @Path("/{host}/{organization}/{repository}/{branch}/{module}/{buildNumber}")
  public synchronized ModuleBuildWithState getSpecificBuildForModule(@PathParam("host") String host,
                                                                     @PathParam("organization") String organization,
                                                                     @PathParam("repository") String repository,
                                                                     @PathParam("branch") String branch,
                                                                     @PathParam("module") String module,
                                                                     @PathParam("buildNumber") int buildNumber) {
    updateBuildMap();

    GitInfo gitInfo = new GitInfo(host, organization, repository, branch);
    for (ModuleBuildWithState build : BUILD_MAP.values()) {
      if (build.getGitInfo().equals(gitInfo)) {
        if (build.getModule().getName().equals(module)) {
          if (buildNumber <= 0 || build.getBuildState().getBuildNumber() < buildNumber) {
            throw new NotFoundException();
          } else {
            ModuleBuildWithState lastBuild = build;

            do {
              List<ModuleBuildWithState> builds = buildHistory(lastBuild);
              for (ModuleBuildWithState previousBuild : builds) {
                if (previousBuild.getBuildState().getBuildNumber() == buildNumber) {
                  return previousBuild;
                }
              }

              lastBuild = builds.get(builds.size() - 1);
            } while (lastBuild.getBuildState().getBuildNumber() > 0);
          }
        }
      }
    }

    throw new NotFoundException();
  }

  private static List<ModuleBuildWithState> buildHistory(ModuleBuildWithState build) {
    List<ModuleBuildWithState> builds = new ArrayList<>();
    builds.add(build);

    ModuleBuildWithState previous = build;
    int buildCount = ThreadLocalRandom.current().nextInt(10) + 10;
    for (int i = 0; i < buildCount; i++) {
      BuildState p = previous.getBuildState();
      Optional<Long> endTime = Optional.of(p.getStartTime() - 15000 - ThreadLocalRandom.current().nextInt(150000));
      long startTime = endTime.get() - 150000 - ThreadLocalRandom.current().nextInt(15000);

      BuildState newBuildState = new BuildState(p.getBuildNumber() - 1, p.getBuildLog(), p.getCommitSha(), randomCompletedResult(), startTime, endTime);
      ModuleBuildWithState current = new ModuleBuildWithState(previous.getGitInfo(), previous.getModule(), newBuildState);
      previous = current;
      builds.add(current);
    }

    Collections.sort(builds, new Comparator<ModuleBuildWithState>() {

      @Override
      public int compare(ModuleBuildWithState build1, ModuleBuildWithState build2) {
        return -1 * Longs.compare(build1.getBuildState().getStartTime(), build2.getBuildState().getStartTime());
      }
    });

    return builds;
  }

  private static void updateBuildMap() {
    for (Entry<ModuleBuild, ModuleBuildWithState> entry : BUILD_MAP.entrySet()) {
      ModuleBuildWithState build = entry.getValue();

      do {
        build = updateBuild(build);
      } while (build.getBuildState().getStartTime() + 120000 < System.currentTimeMillis());

      entry.setValue(build);
    }
  }

  private static ModuleBuildWithState updateBuild(ModuleBuildWithState previous) {
    if (previous.getBuildState().getResult() == IN_PROGRESS) {
      long buildDuration = 15000 + ThreadLocalRandom.current().nextInt(15000);
      if (System.currentTimeMillis() > previous.getBuildState().getStartTime() + buildDuration) {
        BuildState p = previous.getBuildState();
        Optional<Long> endTime = Optional.of(p.getStartTime() + buildDuration);
        BuildState newBuildState = new BuildState(p.getBuildNumber(), p.getBuildLog(), p.getCommitSha(), randomCompletedResult(), p.getStartTime(), endTime);
        return new ModuleBuildWithState(previous.getGitInfo(), previous.getModule(), newBuildState);
      } else {
        return previous;
      }
    } else {
      long sleepDuration = 15000 + ThreadLocalRandom.current().nextInt(15000);
      if (System.currentTimeMillis() > previous.getBuildState().getEndTime().get() + sleepDuration) {
        BuildState p = previous.getBuildState();
        long startTime = p.getEndTime().get() + sleepDuration;
        BuildState newBuildState = new BuildState(p.getBuildNumber() + 1, p.getBuildLog(), p.getCommitSha(), IN_PROGRESS, startTime, Optional.<Long>absent());
        return new ModuleBuildWithState(previous.getGitInfo(), previous.getModule(), newBuildState);
      } else {
        return previous;
      }
    }
  }

  private static Map<ModuleBuild, ModuleBuildWithState> initialBuilds() {
    List<ModuleBuildWithState> builds = new ArrayList<>();

    builds.add(build("Contacts", "ContactsHadoop"));
    builds.add(build("Contacts", "ContactsApiWeb"));
    builds.add(build("Contacts", "ContactsBase"));
    builds.add(build("Contacts", "ContactsData"));
    builds.add(build("Contacts", "ContactsKafka"));
    builds.add(build("Contacts", "ContactsSpark"));
    builds.add(build("Contacts", "ContactsTasks"));
    builds.add(build("HubSpotConnect"));
    builds.add(build("HubSpotConnect", ".", "gc-metrics"));
    builds.add(build("HubSpotConnect", ".", "eager-datasource"));
    builds.add(build("guice-jdbi"));
    builds.add(build("dropwizard-hubspot"));
    builds.add(build("dropwizard-hubspot", ".", "auth4"));
    builds.add(build("dropwizard-hubspot", ".", "new-metrics"));
    builds.add(build("Wormhole"));
    builds.add(build("Wormhole", ".", "better-url-escaping"));
    builds.add(build("Wormhole", ".", "nio-hystrix"));
    builds.add(build("Email", "EmailApiClient"));
    builds.add(build("Email", "EmailApiWeb"));
    builds.add(build("Email", "EmailApiCore"));
    builds.add(build("Email", "EmailApiData"));
    builds.add(build("Email", "EmailJobs"));
    builds.add(build("TaskService", "TaskServiceWorker"));
    builds.add(build("TaskService", "TaskServiceApiWeb"));
    builds.add(build("TaskService", "TaskServiceData"));
    builds.add(build("TaskService", "TaskServiceCommon"));
    builds.add(build("InternalEmail", "InternalEmailJobs"));
    builds.add(build("InternalEmail", "InternalEmailData"));
    builds.add(build("InternalEmail", "InternalEmailService"));
    builds.add(build("InternalEmail", "InternalEmailClient"));
    builds.add(build("InternalEmail", "InternalEmailCore"));
    builds.add(build("DeployService", "DeployServiceData"));
    builds.add(build("DeployService", "DeployServiceApiWeb"));
    builds.add(build("DeployService", "DeployCore"));
    builds.add(build("DeployService", "DeployTasks"));
    builds.add(build("HubSpotConfig"));
    builds.add(build("HubSpotConfig", ".", "new-meta-client"));
    builds.add(build("HubSpotConfig", ".", "no-regex"));
    builds.add(build("Overwatch", "OverwatchCore", "master", "PaaS"));
    builds.add(build("Overwatch", "OverwatchClient", "master", "PaaS"));
    builds.add(build("Overwatch", "OverwatchData", "master", "PaaS"));
    builds.add(build("Overwatch", "OverwatchService", "master", "PaaS"));
    builds.add(build("Overwatch", "OverwatchJobs", "master", "PaaS"));
    builds.add(build("Overwatch", "OverwatchCore", "build-time", "PaaS"));
    builds.add(build("Overwatch", "OverwatchClient", "build-time", "PaaS"));
    builds.add(build("Overwatch", "OverwatchData", "build-time", "PaaS"));
    builds.add(build("Overwatch", "OverwatchService", "build-time", "PaaS"));
    builds.add(build("Overwatch", "OverwatchJobs", "build-time", "PaaS"));
    builds.add(build("Singularity", "SingularityBase", "master", "HubSpot", "github.com"));
    builds.add(build("Singularity", "SingularityData", "master", "HubSpot", "github.com"));
    builds.add(build("Singularity", "SingularityService", "master", "HubSpot", "github.com"));
    builds.add(build("Singularity", "SingularityS3Downloader", "master", "HubSpot", "github.com"));
    builds.add(build("Singularity", "SingularityExecutor", "master", "HubSpot", "github.com"));
    builds.add(build("Singularity", "SingularityBase", "hs_staging", "HubSpot", "github.com"));
    builds.add(build("Singularity", "SingularityData", "hs_staging", "HubSpot", "github.com"));
    builds.add(build("Singularity", "SingularityService", "hs_staging", "HubSpot", "github.com"));
    builds.add(build("Singularity", "SingularityS3Downloader", "hs_staging", "HubSpot", "github.com"));
    builds.add(build("Singularity", "SingularityExecutor", "hs_staging", "HubSpot", "github.com"));

    Map<ModuleBuild, ModuleBuildWithState> buildMap = new ConcurrentHashMap<>();

    for (ModuleBuildWithState build : builds) {
      buildMap.put(new ModuleBuild(build.getGitInfo(), build.getModule()), build);
    }

    return buildMap;
  }

  private static ModuleBuildWithState build(String repoName) {
    return new ModuleBuildWithState(gitInfo(repoName), new Module(repoName, "."), buildState());
  }

  private static ModuleBuildWithState build(String repoName, String moduleName) {
    return build(repoName, moduleName, "master");
  }

  private static ModuleBuildWithState build(String repoName, String moduleName, String branch) {
    return build(repoName, moduleName, branch, "HubSpot");
  }

  private static ModuleBuildWithState build(String repoName, String moduleName, String branch, String organization) {
    return build(repoName, moduleName, branch, organization, "git.hubteam.com");
  }

  private static ModuleBuildWithState build(String repoName, String moduleName, String branch, String organization, String host) {
    GitInfo gitInfo = new GitInfo(host, organization, repoName, branch);
    return new ModuleBuildWithState(gitInfo, new Module(".".equals(moduleName) ? repoName : moduleName, moduleName), buildState());
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

    return new BuildState(buildNumber, "https://s3.amazonaws.com/archive.travis-ci.org/jobs/70997110/log.txt", "abc", result, startTime, endTime);
  }

  private static BuildState.Result randomCompletedResult() {
    BuildState.Result result = BuildState.Result.values()[ThreadLocalRandom.current().nextInt(BuildState.Result.values().length)];
    return result == IN_PROGRESS ? randomCompletedResult() : result;
  }
}
