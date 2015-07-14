package com.hubspot.blazar;

import com.google.inject.Inject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/test")
@Produces(MediaType.APPLICATION_JSON)
public class TestResource {

  @Inject
  public TestResource() {}

  @GET
  public String test() {
    return "hello world!";
  }
}
