package com.hubspot.blazar.resources;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.hubspot.blazar.base.BuildState;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.BuildStateService;
import com.hubspot.blazar.data.service.EventService;
import com.hubspot.blazar.data.service.ModuleService;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/personalization")
@Produces(MediaType.APPLICATION_JSON)
public class PersonalizationResource {

  private EventService eventService;
  private Map<String, GitHub> gitHubByHost;
  private BranchService branchService;
  private ModuleService moduleService;
  private BuildStateService buildStateService;
  private static final Logger LOG = LoggerFactory.getLogger(PersonalizationResource.class);

  @Inject
  public PersonalizationResource(EventService eventService,
                                 Map<String, GitHub> gitHubByHost,
                                 BranchService branchService,
                                 ModuleService moduleService,
                                 BuildStateService buildStateService) {
    this.eventService = eventService;
    this.gitHubByHost = gitHubByHost;
    this.branchService = branchService;
    this.moduleService = moduleService;
    this.buildStateService = buildStateService;
  }

  @GET
  @Path("/activity/{userName}")
  public List<BuildState> getByUserName(@PathParam("userName") String userName, @QueryParam("gitHubHost") @DefaultValue("git.hubteam.com") final String gitHubHost, @QueryParam("since") @DefaultValue("0") long since) throws IOException {
    return eventService.fetch(userName, gitHubHost, since);
  }

  @GET
  @Path("/subscriptions/{userName}")
  public Set<BuildState> getUserSubscriptions(@PathParam("userName") String userName, @QueryParam("gitHubHost") @DefaultValue("git.hubteam.com") String gitHubHost) throws IOException {
    final Set<BuildState> buildStates = buildStateService.getAllActive(1444692939000L);
    GitHub gitHub = Preconditions.checkNotNull(gitHubByHost.get(gitHubHost));
    List<GHRepository> subs = gitHub.getUser(userName).listSubscriptions().asList();
    Set<BuildState> states = new HashSet<>();
    for (GHRepository repo : subs) {
      Set<BuildState> repoStates = getBuildStateFromGHRepository(gitHubHost, repo, buildStates);
      states.addAll(repoStates);
    }
    return states;
  }

  private Set<BuildState> getBuildStateFromGHRepository(final String gitHubHost, final GHRepository repo, Set<BuildState> buildStates) {
    final String ownerName = repo.getOwnerName();
    final String repoName = repo.getName();

    Predicate<BuildState> a = new Predicate<BuildState>() {
      @Override
      public boolean apply(BuildState input) {
        GitInfo inputGitInfo = input.getGitInfo();
        String inputGitHubHost =  inputGitInfo.getHost();
        String inputOwnerName = inputGitInfo.getOrganization();
        String inputRepoName = inputGitInfo.getRepository();
        String inputBranch = inputGitInfo.getBranch();
        return inputGitHubHost.equalsIgnoreCase(gitHubHost) && inputOwnerName.equalsIgnoreCase(ownerName) && inputRepoName.equalsIgnoreCase(repoName) && inputBranch.equalsIgnoreCase("master");
      }
    };
    return Sets.filter(buildStates, a);
  }
}
