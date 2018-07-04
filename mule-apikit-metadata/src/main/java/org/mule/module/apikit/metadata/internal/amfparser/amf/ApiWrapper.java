/*
 * (c) 2003-2017 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.metadata.internal.amfparser.amf;

import amf.client.model.domain.EndPoint;
import amf.client.model.domain.Operation;
import amf.client.model.domain.Parameter;
import amf.client.model.domain.Server;
import amf.client.model.domain.WebApi;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.mule.module.apikit.metadata.api.MetadataSource;
import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.module.apikit.metadata.internal.amfparser.FlowMetadata;
import org.mule.module.apikit.metadata.internal.model.ApiCoordinate;
import org.mule.module.apikit.metadata.internal.model.MetadataResolver;

import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class ApiWrapper implements MetadataResolver {

  private final Map<String, EndPoint> endPoints;
  private final Map<String, Parameter> baseUriParameters;
  private final Notifier notifier;

  public ApiWrapper(final WebApi webApi, final Notifier notifier) {
    endPoints = webApi.endPoints().stream().collect(toMap(endPoint -> endPoint.path().value(), identity()));
    this.baseUriParameters = baseUriParameters(webApi);
    this.notifier = notifier;
  }

  private Map<String, Parameter> baseUriParameters(final WebApi webApi) {
    final List<Server> servers = webApi.servers();
    if (servers.isEmpty())
      return emptyMap();

    final List<Parameter> variables = webApi.servers().get(0).variables();
    return variables.stream().collect(toMap(parameter -> parameter.name().value(), parameter -> parameter));
  }

  public Optional<MetadataSource> getMetadataSource(final ApiCoordinate coordinate, final String httpStatusVar, final String outboundHeadersVar) {
    final EndPoint endPoint = endPoints.get(coordinate.getPath());
    if (endPoint == null)
      return Optional.empty();

    final Optional<Operation> operation = operation(endPoint, coordinate);
    return operation.map(op -> new FlowMetadata(endPoint, op, coordinate, baseUriParameters, notifier));
  }

  private Optional<Operation> operation(final EndPoint endPoint, final ApiCoordinate coordinate) {
    return endPoint.operations().stream()
        .filter(op -> op.method().value().equalsIgnoreCase(coordinate.getMethod())).findFirst();
  }
}


