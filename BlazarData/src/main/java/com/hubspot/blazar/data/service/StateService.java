package com.hubspot.blazar.data.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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
  // Tracks how long it takes to get a single module state
  private static final String METRIC_FETCH_MODULE_STATE_TIMER = StateService.class.getName() + ".module_state.fetch_time";
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

  public Set<ModuleState> getModuleStatesByBranch(int branchId) {
    LOG.debug("Getting composite state for branch {}", branchId);
    long start = System.currentTimeMillis();
    Set<ModuleState> states = new HashSet<>();
    List<CompletableFuture<ModuleState>> futureModuleStates = new ArrayList<>();
    for (Module m : moduleService.getByBranch(branchId)) {
      futureModuleStates.add(getModuleState(m));
    }
    for (CompletableFuture<ModuleState> future : futureModuleStates) {
      try {
        states.add(future.get());
      } catch (InterruptedException e) {
        LOG.error("Getting module state was for branch {} was interrupted", branchId, e);
      } catch (ExecutionException e) {
        LOG.error("Getting module state was for branch {} encountered an exception", branchId, e);
      }
    }
    metricRegistry.timer(METRIC_FETCH_BRANCH_STATE_TIMER).update(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
    LOG.debug("Got composite state in {}", System.currentTimeMillis() - start);
    return states;
  }

  private CompletableFuture<ModuleState> getModuleState(Module module) {
    return CompletableFuture.supplyAsync(() -> {
      long start = System.currentTimeMillis();
      ModuleState moduleState = ModuleState.newBuilder()
          .setModule(module)
          .setLastSuccessfulBuild(stateDao.getLastSuccessfulBuildInfo(module.getId().get()))
          .setLastNonSkippedBuild(stateDao.getLastNonSkippedBuildInfo(module.getId().get()))
          .setLastBuild(stateDao.getLastBuildInfo(module.getId().get()))
          .setInProgressBuild(stateDao.getInProgressBuildInfo(module.getId().get()))
          .setPendingBuild(stateDao.getPendingBuildInfo(module.getId().get()))
          .build();
      metricRegistry.timer(METRIC_FETCH_MODULE_STATE_TIMER).update(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
      return moduleState;
    });
  }
}
