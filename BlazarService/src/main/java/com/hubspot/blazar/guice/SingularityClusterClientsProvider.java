package com.hubspot.blazar.guice;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.singularity.client.SingularityClient;
import com.hubspot.singularity.client.SingularityClientProvider;

@Singleton
public class SingularityClusterClientsProvider implements Provider<Map<String, SingularityClient>> {

  private final Map<String, SingularityClient> singularityClients;

  @Inject
  public SingularityClusterClientsProvider(SingularityClientProvider singularityClientProvider, BlazarConfiguration blazarConfiguration) {
    ImmutableMap.Builder<String, SingularityClient> sigularityClientsBuilder = ImmutableMap.builder();
    blazarConfiguration.getSingularityClusterConfigurations().entrySet().forEach(clusterConfigurationEntry -> {

      singularityClientProvider.setHosts(clusterConfigurationEntry.getValue().getHost());

      if (clusterConfigurationEntry.getValue().getPath().isPresent()) {
        singularityClientProvider.setContextPath(clusterConfigurationEntry.getValue().getPath().get());
      }

      if (clusterConfigurationEntry.getValue().getCredentials().isPresent()) {
        singularityClientProvider.setCredentials(clusterConfigurationEntry.getValue().getCredentials().get());
      }

      sigularityClientsBuilder.put(clusterConfigurationEntry.getKey(), singularityClientProvider.get());
    });

    singularityClients = sigularityClientsBuilder.build();
  }

  @Override
  public Map<String, SingularityClient> get() {
    return singularityClients;
  }
}
