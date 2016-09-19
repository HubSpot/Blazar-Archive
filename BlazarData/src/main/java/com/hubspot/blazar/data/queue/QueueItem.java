package com.hubspot.blazar.data.queue;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.hubspot.blazar.data.queue.QueueItem.QueueItemDeserializer;
import com.hubspot.rosetta.annotations.StoredAsJson;

@JsonDeserialize(using = QueueItemDeserializer.class)
public class QueueItem {
  private final Optional<Long> id;
  private final Class<?> type;
  @StoredAsJson
  private final Object item;
  private final int retryCount;

  public QueueItem(Object item) {
    this(Optional.empty(), item.getClass(), item, 0);
  }

  private QueueItem(Optional<Long> id, Class<?> type, Object item, int retryCount) {
    this.id  = id;
    this.type = type;
    this.item = item;
    this.retryCount = retryCount;
  }

  public Optional<Long> getId() {
    return id;
  }

  public Class<?> getType() {
    return type;
  }

  public Object getItem() {
    return item;
  }

  public int getRetryCount() {
    return retryCount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    QueueItem queueItem = (QueueItem) o;
    return Objects.equals(id, queueItem.id) &&
        Objects.equals(type, queueItem.type) &&
        Objects.equals(item, queueItem.item) &&
        Objects.equals(retryCount, queueItem.retryCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type, item, retryCount);
  }

  static class QueueItemDeserializer extends StdDeserializer<QueueItem> {

    public QueueItemDeserializer() {
      super(QueueItem.class);
    }

    @Override
    public QueueItem deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      GenericQueueItem genericQueueItem = p.readValueAs(GenericQueueItem.class);

      Optional<Long> id = Optional.of(genericQueueItem.getId());
      Object item = p.getCodec().treeToValue(genericQueueItem.getItem(), genericQueueItem.getType());
      return new QueueItem(id, genericQueueItem.getType(), item, genericQueueItem.getRetryCount());
    }
  }

  private static class GenericQueueItem {
    private final long id;
    private final Class<?> type;
    @StoredAsJson
    private final JsonNode item;
    private final int retryCount;

    @JsonCreator
    public GenericQueueItem(@JsonProperty("id") long id,
                            @JsonProperty("type") Class<?> type,
                            @JsonProperty("item") JsonNode item,
                            @JsonProperty("retryCount") int retryCount) {
      this.id = id;
      this.type = type;
      this.item = item;
      this.retryCount = retryCount;
    }

    public long getId() {
      return id;
    }

    public Class<?> getType() {
      return type;
    }

    public JsonNode getItem() {
      return item;
    }

    public int getRetryCount() {
      return retryCount;
    }
  }
}
