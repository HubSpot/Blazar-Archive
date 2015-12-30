package com.hubspot.blazar.util;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.github.GitHubProtos.CreateEvent;
import com.hubspot.blazar.github.GitHubProtos.DeleteEvent;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;
import com.hubspot.blazar.github.GitHubProtos.Repository;
import org.kohsuke.github.GHRepository;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

@Singleton
public class GitHubWebhookHandler {
  private final BranchService branchService;
  private final RepositoryBuildService repositoryBuildService;
  private final GitHubHelper gitHubHelper;
  private final Set<String> whitelist;

  @Inject
  public GitHubWebhookHandler(BranchService branchService,
                              RepositoryBuildService repositoryBuildService,
                              GitHubHelper gitHubHelper,
                              @Named("whitelist") Set<String> whitelist,
                              EventBus eventBus) {
    this.branchService = branchService;
    this.repositoryBuildService = repositoryBuildService;
    this.gitHubHelper = gitHubHelper;
    this.whitelist = whitelist;

    eventBus.register(this);
  }

  @Subscribe
  public void handleCreateEvent(CreateEvent createEvent) throws IOException {
    if (!createEvent.hasRepository()) {
      return;
    }

    if ("branch".equalsIgnoreCase(createEvent.getRefType())) {
      GitInfo gitInfo = gitInfo(createEvent);
      if (isOptedIn(gitInfo)) {
        branchService.upsert(gitInfo);
      }
    }
  }

  @Subscribe
  public void handleDeleteEvent(DeleteEvent deleteEvent) {
    if (!deleteEvent.hasRepository()) {
      return;
    }

    if ("branch".equalsIgnoreCase(deleteEvent.getRefType())) {
      branchService.delete(gitInfo(deleteEvent));
    }
  }

  @Subscribe
  public void handlePushEvent(PushEvent pushEvent) throws IOException {
    if (!pushEvent.hasRepository()) {
      return;
    }

    if (!pushEvent.getRef().startsWith("refs/tags/") && !pushEvent.getDeleted()) {
      GitInfo gitInfo = gitInfo(pushEvent);
      if (isOptedIn(gitInfo)) {
        gitInfo = branchService.upsert(gitInfo(pushEvent));
        repositoryBuildService.enqueue(gitInfo);
      }
    }
  }

  private boolean isOptedIn(GitInfo gitInfo) throws IOException {
    return whitelist.contains(gitInfo.getRepository()) || branchExists(gitInfo) || blazarConfigExists(gitInfo);
  }

  private boolean branchExists(GitInfo gitInfo) {
    return branchService.lookup(gitInfo).isPresent();
  }

  private boolean blazarConfigExists(GitInfo gitInfo) throws IOException  {
    try {
      GHRepository repository = gitHubHelper.repositoryFor(gitInfo);
      String config = gitHubHelper.contentsFor(".blazar.yaml", repository, gitInfo);
      return config.contains("enabled: true");
    } catch (FileNotFoundException e) {
      return false;
    }
  }

  private GitInfo gitInfo(CreateEvent createEvent) {
    return gitInfo(createEvent.getRepository(), createEvent.getRef(), true);
  }

  private GitInfo gitInfo(DeleteEvent deleteEvent) {
    return gitInfo(deleteEvent.getRepository(), deleteEvent.getRef(), false);
  }

  private GitInfo gitInfo(PushEvent pushEvent) {
    return gitInfo(pushEvent.getRepository(), pushEvent.getRef(), true);
  }

  private GitInfo gitInfo(Repository repository, String ref, boolean active) {
    String host = URI.create(repository.getUrl()).getHost();
    if ("api.github.com".equals(host)) {
      host = "github.com";
    }
    String fullName = repository.getFullName();
    String organization = fullName.substring(0, fullName.indexOf('/'));
    String repositoryName = fullName.substring(fullName.indexOf('/') + 1);
    long repositoryId = repository.getId();
    String branch = ref.startsWith("refs/heads/") ? ref.substring("refs/heads/".length()) : ref;

    return new GitInfo(Optional.<Integer>absent(), host, organization, repositoryName, repositoryId, branch, active, System.currentTimeMillis(), System.currentTimeMillis());
  }
}
