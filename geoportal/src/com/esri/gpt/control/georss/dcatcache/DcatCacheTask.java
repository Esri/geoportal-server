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
package com.esri.gpt.control.georss.dcatcache;

import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.scheduler.IScheduledTask;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DCAT cache scheduler task.
 * </p>
 * Use it within &lt;scheduler&gt; node in gpt.xml for example:</br>
 * <pre>
 *    &lt;thread class="com.esri.gpt.control.georss.dcatcache.DcatCacheTask" period='1[DAY]' delay="15[SECOND]"/&gt;
 * </pre>
 */
public class DcatCacheTask implements IScheduledTask, Runnable {
  private static final Logger LOGGER = Logger.getLogger(DcatCacheTask.class.getCanonicalName());
  private StringAttributeMap parameters;

  @Override
  public void setParameters(StringAttributeMap parameters) {
    this.parameters = parameters;
  }

  @Override
  public void run() {
    try {
      DcatCacheUpdateRequest request = new DcatCacheUpdateRequest();
      request.execute();
    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, "Error creating DCAT cache through a scheduled task.", ex);
    }
  }
  
}
