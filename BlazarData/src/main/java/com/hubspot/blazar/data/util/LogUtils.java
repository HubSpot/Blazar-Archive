package com.hubspot.blazar.data.util;

import static org.slf4j.event.Level.DEBUG;
import static org.slf4j.event.Level.INFO;
import static org.slf4j.event.Level.WARN;
import static org.slf4j.event.Level.ERROR;

import org.slf4j.Logger;
import org.slf4j.event.Level;

import com.hubspot.blazar.base.InterProjectBuild;
import com.hubspot.blazar.base.ModuleBuild;
import com.hubspot.blazar.base.RepositoryBuild;

public class LogUtils {

  private final Logger logger;

  private static final String BRANCH_BUILD_CONTEXT_FORMAT_STRING = "During branch build %s";
  private static final String MODULE_BUILD_CONTEXT_FORMAT_STRING = "During module build %s";
  private static final String IPB_BUILD_CONTEXT_FORMAT_STRING = "During inter-project build %s";

  public LogUtils(Logger logger) {
    this.logger = logger;
  }

  private void doLog(Level level, String format, Object... arguments) {
    switch (level) {
      case DEBUG:
        logger.debug(format, arguments);
        break;
      case INFO:
        logger.info(format, arguments);
        break;
      case WARN:
        logger.warn(format, arguments);
        break;
      case ERROR:
        logger.error(format, arguments);
        break;
      default:
        break;
    }

  }

  // DEBUG
  public void debug(String format, Object... arguments) {
    doLog(WARN, format, arguments);
  }

  public void debug(ModuleBuild buildContext, String format, Object... arguments) {
    doLog(DEBUG, String.format(MODULE_BUILD_CONTEXT_FORMAT_STRING, buildContext) + format, arguments);
  }

  public void debug(RepositoryBuild buildContext, String format, Object... arguments) {
    doLog(DEBUG, String.format(BRANCH_BUILD_CONTEXT_FORMAT_STRING, buildContext) + format, arguments);
  }

  public void debug(InterProjectBuild buildContext, String format, Object... arguments) {
    doLog(DEBUG, String.format(IPB_BUILD_CONTEXT_FORMAT_STRING, buildContext) + format, arguments);
  }

  // INFO
  public void info(String format, Object... arguments) {
    doLog(WARN, format, arguments);
  }

  public void info(ModuleBuild buildContext, String format, Object... arguments) {
    doLog(INFO, String.format(MODULE_BUILD_CONTEXT_FORMAT_STRING, buildContext) + format, arguments);
  }

  public void info(RepositoryBuild buildContext, String format, Object... arguments) {
    doLog(INFO, String.format(BRANCH_BUILD_CONTEXT_FORMAT_STRING, buildContext) + format, arguments);
  }

  public void info(InterProjectBuild buildContext, String format, Object... arguments) {
    doLog(INFO, String.format(IPB_BUILD_CONTEXT_FORMAT_STRING, buildContext) + format, arguments);
  }

  // WARN
  public void warn(String format, Object... arguments) {
    doLog(WARN, format, arguments);
  }

  public void warn(ModuleBuild buildContext, String format, Object... arguments) {
    doLog(WARN, String.format(MODULE_BUILD_CONTEXT_FORMAT_STRING, buildContext) + format, arguments);
  }

  public void warn(RepositoryBuild buildContext, String format, Object... arguments) {
    doLog(WARN, String.format(BRANCH_BUILD_CONTEXT_FORMAT_STRING, buildContext) + format, arguments);
  }

  public void warn(InterProjectBuild buildContext, String format, Object... arguments) {
    doLog(WARN, String.format(IPB_BUILD_CONTEXT_FORMAT_STRING, buildContext) + format, arguments);
  }

  // ERROR
  public void error(String format, Object... arguments) {
    doLog(WARN, format, arguments);
  }

  public void error(ModuleBuild buildContext, String format, Object... arguments) {
    doLog(ERROR, String.format(MODULE_BUILD_CONTEXT_FORMAT_STRING, buildContext) + format, arguments);
  }

  public void error(RepositoryBuild buildContext, String format, Object... arguments) {
    doLog(ERROR, String.format(BRANCH_BUILD_CONTEXT_FORMAT_STRING, buildContext) + format, arguments);
  }

  public void error(InterProjectBuild buildContext, String format, Object... arguments) {
    doLog(ERROR, String.format(IPB_BUILD_CONTEXT_FORMAT_STRING, buildContext) + format, arguments);
  }
}
