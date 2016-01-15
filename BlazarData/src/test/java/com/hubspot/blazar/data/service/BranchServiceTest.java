package com.hubspot.blazar.data.service;

import com.google.common.base.Optional;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.data.BlazarDataTestBase;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BranchServiceTest extends BlazarDataTestBase {
  private BranchService branchService;

  @Before
  public void before() {
    this.branchService = getFromGuice(BranchService.class);
  }

  @Test
  public void itCreatesBranch() {
    GitInfo original = newGitInfo("Overwatch");
    GitInfo inserted = branchService.upsert(original);

    assertThat(inserted.getId().isPresent()).isTrue();

    Optional<GitInfo> retrieved = branchService.get(inserted.getId().get());

    assertThat(retrieved.isPresent()).isTrue();
    assertThat(retrieved.get()).isEqualTo(inserted);
  }

  @Test
  public void itUpdatesRepositoryName() {
    GitInfo original = newGitInfo("Overwatch");
    GitInfo inserted = branchService.upsert(original);

    GitInfo renamed = newGitInfo("Underwatch");
    GitInfo updated = branchService.upsert(renamed);

    assertThat(updated.getId().get()).isEqualTo(inserted.getId().get());

    Optional<GitInfo> retrieved = branchService.get(inserted.getId().get());

    assertThat(retrieved.isPresent()).isTrue();
    assertThat(retrieved.get()).isEqualTo(updated);
  }

  private static GitInfo newGitInfo(String repositoryName) {
    return new GitInfo(Optional.<Integer>absent(), "github", "HubSpot", repositoryName, 123, "master", true, System.currentTimeMillis(), System.currentTimeMillis());
  }
}
