package com.hubspot.blazar.resources;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.notifications.InstantMessageConfiguration;
import com.hubspot.blazar.data.service.InstantMessageConfigurationService;
import com.hubspot.blazar.externalservice.slack.SlackChannel;
import com.hubspot.jackson.jaxrs.PropertyFiltering;
import com.ullink.slack.simpleslackapi.SlackSession;

@Path("/instant-message")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InstantMessageResource {

  private static final Logger LOG = LoggerFactory.getLogger(InstantMessageResource.class);
  @Inject(optional = true)
  private final InstantMessageConfigurationService instantMessageConfigurationService;
  private Optional<SlackSession> slackSession;

  @Inject
  public InstantMessageResource(InstantMessageConfigurationService instantMessageConfigurationService,
                                Optional<SlackSession> slackSession) {
    this.instantMessageConfigurationService = instantMessageConfigurationService;
    this.slackSession = slackSession;
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
  public Set<SlackChannel> getChannels() throws IOException {
    if (slackSession.isPresent()) {
      Collection<com.ullink.slack.simpleslackapi.SlackChannel> channels = slackSession.get().getChannels();
      Set<SlackChannel> ourChannels = new HashSet<>();
      for (com.ullink.slack.simpleslackapi.SlackChannel channel : channels) {
        ourChannels.add(new SlackChannel(channel.getId(), channel.getName()));
      }
      return ourChannels;
    } else {
      return Collections.emptySet();
    }
  }
}
