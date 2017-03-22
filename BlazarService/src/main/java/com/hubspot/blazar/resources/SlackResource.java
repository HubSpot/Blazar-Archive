package com.hubspot.blazar.resources;

import java.io.IOException;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;
import com.hubspot.blazar.externalservice.slack.SlackChannel;
import com.hubspot.blazar.util.BlazarSlackClient;

@Path("/slack")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SlackResource {
  private BlazarSlackClient blazarSlackClient;

  @Inject
  public SlackResource(BlazarSlackClient blazarSlackClient) {
    this.blazarSlackClient = blazarSlackClient;
  }

  @GET
  @Path("/channels")
  public Set<SlackChannel> getChannels() throws IOException {
    return blazarSlackClient.getChannels();
  }
}
