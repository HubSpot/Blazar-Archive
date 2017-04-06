package com.hubspot.blazar.externalservice;

import static com.hubspot.blazar.externalservice.BuildClusterService.BuildContainerInfo.BuildContainerState.NOT_STARTED;
import static com.hubspot.blazar.externalservice.BuildClusterService.BuildContainerInfo.BuildContainerState.UNKNOWN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.blazar.base.GitInfo;
import com.hubspot.blazar.base.LogChunk;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.config.BlazarConfiguration;
import com.hubspot.blazar.config.SingularityClusterConfiguration;
import com.hubspot.blazar.data.service.BranchService;
import com.hubspot.blazar.data.service.ModuleBuildService;
import com.hubspot.blazar.data.service.ModuleService;
import com.hubspot.blazar.exception.BuildClusterException;
import com.hubspot.blazar.exception.LogNotFoundException;
import com.hubspot.blazar.exception.NonRetryableBuildException;
import com.hubspot.blazar.externalservice.BuildClusterService.BuildContainerInfo.BuildContainerState;
import com.hubspot.blazar.util.TimeUtils;
import com.hubspot.horizon.AsyncHttpClient;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.mesos.json.MesosFileChunkObject;
import com.hubspot.singularity.ExtendedTaskState;
import com.hubspot.singularity.SingularityS3Log;
import com.hubspot.singularity.SingularitySandbox;
import com.hubspot.singularity.SingularitySandboxFile;
import com.hubspot.singularity.SingularityTaskCleanupResult;
import com.hubspot.singularity.SingularityTaskHistory;
import com.hubspot.singularity.SingularityTaskHistoryUpdate;
import com.hubspot.singularity.SingularityTaskHistoryUpdate.SimplifiedTaskState;
import com.hubspot.singularity.SingularityTaskIdHistory;
import com.hubspot.singularity.api.SingularityKillTaskRequest;
import com.hubspot.singularity.api.SingularityRunNowRequest;
import com.hubspot.singularity.client.SingularityClient;

@Singleton
public class BuildClusterService {

  private static final Logger LOG = LoggerFactory.getLogger(BuildClusterService.class);
  private static final String DEFAULT_LOG_FILE_NAME = "service.log";

  private final Map<String, SingularityClient> singularityClusterClients;
  private final BlazarConfiguration blazarConfiguration;
  private final ModuleService moduleService;
  protected final ModuleBuildService moduleBuildService;
  private final BranchService branchService;
  private final BuildClusterHealthChecker buildClusterHealthChecker;
  private final List<String> availableClusters;
  private final AtomicInteger nextClusterIndex;
  private final SingularityKillTaskRequest singularityKillTaskRequest;
  private final AsyncHttpClient asyncHttpClient;

  @Inject
  public BuildClusterService(Map<String, SingularityClient> singularityClusterClients,
                             BlazarConfiguration blazarConfiguration,
                             ModuleService moduleService,
                             ModuleBuildService moduleBuildService,
                             BranchService branchService,
                             BuildClusterHealthChecker buildClusterHealthChecker,
                             AsyncHttpClient asyncHttpClient) {
    this.singularityClusterClients = singularityClusterClients;
    this.blazarConfiguration = blazarConfiguration;
    this.moduleService = moduleService;
    this.moduleBuildService = moduleBuildService;
    this.buildClusterHealthChecker = buildClusterHealthChecker;
    this.branchService = branchService;
    this.asyncHttpClient = asyncHttpClient;

    this.singularityKillTaskRequest = new SingularityKillTaskRequest(
        Optional.of(true),
        Optional.of("The associated Blazar build has been cancelled"),
        Optional.absent(),
        Optional.absent());

    availableClusters = ImmutableList.<String>builder().addAll(singularityClusterClients.keySet()).build();
    nextClusterIndex = new AtomicInteger(0);
  }

