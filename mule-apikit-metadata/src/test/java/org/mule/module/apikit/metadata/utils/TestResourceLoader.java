/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.utils;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import org.mule.module.apikit.metadata.MetadataTestCase;
import org.mule.module.apikit.metadata.api.ResourceLoader;

public class TestResourceLoader implements ResourceLoader {

  @Override
  public File getResource(String relativePath) {
    try {
      URL resource = MetadataTestCase.class.getResource(relativePath);
      if (resource == null)
        return null;
      return new File(resource.toURI());

    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    return null;
  }

}
