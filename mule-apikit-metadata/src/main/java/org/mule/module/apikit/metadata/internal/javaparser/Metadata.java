/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.javaparser;

import org.mule.metadata.api.model.FunctionType;
import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.module.apikit.metadata.api.ResourceLoader;
import org.mule.module.apikit.metadata.internal.javaparser.raml.RamlHandler;
import org.mule.runtime.config.internal.model.ApplicationModel;

import java.util.Optional;

public class Metadata implements org.mule.module.apikit.metadata.api.Metadata {

  private MetadataHandler metadataHandler;

  public Metadata(ApplicationModel applicationModel, ResourceLoader resourceLoader, Notifier notifier) {
    init(applicationModel, resourceLoader, notifier);
  }

  private void init(ApplicationModel applicationModel, ResourceLoader resourceLoader, Notifier notifier) {

    final RamlHandler ramlHandler = new RamlHandler(resourceLoader, notifier);
    final ApplicationModelWrapper wrapper = new ApplicationModelWrapper(applicationModel, ramlHandler, notifier);
    metadataHandler = new MetadataHandler(wrapper, notifier);
  }

  /**
   * Gets the metadata for a Flow
   * @param flowName Name of the flow           
   * @return The Metadata
   */
  public Optional<FunctionType> getMetadataForFlow(String flowName) {      
    return metadataHandler.getMetadataForFlow(flowName);
  }
}
