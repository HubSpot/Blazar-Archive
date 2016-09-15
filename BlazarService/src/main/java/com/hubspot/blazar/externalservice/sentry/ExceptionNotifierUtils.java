package com.hubspot.blazar.externalservice.sentry;

import java.util.HashMap;
import java.util.Map;

import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;

public class ExceptionNotifierUtils {

  public static void notify(Throwable t, RepositoryBuild build, ExceptionNotifier notifier) {
      Map<String, String> extraData = new HashMap<>();
      extraData.put("repositoryBuild", build.toString());
      extraData.put("buildOptions", build.getBuildOptions().toString());
      extraData.put("buildTrigger", build.getBuildTrigger().toString());
      notifier.notify(t, extraData);
  }

  public static void notify(Throwable t, ModuleBuild build, ExceptionNotifier exceptionNotifier) {
    Map<String, String> extraData = new HashMap<>();
    extraData.put("moduleBuild", build.toString());
    extraData.put("runId", build.getRunId().toString());
    exceptionNotifier.notify(t, extraData);
  }

  public static void notify(Throwable t, InterProjectBuild build, ExceptionNotifier notifier) {
    Map<String, String> extraData = new HashMap<>();
    extraData.put("build", build.toString());
    extraData.put("moduleIds", build.getModuleIds().toString());
    extraData.put("buildTrigger", build.getBuildTrigger().toString());
    notifier.notify(t, extraData);
  }
}
