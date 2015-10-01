package com.hubspot.blazar.resources;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.inject.Inject;
import com.hubspot.blazar.base.BuildDefinition;
import com.hubspot.blazar.base.BuildState;
import com.hubspot.blazar.base.LogChunk;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.data.service.BuildDefinitionService;
import com.hubspot.blazar.data.service.BuildService;
import com.hubspot.blazar.data.service.BuildStateService;
import com.hubspot.horizon.AsyncHttpClient;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.jackson.jaxrs.PropertyFiltering;
import com.hubspot.mesos.json.MesosFileChunkObject;
import com.hubspot.singularity.SingularityS3Log;
import com.hubspot.singularity.client.SingularityClient;
import com.sun.jersey.api.NotFoundException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;

@Path("/build")
@Produces(MediaType.APPLICATION_JSON)
public class BuildResource {
  private final BuildDefinitionService buildDefinitionService;
  private final BuildStateService buildStateService;
  private final BuildService buildService;
  private final SingularityClient singularityClient;
  private final AsyncHttpClient asyncHttpClient;

  @Inject
  public BuildResource(BuildDefinitionService buildDefinitionService,
                       BuildStateService buildStateService,
                       BuildService buildService,
                       SingularityClient singularityClient,
                       AsyncHttpClient asyncHttpClient) {
    this.buildDefinitionService = buildDefinitionService;
    this.buildStateService = buildStateService;
    this.buildService = buildService;
    this.singularityClient = singularityClient;
    this.asyncHttpClient = asyncHttpClient;
  }

  @GET
  @Path("/definitions")
  @PropertyFiltering
  public Set<BuildDefinition> getAllBuildDefinitions(@QueryParam("since") @DefaultValue("0") long since) {
    return buildDefinitionService.getAllActive(since);
  }

  @GET
  @Path("/states")
  @PropertyFiltering
  public Set<BuildState> getAllBuildStates(@QueryParam("since") @DefaultValue("0") long since) {
    return buildStateService.getAllActive(since);
  }

  @GET
  @Path("/{id}")
  public Optional<ModuleBuild> get(@PathParam("id") long id) {
    return buildService.get(id);
  }

  @GET
  @Path("/{id}/log")
  public LogChunk getLog(@PathParam("id") long id,
                         @QueryParam("offset") @DefaultValue("0") long offset,
                         @QueryParam("length") @DefaultValue("90000") long length) throws Exception {
    Optional<ModuleBuild> build = get(id);
    if (!build.isPresent()) {
      throw new NotFoundException("No build found for ID " + id);
    }

    boolean completed = build.get().getBuild().getState().isComplete();
    Optional<String> logUrl = build.get().getBuild().getLog();
    if (!logUrl.isPresent()) {
      throw new NotFoundException("No build log URL found for ID " + id);
    }

    String taskId = parseTaskId(logUrl.get());
    String path = taskId + "/service.log";
    Optional<String> grep = Optional.absent();

    Optional<MesosFileChunkObject> chunk = singularityClient.readSandBoxFile(taskId, path, grep, Optional.of(offset), Optional.of(length));
    if (chunk.isPresent()) {
      if (completed && chunk.get().getData().isEmpty()) {
        return new LogChunk(chunk.get().getData(), chunk.get().getOffset(), -1);
      } else {
        return new LogChunk(chunk.get().getData(), chunk.get().getOffset());
      }
    } else {
      Collection<SingularityS3Log> s3Logs = singularityClient.getTaskLogs(taskId);
      if (s3Logs.isEmpty()) {
        throw new NotFoundException("No S3 log found for ID " + id);
      } else if (s3Logs.size() > 1) {
        throw new NotFoundException("Multiple S3 logs found for ID " + id);
      }

      SingularityS3Log s3Log = s3Logs.iterator().next();
      if (offset >= s3Log.getSize()) {
        return new LogChunk("", s3Log.getSize(), -1);
      }

      return fetchS3Log(s3Log.getGetUrl(), offset, length);
    }
  }

  @POST
  @Path("/module/{moduleId}")
  public ModuleBuild trigger(@PathParam("moduleId") int moduleId) {
    return trigger(buildDefinitionService.getByModule(moduleId).get());
  }

  @POST
  public ModuleBuild trigger(BuildDefinition buildDefinition) {
    BuildState buildState = buildService.enqueue(buildDefinition);

    return new ModuleBuild(buildState.getGitInfo(), buildState.getModule(), buildState.getPendingBuild().get());
  }

  @PUT
  public ModuleBuild update(ModuleBuild moduleBuild) {
    buildService.update(moduleBuild.getBuild());
    return moduleBuild;
  }

  private LogChunk fetchS3Log(String url, long offset, long length) throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .setUrl(url)
        .addHeader("Range", String.format("bytes=%d-%d", offset, offset + length))
        .build();

    HttpResponse response = asyncHttpClient.execute(request).get();
    if (response.isSuccess()) {
      return new LogChunk(response.getAsBytes(), offset);
    } else {
      String message = String.format("Error reading S3 log, status code %d, response %s", response.getStatusCode(), response.getAsString());
      throw new WebApplicationException(Response.serverError().entity(message).type(MediaType.TEXT_PLAIN_TYPE).build());
    }
  }

  private static String parseTaskId(String url) {
    String path = URI.create(url).getPath();
    for (String part : Splitter.on('/').omitEmptyStrings().split(path)) {
      if (part.startsWith("blazar-executor")) {
        return part;
      }
    }

    throw new IllegalArgumentException("Could not parse task ID from log URL " + url);
  }
}
