/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.javaparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.module.apikit.metadata.api.ResourceLoader;
import org.mule.module.apikit.metadata.internal.javaparser.RamlApiWrapper;
import org.mule.module.apikit.metadata.internal.model.MetadataResolver;
import org.mule.module.apikit.metadata.internal.model.MetadataResolverFactory;
import org.mule.raml.implv1.parser.visitor.RamlDocumentBuilderImpl;
import org.mule.raml.implv2.v08.model.RamlImpl08V2;
import org.mule.raml.implv2.v10.model.RamlImpl10V2;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.parser.visitor.IRamlDocumentBuilder;
import org.raml.v2.api.RamlModelBuilder;
import org.raml.v2.api.RamlModelResult;
import org.raml.v2.api.loader.CompositeResourceLoader;
import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.FileResourceLoader;

import static java.lang.Boolean.getBoolean;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

class RamlHandler implements MetadataResolverFactory {

  private static final String PARSER_V2_PROPERTY = "apikit.raml.parser.v2";

  private final ResourceLoader resourceLoader;
  private final Notifier notifier;

  public RamlHandler(ResourceLoader resourceLoader, Notifier notifier) {
    this.resourceLoader = resourceLoader;
    this.notifier = notifier;
  }

  @Override
  public Optional<MetadataResolver> getMetadataResolver(String apiDefinition) {
    return getApi(apiDefinition).map(raml -> new RamlApiWrapper(raml, notifier));
  }

  private Optional<IRaml> getApi(String uri) {
    try {
      final File resource = resourceLoader.getResource(uri);

      if (resource == null) {
        notifier.error(format("RAML document '%s' not found.", uri));
        return empty();
      }

      final String content = getRamlContent(resource);
      final Parseable parser = getParser(content);

      return of(parser.build(resource, content));
    } catch (IOException e) {
      notifier.error(format("Error reading RAML document '%s'. Detail: %s", uri, e.getMessage()));
    }

    return empty();
  }

  private Parseable getParser(String ramlContent) {
    return useParserV2(ramlContent) ? new RamlV2Parser() : new RamlV1Parser();
  }

  private String getRamlContent(File uri) throws IOException {
    try (final InputStream is = new FileInputStream(uri)) {
      return IOUtils.toString(is);
    }
  }

  private static boolean useParserV2(String content) {
    return getBoolean(PARSER_V2_PROPERTY) || content.startsWith("#%RAML 1.0");
  }

  static class RamlV1Parser implements Parseable {

    @Override
    public IRaml build(File ramlFile, String ramlContent) {
      final IRamlDocumentBuilder ramlDocumentBuilder = new RamlDocumentBuilderImpl();
      ramlDocumentBuilder.addPathLookupFirst(ramlFile.getParentFile().getPath());
      return ramlDocumentBuilder.build(ramlContent, ramlFile.getName());
    }
  }

  static class RamlV2Parser implements Parseable {

    @Override
    public IRaml build(File ramlFile, String ramlContent) {
      org.raml.v2.api.loader.ResourceLoader resourceLoader =
          new CompositeResourceLoader(new DefaultResourceLoader(), new FileResourceLoader(ramlFile.getParentFile().getPath()));
      RamlModelResult ramlModelResult = new RamlModelBuilder(resourceLoader).buildApi(ramlContent, ramlFile.getPath());
      return wrapApiModel(ramlModelResult);
    }

    private static IRaml wrapApiModel(RamlModelResult ramlModelResult) {
      if (ramlModelResult.hasErrors()) {
        throw new RuntimeException("Invalid RAML descriptor.");
      }
      if (ramlModelResult.isVersion08()) {
        return new RamlImpl08V2(ramlModelResult.getApiV08());
      }
      return new RamlImpl10V2(ramlModelResult.getApiV10());
    }
  }

  /**
   * Interface that wraps a parser implementation
   **/
  public static interface Parseable {

    /**
     * Parses and builds the model for a RAML API
     *
     * @param ramlFile The API
     * @param ramlContent
     * @return The RAML Model
     */
    IRaml build(File ramlFile, String ramlContent);
  }
}
