/*
 * (c) 2003-2017 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.metadata.internal.model;

import java.util.List;
import java.util.Optional;
import org.mule.module.apikit.metadata.api.Notifier;

public class ApikitConfig {

  final private String name;
  final private String apiDefinition;
  final private List<FlowMapping> flowMappings;
  final private String httpStatusVarName;
  final private String outputHeadersVarName;
  final private Notifier notifier;
  
  final private Optional<MetadataResolver> metadataResolver;

  public ApikitConfig(final String name, final String apiDefinition, List<FlowMapping> flowMappings, 
                      final String httpStatusVarName, final String outputHeadersVarName, final Optional<MetadataResolver> metadataResolver, final Notifier notifier) {
      
    this.name = name;
    this.apiDefinition = apiDefinition;
    this.flowMappings = flowMappings;
    this.httpStatusVarName = httpStatusVarName;
    this.outputHeadersVarName = outputHeadersVarName;
    this.metadataResolver = metadataResolver;
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
     return metadataResolver;
  }
}
