package com.hubspot.blazar.data.service;

import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.ModuleDependency;
import com.hubspot.blazar.data.dao.DependenciesDao;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Set;

public class DependenciesService {
  private final DependenciesDao dependenciesDao;

  @Inject
  public DependenciesService(DependenciesDao dependenciesDao) {
    this.dependenciesDao = dependenciesDao;
  }

  public Set<ModuleDependency> getProvides(GitInfo gitInfo) {
    return dependenciesDao.getProvides(gitInfo);
  }

  public Set<ModuleDependency> getDepends(GitInfo gitInfo) {
    return dependenciesDao.getDepends(gitInfo);
  }

  @Transactional
  public void insert(DiscoveredModule module) {
    dependenciesDao.insertProvides(module.getProvides());
    dependenciesDao.insertDepends(module.getDepends());
  }

  @Transactional
  public void update(DiscoveredModule module) {
    updateProvides(module);
    updateDepends(module);
  }

  @Transactional
  public void delete(int moduleId) {
    dependenciesDao.deleteProvides(moduleId);
    dependenciesDao.deleteDepends(moduleId);
  }

  private void updateProvides(DiscoveredModule module) {
    dependenciesDao.deleteProvides(module.getId().get());
    dependenciesDao.insertProvides(module.getProvides());
  }

  private void updateDepends(DiscoveredModule module) {
    dependenciesDao.deleteDepends(module.getId().get());
    dependenciesDao.insertDepends(module.getDepends());
  }
}
