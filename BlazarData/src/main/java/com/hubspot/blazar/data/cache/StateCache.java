package com.hubspot.blazar.data.cache;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hubspot.blazar.base.RepositoryState;
import com.hubspot.blazar.data.service.StateService;

@Singleton
public class StateCache {
  private final StateService stateService;
  private final Map<Integer, RepositoryState> cache;
  private final AtomicLong cacheTimestamp;
  private final ReadWriteLock lock;

  @Inject
  public StateCache(StateService stateService) {
    this.stateService = stateService;
    this.cache = new ConcurrentHashMap<>();

    long maxTime = 0;
    for (RepositoryState state : stateService.getAllRepositoryStates()) {
      cache.put(state.getGitInfo().getId().get(), state);

      if (state.getGitInfo().getUpdatedTimestamp() > maxTime) {
        maxTime = state.getGitInfo().getUpdatedTimestamp();
      }
    }
    this.cacheTimestamp = new AtomicLong(maxTime);
    this.lock = new ReentrantReadWriteLock();
  }

  public Set<RepositoryState> getAllRepositoryStates() {
    updateCache();

    Lock readLock = lock.readLock();
    readLock.lock();
    try {
      return new HashSet<>(cache.values());
    } finally {
      readLock.unlock();
    }
  }

  private void updateCache() {
    Lock writeLock = lock.writeLock();
    if (writeLock.tryLock()) {
      try {
        Set<RepositoryState> changes = stateService.getChangedRepositoryStates(cacheTimestamp.get());

        long maxTime = cacheTimestamp.get();
        for (RepositoryState change : changes) {
          int branchId = change.getGitInfo().getId().get();
          if (change.getGitInfo().isActive()) {
            cache.put(branchId, change);
          } else {
            cache.remove(branchId);
          }

          if (change.getGitInfo().getUpdatedTimestamp() > maxTime) {
            maxTime = change.getGitInfo().getUpdatedTimestamp();
          }
        }

        cacheTimestamp.set(maxTime);
      } finally {
        writeLock.unlock();
      }
    }
  }
}
