package com.hubspot.blazar.data.service;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.data.BlazarDataTestBase;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

public class BranchServiceTest extends BlazarDataTestBase {
  private BranchService branchService;

  @Before
  public void before() {
    this.branchService = getFromGuice(BranchService.class);
  }

  @Test
  public void itCreatesABranch() {
    GitInfo gitInfo = branchService.upsert(new GitInfo(Optional.<Integer>absent(), "git.hubteam.com", "HubSpot", "Overwatch", 123, "master", true, System.currentTimeMillis(), System.currentTimeMillis()));
    Set<GitInfo> results = branchService.getAll();

    int i = 1;
  }

  @Test
  public void itReturnsAllBranches() {
    Set<GitInfo> results = branchService.getAll();
    System.out.println(results.size());
  }
}
