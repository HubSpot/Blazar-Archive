package com.hubspot.blazar.data;

import com.google.inject.Inject;
import com.hubspot.guice.transactional.TransactionalDataSource;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;

import javax.inject.Provider;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class DBIProvider implements Provider<DBI> {
  private final TransactionalDataSource transactionalDataSource;
  private final ManagedDataSource managedDataSource;
  private final DataSourceFactory dataSourceFactory;
  private final Environment environment;

  @Inject
  public DBIProvider(TransactionalDataSource transactionalDataSource,
                     ManagedDataSource managedDataSource,
                     DataSourceFactory dataSourceFactory,
                     Environment environment) {
    this.transactionalDataSource = transactionalDataSource;
    this.managedDataSource = managedDataSource;
    this.dataSourceFactory = dataSourceFactory;
    this.environment = environment;
  }

  @Override
  public DBI get() {
    return new DBIFactory().build(environment, dataSourceFactory, frankenDataSource(), "db");
  }

  private ManagedDataSource frankenDataSource() {
    return new ManagedDataSource() {

      @Override
      public Connection getConnection() throws SQLException {
        return transactionalDataSource.getConnection();
      }

      @Override
      public Connection getConnection(String username, String password) throws SQLException {
        return transactionalDataSource.getConnection(username, password);
      }

      @Override
      public PrintWriter getLogWriter() throws SQLException {
        return transactionalDataSource.getLogWriter();
      }

      @Override
      public void setLogWriter(PrintWriter out) throws SQLException {
        transactionalDataSource.setLogWriter(out);
      }

      @Override
      public void setLoginTimeout(int seconds) throws SQLException {
        transactionalDataSource.setLoginTimeout(seconds);
      }

      @Override
      public int getLoginTimeout() throws SQLException {
        return transactionalDataSource.getLoginTimeout();
      }

      @Override
      public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
      }

      @Override
      public void start() throws Exception {
        managedDataSource.start();
      }

      @Override
      public void stop() throws Exception {
        managedDataSource.stop();
      }

      @Override
      public <T> T unwrap(Class<T> iface) throws SQLException {
        return transactionalDataSource.unwrap(iface);
      }

      @Override
      public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return transactionalDataSource.isWrapperFor(iface);
      }
    };
  }
}
