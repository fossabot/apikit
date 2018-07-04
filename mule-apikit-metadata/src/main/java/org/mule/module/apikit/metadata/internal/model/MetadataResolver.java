package org.mule.module.apikit.metadata.internal.model;

import java.util.Optional;
import org.mule.module.apikit.metadata.api.MetadataSource;

public interface MetadataResolver {
    Optional<MetadataSource> getMetadataSource(ApiCoordinate coordinate, String httpStatusVar, String outboundHeadersVar);
}
