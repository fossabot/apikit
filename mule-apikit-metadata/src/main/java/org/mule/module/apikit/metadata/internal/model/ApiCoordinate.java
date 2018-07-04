/*
 * (c) 2003-2017 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.metadata.internal.model;

import javax.annotation.Nullable;

/**
 * A Api coordinate. It is composed by a method, a path, a media type (optional), and a APIkit config name (optional).
 */
public class ApiCoordinate {

  final private String flowName;
  final private String method;
  final private String path;
  final private String mediaType;
  final private String configName;

  public ApiCoordinate(String flowName, String method, String path, String mediaType, String configName) {
    this.flowName = flowName;
    this.path = path;
    this.method = method;
    this.mediaType = mediaType;
    this.configName = configName;
  }

  @Nullable
  public String getConfigName() {
    return configName;
  }

  public String getPath() {
    return path;
  }

  @Nullable
  public String getMediaType() {
    return mediaType;
  }

  public String getMethod() {
    return method;
  }

  public String getFlowName() {
    return flowName;
  }
}
