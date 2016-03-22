package com.hubspot.blazar.cctray;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnumValue;
import java.util.Date;

public class CCTrayProject {
  public enum CCTrayActivity {
    @XmlEnumValue("Sleeping") SLEEPING,
    @XmlEnumValue("Building") BUILDING,
    @XmlEnumValue("CheckingModifications") CHECKING_MODIFICATIONS;
  }

  public enum CCTrayStatus {
    @XmlEnumValue("Success") SUCCESS,
    @XmlEnumValue("Failure") FAILURE,
    @XmlEnumValue("Exception") EXCEPTION,
    @XmlEnumValue("Unknown") UNKNOWN;
  }

  private final String name;
  private final CCTrayActivity activity;
  private final CCTrayStatus lastBuildStatus;
  private final String lastBuildLabel;
  private final Date lastBuildTime;
  private final String webUrl;

  // for jaxb
  public CCTrayProject() {
    throw new AssertionError();
  }

  CCTrayProject(String name,
                CCTrayActivity activity,
                CCTrayStatus lastBuildStatus,
                String lastBuildLabel,
                Date lastBuildTime,
                String webUrl) {
    this.name = name;
    this.activity = activity;
    this.lastBuildStatus = lastBuildStatus;
    this.lastBuildLabel = lastBuildLabel;
    this.lastBuildTime = lastBuildTime;
    this.webUrl = webUrl;
  }

  @XmlAttribute
  public String getName() {
    return name;
  }

  @XmlAttribute
  public CCTrayActivity getActivity() {
    return activity;
  }

  @XmlAttribute
  public CCTrayStatus getLastBuildStatus() {
    return lastBuildStatus;
  }

  @XmlAttribute
  public String getLastBuildLabel() {
    return lastBuildLabel;
  }

  @XmlAttribute
  public Date getLastBuildTime() {
    return lastBuildTime;
  }

  @XmlAttribute
  public String getWebUrl() {
    return webUrl;
  }
}
