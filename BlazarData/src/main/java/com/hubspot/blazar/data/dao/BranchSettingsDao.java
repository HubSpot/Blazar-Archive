package com.hubspot.blazar.data.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.BranchSetting;
import com.hubspot.rosetta.jdbi.BindWithRosetta;

public interface BranchSettingsDao {

  @SingleValueResult
  @SqlQuery("SELECT * FROM branch_settings where branchId = :branchId")
  Optional<BranchSetting> getByBranchId(@Bind("branchId") int branchId);


  @SqlUpdate("INSERT INTO branch_settings (branchId, triggerInterProjectBuilds, interProjectBuildOptIn) VALUES (:branchId, :triggerInterProjectBuilds, :interProjectBuildOptIn)")
  int insert(@BindWithRosetta BranchSetting branchSetting);

  @SqlUpdate("UPDATE branch_settings SET triggerInterProjectBuilds = :triggerInterProjectBuilds, " +
                                        "interProjectBuildOptIn = :interProjectBuildOptIn " +
             "WHERE branchId = :branchId")
  int update(@BindWithRosetta BranchSetting branchSetting);
}
