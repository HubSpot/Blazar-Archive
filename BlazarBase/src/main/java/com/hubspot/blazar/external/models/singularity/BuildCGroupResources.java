package com.hubspot.blazar.external.models.singularity;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class BuildCGroupResources {

  private final double cpus;
  private final double memoryMb;

  private static BuildCGroupResources fromMesosResources(com.hubspot.mesos.Resources resources) {
    return new BuildCGroupResources(resources.getCpus(), resources.getMemoryMb());
  }

  /**
   * Blazar's view of mesos resources, currently we do not care about ports or disk, so we leave those fields at 0
   * when converting to Mesos Resources  {@link BuildCGroupResources#toMesosResources(BuildCGroupResources) toMesosResources(Resources)}
   * @param cpus number of cpu
   * @param memoryMb mb ram for build
   */
  @JsonCreator
  public BuildCGroupResources(@JsonProperty("cpus") double cpus, @JsonProperty("memoryMb") double memoryMb) {
    this.cpus = cpus;
    this.memoryMb = memoryMb;
  }

  public double getCpus() {
    return cpus;
  }

  public double getMemoryMb() {
    return memoryMb;
  }

  public BuildCGroupResources add(BuildCGroupResources other) {
    return new BuildCGroupResources(other.getCpus() + cpus, other.getMemoryMb() + memoryMb);
  }

  public com.hubspot.mesos.Resources toMesosResources() {
    return new com.hubspot.mesos.Resources(cpus, memoryMb, 0);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BuildCGroupResources that = (BuildCGroupResources) o;
    return Objects.equals(that.cpus, cpus) && Objects.equals(that.memoryMb, memoryMb);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cpus, memoryMb);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("cpus", cpus)
        .add("memoryMb", memoryMb)
        .toString();
  }
}
