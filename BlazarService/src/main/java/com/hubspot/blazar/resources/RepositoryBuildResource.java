package com.hubspot.blazar.resources;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.RepositoryBuild;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/branches/builds")
@Produces(MediaType.APPLICATION_JSON)
public class RepositoryBuildResource {

  @Inject
  public RepositoryBuildResource() {}

  @POST
  @Path("/branch/{id}")
  public RepositoryBuild trigger(@PathParam("id") int branchId) {
    // TODO
    return null;
  }

  @GET
  @Path("/{id}")
  public Optional<RepositoryBuild> get(@PathParam("id") long repositoryBuildId) {
    // TODO
    return null;
  }

  @POST
  @Path("/{id}/cancel")
  public void cancel(@PathParam("id") long repositoryBuildId) {
    // TODO
  }
}
