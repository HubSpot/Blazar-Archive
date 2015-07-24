package com.hubspot.blazar.data.service;

import com.google.inject.Inject;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.data.dao.BranchDao;

public class BranchService {
  private final BranchDao branchDao;

  @Inject
  public BranchService(BranchDao branchDao) {
    this.branchDao = branchDao;
  }

  public GitInfo upsert(GitInfo gitInfo) {
    long id = branchDao.upsert(gitInfo);
    return gitInfo.withId(id);
  }

  public void delete(GitInfo gitInfo) {
    branchDao.delete(gitInfo);
  }
}
