package com.hubspot.blazar.data.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jackson.Jackson;

public class BlazarTestModule implements Module {
  private static final Logger LOG = LoggerFactory.getLogger(BlazarTestModule.class);

  @Override
  public void configure(Binder binder) {
    binder.bind(DataSourceFactory.class).toInstance(buildDataSourceFactory());
    binder.bind(MetricRegistry.class).toInstance(new MetricRegistry());
    binder.bind(ObjectMapper.class).toInstance(buildObjectMapper());
    binder.bind(EventBus.class).toInstance(buildEventBus());
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

  private EventBus buildEventBus() {
    return new EventBus() {

      @Override
      public void post(Object event) {
        LOG.debug("Got event {}", event);
      }
    };
  }
}
