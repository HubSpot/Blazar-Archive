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

  public Set<Module> reduceModules(Set<Module> modules) {
    Set<Module> reduced = new HashSet<>(modules);
    for (Module source : modules) {
      for (Module target : modules) {
        if (paths.get(source.getId().get()).contains(target.getId().get())) {
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
