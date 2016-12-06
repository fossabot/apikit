/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.model.ParameterMap;
import org.mule.runtime.core.util.StringUtils;
//import org.mule.runtime.module.http.internal.ParameterMap;

import java.net.URI;
import java.net.URISyntaxException;

public class HttpProtocolAdapter
{
    public static final String HTTP_METHOD_PROPERTY = "http.method";
    public static final String HTTP_QUERY_PARAMS = "http.query.params";
    public static final String HTTP_REQUEST_PATH_PROPERTY = "http.request.path";

    private String relativePath;
    private URI resourceURI;
    private String method;
    private String acceptableResponseMediaTypes;
    private String requestMediaType;
    private ParameterMap queryParams;

    public HttpProtocolAdapter(Event event)
    {
        Message message = event.getMessage();
        this.relativePath = UrlUtils.getRelativePath(message);
        String hostHeader = ((HttpRequestAttributes)message.getAttributes()).getHeaders().get("host");
        if (hostHeader == null)
        {
            throw new IllegalArgumentException("host header cannot be null");
        }
        String host = hostHeader;
        int port = 80;
        String requestPath = ((HttpRequestAttributes)message.getAttributes()).getRequestPath();
        if (hostHeader.contains(":"))
        {
            host = hostHeader.substring(0, hostHeader.indexOf(':'));
            port = Integer.parseInt(hostHeader.substring(hostHeader.indexOf(':') + 1));
        }
        try
        {
            this.resourceURI = new URI("http", null, host, port, requestPath, null, null);
        }
        catch (URISyntaxException e)
        {
            throw new IllegalArgumentException("Cannot parse URI", e);
        }

        method = ((HttpRequestAttributes)message.getAttributes()).getMethod();

        if (!StringUtils.isBlank((String) ((HttpRequestAttributes)message.getAttributes()).getHeaders().get("accept")))
        {
            this.acceptableResponseMediaTypes = ((HttpRequestAttributes)message.getAttributes()).getHeaders().get("accept");
        }

        if (!StringUtils.isBlank((String)((HttpRequestAttributes)message.getAttributes()).getHeaders().get("content-type")))
        {
            this.requestMediaType = ((HttpRequestAttributes)message.getAttributes()).getHeaders().get("content-type");
        }
        if (this.requestMediaType == null
            && !StringUtils.isBlank((String)((HttpRequestAttributes)message.getAttributes()).getHeaders().get("content-type")))
        {
            this.requestMediaType = ((HttpRequestAttributes)message.getAttributes()).getHeaders().get("content-type");
        }

        this.queryParams = ((HttpRequestAttributes)message.getAttributes()).getQueryParams();
    }

    public String getRelativePath()
    {
        return relativePath;
    }

    public URI getResourceURI()
    {
        return resourceURI;
    }

    public String getMethod()
    {
        return method;
    }

    public String getAcceptableResponseMediaTypes()
    {
        if (acceptableResponseMediaTypes == null)
        {
            return "*/*";
        }
        return acceptableResponseMediaTypes;
    }

    public String getRequestMediaType()
    {
        return requestMediaType != null ? requestMediaType.split(";")[0] : null;
    }

    public ParameterMap getQueryParams()
    {
        return queryParams;
    }
}
