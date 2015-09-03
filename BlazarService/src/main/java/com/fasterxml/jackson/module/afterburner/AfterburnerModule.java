package com.fasterxml.jackson.module.afterburner;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;

public class AfterburnerModule extends Module implements java.io.Serializable {

  @Override
  public String getModuleName() {
    return getClass().getSimpleName();
  }

  @Override
  public Version version() {
    return Version.unknownVersion();
  }

  @Override
  public void setupModule(SetupContext context) {}
}
