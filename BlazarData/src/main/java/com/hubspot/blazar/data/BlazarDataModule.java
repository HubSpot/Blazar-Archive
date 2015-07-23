package com.hubspot.blazar.data;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.hubspot.blazar.data.dao.BuildDefinitionDao;
import com.hubspot.guice.transactional.TransactionalDataSource;
import com.hubspot.guice.transactional.TransactionalModule;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;

import javax.inject.Singleton;

public class BlazarDataModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new TransactionalModule());

    bind(DBI.class).toProvider(DBIProvider.class).in(Scopes.SINGLETON);

    bindDao(binder(), BuildDefinitionDao.class);
  }

  private static <T> void bindDao(Binder binder, Class<T> type) {
    binder.bind(type).toProvider(new DaoProvider<T>(type)).in(Scopes.SINGLETON);
  }

  @Provides
  @Singleton
  public ManagedDataSource providesManagedDataSource(DataSourceFactory dataSourceFactory,
                                                     Environment environment) throws ClassNotFoundException {
    return dataSourceFactory.build(environment.metrics(), "db");
  }

  @Provides
  @Singleton
  public TransactionalDataSource providesTransactionalDataSource(ManagedDataSource managedDataSource) {
    return new TransactionalDataSource(managedDataSource);
  }
}
