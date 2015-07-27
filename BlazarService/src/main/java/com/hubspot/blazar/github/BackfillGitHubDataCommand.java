package com.hubspot.blazar.github;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
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
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.Map.Entry;

public class BackfillGitHubDataCommand extends ConfiguredCommand<BlazarConfiguration> {
  private final HttpClient httpClient;

  public BackfillGitHubDataCommand() {
    super("backfill", "Reprocess all repo and branch data");

    ObjectMapper mapper = Jackson.newObjectMapper().setSerializationInclusion(Include.NON_NULL);
    this.httpClient = new NingHttpClient(HttpConfig.newBuilder().setObjectMapper(mapper).build());
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
    try {
      for (Entry<String, GitHubConfiguration> entry : configuration.getGitHubConfiguration().entrySet()) {
        String host = entry.getKey();
        GitHubConfiguration gitHubConfig = entry.getValue();
        GitHub gitHub = BlazarServiceModule.toGitHub(host, gitHubConfig);

        for (String organization : gitHubConfig.getOrganizations()) {
          processOrganization(namespace.getString("url"), host, gitHub.getOrganization(organization));
        }
      }
    } finally {
      httpClient.close();
    }
  }

  private void processOrganization(String url, String host, GHOrganization organization) throws IOException {
    for (GHRepository repository : organization.listRepositories()) {
      GitInfo gitInfo = new GitInfo(Optional.<Long>absent(),
          host,
          repository.getOwnerName(),
          repository.getName(),
          repository.getId(),
          "master",
          true);

      try {
        httpClient.execute(HttpRequest.newBuilder().setMethod(Method.POST).setUrl(url).setBody(gitInfo).build());
      } catch (Throwable t) {
        System.out.println(Throwables.getStackTraceAsString(t));
      }
    }
  }
}
