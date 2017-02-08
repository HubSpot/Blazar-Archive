package com.hubspot.blazar.resources;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.LogChunk;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.ModuleBuild.State;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.SingularityConfiguration;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.horizon.AsyncHttpClient;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.mesos.json.MesosFileChunkObject;
import com.hubspot.singularity.SingularityS3Log;
import com.hubspot.singularity.SingularitySandbox;
import com.hubspot.singularity.SingularitySandboxFile;
import com.hubspot.singularity.SingularityTaskHistory;
import com.hubspot.singularity.SingularityTaskHistoryUpdate;
import com.hubspot.singularity.SingularityTaskHistoryUpdate.SimplifiedTaskState;
import com.hubspot.singularity.client.SingularityClient;

@Path("/modules/builds")
@Produces(MediaType.APPLICATION_JSON)
public class ModuleBuildResource {
  private static final Logger LOG = LoggerFactory.getLogger(ModuleBuildResource.class);
  private static final String BUILD_LOG_NAME = "service.log";

  private final ModuleBuildService moduleBuildService;
  private final SingularityClient singularityClient;
  private SingularityConfiguration singularityConfiguration;
  private final AsyncHttpClient asyncHttpClient;

  @Inject
  public ModuleBuildResource(ModuleBuildService moduleBuildService,
                             SingularityClient singularityClient,
                             BlazarConfiguration blazarConfiguration,
                             AsyncHttpClient asyncHttpClient) {
    this.moduleBuildService = moduleBuildService;
    this.singularityClient = singularityClient;
    this.singularityConfiguration = blazarConfiguration.getSingularityConfiguration();
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
    ModuleBuild build = getBuildWithError(moduleBuildId);

    Optional<String> taskId = build.getTaskId();
    if (!taskId.isPresent()) {
      throw new NotFoundException("No taskId found for build: " + moduleBuildId);
    }

    String path = taskId.get() + "/" + BUILD_LOG_NAME;
    Optional<String> grep = Optional.absent();

    Optional<MesosFileChunkObject> chunk = singularityClient.readSandBoxFile(taskId.get(), path, grep, Optional.of(offset), Optional.of(length));
    if (chunk.isPresent()) {
      if (chunk.get().getData().isEmpty() && logCompleted(build)) {
        return new LogChunk(chunk.get().getData(), chunk.get().getOffset(), -1);
      } else {
        return new LogChunk(chunk.get().getData(), chunk.get().getOffset());
      }
    } else {
      SingularityS3Log s3Log = findS3ServiceLog(taskId.get());
      if (offset >= s3Log.getSize()) {
        return new LogChunk("", s3Log.getSize(), -1);
      }

      return readS3LogChunk(s3Log.getGetUrl(), offset, length);
    }
  }

  @GET
  @Path("/{id}/log/size")
  public Object getLogSize(@PathParam("id") long moduleBuildId) {
    ModuleBuild build = getBuildWithError(moduleBuildId);

    Optional<String> taskId = build.getTaskId();
    if (!taskId.isPresent()) {
      throw new NotFoundException("No taskId found for build: " + moduleBuildId);
    }

    String path = taskId.get() + BUILD_LOG_NAME;
    Optional<Long> absent = Optional.absent();
    Optional<String> grep = Optional.absent();

    Optional<MesosFileChunkObject> chunk = singularityClient.readSandBoxFile(taskId.get(), path, grep, absent, absent);
    final long size;
    if (chunk.isPresent()) {
      size = chunk.get().getOffset();
    } else {
      size = findS3ServiceLog(taskId.get()).getSize();
    }

    return new Object() {

      public long getSize() {
        return size;
      }
    };
  }

  @GET
  @Path("{id}/log/download")
  public Response getLog(@PathParam("id") long moduleBuildId) throws URISyntaxException {
    ModuleBuild build = getBuildWithError(moduleBuildId);

    Optional<String> taskId = build.getTaskId();
    if (!taskId.isPresent()) {
      throw new NotFoundException("No taskId found for build: " + moduleBuildId);
    }

    Optional<SingularitySandbox> sandboxOptional = singularityClient.browseTaskSandBox(taskId.get(), taskId.get());

    java.util.Optional<SingularitySandboxFile> buildLogFile = java.util.Optional.empty();
    if (sandboxOptional.isPresent()) {
      buildLogFile = sandboxOptional.get().getFiles().stream().filter(l -> Objects.equals(l.getName(), BUILD_LOG_NAME)).findFirst();
    }

    if (buildLogFile.isPresent()) {
      String host = sandboxOptional.get().getSlaveHostname();
      int port = singularityConfiguration.getSlaveHttpPort();
      String path = sandboxOptional.get().getFullPathToRoot() + "/" + sandboxOptional.get().getCurrentDirectory() + "/" + buildLogFile.get().getName();
      String downloadEndpointPath = "/files/download.json";
      URI uri = new URI("http", null, host, port, downloadEndpointPath, "?path=" + path, null);
      return Response.temporaryRedirect(uri).build();
    }

    try {
      SingularityS3Log urlData = findS3ServiceLog(taskId.get());
      String logUrl = urlData.getDownloadUrl();
      URI uri = new URI(logUrl);
      return Response.temporaryRedirect(uri).build();
    } catch (NotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  private SingularityS3Log findS3ServiceLog(String taskId) {
    Collection<SingularityS3Log> s3Logs = singularityClient.getTaskLogs(taskId);
    List<SingularityS3Log> serviceLogs = new ArrayList<>();
    for (SingularityS3Log s3Log : s3Logs) {
      if (s3Log.getGetUrl().contains("service.log")) {
        serviceLogs.add(s3Log);
      }
    }

    if (serviceLogs.isEmpty()) {
      throw new NotFoundException("No S3 log found for task " + taskId);
    } else if (serviceLogs.size() > 1) {
      throw new NotFoundException("Multiple S3 logs found for task " + taskId);
    } else {
      return serviceLogs.get(0);
    }
  }

  private LogChunk readS3LogChunk(String url, long offset, long length) throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .setUrl(url)
        .addHeader("Range", String.format("bytes=%d-%d", offset, offset + length - 1))
        .build();

    HttpResponse response = asyncHttpClient.execute(request).get();
    if (response.isSuccess()) {
      return new LogChunk(response.getAsBytes(), offset);
    } else {
      String message = String.format("Error reading S3 log, status code %d, response %s", response.getStatusCode(), response.getAsString());
      LOG.warn(message);
      throw new WebApplicationException(Response.serverError().entity(message).type(MediaType.TEXT_PLAIN_TYPE).build());
    }
  }

  private boolean logCompleted(ModuleBuild build) {
    return build.getState().isComplete() && taskComplete(build.getTaskId().get());
  }

  private boolean taskComplete(String taskId) {
    Optional<SingularityTaskHistory> taskHistory = singularityClient.getHistoryForTask(taskId);
    if (!taskHistory.isPresent()) {
      return true;
    }

    SimplifiedTaskState taskState = SingularityTaskHistoryUpdate.getCurrentState(taskHistory.get().getTaskUpdates());
    return taskState == SimplifiedTaskState.DONE;
  }

  private ModuleBuild getBuildWithError(long moduleBuildId) {
    Optional<ModuleBuild> maybeBuild = get(moduleBuildId);
    if (maybeBuild.isPresent()) {
      return maybeBuild.get();
    } else {
      throw new NotFoundException("No build found with id: " + moduleBuildId);
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
