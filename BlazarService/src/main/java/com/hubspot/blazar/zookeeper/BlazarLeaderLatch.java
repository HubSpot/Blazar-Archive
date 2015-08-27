package com.hubspot.blazar.zookeeper;

import com.google.common.net.HostAndPort;
import io.dropwizard.lifecycle.Managed;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

@Singleton
public class BlazarLeaderLatch extends LeaderLatch implements Managed {

  @Inject
  public BlazarLeaderLatch(CuratorFramework client, HostAndPort hostAndPort, Set<LeaderLatchListener> listeners) {
    super(client, "/leader", hostAndPort.toString());

    for (LeaderLatchListener listener : listeners) {
      addListener(listener);
    }
  }

  @Override
  public void stop() throws Exception {
    super.close();
  }
}
