package com.hubspot.blazar.zookeeper;

import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;
import com.hubspot.blazar.data.service.CachingMetricsService;

@Singleton
public class LeaderMetricManager implements LeaderLatchListener {
  private static final Logger LOG = LoggerFactory.getLogger(LeaderMetricManager.class);
  private final CuratorFramework curatorFramework;
  private final MetricRegistry metricRegistry;
  private final CachingMetricsService cachingMetricsService;

  @Inject
  public LeaderMetricManager(CuratorFramework curatorFramework,
                              MetricRegistry metricRegistry,
                              CachingMetricsService cachingMetricsService)  {
    this.curatorFramework = curatorFramework;
    this.metricRegistry = metricRegistry;
    this.cachingMetricsService = cachingMetricsService;
  }

  @Override
  public void isLeader() {
    LOG.info("Now the leader, doing leader-only metric registration");
    registerMysqlGauges();
    registerZkGauges();
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

  private void registerZkGauges() {
    metricRegistry.register(zkPathToMetricName("/queues"), makeZkQueueGauge("/queues"));
    try {
      for (String queue : curatorFramework.getChildren().forPath("/queues")) {
        String queuePath = ZKPaths.makePath("/queues", queue);
        metricRegistry.register(zkPathToMetricName(queuePath), makeZkQueueGauge(queuePath));
      }
    } catch (Exception e) {
      LOG.error("Could not register the metric gauges", e);
    }
  }

  private void registerMysqlGauges() {

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
  }

  private String zkPathToMetricName(String path) {
    return getClass().getName() + path.replace('/', '.') + ".size";
  }

  private String makeMetricName(Class<?> buildClass, Enum<?> state) {
    return getClass().getName() + "." + buildClass.getSimpleName() + "." + state.name() + ".size";
  }

  private Gauge<Number> makeZkQueueGauge(final String path){
    return () -> {
      try {
        return curatorFramework.getChildren().forPath(path).size();
      } catch (Exception e) {
        return 0;
      }
    };
  }

  private Gauge<Number> makeBranchStateGauge(RepositoryBuild.State state) {
    return () -> cachingMetricsService.getCachedActiveBranchBuildCountByState(state);
  }

  private Gauge<Number> makeModuleStateGauge(ModuleBuild.State state) {
    return () -> cachingMetricsService.getCachedActiveModuleBuildCountByState(state);
  }
}
