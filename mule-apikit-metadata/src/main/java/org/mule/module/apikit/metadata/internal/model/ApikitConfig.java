/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.model;

import java.util.List;
import java.util.Optional;
import org.mule.module.apikit.metadata.api.Notifier;

class ApikitConfig {

  final private String name;
  final private String apiDefinition;
  final private List<FlowMapping> flowMappings;
  final private String httpStatusVarName;
  final private String outputHeadersVarName;
  final private Notifier notifier;

  final private MetadataResolverFactory metadataResolverFactory;
  private Optional<MetadataResolver> metadataResolver = null;

  public ApikitConfig(final String name, final String apiDefinition, List<FlowMapping> flowMappings,
                      final String httpStatusVarName, final String outputHeadersVarName,
                      final MetadataResolverFactory metadataResolverFactory, final Notifier notifier) {

    this.name = name;
    this.apiDefinition = apiDefinition;
    this.flowMappings = flowMappings;
    this.httpStatusVarName = httpStatusVarName;
    this.outputHeadersVarName = outputHeadersVarName;
    this.metadataResolverFactory = metadataResolverFactory;
    this.notifier = notifier;
  }

  public String getName() {
    return name;
  }

  /*
  // AMF
  public Optional<ApiWrapper> getApiWrapper() {
    if (apiWrapper == null) {
      apiWrapper = apiHandler.getApi(apiDefinition).map(webApi -> new ApiWrapper(webApi, notifier));
    }
    return apiWrapper;
  }
  
  // and next do -> pi -> api.getMetadataSource(coordinate.get()))
    
    
    /*
     en Java Recibia Supplier<Optional<IRaml>> apiSupplier
     
       public Optional<RamlApiWrapper> getApi() {
    if (ramlApi == null) {
      ramlApi = apiSupplier.get().map(api -> new RamlApiWrapper(api, notifier));
    }
    return ramlApi;
  }
    
    
     */

  public List<FlowMapping> getFlowMappings() {
    return flowMappings;
  }

  public String getHttpStatusVarName() {
    return httpStatusVarName;
  }

  public String getOutputHeadersVarName() {
    return outputHeadersVarName;
  }

  public Optional<MetadataResolver> getMetadataResolver() {
    if (metadataResolver == null) {
      metadataResolver = metadataResolverFactory.getMetadataResolver(apiDefinition);
    }
    return metadataResolver;
  }
}
