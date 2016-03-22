package com.hubspot.blazar.cctray;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

@XmlRootElement(name = "Projects")
public class CCTrayWrapper {
  private final Set<CCTrayProject> projects;

  // for jaxb
  public CCTrayWrapper() {
    throw new AssertionError();
  }

  public CCTrayWrapper(Set<CCTrayProject> projects) {
    this.projects = projects;
  }

  @XmlElement(name = "Project")
  public Set<CCTrayProject> getProjects() {
    return projects;
  }
}
