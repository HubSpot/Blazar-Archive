package com.hubspot.blazar.data;

import com.google.inject.AbstractModule;

public class BlazarDataModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new BlazarDaoModule());
  }
}
