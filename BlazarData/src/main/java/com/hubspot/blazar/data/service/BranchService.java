package com.hubspot.blazar.data.service;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.data.dao.BranchDao;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;

import java.sql.SQLIntegrityConstraintViolationException;

public class BranchService {
  private final BranchDao branchDao;

  @Inject
  public BranchService(BranchDao branchDao) {
    this.branchDao = branchDao;
  }

  public Optional<GitInfo> lookup(String host, String organization, String repository, String branch) {
    return branchDao.lookup(host, organization, repository, branch);
  }

  public Optional<GitInfo> lookup(GitInfo gitInfo) {
    return branchDao.get(gitInfo);
  }

  public GitInfo upsert(GitInfo gitInfo) {
    Optional<GitInfo> existing = lookup(gitInfo);
    if (existing.isPresent()) {
      gitInfo = gitInfo.withId(existing.get().getId().get());

      if (!existing.get().equals(gitInfo)) {
        int updated = branchDao.update(gitInfo);
        Preconditions.checkState(updated == 1, "Expected to update 1 row but updated %s", updated);
      }

      return gitInfo;
    } else {
      try {
        int id = branchDao.insert(gitInfo);
        return gitInfo.withId(id);
      } catch (UnableToExecuteStatementException e) {
        if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
          return branchDao.get(gitInfo).get();
        } else {
          throw e;
        }
      }
    }
  }

  public void delete(GitInfo gitInfo) {
    branchDao.delete(gitInfo);
  }
}
