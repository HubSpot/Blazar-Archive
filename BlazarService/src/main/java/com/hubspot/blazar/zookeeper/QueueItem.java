package com.hubspot.blazar.zookeeper;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.hubspot.blazar.zookeeper.QueueItem.QueueItemDeserializer;

@JsonDeserialize(using = QueueItemDeserializer.class)
public class QueueItem {
  private final Class<?> type;
  private final Object item;
  private final long timestamp;

  public QueueItem(Object item) {
    this(item.getClass(), item, System.currentTimeMillis());
  }

  private QueueItem(Class<?> type, Object item, long timestamp) {
    this.type = type;
    this.item = item;
    this.timestamp = timestamp;
  }

  public Class<?> getType() {
    return type;
  }

  public Object getItem() {
    return item;
  }

  public long getTimestamp() {
    return timestamp;
  }

  static class QueueItemDeserializer extends StdDeserializer<QueueItem> {

    QueueItemDeserializer() {
      super(QueueItem.class);
    }

    @Override
    public QueueItem deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      GenericQueueItem genericQueueItem = p.readValueAs(GenericQueueItem.class);

      Object item = p.getCodec().treeToValue(genericQueueItem.getItem(), genericQueueItem.getType());
      return new QueueItem(genericQueueItem.getType(), item, genericQueueItem.getTimestamp());
    }
  }

  private static class GenericQueueItem {
    private final Class<?> type;
    private final JsonNode item;
    private final long timestamp;

    @JsonCreator
    GenericQueueItem(@JsonProperty("type") Class<?> type,
                     @JsonProperty("item") JsonNode item,
                     @JsonProperty("timestamp") long timestamp) {
      this.type = type;
      this.item = item;
      this.timestamp = timestamp;
    }

    public Class<?> getType() {
      return type;
    }

    public JsonNode getItem() {
      return item;
    }

    public long getTimestamp() {
      return timestamp;
    }
  }
}
