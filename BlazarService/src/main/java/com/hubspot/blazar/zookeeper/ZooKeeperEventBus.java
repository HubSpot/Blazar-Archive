package com.hubspot.blazar.zookeeper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.hubspot.blazar.base.RepositoryBuild;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Singleton
public class ZooKeeperEventBus extends EventBus {
  private static final Logger LOG = LoggerFactory.getLogger(ZooKeeperEventBus.class);

  private final CuratorFramework curatorFramework;
  private final ObjectMapper mapper;

  @Inject
  public ZooKeeperEventBus(CuratorFramework curatorFramework, ObjectMapper mapper, final Set<Object> erroredItems) {
    super(new SubscriberExceptionHandler() {
      private final Logger LOG = LoggerFactory.getLogger("SubscriberExceptionHandler");

      @Override
      public void handleException(Throwable exception, SubscriberExceptionContext context) {
        String className = context.getSubscriber().getClass().getSimpleName();
        String methodName = context.getSubscriberMethod().getName();
        LOG.error("Error calling subscriber method {}.{}", className, methodName, exception);
        erroredItems.add(context.getEvent());
      }
    });

    this.curatorFramework = curatorFramework;
    this.mapper = mapper;
  }

  @Override
  public void post(Object event) {
    try {
      curatorFramework.create()
          .creatingParentsIfNeeded()
          .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
          .forPath(queuePath(event), serialize(event));
    } catch (Exception e) {
      LOG.error("Error enqueuing event to ZooKeeper", e);
      throw Throwables.propagate(e);
    }
  }

  public void dispatch(Object event) {
    super.post(event);
  }

  private String queuePath(Object event) {
    return ZKPaths.makePath("queues", event.getClass().getSimpleName(), "item-");
  }

  private byte[] serialize(Object event) throws JsonProcessingException {
    byte[] bytes = mapper.writeValueAsBytes(new QueueItem(event));
    if (event instanceof RepositoryBuild) {
      LOG.info(new String(bytes, StandardCharsets.UTF_8));
    }
    return bytes;
  }
}
