package com.hubspot.blazar.data;

import javax.inject.Provider;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.StatementContext;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jdbi.InstrumentedTimingCollector;
import com.codahale.metrics.jdbi.strategies.DelegatingStatementNameStrategy;
import com.codahale.metrics.jdbi.strategies.NameStrategies;
import com.codahale.metrics.jdbi.strategies.StatementNameStrategy;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.guice.transactional.TransactionalDataSource;
import com.hubspot.rosetta.jdbi.RosettaMapperFactory;
import com.hubspot.rosetta.jdbi.RosettaObjectMapperOverride;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.jdbi.DBIHealthCheck;
import io.dropwizard.jdbi.GuavaOptionalContainerFactory;
import io.dropwizard.jdbi.ImmutableListContainerFactory;
import io.dropwizard.jdbi.ImmutableSetContainerFactory;
import io.dropwizard.jdbi.args.JodaDateTimeArgumentFactory;
import io.dropwizard.jdbi.args.JodaDateTimeMapper;
import io.dropwizard.jdbi.args.OptionalArgumentFactory;
import io.dropwizard.jdbi.logging.LogbackLog;
import io.dropwizard.setup.Environment;

public class DBIProvider implements Provider<DBI> {
  private final TransactionalDataSource transactionalDataSource;
  private final ManagedDataSource managedDataSource;
  private final DataSourceFactory dataSourceFactory;
  private final ObjectMapper objectMapper;
  private Optional<Environment> environment;

  @Inject
  public DBIProvider(TransactionalDataSource transactionalDataSource,
                     ManagedDataSource managedDataSource,
                     DataSourceFactory dataSourceFactory,
                     ObjectMapper objectMapper) {
    this.transactionalDataSource = transactionalDataSource;
    this.managedDataSource = managedDataSource;
    this.dataSourceFactory = dataSourceFactory;
    this.objectMapper = objectMapper.copy().setSerializationInclusion(Include.ALWAYS);
    this.environment = Optional.absent();
  }

  @Inject(optional = true)
  public void setEnvironment(Environment environment) {
    this.environment = Optional.of(environment);
  }

  @Override
  public DBI get() {
    final DBI dbi = new DBI(transactionalDataSource);
    dbi.setSQLLog(new LogbackLog());

    if (environment.isPresent()) {
      environment.get().lifecycle().manage(managedDataSource);
      environment.get().healthChecks().register("db", new DBIHealthCheck(dbi, dataSourceFactory.getValidationQuery()));
      dbi.setTimingCollector(new InstrumentedTimingCollector(environment.get().metrics(), new SanerNamingStrategy()));
    }

    dbi.registerArgumentFactory(new OptionalArgumentFactory(dataSourceFactory.getDriverClass()));
    dbi.registerContainerFactory(new ImmutableListContainerFactory());
    dbi.registerContainerFactory(new ImmutableSetContainerFactory());
    dbi.registerContainerFactory(new GuavaOptionalContainerFactory());
    dbi.registerArgumentFactory(new JodaDateTimeArgumentFactory());
    dbi.registerColumnMapper(new JodaDateTimeMapper());
    dbi.registerMapper(new RosettaMapperFactory());

    new RosettaObjectMapperOverride(objectMapper).override(dbi);

    return dbi;
  }

  private static class SanerNamingStrategy extends DelegatingStatementNameStrategy {
    private static final String RAW_SQL = MetricRegistry.name(DBI.class, "raw-sql");

    private SanerNamingStrategy() {
      super(NameStrategies.CHECK_EMPTY,
          NameStrategies.CONTEXT_CLASS,
          NameStrategies.CONTEXT_NAME,
          NameStrategies.SQL_OBJECT,
          new StatementNameStrategy() {

            @Override
            public String getStatementName(StatementContext statementContext) {
              return RAW_SQL;
            }
          });
    }
  }
}
