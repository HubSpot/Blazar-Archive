package com.hubspot.blazar.queue;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.inject.name.Named;
import com.hubspot.blazar.data.dao.QueueItemDao;
import com.hubspot.blazar.data.queue.QueueItem;
import com.hubspot.blazar.externalservice.BuildClusterHealthChecker;
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
  private final BuildClusterHealthChecker buildClusterHealthChecker;
  private Optional<ScheduledFuture<?>> processingTask;

  @Inject
  public QueueProcessor(@Named("QueueProcessor") ScheduledExecutorService executorService,
                        QueueItemDao queueItemDao,
                        SqlEventBus eventBus,
                        BuildClusterHealthChecker buildClusterHealthChecker,
                        Set<Object> erroredItems) {
    this.executorService = executorService;
    this.queueExecutors = new ConcurrentHashMap<>();
    this.queueItemDao = queueItemDao;
    this.eventBus = eventBus;
    this.erroredItems = erroredItems;
    this.processingItems = Sets.newConcurrentHashSet();
    this.buildClusterHealthChecker = buildClusterHealthChecker;

    this.running = new AtomicBoolean();
    this.leader = new AtomicBoolean();
    this.processingTask = Optional.absent();
  }

  @Override
  public void start() {
    startProcessorWithCustomPollingRate(1, TimeUnit.SECONDS);
  }

  public void startProcessorWithCustomPollingRate(long delay, TimeUnit timeUnit) {
    running.set(true);
    LOG.info("Starting Queue Processor with delay of {} {}", delay, timeUnit);
    processingTask = Optional.of(executorService.scheduleAtFixedRate(this, 0, delay, timeUnit));
    LOG.info("Queue Processor Started");
  }

  @Override
  public void stop() {
    if (processingTask.isPresent()) {
      running.set(false);
      // gracefully allow processing to stop.
      boolean success = processingTask.get().cancel(false);
      if (!success && (processingTask.get().isCancelled() || processingTask.get().isDone())) {
        RuntimeException scheduledExectuorShutdownError = new RuntimeException("Failed to successfully shut down scheduled queue polling task");
        LOG.error("Error stopping QueueProcessor", scheduledExectuorShutdownError);
      }
    }
    LOG.info("Queue Processor Stopped");
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
      if (running.get() && leader.get() && buildClusterHealthChecker.isSomeClusterAvailable()) {
        List<QueueItem> queueItemsSorted = sort(queueItemDao.getItemsReadyToExecute());
        queueItemsSorted.removeAll(processingItems);
        processingItems.addAll(queueItemsSorted);

        for (QueueItem queueItem : queueItemsSorted) {
          LOG.debug("Processing Item: {}", queueItem);
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
          checkResult(queueItemDao.complete(queueItem), queueItem);
        } else if (queueItem.getRetryCount() < 9) {
          LOG.warn("Queue item {} failed to process, retrying", queueItem.getId().get());
          checkResult(queueItemDao.retry(queueItem), queueItem);
        } else {
          LOG.warn("Queue item {} failed to process 10 times, not retrying", queueItem.getId().get());
          checkResult(queueItemDao.complete(queueItem), queueItem);
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

    private void checkResult(int result, QueueItem queueItem) {
      if (result != 1) {
        LOG.warn("Could not find queue item with id {} to update", queueItem.getId().get());
      }
    }
  }
}
