package com.hubspot.blazar.base.externalservice.mesos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Resources {

  private final double cpus;
  private final double memoryMb;

  public static com.hubspot.mesos.Resources toMesosResources(Resources resources) {
    return new com.hubspot.mesos.Resources(resources.getCpus(), resources.getMemoryMb(), 0);
  }

  private static Resources fromMesosResources(com.hubspot.mesos.Resources resources) {
    return new Resources(resources.getCpus(), resources.getMemoryMb());
  }

  public static Resources add(Resources a, Resources b) {
    return fromMesosResources(com.hubspot.mesos.Resources.add(toMesosResources(a), toMesosResources(b)));
  }

  /**
   * Blazar's view of mesos resources, currently we do not care about ports or disk, so we leave those fields at 0
   * when converting to Mesos Resources  {@link Resources#toMesosResources(Resources) toMesosResources(Resources)}
   * @param cpus number of cpu
   * @param memoryMb mb ram for build
   */
  @JsonCreator
  public Resources(@JsonProperty("cpus") double cpus, @JsonProperty("memoryMb") double memoryMb) {
    this.cpus = cpus;
    this.memoryMb = memoryMb;
  }

  public double getCpus() {
    return cpus;
  }

  public double getMemoryMb() {
    return memoryMb;
  }
}
