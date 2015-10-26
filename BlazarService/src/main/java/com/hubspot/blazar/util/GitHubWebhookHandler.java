package com.hubspot.blazar.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hubspot.blazar.base.BuildDefinition;
import com.hubspot.blazar.base.DependencyGraph;
import com.hubspot.blazar.base.DiscoveredModule;
import com.hubspot.blazar.base.Event;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.BuildService;
import com.hubspot.blazar.data.service.DependenciesService;
import com.hubspot.blazar.data.service.EventService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.discovery.ModuleDiscovery;
import com.hubspot.blazar.github.GitHubProtos.Commit;
import com.hubspot.blazar.github.GitHubProtos.CreateEvent;
import com.hubspot.blazar.github.GitHubProtos.DeleteEvent;
import com.hubspot.blazar.github.GitHubProtos.PullRequestEvent;
import com.hubspot.blazar.github.GitHubProtos.PushEvent;
import com.hubspot.blazar.github.GitHubProtos.Repository;

@Singleton
public class GitHubWebhookHandler {
  private static final Logger LOG = LoggerFactory.getLogger(GitHubWebhookHandler.class);

  private final BranchService branchService;
  private final ModuleService moduleService;
  private final ModuleDiscovery moduleDiscovery;
  private final BuildService buildService;
  private final DependenciesService dependenciesService;
  private EventService eventService;

