package com.hubspot.blazar.data.service;

import java.util.Set;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.notifications.InstantMessageConfiguration;
import com.hubspot.blazar.data.dao.InstantMessageConfigurationDao;

public class InstantMessageConfigurationService {

  private InstantMessageConfigurationDao instantMessageConfigurationDao;

  @Inject
  public InstantMessageConfigurationService(InstantMessageConfigurationDao instantMessageConfigurationDao) {
    this.instantMessageConfigurationDao = instantMessageConfigurationDao;
  }

  public Set<InstantMessageConfiguration> getAll(){
    return instantMessageConfigurationDao.getAll();
  }

  public Optional<InstantMessageConfiguration> get(long id) {
    return instantMessageConfigurationDao.get(id);
  }

  public Set<InstantMessageConfiguration> getAllWithBranchId(long branchId) {
    return instantMessageConfigurationDao.getAllWithBranchId(branchId);
  }

  public Set<InstantMessageConfiguration> getAllWithModuleId(long moduleId) {
    return instantMessageConfigurationDao.getAllWithModuleId(moduleId);
  }

  public long insert(InstantMessageConfiguration instantMessageConfiguration) {
    return instantMessageConfigurationDao.insert(instantMessageConfiguration);
  }

  public int update(InstantMessageConfiguration instantMessageConfiguration) {
    return instantMessageConfigurationDao.update(instantMessageConfiguration);
  }

  public int delete(long id) {
    return instantMessageConfigurationDao.delete(id);
  }
}
