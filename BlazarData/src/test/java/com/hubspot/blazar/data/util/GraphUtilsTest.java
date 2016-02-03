package com.hubspot.blazar.data.util;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import org.junit.Test;

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

  private static final SetMultimap<Integer, Integer> PATHS = ImmutableSetMultimap.<Integer, Integer>builder()
      .put(1, 2)
      .put(1, 3)
      .put(1, 4) // 1 -> 3 -> 4
      .put(1, 5) // 1 -> 3 -> 5
      .put(3, 2) // 3 -> 5 -> 2
      .put(3, 4)
      .put(3, 5)
      .put(4, 2) // 4 -> 5 -> 2
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

  @Test
  public void testFindAllPaths() {
    assertThat(GraphUtils.INSTANCE.findAllPaths(EDGES).asMap()).isEqualTo(PATHS.asMap());
  }

  @Test
  public void testTransitiveReduction() {
    assertThat(GraphUtils.INSTANCE.transitiveReduction(PATHS).asMap()).isEqualTo(REDUCED.asMap());
  }
}
