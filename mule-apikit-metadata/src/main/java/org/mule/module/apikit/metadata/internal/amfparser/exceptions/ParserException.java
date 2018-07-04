/*
 * (c) 2003-2017 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.metadata.internal.amfparser.exceptions;

public class ParserException extends RuntimeException {

  public ParserException(final String message) {
    super(message);
  }

  public ParserException(final String message, Throwable cause) {
    super(message, cause);
  }
}
