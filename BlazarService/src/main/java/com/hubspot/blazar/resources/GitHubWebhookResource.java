package com.hubspot.blazar.resources;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.hubspot.blazar.github.GitHubProtos.CreateEvent;
import com.hubspot.blazar.github.GitHubProtos.DeleteEvent;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/github/webhooks")
@Consumes(MediaType.APPLICATION_JSON)
public class GitHubWebhookResource {
  private final EventBus eventBus;

  @Inject
  public GitHubWebhookResource(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @POST
  @Path("/create")
  public void processCreateEvent(CreateEvent createEvent) {
    eventBus.post(createEvent);
  }

  @POST
  @Path("/delete")
  public void processDeleteEvent(DeleteEvent deleteEvent) {
    eventBus.post(deleteEvent);
  }

  @POST
  @Path("/push")
  public void processPushEvent(PushEvent pushEvent) {
    eventBus.post(pushEvent);
  }
}
