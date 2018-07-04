/*
 * (c) 2003-2017 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.metadata.internal.amfparser.amf;

import amf.client.environment.DefaultEnvironment;
import amf.client.model.document.Document;
import amf.client.model.domain.WebApi;
import amf.client.parse.Parser;
import java.io.File;
import java.net.URI;
import java.util.Optional;
import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.module.apikit.metadata.api.ResourceLoader;
import org.mule.module.apikit.metadata.internal.amfparser.exceptions.ParserException;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class ApiHandler {

  private final ResourceLoader resourceLoader;
  private final Notifier notifier;

  public ApiHandler(final ResourceLoader resourceLoader, final Notifier notifier) {
    this.resourceLoader = resourceLoader;
    this.notifier = notifier;
  }

  public Optional<WebApi> getApi(final String apiDefinition) {
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
