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
package com.esri.gpt.framework.context;

import com.esri.gpt.framework.util.Val;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Maps names to different URN's for different domains.
 */
public class UrnMap {
  public static final String URN_ESRI_GPT = "urn:x-esri:gpt";
  
  /** logger */
  protected static final Logger LG = Logger.getLogger(UrnMap.class.getCanonicalName());
  
  /** singleton instance */
  private static UrnMap instance;

  /** Creates instance */
  protected UrnMap() {
  }

  /**
   * Gets instance.
   * @return instance
   */
  public static UrnMap getInstance() {
    if (instance == null) {
      String className = getUrnMapClassName();
      if (className.isEmpty()) {
        instance = new UrnMap();
      } else {
        try {
          Class urnMapClass = Class.forName(getUrnMapClassName());
          instance = (UrnMap) urnMapClass.newInstance();
        } catch (Exception ex) {
          LG.log(Level.INFO, "Unable to create configured instance of UrnMap: " + className, ex);
          instance = new UrnMap();
        }
      }
    }
    return instance;
  }

  /**
   * Gets URN for the domain.
   * @param domain domain
   * @param name URN name
   * @return URN for the name within domain or empty string if no URN found
   */
  public String getUrn(String domain, String name) {
    Map<String, String> withinDomain = getUrnDomainMap().get(domain);
    if (withinDomain != null) {
      return Val.chkStr(withinDomain.get(name));
    }
    return "";
  }

  /**
   * Gets URN domain map.
   * @return URN domain map
   */
  protected Map<String, Map<String, String>> getUrnDomainMap() {
    if (urnDomainMap == null) {
      urnDomainMap = new TreeMap<String, Map<String, String>>();
      urnDomainMap.put("", getDefaultMap());
    }
    return urnDomainMap;
  }

  /**
   * Gets default URN map.
   * @return default URN map
   */
  protected Map<String, String> getDefaultMap() {
    return defaultMap;
  }

  /**
   * Gets configured class name.
   * @return class name or empty string if no configured class name
   */
  private static String getUrnMapClassName() {
    ApplicationContext appCtx = ApplicationContext.getInstance();
    ApplicationConfiguration appCfg = appCtx.getConfiguration();
    return Val.chkStr(appCfg.getCatalogConfiguration().getParameters().getValue("UrnMapClassName"));
  }
  
  private static Map<String, Map<String, String>> urnDomainMap;
  private static final Map<String, String> defaultMap = new TreeMap<String, String>();

  static {
    defaultMap.put("wms", URN_ESRI_GPT+":service:ogc:wms");
    defaultMap.put("wcs", URN_ESRI_GPT+":service:ogc:wcs");
    defaultMap.put("wfs", URN_ESRI_GPT+":service:ogc:wfs");
    defaultMap.put("csw", URN_ESRI_GPT+":service:ogc:csw");
    defaultMap.put("sos", URN_ESRI_GPT+":service:ogc:sos");
    defaultMap.put("kml", URN_ESRI_GPT+":service:ogc:kml");

    defaultMap.put("aims",       URN_ESRI_GPT+":service:arcgis:ims");
    defaultMap.put("ags",        URN_ESRI_GPT+":service:arcgis:ags");
    defaultMap.put("ArcGIS:nmf", URN_ESRI_GPT+":service:arcgis:nmf");
    defaultMap.put("ArcGIS:lyr", URN_ESRI_GPT+":service:arcgis:lyr");
    defaultMap.put("ArcGIS:mxd", URN_ESRI_GPT+":service:arcgis:mxd");
  }
}
