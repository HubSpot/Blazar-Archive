package com.hubspot.blazar.util;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.ullink.slack.simpleslackapi.SlackAttachment;

@Singleton
public class SlackMessageBuildingUtils {

  private BranchService branchService;
  private ModuleService moduleService;
  private ModuleBuildService moduleBuildService;
  private BlazarUrlHelper blazarUrlHelper;

  @Inject
  public SlackMessageBuildingUtils(BranchService branchService,
                                   ModuleService moduleService,
                                   ModuleBuildService moduleBuildService,
                                   BlazarUrlHelper blazarUrlHelper) {

    this.branchService = branchService;
    this.moduleService = moduleService;
    this.moduleBuildService = moduleBuildService;
    this.blazarUrlHelper = blazarUrlHelper;
  }


  /**
   * This constructs a slack attachment to send along with a message so that users
   * can get a visual for the state of the build in addition to text.
   *
   * The information contained in the attachment is:
   * - Color: Red for failure, Green success, and Yellow for states with less clear Failure / Success connotations
   * - A link to the build history page for the build
   * - A list of the modules that failed or were "unstable".
   *
   */
  public SlackAttachment buildSlackAttachment(RepositoryBuild build) {
    String title = buildTitle(build);
    SlackAttachment attachment = makeAttachmentJustWithTitle(title);
    attachment.setTitleLink(blazarUrlHelper.getBlazarUiLink(build));
    attachment.setColor(getColor(build.getState()));

    if (build.getState().equals(RepositoryBuild.State.SUCCEEDED)) {
      return attachment;
    }

    Set<ModuleBuild> builtModules = moduleBuildService.getByRepositoryBuild(build.getId().get());
    List<String> moduleNames = builtModules.stream()
        .filter(mb -> mb.getState().isFailed())
        .map(mb -> moduleService.get(mb.getModuleId()).get().getName())
        .collect(Collectors.toList());
    moduleNames.sort(Comparator.naturalOrder());

    String attachmentText = String.format("There were %d failing modules:\n", moduleNames.size());
    attachment.setText(attachmentText + Joiner.on("  |  ").join(moduleNames));
    return attachment;
  }

  private String buildTitle(RepositoryBuild build) {
    GitInfo gitInfo = branchService.get(build.getBranchId()).get();
    return String.format("Repository Build %s-%s#%d finished with state %s", gitInfo.getRepository(), gitInfo.getBranch(), build.getBuildNumber(), build.getState().toString().toLowerCase());
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

  private static SlackAttachment makeAttachmentJustWithTitle(String title) {
    return new SlackAttachment(title, title, "", "");
  }
}
