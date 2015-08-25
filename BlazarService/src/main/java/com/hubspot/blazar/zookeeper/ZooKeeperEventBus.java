package com.hubspot.blazar.zookeeper;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZooKeeperEventBus extends EventBus {

  public ZooKeeperEventBus() {
    super(new SubscriberExceptionHandler() {
      private final Logger LOG = LoggerFactory.getLogger("SubscriberExceptionHandler");

      @Override
      public void handleException(Throwable exception, SubscriberExceptionContext context) {
        String className = context.getSubscriber().getClass().getSimpleName();
        String methodName = context.getSubscriberMethod().getName();
        LOG.error("Error calling subscriber method {}.{}", className, methodName, exception);
      }
    });
  }

  @Override
  public void post(Object event) {

  }


}
