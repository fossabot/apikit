/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
