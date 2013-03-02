/*
 * Copyright 2012 Esri.
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
package com.esri.gpt.framework.http;

import java.io.IOException;

/**
 *
 * @author Esri
 */
public class HttpClientException extends IOException {
  private int httpStatusCode;
  
  /**
   * Constructs an instance of
   * <code>HttpClientException</code> with the specified detail message.
   *
   * @param httpStatusCode HTTP status code
   * @param msg the detail message.
   */
  public HttpClientException(int httpStatusCode, String msg) {
    super(msg);
    this.httpStatusCode = httpStatusCode;
  }
  
  public int getHttpStatusCode() {
    return httpStatusCode;
  }
}
