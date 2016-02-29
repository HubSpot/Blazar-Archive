package com.hubspot.blazar.listener;

import java.util.SortedMap;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.ModuleBuildVisitor;
import com.hubspot.blazar.base.visitor.RepositoryBuildVisitor;

@Singleton
public class MetricBuildVisitor implements ModuleBuildVisitor, RepositoryBuildVisitor {

  private final MetricRegistry metricRegistry;

  @Inject
  public MetricBuildVisitor(MetricRegistry metricRegistry){
    this.metricRegistry = metricRegistry;
    for (ModuleBuild.State state : ModuleBuild.State.values()) {
      metricRegistry.register(makeMetricName(ModuleBuild.class, state), new Meter());
    }

    for (RepositoryBuild.State state : RepositoryBuild.State.values()) {
      metricRegistry.register(makeMetricName(RepositoryBuild.class, state), new Meter());
    }
  }

  public void visit(ModuleBuild moduleBuild) {
    SortedMap<String, Meter> meters = metricRegistry.getMeters();
    meters.get(makeMetricName(ModuleBuild.class, moduleBuild.getState())).mark();
  }

  public void visit(RepositoryBuild repositoryBuild) {
    SortedMap<String, Meter> meters = metricRegistry.getMeters();
    meters.get(makeMetricName(RepositoryBuild.class, repositoryBuild.getState())).mark();
  }


  private static String makeMetricName(Class<?> buildClass, Enum<?> state) {
    return buildClass.getName() + "." + state.name();
  }
}
