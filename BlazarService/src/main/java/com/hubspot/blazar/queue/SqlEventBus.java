package com.hubspot.blazar.queue;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.hubspot.blazar.data.dao.QueueItemDao;
import com.hubspot.blazar.data.queue.QueueItem;

@Singleton
public class SqlEventBus extends EventBus {
  private final QueueItemDao queueItemDao;

  @Inject
  public SqlEventBus(QueueItemDao queueItemDao, final Set<Object> erroredItems) {
    super(new SubscriberExceptionHandler() {
      private final Logger LOG = LoggerFactory.getLogger("SubscriberExceptionHandler");

      @Override
      public void handleException(Throwable exception, SubscriberExceptionContext context) {
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
