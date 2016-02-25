package com.hubspot.blazar.resources;


import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.externalservice.slack.SlackChannel;
import com.hubspot.blazar.base.slack.SlackConfiguration;
import com.hubspot.blazar.data.service.SlackConfigurationService;
import com.hubspot.blazar.integration.slack.SlackClient;
import com.hubspot.jackson.jaxrs.PropertyFiltering;

@Path("/slack")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SlackConfigurationResource {


  private final SlackClient slackClient;
  private SlackConfigurationService slackConfigurationService;

  @Inject
  public SlackConfigurationResource(SlackClient slackClient,
                                    SlackConfigurationService slackConfigurationService) {
    this.slackClient = slackClient;
    this.slackConfigurationService = slackConfigurationService;
  }

  @GET
  @PropertyFiltering
  public Set<SlackConfiguration> getAll() {
    return slackConfigurationService.getAll();
  }

  @PUT
  public Optional<SlackConfiguration> insert(SlackConfiguration slackConfiguration) {
    long id = slackConfigurationService.insert(slackConfiguration);
    return slackConfigurationService.get(id);
  }

  @POST
  public Optional<SlackConfiguration> update(SlackConfiguration slackConfiguration) {
    slackConfigurationService.update(slackConfiguration);
    return slackConfigurationService.get(slackConfiguration.getId().get());
  }

  @DELETE
  public void delete(long id) {
    slackConfigurationService.delete(id);
  }

  @GET
  @Path("/channels")
  public List<SlackChannel> getChannels() throws IOException {
    return slackClient.getSlackChannels();
  }

  @GET
  @Path("/{id}")
  @PropertyFiltering
  public Optional<SlackConfiguration> get(@PathParam("id") long id) {
    return slackConfigurationService.get(id);
  }

  @GET
  @Path("/repository/{id}")
  @PropertyFiltering
  public Set<SlackConfiguration> getAllWithBranchId(@PathParam("id") long id) {
    return slackConfigurationService.getAllWithBranchId(id);
  }
}
