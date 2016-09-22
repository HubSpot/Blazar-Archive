package com.hubspot.blazar.test.base.data;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.time.Duration;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Throwables;
import com.mysql.management.MysqldResource;
import com.mysql.management.MysqldResourceI;
import com.zaxxer.hikari.HikariConfig;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.ext.logging.slf4j.Slf4jLogger;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

public class HikariDataSourceFactory extends DataSourceFactory {
  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(HikariDataSourceFactory.class);

  @Override
  public ManagedDataSource build(MetricRegistry metricRegistry, String name) {

    final Path baseDir = Paths.get(System.getProperty("java.io.tmpdir"), "mysqld_" + UUID.randomUUID().toString());
    int port = new Random().nextInt(20000) + 3207;

    final MysqldResource mysqldResource = new MysqldResource(baseDir.toFile());

    Map<String, String> options = new HashMap<>();
    options.put(MysqldResourceI.PORT, Integer.toString(port));
    options.put("character-set-server", "utf8");

    /**
     *  This forces the embedded mysql used for tests to have the same timezone offset
     *  as the parent java process. This fixes things when you're running
     *  locally and java thinks its in UTC but mysql uses the system timezone which is not UTC.
     *
     *  This also avoids having to install and load TimeZone conversion tables to allow mysql
     *  to convert `UTC` to `+00:00`. If they are installed into the embedded mysql then you
     *  can simplify the below to:
     *
     *  options.put("default-time-zone", Calendar.getInstance().getTimeZone().getID());
     */

    Duration offset = Duration.ofMillis(Calendar.getInstance().getTimeZone().getRawOffset());
    long hours = offset.toHours();
    long minutes = offset.minusHours(hours).toMinutes();
    // force the + on '+00' for positive offset
    String mysqlOffset = 0 >= hours ? String.format("+%02d:%02d", hours, minutes) : String.format("%02d:%02d", hours, minutes);
    options.put("default-time-zone", mysqlOffset);
    options.put("sql-mode", "ONLY_FULL_GROUP_BY,NO_AUTO_VALUE_ON_ZERO,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION");
    mysqldResource.start("embedded-mysql", options);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      LOG.info("Shutting down embedded MySQL");
      mysqldResource.shutdown();
      try {
        deleteDirRecursively(baseDir);
      } catch (IOException e) {
        LOG.error("Error deleting embedded MySQL directory", e);
      }
    }));

    if (!mysqldResource.isRunning()) {
      throw new RuntimeException("Unable start embedded MySQL");
    }

    LOG.info("Singleton Embedded MySQL started successfully");

    String jdbcUrlFormat = "jdbc:mysql://127.0.0.1:%d/%s?createDatabaseIfNotExist=true&autoReconnect=true&characterEncoding=UTF8";
    String jdbcUrl = String.format(jdbcUrlFormat, mysqldResource.getPort(), "blazar");
    HikariConfig configuration = new HikariConfig();
    configuration.setJdbcUrl(jdbcUrl);
    configuration.setDriverClassName("com.mysql.jdbc.Driver");
    configuration.setUsername("root");
    configuration.setPassword("");

    HikariManagedDataSource datasource = new HikariManagedDataSource(configuration);
    try {
      runSql(datasource, "schema.sql");
    } catch (Exception e) {
      Throwables.propagate(e);
    }
    return datasource;
  }

  private static void deleteDirRecursively(Path dir) throws IOException {
    Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  private void runSql(DataSource dataSource, String resourceName) throws Exception {
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
