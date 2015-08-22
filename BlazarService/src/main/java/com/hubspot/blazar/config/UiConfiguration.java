package com.hubspot.blazar.config;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.google.common.base.Optional;

public class UiConfiguration {
  public static final String DEFAULT_STATIC_PATH = "static/";

  @NotNull
  private Optional<String> baseUri = Optional.absent();

  @Min(0)
  private int buildsRefresh = 5000;

  @NotNull
  private String staticPath = DEFAULT_STATIC_PATH;

  @NotNull
  private Optional<String> apiRoot = Optional.absent();

  public Optional<String> getBaseUri() {
    return baseUri;
  }

  public void setBaseUri(Optional<String> baseUri) {
    this.baseUri = baseUri;
  }

  public int getBuildsRefresh() {
    return buildsRefresh;
  }

  public void setBuildsRefresh(int buildsRefresh) {
    this.buildsRefresh = buildsRefresh;
  }

  public String getStaticPath() {
    return staticPath;
  }

  public UiConfiguration setStaticPath(String staticPath) {
    this.staticPath = staticPath;
    return this;
  }

  public Optional<String> getApiRoot() {
    return apiRoot;
  }

  public UiConfiguration setApiRoot(Optional<String> apiRoot) {
    this.apiRoot = apiRoot;
    return this;
  }
}
