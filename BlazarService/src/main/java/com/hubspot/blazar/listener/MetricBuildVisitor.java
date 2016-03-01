package com.hubspot.blazar.listener;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.ModuleBuildVisitor;
import com.hubspot.blazar.base.visitor.RepositoryBuildVisitor;
import com.hubspot.blazar.util.SingularityBuildWatcher;

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
    // Ensures that there always are meters for these since they're important to alert on
    metricRegistry.register(SingularityBuildWatcher.class.getName() + ".succeeded", new Meter());
    metricRegistry.register(SingularityBuildWatcher.class.getName() + ".failed", new Meter());
  }

  public void visit(ModuleBuild moduleBuild) {
    metricRegistry.meter(makeMetricName(ModuleBuild.class, moduleBuild.getState())).mark();
  }

  public void visit(RepositoryBuild repositoryBuild) {
    metricRegistry.meter(makeMetricName(RepositoryBuild.class, repositoryBuild.getState())).mark();
  }


  private static String makeMetricName(Class<?> buildClass, Enum<?> state) {
    return buildClass.getName() + "." + state.name();
  }
}
