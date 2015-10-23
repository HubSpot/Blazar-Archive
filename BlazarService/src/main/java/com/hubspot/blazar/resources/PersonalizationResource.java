package com.hubspot.blazar.resources;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;
import com.hubspot.blazar.base.BuildState;
import com.hubspot.blazar.data.service.EventService;

@Path("/personalization")
@Produces(MediaType.APPLICATION_JSON)
public class PersonalizationResource {

  private EventService eventService;

  @Inject
  public PersonalizationResource(EventService eventService) {
    this.eventService = eventService;
  }

  @GET
  @Path("/activity/{userName}")
  public List<BuildState> getByUserName(@PathParam("userName") String userName, @QueryParam("gitHubHost") @DefaultValue("git.hubteam.com") final String gitHubHost, @QueryParam("since") @DefaultValue("0") long since) throws IOException {
    return eventService.fetch(userName, gitHubHost, since);
  }
}
