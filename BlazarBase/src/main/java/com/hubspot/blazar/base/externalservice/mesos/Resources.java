package com.hubspot.blazar.base.externalservice.mesos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Resources {

  private final double cpus;
  private final double memoryMb;

  public static com.hubspot.mesos.Resources toMesosResources(Resources resources) {
    return new com.hubspot.mesos.Resources(resources.getCpus(), resources.getMemoryMb(), 0);
  }

  public static Resources buildDefaultResources() {
    return new Resources(1, 2304);
  }

  @JsonCreator
  public Resources(@JsonProperty("cpus") double cpus,
                   @JsonProperty("memoryMb") double memoryMb) {
    this.cpus = cpus;
    this.memoryMb = memoryMb;
  }

  public double getCpus() {
    return cpus;
  }

  public double getMemoryMb() {
    return memoryMb;
  }

  public com.hubspot.mesos.Resources toMesosResources() {
    return toMesosResources(this);
  }
}
