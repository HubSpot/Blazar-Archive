package com.hubspot.blazar.data.dao;


import java.util.Set;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.slack.SlackConfiguration;
import com.hubspot.rosetta.jdbi.BindWithRosetta;

public interface SlackConfigurationDao {

  @SqlQuery("SELECT * FROM slack_configs")
  Set<SlackConfiguration> getAll();

  @SqlQuery("SELECT * FROM slack_configs WHERE id = :id")
  Optional<SlackConfiguration> get(@Bind("id") long id);

  @SqlQuery("SELECT * FROM slack_configs WHERE repositoryId = :repositoryId and active = 1")
  Set<SlackConfiguration> getAllWithRepositoryId(long repositoryId);

  @SqlQuery("SELECT * FROM slack_configs WHERE moduleId = :moduleId and active = 1")
  Set<SlackConfiguration> getAllWithModuleId(long moduleId);

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO slack_configs (repositoryId, moduleId, channelName, onFinish, onFail, onChange, onRecover, active) VALUES (:repositoryId, :moduleId, :channelName, :onFinish, :onFail, :onChange, :onRecover, :active)")
  long insert(@BindWithRosetta SlackConfiguration slackConfiguration);

  @SqlUpdate("INSERT INTO slack_configs (repositoryId, moduleId, channelName, onFinish, onFail, onChange, onRecover, active) VALUES (:repositoryId, :moduleId, :channelName, :onFinish, :onFail, :onChange, :onRecover, :active)")
  int update(@BindWithRosetta SlackConfiguration slackConfiguration);

  @SqlUpdate("UPDATE slack_configs SET active = 0 WHERE id = :id")
  int delete(@Bind("id") long id);

}
