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
package com.esri.gpt.control.webharvest.common;

import org.apache.commons.httpclient.HttpClient;

/**
 * Common info.
 */
public class CommonInfo {

  private HttpClient batchHttpClient = new HttpClient();

  /**
   * Gets the underlying Apache HttpClient to be used for batch requests to the
   * same server.
   *
   * @return the batch client
   */
  public HttpClient getBatchHttpClient() {
    return this.batchHttpClient;
  }

  /**
   * Sets the underlying Apache HttpClient to be used for batch requests to the
   * same server.
   *
   * @param batchHttpClient the batch client
   */
  public void setBatchHttpClient(HttpClient batchHttpClient) {
    this.batchHttpClient = batchHttpClient;
  }

  /**
   * Destroys info.
   */
  public void destroy() {
    if (getBatchHttpClient() != null && getBatchHttpClient().getHttpConnectionManager() != null) {
      getBatchHttpClient().getHttpConnectionManager().closeIdleConnections(0);
    }
  }
}
