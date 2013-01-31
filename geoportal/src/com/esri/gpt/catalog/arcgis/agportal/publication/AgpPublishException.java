/*
 * Copyright 2011 Esri.
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
package com.esri.gpt.catalog.arcgis.agportal.publication;

/**
 * ArcGIS Portal publish exception.
 * This exception represents any failure happen during any of 'addItem', 
 * 'update', or 'delete' request except errors returned by the server.
 */
public class AgpPublishException extends Exception {

  /**
   * Constructs an instance of
   * <code>AgpPublishException</code> with the specified detail message.
   *
   * @param msg the detail message.
   */
  public AgpPublishException(String msg) {
    super(msg);
  }

  /**
   * Constructs an instance of
   * <code>AgpPublishException</code> with the specified detail message.
   *
   * @param msg the detail message.
   * @param t cause
   */
  public AgpPublishException(String msg, Throwable t) {
    super(msg, t);
  }
}
