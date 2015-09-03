package com.hubspot.blazar.github;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hubspot.blazar.base.Build;
import com.hubspot.blazar.base.Build.State;
import com.hubspot.blazar.base.BuildState;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.GitHubConfiguration;
import com.hubspot.blazar.guice.BlazarServiceModule;
import com.hubspot.horizon.HttpClient;
import com.hubspot.horizon.HttpConfig;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpRequest.Method;
import com.hubspot.horizon.ning.NingHttpClient;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class BackfillGitHubDataCommand extends ConfiguredCommand<BlazarConfiguration> {
  public BackfillGitHubDataCommand() {
    super("backfill", "Reprocess all repo and branch data");
  }

  @Override
  public void configure(Subparser subparser) {
    super.configure(subparser);
    subparser.addArgument("url").nargs("?").help("url to process each repo");
  }

  @Override
  protected void run(Bootstrap<BlazarConfiguration> bootstrap,
                     Namespace namespace,
                     BlazarConfiguration configuration) throws Exception {
    final ObjectMapper mapper = Jackson.newObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    try (HttpClient httpClient = new NingHttpClient(HttpConfig.newBuilder().setObjectMapper(mapper).build())) {
      for (Entry<String, GitHubConfiguration> entry : configuration.getGitHubConfiguration().entrySet()) {
        String host = entry.getKey();
        GitHubConfiguration gitHubConfig = entry.getValue();
        GitHub gitHub = BlazarServiceModule.toGitHub(host, gitHubConfig);

        for (String organization : gitHubConfig.getOrganizations()) {
          System.out.println("Processing " + host + "/" + organization);
          processOrganization(httpClient, namespace.getString("url"), host, gitHub.getOrganization(organization));
          System.out.println("Processed " + host + "/" + organization);
        }
      }
    }
  }

  private void processOrganization(HttpClient httpClient, String url, String host, GHOrganization organization) throws IOException {
    for (GHRepository repository : organization.listRepositories()) {
      GitInfo gitInfo = new GitInfo(
          Optional.<Integer>absent(),
          host,
          repository.getOwnerName(),
          repository.getName(),
          repository.getId(),
          "master",
          true);

      try {
        System.out.println("Processing " + host + "/" + repository.getOwnerName() + "/" + repository.getName());
        httpClient.execute(HttpRequest.newBuilder().setMethod(Method.PUT).setUrl(url).setBody(gitInfo).build());
        System.out.println("Processed " + host + "/" + repository.getOwnerName() + "/" + repository.getName());
      } catch (Throwable t) {
        System.out.println(Throwables.getStackTraceAsString(t));
      }
    }
  }

  private static final String BASE_URL = "https://bootstrap.hubteam.com/blazar/v1/build";

  public static void main(String... args) throws Exception {
    final ObjectMapper mapper = Jackson.newObjectMapper().registerModule(new ProtobufModule()).setSerializationInclusion(JsonInclude.Include.NON_NULL);
    try (HttpClient httpClient = new NingHttpClient(HttpConfig.newBuilder().setConnectTimeoutSeconds(10).setObjectMapper(mapper).build())) {
      HttpRequest request = HttpRequest.newBuilder().setUrl(BASE_URL + "/states").build();
      Set<BuildState> buildStates = httpClient.execute(request).getAs(new TypeReference<Set<BuildState>>() {});

      long now = System.currentTimeMillis();
      for (BuildState buildState : buildStates) {
        if (buildState.getInProgressBuild().isPresent()) {
          Build inProgress = buildState.getInProgressBuild().get();
          if (!inProgress.getStartTimestamp().isPresent()) {
            System.out.println("No start timestamp for build: " + inProgress.getId().get());
          }

          if ((now - inProgress.getStartTimestamp().get()) > 60_000) {
            Build completed = inProgress.withState(State.CANCELLED).withEndTimestamp(now);
            ModuleBuild moduleBuild = new ModuleBuild(buildState.getGitInfo(), buildState.getModule(), completed);

            HttpRequest finishBuild = HttpRequest.newBuilder().setUrl(BASE_URL).setMethod(Method.PUT).setBody(moduleBuild).build();
            HttpResponse response = httpClient.execute(finishBuild);
            if (response.getStatusCode() != 200) {
              throw new IllegalStateException("Unable to finished build: " + response.getStatusCode());
            }
          }
        }
      }
    }
  }
}
