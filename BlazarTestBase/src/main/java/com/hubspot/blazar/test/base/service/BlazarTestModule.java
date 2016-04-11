package com.hubspot.blazar.test.base.service;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jackson.Jackson;

public class BlazarTestModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(DataSourceFactory.class).toInstance(buildDataSourceFactory());
    bind(MetricRegistry.class).toInstance(new MetricRegistry());
    bind(ObjectMapper.class).toInstance(buildObjectMapper());
  }

  private DataSourceFactory buildDataSourceFactory() {
    final DataSourceFactory dataSourceFactory = new DataSourceFactory();
    dataSourceFactory.setDriverClass("org.h2.Driver");
    dataSourceFactory.setUrl("jdbc:h2:mem:blazar;DB_CLOSE_DELAY=-1;mode=MySQL");
    dataSourceFactory.setUser("user");
    dataSourceFactory.setPassword("password");

    return dataSourceFactory;
  }

  private ObjectMapper buildObjectMapper() {
    return Jackson.newObjectMapper()
        .registerModule(new ProtobufModule())
        .setSerializationInclusion(Include.NON_NULL)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
  }
}
