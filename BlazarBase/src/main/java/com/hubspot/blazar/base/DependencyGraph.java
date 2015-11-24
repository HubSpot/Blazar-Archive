package com.hubspot.blazar.base;

import com.google.common.collect.SetMultimap;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class DependencyGraph {
  private final SetMultimap<Integer, Integer> transitiveReduction;

  public DependencyGraph(SetMultimap<Integer, Integer> transitiveReduction) {
    this.transitiveReduction = transitiveReduction;
  }

  public Set<Integer> incomingVertices(int moduleId) {
    Set<Integer> incomingVertices = new HashSet<>();
    for (Entry<Integer, Integer> path : transitiveReduction.entries()) {
      if (path.getValue() == moduleId) {
        incomingVertices.add(path.getKey());
      }
    }

    return incomingVertices;
  }

  public Set<Integer> reachableVertices(int moduleId) {
    Set<Integer> reachableVertices = new HashSet<>();
    for (int vertex : outgoingVertices(moduleId)) {
      reachableVertices.add(vertex);
      reachableVertices.addAll(reachableVertices(vertex));
    }

    return reachableVertices;
  }

  public Set<Integer> outgoingVertices(int moduleId) {
    return transitiveReduction.get(moduleId);
  }

  @Override
  public String toString() {
    return transitiveReduction.toString();
  }
}
