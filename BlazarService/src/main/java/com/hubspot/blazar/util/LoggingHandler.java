package com.hubspot.blazar.util;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Singleton
public class LoggingHandler {
  private static final Logger LOG = LoggerFactory.getLogger(LoggingHandler.class);

  @Inject
  public LoggingHandler(EventBus eventBus) {
    eventBus.register(this);
  }

  @Subscribe
  public void logEvent(Message message) throws IOException {
    LOG.info(message.toString());
  }
}
