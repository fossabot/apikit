/*
 * (c) 2003-2017 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.metadata.api;

import java.util.Optional;
import org.mule.metadata.api.model.FunctionType;
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
  public static class Builder {

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
        return isAmfEnabled ?
            new org.mule.module.apikit.metadata.internal.amfparser.Metadata(applicationModel, resourceLoader, notifier) :
            new org.mule.module.apikit.metadata.internal.javaparser.Metadata(applicationModel, resourceLoader, notifier);
    }
  }
}
