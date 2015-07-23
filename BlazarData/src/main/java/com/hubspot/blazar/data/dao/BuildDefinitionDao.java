package com.hubspot.blazar.data.dao;

import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.rosetta.jdbi.BindWithRosetta;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.util.Set;

public interface BuildDefinitionDao {

  @SqlQuery("SELECT name, path FROM build_definitions WHERE host = :host AND organization = :organization AND repository = :repository AND branch = :branch")
  Set<Module> getModules(@BindWithRosetta GitInfo gitInfo);

  @SqlUpdate("INSERT INTO build_defintions (host, organization, repository, branch, name, path) VALUES (:host, :organization, :repository, :branch, :name, :path)")
  int insertModule(@BindWithRosetta GitInfo gitInfo, @BindWithRosetta Module module);

  @SqlUpdate("DELETE FROM build_definitions WHERE host = :host AND organization = :organization AND repository = :repository AND branch = :branch AND name = :name AND path = :path")
  int deleteModule(@BindWithRosetta GitInfo gitInfo, @BindWithRosetta Module module);
}
