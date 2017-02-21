package com.hubspot.blazar.resources;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;
import com.hubspot.blazar.externalservice.slack.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;

@Path("/slack")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SlackResource {

  private SlackSession slackSession;

  @Inject
  public SlackResource(SlackSession slackSession) {

    this.slackSession = slackSession;
  }

  @GET
  @Path("/channels")
  public Set<SlackChannel> getChannels() throws IOException {
    Collection<com.ullink.slack.simpleslackapi.SlackChannel> channels = slackSession.getChannels();
    return channels.stream().map(channel ->  new SlackChannel(channel.getId(), channel.getName())).collect(Collectors.toSet());
  }

}
