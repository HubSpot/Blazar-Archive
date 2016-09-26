package com.hubspot.blazar.test.base.data;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.hubspot.blazar.test.base.service.BlazarTestBase;
import com.mysql.management.MysqldResource;
import com.mysql.management.MysqldResourceI;
import com.zaxxer.hikari.HikariConfig;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;

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

    options.put("default-time-zone", "+00:00");  // otherwise the timezone will be the Tz of the host
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

    String jdbcUrlFormat = "jdbc:mysql://127.0.0.1:%d/%s?createDatabaseIfNotExist=true&autoReconnect=true&characterEncoding=UTF8&useSSL=false";
    String jdbcUrl = String.format(jdbcUrlFormat, mysqldResource.getPort(), "blazar");
    HikariConfig configuration = new HikariConfig();
    configuration.setJdbcUrl(jdbcUrl);
    configuration.setDriverClassName("com.mysql.jdbc.Driver");
    configuration.setUsername("root");
    configuration.setPassword("");
    ManagedDataSource dataSource = new HikariManagedDataSource(configuration);
    try {
      BlazarTestBase.runSql(dataSource, "schema.sql");
    } catch (Exception e) {
      LOG.error("Got exception setting up schema for database", e);
    }
    return dataSource;
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
}
