package com.hubspot.blazar.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class BuildCommand {

  private final Optional<String> executable;
  private final Optional<List<String>> args;
  private final Optional<List<Integer>> returnCodes;
  private final Optional<Map<String, String>> commandSpecificEnvironment;

  @JsonCreator
  public BuildCommand(@JsonProperty("executable") Optional<String> executable,
                      @JsonProperty("args") Optional<List<String>> args,
                      @JsonProperty("returnCodes") Optional<List<Integer>> returnCodes,
                      @JsonProperty("env") Optional<Map<String, String>> commandSpecificEnvironment) {
    this.executable = executable;
    this.args = args;
    this.returnCodes = returnCodes;
    this.commandSpecificEnvironment = commandSpecificEnvironment;
  }

  @JsonCreator
  public static BuildCommand buildCommandFromString(String cmd) {
    Optional<String> executable = Optional.of("/bin/bash");
    Optional<List<String>> args = Optional.<List<String>>of(Lists.newArrayList("-c", cmd));
    Optional<List<Integer>> returnCodes = Optional.<List<Integer>>of(Lists.newArrayList(0));
    Optional<Map<String, String>> env = Optional.<Map<String,String>>of(new HashMap<String, String>());

    return new BuildCommand(executable, args, returnCodes, env);
  }

  public Optional<String> getExecutable() {
    return executable;
  }

  public Optional<List<String>> getArgs() {
    return args;
  }

  public Optional<List<Integer>> getReturnCodes() {
    return returnCodes;
  }

  public Optional<Map<String, String>> getCommandSpecificEnvironment() {
    return commandSpecificEnvironment;
  }
}
