/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.framework.resource.common;

import com.esri.gpt.framework.resource.api.Publishable;
import com.esri.gpt.framework.resource.api.Resource;
import java.util.ArrayList;
import java.util.Date;

/**
 * Common publishable. Has no sub-nodes.
 */
public abstract class CommonPublishable implements Publishable {

  /** empty node list */
  private static final ArrayList<Resource> nodes = new ArrayList<Resource>();

  @Override
  public Iterable<Resource> getNodes() {
    return nodes;
  }

  @Override
  public Date getUpdateDate() {
    return null;
  }
}
