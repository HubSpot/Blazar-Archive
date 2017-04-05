package com.hubspot.blazar.externalservice;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.singularity.SingularityRequestParent;
import com.hubspot.singularity.client.SingularityClient;

import io.dropwizard.lifecycle.Managed;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@Singleton
public class BuildClusterHealthChecker implements LeaderLatchListener, Managed {
  private static final int HEALTH_CHECK_INTERVAL_SECONDS = 30;

  private static final Logger LOG = LoggerFactory.getLogger(BuildClusterHealthChecker.class);

  private final AtomicBoolean leader;
  private final AtomicBoolean running;
  private final Map<String, SingularityClient> singularityClusterClients;
  private final BlazarConfiguration blazarConfiguration;
  private final Map<String, ClusterHealthCheck> clusterHealthCheckMap = new ConcurrentHashMap<>();
  private final AtomicReference<Disposable> clusterHealthObserver;

  @Inject
  public BuildClusterHealthChecker(Map<String, SingularityClient> singularityClusterClients, BlazarConfiguration blazarConfiguration) {
    this.singularityClusterClients = singularityClusterClients;
    this.blazarConfiguration = blazarConfiguration;

    leader = new AtomicBoolean(false);
    running = new AtomicBoolean(false);
    clusterHealthObserver = new AtomicReference<>();
  }

  @Override
  public void start() throws Exception {
    running.compareAndSet(false, true);
    LOG.info("We have been started");
  }

  @Override
  public void stop() throws Exception {
    running.compareAndSet(true, false);
    LOG.info("We have been stopped. Stopping build clusters health checks");
    clusterHealthObserver.get().dispose();
  }

  @Override
  public void isLeader() {
    LOG.info("We are the leader. Starting build cluster health check monitoring");
    leader.set(true);
    if (running.get()) {
      Disposable disposable = getObservableOfAllClustersHealth().subscribe(clusterHealthCheck -> {
        clusterHealthCheckMap.put(clusterHealthCheck.getClusterName(), clusterHealthCheck);
      });
      clusterHealthObserver.getAndSet(disposable);
    }
  }

  @Override
  public void notLeader() {
    LOG.info("We are not the leader. Stopping build cluster health check monitoring");
    leader.set(false);
    clusterHealthObserver.get().dispose();
  }

  public boolean isSomeClusterAvailable() {
    return clusterHealthCheckMap.values().stream().anyMatch((ClusterHealthCheck::isHealthy));
  }

  public boolean isClusterAvailable(String clusterName) {
    return clusterHealthCheckMap.get(clusterName) != null && clusterHealthCheckMap.get(clusterName).isHealthy();
  }

  private Observable<ClusterHealthCheck> getObservableOfAllClustersHealth() {
    List<Observable<ClusterHealthCheck>> clusterHealthCheckObservables = singularityClusterClients.keySet().stream()
        .map(clusterName -> getObservableSingularityClusterHealth(clusterName)).collect(Collectors.toList());
    return Observable.merge(clusterHealthCheckObservables);
  }

  private Observable<ClusterHealthCheck> getObservableSingularityClusterHealth(String clusterName) {
    return Observable.timer(HEALTH_CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS).map(tick -> {
      SingularityClient singularityClient = singularityClusterClients.get(clusterName);
      String singularityRequestForBuilds = blazarConfiguration.getSingularityClusterConfigurations().get(clusterName).getRequest();
      Optional<SingularityRequestParent> singularityRequestParentOptional;
      try {
        singularityRequestParentOptional = singularityClient.getSingularityRequest(singularityRequestForBuilds);
        if (singularityRequestParentOptional.isPresent()) {
          return new ClusterHealthCheck(clusterName, true);
        }
        return new ClusterHealthCheck(clusterName, false);
      } catch (Exception e) {
        return new ClusterHealthCheck(clusterName,false);
      }

    }).subscribeOn(Schedulers.io()).observeOn(Schedulers.computation());
  }

  private static final class ClusterHealthCheck {
    private final String clusterName;
    private final boolean healthy;

    public String getClusterName() {
      return clusterName;
    }

    public boolean isHealthy() {
      return healthy;
    }

    public ClusterHealthCheck(String clusterName, boolean healthy) {
      this.clusterName = clusterName;
      this.healthy = healthy;


    }
  }
}
