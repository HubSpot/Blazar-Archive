package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.SetMultimap;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class DependencyGraph {
  private final SetMultimap<Integer, Integer> transitiveReduction;
  private final List<Integer> topologicalSort;

  @JsonCreator
  public DependencyGraph(@JsonProperty("transitiveReduction") SetMultimap<Integer, Integer> transitiveReduction,
                         @JsonProperty("topologicalSort") List<Integer> topologicalSort) {
    this.transitiveReduction = transitiveReduction;
    this.topologicalSort = topologicalSort;
  }

  public SetMultimap<Integer, Integer> getTransitiveReduction() {
    return transitiveReduction;
  }

  public List<Integer> getTopologicalSort() {
    return topologicalSort;
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
