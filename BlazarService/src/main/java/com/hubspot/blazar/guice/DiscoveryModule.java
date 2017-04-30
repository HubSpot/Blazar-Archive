package com.hubspot.blazar.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.hubspot.blazar.util.BlazarServiceLoader;

public class DiscoveryModule implements Module {
  private static final Logger LOG = LoggerFactory.getLogger(DiscoveryModule.class);

  @Override
  public void configure(Binder binder) {
    Multibinder<ModuleDiscovery> multibinder = Multibinder.newSetBinder(binder, ModuleDiscovery.class);
    for (Class<? extends ModuleDiscovery> moduleDiscovery : BlazarServiceLoader.load(ModuleDiscovery.class)) {
      LOG.info("Found module discovery: " + moduleDiscovery);
      multibinder.addBinding().to(moduleDiscovery);
    }

    binder.bind(BlazarConfigModuleDiscovery.class);
    binder.bind(ModuleDiscovery.class).to(CompositeModuleDiscovery.class);
  }
}
