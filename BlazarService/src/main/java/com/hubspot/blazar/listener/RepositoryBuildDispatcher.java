package com.hubspot.blazar.listener;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.listener.RepositoryBuildListener;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.exception.NonRetryableBuildException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;

@Singleton
public class RepositoryBuildDispatcher {
  private static final Logger LOG = LoggerFactory.getLogger(RepositoryBuildDispatcher.class);

  private final RepositoryBuildService repositoryBuildService;
  private final Map<RepositoryBuild.State, Set<RepositoryBuildListener>> listeners;

  @Inject
  public RepositoryBuildDispatcher(RepositoryBuildService repositoryBuildService,
                                   Map<RepositoryBuild.State, Set<RepositoryBuildListener>> listeners,
                                   EventBus eventBus) {
    this.repositoryBuildService = repositoryBuildService;
    this.listeners = listeners;

    eventBus.register(this);
  }

  @Subscribe
  public void dispatch(RepositoryBuild build) throws Exception {
    try {
      if (listeners.containsKey(build.getState())) {
        for (RepositoryBuildListener listener : listeners.get(build.getState())) {
          listener.buildChanged(build);
        }
      }
    } catch (NonRetryableBuildException e) {
      LOG.warn("Failing build {}", build.getId().get(), e);
      repositoryBuildService.fail(build);
    }
  }
}
