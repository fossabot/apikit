/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.javaparser;

import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.module.apikit.metadata.api.ResourceLoader;
import org.mule.module.apikit.metadata.internal.model.AbstractMetadata;
import org.mule.module.apikit.metadata.internal.model.MetadataResolverFactory;
import org.mule.runtime.config.internal.model.ApplicationModel;

public class RamlMetadata extends AbstractMetadata {

  public RamlMetadata(final ApplicationModel model, final ResourceLoader loader, final Notifier notifier) {
    super(model, loader, notifier);
  }

  @Override
  protected MetadataResolverFactory createMetadataResolverFactory(final ResourceLoader loader, final Notifier notifier) {
    return new RamlHandler(loader, notifier);
  }
}
