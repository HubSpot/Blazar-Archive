package com.hubspot.blazar.integration.slack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.BlazarSlackConfiguration;
import com.hubspot.blazar.externalservice.slack.SlackApiResponse;
import com.hubspot.blazar.externalservice.slack.SlackChannel;
import com.hubspot.blazar.externalservice.slack.SlackMessage;
import com.hubspot.blazar.externalservice.slack.SlackUser;
import com.hubspot.horizon.AsyncHttpClient;
import com.hubspot.horizon.AsyncHttpClient.Callback;
import com.hubspot.horizon.HttpClient;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpRequest.Method;
import com.hubspot.horizon.HttpResponse;

@Singleton
public class SlackClient {
  private static final Logger LOG = LoggerFactory.getLogger(SlackClient.class);

  private final AsyncHttpClient asyncHttpClient;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;
  private final BlazarSlackConfiguration blazarSlackConfiguration;
  private List<SlackUser> slackUsers;

  @Inject
  public SlackClient(AsyncHttpClient asyncHttpClient,
                     BlazarConfiguration blazarConfiguration,
                     ObjectMapper objectMapper,
                     HttpClient httpClient) {
    this.asyncHttpClient = asyncHttpClient;
    this.objectMapper = objectMapper;
    this.httpClient = httpClient;
    this.blazarSlackConfiguration = blazarConfiguration.getSlackConfiguration().get();
    this.slackUsers = new ArrayList<>();
  }

  public BlazarSlackConfiguration getBlazarSlackConfiguration() {
    return blazarSlackConfiguration;
  }

  public void sendMessage(final SlackMessage message) {
    LOG.info("Sending Slack message {}", message);

    final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
        .setUrl(blazarSlackConfiguration.getSlackApiBaseUrl() + "/chat.postMessage")
        .setMethod(Method.POST)
        .setContentType(HttpRequest.ContentType.FORM);

    requestBuilder.setFormParam("token").to(message.getToken().or(blazarSlackConfiguration.getSlackApiToken()));
    requestBuilder.setFormParam("channel").to("#" + message.getChannel());
    requestBuilder.setFormParam("text").to(message.getText());
    requestBuilder.setFormParam("username").to(message.getUsername().or(blazarSlackConfiguration.getUsername()));

    if (message.getAttachments() != null) {
      try {
        requestBuilder.setFormParam("attachments").to(objectMapper.writeValueAsString(message.getAttachments()));
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }
    if (message.getIcon_emoji().isPresent()) {
      requestBuilder.setFormParam("icon_emoji").to(message.getIcon_emoji().get());
    }
    asyncHttpClient.execute(requestBuilder.build(), new Callback() {

      @Override
      public void failed(Exception e) {
        LOG.error(String.format("Failed to send notification to Slack channel %s", message.getChannel()), e);
      }

      @Override
      public void completed(HttpResponse httpResponse) {
        if (httpResponse.isSuccess()) {
          SlackApiResponse apiResponse = httpResponse.getAs(SlackApiResponse.class);
          if (apiResponse.getOk()) {
            LOG.debug("Successfully sent Slack notification to channel {}.  Response: {}", message.getChannel(), apiResponse);
          } else {
            LOG.error("Slack didn't returned an ok for notification to channel {}. Slack response is: {}", message.getChannel(), apiResponse);
          }
        } else {
          LOG.error("Failed to send Slack notification to channel {}. Status code: {}, Response: {}",
              message.getChannel(), httpResponse.getStatusCode(), httpResponse.getAsString());
        }
      }
    });
  }

  public Optional<SlackUser> getUserByEmail(String email) throws IOException {
    Optional<SlackUser> foundUser = findSlackUserInListByEmail(slackUsers, email);
    if (foundUser.isPresent()) {
      return foundUser;
    }
    return findSlackUserInListByEmail(getSlackUsers(), email);
  }

  public List<SlackUser> getSlackUsers() throws IOException {
    HttpResponse httpResponse = httpClient.execute(makeWithApiUrl("/users.list"));
    if (!httpResponse.isSuccess()) {
      throw makeException(httpResponse);
    }
    SlackApiResponse apiResponse = httpResponse.getAs(SlackApiResponse.class);
    if (httpResponse.isSuccess() && apiResponse.getOk()) {
      slackUsers = apiResponse.getUsers().get();
      return slackUsers;
    } else if (apiResponse.getOk()) {
      return Collections.emptyList();
    } else {
      throw makeException(httpResponse);
    }
  }

  public List<SlackChannel> getSlackChannels() throws IOException {
    HttpResponse httpResponse = httpClient.execute(makeWithApiUrl("/channels.list"));
    if (!httpResponse.isSuccess()) {
      throw makeException(httpResponse);
    }
    SlackApiResponse slackApiResponse = httpResponse.getAs(SlackApiResponse.class);
    if (slackApiResponse.getOk() && slackApiResponse.getChannels().isPresent()) {
      return slackApiResponse.getChannels().get();
    } if (slackApiResponse.getOk()) {
      return Collections.emptyList();
    } else {
      throw makeException(httpResponse);
    }
  }

  private Optional<SlackUser> findSlackUserInListByEmail(List<SlackUser> users, String email) {
    for (SlackUser user : slackUsers) {
      if (user.getProfile().getEmail().equals(email)) {
         return Optional.of(user);
      }
    }
    return Optional.absent();
  }

  private HttpRequest makeWithApiUrl(String apiEndpoint) {
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
        .setUrl(blazarSlackConfiguration.getSlackApiBaseUrl() + apiEndpoint)
        .setMethod(Method.POST)
        .setContentType(HttpRequest.ContentType.FORM);
    requestBuilder.setFormParam("token").to(blazarSlackConfiguration.getSlackApiToken());
    return requestBuilder.build();
  }

  private static RuntimeException makeException(HttpResponse httpResponse) {
    return new RuntimeException(String.format("Non OK response from Slack API code: %d response: %s", httpResponse.getStatusCode(), httpResponse.getAsString()));
  }
}
