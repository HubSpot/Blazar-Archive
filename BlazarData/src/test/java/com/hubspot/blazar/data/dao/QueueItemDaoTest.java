package com.hubspot.blazar.data.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.Set;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;
import com.hubspot.blazar.base.BranchSetting;
import com.hubspot.blazar.data.BlazarDataTestModule;
import com.hubspot.blazar.data.queue.QueueItem;
import com.hubspot.blazar.test.base.service.DatabaseBackedTest;

@RunWith(JukitoRunner.class)
@UseModules({BlazarDataTestModule.class})
public class QueueItemDaoTest extends DatabaseBackedTest {
  @Inject
  private QueueItemDao queueItemDao;

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
