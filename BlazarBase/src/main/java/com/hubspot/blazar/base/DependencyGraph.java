package com.hubspot.blazar.base;

import com.google.common.collect.SetMultimap;

import java.util.HashSet;
import java.util.Set;

public class DependencyGraph {
  private final SetMultimap<Integer, Integer> transitiveReduction;
  private final SetMultimap<Integer, Integer> paths;

  public DependencyGraph(SetMultimap<Integer, Integer> transitiveReduction, SetMultimap<Integer, Integer> paths) {
    this.transitiveReduction = transitiveReduction;
    this.paths = paths;
  }

  public Set<Integer> reachableVertices(int moduleId) {
    return transitiveReduction.get(moduleId);
  }

  public Set<Integer> reduceVertices(Set<Integer> modules) {
    Set<Integer> reduced = new HashSet<>(modules);
    for (int source : modules) {
      for (int target : modules) {
        if (paths.get(source).contains(target)) {
          reduced.remove(target);
        }
      }
    }

    return reduced;
  }

  @Override
  public String toString() {
    return transitiveReduction.toString();
  }
}
