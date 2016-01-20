package com.hubspot.blazar.listener;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.MapBinder;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.base.RepositoryBuild.State;
import com.hubspot.blazar.base.listener.ModuleBuildListener;
import com.hubspot.blazar.base.listener.RepositoryBuildListener;

public class BuildListenerModule implements Module {

  @Override
  public void configure(Binder binder) {
    binder.bind(BuildEventDispatcher.class);

    MapBinder<State, RepositoryBuildListener> repositoryBuildListeners = MapBinder.newMapBinder(binder, RepositoryBuild.State.class, RepositoryBuildListener.class).permitDuplicates();

    // launch the queued build if nothing in progress
    repositoryBuildListeners.addBinding(RepositoryBuild.State.QUEUED).to(QueuedRepositoryBuildListener.class);

    // queue builds for the affected modules
    repositoryBuildListeners.addBinding(RepositoryBuild.State.LAUNCHING).to(LaunchingRepositoryBuildListener.class);

    // cancel module builds
    repositoryBuildListeners.addBinding(RepositoryBuild.State.CANCELLED).to(CancelledRepositoryBuildListener.class);

    // launch any queued repository build if present
    repositoryBuildListeners.addBinding(RepositoryBuild.State.SUCCEEDED).to(CompletedRepositoryBuildListener.class);
    repositoryBuildListeners.addBinding(RepositoryBuild.State.CANCELLED).to(CompletedRepositoryBuildListener.class);
    repositoryBuildListeners.addBinding(RepositoryBuild.State.FAILED).to(CompletedRepositoryBuildListener.class);
    repositoryBuildListeners.addBinding(RepositoryBuild.State.UNSTABLE).to(CompletedRepositoryBuildListener.class);

    // update GitHub status
    repositoryBuildListeners.addBinding(RepositoryBuild.State.LAUNCHING).to(GitHubStatusListener.class);
    repositoryBuildListeners.addBinding(RepositoryBuild.State.IN_PROGRESS).to(GitHubStatusListener.class);
    repositoryBuildListeners.addBinding(RepositoryBuild.State.SUCCEEDED).to(GitHubStatusListener.class);
    repositoryBuildListeners.addBinding(RepositoryBuild.State.CANCELLED).to(GitHubStatusListener.class);
    repositoryBuildListeners.addBinding(RepositoryBuild.State.FAILED).to(GitHubStatusListener.class);
    repositoryBuildListeners.addBinding(RepositoryBuild.State.UNSTABLE).to(GitHubStatusListener.class);

    MapBinder<ModuleBuild.State, ModuleBuildListener> moduleBuildListeners = MapBinder.newMapBinder(binder, ModuleBuild.State.class, ModuleBuildListener.class).permitDuplicates();

    // launch the queued build if nothing upstream
    moduleBuildListeners.addBinding(ModuleBuild.State.QUEUED).to(QueuedModuleBuildListener.class);

    // kick off the singularity task if the build is launching
    moduleBuildListeners.addBinding(ModuleBuild.State.LAUNCHING).to(LaunchingModuleBuildListener.class);

    // launch downstream builds if all upstreams succeeded
    moduleBuildListeners.addBinding(ModuleBuild.State.SUCCEEDED).to(DownstreamModuleBuildLauncher.class);

    // kill the singularity task
    moduleBuildListeners.addBinding(ModuleBuild.State.CANCELLED).to(SingularityTaskKiller.class);

    // cancel all downstream builds
    moduleBuildListeners.addBinding(ModuleBuild.State.CANCELLED).to(DownstreamModuleBuildCanceller.class);
    moduleBuildListeners.addBinding(ModuleBuild.State.FAILED).to(DownstreamModuleBuildCanceller.class);

    // complete the repository build once all of the module builds have finished
    moduleBuildListeners.addBinding(ModuleBuild.State.SUCCEEDED).to(RepositoryBuildCompleter.class);
    moduleBuildListeners.addBinding(ModuleBuild.State.CANCELLED).to(RepositoryBuildCompleter.class);
    moduleBuildListeners.addBinding(ModuleBuild.State.FAILED).to(RepositoryBuildCompleter.class);
  }
}
