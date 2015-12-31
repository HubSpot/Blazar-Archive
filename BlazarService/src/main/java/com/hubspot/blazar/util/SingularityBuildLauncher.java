package com.hubspot.blazar.util;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.SingularityConfiguration;
import com.hubspot.horizon.AsyncHttpClient;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpRequest.Method;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.singularity.SingularityClientCredentials;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;

@Singleton
public class SingularityBuildLauncher {
  private final AsyncHttpClient asyncHttpClient;
  private final SingularityConfiguration singularityConfiguration;

  @Inject
  public SingularityBuildLauncher(AsyncHttpClient asyncHttpClient, BlazarConfiguration blazarConfiguration) {
    this.asyncHttpClient = asyncHttpClient;
    this.singularityConfiguration = blazarConfiguration.getSingularityConfiguration();
  }

  public synchronized HttpResponse launchBuild(ModuleBuild build) throws Exception {
    return asyncHttpClient.execute(buildRequest(build)).get();
  }

  private HttpRequest buildRequest(ModuleBuild build) {
    String buildId = String.valueOf(build.getId().get());

    return buildRequest(buildId, Collections.singletonList("hello"));
  }

  private HttpRequest buildRequest(String buildId, Object body) {
    Optional<SingularityClientCredentials> credentials = singularityConfiguration.getCredentials();

    HttpRequest.Builder builder = HttpRequest.newBuilder()
        .setMethod(Method.POST)
        .setUrl(buildUrl())
        .setQueryParam("runId").to(buildId)
        .setBody(body);

    if (credentials.isPresent()) {
      builder.addHeader(credentials.get().getHeaderName(), credentials.get().getToken());
    }

    return builder.build();
  }

  private String buildUrl() {
    String host = singularityConfiguration.getHost();
    String path = singularityConfiguration.getPath().or("singularity/api");
    String request = singularityConfiguration.getRequest();
    return String.format("http://%s/%s/requests/request/%s/run", host, path, request);
  }
}
