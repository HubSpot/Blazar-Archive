package com.hubspot.blazar.data.service;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.data.BlazarDataModule;
import com.hubspot.blazar.data.BlazarDataTestModule;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Set;

public class BranchServiceTest {
  private static BranchService branchService;

  @BeforeClass
  public static void setup() {
    Injector injector = Guice.createInjector(new BlazarDataTestModule(), new BlazarDataModule());
    branchService = injector.getInstance(BranchService.class);
  }

  @Test
  public void itReturnsAllBranches() {
    Set<GitInfo> results = branchService.getAll();
    System.out.println(results.size());
  }
}