  @Inject
  public GitHubWebhookHandler(BranchService branchService,
                              ModuleService moduleService,
                              ModuleDiscovery moduleDiscovery,
                              BuildService buildService,
                              DependenciesService dependenciesService,
                              EventService eventService,
                              EventBus eventBus) {
    this.branchService = branchService;
    this.moduleService = moduleService;
    this.moduleDiscovery = moduleDiscovery;
    this.buildService = buildService;
    this.dependenciesService = dependenciesService;
    this.eventService = eventService;

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
      GitInfo eventGitInfo = gitInfo(pushEvent);
      Optional<GitInfo> gitInfo = branchService.lookup(eventGitInfo.getHost(), eventGitInfo.getOrganization(), eventGitInfo.getRepository(), eventGitInfo.getBranch());
      Set<Module> modules;

      if (!gitInfo.isPresent()) {
        LOG.info("Branch {} does not exist on repo {} creating in an inactive state", eventGitInfo.getBranch(), eventGitInfo.getFullRepositoryName());
      } else {
        modules = updateModules(gitInfo.get(), pushEvent);
        recordEvents(modules, pushEvent.getPusher().getName());

        if (gitInfo.get().isActive()) {
          LOG.info("There is an open PR for branch {} on repo {}, triggering build", gitInfo.get().getBranch(), gitInfo.get().getFullRepositoryName());
          triggerBuilds(pushEvent, gitInfo.get(), modules);
        } else { // not active => no open PullRequest
          LOG.info("Branch {} on repo {} is not active (No open pull request) not triggering builds", gitInfo.get().getBranch(), gitInfo.get().getFullRepositoryName());
        }
      }
    }
  }

  @Subscribe
  public void handlePullRequestEvent(PullRequestEvent pullRequestEvent) throws IOException {
    Set<PullRequestEvent.Action> openActions = EnumSet.of(PullRequestEvent.Action.opened, PullRequestEvent.Action.reopened);
    GitInfo gitInfo = branchService.upsert(gitInfo(pullRequestEvent, openActions.contains(pullRequestEvent.getAction())));
    if (pullRequestEvent.getAction().equals(PullRequestEvent.Action.opened) || pullRequestEvent.getAction().equals(PullRequestEvent.Action.reopened)) {
      Set<Module> modules = moduleService.getByBranch(gitInfo.getId().get());
      triggerBuilds(gitInfo, modules);
      recordEvents(modules, pullRequestEvent.getPullRequestOrBuilder().getUser().getUsername());
    } else if (pullRequestEvent.getAction().equals(PullRequestEvent.Action.closed)) {
      branchService.delete(gitInfo);
    } else {
      LOG.info("Pull request action {} has no impact on builds, not doing anything", pullRequestEvent.getAction());
    }
  }

  public Set<Module> processBranch(GitInfo gitInfo) throws IOException {
    gitInfo = branchService.upsert(gitInfo);

    try {
      Set<DiscoveredModule> discovered = moduleDiscovery.discover(gitInfo);
      Set<Module> modules = moduleService.setModules(gitInfo, discovered);
      return modules;
    } catch (FileNotFoundException e) {
      branchService.delete(gitInfo);
      return Collections.emptySet();
    }
  }

  private Set<Module> updateModules(GitInfo gitInfo, PushEvent pushEvent) throws IOException {
    try {
      if (pushEvent.getForced() || moduleDiscovery.shouldRediscover(gitInfo, pushEvent)) {
        return moduleService.setModules(gitInfo, moduleDiscovery.discover(gitInfo));
      }

      return moduleService.getByBranch(gitInfo.getId().get());
    } catch (FileNotFoundException e) {
      return Collections.emptySet();
    }
  }

  private void triggerBuilds(PushEvent pushEvent, GitInfo gitInfo, Set<Module> modules) throws IOException {
    Set<Module> toBuild = new HashSet<>();
    if (pushEvent.getForced()) {
      toBuild = modules;
    } else {
      for (String path : affectedPaths(pushEvent)) {
        for (Module module : modules) {
          if (module.contains(FileSystems.getDefault().getPath(path))) {
            toBuild.add(module);
          }
        }
      }
    }

    triggerBuilds(gitInfo, toBuild);
  }

  private void triggerBuilds(GitInfo gitInfo, Set<Module> modules) throws IOException {
    DependencyGraph graph = dependenciesService.buildDependencyGraph(gitInfo);

    Map<Integer, Module> moduleMap = new HashMap<>();
    for (Module module : modules) {
      moduleMap.put(module.getId().get(), module);
    }

    moduleMap.keySet().retainAll(graph.reduceVertices(moduleMap.keySet()));

    for (Module module : moduleMap.values()) {
      LOG.info("Going to build module {}", module.getId().get());
      if ("true".equals(System.getenv("TRIGGER_BUILDS"))) {
        buildService.enqueue(new BuildDefinition(gitInfo, module));
      }
    }
  }

  private GitInfo gitInfo(CreateEvent createEvent) {
    String ref = createEvent.getRef();
    String branch = ref.startsWith("refs/heads/") ? ref.substring("refs/heads/".length()): ref;
    String defaultBranch = createEvent.getRepository().getDefaultBranch();
    if (defaultBranch.equals(branch)) {
      return gitInfo(createEvent.getRepository(), createEvent.getRef(), true);
    } else {
      return gitInfo(createEvent.getRepository(), createEvent.getRef(), false);
    }
  }

  private GitInfo gitInfo(DeleteEvent deleteEvent) {
    return gitInfo(deleteEvent.getRepository(), deleteEvent.getRef(), false);
  }

  private GitInfo gitInfo(PushEvent pushEvent) {
    return gitInfo(pushEvent.getRepository(), pushEvent.getRef(), false);
  }

  private GitInfo gitInfo(PullRequestEvent pullRequestEvent, boolean active) {
    return gitInfo(pullRequestEvent.getRepository(), pullRequestEvent.getPullRequestOrBuilder().getHead().getRef(), active);
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

    return new GitInfo(Optional.<Integer>absent(), host, organization, repositoryName, repositoryId, branch, active);
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

  private void recordEvents(Set<Module> modules, String username){
    String lowercaseName = username.toLowerCase();
    long now = System.currentTimeMillis();
    Optional<Integer> absentInt = Optional.absent();
    for (Module module : modules) {
      eventService.add(new Event(absentInt, module.getId().get(), now, lowercaseName));
    }
  }
}
