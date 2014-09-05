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
  private static Types instance = new Types();
  
  public static Types getInstance() {
    return instance;
  }
  
  private Types() {
    
  }
  
  {
    add(new Type("Web Map",DataType.JSON,DataCategory.MAPS));
    add(new Type("CityEngine Web Scene",DataType.FILE,DataCategory.MAPS));
    add(new Type("Web Scene",DataType.FILE,DataCategory.MAPS));
    add(new Type("Pro Map",DataType.FILE,DataCategory.MAPS));
    
    add(new ServiceType("Feature Service",DataType.JSON,DataCategory.LAYERS));
    add(new ServiceType("Map Service",DataType.JSON,DataCategory.LAYERS));
    add(new ServiceType("Image Service",DataType.JSON,DataCategory.LAYERS));
    add(new Type("KML",DataType.FILE,DataCategory.LAYERS,KMZ));
    add(new Type("WMS",DataType.UNSPECIFED,DataCategory.LAYERS, new WMSServiceTypePredicate()));
    add(new Type("Feature Collection",DataType.JSON,DataCategory.LAYERS));
    add(new Type("Feature Collection Template",DataType.JSON,DataCategory.LAYERS));
    add(new ServiceType("Geodata Service",DataType.UNSPECIFED,DataCategory.LAYERS));
    add(new ServiceType("Globe Service",DataType.UNSPECIFED,DataCategory.LAYERS));
    
    add(new ServiceType("Geometry Service",DataType.UNSPECIFED,DataCategory.TOOLS));
    add(new ServiceType("Geocoding Service",DataType.UNSPECIFED,DataCategory.TOOLS));
    add(new ServiceType("Network Analysis Service",DataType.UNSPECIFED,DataCategory.TOOLS,"NAServer"));
    add(new ServiceType("Geoprocessing Service",DataType.UNSPECIFED,DataCategory.TOOLS,"GPServer"));
    add(new Type("Workflow Manager Service",DataType.UNSPECIFED,DataCategory.TOOLS));
    
    add(new Type("Web Mapping Application",DataType.JSON,DataCategory.APPLICATIONS));
    add(new Type("Mobile Application",DataType.JSON,DataCategory.APPLICATIONS));
    add(new Type("Code Attachment",DataType.FILE,DataCategory.APPLICATIONS));
    add(new Type("Operations Dashboard Add In",DataType.FILE,DataCategory.APPLICATIONS, OPDASHBOARDADDIN));
    add(new Type("Operation View",DataType.TEXT,DataCategory.APPLICATIONS));
    
    add(new Type("Symbol Set",DataType.JSON,DataCategory.DATAFILES));
    add(new Type("Color Set",DataType.JSON,DataCategory.DATAFILES));
    
    add(new Type("Shapefile",DataType.FILE,DataCategory.DATAFILES));
    add(new Type("CSV",DataType.FILE,DataCategory.DATAFILES,CSV));
    add(new Type("CAD Drawing",DataType.FILE,DataCategory.DATAFILES));
    add(new Type("Service Definition",DataType.FILE,DataCategory.DATAFILES,SD));
    add(new Type("Document Link",DataType.URL,DataCategory.DATAFILES));
    add(new Type("Microsoft Word",DataType.FILE,DataCategory.DATAFILES,DOC));
    add(new Type("Microsoft PowerPoint",DataType.FILE,DataCategory.DATAFILES,PPT));
    add(new Type("Microsoft Excel",DataType.FILE,DataCategory.DATAFILES,XLS));
    add(new Type("PDF",DataType.FILE,DataCategory.DATAFILES,PDF));
    add(new Type("Image",DataType.FILE,DataCategory.DATAFILES,IMG));
    add(new Type("Visio Document",DataType.FILE,DataCategory.DATAFILES,VSD));
    add(new Type("iWork Keynote",DataType.FILE,DataCategory.DATAFILES,KEY));
    add(new Type("iWork Pages",DataType.FILE,DataCategory.DATAFILES,PAGES));
    add(new Type("iWork Numbers",DataType.FILE,DataCategory.DATAFILES,NUMBERS));
    
    add(new Type("Map Document",DataType.FILE,DataCategory.MAPS,MXD));
    add(new Type("Map Package",DataType.FILE,DataCategory.MAPS,MPK));
    add(new Type("Basemap Package",DataType.FILE,DataCategory.MAPS,BPK));
    add(new Type("Tile Package",DataType.FILE,DataCategory.MAPS,TPK));
    add(new Type("Project Package",DataType.FILE,DataCategory.MAPS,PPKX));
    add(new Type("Task File",DataType.FILE,DataCategory.MAPS,ESRITASKS));
    add(new Type("ArcPad Package",DataType.FILE,DataCategory.MAPS));
    add(new Type("Explorer Map",DataType.FILE,DataCategory.MAPS,NMF));
    add(new Type("Globe Document",DataType.FILE,DataCategory.MAPS,GLOBE));
    add(new Type("Scene Document",DataType.FILE,DataCategory.MAPS,SXD));
    add(new Type("Published Map",DataType.FILE,DataCategory.MAPS,PMF));
    add(new Type("Map Template",DataType.FILE,DataCategory.MAPS));
    add(new Type("Windows Mobile Package",DataType.FILE,DataCategory.MAPS));
    add(new Type("Pro Map",DataType.FILE,DataCategory.MAPS,MAPX));
    add(new Type("Layout",DataType.FILE,DataCategory.MAPS,PAGX));
    
    add(new Type("Layer",DataType.FILE,DataCategory.LAYERS,LYR));
    add(new Type("Layer Package",DataType.FILE,DataCategory.LAYERS,LPK));
    add(new Type("Explorer Layer",DataType.FILE,DataCategory.LAYERS,NMC));
    
    add(new Type("Geoprocessing Package",DataType.FILE,DataCategory.TOOLS,GPK));
    add(new Type("Geoprocessing Sample",DataType.FILE,DataCategory.TOOLS));
    add(new Type("Locator Package",DataType.FILE,DataCategory.TOOLS,GCPK));
    add(new Type("Rule Package",DataType.FILE,DataCategory.TOOLS,RPK));

    add(new Type("Workflow Manager Package",DataType.FILE,DataCategory.APPLICATIONS,WPK));
    add(new Type("Desktop Application",DataType.FILE,DataCategory.APPLICATIONS));
    add(new Type("Desktop Application Template",DataType.FILE,DataCategory.APPLICATIONS));
    add(new Type("Code Sample",DataType.FILE,DataCategory.APPLICATIONS));
    add(new Type("Desktop Add In",DataType.FILE,DataCategory.APPLICATIONS,ESRIADDIN));
    add(new Type("Explorer Add In",DataType.FILE,DataCategory.APPLICATIONS,EAZ));
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
