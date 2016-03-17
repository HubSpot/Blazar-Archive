package com.hubspot.blazar.data.dao;

import java.util.Set;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.InterProjectBuildMapping;
import com.hubspot.rosetta.jdbi.BindWithRosetta;

public interface InterProjectRepositoryBuildMappingDao {

  @SqlQuery("SELECT * FROM inter_project_repo_build_mappings WHERE interProjectBuildId = :id")
  Set<InterProjectBuildMapping> getMappingsForInterProjectBuild(@BindWithRosetta InterProjectBuild interProjectBuild);

  @SingleValueResult
  @SqlQuery("SELECT interProjectBuildId FROM inter_project_repo_build_mappings WHERE repoBuildId = :buildId")
  Optional<InterProjectBuildMapping> findByBuildId(@Bind("buildId") long buildId);

  @SqlUpdate("INSERT INTO inter_project_repo_build_mappings (interProjectBuildId, branchId, repoBuildId)" +
             "VALUES (:interProjectBuildId, :mappingId, :buildId)")
  int insert(@BindWithRosetta InterProjectBuildMapping interProjectBuildMapping);
}
