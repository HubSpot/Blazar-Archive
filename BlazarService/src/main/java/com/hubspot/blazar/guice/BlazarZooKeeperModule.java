package com.hubspot.blazar.guice;

import com.google.common.base.Optional;
import com.google.common.net.HostAndPort;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.util.HostUtils;
import com.hubspot.blazar.util.HostUtils.Host;
import com.hubspot.blazar.util.HostUtils.Port;
import com.hubspot.blazar.zookeeper.BlazarCuratorProvider;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.SimpleServerFactory;
import org.apache.curator.framework.CuratorFramework;

public class BlazarZooKeeperModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(CuratorFramework.class).toProvider(BlazarCuratorProvider.class).in(Scopes.SINGLETON);
  }

  @Provides
  @Singleton
  @Host
  public String determineBestHostIdentifier() {
    Optional<String> hostName = HostUtils.getHostName();
    if (hostName.isPresent()) {
      return hostName.get();
    }

    Optional<String> hostAddress = HostUtils.getHostAddress();
    if (hostAddress.isPresent()) {
      return hostAddress.get();
    }

    throw new RuntimeException("Unable to find a host identifier");
  }

  @Provides
  @Singleton
  @Port
  public int providesPort(BlazarConfiguration configuration) {
    SimpleServerFactory serverFactory = (SimpleServerFactory) configuration.getServerFactory();
    HttpConnectorFactory connector = (HttpConnectorFactory) serverFactory.getConnector();
    return connector.getPort();
  }

  @Provides
  @Singleton
  public HostAndPort providesHostAndPort(@Host String host, @Port int port) {
    return HostAndPort.fromParts(host, port);
  }
}
