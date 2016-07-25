package com.hubspot.blazar.zookeeper;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.hubspot.blazar.data.dao.QueueItemDao;
import com.hubspot.blazar.data.queue.QueueItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

@Singleton
public class SqlEventBus extends EventBus {
  private final QueueItemDao queueItemDao;

  @Inject
  public SqlEventBus(QueueItemDao queueItemDao, final Set<Object> erroredItems) {
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

    this.queueItemDao = queueItemDao;
  }

  @Override
  public void post(Object event) {
    queueItemDao.insert(new QueueItem(event));
  }

  public void dispatch(Object event) {
    super.post(event);
  }
}
