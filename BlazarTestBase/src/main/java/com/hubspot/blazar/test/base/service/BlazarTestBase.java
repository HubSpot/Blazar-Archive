package com.hubspot.blazar.test.base.service;

import java.sql.Connection;

import javax.sql.DataSource;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.ext.logging.slf4j.Slf4jLogger;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

public abstract class BlazarTestBase {

  protected static void runSql(DataSource dataSource, String resourceName) throws Exception {
    liquibase.logging.LogFactory.setInstance(new LogFactory() {

      @Override
      public Logger getLog(String name) {
        liquibase.logging.Logger log = new Slf4jLogger();
        log.setName(name);
        return log;
      }
    });

    try (Connection connection = dataSource.getConnection()) {
      ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
      JdbcConnection jdbcConnection = new JdbcConnection(connection);
      Liquibase liquibase = new Liquibase(resourceName, resourceAccessor, jdbcConnection);
      liquibase.update(new Contexts());
    }
  }
}
