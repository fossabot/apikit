/*
 * (c) 2003-2017 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.metadata.internal.amfparser;

public enum HttpRequestAttributesFields {

  ATTRIBUTES_QUERY_PARAMS("queryParams"), ATTRIBUTES_HEADERS("headers"), ATTRIBUTES_URI_PARAMS(
      "uriParams"), ATTRIBUTES_LISTENER_PATH(
          "listenerPath"), ATTRIBUTES_RELATIVE_PATH("relativePath"), ATTRIBUTES_VERSION("version"), ATTRIBUTES_SCHEME(
              "scheme"), ATTRIBUTES_METHOD("method"), ATTRIBUTES_REQUEST_URI("requestUri"), ATTRIBUTES_QUERY_STRING(
                  "queryString"), ATTRIBUTES_REMOTE_ADDRESS("remoteAddress"), ATTRIBUTES_CLIENT_CERTIFICATE(
                      "clientCertificate"), ATTRIBUTES_REQUEST_PATH("requestPath"), ATTRIBUTES_LOCAL_ADDRESS("localAddress");

  private String name;

  HttpRequestAttributesFields(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
