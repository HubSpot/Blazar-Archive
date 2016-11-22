package com.hubspot.blazar.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.hubspot.blazar.github.GitHubProtos.CreateEvent;
import com.hubspot.blazar.github.GitHubProtos.DeleteEvent;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;

@Path("/github/webhooks")
@Consumes(MediaType.APPLICATION_JSON)
public class GitHubWebhookResource {
  private static final Logger LOG = LoggerFactory.getLogger(GitHubWebhookResource.class);
  private final EventBus eventBus;

  @Inject
  public GitHubWebhookResource(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @POST
  @Path("/create")
  public void processCreateEvent(CreateEvent createEvent) {
    LOG.info("Got CreateEvent for ref {} for {} ", createEvent.getRef(), createEvent.getRepository().getFullName());
    eventBus.post(createEvent);
  }

  @POST
  @Path("/delete")
  public void processDeleteEvent(DeleteEvent deleteEvent) {
    LOG.info("Got DeleteEvent for ref {} for {}", deleteEvent.getRef(), deleteEvent.getRepository().getFullName());
    eventBus.post(deleteEvent);
  }

  @POST
  @Path("/push")
  public void processPushEvent(PushEvent pushEvent) {
    LOG.info("Got PushEvent for ref {} beforeSha {} afterSha {} for {}",
        pushEvent.getRef(), pushEvent.getBefore(), pushEvent.getAfter(), pushEvent.getRepository().getFullName());
    eventBus.post(pushEvent);
  }
}
