package com.hubspot.blazar.data;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.BuildDefinition;
import com.hubspot.blazar.base.Module;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class CachingService<T extends BuildDefinition> {
  private final Map<Integer, T> cache;
  private final AtomicLong cacheTime;
  private final ReadWriteLock lock;

  public CachingService() {
    this.cache = new ConcurrentHashMap<>();
    this.cacheTime = new AtomicLong(0);
    this.lock = new ReentrantReadWriteLock();
  }

  protected abstract Set<T> fetch(long since);

  public Set<T> getAll(long since) {
    update();

    Lock readLock = lock.readLock();
    readLock.lock();
    try {
      Set<T> values = new HashSet<>(cache.values());
      for (Iterator<T> iterator = values.iterator(); iterator.hasNext(); ) {
        T value = iterator.next();
        if (value.getModule().getUpdatedTimestamp() < since) {
          iterator.remove();
        }
      }

      return values;
    } finally {
      readLock.unlock();
    }
  }

  public Optional<T> getByModule(int moduleId) {
    update();

    return Optional.fromNullable(cache.get(moduleId));
  }

  private void update() {
    Lock writeLock = lock.writeLock();
    if (writeLock.tryLock()) {
      try {
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
      } finally {
        writeLock.unlock();
      }
    } else {
      writeLock.lock();
      writeLock.unlock();
    }
  }
}
