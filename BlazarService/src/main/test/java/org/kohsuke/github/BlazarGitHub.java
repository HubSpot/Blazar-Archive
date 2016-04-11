package org.kohsuke.github;

import java.io.IOException;
import java.util.List;

public class BlazarGitHub extends GitHub {

  private List<BlazarGHRepository> repos;

  public static BlazarGitHub getTestBlazarGitHub(List<BlazarGHRepository> repos) throws IOException {
    BlazarGitHub b = new BlazarGitHub("localhost", "testyMcTest", null, "asdf", null, null);
    b.configure(repos);
    return b;
  }

  BlazarGitHub(String apiUrl, String login, String oauthAccessToken, String password, HttpConnector connector, RateLimitHandler rateLimitHandler) throws IOException {
    super(apiUrl, login, oauthAccessToken, password, connector, rateLimitHandler);
  }

  public void configure(List<BlazarGHRepository> repos) {
    this.repos = repos;
  }

  @Override
  public GHRepository getRepository(String name) throws IOException {
    for (BlazarGHRepository b : repos) {
      if (b.getFullName().equals(name)) {
        return b;
      }
    }
    throw new IllegalArgumentException(String.format("Could not find repo with name %s", name));
  }
}
