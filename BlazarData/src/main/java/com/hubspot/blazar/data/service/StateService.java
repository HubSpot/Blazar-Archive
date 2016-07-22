package com.hubspot.blazar.data.service;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleState;
import com.hubspot.blazar.base.RepositoryState;
import com.hubspot.blazar.data.dao.StateDao;

@Singleton
public class StateService {
  private final StateDao stateDao;
  private ModuleService moduleService;

  @Inject
  public StateService(StateDao stateDao, ModuleService moduleService) {
    this.stateDao = stateDao;
    this.moduleService = moduleService;
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
    Set<ModuleState> states = new HashSet<>();
    for (Module m : moduleService.getByBranch(branchId)) {
      Optional<ModuleState> state = stateDao.getModuleStateById(m);
      if (state.isPresent()) {
        states.add(state.get());
      }
    }
    return states;
  }
}
