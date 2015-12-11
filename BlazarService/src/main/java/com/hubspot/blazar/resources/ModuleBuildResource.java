package com.hubspot.blazar.resources;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.LogChunk;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuild.State;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.sun.jersey.api.NotFoundException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/modules/builds")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ModuleBuildResource {
  private final ModuleBuildService moduleBuildService;

  @Inject
  public ModuleBuildResource(ModuleBuildService moduleBuildService) {
    this.moduleBuildService = moduleBuildService;
  }

  @GET
  @Path("/{id}")
  public Optional<ModuleBuild> get(@PathParam("id") long moduleBuildId) {
    return moduleBuildService.get(moduleBuildId);
  }

  @PUT
  @Path("/{id}/start")
  public ModuleBuild start(@PathParam("id") long moduleBuildId, @QueryParam("taskId") Optional<String> taskId) {
    ModuleBuild build = getBuildWithExpectedState(moduleBuildId, State.LAUNCHING);

    if (!taskId.isPresent()) {
      throw new IllegalArgumentException("Task ID is required");
    }

    ModuleBuild inProgress = build.withState(State.IN_PROGRESS).withTaskId(taskId.get());
    moduleBuildService.update(inProgress);
    return inProgress;
  }

  @PUT
  @Path("/{id}/success")
  public ModuleBuild completeSuccess(@PathParam("id") long moduleBuildId) {
    ModuleBuild build = getBuildWithExpectedState(moduleBuildId, State.IN_PROGRESS);

    ModuleBuild succeeded = build.withState(State.SUCCEEDED).withEndTimestamp(System.currentTimeMillis());
    moduleBuildService.update(succeeded);
    return succeeded;
  }

  @PUT
  @Path("/{id}/failure")
  public ModuleBuild completeFailure(@PathParam("id") long moduleBuildId) {
    ModuleBuild build = getBuildWithExpectedState(moduleBuildId, State.IN_PROGRESS);

    ModuleBuild failed = build.withState(State.FAILED).withEndTimestamp(System.currentTimeMillis());
    moduleBuildService.update(failed);
    return failed;
  }

  @GET
  @Path("/{id}/log")
  public LogChunk getLog(@PathParam("id") long moduleBuildId) {
    // TODO
    return null;
  }

  @GET
  @Path("/{id}/log/size")
  public Object getLogSize(@PathParam("id") long moduleBuildId) {
    // TODO
    return null;
  }

  private ModuleBuild getBuildWithExpectedState(long moduleBuildId, State expected) {
    Optional<ModuleBuild> maybeBuild = get(moduleBuildId);
    if (!maybeBuild.isPresent()) {
      throw new NotFoundException("No build found with id: " + moduleBuildId);
    }

    ModuleBuild build = maybeBuild.get();
    if (build.getState() == expected) {
      return build;
    } else {
      throw new IllegalStateException(String.format("Build is in state %s, expected %s", build.getState(), expected));
    }
  }
}
