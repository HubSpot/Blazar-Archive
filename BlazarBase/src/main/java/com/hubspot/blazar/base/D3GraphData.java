package com.hubspot.blazar.base;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class D3GraphData {

  private final List<D3GraphLink> links;
  private final List<D3GraphNode> nodes;

  @JsonCreator
  public D3GraphData(@JsonProperty("links") List<D3GraphLink> links,
                     @JsonProperty("nodes") List<D3GraphNode> nodes) {

    this.links = links;
    this.nodes = nodes;
  }

  public List<D3GraphLink> getLinks() {
    return links;
  }

  public List<D3GraphNode> getNodes() {
    return nodes;
  }
}
