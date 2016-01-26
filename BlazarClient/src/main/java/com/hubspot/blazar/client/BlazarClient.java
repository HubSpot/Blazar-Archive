package com.hubspot.blazar.client;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Optional;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.Module;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.horizon.HttpClient;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpRequest.Method;
import com.hubspot.horizon.HttpResponse;

public class BlazarClient {
  private static final String GET_GIT_INFO_PATH = "/branches/%s";

  private static final String GET_REPOSITORY_BUILD_PATH = "/branches/builds/%s";

  private static final String MODULE_BUILD_PATH = "/modules/builds";
  private static final String BRANCHES_MODULES_PATH = "/branches/%s/modules";
  private static final String GET_MODULE_BUILD_PATH = MODULE_BUILD_PATH + "/%s";
  private static final String START_MODULE_BUILD_PATH = MODULE_BUILD_PATH + "/%s/start";
  private static final String COMPLETE_MODULE_BUILD_SUCCESS_PATH = MODULE_BUILD_PATH + "/%s/success";
  private static final String COMPLETE_MODULE_BUILD_FAILURE_PATH = MODULE_BUILD_PATH + "/%s/failure";

  private final HttpClient httpClient;
  private final String baseUrl;

  // Create via BlazarClientConfig
  BlazarClient(HttpClient httpClient, String baseUrl) {
    this.httpClient = httpClient;
    this.baseUrl = baseUrl;
  }

  public Optional<GitInfo> getGitInfo(long branchId) {
    String url = String.format(baseUrl + GET_GIT_INFO_PATH, branchId);
    HttpRequest request = HttpRequest.newBuilder().setMethod(Method.GET).setUrl(url).build();

    HttpResponse response = httpClient.execute(request);
    if (response.getStatusCode() == 404) {
      return Optional.absent();
    } else if (response.getStatusCode() == 200) {
      return Optional.of(response.getAs(GitInfo.class));
    } else {
      throw toException(response);
    }
  }

  public List<Module> getModules(long branchId) {
    String url = String.format(baseUrl + BRANCHES_MODULES_PATH, branchId);
    HttpRequest request = HttpRequest.newBuilder().setMethod(Method.GET).setUrl(url).build();
    HttpResponse response = httpClient.execute(request);
    if (response.getStatusCode() == 404) {
      return Collections.emptyList();
    } else if (response.getStatusCode() == 200) {
      return response.getAs(new TypeReference<List<Module>>() {
      });
    } else {
      throw toException(response);
    }
  }

  public Optional<Module> getModule(long branchId, int moduleId) {
    List<Module> modules = getModules(branchId);
    for (Module each : modules) {
      if (each.getId().get() == moduleId) {
        return Optional.of(each);
      }
    }
    throw new RuntimeException(String.format("Module id not found %d", moduleId));  // todo better exception type
  }

  public Optional<RepositoryBuild> getRepositoryBuild(long repositoryBuildId) {
    String url = String.format(baseUrl + GET_REPOSITORY_BUILD_PATH, repositoryBuildId);
    HttpRequest request = HttpRequest.newBuilder().setMethod(Method.GET).setUrl(url).build();

    HttpResponse response = httpClient.execute(request);
    if (response.getStatusCode() == 404) {
      return Optional.absent();
    } else if (response.getStatusCode() == 200) {
      return Optional.of(response.getAs(RepositoryBuild.class));
    } else {
      throw toException(response);
    }
  }

  public Optional<ModuleBuild> getModuleBuild(long moduleBuildId) {
    String url = String.format(baseUrl + GET_MODULE_BUILD_PATH, moduleBuildId);
    HttpRequest request = HttpRequest.newBuilder().setMethod(Method.GET).setUrl(url).build();

    HttpResponse response = httpClient.execute(request);
    if (response.getStatusCode() == 404) {
      return Optional.absent();
    } else if (response.getStatusCode() == 200) {
      return Optional.of(response.getAs(ModuleBuild.class));
    } else {
      throw toException(response);
    }
  }

  public void startModuleBuild(long moduleBuildId, String taskId) {
    String url = String.format(baseUrl + START_MODULE_BUILD_PATH, moduleBuildId);
    HttpRequest request = HttpRequest.newBuilder()
        .setMethod(Method.PUT)
        .setUrl(url)
        .setQueryParam("taskId").to(taskId)
        .build();

    HttpResponse response = httpClient.execute(request);
    if (response.getStatusCode() != 200) {
      throw toException(response);
    }
  }

  public void completeModuleBuildSuccess(long moduleBuildId) {
    String url = String.format(baseUrl + COMPLETE_MODULE_BUILD_SUCCESS_PATH, moduleBuildId);
    HttpRequest request = HttpRequest.newBuilder().setMethod(Method.PUT).setUrl(url).build();

    HttpResponse response = httpClient.execute(request);
    if (response.getStatusCode() != 200) {
      throw toException(response);
    }
  }

  public void completeModuleBuildFailure(long moduleBuildId) {
    String url = String.format(baseUrl + COMPLETE_MODULE_BUILD_FAILURE_PATH, moduleBuildId);
    HttpRequest request = HttpRequest.newBuilder().setMethod(Method.PUT).setUrl(url).build();

    HttpResponse response = httpClient.execute(request);
    if (response.getStatusCode() != 200) {
      throw toException(response);
    }
  }

  private BlazarClientException toException(HttpResponse response) {
    String messageFormat = "Error hitting URL %s, status code: %s, response: %s";
    String message = String.format(messageFormat, response.getRequest().getUrl(), response.getStatusCode(), response.getAsString());
    throw new BlazarClientException(message);
  }
}
