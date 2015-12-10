package com.hubspot.blazar.data;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.hubspot.blazar.data.dao.BranchDao;
import com.hubspot.blazar.data.dao.DependenciesDao;
import com.hubspot.blazar.data.dao.ModuleBuildDao;
import com.hubspot.blazar.data.dao.ModuleDao;
import com.hubspot.blazar.data.dao.RepositoryBuildDao;
import com.hubspot.guice.transactional.DataSourceLocator;
import com.hubspot.guice.transactional.TransactionalDataSource;
import com.hubspot.guice.transactional.TransactionalModule;
import com.hubspot.guice.transactional.impl.DefaultDataSourceLocator;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;

import javax.inject.Singleton;

public class BlazarDaoModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new TransactionalModule());

    bind(DataSourceLocator.class).to(DefaultDataSourceLocator.class).in(Scopes.SINGLETON);
    bind(DBI.class).toProvider(DBIProvider.class).in(Scopes.SINGLETON);

    bindDao(binder(), BranchDao.class);
    bindDao(binder(), ModuleDao.class);
    bindDao(binder(), RepositoryBuildDao.class);
    bindDao(binder(), ModuleBuildDao.class);
    bindDao(binder(), DependenciesDao.class);
  }

  private static <T> void bindDao(Binder binder, Class<T> type) {
    binder.bind(type).toProvider(new DaoProvider<>(type)).in(Scopes.SINGLETON);
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
