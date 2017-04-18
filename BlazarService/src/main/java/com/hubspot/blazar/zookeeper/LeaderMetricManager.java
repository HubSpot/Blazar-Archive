package com.hubspot.blazar.zookeeper;

import java.util.Map;
import java.util.Set;

import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.data.service.CachingMetricsService;
import com.hubspot.blazar.data.service.MetricsService;
import com.hubspot.blazar.externalservice.BuildClusterHealthChecker;
import com.hubspot.blazar.github.GitHubProtos;
import com.hubspot.singularity.client.SingularityClient;

@Singleton
public class LeaderMetricManager implements LeaderLatchListener {
  private static final Logger LOG = LoggerFactory.getLogger(LeaderMetricManager.class);
  private final MetricRegistry metricRegistry;
  private MetricsService metricsService;
  private BuildClusterHealthChecker buildClusterHealthChecker;
  private Map<String, SingularityClient> singularityClusterClients;
  private final CachingMetricsService cachingMetricsService;
  private static final Set<Class<?>> EXPECTED_EVENT_TYPES = ImmutableSet.of(ModuleBuild.class,
      RepositoryBuild.class, InterProjectBuild.class, GitHubProtos.DeleteEvent.class, GitHubProtos.PushEvent.class, GitHubProtos.CreateEvent.class);

  @Inject
  public LeaderMetricManager(MetricRegistry metricRegistry,
                             MetricsService metricsService,
                             BuildClusterHealthChecker buildClusterHealthChecker,
                             Map<String, SingularityClient> singularityClusterClients,
                             CachingMetricsService cachingMetricsService)  {
    this.metricRegistry = metricRegistry;
    this.metricsService = metricsService;
    this.buildClusterHealthChecker = buildClusterHealthChecker;
    this.singularityClusterClients = singularityClusterClients;
    this.cachingMetricsService = cachingMetricsService;
  }

  @Override
  public void isLeader() {
    LOG.info("Now the leader, doing leader-only metric registration");
    registerGauges();
  }

  @Override
  public void notLeader() {
    LOG.info("Not the leader, removing leader-only registered metrics");
    deRegisterAllLeaderGauges();
  }

  private void deRegisterAllLeaderGauges() {
    for (Map.Entry<String, Gauge> entry : metricRegistry.getGauges().entrySet()) {
      if (entry.getKey().contains(getClass().getName())) {
        metricRegistry.remove(entry.getKey());
      }
    }
  }

  private void registerGauges() {
    // Queued event gauges
    for (Class<?> type : EXPECTED_EVENT_TYPES) {
      metricRegistry.register(queuedEventTypeToMetricName(type), makeQueuedItemGauge(type));
    }

    metricRegistry.register(getClass() + "." + "queues.size", makeQueuedItemTypesGauge());

    // build state gauges
    for (ModuleBuild.State state : ModuleBuild.State.values()) {
      if (state.isRunning() || state.isWaiting()) {
        metricRegistry.register(makeMetricName(ModuleBuild.class, state), makeModuleStateGauge(state));
      }
    }

    for (RepositoryBuild.State state : RepositoryBuild.State.values()) {
      if (!state.isComplete()) {
        metricRegistry.register(makeMetricName(RepositoryBuild.class, state), makeBranchStateGauge(state));
      }
    }

    for (InterProjectBuild.State state : InterProjectBuild.State.values()) {
      if (!state.isFinished()) {
        metricRegistry.register(makeMetricName(InterProjectBuild.class, state), makeInterProjectStateGauge(state));
      }
    }

    // Hung build gauges (To alert on any builds that appear to not be progressing)
    Gauge<Integer> hungBuildCountGauge = cachingMetricsService::getCachedHungBuildCount;
    metricRegistry.register(getClass().getName() + ".RepositoryBuild.hung.count", hungBuildCountGauge);

    Gauge<String> hungBuildDataGauge = cachingMetricsService::getCachedHungBuildData;
    metricRegistry.register(getClass().getName() + ".RepositoryBuild.hung.data", hungBuildDataGauge);

    for (String clusterName : singularityClusterClients.keySet()) {
      Gauge<Boolean> clusterStatusGauge = () -> buildClusterHealthChecker.isClusterAvailable(clusterName);
      String name = getClass().getName() + String.format(".%s.ClusterHealthy", clusterName);

      LOG.info(name);
      metricRegistry.register(name, clusterStatusGauge);
    }
  }

  private String queuedEventTypeToMetricName(Class<?> eventType) {
    return getClass().getName() + ".queues." + eventType.getSimpleName() + ".size";
  }

  private String makeMetricName(Class<?> buildClass, Enum<?> state) {
    return getClass().getName() + "." + buildClass.getSimpleName() + "." + state.name() + ".size";
  }

  private Gauge<Number> makeBranchStateGauge(RepositoryBuild.State state) {
    return () -> cachingMetricsService.getCachedActiveBranchBuildCountByState(state);
  }

  private Gauge<Number> makeModuleStateGauge(ModuleBuild.State state) {
    return () -> cachingMetricsService.getCachedActiveModuleBuildCountByState(state);
  }

  private Gauge<Number> makeInterProjectStateGauge(InterProjectBuild.State state) {
    return () -> cachingMetricsService.getCachedActiveInterProjectBuildCountByState(state);
  }

  private Gauge<Number> makeQueuedItemGauge(Class<?> type) {
    return () -> cachingMetricsService.getCachedQueuedItemCountByType(type);
  }

  private Gauge<Number> makeQueuedItemTypesGauge() {
    return cachingMetricsService::getPendingEventTypeCount;
  }

}
