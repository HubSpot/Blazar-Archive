package com.hubspot.blazar.data.service;

import java.util.Set;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.slack.SlackConfiguration;
import com.hubspot.blazar.data.dao.SlackConfigurationDao;

public class SlackConfigurationService {

  private SlackConfigurationDao slackConfigurationDao;

  @Inject
  public SlackConfigurationService(SlackConfigurationDao slackConfigurationDao) {
    this.slackConfigurationDao = slackConfigurationDao;
  }

  public Set<SlackConfiguration> getAll(){
    return slackConfigurationDao.getAll();
  }

  public Optional<SlackConfiguration> get(long id) {
    return slackConfigurationDao.get(id);
  }

  public Set<SlackConfiguration> getAllWithBranchId(long branchId) {
    return slackConfigurationDao.getAllWithBranchId(branchId);
  }

  public long insert(SlackConfiguration slackConfiguration) {
    return slackConfigurationDao.insert(slackConfiguration);
  }

  public int update(SlackConfiguration slackConfiguration) {
    return slackConfigurationDao.update(slackConfiguration);
  }

  public int delete(long id) {
    return slackConfigurationDao.delete(id);
  }

}
