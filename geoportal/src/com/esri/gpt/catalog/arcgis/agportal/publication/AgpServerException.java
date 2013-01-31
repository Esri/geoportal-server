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
 * ArcGIS Portal server exception.
 * This exception represents ArcGIS Portal error found in the server response to
 * any of 'addItem', 'update', or 'delete' request.
 */
public class AgpServerException extends Exception {

  private int code;

  /**
   * Creates a new instance of
   * <code>AgpServerException</code> without detail message.
   *
   * @param code error code
   * @param message error message
   */
  public AgpServerException(int code, String message) {
    super(message);
    this.code = code;
  }

  /**
   * Gets code.
   *
   * @return code
   */
  public int getCode() {
    return code;
  }

  @Override
  public String toString() {
    return "ArcGIS Portal returned error [" + getCode() + "]: " + getMessage();
  }
}
