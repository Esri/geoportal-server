/*
 * Copyright 2014 Esri, Inc..
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

package com.esri.gpt.framework.ArcGISOnline;

import static com.esri.gpt.framework.ArcGISOnline.FileType.*;
import com.esri.gpt.framework.ArcGISOnline.Type.ServiceType;
import java.util.ArrayList;
import java.util.List;

/**
 * Types.
 */
public final class Types extends ArrayList<Type> {
  private static Types instance = null;
  
  public static Types getInstance() {
    if (instance==null) {
      instance = new Types();
    }
    return instance;
  }
  
  private Types() {
    
  }
  
  {
    add(new Type("Web Map",DataType.JSON));
    add(new Type("CityEngine Web Scene",DataType.FILE));
    add(new Type("Web Scene",DataType.FILE));
    add(new Type("Pro Map",DataType.FILE));
    
    add(new ServiceType("Feature Service",DataType.JSON));
    add(new ServiceType("Map Service",DataType.JSON));
    add(new ServiceType("Image Service",DataType.JSON));
    add(new Type("KML",DataType.FILE,KMZ));
    add(new Type("WMS",DataType.UNSPECIFED, new WMSServiceTypePredicate()));
    add(new Type("Feature Collection",DataType.JSON));
    add(new Type("Feature Collection Template",DataType.JSON));
    add(new ServiceType("Geodata Service",DataType.UNSPECIFED));
    add(new ServiceType("Globe Service",DataType.UNSPECIFED));
    
    add(new ServiceType("Geometry Service",DataType.UNSPECIFED));
    add(new ServiceType("Geocoding Service",DataType.UNSPECIFED));
    add(new ServiceType("Network Analysis Service",DataType.UNSPECIFED,"NAServer"));
    add(new ServiceType("Geoprocessing Service",DataType.UNSPECIFED));
    add(new Type("Workflow Manager Service",DataType.UNSPECIFED));
    
    add(new Type("Web Mapping Application",DataType.JSON));
    add(new Type("Mobile Application",DataType.JSON));
    add(new Type("Code Attachment",DataType.FILE));
    add(new Type("Operations Dashboard Add In",DataType.FILE, OPDASHBOARDADDIN));
    add(new Type("Operation View",DataType.TEXT));
    
    add(new Type("Symbol Set",DataType.JSON));
    add(new Type("Color Set",DataType.JSON));
    
    add(new Type("Shapefile",DataType.FILE));
    add(new Type("CSV",DataType.FILE,CSV));
    add(new Type("CAD Drawing",DataType.FILE));
    add(new Type("Service Definition",DataType.FILE,SD));
    add(new Type("Document Link",DataType.URL));
    add(new Type("Microsoft Word",DataType.FILE,DOC));
    add(new Type("Microsoft PowerPoint",DataType.FILE,PPT));
    add(new Type("Microsoft Excel",DataType.FILE,XLS));
    add(new Type("PDF",DataType.FILE,PDF));
    add(new Type("Image",DataType.FILE,IMG));
    add(new Type("Visio Document",DataType.FILE,VSD));
    add(new Type("iWork Keynote",DataType.FILE,KEY));
    add(new Type("iWork Pages",DataType.FILE,PAGES));
    add(new Type("iWork Numbers",DataType.FILE,NUMBERS));
    
    add(new Type("Map Document",DataType.FILE,MXD));
    add(new Type("Map Package",DataType.FILE,MPK));
    add(new Type("Basemap Package",DataType.FILE,BPK));
    add(new Type("Tile Package",DataType.FILE,TPK));
    add(new Type("Project Package",DataType.FILE,PPKX));
    add(new Type("Task File",DataType.FILE,ESRITASKS));
    add(new Type("ArcPad Package",DataType.FILE));
    add(new Type("Explorer Map",DataType.FILE,NMF));
    add(new Type("Globe Document",DataType.FILE,GLOBE));
    add(new Type("Scene Document",DataType.FILE,SXD));
    add(new Type("Published Map",DataType.FILE,PMF));
    add(new Type("Map Template",DataType.FILE));
    add(new Type("Windows Mobile Package",DataType.FILE));
    add(new Type("Pro Map",DataType.FILE,MAPX));
    add(new Type("Layout",DataType.FILE,PAGX));
    
    add(new Type("Layer",DataType.FILE,LYR));
    add(new Type("Layer Package",DataType.FILE,LPK));
    add(new Type("Explorer Layer",DataType.FILE,NMC));
    
    add(new Type("Geoprocessing Package",DataType.FILE,GPK));
    add(new Type("Geoprocessing Sample",DataType.FILE));
    add(new Type("Locator Package",DataType.FILE,GCPK));
    add(new Type("Rule Package",DataType.FILE,RPK));

    add(new Type("Workflow Manager Package",DataType.FILE,WPK));
    add(new Type("Desktop Application",DataType.FILE));
    add(new Type("Desktop Application Template",DataType.FILE));
    add(new Type("Code Sample",DataType.FILE));
    add(new Type("Desktop Add In",DataType.FILE,ESRIADDIN));
    add(new Type("Explorer Add In",DataType.FILE,EAZ));
  }
  
  public List<Type> interrogate(String url) {
    ArrayList<Type> matches = new ArrayList<Type>();
    for (Type t: this) {
      if (t.matches(url)) {
        matches.add(t);
      }
    }
    return matches;
  }
}
