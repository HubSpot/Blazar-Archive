package com.hubspot.blazar.listener;

import java.util.SortedMap;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.visitor.ModuleBuildVisitor;

@Singleton
public class MetricModuleBuildVisitor implements ModuleBuildVisitor {

  private static final String METRIC_NAME_BASE = "com.hubspot.moduleBuild.";
  private final MetricRegistry metricRegistry;

  @Inject
  public MetricModuleBuildVisitor(MetricRegistry metricRegistry){
    this.metricRegistry = metricRegistry;
    for (ModuleBuild.State state : ModuleBuild.State.values()) {
      metricRegistry.register(getMetricName(state), new Meter());
    }
  }

  public void visit(ModuleBuild moduleBuild) {
    SortedMap<String, Meter> meters = metricRegistry.getMeters();
    meters.get(getMetricName(moduleBuild.getState())).mark();
  }

  private String getMetricName(ModuleBuild.State state) {
    return METRIC_NAME_BASE + state.toString().toLowerCase();
  }
}
