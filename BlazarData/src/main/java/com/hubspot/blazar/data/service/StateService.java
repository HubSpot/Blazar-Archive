package com.hubspot.blazar.data.service;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.ModuleState;
import com.hubspot.blazar.base.RepositoryState;
import com.hubspot.blazar.data.dao.StateDao;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

@Singleton
public class StateService {
  private final StateDao stateDao;

  @Inject
  public StateService(StateDao stateDao) {
    this.stateDao = stateDao;
  }

  public Set<RepositoryState> getAllRepositoryStates() {
    return stateDao.getAllRepositoryStates();
  }

  public Set<RepositoryState> getChangedRepositoryStates(long since) {
    return stateDao.getChangedRepositoryStates(since);
  }

  public Optional<RepositoryState> getRepositoryState(int branchId) {
    return stateDao.getRepositoryState(branchId);
  }

  public Set<ModuleState> getModuleStatesByBranch(int branchId) {
    return stateDao.getModuleStatesByBranch(branchId);
  }
}
