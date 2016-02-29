package com.hubspot.blazar.listener;

import java.util.SortedMap;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.RepositoryBuildVisitor;

@Singleton
public class MetricRepositoryBuildVisitor implements RepositoryBuildVisitor {

  private static final String METRIC_NAME_BASE = "com.hubspot.moduleBuild.";
  private final MetricRegistry metricRegistry;

  @Inject
  public MetricRepositoryBuildVisitor(MetricRegistry metricRegistry) {
    this.metricRegistry = metricRegistry;
    for (RepositoryBuild.State state : RepositoryBuild.State.values()) {
      metricRegistry.register("com.hubspot.repoBuild." + state.toString().toLowerCase(), new Meter());
    }
  }

  public void visit(RepositoryBuild repositoryBuild) {
    SortedMap<String, Meter> meters = metricRegistry.getMeters();
    meters.get(getMetricName(repositoryBuild.getState())).mark();
  }

  private String getMetricName(RepositoryBuild.State state) {
    return METRIC_NAME_BASE + state.toString().toLowerCase();
  }
}
