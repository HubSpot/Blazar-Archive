package com.hubspot.blazar.zookeeper;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.skife.jdbi.v2.Transaction;
import org.skife.jdbi.v2.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.inject.name.Named;
import com.hubspot.blazar.data.dao.QueueItemDao;
import com.hubspot.blazar.data.queue.QueueItem;
import com.hubspot.blazar.util.ManagedScheduledExecutorServiceProvider;

import io.dropwizard.lifecycle.Managed;

@Singleton
public class QueueProcessor implements LeaderLatchListener, Managed, Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(QueueProcessor.class);

  private final ScheduledExecutorService executorService;
  private final Map<String, ScheduledExecutorService> queueExecutors;
  private final QueueItemDao queueItemDao;
  private final SqlEventBus eventBus;
  private final Set<Object> erroredItems;
  private final Set<QueueItem> processingItems;
  private final AtomicBoolean running;
  private final AtomicBoolean leader;

  @Inject
  public QueueProcessor(@Named("QueueProcessor") ScheduledExecutorService executorService,
                        QueueItemDao queueItemDao,
                        SqlEventBus eventBus,
                        Set<Object> erroredItems) {
    this.executorService = executorService;
    this.queueExecutors = new ConcurrentHashMap<>();
    this.queueItemDao = queueItemDao;
    this.eventBus = eventBus;
    this.erroredItems = erroredItems;
    this.processingItems = Sets.newConcurrentHashSet();

    this.running = new AtomicBoolean();
    this.leader = new AtomicBoolean();
  }

  @Override
  public void start() {
    running.set(true);
    executorService.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
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

  @Override
  public void run() {
    try {
      if (running.get() && leader.get()) {
        List<QueueItem> queueItemsSorted = sort(queueItemDao.getItemsReadyToExecute());
        queueItemsSorted.removeAll(processingItems);
        processingItems.addAll(queueItemsSorted);

        for (QueueItem queueItem : queueItemsSorted) {
          String queueName = queueItem.getType().getSimpleName();
          queueExecutors.computeIfAbsent(queueName, k -> {
            return new ManagedScheduledExecutorServiceProvider(1, "QueueProcessor-" + queueName).get();
          }).execute(new ProcessItemRunnable(queueItem));
        }
      }
    } catch (Throwable t) {
      LOG.error("Error processing queue", t);
    }
  }

  private List<QueueItem> sort(Set<QueueItem> queueItems) {
    return queueItems.stream()
        .sorted(Comparator.comparing(item -> item.getId().get()))
        .collect(Collectors.toList());
  }

  private class ProcessItemRunnable implements Runnable {
    private final QueueItem queueItem;

    public ProcessItemRunnable(QueueItem queueItem) {
      this.queueItem = queueItem;
    }

    @Override
    public void run() {
      try {
        if (!queueItemDao.isItemStillQueued(queueItem)) {
          LOG.info("Queue item {} was already completed, skipping", queueItem.getId().get());
        } else if (process(queueItem.getItem())) {
          LOG.debug("Queue item {} was successfully processed, deleting", queueItem.getId().get());
          queueItemDao.complete(queueItem);
        } else if (queueItem.getRetryCount() < 9) {
          LOG.warn("Queue item {} failed to process, retrying", queueItem.getId().get());
          queueItemDao.inTransaction(new Transaction<Void, QueueItemDao>() {

            @Override
            public Void inTransaction(QueueItemDao transactional, TransactionStatus status) {
              queueItemDao.complete(queueItem);
              queueItemDao.insert(queueItem.forRetry());
              return null;
            }
          });
        } else {
          LOG.warn("Queue item {} failed to process 10 times, not retrying", queueItem.getId().get());
          queueItemDao.complete(queueItem);
        }
      } catch (Throwable t) {
        LOG.error("Unexpected error processing queue item: {}", queueItem.getId().get(), t);
      } finally {
        processingItems.remove(queueItem);
      }
    }

    private boolean process(Object event) {
      eventBus.dispatch(event);

      return !erroredItems.remove(event);
    }
  }
}
