package com.hubspot.blazar.base;

import static org.assertj.core.api.Assertions.assertThat;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.hubspot.blazar.base.externalservice.mesos.Resources;

@RunWith(JukitoRunner.class)
@UseModules({BlazarBaseTestModule.class})
public class ResourcesTest {
  private static double TEST_CPUS = 1.0;
  private static double TEST_RAM_MB = 5.0;

  @Test
  public void testResourceConversion() {
    Resources original = makeTestResources();
    com.hubspot.mesos.Resources mesosResources = Resources.toMesosResources(original);
    assertThat(mesosResources.getCpus()).isEqualTo(TEST_CPUS);
    assertThat(mesosResources.getMemoryMb()).isEqualTo(TEST_RAM_MB);
    assertThat(mesosResources.getNumPorts()).isEqualTo(0);
    assertThat(mesosResources.getDiskMb()).isEqualTo(0);
  }

  @Test
  public void testResourceAdd() {
    Resources original = makeTestResources();
    Resources added = Resources.add(original, new Resources(1.0, 1.0));
    assertThat(added.getCpus()).isEqualTo(TEST_CPUS + 1.0);
    assertThat(added.getMemoryMb()).isEqualTo(TEST_RAM_MB + 1.0);
  }

  private static Resources makeTestResources() {
    return new Resources(TEST_CPUS, TEST_RAM_MB);
  }
}
