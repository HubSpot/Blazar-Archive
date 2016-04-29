package com.hubspot.blazar.util;

import java.util.Set;

import com.google.common.base.Optional;
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
      // If a build fails, SUCCEEDED & Skipped & CANCELLED are common, and unnecessary to include
      if (!b.getState().equals(ModuleBuild.State.SUCCEEDED) && !b.getState().equals(ModuleBuild.State.CANCELLED) && !b.getState().equals(ModuleBuild.State.SKIPPED)) {
        Module m = moduleService.get(b.getModuleId()).get();
        attachment.addField(m.getName(), b.getState().name().toLowerCase(), true);
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
}
