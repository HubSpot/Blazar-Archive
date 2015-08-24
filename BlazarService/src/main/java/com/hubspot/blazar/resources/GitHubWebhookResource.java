package com.hubspot.blazar.resources;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.github.GitHubProtos.CreateEvent;
import com.hubspot.blazar.github.GitHubProtos.DeleteEvent;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;
import com.hubspot.blazar.util.GitHubWebhookHandler;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Set;

@Path("/github/webhooks")
@Consumes(MediaType.APPLICATION_JSON)
public class GitHubWebhookResource {
  private final EventBus eventBus;
  private final GitHubWebhookHandler handler;

  @Inject
  public GitHubWebhookResource(EventBus eventBus, GitHubWebhookHandler handler) {
    this.eventBus = eventBus;
    this.handler = handler;
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

  @PUT
  @Path("/process")
  @Produces(MediaType.APPLICATION_JSON)
  public Set<Module> processBranch(GitInfo gitInfo) throws IOException {
    Preconditions.checkArgument(gitInfo.getRepositoryId().get() > 0, "repositoryId must be > 0");
    return handler.processBranch(gitInfo);
  }
}
