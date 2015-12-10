package com.hubspot.blazar.resources;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.LogChunk;
import com.hubspot.blazar.base.ModuleBuild;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/modules/builds")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ModuleBuildResource {

  @Inject
  public ModuleBuildResource() {}

  @GET
  @Path("/{id}")
  public Optional<ModuleBuild> get(@PathParam("id") long moduleBuildId) {
    // TODO
    return null;
  }

  @PUT
  public ModuleBuild update(ModuleBuild moduleBuild) {
    // TODO
    return null;
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
}
