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
import com.hubspot.blazar.base.notifications.InstantMessageConfiguration;
import com.hubspot.blazar.data.service.InstantMessageConfigurationService;
import com.hubspot.blazar.externalservice.slack.SlackChannel;
import com.hubspot.blazar.integration.slack.SlackClient;
import com.hubspot.jackson.jaxrs.PropertyFiltering;

@Path("/instant-message")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InstantMessageResource {

  private final SlackClient slackClient;
  private InstantMessageConfigurationService instantMessageConfigurationService;

  @Inject
  public InstantMessageResource(SlackClient slackClient, InstantMessageConfigurationService instantMessageConfigurationService) {
    this.slackClient = slackClient;
    this.instantMessageConfigurationService = instantMessageConfigurationService;
  }

  @GET
  @PropertyFiltering
  @Path("/configurations")
  public Set<InstantMessageConfiguration> getAll() {
    return instantMessageConfigurationService.getAll();
  }

  @POST
  @Path("/configurations")
  public InstantMessageConfiguration insert(InstantMessageConfiguration instantMessageConfiguration) {
    return instantMessageConfigurationService.insert(instantMessageConfiguration);
  }

  @PUT
  @Path("/configurations/{id}")
  public void update(InstantMessageConfiguration instantMessageConfiguration, @PathParam("id") long id) {
    instantMessageConfigurationService.update(instantMessageConfiguration.withNewId(id));
  }

  @DELETE
  @Path("/configurations/{id}")
  public void delete(@PathParam("id") long id) {
    instantMessageConfigurationService.delete(id);
  }

  @GET
  @Path("/configurations/{id}")
  @PropertyFiltering
  public Optional<InstantMessageConfiguration> get(@PathParam("id") long id) {
    return instantMessageConfigurationService.get(id);
  }

  @GET
  @Path("/configurations/branches/{branchId}")
  @PropertyFiltering
  public Set<InstantMessageConfiguration> getAllWithBranchId(@PathParam("branchId") long branchId) {
    return instantMessageConfigurationService.getAllWithBranchId(branchId);
  }

  @GET
  @Path("/slack/list-channels")
  public List<SlackChannel> getChannels() throws IOException {
    return slackClient.getSlackChannels();
  }
}
