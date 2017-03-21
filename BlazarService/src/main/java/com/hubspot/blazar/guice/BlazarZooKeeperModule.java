package com.hubspot.blazar.guice;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.state.ConnectionStateListener;

import com.google.common.base.Optional;
import com.google.common.net.HostAndPort;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.hubspot.blazar.config.BlazarConfigurationWrapper;
import com.hubspot.blazar.queue.QueueProcessor;
import com.hubspot.blazar.util.HostUtils;
import com.hubspot.blazar.util.HostUtils.Host;
import com.hubspot.blazar.util.HostUtils.Port;
import com.hubspot.blazar.zookeeper.BlazarCuratorProvider;
import com.hubspot.blazar.zookeeper.BlazarLeaderLatch;
import com.hubspot.blazar.zookeeper.LeaderMetricManager;

import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.SimpleServerFactory;

public class BlazarZooKeeperModule implements Module {

  @Override
  public void configure(Binder binder) {
    binder.bind(CuratorFramework.class).toProvider(BlazarCuratorProvider.class).in(Scopes.SINGLETON);
    Multibinder.newSetBinder(binder, ConnectionStateListener.class); // TODO
    binder.bind(LeaderLatch.class).to(BlazarLeaderLatch.class);
    Multibinder<LeaderLatchListener> leaderLatchListeners = Multibinder.newSetBinder(binder, LeaderLatchListener.class);
    leaderLatchListeners.addBinding().to(QueueProcessor.class);
    leaderLatchListeners.addBinding().to(LeaderMetricManager.class);
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
  public int providesPort(BlazarConfigurationWrapper configuration) {
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
