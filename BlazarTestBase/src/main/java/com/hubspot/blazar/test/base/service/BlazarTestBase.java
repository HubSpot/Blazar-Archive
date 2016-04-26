package com.hubspot.blazar.test.base.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import com.google.inject.Injector;
import io.dropwizard.db.ManagedDataSource;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.ext.logging.slf4j.Slf4jLogger;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

public abstract class BlazarTestBase {
  protected static final AtomicReference<Injector> injector = new AtomicReference<>();



  protected <T> T getFromGuice(Class<T> type) {
    return injector.get().getInstance(type);
  }

  private static Connection getConnection() throws SQLException {
    return injector.get().getInstance(ManagedDataSource.class).getConnection();
  }

  protected static void runSql(String resourceName) throws Exception {
    liquibase.logging.LogFactory.setInstance(new LogFactory() {

      @Override
      public Logger getLog(String name) {
        liquibase.logging.Logger log = new Slf4jLogger();
        log.setName(name);
        return log;
      }
    });
    try (Connection connection = getConnection()) {
      ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
      JdbcConnection jdbcConnection = new JdbcConnection(connection);
      Liquibase liquibase = new Liquibase(resourceName, resourceAccessor, jdbcConnection);
      liquibase.update(new Contexts());
    }
  }
}
