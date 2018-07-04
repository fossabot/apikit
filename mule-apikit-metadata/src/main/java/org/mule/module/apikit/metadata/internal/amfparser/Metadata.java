/*
 * (c) 2003-2017 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.metadata.internal.amfparser;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.mule.metadata.api.model.FunctionType;
import org.mule.module.apikit.metadata.api.MetadataSource;
import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.module.apikit.metadata.api.ResourceLoader;
import org.mule.module.apikit.metadata.internal.amfparser.amf.ApiHandler;
import org.mule.module.apikit.metadata.internal.model.ApiCoordinate;
import org.mule.module.apikit.metadata.internal.model.ApikitConfig;
import org.mule.runtime.config.internal.model.ApplicationModel;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Optional.empty;

public class Metadata implements org.mule.module.apikit.metadata.api.Metadata {

  private final ApplicationModelWrapper applicationModel;
  private final Map<String, String> httpStatus; // [{config -> http status var name}]
  private final Map<String, String> outboundHeaders; // [{config -> output header map name}]


    public Metadata(final ApplicationModel model, final ResourceLoader loader, final Notifier notifier) {
    final ApiHandler apiHandler = new ApiHandler(loader, notifier);
    this.applicationModel = new ApplicationModelWrapper(model, apiHandler, notifier);
    this.httpStatus = loadHttpStatusVars(applicationModel);
    this.outboundHeaders = loadOutboundHeaders(applicationModel);
  }

  private static Map<String, String> loadOutboundHeaders(final ApplicationModelWrapper modelWrapper) {
        final Map<String, String> outboundHeaders = new HashMap<>();
        modelWrapper.getConfigurations().forEach(c -> outboundHeaders.put(c.getName(), c.getOutputHeadersVarName()));
        return outboundHeaders;
    }

    private static Map<String, String> loadHttpStatusVars(final ApplicationModelWrapper modelWrapper) {
        final Map<String, String> httpStatusVars = new HashMap<>();
        modelWrapper.getConfigurations().forEach(c -> httpStatusVars.put(c.getName(), c.getHttpStatusVarName()));
        return httpStatusVars;
    }
  
  
  /**
   * Gets the metadata for a Flow
   * @param flowName Name of the flow
   * @return The Metadata
   */
  public Optional<FunctionType> getMetadataForFlow(final String flowName) {
    // Getting the RAML Coordinate for the specified flowName
    final Optional<ApiCoordinate> coordinate = applicationModel.getApiCoordinate(flowName);

    if (!coordinate.isPresent()) {
      return empty();
    }

    final Optional<ApikitConfig> config = applicationModel.getConfig(coordinate.get().getConfigName());

    if (!config.isPresent())
      return empty();

    final String httpStatusVar = httpStatus.get(config.get().getName());
    final String outboundHeadersVar = outboundHeaders.get(config.get().getName());

    if (isNullOrEmpty(httpStatusVar) || isNullOrEmpty(outboundHeadersVar)) {
        return empty();
    }

    // If there exists metadata for the flow, we get the Api
    return config
        .flatMap(ApikitConfig::getMetadataResolver)
        .flatMap(resolver -> resolver.getMetadataSource(coordinate.get(), httpStatusVar, outboundHeadersVar))
        .flatMap(MetadataSource::getMetadata);
  }
}
