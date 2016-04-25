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
  Set<InterProjectBuildMapping> getMappingsForBuild(@BindWithRosetta InterProjectBuild interProjectBuild);

  @SqlQuery("SELECT * FROM inter_project_build_mappings WHERE interProjectBuildId = :interProjectBuildId and repoId = :repoId")
  Set<InterProjectBuildMapping> getMappingsForRepo(@Bind("interProjectBuildId") long interProjectBuildId, @Bind("repoId") int repoId);

   @SqlQuery("SELECT * FROM inter_project_build_mappings WHERE interProjectBuildId = :interProjectBuildId and moduleId = :moduleId")
  Set<InterProjectBuildMapping> getMappingsForModule(@Bind("interProjectBuildId") long interProjectBuildId, @Bind("moduleId") int moduleId);

  @SqlQuery("SELECT * FROM inter_project_build_mappings WHERE repoBuildId = :repoBuildId")
  Set<InterProjectBuildMapping> getByRepoBuildId(@Bind("repoBuildId") long repoBuildId);

  @SqlQuery("SELECT * FROM inter_project_build_mappings WHERE moduleBuildId = :moduleBuildId")
  Set<InterProjectBuildMapping> getByModuleBuildId(@Bind("moduleBuildId") long moduleBuildId);

  @SingleValueResult
  @SqlQuery("SELECT * FROM inter_project_build_mappings WHERE id = :id")
  Optional<InterProjectBuildMapping> getById(@Bind("id") long id);

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO inter_project_build_mappings (interProjectBuildId, repoId, repoBuildId, moduleId, moduleBuildId) " +
             "VALUES (:interProjectBuildId, :repoId, :repoBuildId, :moduleId, :moduleBuildId)")
  long insert(@BindWithRosetta InterProjectBuildMapping interProjectBuildMapping);

  @SqlUpdate("UPDATE inter_project_build_mappings SET repoBuildId = :repoBuildId, moduleBuildId = :moduleBuildId where id = :id")
  int updateBuilds(@BindWithRosetta InterProjectBuildMapping mapping);
}
