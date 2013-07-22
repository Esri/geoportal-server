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
package com.esri.gpt.catalog.harvest.protocols;

/**
 * host/context pair.
 */
public class HostContextPair {
  private String host;
  private String context;
  
  public HostContextPair(String host) {
    this.host = host;
  }
  
  public HostContextPair(String host, String context) {
    this.host = host;
    this.context = context;
  }
  

  /**
   * Makes host/context pair.
   * @param host host name
   * @return host/context pair
   */
  public static HostContextPair makeHostContextPair(String host) {
    if (host!=null) {
      int index = host.indexOf("/");
      if (index>=0) {
        return new HostContextPair(host.substring(0, index), host.substring(index));
      } else {
        return new HostContextPair(host);
      }
    }

    return null;
  }
  
  public String getHost() {
    return host;
  }
  
  public String getContext() {
    return context;
  }
  
  @Override
  public String toString() {
    return host!=null? host + (context!=null? context: ""): "";
  }
}
