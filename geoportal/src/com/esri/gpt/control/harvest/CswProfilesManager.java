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
package com.esri.gpt.control.harvest;

import com.esri.gpt.framework.jsf.SelectItemComparable;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.server.csw.client.CswProfile;
import com.esri.gpt.server.csw.client.CswProfiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.faces.model.SelectItem;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

/**
 * CSW CswProfilesManager manager.
 */
class CswProfilesManager {

/** all known profiles. */  
private static ArrayList<SelectItem> _allProfiles;
static {
  buildAllProfiles();
}

/**
 * Builds all known profiles info.
 */
@SuppressWarnings("unchecked")
private static void buildAllProfiles() {
  if (_allProfiles == null) {
    ArrayList allProfiles = 
      new ArrayList();
   
    CswProfiles cswProfiles = new CswProfiles();
    try {

      cswProfiles.loadProfilefromConfig();
      Collection<CswProfile> profiles = cswProfiles.getProfilesAsCollection();
      for (CswProfile profile : profiles) {
        String id = profile.getId();
        String name = profile.getName();
        if (id.length() > 0) {
          if (name.length() == 0) {
            name = id.toUpperCase();
          }
          allProfiles.add(new SelectItemComparable(id, name));
        }
      }
      Collections.sort(allProfiles);
      _allProfiles = allProfiles;
    } catch (XPathExpressionException ex) {
      LogUtil.getLogger().severe(
        "Error getting CSW profiles: " + ex.getMessage());
    } catch (ParserConfigurationException ex) {
      LogUtil.getLogger().severe(
        "Error getting CSW profiles: " + ex.getMessage());
    } catch (SAXException ex) {
      LogUtil.getLogger().severe(
        "Error getting CSW profiles: " + ex.getMessage());
    } catch (IOException ex) {
      LogUtil.getLogger().severe(
        "Error getting CSW profiles: " + ex.getMessage());
    }
  }
}

/**
 * Gets all select items.
 * @return array of select items.
 */
public ArrayList<SelectItem> getAllItems() {
  return _allProfiles;
}
}
