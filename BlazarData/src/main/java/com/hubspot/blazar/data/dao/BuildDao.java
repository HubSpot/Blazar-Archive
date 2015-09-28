package com.hubspot.blazar.data.dao;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.Build;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.rosetta.jdbi.BindWithRosetta;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import java.util.List;

public interface BuildDao {

  @SingleValueResult
  @SqlQuery("" +
      "SELECT gitInfo.*, module.*, build.* " +
      "FROM builds AS build " +
      "INNER JOIN modules AS module ON (build.moduleId = module.id) " +
      "INNER JOIN branches AS gitInfo ON (module.branchId = gitInfo.id) " +
      "WHERE build.id = :it")
  Optional<ModuleBuild> get(@Bind long id);

  @SqlQuery("SELECT * FROM builds WHERE moduleId = :id ORDER BY buildNumber DESC")
  List<Build> getAllByModule(@BindWithRosetta Module module);

  @SingleValueResult
  @SqlQuery("SELECT * FROM builds WHERE moduleId = :id AND buildNumber = :buildNumber ORDER BY buildNumber DESC")
  Optional<Build> getByModuleAndNumber(@BindWithRosetta Module module, @Bind("buildNumber") int buildNumber);

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO builds (moduleId, buildNumber, state) VALUES (:moduleId, :buildNumber, :state)")
  long enqueue(@BindWithRosetta Build build);

  @SqlUpdate("UPDATE builds SET startTimestamp = :startTimestamp, sha = :sha, state = :state, commitInfo = :commitInfo, buildConfig = :buildConfig, resolvedConfig = :resolvedConfig WHERE id = :id AND state = 'QUEUED'")
  int begin(@BindWithRosetta Build build);

  @SqlUpdate("UPDATE builds SET log = :log, state = :state WHERE id = :id AND state IN ('LAUNCHING', 'IN_PROGRESS')")
  int update(@BindWithRosetta Build build);

  @SqlUpdate("UPDATE builds SET endTimestamp = :endTimestamp, log = :log, state = :state WHERE id = :id AND state IN ('QUEUED', 'LAUNCHING', 'IN_PROGRESS')")
  int complete(@BindWithRosetta Build build);
}
