package com.hubspot.blazar.data.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphUtilsTest {
  private static final SetMultimap<Integer, Integer> EDGES = ImmutableSetMultimap.<Integer, Integer>builder()
      .put(1, 2)
      .put(1, 3)
      .put(3, 4)
      .put(3, 5)
      .put(4, 5)
      .put(5, 2)
      .build();

  private static final SetMultimap<Integer, Integer> REDUCED = ImmutableSetMultimap.<Integer, Integer>builder()
      //.put(1, 2) Not in transitive reduction, handled by 1 -> 3 -> 4 -> 5 -> 2
      .put(1, 3)
      .put(3, 4)
      //.put(3, 5) Not in transitive reduction, handled by 3 -> 4 -> 5
      .put(4, 5)
      .put(5, 2)
      .build();

  private static final SetMultimap<Integer, Integer> RETAINED = ImmutableSetMultimap.<Integer, Integer>builder()
      .put(1, 5)
      .build();

  private static final SetMultimap<Integer, Integer> BLAZAR = ImmutableSetMultimap.<Integer, Integer>builder()
      .put(278, 280)
      .put(280, 277)
      .put(280, 281)
      .put(280, 279)
      .put(279, 276)
      .build();

  @Test
  public void testTransitiveReduction() {
    assertThat(GraphUtils.INSTANCE.transitiveReduction(EDGES).asMap()).isEqualTo(REDUCED.asMap());
  }

  @Test
  public void testTopologicalSort() {
    assertThat(GraphUtils.INSTANCE.topologicalSort(REDUCED)).isEqualTo(Arrays.asList(1, 3, 4, 5, 2));
  }

  @Test
  public void testBlazarTopologicalSort() {
    assertThat(GraphUtils.INSTANCE.topologicalSort(BLAZAR)).isEqualTo(Arrays.asList(278, 280, 277, 279, 281, 276));
  }

  @Test
  public void testRetain() {
    assertThat(GraphUtils.INSTANCE.retain(EDGES, ImmutableSet.of(1, 5)).asMap()).isEqualTo(RETAINED.asMap());
  }
}
