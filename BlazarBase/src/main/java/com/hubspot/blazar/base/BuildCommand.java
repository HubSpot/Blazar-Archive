package com.hubspot.blazar.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class BuildCommand {
  private static String DEFAULT_CMD = "/bin/bash";
  private static String DEFAULT_ARG = "-c";

  private final String executable;
  private final List<String> args;
  private final List<Integer> returnCodes;
  private final Map<String, String> commandSpecificEnvironment;

  @JsonCreator
  public BuildCommand(@JsonProperty("executable") String executable,
                      @JsonProperty("args") List<String> args,
                      @JsonProperty("returnCodes") List<Integer> returnCodes,
                      @JsonProperty("env") Map<String, String> commandSpecificEnvironment) {
    this.executable = Objects.firstNonNull(executable, DEFAULT_CMD);
    this.args = Objects.firstNonNull(args, Lists.newArrayList(DEFAULT_ARG));
    this.returnCodes = Objects.firstNonNull(returnCodes, Lists.newArrayList(0));
    this.commandSpecificEnvironment = Objects.firstNonNull(commandSpecificEnvironment, new HashMap<String, String>());
  }

  @JsonCreator
  public static BuildCommand buildCommandFromString(String cmd) {
    String executable = DEFAULT_CMD;
    List<String> args = Lists.newArrayList(DEFAULT_ARG, cmd);
    List<Integer> returnCodes = Lists.newArrayList(0);
    Map<String, String> env = new HashMap<String, String>();

    return new BuildCommand(executable, args, returnCodes, env);
  }

  public String getExecutable() {
    return executable;
  }

  public List<String> getArgs() {
    return args;
  }

  public List<Integer> getReturnCodes() {
    return returnCodes;
  }

  public Map<String, String> getCommandSpecificEnvironment() {
    return commandSpecificEnvironment;
  }

  public BuildCommand withDifferentExecutable(String newExecutable) {
    return new BuildCommand(newExecutable, args, returnCodes, commandSpecificEnvironment);
  }
}
