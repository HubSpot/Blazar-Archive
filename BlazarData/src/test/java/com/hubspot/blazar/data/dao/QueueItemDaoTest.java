package com.hubspot.blazar.data.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.blazar.base.BranchSetting;
import com.hubspot.blazar.data.BlazarDataTestBase;
import com.hubspot.blazar.data.queue.QueueItem;

public class QueueItemDaoTest extends BlazarDataTestBase {
  private QueueItemDao queueItemDao;

  @Before
  public void before() {
    this.queueItemDao = getFromGuice(QueueItemDao.class);
  }

  @Test
  public void testInsert() {
    queueItemDao.insert(new QueueItem(new BranchSetting(123, true, false), 0));

    Set<QueueItem> queueItems = queueItemDao.getItemsReadyToExecute();

    int i = 1;
  }
}
