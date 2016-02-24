package com.hubspot.blazar.data.dao;


import java.util.Set;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.slack.SlackConfiguration;
import com.hubspot.rosetta.jdbi.BindWithRosetta;

public interface SlackConfigurationDao {

  @SqlQuery("SELECT * FROM slack_configs where active = 1")
  Set<SlackConfiguration> getAll();

  @SingleValueResult
  @SqlQuery("SELECT * FROM slack_configs WHERE id = :id")
  Optional<SlackConfiguration> get(@Bind("id") long id);

  @SqlQuery("SELECT * FROM slack_configs WHERE branchId = :branchId and active = 1")
  Set<SlackConfiguration> getAllWithBranchId(long branchId);

  @SqlQuery("SELECT * FROM slack_configs WHERE moduleId = :moduleId and active = 1")
  Set<SlackConfiguration> getAllWithModuleId(long moduleId);

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO slack_configs (branchId, moduleId, channelName, onFinish, onFail, onChange, onRecover, active) VALUES (:branchId, :moduleId, :channelName, :onFinish, :onFail, :onChange, :onRecover, :active)")
  long insert(@BindWithRosetta SlackConfiguration slackConfiguration);

  @SqlUpdate("INSERT INTO slack_configs (branchId, moduleId, channelName, onFinish, onFail, onChange, onRecover, active) VALUES (:branchId, :moduleId, :channelName, :onFinish, :onFail, :onChange, :onRecover, :active)")
  int update(@BindWithRosetta SlackConfiguration slackConfiguration);

  @SqlUpdate("UPDATE slack_configs SET active = 0 WHERE id = :id")
  int delete(@Bind("id") long id);

}
