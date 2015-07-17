package com.hubspot.blazar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.hubspot.blazar.github.GitHubProtos;
import io.dropwizard.setup.Environment;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/github/webhooks")
@Consumes(MediaType.APPLICATION_JSON)
public class GitHubWebHookResource {
  private final ObjectMapper mapper;

  @Inject
  public GitHubWebHookResource(Environment environment) {
    this.mapper = environment.getObjectMapper();
  }

  @POST
  @Path("/push")
  public void processWebhook(GitHubProtos.PushEvent pushEvent) throws IOException {
    System.out.println(mapper.writeValueAsString(pushEvent));
  }
}