  /**
   * It starts the docker container that is going to do the build
   * This doesn't initiate the build itself. The build executor should periodically call back to blazar and check
   * the module build state. Building can start when the state is LAUNCHING. At the point that we launch the build
   * container the state of the build is either 'QUEUED' or 'WAITING_FOR_UPSTREAM_BUILD'
   * @param moduleBuild
   *    the module build that the launched container should execute
   * @throws NonRetryableBuildException
   */
  public synchronized void launchBuildContainer(ModuleBuild moduleBuild) throws BuildClusterException {
    Optional<String> clusterToUse = pickClusterToLaunchBuild(moduleBuild, Collections.emptySet());
    if (!clusterToUse.isPresent()) {
      String message = String.format("Could not find a cluster to launch module build %d", moduleBuild.getId().get());
      LOG.warn(message);
      throw new BuildClusterException(message);
    }
    SingularityClient singularityClient = singularityClusterClients.get(clusterToUse);
    SingularityClusterConfiguration singularityClusterConfiguration = blazarConfiguration.getSingularityClusterConfigurations().get(clusterToUse);
    try {
      singularityClient.runSingularityRequest(singularityClusterConfiguration.getRequest(), Optional.of(buildRequest(moduleBuild)));
    } catch (Exception e) {
      String message = String.format("Failed to start build container in cluster %s for module build %d", clusterToUse, moduleBuild.getId().get());
      LOG.error(message, e);
      throw new BuildClusterException(message, e);
    }
  }

  public BuildContainerInfo getBuildContainerInfo(ModuleBuild moduleBuild) throws BuildClusterException {
    if (!moduleBuild.getBuildClusterName().isPresent()) {
      throw new BuildClusterException(String.format("The 'buildClusterName' is missing in module build %d. Cannot get the state of the associated build container", moduleBuild.getId().get()));
    }
    String buildClusterName = moduleBuild.getBuildClusterName().get();
    if (isMesosCluster(buildClusterName)) {
      return getMesosContainerState(moduleBuild);
    } else {
      throw new BuildClusterException(String.format("Could not find build cluster name: '%s' among the configured build clusters", buildClusterName));
    }
  }

  public void killBuildContainer(ModuleBuild moduleBuild) throws BuildClusterException {
    if (!moduleBuild.getBuildClusterName().isPresent()) {
      throw new BuildClusterException(String.format("The 'buildClusterName' is missing in module build %d. Cannot find and kill the associated build container", moduleBuild.getId().get()));
    }

    String buildClusterName = moduleBuild.getBuildClusterName().get();
    if (isMesosCluster(buildClusterName)) {
      killMesosContainer(moduleBuild);
    } else {
      throw new BuildClusterException(String.format("Could not find build cluster name: '%s' among the configured build clusters", buildClusterName));
    }
  }

  public LogChunk getBuildContainerLog(ModuleBuild moduleBuild, long byteOffset, long byteLength) throws Exception {
    if (!moduleBuild.getBuildClusterName().isPresent()) {
      throw new BuildClusterException(String.format("The 'buildClusterName' is missing in module build %d. Cannot get the log of the associated build container", moduleBuild.getId().get()));
    }

    String buildClusterName = moduleBuild.getBuildClusterName().get();
    if (isMesosCluster(buildClusterName)) {
      return getMesosContainerLog(moduleBuild, byteOffset, byteLength);
    } else {
      throw new BuildClusterException(String.format("Could not find build cluster name: '%s' among the configured build clusters", buildClusterName));
    }
  }

  public long getBuildContainerLogSize(ModuleBuild moduleBuild) throws BuildClusterException, LogNotFoundException {
    if (!moduleBuild.getBuildClusterName().isPresent()) {
      throw new BuildClusterException(String.format("The 'buildClusterName' is missing in module build %d. Cannot get the log size of the associated build container", moduleBuild.getId().get()));
    }

    String buildClusterName = moduleBuild.getBuildClusterName().get();
    if (isMesosCluster(buildClusterName)) {
      return getMesosContainerLogSize(moduleBuild);
    } else {
      throw new BuildClusterException(String.format("Could not find build cluster name: '%s' among the configured build clusters. Cannot get the log size of the build container associated with module build %d",
          buildClusterName, moduleBuild.getId().get()));
    }
  }

