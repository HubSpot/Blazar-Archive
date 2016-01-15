package com.hubspot.blazar.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.hubspot.horizon.HttpClient;
import com.hubspot.horizon.HttpConfig;
import com.hubspot.horizon.ning.NingHttpClient;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;

public class BlazarClientConfig {
  private final Optional<HttpClient> httpClient;
  private final Optional<ObjectMapper> objectMapper;
  private final Optional<String> domain;
  private final Optional<String> contextPath;

  private BlazarClientConfig(Optional<HttpClient> httpClient,
                             Optional<ObjectMapper> objectMapper,
                             Optional<String> domain,
                             Optional<String> contextPath) {
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
    this.domain = domain;
    this.contextPath = contextPath;
  }

  public static BlazarClientConfig newBuilder() {
    Optional<String> absent = Optional.absent();
    return new BlazarClientConfig(Optional.<HttpClient>absent(), Optional.<ObjectMapper>absent(), absent, absent);
  }

  public BlazarClientConfig withHttpClient(HttpClient httpClient) {
    return new BlazarClientConfig(Optional.of(httpClient), objectMapper, domain, contextPath);
  }

  public BlazarClientConfig withObjectMapper(ObjectMapper objectMapper) {
    return new BlazarClientConfig(httpClient, Optional.of(objectMapper), domain, contextPath);
  }

  public BlazarClientConfig withDomain(String domain) {
    return new BlazarClientConfig(httpClient, objectMapper, Optional.of(domain), contextPath);
  }

  public BlazarClientConfig withContextPath(String contextPath) {
    return new BlazarClientConfig(httpClient, objectMapper, domain, Optional.of(contextPath));
  }

  public BlazarClient build() {
    Preconditions.checkState(domain.isPresent(), "Domain is required");

    return new BlazarClient(getOrCreateHttpClient(), constructBaseUrl());
  }

  private HttpClient getOrCreateHttpClient() {
    if (httpClient.isPresent()) {
      return httpClient.get();
    } else {
      final ObjectMapper objectMapper;
      if (this.objectMapper.isPresent()) {
        objectMapper = this.objectMapper.get();
      } else {
        objectMapper = new ObjectMapper().registerModule(new GuavaModule()).registerModule(new ProtobufModule());
      }

      return new NingHttpClient(HttpConfig.newBuilder().setObjectMapper(objectMapper).build());
    }
  }

  private String constructBaseUrl() {
    return domain.get() + contextPath.or("/blazar");
  }
}
