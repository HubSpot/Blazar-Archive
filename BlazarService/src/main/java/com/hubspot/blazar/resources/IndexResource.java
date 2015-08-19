package com.hubspot.blazar.resources;

import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;

@Singleton
@Path("/{wildcard:.*}")
@Produces(MediaType.TEXT_HTML)
public class IndexResource {
  private final byte[] html;

  @Inject
  public IndexResource() throws IOException {
    try (InputStream htmlStream = Resources.getResource("assets/index.html").openStream()) {
      this.html = ByteStreams.toByteArray(htmlStream);
    }
  }

  @GET
  public byte[] getIndex() {
    return html;
  }
}
