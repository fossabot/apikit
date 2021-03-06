/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import amf.ProfileName;
import amf.ProfileNames;
import amf.client.AMF;
import amf.client.environment.Environment;
import amf.client.model.document.BaseUnit;
import amf.client.model.document.Document;
import amf.client.model.domain.WebApi;
import amf.client.parse.Oas20Parser;
import amf.client.parse.Parser;
import amf.client.parse.RamlParser;
import amf.client.validate.ValidationReport;
import amf.client.validate.ValidationResult;
import amf.plugins.features.validation.AMFValidatorPlugin;
import amf.plugins.xml.XmlValidationPlugin;
import org.mule.amf.impl.exceptions.ParserException;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.sun.jmx.mbeanserver.Util.cast;
import static org.apache.commons.io.FilenameUtils.getExtension;

public class DocumentParser {

  private DocumentParser() {}

  private static RamlParser ramlParser(Environment environment) {
    return new RamlParser(environment);
  }

  private static Oas20Parser oas20Parser(Environment environment) {
    return new Oas20Parser(environment);
  }

  private static <T, U> U handleFuture(CompletableFuture<T> f) throws ParserException {
    try {
      return (U) f.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new ParserException("An error happened while parsing the api. Message: " + e.getMessage(), e);
    }
  }

  public static Document parseFile(final Parser parser, final URI uri) throws ParserException {
    return parseFile(parser, uri, false);
  }

  public static Document parseFile(final Parser parser, final URI uri, final boolean validate) throws ParserException {
    final Document document = parseFile(parser, URLDecoder.decode(uri.toString()));

    if (validate) {
      final ValidationReport parsingReport = DocumentParser.getParsingReport(parser, ProfileNames.RAML());
      if (!parsingReport.conforms()) {
        final List<ValidationResult> results = parsingReport.results();
        if (!results.isEmpty()) {
          final String message = results.get(0).message();
          throw new ParserException(message);
        }
      }
    }
    return document;
  }

  private static Document parseFile(final Parser parser, final String url) throws ParserException {
    return handleFuture(parser.parseFileAsync(url));
  }

  public static WebApi getWebApi(final URI apiDefinition, Environment environment) throws ParserException {
    return getWebApi(getParserForApi(apiDefinition, environment), apiDefinition);
  }

  public static Parser getParserForApi(final URI apiDefinition, Environment environment) {
    final String ext = getExtension(apiDefinition.getPath());
    return "raml".equalsIgnoreCase(ext) || "yaml".equalsIgnoreCase(ext) || "yml".equalsIgnoreCase(ext) ? ramlParser(environment)
        : oas20Parser(environment);
  }

  private static WebApi getWebApi(final Parser parser, final Path path) throws ParserException {
    return getWebApi(parseFile(parser, path.toUri().toString()));
  }

  public static WebApi getWebApi(final Parser parser, final URI uri) throws ParserException {
    return getWebApi(parseFile(parser, uri));
  }

  public static WebApi getWebApi(final BaseUnit baseUnit) throws ParserException {
    final Document document = cast(AMF.resolveRaml10(baseUnit));
    return cast(document.encodes());
  }

  private static ValidationReport getParsingReport(final Oas20Parser parser) throws ParserException {
    return getParsingReport(parser, ProfileNames.OAS());
  }

  public static ValidationReport getParsingReport(final Parser parser, final ProfileName profile) throws ParserException {
    return handleFuture(parser.reportValidation(profile));
  }

  static {
    try {
      AMF.init().get();
      AMFValidatorPlugin.withEnabledValidation(true);
      amf.core.AMF.registerPlugin(new XmlValidationPlugin());
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }
}
