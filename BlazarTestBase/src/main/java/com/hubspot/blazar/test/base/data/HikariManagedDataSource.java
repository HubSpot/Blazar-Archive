package com.hubspot.blazar.test.base.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.dropwizard.db.ManagedDataSource;

public class HikariManagedDataSource extends HikariDataSource implements ManagedDataSource{

  public HikariManagedDataSource(HikariConfig configuration) {
    super(configuration);
  }

  @Override
  public void start() throws Exception {
    // no op
  }

  @Override
  public void stop() throws Exception {
    close();
  }
}
