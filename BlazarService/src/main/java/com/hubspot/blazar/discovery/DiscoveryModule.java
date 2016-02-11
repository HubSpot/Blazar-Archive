package com.hubspot.blazar.discovery;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.hubspot.blazar.discovery.docker.DockerModuleDiscovery;
import com.hubspot.blazar.discovery.hubspotstatic.HubSpotStaticModuleDiscovery;
import com.hubspot.blazar.discovery.maven.MavenModuleDiscovery;
import com.hubspot.blazar.util.BlazarServiceLoader;

public class DiscoveryModule implements Module {

  @Override
  public void configure(Binder binder) {
    Multibinder<ModuleDiscovery> multibinder = Multibinder.newSetBinder(binder, ModuleDiscovery.class);
    multibinder.addBinding().to(MavenModuleDiscovery.class);
    multibinder.addBinding().to(DockerModuleDiscovery.class);
    multibinder.addBinding().to(HubSpotStaticModuleDiscovery.class);
    for (Class<? extends ModuleDiscovery> moduleDiscovery : BlazarServiceLoader.load(ModuleDiscovery.class)) {
      multibinder.addBinding().to(moduleDiscovery);
    }

    binder.bind(BlazarConfigModuleDiscovery.class);
    binder.bind(ModuleDiscovery.class).to(CompositeModuleDiscovery.class);
  }
}
