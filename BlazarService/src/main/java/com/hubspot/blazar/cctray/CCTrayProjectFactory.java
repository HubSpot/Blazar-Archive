package com.hubspot.blazar.cctray;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.RepositoryBuild.State;
import com.hubspot.blazar.base.RepositoryState;
import com.hubspot.blazar.cctray.CCTrayProject.CCTrayActivity;
import com.hubspot.blazar.cctray.CCTrayProject.CCTrayStatus;
import com.hubspot.blazar.util.BlazarUrlHelper;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;

@Singleton
public class CCTrayProjectFactory implements Function<RepositoryState, Optional<CCTrayProject>> {
  private final BlazarUrlHelper urlHelper;

  @Inject
  public CCTrayProjectFactory(BlazarUrlHelper urlHelper) {
    this.urlHelper = urlHelper;
  }


  @Override
  public Optional<CCTrayProject> apply(@Nullable RepositoryState repositoryState) {
    if (repositoryState == null || !repositoryState.getLastBuild().isPresent()) {
      return Optional.absent();
    }

    RepositoryBuild lastBuild = repositoryState.getLastBuild().get();

    String name = computeCCTrayName(repositoryState.getGitInfo());
    CCTrayActivity activity = computeCCTrayActivity(repositoryState.getInProgressBuild());
    CCTrayStatus lastBuildState = computeLastBuildState(lastBuild);
    String lastBuildLabel = computeLastBuildLabel(lastBuild);
    Date lastBuildTime = computeLastBuildTime(lastBuild);
    String webUrl = computeWebUrl(repositoryState.getGitInfo());


    return Optional.of(new CCTrayProject(name, activity, lastBuildState, lastBuildLabel, lastBuildTime, webUrl));
  }

  private String computeCCTrayName(GitInfo gitInfo) {
    return gitInfo.getRepository() + '/' + gitInfo.getBranch();
  }

  private CCTrayActivity computeCCTrayActivity(Optional<RepositoryBuild> inProgressBuild) {
    if (inProgressBuild.isPresent()) {
      if (inProgressBuild.get().getState() == State.LAUNCHING) {
        return CCTrayActivity.CHECKING_MODIFICATIONS;
      } else {
        return CCTrayActivity.BUILDING;
      }
    } else {
      return CCTrayActivity.SLEEPING;
    }
  }

  private CCTrayStatus computeLastBuildState(RepositoryBuild lastBuild) {
    switch (lastBuild.getState()) {
      case SUCCEEDED:
        return CCTrayStatus.SUCCESS;
      case CANCELLED:
        return CCTrayStatus.UNKNOWN;
      case FAILED:
      case UNSTABLE:
        return CCTrayStatus.FAILURE;
      default:
        return CCTrayStatus.UNKNOWN;
    }
  }

  private String computeLastBuildLabel(RepositoryBuild lastBuild) {
    return String.valueOf(lastBuild.getBuildNumber());
  }

  private Date computeLastBuildTime(RepositoryBuild lastBuild) {
    if (lastBuild.getStartTimestamp().isPresent()) {
      return new Date(lastBuild.getStartTimestamp().get());
    } else {
      return new Date(0);
    }
  }

  private String computeWebUrl(GitInfo gitInfo) {
    return urlHelper.getBlazarUiLink(gitInfo).toString();
  }
}
