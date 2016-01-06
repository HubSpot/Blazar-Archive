package com.hubspot.blazar.base;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

public class BuildCommand {
  private static final String DEFAULT_CMD = "/bin/bash";
  private static final String DEFAULT_ARG = "-c";
  public static final Set<Integer> DEFAULT_RETURN_CODES = ImmutableSet.of(0);

  private final String executable;
  private final List<String> args;
  private final Set<Integer> returnCodes;
  private final Map<String, String> commandSpecificEnvironment;

  @JsonCreator
  public BuildCommand(@JsonProperty("executable") String executable,
                      @JsonProperty("args") List<String> args,
                      @JsonProperty("returnCodes") Set<Integer> returnCodes,
                      @JsonProperty("env") Map<String, String> commandSpecificEnvironment) {
    this.executable = Objects.firstNonNull(executable, DEFAULT_CMD);
    this.args = Objects.firstNonNull(args, Lists.newArrayList(DEFAULT_ARG));
    this.returnCodes = Objects.firstNonNull(returnCodes, DEFAULT_RETURN_CODES);
    this.commandSpecificEnvironment = Objects.firstNonNull(commandSpecificEnvironment, new HashMap<String, String>());
  }

  @JsonCreator
  public static BuildCommand buildCommandFromString(String cmd) {
    return new BuildCommand(DEFAULT_CMD, Lists.newArrayList(DEFAULT_ARG, cmd), DEFAULT_RETURN_CODES, Collections.EMPTY_MAP);
  }

  public String getExecutable() {
    return executable;
  }

  public List<String> getArgs() {
    return args;
  }

  public Set<Integer> getReturnCodes() {
    return returnCodes;
  }

  @JsonProperty("env")
  public Map<String, String> getCommandSpecificEnvironment() {
    return commandSpecificEnvironment;
  }

  public BuildCommand withDifferentExecutable(String newExecutable) {
    return new BuildCommand(newExecutable, args, returnCodes, commandSpecificEnvironment);
  }
}
