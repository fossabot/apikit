/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.api;

import java.util.Optional;
import org.mule.metadata.api.model.FunctionType;
import org.mule.module.apikit.metadata.internal.amfparser.AmfMetadata;
import org.mule.module.apikit.metadata.internal.javaparser.RamlMetadata;
import org.mule.runtime.config.internal.model.ApplicationModel;

public interface Metadata {

  String MULE_APIKIT_PARSER_AMF = "mule.apikit.parser.amf";

  /**
  * Gets the metadata for a Flow
  * @param flowName Name of the flow
  * @return The Metadata
  */
  public Optional<FunctionType> getMetadataForFlow(final String flowName);

  /**
   * Builder for Metadata module
   */
  class Builder {

    private ResourceLoader resourceLoader;
    private ApplicationModel applicationModel;
    private Notifier notifier;

    public Builder() {

    }

    public Builder withResourceLoader(ResourceLoader resourceLoader) {
      this.resourceLoader = resourceLoader;
      return this;
    }

    public Builder withApplicationModel(ApplicationModel applicationModel) {
      this.applicationModel = applicationModel;
      return this;
    }

    public Builder withNotifier(Notifier notifier) {
      this.notifier = notifier;
      return this;
    }

    public Metadata build() {
      boolean isAmfEnabled = Boolean.getBoolean(MULE_APIKIT_PARSER_AMF);
      return isAmfEnabled ? new AmfMetadata(applicationModel, resourceLoader, notifier)
          : new RamlMetadata(applicationModel, resourceLoader, notifier);
    }
  }
}
