/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.javaparser;

import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.builder.FunctionTypeBuilder;
import org.mule.metadata.api.builder.ObjectTypeBuilder;
import org.mule.metadata.api.model.FunctionType;
import org.mule.metadata.api.model.MetadataFormat;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.api.model.ObjectType;
import org.mule.metadata.message.api.MessageMetadataType;
import org.mule.metadata.message.api.MessageMetadataTypeBuilder;
import org.mule.metadata.message.api.MuleEventMetadataType;
import org.mule.metadata.message.api.MuleEventMetadataTypeBuilder;
import org.mule.module.apikit.metadata.api.MetadataSource;
import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.module.apikit.metadata.internal.model.ApiCoordinate;
import org.mule.module.apikit.metadata.internal.model.CertificateFields;
import org.mule.module.apikit.metadata.internal.model.HttpRequestAttributesFields;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IResponse;
import org.mule.raml.interfaces.model.parameter.IParameter;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Optional.of;

public class FlowMetadata implements MetadataSource {

  private static final String PARAMETER_INPUT_METADATA = "inputMetadata";

  final private IAction action;
  final private ApiCoordinate coordinate;
  final private Map<String, IParameter> baseUriParameters;
  final private String httpStatusVar;
  final private String outboundHeadersVar;
  final private RamlApiWrapper api;
  final private Notifier notifier;

  public FlowMetadata(final RamlApiWrapper api, final IAction action, final ApiCoordinate coordinate, final Map<String, IParameter> baseUriParameters,
                      final String httpStatusVar,
                      final String outboundHeadersVar, Notifier notifier) {
    this.api = api;
    this.action = action;
    this.coordinate = coordinate;
    this.baseUriParameters = baseUriParameters;
    this.httpStatusVar = httpStatusVar;
    this.outboundHeadersVar = outboundHeadersVar;
    this.notifier = notifier;
  }

  @Override
  public Optional<FunctionType> getMetadata() {
    final MuleEventMetadataType inputEvent = inputMetadata(action, coordinate, baseUriParameters);
    final MuleEventMetadataType outputEvent = outputMetadata(action, coordinate, outboundHeadersVar, httpStatusVar);

    // FunctionType
    final FunctionTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).functionType();
    final FunctionType function = builder
        .addParameterOf(PARAMETER_INPUT_METADATA, inputEvent)
        .returnType(outputEvent)
        .build();

