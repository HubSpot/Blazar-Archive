package com.hubspot.blazar.data;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Sets;
import com.hubspot.blazar.base.BuildDefinition;
import com.hubspot.blazar.base.Module;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public abstract class CachingService<T extends BuildDefinition> {
  private final Map<Integer, T> cache;
  private final AtomicLong cacheTime;
  private final AtomicLong updateCount;
  private final Predicate<T> active;

  public CachingService() {
    this.cache = new ConcurrentHashMap<>();
    this.cacheTime = new AtomicLong(0);
    this.updateCount = new AtomicLong(0);
    this.active = new Predicate<T>() {
      @Override
      public boolean apply(T definition) {
        return definition.getGitInfo().isActive() && definition.getModule().isActive();
      }
    };
  }

  protected abstract Set<T> fetch(long since);

  public Set<T> getAll(long since) {
    update();

    Set<T> values = new HashSet<>(cache.values());
    for (Iterator<T> iterator = values.iterator(); iterator.hasNext(); ) {
      T value = iterator.next();
      if (value.getModule().getUpdatedTimestamp() < since) {
        iterator.remove();
      }
    }

    return values;
  }

  public Set<T> getAllActive(long since) {
    return Sets.filter(getAll(since), active);
  }

  public Set<T> getAllInactive(long since) {
    return Sets.filter(getAll(since), Predicates.not(active));
  }

  public Optional<T> getByModule(int moduleId) {
    update();

    return Optional.fromNullable(cache.get(moduleId));
  }

  private void update() {
    long count = updateCount.get();
    synchronized (this) {
      if (updateCount.get() == count) {
        Set<T> changes = fetch(cacheTime.get());

        long maxTime = cacheTime.get();
        for (T change : changes) {
          Module module = change.getModule();
          cache.put(module.getId().get(), change);
          if (module.getUpdatedTimestamp() > maxTime) {
            maxTime = module.getUpdatedTimestamp();
          }
        }

        cacheTime.set(maxTime);
        updateCount.incrementAndGet();
      }
    }
  }
}
