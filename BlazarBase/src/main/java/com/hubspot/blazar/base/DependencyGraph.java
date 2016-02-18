package com.hubspot.blazar.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DependencyGraph {
  private final Map<Integer, Set<Integer>> transitiveReduction;
  private final List<Integer> topologicalSort;

  @JsonCreator
  public DependencyGraph(@JsonProperty("transitiveReduction") Map<Integer, Set<Integer>> transitiveReduction,
                         @JsonProperty("topologicalSort") List<Integer> topologicalSort) {
    this.transitiveReduction = transitiveReduction;
    this.topologicalSort = topologicalSort;
  }

  public Map<Integer, Set<Integer>> getTransitiveReduction() {
    return transitiveReduction;
  }

  public List<Integer> getTopologicalSort() {
    return topologicalSort;
  }

  public Set<Integer> incomingVertices(int moduleId) {
    Set<Integer> incomingVertices = new HashSet<>();
    for (Entry<Integer, Set<Integer>> edgeSet : transitiveReduction.entrySet()) {
      int source = edgeSet.getKey();
      for (int target : edgeSet.getValue()) {
        if (target == moduleId) {
          incomingVertices.add(source);
        }
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
    return Objects.firstNonNull(transitiveReduction.get(moduleId), Collections.<Integer>emptySet());
  }

  @Override
  public String toString() {
    return transitiveReduction.toString();
  }
}
