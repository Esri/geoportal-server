/*
 * Copyright 2013 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.framework.dcat;

/**
 * DCAT parse exception.
 */
public class DcatParseException extends Exception {

  /**
   * Creates a new instance of
   * <code>DcatParseException</code> without detail message.
   */
  public DcatParseException() {
  }

  /**
   * Constructs an instance of
   * <code>DcatParseException</code> with the specified detail message.
   *
   * @param msg the detail message.
   */
  public DcatParseException(String msg) {
    super(msg);
  }

  /**
   * Constructs an instance of
   * <code>DcatParseException</code> with the specified detail message and cause.
   *
   * @param msg the detail message.
   * @param t cause
   */
  public DcatParseException(String msg, Throwable t) {
    super(msg, t);
  }
}