  public String getBuildContainerLogUrl(ModuleBuild moduleBuild) throws BuildClusterException, LogNotFoundException {
    if (!moduleBuild.getBuildClusterName().isPresent()) {
      throw new BuildClusterException(String.format("The 'buildClusterName' is missing in module build %d. Cannot get the log url of the associated build container", moduleBuild.getId().get()));
    }

    String buildClusterName = moduleBuild.getBuildClusterName().get();
    if (isMesosCluster(buildClusterName)) {
      return getMesosContainerLogUrl(moduleBuild);
    } else {
      throw new BuildClusterException(String.format("Could not find build cluster name: '%s' among the configured build clusters. Cannot get the log size of the build container associated with module build %d",
          buildClusterName, moduleBuild.getId().get()));
    }
  }

  private String getMesosContainerLogUrl(ModuleBuild moduleBuild) throws BuildClusterException, LogNotFoundException {
    if (!moduleBuild.getTaskId().isPresent()) {
      throw new LogNotFoundException(String.format("Cannot get container log for module build %s. Could not find the associated singularity task id.",
          moduleBuild.getId().get()));
    }

    String singularityTaskId = moduleBuild.getTaskId().get();
    String buildClusterName = moduleBuild.getBuildClusterName().get();
    SingularityClient singularityClient = singularityClusterClients.get(buildClusterName);

    Optional<SingularitySandbox> sandboxOptional = singularityClient.browseTaskSandBox(singularityTaskId, singularityTaskId);

    java.util.Optional<SingularitySandboxFile> buildLogFile = java.util.Optional.empty();
    if (sandboxOptional.isPresent()) {
      buildLogFile = sandboxOptional.get().getFiles().stream().filter(logFile -> DEFAULT_LOG_FILE_NAME.equals(logFile.getName())).findFirst();
    }

    final String buildLogUrl;

    if (buildLogFile.isPresent()) {
      String host = sandboxOptional.get().getSlaveHostname();
      int port = blazarConfiguration.getSingularityClusterConfigurations().get(buildClusterName).getSlaveHttpPort();
      String path = sandboxOptional.get().getFullPathToRoot() + "/" + sandboxOptional.get().getCurrentDirectory() + "/" + buildLogFile.get().getName();
      buildLogUrl = String.format("http://%s:%d/files/download.json?path=%s", host, port, path);
    } else {
      SingularityS3Log urlData = findS3ServiceLog(singularityTaskId, singularityClient);
      buildLogUrl = urlData.getDownloadUrl();
    }

    return buildLogUrl;
  }

  private long getMesosContainerLogSize(ModuleBuild moduleBuild) throws LogNotFoundException {
    if (!moduleBuild.getTaskId().isPresent()) {
      throw new LogNotFoundException(String.format("Cannot get container log for module build %s. Could not find the associated singularity task id.",
          moduleBuild.getId().get()));
    }

    String singularityTaskId = moduleBuild.getTaskId().get();
    String buildClusterName = moduleBuild.getBuildClusterName().get();
    SingularityClient singularityClient = singularityClusterClients.get(buildClusterName);
    String path = singularityTaskId + "/" + DEFAULT_LOG_FILE_NAME;

    Optional<MesosFileChunkObject> completeLogFileChunk = singularityClient.readSandBoxFile(singularityTaskId, path,
        Optional.absent(), Optional.absent(), Optional.absent());
    long logFileSize;
    if (completeLogFileChunk.isPresent()) {
      logFileSize = completeLogFileChunk.get().getOffset();
    } else {
      logFileSize = findS3ServiceLog(singularityTaskId, singularityClient).getSize();
    }

    return logFileSize;
  }

