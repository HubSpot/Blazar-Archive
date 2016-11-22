package com.hubspot.blazar.zookeeper;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.ZooKeeperConfiguration;

import io.dropwizard.lifecycle.Managed;

public class BlazarCuratorProvider implements Provider<CuratorFramework>, Managed {
  private static final Logger LOG = LoggerFactory.getLogger(BlazarCuratorProvider.class);

  private final CuratorFramework curatorFramework;
  private final AtomicBoolean started = new AtomicBoolean();
  private final AtomicBoolean stopped = new AtomicBoolean();

  @Inject
  public BlazarCuratorProvider(BlazarConfiguration configuration, Set<ConnectionStateListener> listeners) {
    Optional<ZooKeeperConfiguration> maybeZooKeeperConfiguration = configuration.getZooKeeperConfiguration();
    if (!maybeZooKeeperConfiguration.isPresent()) {
      throw new IllegalArgumentException("Can not create a CuratorProvider with no zookeeper connection info in BlazarConfiguration");
    }
    ZooKeeperConfiguration zooKeeperConfiguration = maybeZooKeeperConfiguration.get();
    this.curatorFramework = CuratorFrameworkFactory.builder()
        .connectString(zooKeeperConfiguration.getQuorum())
        .sessionTimeoutMs(zooKeeperConfiguration.getSessionTimeoutMillis())
        .connectionTimeoutMs(zooKeeperConfiguration.getConnectTimeoutMillis())
        .retryPolicy(new ExponentialBackoffRetry(zooKeeperConfiguration.getInitialRetryBackoffMillis(), zooKeeperConfiguration.getMaxRetries()))
        .namespace(zooKeeperConfiguration.getNamespace())
        .defaultData(null)
        .build();

    for (ConnectionStateListener listener : listeners) {
      curatorFramework.getConnectionStateListenable().addListener(listener);
    }
  }

  @Override
  public void start() throws Exception {
    if (started.compareAndSet(false, true)) {
      curatorFramework.start();

      long start = System.currentTimeMillis();

      try {
        Preconditions.checkState(curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut(), "Could not connect to zookeeper");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      long end = System.currentTimeMillis();

      LOG.info("Connected to ZooKeeper in {}ms", end - start);
    }
  }

  @Override
  public CuratorFramework get() {
    return curatorFramework;
  }

  @Override
  public void stop() throws Exception {
    if (started.get() && stopped.compareAndSet(false, true)) {
      curatorFramework.close();
    }
  }
}
