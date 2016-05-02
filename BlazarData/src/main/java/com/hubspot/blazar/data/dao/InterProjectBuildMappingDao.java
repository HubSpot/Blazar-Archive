package com.hubspot.blazar.data.dao;

import java.util.Set;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.InterProjectBuildMapping;
import com.hubspot.rosetta.jdbi.BindWithRosetta;

public interface InterProjectBuildMappingDao {

  @SqlQuery("SELECT * FROM inter_project_build_mappings WHERE interProjectBuildId = :id")
  Set<InterProjectBuildMapping> getMappingsForInterProjectBuild(@BindWithRosetta InterProjectBuild interProjectBuild);

  @SqlQuery("SELECT * FROM inter_project_build_mappings WHERE interProjectBuildId = :interProjectBuildId and branchId = :branchId")
  Set<InterProjectBuildMapping> getMappingsForRepo(@Bind("interProjectBuildId") long interProjectBuildId, @Bind("branchId") int branchId);

   @SqlQuery("SELECT * FROM inter_project_build_mappings WHERE interProjectBuildId = :interProjectBuildId and moduleId = :moduleId")
  Set<InterProjectBuildMapping> getMappingsForModule(@Bind("interProjectBuildId") long interProjectBuildId, @Bind("moduleId") int moduleId);

  @SqlQuery("SELECT * FROM inter_project_build_mappings WHERE repoBuildId = :repoBuildId")
  Set<InterProjectBuildMapping> getByRepoBuildId(@Bind("repoBuildId") long repoBuildId);

  @SingleValueResult
  @SqlQuery("SELECT * FROM inter_project_build_mappings WHERE moduleBuildId = :moduleBuildId")
  Optional<InterProjectBuildMapping> getByModuleBuildId(@Bind("moduleBuildId") long moduleBuildId);

  @SingleValueResult
  @SqlQuery("SELECT * FROM inter_project_build_mappings WHERE id = :id")
  Optional<InterProjectBuildMapping> getByMappingId(@Bind("id") long id);

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO inter_project_build_mappings (interProjectBuildId, branchId, repoBuildId, moduleId, moduleBuildId, state) " +
             "VALUES (:interProjectBuildId, :branchId, :repoBuildId, :moduleId, :moduleBuildId, :state)")
  long insert(@BindWithRosetta InterProjectBuildMapping interProjectBuildMapping);

  @SqlUpdate("UPDATE inter_project_build_mappings SET repoBuildId = :repoBuildId, moduleBuildId = :moduleBuildId, state = :state where id = :id")
  int updateBuilds(@BindWithRosetta InterProjectBuildMapping mapping);
}
