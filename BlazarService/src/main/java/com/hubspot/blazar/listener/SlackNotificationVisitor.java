package com.hubspot.blazar.listener;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.slack.SlackConfiguration;
import com.hubspot.blazar.base.visitor.RepositoryBuildVisitor;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.BlazarSlackConfiguration;
import com.hubspot.blazar.data.service.SlackConfigurationService;

public class SlackNotificationVisitor implements RepositoryBuildVisitor {

  private static final Logger LOG = LoggerFactory.getLogger(SlackNotificationVisitor.class);
  private final Optional<BlazarSlackConfiguration> blazarSlackConfig;

  private SlackConfigurationService slackConfigurationService;

  @Inject
  public SlackNotificationVisitor (SlackConfigurationService slackConfigurationService,
                                   BlazarConfiguration blazarConfiguration) {
    this.slackConfigurationService = slackConfigurationService;
    this.blazarSlackConfig = blazarConfiguration.getSlackConfiguration();
  }


  @Override
  public void visit(RepositoryBuild build) throws Exception {
    if (!build.getId().isPresent()) {
      return;
    }

    Set<SlackConfiguration> configurationSet = slackConfigurationService.getAllWithRepositoryId(build.getId().get());

    for (SlackConfiguration slackConfiguration : configurationSet ) {

    }

  }

  private void SendSlackMessage(SlackConfiguration slackConfiguration) {

  }


}
