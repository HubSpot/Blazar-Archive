package com.hubspot.blazar.data.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.data.BlazarDataTestBase;
import com.hubspot.blazar.data.BlazarDataTestModule;

@RunWith(JukitoRunner.class)
@UseModules({BlazarDataTestModule.class})
public class BranchServiceTest extends BlazarDataTestBase {
  @Inject
  private BranchService branchService;

  @Test
  public void testUpsertBasic() {
    GitInfo original = newGitInfo(123, "Overwatch", "master");
    GitInfo inserted = branchService.upsert(original);

    assertThat(inserted.getId().isPresent()).isTrue();

    Optional<GitInfo> retrieved = branchService.get(inserted.getId().get());

    assertThat(retrieved.isPresent()).isTrue();
    assertThat(retrieved.get()).isEqualTo(inserted);
    assertThat(retrieved.get().getCreatedTimestamp()).isBetween(System.currentTimeMillis() - 1000, System.currentTimeMillis());
    assertThat(retrieved.get().getUpdatedTimestamp()).isBetween(System.currentTimeMillis() - 1000, System.currentTimeMillis());
    assertThat(retrieved.get().getUpdatedTimestamp()).isEqualTo(retrieved.get().getCreatedTimestamp());
  }

  @Test
  public void testUpsertRepositoryRename() throws InterruptedException {
    long before = System.currentTimeMillis();
    GitInfo original = newGitInfo(123, "Overwatch", "master");
    original = branchService.upsert(original);
    // Embedded Mysql uses 1s precision
    // We have to ensure we wait for at least 1s for updated timestamp to be larger
    Thread.sleep(1000);

    GitInfo renamed = newGitInfo(123, "Underwatch", "master");
    renamed = branchService.upsert(renamed);

    assertThat(renamed.getId().get()).isEqualTo(original.getId().get());

    Optional<GitInfo> retrieved = branchService.get(original.getId().get());

    assertThat(retrieved.isPresent()).isTrue();
    assertThat(retrieved.get()).isEqualTo(renamed);
    assertThat(retrieved.get().getUpdatedTimestamp()).isBetween(before, System.currentTimeMillis());
    assertThat(retrieved.get().getUpdatedTimestamp()).isGreaterThan(retrieved.get().getCreatedTimestamp());
  }

  @Test
  public void testUpsertMultipleBranches() {
    GitInfo master = newGitInfo(123, "Overwatch", "master");
    master = branchService.upsert(master);

    GitInfo branch = newGitInfo(123, "Overwatch", "branch");
    branch = branchService.upsert(branch);

    assertThat(branch.getId().get()).isNotEqualTo(master.getId().get());

    Optional<GitInfo> masterRetrieved = branchService.get(master.getId().get());

    assertThat(masterRetrieved.isPresent()).isTrue();
    assertThat(masterRetrieved.get()).isEqualTo(master);

    Optional<GitInfo> branchRetrieved = branchService.get(branch.getId().get());

    assertThat(branchRetrieved.isPresent()).isTrue();
    assertThat(branchRetrieved.get()).isEqualTo(branch);
  }

  private static GitInfo newGitInfo(int repositoryId, String repositoryName, String branch) {
    return new GitInfo(Optional.<Integer>absent(), "github", "HubSpot", repositoryName, repositoryId, branch, true, System.currentTimeMillis(), System.currentTimeMillis());
  }
}
