package com.hubspot.blazar.data;

import javax.inject.Singleton;

import org.skife.jdbi.v2.DBI;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.hubspot.blazar.data.dao.BranchDao;
import com.hubspot.blazar.data.dao.BranchSettingsDao;
import com.hubspot.blazar.data.dao.DependenciesDao;
import com.hubspot.blazar.data.dao.InstantMessageConfigurationDao;
import com.hubspot.blazar.data.dao.InterProjectBuildDao;
import com.hubspot.blazar.data.dao.InterProjectBuildMappingDao;
import com.hubspot.blazar.data.dao.MalformedFileDao;
import com.hubspot.blazar.data.dao.MetricsDao;
import com.hubspot.blazar.data.dao.ModuleBuildDao;
import com.hubspot.blazar.data.dao.ModuleDao;
import com.hubspot.blazar.data.dao.QueueItemDao;
import com.hubspot.blazar.data.dao.RepositoryBuildDao;
import com.hubspot.blazar.data.dao.StateDao;
import com.hubspot.guice.transactional.DataSourceLocator;
import com.hubspot.guice.transactional.TransactionalDataSource;
import com.hubspot.guice.transactional.TransactionalModule;
import com.hubspot.guice.transactional.impl.DefaultDataSourceLocator;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;

public class BlazarDaoModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new TransactionalModule());

    bind(DataSourceLocator.class).to(DefaultDataSourceLocator.class).in(Scopes.SINGLETON);
    bind(DBI.class).toProvider(DBIProvider.class).in(Scopes.SINGLETON);

    bindDao(binder(), BranchDao.class);
    bindDao(binder(), ModuleDao.class);
    bindDao(binder(), StateDao.class);
    bindDao(binder(), RepositoryBuildDao.class);
    bindDao(binder(), ModuleBuildDao.class);
    bindDao(binder(), DependenciesDao.class);
    bindDao(binder(), MalformedFileDao.class);
    bindDao(binder(), InstantMessageConfigurationDao.class);
    bindDao(binder(), InterProjectBuildDao.class);
    bindDao(binder(), InterProjectBuildMappingDao.class);
    bindDao(binder(), BranchSettingsDao.class);
    bindDao(binder(), MetricsDao.class);
    bindDao(binder(), QueueItemDao.class);
  }

  private static <T> void bindDao(Binder binder, Class<T> type) {
    binder.bind(type).toProvider(new DaoProvider<>(type)).in(Scopes.SINGLETON);
  }

  @Provides
  @Singleton
  public ManagedDataSource providesManagedDataSource(DataSourceFactory dataSourceFactory,
                                                     MetricRegistry metricRegistry) throws ClassNotFoundException {
    return dataSourceFactory.build(metricRegistry, "db");
  }

  @Provides
  @Singleton
  public TransactionalDataSource providesTransactionalDataSource(ManagedDataSource managedDataSource) {
    return new TransactionalDataSource(managedDataSource);
  }

  @Override
  public boolean equals(Object o) {
    return o != null && getClass().equals(o.getClass());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
