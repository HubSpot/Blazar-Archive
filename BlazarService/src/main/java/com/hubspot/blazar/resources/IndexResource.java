package com.hubspot.blazar.resources;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.views.IndexView;

@Singleton
@Path("/{wildcard:.*}")
@Produces(MediaType.TEXT_HTML)
public class IndexResource {
  private final IndexView indexView;

  @Inject
  public IndexResource(BlazarConfiguration configuration) {
    this.indexView = new IndexView(configuration);
  }

  @GET
  public IndexView getIndex() {
    return indexView;
  }
}
