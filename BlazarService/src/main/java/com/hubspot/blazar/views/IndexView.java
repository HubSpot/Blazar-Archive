package com.hubspot.blazar.views;

import com.hubspot.blazar.config.BlazarConfiguration;

import io.dropwizard.server.SimpleServerFactory;
import io.dropwizard.views.View;

public class IndexView extends View {
  private final String appRoot;
  private final String staticRoot;
  private final String apiRoot;
  private final int buildsRefresh;

  public IndexView(BlazarConfiguration configuration) {
    super("index.mustache");

    if (configuration.getUiConfiguration().getBaseUri().isPresent()) {
      appRoot = configuration.getUiConfiguration().getBaseUri().get();
    } else {
      final String rawAppRoot = ((SimpleServerFactory) configuration.getServerFactory()).getApplicationContextPath();
      appRoot = !rawAppRoot.endsWith("/") ? rawAppRoot : rawAppRoot.substring(0, rawAppRoot.length() - 1);
    }

    buildsRefresh = configuration.getUiConfiguration().getBuildsRefresh();
    apiRoot = configuration.getUiConfiguration().getApiRoot().or(appRoot);
    staticRoot = configuration.getUiConfiguration().getStaticPath();
  }

  public String getStaticRoot() {
    return staticRoot;
  }

  public String getAppRoot() {
    return appRoot;
  }

  public int getBuildsRefresh() {
    return buildsRefresh;
  }

  public String getApiRoot() {
    return apiRoot;
  }
}
