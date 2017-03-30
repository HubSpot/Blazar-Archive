package com.hubspot.blazar.data.service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Set;

import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;

import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.data.dao.BranchDao;

public class BranchService {
  private static final Logger LOG = LoggerFactory.getLogger(BranchService.class);
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

  @Transactional
  public GitInfo upsert(GitInfo gitInfo) {
    Optional<GitInfo> existing = getByRepositoryAndBranch(gitInfo.getRepositoryId(), gitInfo.getBranch());
    if (existing.isPresent()) {
      gitInfo = gitInfo.withId(existing.get().getId().get());

      if (!existing.get().equals(gitInfo)) {
        int updated = branchDao.update(gitInfo);
        Preconditions.checkState(updated == 1, "Expected to update 1 row but updated %s", updated);
        handleConflictingBranches(gitInfo);
      }

      return gitInfo;
    } else {
      try {
        int id = branchDao.insert(gitInfo);
        handleConflictingBranches(gitInfo);
        return gitInfo.withId(id);
      } catch (UnableToExecuteStatementException e) {
        if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
          return getByRepositoryAndBranch(gitInfo.getRepositoryId(), gitInfo.getBranch()).get();
        } else {
          throw e;
        }
      }
    }
  }

  private void handleConflictingBranches(GitInfo gitInfo) {
    for (GitInfo conflicting : branchDao.getConflictingBranches(gitInfo)) {
      LOG.warn("Found {} which conflicts with updated {} marking the former as inactive", conflicting, gitInfo);
      deactivate(conflicting);
    }
  }


  public void deactivate(GitInfo gitInfo) {
    branchDao.deactivate(gitInfo);
  }
}
