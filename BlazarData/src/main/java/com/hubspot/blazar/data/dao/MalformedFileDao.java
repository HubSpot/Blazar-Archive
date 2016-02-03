package com.hubspot.blazar.data.dao;

import com.hubspot.blazar.base.MalformedFile;
import com.hubspot.rosetta.jdbi.BindWithRosetta;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.util.Set;

public interface MalformedFileDao {

  @SqlQuery("SELECT * FROM malformed_files WHERE branchId = :branchId")
  Set<MalformedFile> getMalformedFiles(@Bind("branchId") int branchId);

  @SqlBatch("INSERT INTO malformed_files (branchId, type, path, details) VALUES (:branchId, :type, :path, :details)")
  void insertMalformedFile(@BindWithRosetta Set<MalformedFile> malformedFiles);

  @SqlUpdate("DELETE FROM malformed_files WHERE branchId = :branchId")
  void clearMalformedFiles(@Bind("branchId") int branchId);
}
