package com.hubspot.blazar.data.service;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.BranchSetting;
import com.hubspot.blazar.data.dao.BranchSettingsDao;

public class BranchSettingsService {

  private final BranchSettingsDao dao;

  @Inject
  public BranchSettingsService(BranchSettingsDao dao) {
    this.dao = dao;
  }

  public Optional<BranchSetting> getByBranchId(int branchId) {
    return dao.getByBranchId(branchId);
  }

  public void insert(BranchSetting branchSetting) {
    dao.insert(branchSetting);
  }

  public void update(BranchSetting branchSetting) {
    dao.update(branchSetting);
  }

}
