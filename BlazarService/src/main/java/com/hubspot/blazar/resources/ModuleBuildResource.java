package com.hubspot.blazar.resources;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.LogChunk;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuild.State;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.exception.LogNotFoundException;
import com.hubspot.blazar.externalservice.BuildClusterService;
import com.hubspot.horizon.AsyncHttpClient;

@Path("/modules/builds")
@Produces(MediaType.APPLICATION_JSON)
public class ModuleBuildResource {
  private static final Logger LOG = LoggerFactory.getLogger(ModuleBuildResource.class);
  private static final String BUILD_LOG_NAME = "service.log";

  private final BuildClusterService buildClusterService;
  private final ModuleBuildService moduleBuildService;
  private final AsyncHttpClient asyncHttpClient;

  @Inject
  public ModuleBuildResource(BuildClusterService buildClusterService,
                             ModuleBuildService moduleBuildService,
                             AsyncHttpClient asyncHttpClient) {

    this.buildClusterService = buildClusterService;
    this.moduleBuildService = moduleBuildService;
    this.asyncHttpClient = asyncHttpClient;
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

    ModuleBuild inProgress = build.toBuilder().setState(State.IN_PROGRESS).setTaskId(Optional.of(taskId.get())).build();
    moduleBuildService.update(inProgress);
    return inProgress;
  }

  @PUT
  @Path("/{id}/success")
  public ModuleBuild completeSuccess(@PathParam("id") long moduleBuildId) {
    ModuleBuild build = getBuildWithExpectedState(moduleBuildId, State.IN_PROGRESS);

    ModuleBuild succeeded = build.toBuilder().setState(State.SUCCEEDED).setEndTimestamp(Optional.of(System.currentTimeMillis())).build();
    moduleBuildService.update(succeeded);
    return succeeded;
  }

  @PUT
  @Path("/{id}/failure")
  public ModuleBuild completeFailure(@PathParam("id") long moduleBuildId) {
    ModuleBuild build = getBuildWithExpectedState(moduleBuildId, State.IN_PROGRESS);

    ModuleBuild failed = build.toBuilder().setState(State.FAILED).setEndTimestamp(Optional.of(System.currentTimeMillis())).build();
    moduleBuildService.update(failed);
    return failed;
  }

  @GET
  @Path("/{id}/log")
  public LogChunk getLog(@PathParam("id") long moduleBuildId,
                         @QueryParam("offset") @DefaultValue("0") long offset,
                         @QueryParam("length") @DefaultValue("65536") long length) throws Exception {
    try {
      ModuleBuild moduleBuild = getBuildWithError(moduleBuildId);
      return buildClusterService.getBuildContainerLog(moduleBuild, offset, length);
    } catch (LogNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);
    } catch (NotFoundException e) {
      throw e;
    }
    catch (Exception e) {
      throw new WebApplicationException(String.format("An error occurred while retrieving the log for module build %d. The error is: %s",
          moduleBuildId, e.getMessage()), Status.INTERNAL_SERVER_ERROR);
    }
  }

  @GET
  @Path("/{id}/log/size")
  public Object getLogSize(@PathParam("id") long moduleBuildId) {
    try {
      ModuleBuild moduleBuild = getBuildWithError(moduleBuildId);

      long logFileSize = buildClusterService.getBuildContainerLogSize(moduleBuild);
      return new Object() {

        public long getSize() {
          return logFileSize;
        }
      };
    } catch (LogNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);
    } catch (NotFoundException e) {
      throw e;
    }
    catch (Exception e) {
      throw new WebApplicationException(String.format("An error occurred while retrieving the log size for module build %d. The error is: %s",
          moduleBuildId, e.getMessage()), Status.INTERNAL_SERVER_ERROR);
    }
  }

  @GET
  @Path("{id}/log/download-url")
  public Object getLogDownloadUrl(@PathParam("id") long moduleBuildId) {
    try {
      ModuleBuild moduleBuild = getBuildWithError(moduleBuildId);

      String buildLogUrl = buildClusterService.getBuildContainerLogUrl(moduleBuild);
      return new Object() {
        public String getDownloadUrl() {
          return buildLogUrl;
        }
      };
    } catch (LogNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);
    } catch (NotFoundException e) {
      throw e;
    }
    catch (Exception e) {
      throw new WebApplicationException(String.format("An error occurred while retrieving the log url for module build %d. The error is: %s",
          moduleBuildId, e.getMessage()), Status.INTERNAL_SERVER_ERROR);
    }
  }

  private ModuleBuild getBuildWithError(long moduleBuildId) {
    Optional<ModuleBuild> maybeBuild = get(moduleBuildId);
    if (maybeBuild.isPresent()) {
      return maybeBuild.get();
    } else {
      throw new NotFoundException("No module build found with id: " + moduleBuildId);
    }
  }

  private ModuleBuild getBuildWithExpectedState(long moduleBuildId, State expected) {
    ModuleBuild build = getBuildWithError(moduleBuildId);
    if (build.getState() == expected) {
      return build;
    } else {
      throw new IllegalStateException(String.format("Build is in state %s, expected %s", build.getState(), expected));
    }
  }
}
