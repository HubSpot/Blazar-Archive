package com.hubspot.blazar.guice;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.state.ConnectionStateListener;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.net.HostAndPort;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.util.HostUtils;
import com.hubspot.blazar.util.HostUtils.Host;
import com.hubspot.blazar.util.HostUtils.Port;
import com.hubspot.blazar.util.ManagedScheduledExecutorServiceProvider;
import com.hubspot.blazar.zookeeper.BlazarCuratorProvider;
import com.hubspot.blazar.zookeeper.BlazarLeaderLatch;
import com.hubspot.blazar.zookeeper.LeaderMetricManager;
import com.hubspot.blazar.zookeeper.QueueProcessor;
import com.hubspot.blazar.zookeeper.SqlEventBus;

import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.SimpleServerFactory;

public class BlazarZooKeeperModule implements Module {
  private final boolean isWebhookOnly;

  public BlazarZooKeeperModule(BlazarConfiguration configuration) {
    this.isWebhookOnly = configuration.isWebhookOnly();
  }

  @Override
  public void configure(Binder binder) {
    binder.bind(CuratorFramework.class).toProvider(BlazarCuratorProvider.class).in(Scopes.SINGLETON);
    binder.bind(SqlEventBus.class);
    Multibinder.newSetBinder(binder, ConnectionStateListener.class); // TODO

    if (!isWebhookOnly) {
      binder.bind(LeaderLatch.class).to(BlazarLeaderLatch.class);

      Multibinder<LeaderLatchListener> leaderLatchListeners = Multibinder.newSetBinder(binder, LeaderLatchListener.class);
      leaderLatchListeners.addBinding().to(QueueProcessor.class);
      leaderLatchListeners.addBinding().to(LeaderMetricManager.class);
      binder.bind(ScheduledExecutorService.class)
          .annotatedWith(Names.named("QueueProcessor"))
          .toProvider(new ManagedScheduledExecutorServiceProvider(1, "QueueProcessor"))
          .in(Scopes.SINGLETON);
    }
  }

  @Provides
  @Singleton
  public EventBus providesEventBus(SqlEventBus sqlEventBus) {
    return sqlEventBus;
  }

  @Provides
  @Singleton
  public Set<Object> erroredQueueItems() {
    return Sets.newConcurrentHashSet();
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
