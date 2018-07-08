/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.amfparser;

import amf.client.environment.DefaultEnvironment;
import amf.client.model.document.Document;
import amf.client.model.domain.WebApi;
import amf.client.parse.Parser;
import java.io.File;
import java.net.URI;
import java.util.Optional;
import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.module.apikit.metadata.api.ResourceLoader;
import org.mule.module.apikit.metadata.internal.model.MetadataResolver;
import org.mule.module.apikit.metadata.internal.model.MetadataResolverFactory;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

class AmfHandler implements MetadataResolverFactory {

  private final ResourceLoader resourceLoader;
  private final Notifier notifier;

  public AmfHandler(final ResourceLoader resourceLoader, final Notifier notifier) {
    this.resourceLoader = resourceLoader;
    this.notifier = notifier;
  }

  @Override
  public Optional<MetadataResolver> getMetadataResolver(final String apiDefinition) {
    return getApi(apiDefinition).map(webApi -> new AmfWrapper(webApi, notifier));
  }

  private Optional<WebApi> getApi(final String apiDefinition) {
    try {
      final File file = resourceLoader.getResource(apiDefinition);

      if (file == null) {
        notifier.error(format("API definition '%s' not found.", apiDefinition));
        return empty();
      }

      final URI uri = file.toURI();
      final Parser parser = DocumentParser.getParserForApi(uri, DefaultEnvironment.apply());
      final Document document = DocumentParser.parseFile(parser, uri, true);
      final WebApi webApi = DocumentParser.getWebApi(document);

      return of(webApi);
    } catch (final ParserException e) {
      notifier.error(format("Error reading API definition '%s'. Detail: %s", apiDefinition, e.getMessage()));
    }
    return empty();
  }
}
