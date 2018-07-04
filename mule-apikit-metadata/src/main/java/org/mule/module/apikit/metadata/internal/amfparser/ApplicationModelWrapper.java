/*
 * (c) 2003-2017 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.metadata.internal.amfparser;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.module.apikit.metadata.internal.amfparser.amf.ApiHandler;
import org.mule.module.apikit.metadata.internal.amfparser.amf.ApiWrapper;
import org.mule.module.apikit.metadata.internal.model.ApiCoordinate;
import org.mule.module.apikit.metadata.internal.model.ApiCoordinateFactory;
import org.mule.module.apikit.metadata.internal.model.ApikitConfig;
import org.mule.module.apikit.metadata.internal.model.ApikitElementIdentifiers;
import org.mule.module.apikit.metadata.internal.model.Flow;
import org.mule.module.apikit.metadata.internal.model.FlowMapping;
import org.mule.module.apikit.metadata.internal.model.MetadataResolver;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.config.internal.model.ApplicationModel;
import org.mule.runtime.config.internal.model.ComponentModel;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.mule.apikit.common.CollectionUtils.merge;
import static org.mule.runtime.api.component.ComponentIdentifier.buildFromStringRepresentation;

public class ApplicationModelWrapper {

  private final static String PARAMETER_NAME = "name";
  private final static String PARAMETER_API_DEFINITION = "raml";
  private final static String PARAMETER_OUTPUT_HEADERS_VAR = "outboundHeadersMapName";
  private final static String PARAMETER_HTTP_STATUS_VAR = "httpStatusVarName";
  
  private final static String PARAMETER_RESOURCE = "resource";
  private final static String PARAMETER_ACTION = "action";
  private final static String PARAMETER_CONTENT_TYPE = "content-type";
  private final static String PARAMETER_FLOW_REF = "flow-ref";

  private static final ComponentIdentifier FLOW = buildFromStringRepresentation("flow");
  private static final ComponentIdentifier APIKIT_CONFIG = buildFromStringRepresentation("apikit:config");

  private ApplicationModel applicationModel;
  private ApiHandler apiHandler;
  private Notifier notifier;

  private Map<String, ApikitConfig> configMap;
  private Map<String, ApiCoordinate> metadataFlows;

  public ApplicationModelWrapper(final ApplicationModel applicationModel, final ApiHandler apiHandler, final Notifier notifier) {
    this.applicationModel = applicationModel;
    this.apiHandler = apiHandler;
    this.notifier = notifier;
    configMap = loadConfigs();
    metadataFlows = loadFlows();
  }

    private Map<String, ApiCoordinate> loadFlows() {
        // Finding all valid flows
        final List<Flow> flows = findFlows();

        // Creating a Coords Factory, giving the list of all valid config names
        final ApiCoordinateFactory coordsFactory = new ApiCoordinateFactory(getConfigNames());
        final Map<String, ApiCoordinate> conventionCoordinates = createCoordinatesForConventionFlows(flows, coordsFactory);
        final Map<String, ApiCoordinate> flowMappingCoordinates = createCoordinatesForMappingFlows(flows, coordsFactory);

        // Merging both results
        return merge(asList(conventionCoordinates, flowMappingCoordinates));
    }

    private Map<String, ApikitConfig> loadConfigs() {
    return applicationModel.getRootComponentModel().getInnerComponents().stream()
        .filter(ApplicationModelWrapper::isApikitConfig)
        .map(this::createApikitConfig)
        .collect(toMap(ApikitConfig::getName, identity()));
  }

  private Set<String> getConfigNames() {
    return configMap.keySet();
  }

  public Collection<ApikitConfig> getConfigurations() {
      return configMap.values();
  }


    private Map<String, ApiCoordinate> createCoordinatesForConventionFlows(final List<Flow> flows,
                                                                         final ApiCoordinateFactory coordsFactory) {
    return flows
        .stream()
        .map(flow -> coordsFactory.fromFlowName(flow.getName()))
        .filter(Optional::isPresent).map(Optional::get)
        .collect(toMap(ApiCoordinate::getFlowName, identity()));
  }

  private ApikitConfig createApikitConfig(final ComponentModel config) {
    final Map<String, String> parameters = config.getParameters();
    final String configName = parameters.get(PARAMETER_NAME);
    final String apiDefinition = parameters.get(PARAMETER_API_DEFINITION);
      final String outputHeadersVarName = parameters.get(PARAMETER_OUTPUT_HEADERS_VAR);
      final String httpStatusVarName = parameters.get(PARAMETER_HTTP_STATUS_VAR);

      final List<FlowMapping> flowMappings = config.getInnerComponents()
              .stream()
              .filter(cfg -> ApikitElementIdentifiers.isFlowMappings(cfg.getIdentifier()))
              .flatMap(flowMappingsElement -> flowMappingsElement.getInnerComponents().stream())
              .filter(flowMapping -> ApikitElementIdentifiers.isFlowMapping(flowMapping.getIdentifier()))
              .map(unwrappedFlowMapping -> createFlowMapping(configName, unwrappedFlowMapping))
              .collect(toList());
      
      final Optional<MetadataResolver> resolver = apiHandler.getApi(apiDefinition).map(webApi -> new ApiWrapper(webApi, notifier));

      return new ApikitConfig(configName, apiDefinition, flowMappings, httpStatusVarName, outputHeadersVarName, resolver, notifier);
  }

  public List<Flow> findFlows() {
    return applicationModel.getRootComponentModel().getInnerComponents().stream()
        .filter(ApplicationModelWrapper::isFlow)
        .map(this::createFlow)
        .collect(toList());
  }

  private Flow createFlow(ComponentModel componentModel) {
    final Map<String, String> parameters = componentModel.getParameters();
    final String flowName = parameters.get(PARAMETER_NAME);
    return new Flow(flowName);
  }

  public Optional<ApiCoordinate> getApiCoordinate(final String flowName) {
    return ofNullable(metadataFlows.get(flowName));
  }

  public Optional<ApikitConfig> getConfig(final String name) {
    if (configMap.isEmpty()) {
      return empty();
    }

    // If the flow is not explicitly naming the config it belongs, we assume there is only one API
    return Optional.of(configMap.getOrDefault(name, configMap.values().iterator().next()));
  }

    private Map<String, ApiCoordinate> createCoordinatesForMappingFlows(final List<Flow> flows, final ApiCoordinateFactory factory) {
        final Set<String> flowNames = flows.stream().map(Flow::getName).collect(toSet());

        return configMap.values().stream()
                .flatMap(config -> config.getFlowMappings().stream())
                .filter(mapping -> flowNames.contains(mapping.getFlowRef()))
                .map(factory::createFromFlowMapping)
                .collect(toMap(ApiCoordinate::getFlowName, identity()));
    }


    private static boolean isFlow(final ComponentModel component) {
    return component.getIdentifier().equals(FLOW);
  }

  private static boolean isApikitConfig(final ComponentModel component) {
    return component.getIdentifier().equals(APIKIT_CONFIG);
  }

  private static FlowMapping createFlowMapping(final String configName, final ComponentModel component) {
        final Map<String, String> flowMappingParameters = component.getParameters();

        final String resource = flowMappingParameters.get(PARAMETER_RESOURCE);
        final String action = flowMappingParameters.get(PARAMETER_ACTION);
        final String contentType = flowMappingParameters.get(PARAMETER_CONTENT_TYPE);
        final String flowRef = flowMappingParameters.get(PARAMETER_FLOW_REF);

        return new FlowMapping(configName, resource, action, contentType, flowRef);
  }

}