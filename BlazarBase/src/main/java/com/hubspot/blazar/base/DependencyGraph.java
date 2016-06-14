package com.hubspot.blazar.base;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

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

  public Set<Integer> getAllUpstreamNodes(int moduleId) {
    Stack<Integer> stack = new Stack<>();
    stack.push(moduleId);
    Set<Integer> seen = new HashSet<>();
    Set<Integer> allIncoming = new HashSet<>();
    while (!stack.empty()) {
      int i = stack.pop();
      if (seen.contains(i)) {
        continue;
      }
      seen.add(i);
      Set<Integer> incoming = incomingVertices(i);
      allIncoming.addAll(incoming);
      for (int each : incoming) {
        if (seen.contains(each)) {
          continue;
        }
        stack.add(each);
      }
    }
    return allIncoming;
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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DependencyGraph that = (DependencyGraph) o;
    return java.util.Objects.equals(transitiveReduction, that.transitiveReduction) &&
        java.util.Objects.equals(topologicalSort, that.topologicalSort);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(transitiveReduction, topologicalSort);
  }

  @Override
  public String toString() {
    return transitiveReduction.toString();
  }
}
