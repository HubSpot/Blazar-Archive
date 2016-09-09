package com.hubspot.blazar.data.service;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleState;
import com.hubspot.blazar.base.RepositoryState;
import com.hubspot.blazar.data.dao.StateDao;

@Singleton
public class StateService {
  // Tracks how long it takes to get all module states for a branch
  private static final String METRIC_FETCH_BRANCH_STATE_TIMER = StateService.class.getName() + ".branch_state.fetch_time";
  private static final Logger LOG = LoggerFactory.getLogger(StateService.class);
  private final StateDao stateDao;
  private ModuleService moduleService;
  private MetricRegistry metricRegistry;

  @Inject
  public StateService(StateDao stateDao, ModuleService moduleService, MetricRegistry metricRegistry) {
    this.stateDao = stateDao;
    this.moduleService = moduleService;
    this.metricRegistry = metricRegistry;
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

  /**
   * Collects the ModuleState for every module on the branch.
   *
   */
  public Set<ModuleState> getModuleStatesByBranch(int branchId) {
    long start = System.currentTimeMillis();
    Set<ModuleState> states = new HashSet<>();
    Set<Module> modules = moduleService.getByBranch(branchId);
    for (Module m : modules) {
      Optional<ModuleState> optionalState = stateDao.getModuleStateById(m.getId().get());
      if (optionalState.isPresent()) {
        states.add(optionalState.get());
      }
    }
    LOG.info("Built composite state for branch {} in {}", branchId, System.currentTimeMillis() - start);
    return states;
  }

}
