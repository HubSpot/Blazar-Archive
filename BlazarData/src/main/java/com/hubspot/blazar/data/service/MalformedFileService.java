package com.hubspot.blazar.data.service;

import com.hubspot.blazar.base.MalformedFile;
import com.hubspot.blazar.data.dao.MalformedFileDao;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.Set;

@Singleton
public class MalformedFileService {
  private final MalformedFileDao malformedFileDao;

  @Inject
  public MalformedFileService(MalformedFileDao malformedFileDao) {
    this.malformedFileDao = malformedFileDao;
  }

  public Set<MalformedFile> getMalformedFiles(int branchId) {
    return malformedFileDao.getMalformedFiles(branchId);
  }

  @Transactional
  public void setMalformedFiles(Set<MalformedFile> malformedFiles) {
    if (malformedFiles.isEmpty()) {
      return;
    }

    int branchId = malformedFiles.iterator().next().getBranchId();
    for (MalformedFile file : malformedFiles) {
      if (file.getBranchId() != branchId) {
        throw new IllegalArgumentException("All files must be for the same repository");
      }
    }

    malformedFileDao.clearMalformedFiles(branchId);
    malformedFileDao.insertMalformedFile(malformedFiles);
  }
}
