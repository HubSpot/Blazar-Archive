package com.hubspot.blazar.resources;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.hubspot.blazar.base.Build;
import com.hubspot.blazar.base.Build.State;
import com.hubspot.blazar.base.BuildDefinition;
import com.hubspot.blazar.base.BuildState;
import com.hubspot.blazar.data.service.BuildStateService;
import com.hubspot.jackson.jaxrs.PropertyFiltering;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Set;

@Path("/build/states")
@Produces(MediaType.APPLICATION_JSON)
public class BuildStateResource {
  private final BuildStateService buildStateService;

  @Inject
  public BuildStateResource(BuildStateService buildStateService) {
    this.buildStateService = buildStateService;
  }

  @GET
  @Path("/")
  @PropertyFiltering
  public Response getAllBuildStates(@QueryParam("since") @DefaultValue("0") long since,
                                    @QueryParam("moduleId") Set<Integer> moduleIds,
                                    @QueryParam("branchId") Set<Integer> branchIds,
                                    @QueryParam("buildState") Set<Build.State> buildStates,
                                    @QueryParam("repositoryId") Set<Long> repositoryIds) {
    Set<BuildState> builds = buildStateService.getAllActive(since);
    long offset = Math.max(maxUpdatedTimestamp(builds), since);

    Predicate<BuildState> predicate = new BuildStateQuery(moduleIds, branchIds, buildStates, repositoryIds);
    builds = ImmutableSet.copyOf(Iterables.filter(builds, predicate));

    return Response.ok(builds).header("x-last-modified-timestamp", offset).build();
  }

  private static class BuildStateQuery implements Predicate<BuildState> {
    private final Set<Integer> moduleIds;
    private final Set<Integer> branchIds;
    private final Set<Build.State> buildStates;
    private final Set<Long> repositoryIds;
    private final boolean acceptAll;

    public BuildStateQuery(Set<Integer> moduleIds,
                           Set<Integer> branchIds,
                           Set<State> buildStates,
                           Set<Long> repositoryIds) {
      this.moduleIds = moduleIds;
      this.branchIds = branchIds;
      this.buildStates = buildStates;
      this.repositoryIds = repositoryIds;
      this.acceptAll = moduleIds.isEmpty() && branchIds.isEmpty() && buildStates.isEmpty() && repositoryIds.isEmpty();
    }

    @Override
    public boolean apply(BuildState build) {
      if (acceptAll) {
        return true;
      } else if (moduleIds.contains(build.getModule().getId().get())) {
        return true;
      } else if (branchIds.contains(build.getGitInfo().getId().get())) {
        return true;
      } else if (repositoryIds.contains(build.getGitInfo().getRepositoryId())) {
        return true;
      } else if (build.getPendingBuild().isPresent() && buildStates.contains(build.getPendingBuild().get().getState())) {
        return true;
      } else if (build.getInProgressBuild().isPresent() && buildStates.contains(build.getInProgressBuild().get().getState())) {
        return true;
      } else if (build.getLastBuild().isPresent() && buildStates.contains(build.getLastBuild().get().getState())) {
        return true;
      } else {
        return false;
      }
    }
  }

  private static long maxUpdatedTimestamp(Collection<? extends BuildDefinition> definitions) {
    long maxUpdatedTimestamp = 0;
    for (BuildDefinition definition : definitions) {
      if (definition.getModule().getUpdatedTimestamp() > maxUpdatedTimestamp) {
        maxUpdatedTimestamp = definition.getModule().getUpdatedTimestamp();
      }
    }

    return maxUpdatedTimestamp;
  }
}
