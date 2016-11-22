package com.hubspot.blazar.data.service;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.RepositoryState;
import com.hubspot.blazar.data.dao.StateDao;

@Singleton
public class StateService {
  // Tracks how long it takes to get all module states for a branch
  private static final Logger LOG = LoggerFactory.getLogger(StateService.class);
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
}
