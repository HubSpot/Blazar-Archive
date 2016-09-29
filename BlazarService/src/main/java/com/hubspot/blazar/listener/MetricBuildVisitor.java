package com.hubspot.blazar.listener;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.visitor.ModuleBuildVisitor;
import com.hubspot.blazar.base.visitor.RepositoryBuildVisitor;
import com.hubspot.blazar.data.service.CachingMetricsService;
import com.hubspot.blazar.util.SingularityBuildWatcher;

@Singleton
public class MetricBuildVisitor implements ModuleBuildVisitor, RepositoryBuildVisitor {

  private final MetricRegistry metricRegistry;
  private CachingMetricsService cachingMetricsService;

  @Inject
  public MetricBuildVisitor(MetricRegistry metricRegistry,
                            CachingMetricsService cachingMetricsService){
    this.metricRegistry = metricRegistry;
    this.cachingMetricsService = cachingMetricsService;
    // Ensures that there always are meters for these since they're important to alert on
    metricRegistry.register(SingularityBuildWatcher.class.getName() + ".succeeded", new Meter());
    metricRegistry.register(SingularityBuildWatcher.class.getName() + ".failed", new Meter());
    registerActiveBuildStateGauges();
  }

  public void visit(ModuleBuild moduleBuild) {
  }

  public void visit(RepositoryBuild repositoryBuild) {
  }


  private static String makeMetricName(Class<?> buildClass, Enum<?> state) {
    return buildClass.getName() + "." + state.name();
  }

  private void registerActiveBuildStateGauges() {

    for (ModuleBuild.State state : ModuleBuild.State.values()) {
      if (state.isRunning() || state.isWaiting()) {
        metricRegistry.register((state.getClass().getCanonicalName() + "." + state.toString() + ".count"), buildModuleStateGuage(state));
      }
    }

    for (RepositoryBuild.State state : RepositoryBuild.State.values()) {
      if (!state.isComplete()) {
        metricRegistry.register((state.getClass().getCanonicalName() + "." + state.toString() + ".count"), buildBranchStateGuage(state));
      }
    }
  }

  private Gauge<Number> buildBranchStateGuage(RepositoryBuild.State state) {
    return () -> cachingMetricsService.getCachedActiveBranchBuildCountByState(state);
  }

  private Gauge<Number> buildModuleStateGuage(ModuleBuild.State state) {
    return () -> cachingMetricsService.getCachedActiveModuleBuildCountByState(state);
  }
}
