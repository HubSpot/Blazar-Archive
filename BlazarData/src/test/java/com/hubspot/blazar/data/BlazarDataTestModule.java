package com.hubspot.blazar.data;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.jackson.Jackson;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

import javax.inject.Inject;
import java.sql.Connection;

public class BlazarDataTestModule implements Module {

  @Override
  public void configure(Binder binder) {
    binder.bind(DataSourceFactory.class).toInstance(buildDataSourceFactory());
    binder.bind(MetricRegistry.class).toInstance(new MetricRegistry());
    binder.bind(ObjectMapper.class).toInstance(buildObjectMapper());
    binder.bind(EventBus.class).toInstance(buildEventBus());
    binder.bind(InitializeSchema.class).asEagerSingleton();
  }

  public static class InitializeSchema {

    @Inject
    public InitializeSchema(ManagedDataSource dataSource) throws Exception {
      try (Connection connection = dataSource.getConnection()) {
        ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
        JdbcConnection jdbcConnection = new JdbcConnection(connection);
        Liquibase liquibase = new Liquibase("schema.sql", resourceAccessor, jdbcConnection);
        liquibase.update(new Contexts());
      }
    }
  }

  private DataSourceFactory buildDataSourceFactory() {
    final DataSourceFactory dataSourceFactory = new DataSourceFactory();
    dataSourceFactory.setDriverClass("org.h2.Driver");
    dataSourceFactory.setUrl("jdbc:h2:mem:blazar;DB_CLOSE_DELAY=-1;mode=MySQL;DATABASE_TO_UPPER=false");
    dataSourceFactory.setUser("user");
    dataSourceFactory.setPassword("password");

    return dataSourceFactory;
  }

  private ObjectMapper buildObjectMapper() {
    return Jackson.newObjectMapper()
        .registerModule(new ProtobufModule())
        .setSerializationInclusion(Include.NON_NULL)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private EventBus buildEventBus() {
    return new EventBus() {

      @Override
      public void post(Object event) {
        // TODO
      }
    };
  }
}
