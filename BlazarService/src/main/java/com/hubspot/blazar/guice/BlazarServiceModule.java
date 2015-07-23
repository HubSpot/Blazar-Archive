package com.hubspot.blazar.guice;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.hubspot.blazar.GitHubNamingFilter;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.GitHubConfiguration;
import com.hubspot.blazar.data.BlazarDataModule;
import com.hubspot.blazar.resources.BuildDefinitionResource;
import com.hubspot.blazar.resources.BuildResource;
import com.hubspot.blazar.resources.GitHubWebHookResource;
import com.hubspot.horizon.AsyncHttpClient;
import com.hubspot.horizon.ning.NingAsyncHttpClient;
import com.hubspot.jackson.jaxrs.PropertyFilteringMessageBodyWriter;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import io.dropwizard.db.DataSourceFactory;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;

public class BlazarServiceModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new BlazarDataModule());

    bind(PropertyFilteringMessageBodyWriter.class).in(Scopes.SINGLETON);

    bind(BuildResource.class);
    bind(BuildDefinitionResource.class);
    bind(GitHubWebHookResource.class);

    Multibinder.newSetBinder(binder(), ContainerRequestFilter.class).addBinding().to(GitHubNamingFilter.class).in(Scopes.SINGLETON);
  }

  @Provides
  @Singleton
  public DataSourceFactory providesDataSourceFactory(BlazarConfiguration configuration) {
    return configuration.getDatabaseConfiguration();
  }

  @Provides
  @Singleton
  public GitHub providesGitHub(BlazarConfiguration configuration) throws IOException {
    Optional<GitHubConfiguration> maybeGitHubConfig = configuration.getGitHubConfiguration();
    if (!maybeGitHubConfig.isPresent()) {
      return GitHub.connect();
    }

    GitHubConfiguration gitHubConfig = maybeGitHubConfig.get();

    GitHubBuilder builder = new GitHubBuilder();
    if (gitHubConfig.getEndpoint().isPresent()) {
      builder.withEndpoint(gitHubConfig.getEndpoint().get());
    }

    if (gitHubConfig.getOauthToken().isPresent()) {
      builder.withOAuthToken(gitHubConfig.getOauthToken().get(), gitHubConfig.getUser().orNull());
    } else if (gitHubConfig.getPassword().isPresent()) {
      builder.withPassword(gitHubConfig.getUser().orNull(), gitHubConfig.getPassword().get());
    }

    return builder.build();
  }

  @Provides
  @Singleton
  public XmlMapper providesXmlMapper() {
    return new XmlMapper();
  }

  @Provides
  @Singleton
  public AsyncHttpClient providesAsyncHttpClient() {
    return new NingAsyncHttpClient();
  }
}
