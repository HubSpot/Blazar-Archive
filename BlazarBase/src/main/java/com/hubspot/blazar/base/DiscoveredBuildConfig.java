package com.hubspot.blazar.base;

import java.util.Objects;

public class DiscoveredBuildConfig {

  private final BuildConfig buildConfig;
  private final String path;
  private final String glob;

  public DiscoveredBuildConfig(BuildConfig buildConfig,
                               String path,
                               String glob) {

    this.buildConfig = buildConfig;
    this.path = path;
    this.glob = glob;
  }

  public BuildConfig getBuildConfig() {
    return buildConfig;
  }

  public String getPath() {
    return path;
  }

  public String getGlob() {
    return glob;
  }

  public String getFolder() {
    return path.contains("/") ? path.substring(0, path.lastIndexOf('/')) : "";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DiscoveredBuildConfig that = (DiscoveredBuildConfig) o;
    return Objects.equals(buildConfig, that.buildConfig) &&
        Objects.equals(path, that.getPath()) &&
        Objects.equals(glob, that.getGlob());
  }

  @Override
  public int hashCode() {
    return Objects.hash(buildConfig.hashCode(), path, glob);
  }
}
