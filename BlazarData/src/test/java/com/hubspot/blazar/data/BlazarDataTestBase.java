package com.hubspot.blazar.data;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.db.ManagedDataSource;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.junit.After;
import org.junit.BeforeClass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

public abstract class BlazarDataTestBase {
  private static final AtomicReference<Injector> injector = new AtomicReference<>();

  @BeforeClass
  public static void setup() throws Exception {
    synchronized (injector) {
      if (injector.get() == null) {
        injector.set(Guice.createInjector(new BlazarDataTestModule(), new BlazarDataModule()));

        initializeEmptyTables();
      }
    }
  }

  @After
  public void cleanup() throws Exception {
    initializeEmptyTables();
  }

  protected <T> T getFromGuice(Class<T> type) {
    return injector.get().getInstance(type);
  }

  private static void initializeEmptyTables() throws Exception {
    try (Connection connection = getConnection()) {
      ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
      JdbcConnection jdbcConnection = new JdbcConnection(connection);
      Liquibase liquibase = new Liquibase("schema.sql", resourceAccessor, jdbcConnection);
      liquibase.update(new Contexts());
    }
  }

  private static Connection getConnection() throws SQLException {
    return injector.get().getInstance(ManagedDataSource.class).getConnection();
  }
}
