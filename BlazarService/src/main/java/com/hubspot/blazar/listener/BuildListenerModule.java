package com.hubspot.blazar.listener;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.hubspot.blazar.base.visitor.ModuleBuildVisitor;
import com.hubspot.blazar.base.visitor.RepositoryBuildVisitor;

public class BuildListenerModule implements Module {

  @Override
  public void configure(Binder binder) {
    binder.bind(BuildEventDispatcher.class);

    Multibinder<RepositoryBuildVisitor> repositoryBuildVisitors = Multibinder.newSetBinder(binder, RepositoryBuildVisitor.class);

    // launch the queued build if nothing in progress
    repositoryBuildVisitors.addBinding().to(QueuedRepositoryBuildVisitor.class);
    // queue builds for the affected modules
    repositoryBuildVisitors.addBinding().to(LaunchingRepositoryBuildVisitor.class);
    // cancel module builds
    repositoryBuildVisitors.addBinding().to(CancelledRepositoryBuildVisitor.class);
    // launch any queued repository build if present
    repositoryBuildVisitors.addBinding().to(CompletedRepositoryBuildVisitor.class);
    // update GitHub status
    repositoryBuildVisitors.addBinding().to(GitHubStatusVisitor.class);

    Multibinder<ModuleBuildVisitor> moduleBuildVisitors = Multibinder.newSetBinder(binder, ModuleBuildVisitor.class);

    // launch the queued build if nothing upstream
    moduleBuildVisitors.addBinding().to(QueuedModuleBuildVisitor.class);
    // kick off the singularity task if the build is launching
    moduleBuildVisitors.addBinding().to(LaunchingModuleBuildVisitor.class);
    // launch downstream builds if all upstreams succeeded
    moduleBuildVisitors.addBinding().to(DownstreamModuleBuildVisitor.class);
    // kill the singularity task
    moduleBuildVisitors.addBinding().to(SingularityTaskKiller.class);
    // cancel all downstream builds
    moduleBuildVisitors.addBinding().to(DownstreamModuleBuildCanceller.class);
    // complete the repository build once all of the module builds have finished
    moduleBuildVisitors.addBinding().to(RepositoryBuildCompleter.class);
  }
}