  private LogChunk getMesosContainerLog(ModuleBuild moduleBuild, long byteOffset, long byteLength) throws BuildClusterException, LogNotFoundException {
    if (!moduleBuild.getTaskId().isPresent()) {
      throw new LogNotFoundException(String.format("Cannot get container log for module build %s. Could not find the associated singularity task id.",
          moduleBuild.getId().get()));
    }

    String singularityTaskId = moduleBuild.getTaskId().get();
    String buildClusterName = moduleBuild.getBuildClusterName().get();
    SingularityClient singularityClient = singularityClusterClients.get(buildClusterName);
    String path = singularityTaskId + "/" + DEFAULT_LOG_FILE_NAME;
    Optional<String> grep = Optional.absent();

    Optional<MesosFileChunkObject> chunk = singularityClient.readSandBoxFile(singularityTaskId, path, grep,
        Optional.of(byteOffset), Optional.of(byteLength));
    if (chunk.isPresent()) {
      if (chunk.get().getData().isEmpty() && logCompleted(moduleBuild, singularityClient)) {
        return new LogChunk(chunk.get().getData(), chunk.get().getOffset(), -1);
      } else {
        return new LogChunk(chunk.get().getData(), chunk.get().getOffset());
      }
    } else {
      SingularityS3Log s3Log = findS3ServiceLog(singularityTaskId, singularityClient);
      if (byteOffset >= s3Log.getSize()) {
        return new LogChunk("", s3Log.getSize(), -1);
      }

      return readS3LogChunk(s3Log.getGetUrl(), byteOffset, byteLength);
    }

  }

  private SingularityS3Log findS3ServiceLog(String singularityTaskId, SingularityClient singularityClient) throws LogNotFoundException {
    Collection<SingularityS3Log> s3Logs = singularityClient.getTaskLogs(singularityTaskId);
    List<SingularityS3Log> serviceLogs = new ArrayList<>();
    for (SingularityS3Log s3Log : s3Logs) {
      if (s3Log.getGetUrl().contains(DEFAULT_LOG_FILE_NAME)) {
        serviceLogs.add(s3Log);
      }
    }

    if (serviceLogs.isEmpty()) {
      throw new LogNotFoundException("No S3 log found for singularity task id" + singularityTaskId);
    } else if (serviceLogs.size() > 1) {
      throw new LogNotFoundException("Multiple S3 logs found for singularity task id" + singularityTaskId);
    } else {
      return serviceLogs.get(0);
    }
  }

  private boolean logCompleted(ModuleBuild moduleBuild, SingularityClient singularityClient) {
    return moduleBuild.getState().isComplete() && mesosContainerFinishedOrNeverLaunched(moduleBuild.getTaskId().get(), singularityClient);
  }

  private boolean mesosContainerFinishedOrNeverLaunched(String singularityTaskId, SingularityClient singularityClient) {
    // never launched
    Optional<SingularityTaskHistory> taskHistory = singularityClient.getHistoryForTask(singularityTaskId);
    if (!taskHistory.isPresent()) {
      return true;
    }

    // finished
    SimplifiedTaskState taskState = SingularityTaskHistoryUpdate.getCurrentState(taskHistory.get().getTaskUpdates());
    return taskState == SimplifiedTaskState.DONE;
  }

  private LogChunk readS3LogChunk(String url, long offset, long length) throws BuildClusterException {
    HttpRequest request = HttpRequest.newBuilder()
        .setUrl(url)
        .addHeader("Range", String.format("bytes=%d-%d", offset, offset + length - 1))
        .build();

    HttpResponse response;
    try {
      response = asyncHttpClient.execute(request).get();
    } catch (Exception e) {
      throw new BuildClusterException(String.format("An error occured while retrieving container log from S3. The error is: %s",
          e.getMessage()), e);
    }

    if (response.isSuccess()) {
      return new LogChunk(response.getAsBytes(), offset);
    } else {
      String message = String.format("Error reading S3 log, status code %d, response %s", response.getStatusCode(), response.getAsString());
      throw new BuildClusterException(message);
    }
  }

