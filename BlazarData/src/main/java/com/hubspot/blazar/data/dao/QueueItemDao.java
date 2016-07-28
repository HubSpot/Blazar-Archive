package com.hubspot.blazar.data.dao;

import java.util.Set;

import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import com.hubspot.blazar.data.queue.QueueItem;
import com.hubspot.rosetta.jdbi.BindWithRosetta;

public interface QueueItemDao {

  @SqlQuery("SELECT * FROM queue_items WHERE completedTimestamp IS NULL AND desiredExecutionTimestamp <= NOW()")
  Set<QueueItem> getItemsReadyToExecute();

  @SqlQuery("SELECT count(*) FROM queue_items WHERE id = :id AND completedTimestamp IS NULL")
  boolean isItemStillQueued(@BindWithRosetta QueueItem queueItem);

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO queue_items (type, item, retryCount, desiredExecutionTimestamp) VALUES (:type, :item, :retryCount, TIMESTAMPADD(SECOND, :retryCount * 10, NOW()))")
  int insert(@BindWithRosetta QueueItem queueItem);

  @SqlUpdate("UPDATE queue_items SET completedTimestamp = NOW() WHERE id = :id")
  void complete(@BindWithRosetta QueueItem queueItem);
}
