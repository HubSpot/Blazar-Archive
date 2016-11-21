package com.hubspot.blazar.data.service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Set;

import javax.ws.rs.NotFoundException;

import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.data.dao.BranchDao;

public class BranchService {
  private final BranchDao branchDao;

  @Inject
  public BranchService(BranchDao branchDao) {
    this.branchDao = branchDao;
  }

  public Set<GitInfo> getAll() {
    return branchDao.getAll();
  }

  public Set<GitInfo> getAllActive() {
    return branchDao.getAllActive();
  }

  public Optional<GitInfo> get(int id) {
    return branchDao.get(id);
  }

  public Set<GitInfo> getByRepository(int repositoryId) {
    return branchDao.getByRepository(repositoryId);
  }

  public Optional<GitInfo> getByRepositoryAndBranch(int repositoryId, String branch) {
    return branchDao.getByRepositoryAndBranch(repositoryId, branch);
  }

  public void checkBranchExists(int branchId) {
    Optional<GitInfo> maybeBranch = get(branchId);
    if (!maybeBranch.isPresent()) {
      throw new NotFoundException(String.format("Could not find branch with id %d", branchId));
    }
  }

  public GitInfo upsert(GitInfo gitInfo) {
    Optional<GitInfo> existing = getByRepositoryAndBranch(gitInfo.getRepositoryId(), gitInfo.getBranch());
    if (existing.isPresent()) {
      gitInfo = gitInfo.toBuilder().setId(Optional.of(existing.get().getId().get())).build();

      if (!existing.get().equals(gitInfo)) {
        int updated = branchDao.update(gitInfo);
        Preconditions.checkState(updated == 1, "Expected to update 1 row but updated %s", updated);
      }

      return gitInfo;
    } else {
      try {
        int id = branchDao.insert(gitInfo);
        return gitInfo.toBuilder().setId(Optional.of(id)).build();
      } catch (UnableToExecuteStatementException e) {
        if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
          return getByRepositoryAndBranch(gitInfo.getRepositoryId(), gitInfo.getBranch()).get();
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
