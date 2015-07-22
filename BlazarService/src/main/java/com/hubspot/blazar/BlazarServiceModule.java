package com.hubspot.blazar;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.hubspot.jackson.jaxrs.PropertyFilteringMessageBodyWriter;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;

public class BlazarServiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(PropertyFilteringMessageBodyWriter.class).in(Scopes.SINGLETON);

    bind(BuildResource.class);
    bind(GitHubWebHookResource.class);

    bind(BuildService.class);

    Multibinder.newSetBinder(binder(), ContainerRequestFilter.class).addBinding().to(GitHubNamingFilter.class).in(Scopes.SINGLETON);
  }

  @Provides
  @Singleton
  public GitHub providesGitHub(BlazarConfiguration config) throws IOException {
    Optional<GitHubConfiguration> maybeGitHubConfig = config.getGitHubConfiguration();
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
}
