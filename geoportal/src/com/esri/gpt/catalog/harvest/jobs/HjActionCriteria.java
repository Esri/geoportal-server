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
package com.esri.gpt.catalog.harvest.jobs;

import com.esri.gpt.framework.request.ActionCriteria;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Harvest job action criteria.
 * @see HjCriteria
 */
public class HjActionCriteria extends ActionCriteria {

// class variables =============================================================

// instance variables ==========================================================
private String[] resourceUuids = new String[]{};
// constructors ================================================================

// properties ==================================================================

/**
 * Gets resource UUIDS.
 * @return the resourceUuids
 */
public String[] getResourceUuids() {
  return resourceUuids;
}

/**
 * Gets resource uuids formated to use in WHERE IN(&lt;collection&gt;) clause.
 * @return resources as &lt;collection&gt;
 */
public String getResourceUuidsForSql() {
  StringBuilder sb = new StringBuilder();
  for (String uuid : resourceUuids) {
    if (uuid.length()>0) {
      if (sb.length()>0) {
        sb.append(",");
      }
      sb.append("'"+uuid+"'");
    }
  }
  return sb.toString();
}
/**
 * Sets resource UUIDS.
 * @param resourceUuids the resourceUuids to set
 */
public void setResourceUuids(String[] resourceUuids) {
  this.resourceUuids = resourceUuids!=null? resourceUuids: new String[]{};
}

// methods =====================================================================

/**
 * Gets host address.
 * @return host address
 */
public String getHostAddress() {
  try {
    return InetAddress.getLocalHost().getHostAddress();
  } catch (UnknownHostException ex) {
    return "";
  }
}

}
