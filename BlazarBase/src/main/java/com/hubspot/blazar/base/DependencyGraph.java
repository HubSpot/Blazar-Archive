package com.hubspot.blazar.base;

import com.google.common.base.Objects;
import com.google.common.collect.Multimap;

import java.util.HashSet;
import java.util.Set;

public class DependencyGraph {
  private final Multimap<Integer, Integer> graph;

  public DependencyGraph(Multimap<Integer, Integer> graph) {
    this.graph = graph;
  }

  public Set<Integer> getDownstreamModules(int moduleId) {
    return new HashSet<>(graph.get(moduleId));
  }

  public Set<Integer> removeRedundantModules(Set<Integer> modules) {
    Set<Integer> trimmed = new HashSet<>(modules);
    for (int module : modules) {
      trimmed.removeAll(graph.get(module));
    }

    return trimmed;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("graph", graph).toString();
  }
}
