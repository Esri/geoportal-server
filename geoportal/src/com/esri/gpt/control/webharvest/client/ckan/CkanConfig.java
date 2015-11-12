/*
 * Copyright 2015 Esri.
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
package com.esri.gpt.control.webharvest.client.ckan;

import com.esri.gpt.framework.robots.BotsMode;

/**
 * Ckan configuration.
 * <p>
 * It determines various aspects of ckan crawling.
 */
public class CkanConfig {
  private BotsMode mode;
  private String defaultUserAgent;
  private Long rows;
  private boolean skipList;

  public CkanConfig () {
    
  }
  
  public CkanConfig(BotsMode mode, String defaultUserAgent, Long rows, boolean skipList) {
    this.mode = mode;
    this.defaultUserAgent = defaultUserAgent;
    this.rows = rows;
    this.skipList = skipList;
  }
  
  public BotsMode getMode() {
    return mode;
  }

  public void setMode(BotsMode mode) {
    this.mode = mode;
  }

  public String getDefaultUserAgent() {
    return defaultUserAgent;
  }

  public void setDefaultUserAgent(String defaultUserAgent) {
    this.defaultUserAgent = defaultUserAgent;
  }

  public Long getRows() {
    return rows;
  }

  public void setRows(Long rows) {
    this.rows = rows;
  }

  public boolean getSkipList() {
    return skipList;
  }

  public void setSkipList(boolean skipList) {
    this.skipList = skipList;
  }
  
  
}
