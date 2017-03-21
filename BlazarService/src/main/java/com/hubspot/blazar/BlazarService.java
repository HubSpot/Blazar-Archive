package com.hubspot.blazar;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.hubspot.blazar.command.CleanRepoMetadataCommand;
import com.hubspot.blazar.command.VersionBackFillCommand;
import com.hubspot.blazar.config.BlazarConfigurationWrapper;
import com.hubspot.blazar.guice.BlazarServiceModule;
import com.hubspot.dropwizard.guicier.GuiceBundle;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;

import io.dropwizard.Application;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

public class BlazarService<T extends BlazarConfigurationWrapper> extends Application<T> {

  @Override
  public void initialize(final Bootstrap<T> bootstrap) {
    bootstrap.addBundle(buildGuiceBundle());
    bootstrap.addBundle(new MigrationsBundle<BlazarConfigurationWrapper>() {

      @Override
      public DataSourceFactory getDataSourceFactory(final BlazarConfigurationWrapper configuration) {
        return configuration.getBlazarConfiguration().getDatabaseConfiguration();
      }
    });
    bootstrap.addBundle(new AssetsBundle("/static"));
    bootstrap.addBundle(new ViewBundle());
    bootstrap.getObjectMapper().registerModule(new ProtobufModule());
    bootstrap.getObjectMapper().setSerializationInclusion(Include.NON_NULL);
    bootstrap.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    bootstrap.addCommand(new VersionBackFillCommand());
    bootstrap.addCommand(new CleanRepoMetadataCommand());
  }

  @Override
  public void run(final T configuration, final Environment environment) {}

  private ConfiguredBundle<BlazarConfigurationWrapper> buildGuiceBundle() {
    return GuiceBundle.defaultBuilder(BlazarConfigurationWrapper.class)
        .enableGuiceEnforcer(false)
        .modules(new BlazarServiceModule()).build();
  }

  public static void main(String... args) throws Exception {
    try {
      new BlazarService<>().run(args);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}
