package com.hubspot.blazar.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHRepository;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hubspot.blazar.base.BranchSetting;
import com.hubspot.blazar.base.BuildOptions;
import com.hubspot.blazar.base.BuildTrigger;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.InterProjectBuildMapping;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.BranchSettingsService;
import com.hubspot.blazar.data.service.InterProjectBuildMappingService;
import com.hubspot.blazar.data.service.InterProjectBuildService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.hubspot.blazar.github.GitHubProtos.CreateEvent;
import com.hubspot.blazar.github.GitHubProtos.DeleteEvent;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;
import com.hubspot.blazar.github.GitHubProtos.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class GitHubWebhookHandler {
  private static final Logger LOG = LoggerFactory.getLogger(GitHubWebhookHandler.class);

  private final BranchService branchService;
  private final RepositoryBuildService repositoryBuildService;
  private final InterProjectBuildService interProjectBuildService;
  private final InterProjectBuildMappingService interProjectBuildMappingService;
  private final BranchSettingsService branchSettingsService;
  private final GitHubHelper gitHubHelper;
  private final Set<String> whitelist;

  @Inject
  public GitHubWebhookHandler(BranchService branchService,
                              RepositoryBuildService repositoryBuildService,
                              InterProjectBuildService interProjectBuildService,
                              InterProjectBuildMappingService interProjectBuildMappingService,
                              BranchSettingsService branchSettingsService,
                              GitHubHelper gitHubHelper,
                              @Named("whitelist") Set<String> whitelist,
                              EventBus eventBus) {
    this.branchService = branchService;
    this.repositoryBuildService = repositoryBuildService;
    this.interProjectBuildService = interProjectBuildService;
    this.interProjectBuildMappingService = interProjectBuildMappingService;
    this.branchSettingsService = branchSettingsService;
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
      if (!gitInfo.isActive()) {
        String message = "Ignoring push event for inactive branch {}-{}@{}";
        LOG.warn(message, gitInfo.getFullRepositoryName(), gitInfo.getBranch(), pushEvent.getAfter());
        return;
      }
      if (!isOptedIn(gitInfo)) {
        LOG.debug("Not {}#{} is not opted in to Blazar", gitInfo.getFullRepositoryName(), gitInfo.getBranch());
        return;
      }
      gitInfo = branchService.upsert(gitInfo);
      Optional<BranchSetting> branchSetting = branchSettingsService.getByBranchId(gitInfo.getId().get());
      if (branchSetting.isPresent() && branchSetting.get().isTriggerInterProjectBuilds()) {
        InterProjectBuild ipb = InterProjectBuild.getQueuedBuild(ImmutableSet.<Integer>of(), new BuildTrigger(BuildTrigger.Type.PUSH, String.format("%d_%s", gitInfo.getId().get(), pushEvent.getBefore())));
        interProjectBuildService.enqueue(ipb);
      } else {
        repositoryBuildService.enqueue(gitInfo, BuildTrigger.forCommit(pushEvent.getAfter()), BuildOptions.defaultOptions());
      }
    }
  }

  private boolean isOptedIn(GitInfo gitInfo) throws IOException {
    return whitelist.contains(gitInfo.getRepository()) || blazarConfigExists(gitInfo);
  }

  private boolean blazarConfigExists(GitInfo gitInfo) throws IOException {
    GHRepository repository = gitHubHelper.repositoryFor(gitInfo);
    try {
      gitHubHelper.contentsFor(".blazar-enabled", repository, gitInfo);
      return true;
    } catch (FileNotFoundException e) {
      try {
        String config = gitHubHelper.contentsFor(".blazar.yaml", repository, gitInfo);
        return config.contains("enabled: true");
      } catch (FileNotFoundException e1) {
        return false;
      }
    }
  }

  private GitInfo gitInfo(CreateEvent createEvent) {
    return gitInfo(createEvent.getRepository(), createEvent.getRef(), true);
  }

  private GitInfo gitInfo(DeleteEvent deleteEvent) {
    return gitInfo(deleteEvent.getRepository(), deleteEvent.getRef(), false);
  }

  private GitInfo gitInfo(PushEvent pushEvent) {
    int repositoryId = pushEvent.getRepository().getId();
    String branch = branchFromRef(pushEvent.getRef());
    Optional<GitInfo> gitInfo = branchService.getByRepositoryAndBranch(repositoryId, branch);
    boolean active = !gitInfo.isPresent() || gitInfo.get().isActive();
    return gitInfo(pushEvent.getRepository(), pushEvent.getRef(), active);
  }

  private GitInfo gitInfo(Repository repository, String ref, boolean active) {
    String host = URI.create(repository.getUrl()).getHost();
    if ("api.github.com".equals(host)) {
      host = "github.com";
    }
    String fullName = repository.getFullName();
    String organization = fullName.substring(0, fullName.indexOf('/'));
    String repositoryName = fullName.substring(fullName.indexOf('/') + 1);
    int repositoryId = repository.getId();
    String branch = branchFromRef(ref);

    return new GitInfo(Optional.<Integer>absent(), host, organization, repositoryName, repositoryId, branch, active, System.currentTimeMillis(), System.currentTimeMillis());
  }

  private static String branchFromRef(String ref) {
    return ref.startsWith("refs/heads/") ? ref.substring("refs/heads/".length()) : ref;
  }
}
