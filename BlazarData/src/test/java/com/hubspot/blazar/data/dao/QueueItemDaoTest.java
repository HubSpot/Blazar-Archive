package com.hubspot.blazar.data.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
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
    queueItemDao.insert(new QueueItem(new BranchSetting(123, true, false)));

    Set<QueueItem> queueItems = queueItemDao.getItemsReadyToExecute();

    assertThat(queueItems).hasSize(1);

    QueueItem queueItem = queueItems.iterator().next();
    assertThat(queueItem.getId()).isEqualTo(Optional.of(1L));
    assertThat(queueItem.getType()).isEqualTo(BranchSetting.class);
    assertThat(queueItem.getRetryCount()).isEqualTo(0);

    BranchSetting branchSetting = (BranchSetting) queueItem.getItem();
    assertThat(branchSetting.getBranchId()).isEqualTo(123);
    assertThat(branchSetting.isTriggerInterProjectBuilds()).isTrue();
    assertThat(branchSetting.isInterProjectBuildOptIn()).isFalse();
  }
}
