/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.proxy;

//import org.mule.api.lifecycle.InitialisationException;
//import org.mule.module.apikit.ProxyConfiguration;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.tck.junit4.AbstractMuleContextTestCase;

import org.junit.Ignore;
import org.junit.Test;

public class ProxyLocalRamlTestCase extends AbstractMuleContextTestCase
{
    @Ignore
    @Test
    public void ramlInClasspath() throws InitialisationException
    {
        //ProxyConfiguration config = new ProxyConfiguration();
        //config.setRaml("org/mule/module/apikit/proxy/self.raml");
        //config.setMuleContext(muleContext);
        //config.initialise();
    }
}