    return of(function);
  }

  private MuleEventMetadataType inputMetadata(final IAction action, final ApiCoordinate coordinate,
                                              final Map<String, IParameter> baseUriParameters) {
    final MessageMetadataType message = new MessageMetadataTypeBuilder()
        .payload(getInputPayload(action, coordinate))
        .attributes(getInputAttributes(action, baseUriParameters)).build();

    return new MuleEventMetadataTypeBuilder().message(message).build();
  }

  private MuleEventMetadataType outputMetadata(final IAction action, final ApiCoordinate coordinate, final String outboundHeadersVar,
                                               String httpStatusVar) {
    final MessageMetadataType message = new MessageMetadataTypeBuilder()
        .payload(getOutputPayload(action, coordinate)).build();

    return new MuleEventMetadataTypeBuilder().message(message)
        .addVariable(outboundHeadersVar, getOutputHeaders(action).build())
        .addVariable(httpStatusVar, MetadataFactory.stringMetadata()).build();
  }

  private ObjectTypeBuilder getOutputHeaders(final IAction action) {
    final Map<String, IParameter> headers = findFirstResponse(action).map(IResponse::getHeaders).orElse(emptyMap());

    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    headers.forEach((name, value) -> builder.addField().key(name.toLowerCase()).value(value.getMetadata()));

    return builder;
  }

  private Optional<IResponse> findFirstResponse(final IAction action) {
    final Optional<IResponse> response = getResponse(action, "200");

    if (response.isPresent())
      return response;

    return action.getResponses().keySet().stream()
        .map(code -> getResponse(action, code))
        .filter(Optional::isPresent).map(Optional::get)
        .findFirst();
  }

  private Optional<IResponse> getResponse(final IAction action, final String statusCode) {
    return Optional.ofNullable(action.getResponses().get(statusCode)).filter(IResponse::hasBody);
  }

  private ObjectTypeBuilder getQueryParameters(IAction action) {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    action.getQueryParameters().forEach(
                                        (key, value) -> builder.addField().key(key).value(value.getMetadata()));

    return builder;
  }

  private ObjectTypeBuilder getInputHeaders(final IAction action) {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    action.getHeaders().forEach(
                                (key, value) -> builder.addField().key(key).value(value.getMetadata()));

    return builder;
  }

  private ObjectType getInputAttributes(final IAction action, final Map<String, IParameter> baseUriParameters) {

    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_CLIENT_CERTIFICATE.getName())
        .required(false)
        .value(getClientCertificate());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_HEADERS.getName())
        .required(true)
        .value(getInputHeaders(action));
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_LISTENER_PATH.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_METHOD.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_QUERY_PARAMS.getName())
        .required(true)
        .value(getQueryParameters(action));
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_QUERY_STRING.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_RELATIVE_PATH.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_REMOTE_ADDRESS.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_REQUEST_PATH.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_REQUEST_URI.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_SCHEME.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_URI_PARAMS.getName())
        .required(true)
        .value(getUriParameters(action, baseUriParameters));
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_VERSION.getName())
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_LOCAL_ADDRESS.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());

    return builder.build();
  }

  private MetadataType getClientCertificate() {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    builder.addField().key(CertificateFields.CLIENT_CERTIFICATE_PUBLIC_KEY.getName()).value(MetadataFactory.objectMetadata());
    builder.addField().key(CertificateFields.CLIENT_CERTIFICATE_TYPE.getName()).value(MetadataFactory.stringMetadata());
    builder.addField().key(CertificateFields.CLIENT_CERTIFICATE_ENCODED.getName()).value(MetadataFactory.binaryMetadata());

    return builder.build();
  }

  private ObjectTypeBuilder getUriParameters(final IAction action, Map<String, IParameter> baseUriParameters) {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    baseUriParameters.forEach((name, parameter) -> builder.addField().key(name).value(parameter.getMetadata()));
    action.getResource().getResolvedUriParameters()
        .forEach((name, parameter) -> builder.addField().key(name).value(parameter.getMetadata()));

    return builder;
  }

  private MetadataType getOutputPayload(final IAction action, final ApiCoordinate coordinate) {
    final Optional<Collection<IMimeType>> mimeTypes = findFirstResponse(action)
        .map(response -> response.getBody().values());

    @Nullable
    final IMimeType mimeType;
    if (mimeTypes.isPresent()) {
      mimeType = mimeTypes.get().stream().findFirst().orElse(null);
    } else {
      mimeType = null;
    }

    return loadIOPayloadMetadata(mimeType, coordinate, api, "output");
  }

  private MetadataType getInputPayload(final IAction action, final ApiCoordinate coordinate) {
    @Nullable
    IMimeType mimeType = null;

    if (action.hasBody()) {
      if (action.getBody().size() == 1) {
        mimeType = action.getBody().values().iterator().next();
      } else if (coordinate.getMediaType() != null) {
        mimeType = action.getBody().get(coordinate.getMediaType());
      }
    }

    return loadIOPayloadMetadata(mimeType, coordinate, api, "input");
  }

  private MetadataType loadIOPayloadMetadata(final IMimeType mimeType, final ApiCoordinate coordinate, final RamlApiWrapper api,
                                             final String payloadDescription) {
    try {
      return MetadataFactory.payloadMetadata(api, mimeType);
    } catch (Exception e) {
      notifier.warn(format("Error while trying to resolve %s payload metadata for flow '%s'.\nDetails: %s", payloadDescription,
                           coordinate.getFlowName(), e.getMessage()));
      return MetadataFactory.defaultMetadata();
    }
  }
}
