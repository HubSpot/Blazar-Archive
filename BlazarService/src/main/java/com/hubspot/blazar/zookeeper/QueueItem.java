package com.hubspot.blazar.zookeeper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.hubspot.blazar.zookeeper.QueueItem.QueueItemDeserializer;

import java.io.IOException;

@JsonDeserialize(using = QueueItemDeserializer.class)
public class QueueItem {
  private final Class<?> type;
  private final Object item;

  public QueueItem(Object item) {
    this(item.getClass(), item);
  }

  private QueueItem(Class<?> type, Object item) {
    this.type = type;
    this.item = item;
  }

  public Class<?> getType() {
    return type;
  }

  public Object getItem() {
    return item;
  }

  static class QueueItemDeserializer extends StdDeserializer<QueueItem> {

    public QueueItemDeserializer() {
      super(QueueItem.class);
    }

    @Override
    public QueueItem deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      GenericQueueItem genericQueueItem = p.readValueAs(GenericQueueItem.class);

      Object item = p.getCodec().treeToValue(genericQueueItem.getItem(), genericQueueItem.getType());
      return new QueueItem(genericQueueItem.getType(), item);
    }
  }

  private static class GenericQueueItem {
    private final Class<?> type;
    private final JsonNode item;

    @JsonCreator
    public GenericQueueItem(@JsonProperty("type") Class<?> type, @JsonProperty("item") JsonNode item) {
      this.type = type;
      this.item = item;
    }

    public Class<?> getType() {
      return type;
    }

    public JsonNode getItem() {
      return item;
    }
  }
}
