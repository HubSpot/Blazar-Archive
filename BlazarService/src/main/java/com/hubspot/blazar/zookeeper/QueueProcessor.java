package com.hubspot.blazar.zookeeper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.google.inject.name.Named;

import io.dropwizard.lifecycle.Managed;

@Singleton
public class QueueProcessor implements LeaderLatchListener, Managed {
  private static final Logger LOG = LoggerFactory.getLogger(QueueProcessor.class);

  private final ScheduledExecutorService executorService;
  private final CuratorFramework curatorFramework;
  private final ZooKeeperEventBus eventBus;
  private final ObjectMapper mapper;
  private final Set<Object> erroredItems;
  private final AtomicBoolean running;
  private final AtomicBoolean leader;

  @Inject
  public QueueProcessor(@Named("QueueProcessor") ScheduledExecutorService executorService,
                        CuratorFramework curatorFramework,
                        ZooKeeperEventBus eventBus,
                        ObjectMapper mapper,
                        Set<Object> erroredItems) {
    this.executorService = executorService;
    this.curatorFramework = curatorFramework;
    this.eventBus = eventBus;
    this.mapper = mapper;
    this.erroredItems = erroredItems;

    this.running = new AtomicBoolean();
    this.leader = new AtomicBoolean();
  }

  @Override
  public void start() {
    running.set(true);
    executorService.scheduleAtFixedRate(new QueueManager(), 0, 10, TimeUnit.SECONDS);

  }

  @Override
  public void stop() {
    running.set(false);
  }

  @Override
  public void isLeader() {
    LOG.info("Now the leader, starting queue processing");
    leader.set(true);
  }

  @Override
  public void notLeader() {
    LOG.info("Not the leader, stopping queue processing");
    leader.set(false);
  }

  private class QueueManager implements Runnable {
    private final Map<String, Future<?>> activeQueues = new ConcurrentHashMap<>();

    @Override
    public void run() {
      try {
        if (running.get() && leader.get()) {
          Set<String> queues = new HashSet<>(curatorFramework.getChildren().forPath("/queues"));
          LOG.debug("Found queues to process: {}", queues);

          for (String addedQueue : Sets.difference(queues, activeQueues.keySet())) {
            Runnable processQueueRunnable = new ProcessQueueRunnable(addedQueue);
            Future<?> future = executorService.scheduleAtFixedRate(processQueueRunnable, 0, 1, TimeUnit.SECONDS);
            activeQueues.put(addedQueue, future);
          }

          for (String removedQueue : Sets.difference(activeQueues.keySet(), queues)) {
            activeQueues.remove(removedQueue).cancel(false);
          }
        } else {
          LOG.debug("Not checking for queues to process");
          Set<String> queues = new HashSet<>(activeQueues.keySet());
          for (String queue : queues) {
            activeQueues.remove(queue).cancel(false);
          }
        }
      } catch (NoNodeException e) {
        LOG.warn("No node found for path: {}", e.getPath());
      } catch (Throwable t) {
        LOG.error("Error checking for queues to process", t);
      }
    }
  }

  private class ProcessQueueRunnable implements Runnable {
    private final String queue;

    private ProcessQueueRunnable(String queue) {
      this.queue = queue;
    }

    @Override
    public void run() {
      try {
        List<String> items = curatorFramework.getChildren().forPath(ZKPaths.makePath("queues", queue));
        LOG.debug("Found {} items in queue: {}", items.size(), queue);

        for (String item : sort(items)) {
          String path = ZKPaths.makePath("queues", queue, item);

          if (process(path)) {
            LOG.debug("Deleting successfully processed queue item: {}", path);
            curatorFramework.delete().guaranteed().forPath(path);
          } else {
            LOG.info("Failed to process queue item, will retry: {}", path);
          }
        }
      } catch (Throwable t) {
        LOG.error("Error processing queue: {}", queue, t);
      }
    }

    private boolean process(String path) throws Exception {
      try {
        byte[] data = curatorFramework.getData().forPath(path);
        QueueItem item = mapper.readValue(data, QueueItem.class);
        long age = System.currentTimeMillis() - item.getTimestamp();
        if (age < 100) {
          Thread.sleep(100 - age);
        }
        return process(item.getItem());
      } catch (NoNodeException e) {
        LOG.warn("No node found for path: {}", e.getPath());
        return false;
      } catch (Exception e) {
        LOG.error("Error processing queue item: {}", path, e);
        return false;
      }
    }

    private boolean process(Object event) {
      eventBus.dispatch(event);

      return !erroredItems.remove(event);
    }

    private List<String> sort(Collection<String> unsorted) {
      List<String> sorted = new ArrayList<>(unsorted);
      Collections.sort(sorted);
      return sorted;
    }
  }

}
