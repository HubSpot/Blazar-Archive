package com.hubspot.blazar.data;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Binder;
import com.google.inject.Module;
import io.dropwizard.db.DataSourceFactory;

public class BlazarDataTestModule implements Module {

  @Override
  public void configure(Binder binder) {
    binder.bind(DataSourceFactory.class).toInstance(buildDataSourceFactory());
    binder.bind(MetricRegistry.class).toInstance(new MetricRegistry());

    // TODO: EventBus, ObjectMapper
  }

  private DataSourceFactory buildDataSourceFactory() {
    final DataSourceFactory dataSourceFactory = new DataSourceFactory();
    dataSourceFactory.setDriverClass("org.h2.Driver");
    dataSourceFactory.setUrl("jdbc:h2:mem:blazar;DB_CLOSE_DELAY=-1;mode=MySQL;DATABASE_TO_UPPER=false");
    dataSourceFactory.setUser("user");
    dataSourceFactory.setPassword("password");

    return dataSourceFactory;
  }
}
