/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.metadata.api.model.FunctionType;
import org.mule.metadata.internal.utils.MetadataTypeWriter;
import org.mule.module.apikit.metadata.api.Metadata;
import org.mule.module.apikit.metadata.internal.model.ApplicationModelWrapper;
import org.mule.module.apikit.metadata.internal.model.Flow;
import org.mule.module.apikit.metadata.utils.MockedApplicationModel;
import org.mule.module.apikit.metadata.utils.TestNotifier;
import org.mule.module.apikit.metadata.utils.TestResourceLoader;
import org.mule.runtime.config.internal.model.ApplicationModel;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mule.module.apikit.metadata.api.Metadata.MULE_APIKIT_PARSER_AMF;

@RunWith(Parameterized.class)
public class MetadataTestCase {

  private static final PathMatcher API_MATCHER = FileSystems.getDefault().getPathMatcher("glob:app.xml");

  private static final String AMF_PARSER = "AmfParser";
  private static final String JAVA_PARSER = "JavaParser";

  private String parser;
  private String folderName;
  private File app;
  private Flow flow;

  public MetadataTestCase(final String parser, final String folderName, final File app, final Flow flow) {

    this.parser = parser;
    this.folderName = folderName;
    this.app = app;
    this.flow = flow;
  }

  @Before
  public void beforeTest() {
    final Boolean value = AMF_PARSER.equals(parser) ? Boolean.TRUE : Boolean.FALSE;
    System.setProperty(MULE_APIKIT_PARSER_AMF, value.toString());
  }

  @After
  public void afterTest() {
    System.clearProperty(MULE_APIKIT_PARSER_AMF);
  }

  @Test
  public void checkMetadata() throws Exception {
    final boolean amf = Boolean.getBoolean(MULE_APIKIT_PARSER_AMF);
    final File goldenFile = goldenFile();


    //System.out.println("MetadataTestCase2 [" + parser + "] " + amf + " " + folderName + " " + goldenFile);

    final ApplicationModel applicationModel = createApplicationModel(app);
    assertThat(applicationModel, notNullValue());

    final Optional<FunctionType> metadata = getMetadata(applicationModel, flow);
    assertThat(metadata.isPresent(), is(true));

    final String current = metadataToString(metadata.get());

    final String expected;
    if (!goldenFile.exists()) {

      final Path goldenPath = createGoldenFile(goldenFile, current);
      expected = readFile(goldenPath);
    } else
      expected = readFile(goldenFile.toPath());

    try {
      assertThat(format("Function metadata differ from expected. File: '%s'", goldenFile.getName()), current,
                 is(equalTo(expected)));
    } catch (final AssertionError error) {
      final String name = goldenFile.getName();
      final File folder = goldenFile().getParentFile();
      final File newGoldenFile = new File(folder, name + "-New");
      createGoldenFile(newGoldenFile, current);
      throw error;
    }
  }

  @Parameterized.Parameters(name = "{0}-{1}-{3}")
  public static Collection<Object[]> getData() throws IOException, URISyntaxException {
    final URI baseFolder = MetadataTestCase.class.getResource("").toURI();
    return getData(baseFolder);
  }

  static Collection<Object[]> getData(final URI baseFolder) throws IOException {
    final List<File> apps = scan(baseFolder);

    final List<Object[]> parameters = new ArrayList<>();

    apps.forEach(app -> {
      try {
        final ApplicationModel applicationModel = createApplicationModel(app);

        // Only flow with metadata included
        final List<Flow> flows = ApplicationModelWrapper.findFlows(applicationModel).stream()
            .filter(flow -> hasMetadata(applicationModel, flow)).collect(toList());

        final String folderName = app.getParentFile().getName();

        flows.forEach(flow -> parameters.add(new Object[] {JAVA_PARSER, folderName, app, flow}));
        flows.forEach(flow -> parameters.add(new Object[] {AMF_PARSER, folderName, app, flow}));

      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    return parameters;
  }

  private static boolean hasMetadata(final ApplicationModel applicationModel, final Flow flow) {
    try {
      return getMetadata(applicationModel, flow).isPresent();
    } catch (Exception e) {
      return false;
    }
  }

  private static List<File> scan(final URI resources) throws IOException {

    return Files.walk(Paths.get(resources))
        // .peek(path -> System.out.println("Path:" + path + " isApi:" + API_MATCHER.matches(path.getFileName())))
        .filter(path -> Files.isRegularFile(path) && API_MATCHER.matches(path.getFileName()))
        .map(Path::toFile)
        .collect(toList());
  }

  private static ApplicationModel createApplicationModel(final File app) throws Exception {
    final MockedApplicationModel.Builder builder = new MockedApplicationModel.Builder();
    builder.addConfig("apiKitSample", app);
    final MockedApplicationModel mockedApplicationModel = builder.build();
    return mockedApplicationModel.getApplicationModel();
  }

  private File goldenFile() {
    final String fileName = flow.getName()
        .replace("\\", "")
        .replace(":", "-") + ".out";

    final File parserFolder = new File(app.getParentFile(), parser);
    return new File(parserFolder, fileName);
  }

  private static String readFile(final Path path) {
    try {
      return new String(Files.readAllBytes(path));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Optional<FunctionType> getMetadata(Metadata metadata, Flow flow) {
    return metadata.getMetadataForFlow(flow.getName());
  }

  private static Optional<FunctionType> getMetadata(final ApplicationModel applicationModel, final Flow flow) throws Exception {

    final Metadata metadata = new Metadata.Builder()
        .withApplicationModel(applicationModel)
        .withResourceLoader(new TestResourceLoader())
        .withNotifier(new TestNotifier()).build();

    return metadata.getMetadataForFlow(flow.getName());
  }

  private static String metadataToString(final FunctionType functionType) {
    return new MetadataTypeWriter().toString(functionType);
  }

  private static Path createGoldenFile(final File goldenFile, final String content) throws IOException {

    final String srcPath = goldenFile.getPath().replace("target/test-classes", "src/test/resources");
    final Path goldenPath = Paths.get(srcPath);
    System.out.println("*** Create Golden " + goldenPath);

    // Write golden files  with current values
    final Path parent = goldenPath.getParent();
    if (!Files.exists(parent))
      Files.createDirectory(parent);
    return Files.write(goldenPath, content.getBytes("UTF-8"));
  }
}
