package com.hubspot.blazar.util;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.net.UrlEscapers;
import com.hubspot.blazar.base.BuildDefinition;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.BuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.github.GitHubProtos.Commit;
import com.hubspot.blazar.github.GitHubProtos.CreateEvent;
import com.hubspot.blazar.github.GitHubProtos.DeleteEvent;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;
import com.hubspot.blazar.github.GitHubProtos.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public class GitHubWebhookHandler {
  private static final Logger LOG = LoggerFactory.getLogger(GitHubWebhookHandler.class);

  private final BranchService branchService;
  private final ModuleService moduleService;
  private final ModuleDiscovery moduleDiscovery;
  private final BuildService buildService;

  @Inject
  public GitHubWebhookHandler(BranchService branchService,
                              ModuleService moduleService,
                              ModuleDiscovery moduleDiscovery,
                              BuildService buildService,
                              EventBus eventBus) {
    this.branchService = branchService;
    this.moduleService = moduleService;
    this.moduleDiscovery = moduleDiscovery;
    this.buildService = buildService;

    eventBus.register(this);
  }

  @Subscribe
  public void handleCreateEvent(CreateEvent createEvent) throws IOException {
    if ("branch".equalsIgnoreCase(createEvent.getRefType())) {
      processBranch(gitInfo(createEvent));
    }
  }

  @Subscribe
  public void handleDeleteEvent(DeleteEvent deleteEvent) {
    if ("branch".equalsIgnoreCase(deleteEvent.getRefType())) {
      branchService.delete(gitInfo(deleteEvent));
    }
  }

  @Subscribe
  public void handlePushEvent(PushEvent pushEvent) throws IOException {
    if (!pushEvent.getRef().startsWith("refs/tags/")) {
      GitInfo gitInfo = branchService.upsert(gitInfo(pushEvent));

      Set<Module> modules = updateModules(gitInfo, pushEvent);
      triggerBuilds(pushEvent, gitInfo, modules);
    }
  }

  public Set<Module> processBranch(GitInfo gitInfo) throws IOException {
    gitInfo = branchService.upsert(gitInfo);

    Set<Module> modules = moduleDiscovery.discover(gitInfo);
    return moduleService.setModules(gitInfo, modules);
  }

  private Set<Module> updateModules(GitInfo gitInfo, PushEvent pushEvent) throws IOException {
    if (moduleDiscovery.shouldRediscover(gitInfo, pushEvent)) {
      return moduleService.setModules(gitInfo, moduleDiscovery.discover(gitInfo));
    }

    return moduleService.getByBranch(gitInfo.getId().get());
  }

  private void triggerBuilds(PushEvent pushEvent, GitInfo gitInfo, Set<Module> modules) throws IOException {
    Set<Module> toBuild = new HashSet<>();
    for (String path : affectedPaths(pushEvent)) {
      for (Module module : modules) {
        if (module.contains(FileSystems.getDefault().getPath(path))) {
          toBuild.add(module);
        }
      }
    }

    for (Module module : toBuild) {
      LOG.info("Going to build module {}", module.getId().get());
      if ("true".equals(System.getenv("TRIGGER_BUILDS"))) {
        buildService.enqueue(new BuildDefinition(gitInfo, module));
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
    String branch = escapeBranchName(ref.startsWith("refs/heads/") ? ref.substring("refs/heads/".length()) : ref);

    return new GitInfo(Optional.<Integer>absent(), host, organization, repositoryName, repositoryId, branch, active);
  }

  private static String escapeBranchName(String branchName) {
    List<String> parts = Splitter.on('/').splitToList(branchName);
    List<String> escaped = new ArrayList<>();
    for (String part : parts) {
      escaped.add(UrlEscapers.urlPathSegmentEscaper().escape(part));
    }

    return Joiner.on('/').join(escaped);
  }

  private static Set<String> affectedPaths(PushEvent pushEvent) {
    Set<String> affectedPaths = new HashSet<>();
    for (Commit commit : pushEvent.getCommitsList()) {
      affectedPaths.addAll(commit.getAddedList());
      affectedPaths.addAll(commit.getModifiedList());
      affectedPaths.addAll(commit.getRemovedList());
    }

    return affectedPaths;
  }

  public static void main(String... args) throws Exception {
    System.out.println(escapeBranchName("jgetto/contact-details-slowlane"));
  }
}