  private void killMesosContainer(ModuleBuild moduleBuild) throws BuildClusterException {
    if (!moduleBuild.getTaskId().isPresent()) {
      throw new BuildClusterException(String.format("Cannot kill container for module build %s. Could not find the associated singularity task id.",
          moduleBuild.getId().get()));
    }

    try {
      String singularityTaskId = moduleBuild.getTaskId().get();
      String buildClusterName = moduleBuild.getBuildClusterName().get();
      SingularityClient singularityClient = singularityClusterClients.get(buildClusterName);
      Optional<SingularityTaskCleanupResult> result = singularityClient.killTask(singularityTaskId,
          Optional.of(singularityKillTaskRequest));
      if (!result.isPresent()) {
        LOG.info("Tried to kill singularity task for module build {} but the task was not found", moduleBuild.getId().get());
      } else {
        LOG.info("Request to kill singularity task for module build {} was successfully sent. The result is {}",
            moduleBuild.getId().get(), result.get().toString());
      }
    } catch (Exception e) {
      LOG.error("The request to kill singularity task for module build {} failed. The error is: {}",
          moduleBuild.getId().get(), e.getMessage(), e);
      throw new BuildClusterException(String.format("The request to kill singularity task for module build %d failed. The error is: %s",
          moduleBuild.getId().get(), e.getMessage()), e);
    }
  }

  private BuildContainerInfo getMesosContainerState(ModuleBuild moduleBuild) {
    String buildClusterName = moduleBuild.getBuildClusterName().get();
    String singularityRequestId = blazarConfiguration.getSingularityClusterConfigurations().get(buildClusterName).getRequest();
    String runId =  String.valueOf(moduleBuild.getId().get());
    SingularityClient singularityClient = singularityClusterClients.get(buildClusterName);

    Optional<SingularityTaskIdHistory> task = singularityClient.getHistoryForTask(singularityRequestId, runId);

    if (!task.isPresent()) {
      return new BuildContainerInfo(NOT_STARTED, Optional.absent(), TimeUtils.nowInUtcMillis());
    }

    long taskStateUpdatedAtMillis = task.get().getUpdatedAt();
    Optional<ExtendedTaskState> taskState = task.get().getLastTaskState();
    if (!taskState.isPresent()) {
      return new BuildContainerInfo(UNKNOWN, Optional.of(task.get().getTaskId().getId()), taskStateUpdatedAtMillis);
    }

    if (taskState.get().isDone()) {
      return new BuildContainerInfo(BuildContainerState.FINISHED, Optional.of(task.get().getTaskId().getId()), taskStateUpdatedAtMillis);
    }

    return new BuildContainerInfo(BuildContainerState.RUNNING, Optional.of(task.get().getTaskId().getId()), taskStateUpdatedAtMillis);
  }

  private boolean isMesosCluster(String buildClusterName) {
    return blazarConfiguration.getSingularityClusterConfigurations().keySet().contains(buildClusterName);
  }

  private Optional<String> pickClusterToLaunchBuild(ModuleBuild moduleBuild, Set<String> examinedClusters) throws BuildClusterException {
    if (examinedClusters.equals(availableClusters)) {
      return Optional.absent();
    }

    String clusterToUse = getAndSetNextClusterToUse();
    examinedClusters.add(clusterToUse);
    SingularityClusterConfiguration singularityClusterConfiguration = blazarConfiguration.getSingularityClusterConfigurations().get(clusterToUse);

    switch (singularityClusterConfiguration.getBuildStrategy()) {
      case ALWAYS:
        return checkAvailabilityAndPersistCluster(clusterToUse, moduleBuild, examinedClusters);
      case WHITELIST:
        if (singularityClusterConfiguration.getRepositories().isEmpty()) {
          LOG.warn("You have selected the 'WHITELIST' build strategy for cluster {} but you have not provided any repositories. So the cluster is considered always available", clusterToUse);
          return checkAvailabilityAndPersistCluster(clusterToUse, moduleBuild, examinedClusters);
        }
        Optional<String> moduleRepository = getModuleRepository(moduleBuild.getModuleId());
        if (!moduleRepository.isPresent()) {
          throw new BuildClusterException(String.format("Could not get the repository for module %d", moduleBuild.getModuleId()));
        }
        if (singularityClusterConfiguration.getRepositories().contains(moduleRepository.get())) {
          return checkAvailabilityAndPersistCluster(clusterToUse, moduleBuild, examinedClusters);
        }
        return pickClusterToLaunchBuild(moduleBuild, examinedClusters);
      case EXCLUSIVE_WHITELIST:
      case EMERGENCY:
      case EMERGENCY_AND_WHITELIST:
      case EMERGENCY_AND_EXCLUSIVE_WHITELIST:
      default:
        throw new BuildClusterException(String.format("Strategy %s is not yet implemented", singularityClusterConfiguration.getBuildStrategy()));
    }
  }

