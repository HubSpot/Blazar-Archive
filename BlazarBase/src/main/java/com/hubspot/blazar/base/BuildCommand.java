package com.hubspot.blazar.base;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

public class BuildCommand {
  public static final String DEFAULT_EXECUTABLE = "/bin/bash";
  public static final String DEFAULT_ARG = "-c";
  public static final Set<Integer> DEFAULT_SUCCESSFUL_RETURN_CODES = ImmutableSet.of(0);

  private final String executable;
  private final List<String> args;
  private final Set<Integer> successfulReturnCodes;
  private final Map<String, String> env;

  @JsonCreator
  public BuildCommand(@JsonProperty("executable") Optional<String> executable,
                      @JsonProperty("args") Optional<List<String>> args,
                      @JsonProperty("command") Optional<String> command,
                      @JsonProperty("successfulReturnCodes") Set<Integer> successfulReturnCodes,
                      @JsonProperty("env") Map<String, String> env) {
    if (executable.isPresent()) {
      Preconditions.checkState(!command.isPresent());
      this.executable = executable.get();
      this.args = args.or(Collections.<String>emptyList());
    } else {
      Preconditions.checkState(command.isPresent());
      Preconditions.checkState(!args.isPresent());
      this.executable = DEFAULT_EXECUTABLE;
      this.args = Arrays.asList(DEFAULT_ARG, command.get());
    }

    this.successfulReturnCodes = MoreObjects.firstNonNull(successfulReturnCodes, DEFAULT_SUCCESSFUL_RETURN_CODES);
    this.env = MoreObjects.firstNonNull(env, Collections.<String, String>emptyMap());
  }

  public BuildCommand(String executable, List<String> args) {
    this(executable, args, DEFAULT_SUCCESSFUL_RETURN_CODES, Collections.<String, String>emptyMap());
  }

  public BuildCommand(String executable, List<String> args, Map<String, String> env) {
    this(executable, args, DEFAULT_SUCCESSFUL_RETURN_CODES, env);
  }

  public BuildCommand(String executable, List<String> args, Set<Integer> successfulReturnCodes, Map<String, String> env) {
    this.executable = Preconditions.checkNotNull(executable);
    this.args = Preconditions.checkNotNull(args);
    this.successfulReturnCodes = Preconditions.checkNotNull(successfulReturnCodes);
    this.env = Preconditions.checkNotNull(env);
  }

  @JsonCreator
  public static BuildCommand fromString(String command) {
    return new BuildCommand(
        Optional.<String>absent(),
        Optional.<List<String>>absent(),
        Optional.of(command),
        null,
        null
    );
  }

  public String getExecutable() {
    return executable;
  }

  public List<String> getArgs() {
    return args;
  }

  public Set<Integer> getSuccessfulReturnCodes() {
    return successfulReturnCodes;
  }

  public Map<String, String> getEnv() {
    return env;
  }

  public BuildCommand withDifferentExecutable(String newExecutable) {
    return new BuildCommand(newExecutable, args, successfulReturnCodes, env);
  }
}
