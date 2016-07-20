package com.hubspot.blazar.util;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.inject.Inject;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.data.service.RepositoryBuildService;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;

public class SlackUtils {

  private BranchService branchService;
  private ModuleService moduleService;
  private ModuleBuildService moduleBuildService;
  private RepositoryBuildService repositoryBuildService;
  private BlazarUrlHelper blazarUrlHelper;

  @Inject
  public SlackUtils(BranchService branchService,
                    ModuleService moduleService,
                    ModuleBuildService moduleBuildService,
                    RepositoryBuildService repositoryBuildService,
                    BlazarUrlHelper blazarUrlHelper) {

    this.branchService = branchService;
    this.moduleService = moduleService;
    this.moduleBuildService = moduleBuildService;
    this.repositoryBuildService = repositoryBuildService;
    this.blazarUrlHelper = blazarUrlHelper;
  }

  public static Optional<SlackChannel> getChannelByName(SlackSession session, String name) {
    return Optional.fromNullable(session.findChannelByName(name));
  }


  public SlackAttachment buildSlackAttachment(RepositoryBuild build) {
    String title = buildNormalStateMessage(build);
    SlackAttachment attachment = makeAttachment(title);
    attachment.setTitleLink(blazarUrlHelper.getBlazarUiLink(build));
    attachment.setColor(getColor(build.getState()));

    if (build.getState().equals(RepositoryBuild.State.SUCCEEDED)) {
      return attachment;
    }
    attachment.setText("The failing modules were:");

    Set<ModuleBuild> builtModules = moduleBuildService.getByRepositoryBuild(build.getId().get());
    for (ModuleBuild b : builtModules) {
      if (b.getState().equals(ModuleBuild.State.SUCCEEDED)) {
        continue;
      }
      // If a build fails, SUCCEEDED & Skipped & CANCELLED are common, and unnecessary to include
      Module m = moduleService.get(b.getModuleId()).get();
      boolean failedButNotUnstable = b.getState().equals(ModuleBuild.State.FAILED)
          || (build.getState().equals(RepositoryBuild.State.CANCELLED) && b.getState().equals(ModuleBuild.State.CANCELLED));
      boolean unstable = b.getState().equals(ModuleBuild.State.SKIPPED) && build.getState().equals(RepositoryBuild.State.UNSTABLE);

      if (failedButNotUnstable) {
        attachment.addField(m.getName(), b.getState().name().toLowerCase(), true);
      } else if (unstable) {
        Optional<ModuleBuild> maybePrevious = moduleBuildService.getPreviousBuild(b);
        boolean previousBuildCancelledOrFailed = maybePrevious.isPresent() && (maybePrevious.get().getState().equals(ModuleBuild.State.CANCELLED) || maybePrevious.get().getState().equals(ModuleBuild.State.FAILED));
        if (previousBuildCancelledOrFailed) {
          String unstableMessage = String.format("Unstable, last attempted build was in branch build #%d", maybePrevious.get().getBuildNumber());
          attachment.addField(m.getName(), unstableMessage, false);
        } else if (!maybePrevious.isPresent()) {
          attachment.addField(m.getName(), "This module has never built.", true);
        }
      }
    }
    return attachment;
  }

  public SlackAttachment buildSlackAttachment(ModuleBuild build) {
    String title = buildNormalStateMessage(build);
    SlackAttachment attachment = makeAttachment(title);
    attachment.setTitleLink(blazarUrlHelper.getBlazarUiLink(build));
    attachment.setColor(getColor(build.getState()));
    return attachment;
  }

  public String buildNormalStateMessage(ModuleBuild build) {
    Module module = moduleService.get(build.getModuleId()).get();
    GitInfo gitInfo = branchService.get(repositoryBuildService.get(build.getRepoBuildId()).get().getBranchId()).get();
    return String.format("Module Build %s-%s-%s#%d finished with state %s", gitInfo.getRepository(), gitInfo.getBranch(), module.getName(), build.getBuildNumber(), build.getState().toString().toLowerCase());
  }

  public String buildNormalStateMessage(RepositoryBuild build) {
    GitInfo gitInfo = branchService.get(build.getBranchId()).get();
    return String.format("Repository Build %s-%s#%d finished with state %s", gitInfo.getRepository(), gitInfo.getBranch(), build.getBuildNumber(), build.getState().toString().toLowerCase());
  }

  private String getColor(ModuleBuild.State state) {
    String color;
    switch (state) {
      case SUCCEEDED:
        color = "good";
        break;
      case CANCELLED:
        color = "warning";
        break;
      case SKIPPED:
        color = "grey";
        break;
      case FAILED:
        color = "danger";
        break;
      default:
        color = "blue";
        break;
    }
    return color;
  }

  private String getColor(RepositoryBuild.State state) {
    String color;
    switch (state) {
      case SUCCEEDED:
        color = "good";
        break;
      case CANCELLED:
        color = "warning";
        break;
      case UNSTABLE:
        color = "danger";
        break;
      case FAILED:
        color = "danger";
        break;
      default:
        color = "blue";
        break;
    }
    return color;
  }

  private static SlackAttachment makeAttachment(String title) {
    return new SlackAttachment(title, title, "", "");
  }

  public static Retryer<Boolean> makeSlackMessageSendingRetryer() {
    return RetryerBuilder.<Boolean>newBuilder()
        .retryIfResult(Predicates.equalTo(Boolean.FALSE))
        .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
        .withStopStrategy(StopStrategies.stopAfterAttempt(3))
        .build();
  }
}
