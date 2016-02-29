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
  }

  public void visit(ModuleBuild moduleBuild) {
    SortedMap<String, Meter> meters = metricRegistry.getMeters();
    meters.get(moduleBuild.getClass().getName() + "." + moduleBuild.getState().name().toLowerCase()).mark();
  }

  public void visit(RepositoryBuild repositoryBuild) {
    SortedMap<String, Meter> meters = metricRegistry.getMeters();
    meters.get(repositoryBuild.getClass().getName() + "." + repositoryBuild.getState().name().toLowerCase()).mark();
  }
}