  private SingularityRunNowRequest buildRequest(ModuleBuild moduleBuild) {
    String buildId = Long.toString(moduleBuild.getId().get());
    Optional<com.hubspot.mesos.Resources> buildResources = Optional.absent();
    if (moduleBuild.getResolvedConfig().isPresent() && moduleBuild.getResolvedConfig().get().getBuildResources().isPresent()) {
      buildResources = Optional.of(moduleBuild.getResolvedConfig().get().getBuildResources().get().toMesosResources());
    }

    return new SingularityRunNowRequest(
        Optional.of("Running Blazar module build " + buildId),
        Optional.of(false),
        Optional.of(buildId),
        Optional.of(Arrays.asList("--buildId", buildId)),
        buildResources);
  }

  private Optional<String> getModuleRepository(int moduleId) {
    int branchId = moduleService.getBranchIdFromModuleId(moduleId);
    Optional<GitInfo> moduleBranchMaybe = branchService.get(branchId);
    if (moduleBranchMaybe.isPresent()) {
      GitInfo moduleBranch = moduleBranchMaybe.get();
      return Optional.of(String.format("%s-%s-%s", moduleBranch.getHost(), moduleBranch.getOrganization(), moduleBranch.getRepository()));
    }
    return Optional.absent();
  }

  private Optional<String> checkAvailabilityAndPersistCluster(String clusterToUse,
                                                              ModuleBuild moduleBuild,
                                                              Set<String> examinedClusters) throws BuildClusterException {
    if (buildClusterHealthChecker.isClusterAvailable(clusterToUse)) {
      moduleBuildService.updateBuildClusterName(moduleBuild.getId().get(), clusterToUse);
      return Optional.of(clusterToUse);
    } else {
      LOG.warn("Build cluster {} is unavailable. Will look for another cluster", clusterToUse);
      return pickClusterToLaunchBuild(moduleBuild, examinedClusters);
    }
  }

  private String getAndSetNextClusterToUse() {
    int clusterIndex;
    try {
      clusterIndex = nextClusterIndex.incrementAndGet();
    } catch (Exception e) { // in the almost improbable case we have reached the max int we reset the counter;
      clusterIndex = nextClusterIndex.getAndSet(0);
    }
    return availableClusters.get((clusterIndex % availableClusters.size()) - 1);
  }

  public static class BuildContainerInfo {
    public enum BuildContainerState {
      FINISHED, // The container has run and is not running any more, it could have been lost, killed, failed, succeeded
      RUNNING, // Container has started and build is still running
      NOT_STARTED, // Container has not yet started
      UNKNOWN // We got back incomplete information about the container and the state couldn't be determined
    }

    BuildContainerState state;
    /**
     * An identifier for the container that runs the build
     */
    Optional<String>containerId;
    /**
     * Last time the state was updated
     */
    long updatedAtMillis;

    public BuildContainerInfo(BuildContainerState state, Optional<String> containerId, long updatedAtMillis) {
      this.state = state;
      this.containerId = MoreObjects.firstNonNull(containerId, Optional.absent());
      this.updatedAtMillis = updatedAtMillis;
    }

    public BuildContainerState getState() {
      return state;
    }

    public Optional<String> getContainerId() {
      return containerId;
    }

    public long getUpdatedAtMillis() {
      return updatedAtMillis;
    }
  }

}
