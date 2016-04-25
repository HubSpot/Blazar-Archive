package com.hubspot.blazar.data.service;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.InterProjectBuildMapping;
import com.hubspot.blazar.data.dao.InterProjectBuildDao;

public class InterProjectBuildService {

  private static final Logger LOG = LoggerFactory.getLogger(InterProjectBuildService.class);
  private InterProjectBuildDao interProjectBuildDao;
  private EventBus eventBus;

  @Inject
  public InterProjectBuildService(InterProjectBuildDao interProjectBuildDao, EventBus eventBus) {
    this.interProjectBuildDao = interProjectBuildDao;
    this.eventBus = eventBus;
  }

  public Optional<InterProjectBuild> getWithId(long id) {
    return interProjectBuildDao.getWithId(id);
  }

  public long enqueue(InterProjectBuild interProjectBuild) {
    if (!(interProjectBuild.getState() == InterProjectBuild.State.CALCULATING)) {
      throw new IllegalArgumentException("Cannot Queue Build with state that has progressed further than CALCULATING");
    }
    long id = interProjectBuildDao.enqueue(interProjectBuild);
    InterProjectBuild build = interProjectBuildDao.getWithId(id).get();
    LOG.info("Enqueued InterRepo build {} for moduleIds {}", id, build.getModuleIds());
    eventBus.post(build);
    return id;
  }

  public void start(InterProjectBuild build) {
    InterProjectBuild started = InterProjectBuild.getStarted(build);
    interProjectBuildDao.start(started);
    eventBus.post(started);
  }

  public void finish(InterProjectBuild build) {
    interProjectBuildDao.finish(build);
  }

  public InterProjectBuild getBuildAssociatedWithMappings(Set<InterProjectBuildMapping> mappings) throws IllegalStateException {
    Set<InterProjectBuild> builds = new HashSet<>();
    for (InterProjectBuildMapping m : mappings) {
      LOG.info("{}", m);
      Optional<InterProjectBuild> b = getWithId(m.getInterProjectBuildId());
      if (!b.isPresent()) {
        throw new IllegalStateException(String.format("Cannot have mapping with no parent InterProjectBuild %s", m.toString()));
      }
      builds.add(b.get());
    }
    if (builds.size() > 1) {
      throw new IllegalStateException("Should not have multiple InterProjectBuilds claiming ownership of a module build");
    }
    return (InterProjectBuild) builds.toArray()[0];
  }
}
